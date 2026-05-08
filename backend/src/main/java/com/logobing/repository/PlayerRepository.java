package com.logobing.repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.Param;

import com.logobing.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
//
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, String> {
    Optional<Player> findById(String id);
    Optional<Player> findByPhoneNumber(String phoneNumber);
    
    @Modifying
    @Transactional
    @Query("UPDATE Player p SET p.wallet = p.wallet + :amount WHERE p.id = :playerId")
    int addToWallet(@Param("playerId") String playerId, @Param("amount") Double amount);
    
    @Modifying
    @Transactional
    @Query("UPDATE Player p SET p.wallet = p.wallet - :amount WHERE p.id = :playerId AND p.wallet >= :amount")
    int deductFromWallet(@Param("playerId") String playerId, @Param("amount") Double amount);
    
    @Query("SELECT COUNT(p) FROM Player p WHERE p.isRegistered = true")
    long countRegisteredPlayers();
    
    @Query("SELECT COALESCE(SUM(p.totalDeposited), 0) FROM Player p")
    Double sumTotalDeposits();
    
    @Query("SELECT COALESCE(SUM(p.totalWithdrawn), 0) FROM Player p")
    Double sumTotalWithdrawals();
}
