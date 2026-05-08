package com.logobing.service;

import com.logobing.model.Player;
import com.logobing.model.Transaction;
import com.logobing.repository.PlayerRepository;
import com.logobing.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {
    private final PlayerRepository playerRepository;
    private final TransactionRepository transactionRepository;
    
    @Transactional
    public boolean deductBet(String playerId, Double amount) {
        int updated = playerRepository.deductFromWallet(playerId, amount);
        if (updated > 0) { log.info("💰 Deducted {} ETB from {}", amount, playerId); return true; }
        else { log.warn("⚠️ Insufficient funds for {}", playerId); return false; }
    }
    
    @Transactional
    public void addWinning(String playerId, Double amount) {
        playerRepository.addToWallet(playerId, amount);
        log.info("💰 Added {} ETB to {}", amount, playerId);
    }
    
    @Transactional
    public void refundBet(String playerId, Double amount) {
        playerRepository.addToWallet(playerId, amount);
        log.info("💰 Refunded {} ETB to {}", amount, playerId);
    }
    
    public Double getBalance(String playerId) {
        return playerRepository.findById(playerId).map(Player::getWallet).orElse(0.0);
    }
}
