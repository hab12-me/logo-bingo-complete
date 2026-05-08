package com.logobing.service;

import com.logobing.model.*;
import com.logobing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PlayerRepository playerRepository;
    private final TransactionRepository transactionRepository;
    private final DepositRequestRepository depositRequestRepository;
    private final WithdrawRequestRepository withdrawRequestRepository;
    
    public List<Transaction> getUserTransactionHistory(String playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new RuntimeException("Player not found"));
        return transactionRepository.findByPlayerOrderByCreatedAtDesc(player);
    }
    
    public List<DepositRequest> getUserDepositRequests(String playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new RuntimeException("Player not found"));
        return depositRequestRepository.findByPlayerOrderByCreatedAtDesc(player);
    }
    
    public List<WithdrawRequest> getUserWithdrawRequests(String playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new RuntimeException("Player not found"));
        return withdrawRequestRepository.findByPlayerOrderByCreatedAtDesc(player);
    }
    
    public Map<String, Object> getWalletSummary(String playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new RuntimeException("Player not found"));
        Map<String, Object> summary = new HashMap<>();
        summary.put("balance", player.getWallet());
        summary.put("totalDeposited", player.getTotalDeposited());
        summary.put("totalWithdrawn", player.getTotalWithdrawn());
        summary.put("totalWon", player.getTotalWon());
        summary.put("totalBet", player.getTotalBet());
        summary.put("gamesPlayed", player.getGamesPlayed());
        summary.put("gamesWon", player.getGamesWon());
        return summary;
    }
}
