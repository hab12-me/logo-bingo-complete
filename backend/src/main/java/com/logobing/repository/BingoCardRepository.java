package com.logobing.repository;
import org.springframework.data.repository.query.Param;

import com.logobing.model.BingoCard;
import com.logobing.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BingoCardRepository extends JpaRepository<BingoCard, Long> {
    Optional<BingoCard> findById(Long id);
    List<BingoCard> findByIsAvailableTrue();
    List<BingoCard> findByIsAvailableFalse();
    Optional<BingoCard> findByLockedBy(Player lockedBy);
    long countByIsAvailableTrue();
    
    @Modifying
    @Transactional
    @Query("UPDATE BingoCard c SET c.isAvailable = true, c.lockedBy = null, c.lockedAt = null WHERE c.lockedAt < :timeout")
    int unlockExpiredCards(@Param("timeout") LocalDateTime timeout);
}
