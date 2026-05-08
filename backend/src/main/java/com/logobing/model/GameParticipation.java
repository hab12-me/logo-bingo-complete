package com.logobing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_participations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameParticipation {
    
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
    private Boolean hasClaimed = false;
    private Boolean isDismissed = false;
    private LocalDateTime joinedAt;
    
    @PrePersist
    protected void onCreate() { joinedAt = LocalDateTime.now(); }
}
