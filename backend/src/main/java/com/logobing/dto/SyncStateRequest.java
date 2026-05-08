package com.logobing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SyncStateRequest {
    private String playerId;
    private String playerName;
}
