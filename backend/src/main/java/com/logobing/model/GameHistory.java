package com.logobing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;
    
    @ManyToOne
    @JoinColumn(name = "card_id")
    private BingoCard card;
    
    private Integer gameRound;
    private Double betAmount;
    private Double winAmount = 0.0;
    private Boolean isWinner = false;
    
    @Column(columnDefinition = "TEXT")
    private String draws;
    
    @Column(columnDefinition = "TEXT")
    private String winningNumbers;
    
    private LocalDateTime playedAt;
    
    @PrePersist
    protected void onCreate() { playedAt = LocalDateTime.now(); }
}
