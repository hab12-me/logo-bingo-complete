package com.logobing.repository;

import com.logobing.model.GameParticipation;
import com.logobing.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GameParticipationRepository extends JpaRepository<GameParticipation, Long> {
    List<GameParticipation> findByGameRound(Integer gameRound);
    Optional<GameParticipation> findByPlayerAndGameRound(Player player, Integer gameRound);
}
