package com.logobing.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Entity
@Table(name = "game_rooms")
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
    protected void onCreate() { 
        createdAt = LocalDateTime.now(); 
    }
    
    // Getters
    public Long getId() { return id; }
    public String getRoomId() { return roomId; }
    public Integer getGameRound() { return gameRound; }
    public Integer getLobbyTimer() { return lobbyTimer; }
    public Integer getGameTimer() { return gameTimer; }
    public Integer getOverTimer() { return overTimer; }
    public Double getRewardAmount() { return rewardAmount; }
    public Integer getActivePlayersCount() { return activePlayersCount; }
    public String getStatus() { return status; }
    public String getDraws() { return draws; }
    public String getWinners() { return winners; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public Map<String, PlayerParticipation> getParticipants() { return participants; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public void setGameRound(Integer gameRound) { this.gameRound = gameRound; }
    public void setLobbyTimer(Integer lobbyTimer) { this.lobbyTimer = lobbyTimer; }
    public void setGameTimer(Integer gameTimer) { this.gameTimer = gameTimer; }
    public void setOverTimer(Integer overTimer) { this.overTimer = overTimer; }
    public void setRewardAmount(Double rewardAmount) { this.rewardAmount = rewardAmount; }
    public void setActivePlayersCount(Integer activePlayersCount) { this.activePlayersCount = activePlayersCount; }
    public void setStatus(String status) { this.status = status; }
    public void setDraws(String draws) { this.draws = draws; }
    public void setWinners(String winners) { this.winners = winners; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public void setParticipants(Map<String, PlayerParticipation> participants) { this.participants = participants; }
    
    // Inner Class
    public static class PlayerParticipation {
        private Player player;
        private BingoCard card;
        private Boolean hasClaimed = false;
        private Boolean isDismissed = false;
        private LocalDateTime joinedAt;
        private java.util.List<Integer> winningNumbers;
        
        public PlayerParticipation() {}
        
        public PlayerParticipation(Player player, BingoCard card, Boolean hasClaimed, Boolean isDismissed, LocalDateTime joinedAt, java.util.List<Integer> winningNumbers) {
            this.player = player;
            this.card = card;
            this.hasClaimed = hasClaimed;
            this.isDismissed = isDismissed;
            this.joinedAt = joinedAt;
            this.winningNumbers = winningNumbers;
        }
        
        // Getters
        public Player getPlayer() { return player; }
        public BingoCard getCard() { return card; }
        public Boolean getHasClaimed() { return hasClaimed; }
        public Boolean getIsDismissed() { return isDismissed; }
        public LocalDateTime getJoinedAt() { return joinedAt; }
        public java.util.List<Integer> getWinningNumbers() { return winningNumbers; }
        
        // Setters
        public void setPlayer(Player player) { this.player = player; }
        public void setCard(BingoCard card) { this.card = card; }
        public void setHasClaimed(Boolean hasClaimed) { this.hasClaimed = hasClaimed; }
        public void setIsDismissed(Boolean isDismissed) { this.isDismissed = isDismissed; }
        public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
        public void setWinningNumbers(java.util.List<Integer> winningNumbers) { this.winningNumbers = winningNumbers; }
    }
}
