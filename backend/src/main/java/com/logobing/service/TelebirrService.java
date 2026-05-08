package com.logobing.service;

import com.logobing.model.*;
import com.logobing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelebirrService {
    
    private final DepositRequestRepository depositRequestRepository;
    private final WithdrawRequestRepository withdrawRequestRepository;
    private final TransactionRepository transactionRepository;
    private final PlayerRepository playerRepository;
    private final TelegramBotService botService;
    
    private static final Double MIN_WITHDRAW = 10.0;
    private static final Double MIN_DEPOSIT = 10.0;
    private static final Double MAX_TRANSACTION = 50000.0;
    private static final String ADMIN_PHONE = "0931721793";
    
    // ==================== DEPOSIT METHODS ====================
    
    @Transactional
    public DepositRequest createDepositRequest(String playerId, Double amount, String transactionId, String senderPhone) {
        validateDeposit(amount);
        Player player = getValidatedPlayer(playerId);
        
        if (isDuplicateTransaction(transactionId)) {
            throw new RuntimeException("Transaction ID already submitted");
        }
        
        DepositRequest request = createNewDepositRequest(player, amount, transactionId, senderPhone);
        DepositRequest saved = depositRequestRepository.save(request);
        
        sendDepositConfirmations(player, saved);
        log.info("💰 Deposit request: {} - {} ETB", saved.getRequestId(), amount);
        return saved;
    }
    
    private void validateDeposit(Double amount) {
        if (amount < MIN_DEPOSIT) throw new RuntimeException("Minimum deposit is " + MIN_DEPOSIT + " ETB");
        if (amount > MAX_TRANSACTION) throw new RuntimeException("Maximum deposit is " + MAX_TRANSACTION + " ETB");
    }
    
    private Player getValidatedPlayer(String playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        if (!player.getIsRegistered()) {
            throw new RuntimeException("Please complete registration first");
        }
        return player;
    }
    
    private boolean isDuplicateTransaction(String transactionId) {
        return depositRequestRepository.findAll().stream()
                .anyMatch(req -> transactionId.equals(req.getTransactionId()) && 
                        ("PENDING".equals(req.getStatus()) || "COMPLETED".equals(req.getStatus())));
    }
    
    private DepositRequest createNewDepositRequest(Player player, Double amount, String txnId, String senderPhone) {
        DepositRequest request = new DepositRequest();
        request.setPlayer(player);
        request.setAmount(amount);
        request.setTransactionId(txnId);
        request.setSenderPhoneNumber(senderPhone);
        request.setReceiverPhoneNumber(ADMIN_PHONE);
        request.setStatus("PENDING");
        return request;
    }
    
    private void sendDepositConfirmations(Player player, DepositRequest request) {
        botService.sendMessage(player.getId(), 
            "✅ Deposit request submitted!\n\n📝 ID: " + request.getRequestId() + 
            "\n💰 Amount: " + request.getAmount() + " ETB\n🔢 TXN: " + request.getTransactionId() + 
            "\n\n⏳ Admin will verify within 5-15 minutes.");
        botService.sendNewDepositNotification(request);
    }
    
    @Transactional
    public void approveDeposit(String requestId, Long adminId) {
        DepositRequest request = depositRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request already processed");
        }
        
        Player player = request.getPlayer();
        
        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setPlayer(player);
        transaction.setType("DEPOSIT");
        transaction.setAmount(request.getAmount());
        transaction.setPreviousBalance(player.getWallet());
        transaction.setNewBalance(player.getWallet() + request.getAmount());
        transaction.setStatus("COMPLETED");
        transaction.setPaymentMethod("TELEBIRR");
        transaction.setPaymentReference(request.getTransactionId());
        transaction.complete();
        transactionRepository.save(transaction);
        
        // Update wallet
        player.addDeposit(request.getAmount());
        playerRepository.save(player);
        
        // Update request
        request.approve(adminId);
        depositRequestRepository.save(request);
        
        // Send notifications
        botService.sendDepositApproved(player.getId(), request.getAmount(), player.getWallet());
        botService.sendAdminMessage("✅ Deposit approved: " + requestId + " - " + request.getAmount() + " ETB");
        
        // First deposit bonus
        checkFirstDepositBonus(player, request.getAmount());
        
        log.info("✅ Deposit approved: {} - {} ETB", requestId, request.getAmount());
    }
    
    private void checkFirstDepositBonus(Player player, Double depositAmount) {
        Double totalDeposits = transactionRepository.sumDepositsByPlayer(player);
        if (totalDeposits != null && Math.abs(totalDeposits - depositAmount) < 0.01) {
            double bonus = depositAmount * 0.10;
            if (bonus > 0 && bonus <= 500) {
                Transaction bonusTxn = new Transaction();
                bonusTxn.setPlayer(player);
                bonusTxn.setType("BONUS");
                bonusTxn.setAmount(bonus);
                bonusTxn.setPreviousBalance(player.getWallet());
                bonusTxn.setNewBalance(player.getWallet() + bonus);
                bonusTxn.setStatus("COMPLETED");
                bonusTxn.setDescription("Welcome bonus - 10% of first deposit");
                bonusTxn.complete();
                transactionRepository.save(bonusTxn);
                
                player.addToWallet(bonus);
                playerRepository.save(player);
                
                botService.sendMessage(player.getId(), 
                    "🎉 WELCOME BONUS!\n\nYou received " + bonus + " ETB bonus!\n💰 New balance: " + player.getWallet() + " ETB");
            }
        }
    }
    
    @Transactional
    public void rejectDeposit(String requestId, String reason, Long adminId) {
        DepositRequest request = depositRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        
        request.reject(reason, adminId);
        depositRequestRepository.save(request);
        
        botService.sendDepositRejected(request.getPlayer().getId(), request.getAmount(), reason);
        botService.sendAdminMessage("❌ Deposit rejected: " + requestId + " - " + reason);
        
        log.info("❌ Deposit rejected: {} - {}", requestId, reason);
    }
    
    // ==================== WITHDRAWAL METHODS ====================
    
    @Transactional
    public WithdrawRequest createWithdrawRequest(String playerId, Double amount, String phone, String name) {
        validateWithdrawal(amount);
        Player player = getValidatedPlayer(playerId);
        
        if (!player.hasSufficientFunds(amount)) {
            throw new RuntimeException("Insufficient balance. Available: " + player.getWallet() + " ETB");
        }
        
        WithdrawRequest request = createNewWithdrawRequest(player, amount, phone, name);
        WithdrawRequest saved = withdrawRequestRepository.save(request);
        
        sendWithdrawConfirmations(player, saved);
        log.info("💸 Withdraw request: {} - {} ETB", saved.getRequestId(), amount);
        return saved;
    }
    
    private void validateWithdrawal(Double amount) {
        if (amount < MIN_WITHDRAW) throw new RuntimeException("Minimum withdrawal is " + MIN_WITHDRAW + " ETB");
        if (amount > MAX_TRANSACTION) throw new RuntimeException("Maximum withdrawal is " + MAX_TRANSACTION + " ETB");
    }
    
    private WithdrawRequest createNewWithdrawRequest(Player player, Double amount, String phone, String name) {
        WithdrawRequest request = new WithdrawRequest();
        request.setPlayer(player);
        request.setAmount(amount);
        request.setRecipientPhoneNumber(phone);
        request.setRecipientName(name);
        request.setStatus("PENDING");
        return request;
    }
    
    private void sendWithdrawConfirmations(Player player, WithdrawRequest request) {
        botService.sendMessage(player.getId(), 
            "📤 Withdrawal request submitted!\n\n📝 ID: " + request.getRequestId() + 
            "\n💰 Amount: " + request.getAmount() + " ETB\n📱 To: " + request.getRecipientPhoneNumber() + 
            "\n\n⏳ Admin will process within 5-30 minutes.");
        botService.sendNewWithdrawalNotification(request);
    }
    
    @Transactional
    public void approveWithdrawal(String requestId, String reference, Long adminId) {
        WithdrawRequest request = withdrawRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request already processed");
        }
        
        Player player = request.getPlayer();
        
        if (!player.hasSufficientFunds(request.getAmount())) {
            rejectWithdrawal(requestId, "Insufficient balance", adminId);
            throw new RuntimeException("Insufficient balance");
        }
        
        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setPlayer(player);
        transaction.setType("WITHDRAW");
        transaction.setAmount(request.getAmount());
        transaction.setPreviousBalance(player.getWallet());
        transaction.setNewBalance(player.getWallet() - request.getAmount());
        transaction.setStatus("COMPLETED");
        transaction.setPaymentMethod("TELEBIRR");
        transaction.setPaymentReference(reference);
        transaction.complete();
        transactionRepository.save(transaction);
        
        // Update wallet
        player.addWithdraw(request.getAmount());
        playerRepository.save(player);
        
        // Update request
        request.approve(adminId, reference);
        withdrawRequestRepository.save(request);
        
        // Send notifications
        botService.sendWithdrawalApproved(player.getId(), request.getAmount(), reference);
        botService.sendAdminMessage("✅ Withdrawal approved: " + requestId + " - " + request.getAmount() + " ETB");
        
        log.info("✅ Withdrawal approved: {} - {} ETB", requestId, request.getAmount());
    }
    
    @Transactional
    public void rejectWithdrawal(String requestId, String reason, Long adminId) {
        WithdrawRequest request = withdrawRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        
        request.reject(reason, adminId);
        withdrawRequestRepository.save(request);
        
        botService.sendWithdrawalRejected(request.getPlayer().getId(), request.getAmount(), reason);
        botService.sendAdminMessage("❌ Withdrawal rejected: " + requestId + " - " + reason);
        
        log.info("❌ Withdrawal rejected: {} - {}", requestId, reason);
    }
    
    // ==================== HELPER METHODS ====================
    
    public List<DepositRequest> getPendingDeposits() {
        return depositRequestRepository.findByStatus("PENDING");
    }
    
    public List<WithdrawRequest> getPendingWithdrawals() {
        return withdrawRequestRepository.findByStatus("PENDING");
    }
    
    public DepositRequest getDepositRequest(String requestId) {
        return depositRequestRepository.findByRequestId(requestId).orElse(null);
    }
    
    public WithdrawRequest getWithdrawRequest(String requestId) {
        return withdrawRequestRepository.findByRequestId(requestId).orElse(null);
    }
    
    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("pendingDeposits", getPendingDeposits().size());
        dashboard.put("pendingWithdrawals", getPendingWithdrawals().size());
        dashboard.put("adminTelebirrNumber", ADMIN_PHONE);
        dashboard.put("totalPlayers", playerRepository.count());
        dashboard.put("registeredPlayers", playerRepository.countRegisteredPlayers());
        return dashboard;
    }
}
