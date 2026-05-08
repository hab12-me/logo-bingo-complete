package com.logobing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Entity
@Table(name = "game_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String roomId;
    private Integer gameRound = 0;
    private Integer lobbyTimer = 30;
    private Integer gameTimer = 180;
    private Integer overTimer = 10;
    private Double rewardAmount = 0.0;
    private Integer activePlayersCount = 0;
    private String status = "WAITING";
    
    @Column(columnDefinition = "TEXT")
    private String draws;
    
    @Column(columnDefinition = "TEXT")
    private String winners;
    
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    @Transient
    private Map<String, PlayerParticipation> participants = new ConcurrentHashMap<>();
    
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerParticipation {
        private Player player;
        private BingoCard card;
        private Boolean hasClaimed = false;
        private Boolean isDismissed = false;
        private LocalDateTime joinedAt;
        private List<Integer> winningNumbers;
    }
}
