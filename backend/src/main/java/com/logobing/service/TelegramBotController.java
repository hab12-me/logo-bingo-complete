package com.logobing.service;

import com.logobing.model.*;
import com.logobing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotController {
    
    private final PlayerRepository playerRepository;
    private final TransactionRepository transactionRepository;
    private final TelegramBotService botService;
    private final PaymentService paymentService;
    private final UserRegistrationService registrationService;
    private final TelebirrService telebirrService;
    
    private final Map<String, UserSession> sessions = new HashMap<>();
    
    public void handleMessage(String chatId, String text, String username, String firstName, String lastName) {
        String cmd = text.toLowerCase().trim();
        
        if (sessions.containsKey(chatId)) {
            handleSession(chatId, text);
            return;
        }
        
        switch (cmd) {
            case "/start": handleStart(chatId, firstName, username); break;
            case "/register": handleRegister(chatId, firstName, username); break;
            case "/play": handlePlay(chatId); break;
            case "/deposit": handleDeposit(chatId); break;
            case "/withdrawal": handleWithdrawal(chatId); break;
            case "/balance": handleBalance(chatId); break;
            case "/instruction": botService.sendGameInstructions(chatId); break;
            case "/help": handleHelp(chatId); break;
            case "/admin": handleAdmin(chatId); break;
            case "/pending_deposits": handlePendingDeposits(chatId); break;
            case "/pending_withdrawals": handlePendingWithdrawals(chatId); break;
            case "/stats": handleStats(chatId); break;
            default:
                if (cmd.startsWith("/approve_deposit")) handleApproveDeposit(chatId, text);
                else if (cmd.startsWith("/reject_deposit")) handleRejectDeposit(chatId, text);
                else if (cmd.startsWith("/approve_withdraw")) handleApproveWithdraw(chatId, text);
                else if (cmd.startsWith("/reject_withdraw")) handleRejectWithdraw(chatId, text);
                else handleUnknown(chatId);
                break;
        }
    }
    
    private void handleStart(String chatId, String firstName, String username) {
        botService.sendWelcomeMessage(chatId, firstName);
    }
    
    private void handleRegister(String chatId, String firstName, String username) {
        Player existing = playerRepository.findById(chatId).orElse(null);
        if (existing != null && existing.getIsRegistered()) {
            botService.sendMessage(chatId, "✅ You are already registered!\n\nUse /balance to check your wallet.");
            return;
        }
        
        UserSession session = new UserSession();
        session.command = "REGISTER";
        session.step = 1;
        session.data.put("firstName", firstName);
        session.data.put("username", username);
        sessions.put(chatId, session);
        
        botService.sendMessage(chatId, "📝 <b>Registration - Step 1 of 3</b>\n\nPlease enter your Ethiopian phone number.\n\nFormat: 09XXXXXXXX\nExample: 0912345678\n\nType /cancel to cancel.");
    }
    
    private void handlePlay(String chatId) {
        Player player = playerRepository.findById(chatId).orElse(null);
        if (player == null || !player.getIsRegistered()) {
            botService.sendRegistrationPrompt(chatId);
            return;
        }
        botService.sendGameLaunch(chatId, player);
    }
    
    private void handleDeposit(String chatId) {
        Player player = playerRepository.findById(chatId).orElse(null);
        if (player == null || !player.getIsRegistered()) {
            botService.sendRegistrationPrompt(chatId);
            return;
        }
        botService.sendDepositInstructions(chatId);
        
        UserSession session = new UserSession();
        session.command = "DEPOSIT";
        session.step = 1;
        sessions.put(chatId, session);
        
        botService.sendMessage(chatId, "💰 Please enter the amount you want to deposit (minimum 10 ETB):\n\nExample: 100");
    }
    
    private void handleWithdrawal(String chatId) {
        Player player = playerRepository.findById(chatId).orElse(null);
        if (player == null || !player.getIsRegistered()) {
            botService.sendRegistrationPrompt(chatId);
            return;
        }
        if (player.getWallet() < 10) {
            botService.sendMessage(chatId, "❌ Insufficient balance! Minimum withdrawal is 10 ETB.\n\nYour balance: " + player.getWallet() + " ETB");
            return;
        }
        
        UserSession session = new UserSession();
        session.command = "WITHDRAW";
        session.step = 1;
        session.data.put("currentBalance", player.getWallet());
        sessions.put(chatId, session);
        
        botService.sendMessage(chatId, "💸 Enter amount to withdraw (minimum 10 ETB, max " + player.getWallet() + " ETB):\n\nExample: 100");
    }
    
    private void handleBalance(String chatId) {
        Player player = playerRepository.findById(chatId).orElse(null);
        if (player == null || !player.getIsRegistered()) {
            botService.sendRegistrationPrompt(chatId);
            return;
        }
        botService.sendBalanceDetails(chatId, player);
    }
    
    private void handleHelp(String chatId) {
        Player p = playerRepository.findById(chatId).orElse(null);
        botService.sendHelpMenu(chatId, p != null && p.getIsRegistered(), p != null && p.getIsAdmin());
    }
    
    private void handleAdmin(String chatId) {
        Player p = playerRepository.findById(chatId).orElse(null);
        if (p == null || !p.getIsAdmin()) { botService.sendMessage(chatId, "❌ Admin only"); return; }
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingDeposits", telebirrService.getPendingDeposits().size());
        stats.put("pendingWithdrawals", telebirrService.getPendingWithdrawals().size());
        stats.put("todayDeposits", transactionRepository.sumTodayDeposits());
        stats.put("newUsersToday", 0L);
        stats.put("totalPlayers", playerRepository.count());
        botService.sendAdminDashboard(stats);
    }
    
    private void handlePendingDeposits(String chatId) {
        Player p = playerRepository.findById(chatId).orElse(null);
        if (p == null || !p.getIsAdmin()) { botService.sendMessage(chatId, "❌ Admin only"); return; }
        botService.sendPendingDepositsList(telebirrService.getPendingDeposits());
    }
    
    private void handlePendingWithdrawals(String chatId) {
        Player p = playerRepository.findById(chatId).orElse(null);
        if (p == null || !p.getIsAdmin()) { botService.sendMessage(chatId, "❌ Admin only"); return; }
        botService.sendPendingWithdrawalsList(telebirrService.getPendingWithdrawals());
    }
    
    private void handleStats(String chatId) {
        Player p = playerRepository.findById(chatId).orElse(null);
        if (p == null || !p.getIsAdmin()) { botService.sendMessage(chatId, "❌ Admin only"); return; }
        botService.sendAdminMessage("📊 <b>Game Statistics</b>\n\nTotal Players: " + playerRepository.count() + "\nRegistered: " + playerRepository.countRegisteredPlayers() + "\nTotal Deposits: " + playerRepository.sumTotalDeposits() + " ETB\nTotal Withdrawals: " + playerRepository.sumTotalWithdrawals() + " ETB");
    }
    
    private void handleApproveDeposit(String chatId, String text) {
        Player p = playerRepository.findById(chatId).orElse(null);
        if (p == null || !p.getIsAdmin()) { botService.sendMessage(chatId, "❌ Admin only"); return; }
        String[] parts = text.split(" ");
        if (parts.length < 2) { botService.sendMessage(chatId, "Usage: /approve_deposit [RequestID]"); return; }
        try {
            telebirrService.approveDeposit(parts[1], Long.parseLong(chatId));
            botService.sendMessage(chatId, "✅ Deposit approved!");
        } catch(Exception e) { botService.sendMessage(chatId, "❌ Error: " + e.getMessage()); }
    }
    
    private void handleRejectDeposit(String chatId, String text) {
        Player p = playerRepository.findById(chatId).orElse(null);
        if (p == null || !p.getIsAdmin()) { botService.sendMessage(chatId, "❌ Admin only"); return; }
        String[] parts = text.split(" ", 3);
        if (parts.length < 2) { botService.sendMessage(chatId, "Usage: /reject_deposit [RequestID] [reason]"); return; }
        String reason = parts.length > 2 ? parts[2] : "No reason";
        try {
            telebirrService.rejectDeposit(parts[1], reason, Long.parseLong(chatId));
            botService.sendMessage(chatId, "✅ Deposit rejected!");
        } catch(Exception e) { botService.sendMessage(chatId, "❌ Error: " + e.getMessage()); }
    }
    
    private void handleApproveWithdraw(String chatId, String text) {
        Player p = playerRepository.findById(chatId).orElse(null);
        if (p == null || !p.getIsAdmin()) { botService.sendMessage(chatId, "❌ Admin only"); return; }
        String[] parts = text.split(" ", 3);
        if (parts.length < 2) { botService.sendMessage(chatId, "Usage: /approve_withdraw [RequestID] [reference]"); return; }
        String ref = parts.length > 2 ? parts[2] : "ADMIN_" + System.currentTimeMillis();
        try {
            telebirrService.approveWithdrawal(parts[1], ref, Long.parseLong(chatId));
            botService.sendMessage(chatId, "✅ Withdrawal approved!");
        } catch(Exception e) { botService.sendMessage(chatId, "❌ Error: " + e.getMessage()); }
    }
    
    private void handleRejectWithdraw(String chatId, String text) {
        Player p = playerRepository.findById(chatId).orElse(null);
        if (p == null || !p.getIsAdmin()) { botService.sendMessage(chatId, "❌ Admin only"); return; }
        String[] parts = text.split(" ", 3);
        if (parts.length < 2) { botService.sendMessage(chatId, "Usage: /reject_withdraw [RequestID] [reason]"); return; }
        String reason = parts.length > 2 ? parts[2] : "No reason";
        try {
            telebirrService.rejectWithdrawal(parts[1], reason, Long.parseLong(chatId));
            botService.sendMessage(chatId, "✅ Withdrawal rejected!");
        } catch(Exception e) { botService.sendMessage(chatId, "❌ Error: " + e.getMessage()); }
    }
    
    private void handleUnknown(String chatId) {
        botService.sendMessage(chatId, "❌ Unknown command. Use /help to see available commands.");
    }
    
    private void handleSession(String chatId, String input) {
        UserSession session = sessions.get(chatId);
        if (input.equalsIgnoreCase("/cancel")) {
            sessions.remove(chatId);
            botService.sendMessage(chatId, "❌ Cancelled.");
            return;
        }
        
        switch (session.command) {
            case "REGISTER": handleRegisterSession(chatId, session, input); break;
            case "DEPOSIT": handleDepositSession(chatId, session, input); break;
            case "WITHDRAW": handleWithdrawSession(chatId, session, input); break;
        }
    }
    
    private void handleRegisterSession(String chatId, UserSession session, String input) {
        if (session.step == 1) {
            if (!Pattern.matches("^09[0-9]{8}$", input)) {
                botService.sendMessage(chatId, "❌ Invalid phone number! Format: 09XXXXXXXX\n\nExample: 0912345678");
                return;
            }
            session.data.put("phone", input);
            session.step = 2;
            botService.sendMessage(chatId, "📝 Step 2 of 3\n\nPlease enter your full name:\n\nExample: Alemu Bekele");
        } else if (session.step == 2) {
            if (input.length() < 3) {
                botService.sendMessage(chatId, "❌ Name too short! Please enter your full name.");
                return;
            }
            String[] parts = input.split(" ", 2);
            session.data.put("firstName", parts[0]);
            session.data.put("lastName", parts.length > 1 ? parts[1] : "");
            session.step = 3;
            botService.sendMessage(chatId, "📝 Step 3 of 3 - Confirmation\n\nPhone: " + session.data.get("phone") + "\nName: " + session.data.get("firstName") + " " + session.data.get("lastName") + "\n\nType YES to confirm, NO to cancel.");
        } else if (session.step == 3) {
            if (input.equalsIgnoreCase("YES")) {
                Player player = registrationService.registerUser(chatId, (String)session.data.get("username"), 
                    (String)session.data.get("firstName"), (String)session.data.get("lastName"), 
                    (String)session.data.get("phone"), "en");
                botService.sendRegistrationSuccess(chatId, player);
                sessions.remove(chatId);
            } else {
                botService.sendMessage(chatId, "❌ Registration cancelled.");
                sessions.remove(chatId);
            }
        }
    }
    
    private void handleDepositSession(String chatId, UserSession session, String input) {
        if (session.step == 1) {
            try {
                double amount = Double.parseDouble(input);
                if (amount < 10) { botService.sendMessage(chatId, "❌ Minimum deposit is 10 ETB"); return; }
                if (amount > 50000) { botService.sendMessage(chatId, "❌ Maximum deposit is 50,000 ETB"); return; }
                session.data.put("amount", amount);
                session.step = 2;
                botService.sendMessage(chatId, "💰 Enter your Telebirr Transaction ID:\n\nExample: TXN123456789");
            } catch(Exception e) {
                botService.sendMessage(chatId, "❌ Invalid amount! Please enter a number.\n\nExample: 100");
            }
        } else if (session.step == 2) {
            session.data.put("txnId", input);
            session.step = 3;
            botService.sendMessage(chatId, "📱 Enter your Telebirr phone number:\n\nExample: 0912345678");
        } else if (session.step == 3) {
            if (!Pattern.matches("^09[0-9]{8}$", input)) {
                botService.sendMessage(chatId, "❌ Invalid phone number! Format: 09XXXXXXXX");
                return;
            }
            try {
                telebirrService.createDepositRequest(chatId, (Double)session.data.get("amount"), 
                    (String)session.data.get("txnId"), input);
                botService.sendMessage(chatId, "✅ Deposit request submitted! Admin will verify within 5-15 minutes.");
                sessions.remove(chatId);
            } catch(Exception e) {
                botService.sendMessage(chatId, "❌ Error: " + e.getMessage());
                sessions.remove(chatId);
            }
        }
    }
    
    private void handleWithdrawSession(String chatId, UserSession session, String input) {
        if (session.step == 1) {
            try {
                double amount = Double.parseDouble(input);
                double balance = (Double)session.data.get("currentBalance");
                if (amount < 10) { botService.sendMessage(chatId, "❌ Minimum withdrawal is 10 ETB"); return; }
                if (amount > 50000) { botService.sendMessage(chatId, "❌ Maximum withdrawal is 50,000 ETB"); return; }
                if (amount > balance) { botService.sendMessage(chatId, "❌ Insufficient balance! Available: " + balance + " ETB"); return; }
                session.data.put("amount", amount);
                session.step = 2;
                botService.sendMessage(chatId, "📱 Enter your Telebirr phone number to receive funds:\n\nExample: 0912345678");
            } catch(Exception e) {
                botService.sendMessage(chatId, "❌ Invalid amount! Please enter a number.");
            }
        } else if (session.step == 2) {
            if (!Pattern.matches("^09[0-9]{8}$", input)) {
                botService.sendMessage(chatId, "❌ Invalid phone number! Format: 09XXXXXXXX");
                return;
            }
            session.data.put("phone", input);
            session.step = 3;
            botService.sendMessage(chatId, "👤 Enter your full name (as registered with Telebirr):\n\nExample: Alemu Bekele");
        } else if (session.step == 3) {
            if (input.length() < 3) {
                botService.sendMessage(chatId, "❌ Name too short!");
                return;
            }
            try {
                telebirrService.createWithdrawRequest(chatId, (Double)session.data.get("amount"), 
                    (String)session.data.get("phone"), input);
                botService.sendMessage(chatId, "✅ Withdrawal request submitted! Admin will process within 5-30 minutes.");
                sessions.remove(chatId);
            } catch(Exception e) {
                botService.sendMessage(chatId, "❌ Error: " + e.getMessage());
                sessions.remove(chatId);
            }
        }
    }
    
    private static class UserSession {
        String command;
        int step;
        Map<String, Object> data = new HashMap<>();
    }
}
