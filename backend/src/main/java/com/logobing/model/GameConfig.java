package com.logobing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "game_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer numberOfCards = 500;
    private Integer lobbyTimerSeconds = 30;
    private Integer gameTimerSeconds = 180;
    private Double minBetAmount = 10.0;
    private Double maxBetAmount = 1000.0;
    private Double houseFee = 0.10;
    private Boolean isActive = true;
}
