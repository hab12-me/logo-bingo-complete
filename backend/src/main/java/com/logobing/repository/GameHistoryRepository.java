package com.logobing.repository;
import org.springframework.data.repository.query.Param;

import com.logobing.model.GameHistory;
import com.logobing.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {
    List<GameHistory> findByPlayerOrderByPlayedAtDesc(Player player);
    long countByIsWinnerTrue();
}
