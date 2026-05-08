package com.logobing.model;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class WinResult {
    private List<Integer> drawnNumbers;
    private List<WinnerInfo> winners;
    
    @Data
    @Builder
    public static class WinnerInfo {
        private String playerId;
        private String playerName;
        private BingoCard card;
        private List<Integer> winningNumbers;
        private Double winningAmount;
    }
}
