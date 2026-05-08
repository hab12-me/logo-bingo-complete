package com.logobing.repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.Param;

import com.logobing.model.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
    Optional<GameRoom> findByRoomId(String roomId);
}
