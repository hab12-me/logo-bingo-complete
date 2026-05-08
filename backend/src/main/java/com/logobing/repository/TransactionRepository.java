package com.logobing.repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.Param;

import com.logobing.model.Player;
import com.logobing.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
//
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByPlayerOrderByCreatedAtDesc(Player player);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.player = :player AND t.type = 'DEPOSIT' AND t.status = 'COMPLETED'")
    Double sumDepositsByPlayer(@Param("player") Player player);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = 'DEPOSIT' AND t.status = 'COMPLETED' AND DATE(t.createdAt) = CURRENT_DATE")
    Double sumTodayDeposits();
}
