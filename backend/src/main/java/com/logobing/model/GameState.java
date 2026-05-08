package com.logobing.model;

import lombok.Data;
import lombok.Builder;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class GameState {
    private String roomId;
    private Integer gameRound;
    private Integer lobbyTimer;
    private Integer gameTimer;
    private Integer overTimer;
    private Double reward;
    private Integer playerCount;
    private String status;
    private List<Integer> draws;
    private Map<String, Long> cardOwners;
    private PlayerState user;
    private WinResult winResult;
    
    @Data
    @Builder
    public static class PlayerState {
        private Player player;
        private BingoCard card;
        private Double wallet;
        private Boolean dismissed;
        private Boolean hasClaimed;
    }
}
