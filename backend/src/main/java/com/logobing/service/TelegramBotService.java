package com.logobing.service;

import com.logobing.config.TelegramBotConfig;
import com.logobing.model.DepositRequest;
import com.logobing.model.Player;
import com.logobing.model.WithdrawRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotService {
    
    private final TelegramBotConfig config;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public void sendMessage(String chatId, String text) {
        try {
            String url = TELEGRAM_API_URL + config.getToken() + "/sendMessage";
            Map<String, Object> request = new HashMap<>();
            request.put("chat_id", chatId);
            request.put("text", text);
            request.put("parse_mode", "HTML");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, new HttpHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) log.info("✅ Message sent to {}", chatId);
            else log.error("❌ Failed to send message");
        } catch (Exception e) { log.error("Error sending message: {}", e.getMessage()); }
    }
    
    public void sendAdminMessage(String message) { sendMessage(config.getAdminChatId(), message); }
    
    // Welcome & Registration
    public void sendWelcomeMessage(String chatId, String firstName) {
        sendMessage(chatId, "🎯 <b>Welcome to Logo Bing Bingo!</b> 🎯\n\n👋 Hello " + firstName + "!\n\n📝 Use /register to create account\n💰 Use /deposit to add funds\n🎮 Use /play to start playing\n💸 Use /withdrawal to withdraw winnings\n\nUse /help for all commands");
    }
    
    public void sendRegistrationPrompt(String chatId) {
        sendMessage(chatId, "📝 <b>Registration Required</b>\n\nPlease use /register to complete your registration first.");
    }
    
    // Deposit
    public void sendDepositInstructions(String chatId) {
        String message = "💰 <b>Telebirr Deposit Instructions</b> 💰\n\n━━━━━━━━━━━━━━━━━━━━\n📱 Send to: <code>0931721793</code>\n👤 Name: Logo Bing Bingo\n━━━━━━━━━━━━━━━━━━━━\n\n📝 Steps:\n1) Send money to 0931721793 via Telebirr\n2) Copy the Transaction ID from receipt\n3) Use /deposit command with amount and transaction ID\n\nExample: /deposit 100 TXN123456789 0912345678";
        sendMessage(chatId, message);
    }
    
    // Balance
    public void sendBalanceDetails(String chatId, Player player) {
        String message = "💰 <b>Your Wallet</b> 💰\n\n━━━━━━━━━━━━━━━━━━━━\n👤 Name: " + player.getFirstName() + "\n📱 Phone: " + player.getPhoneNumber() + "\n━━━━━━━━━━━━━━━━━━━━\n💰 Balance: " + String.format("%.2f", player.getWallet()) + " ETB\n💰 Total Deposited: " + String.format("%.2f", player.getTotalDeposited()) + " ETB\n💸 Total Withdrawn: " + String.format("%.2f", player.getTotalWithdrawn()) + " ETB\n🏆 Total Won: " + String.format("%.2f", player.getTotalWon()) + " ETB\n🎮 Games: " + player.getGamesPlayed() + " played, " + player.getGamesWon() + " won\n━━━━━━━━━━━━━━━━━━━━";
        sendMessage(chatId, message);
    }
    
    // Game
    public void sendGameLaunch(String chatId, Player player) {
        String message = "🎮 <b>Logo Bing Game</b> 🎮\n\n👤 Player: " + player.getFirstName() + "\n💰 Balance: " + String.format("%.2f", player.getWallet()) + " ETB\n\nClick below to launch the game!";
        sendMessage(chatId, message);
    }
    
    public void sendGameInstructions(String chatId) {
        String message = "🎯 <b>How to Play Logo Bing</b> 🎯\n\n1️⃣ Register with /register\n2️⃣ Deposit with /deposit\n3️⃣ Select a Bingo card (1-500)\n4️⃣ Mark called numbers\n5️⃣ Complete a row/column/diagonal\n6️⃣ Click BINGO to win!\n\n💡 Tip: Wait for at least 4 numbers before claiming!";
        sendMessage(chatId, message);
    }
    
    // Help
    public void sendHelpMenu(String chatId, boolean isRegistered, boolean isAdmin) {
        StringBuilder msg = new StringBuilder("📚 <b>Logo Bing Help</b>\n\n");
        msg.append("/start - Welcome\n/register - Register\n/instruction - Game rules\n");
        if (isRegistered) {
            msg.append("/play - Launch game\n/deposit - Deposit funds\n/withdrawal - Withdraw\n/balance - Check balance\n/transfer - Transfer funds\n");
        }
        if (isAdmin) {
            msg.append("\n👑 <b>Admin Commands:</b>\n");
            msg.append("/admin - Dashboard\n/pending_deposits - List deposits\n/pending_withdrawals - List withdrawals\n");
            msg.append("/approve_deposit [ID] - Approve deposit\n/reject_deposit [ID] [reason] - Reject deposit\n");
            msg.append("/approve_withdraw [ID] - Approve withdrawal\n/reject_withdraw [ID] [reason] - Reject withdrawal\n");
            msg.append("/stats - Statistics\n");
        }
        sendMessage(chatId, msg.toString());
    }
    
    // Admin
    public void sendAdminDashboard(Map<String, Object> stats) {
        String msg = "👑 <b>Admin Dashboard</b>\n\n━━━━━━━━━━━━━━━━━━━━\n📊 Pending:\n💰 Deposits: " + stats.get("pendingDeposits") + "\n💸 Withdrawals: " + stats.get("pendingWithdrawals") + "\n━━━━━━━━━━━━━━━━━━━━\n📈 Today:\n💰 Deposits: " + stats.get("todayDeposits") + " ETB\n👥 New Users: " + stats.get("newUsersToday") + "\n━━━━━━━━━━━━━━━━━━━━\n👥 Total: " + stats.get("totalPlayers") + " players\n📱 Admin Telebirr: <code>0931721793</code>";
        sendAdminMessage(msg);
    }
    
    public void sendPendingDepositsList(List<DepositRequest> deposits) {
        if (deposits.isEmpty()) { sendAdminMessage("✅ No pending deposits."); return; }
        StringBuilder msg = new StringBuilder("📋 <b>Pending Deposits</b>\n\n");
        for (DepositRequest d : deposits) {
            msg.append("━━━━━━━━━━━━━━━━━━━━\n");
            msg.append("📝 ID: <code>").append(d.getRequestId()).append("</code>\n");
            msg.append("👤 Player: ").append(d.getPlayer().getFirstName()).append("\n");
            msg.append("💰 Amount: ").append(d.getAmount()).append(" ETB\n");
            msg.append("🔢 TXN: <code>").append(d.getTransactionId()).append("</code>\n");
            msg.append("📱 From: ").append(d.getSenderPhoneNumber()).append("\n");
            msg.append("⏰ Time: ").append(d.getCreatedAt().toString()).append("\n\n");
        }
        sendAdminMessage(msg.toString());
    }
    
    public void sendPendingWithdrawalsList(List<WithdrawRequest> withdrawals) {
        if (withdrawals.isEmpty()) { sendAdminMessage("✅ No pending withdrawals."); return; }
        StringBuilder msg = new StringBuilder("📋 <b>Pending Withdrawals</b>\n\n");
        for (WithdrawRequest w : withdrawals) {
            msg.append("━━━━━━━━━━━━━━━━━━━━\n");
            msg.append("📝 ID: <code>").append(w.getRequestId()).append("</code>\n");
            msg.append("👤 Player: ").append(w.getPlayer().getFirstName()).append("\n");
            msg.append("💰 Amount: ").append(w.getAmount()).append(" ETB\n");
            msg.append("📱 Send to: ").append(w.getRecipientPhoneNumber()).append("\n");
            msg.append("👤 Name: ").append(w.getRecipientName()).append("\n");
            msg.append("💵 Balance: ").append(w.getPlayer().getWallet()).append(" ETB\n");
            msg.append("⏰ Time: ").append(w.getCreatedAt().toString()).append("\n\n");
        }
        sendAdminMessage(msg.toString());
    }
    
    public void sendNewDepositNotification(DepositRequest request) {
        String msg = "💰 <b>NEW DEPOSIT - Manual Verification Required</b>\n\n📝 ID: " + request.getRequestId() + "\n👤 Player: " + request.getPlayer().getFirstName() + "\n💰 Amount: " + request.getAmount() + " ETB\n🔢 TXN: " + request.getTransactionId() + "\n📱 From: " + request.getSenderPhoneNumber() + "\n\nUse /approve_deposit " + request.getRequestId() + " to approve";
        sendAdminMessage(msg);
    }
    
    public void sendNewWithdrawalNotification(WithdrawRequest request) {
        String msg = "💸 <b>NEW WITHDRAWAL - Manual Processing Required</b>\n\n📝 ID: " + request.getRequestId() + "\n👤 Player: " + request.getPlayer().getFirstName() + "\n💰 Amount: " + request.getAmount() + " ETB\n📱 Send to: " + request.getRecipientPhoneNumber() + "\n👤 Name: " + request.getRecipientName() + "\n💵 Balance: " + request.getPlayer().getWallet() + " ETB\n\nUse /approve_withdraw " + request.getRequestId() + " to approve";
        sendAdminMessage(msg);
    }
    
    public void sendDepositApproved(String chatId, double amount, double newBalance) {
        sendMessage(chatId, "✅ <b>DEPOSIT APPROVED!</b>\n\n💰 Amount: " + amount + " ETB\n💵 New Balance: " + String.format("%.2f", newBalance) + " ETB\n\nUse /play to start playing!");
    }
    
    public void sendDepositRejected(String chatId, double amount, String reason) {
        sendMessage(chatId, "❌ <b>DEPOSIT REJECTED</b>\n\n💰 Amount: " + amount + " ETB\n📌 Reason: " + reason + "\n\nPlease contact support.");
    }
    
    public void sendWithdrawalApproved(String chatId, double amount, String reference) {
        sendMessage(chatId, "✅ <b>WITHDRAWAL COMPLETED!</b>\n\n💰 Amount: " + amount + " ETB\n🔢 Reference: " + reference + "\n\nFunds sent to your Telebirr!");
    }
    
    public void sendWithdrawalRejected(String chatId, double amount, String reason) {
        sendMessage(chatId, "❌ <b>WITHDRAWAL REJECTED</b>\n\n💰 Amount: " + amount + " ETB\n📌 Reason: " + reason + "\n\nPlease contact support.");
    }
    
    public void sendRegistrationSuccess(String chatId, Player player) {
        sendMessage(chatId, "✅ <b>Registration Successful!</b>\n\n📱 Phone: " + player.getPhoneNumber() + "\n👤 Name: " + player.getFirstName() + "\n💰 Balance: " + player.getWallet() + " ETB\n\nUse /deposit to add funds or /play to start!");
    }
}
