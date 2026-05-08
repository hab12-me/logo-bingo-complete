package com.logobing.controller;

import com.logobing.dto.ApiResponse;
import com.logobing.dto.SyncStateRequest;
import com.logobing.model.GameConfig;
import com.logobing.model.GameState;
import com.logobing.service.GameService;
import com.logobing.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {
    
    private final GameService gameService;
    private final WebSocketService webSocketService;
    
    @GetMapping("/config")
    public GameConfig getConfig() {
        return gameService.getGameConfig();
    }
    
    @PostMapping("/join")
    public ApiResponse<?> joinGame(@RequestParam String name, @RequestParam String id) {
        return gameService.joinGame(id, name);
    }
    
    @PostMapping("/state")
    public GameState getGameState(@RequestParam String playerId, @RequestParam String playerName) {
        return gameService.getGameState(playerId, playerName);
    }
    
    @PostMapping("/lockCard")
    public ApiResponse<?> lockCard(@RequestParam String cardId, @RequestHeader("User-Id") String playerId) {
        return gameService.lockCard(playerId, Long.parseLong(cardId));
    }
    
    @MessageMapping("/syncState")
    public void syncState(@Payload SyncStateRequest request) {
        webSocketService.syncPlayerState(request);
    }
    
    @MessageMapping("/claimBingo")
    public void claimBingo(@Payload String playerId) {
        webSocketService.claimBingo(playerId);
    }
}
