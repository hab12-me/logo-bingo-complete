package com.logobing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logobing.dto.SyncStateRequest;
import com.logobing.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;
    private final BingoService bingoService;
    private final WalletService walletService;
    private final ObjectMapper objectMapper;
    
    private final Set<Integer> drawnNumbers = ConcurrentHashMap.newKeySet();
    private final List<Integer> drawsList = new ArrayList<>();
    private AtomicBoolean isGameActive = new AtomicBoolean(false);
    private AtomicBoolean isGamePaused = new AtomicBoolean(false);
    private AtomicBoolean hasWinner = new AtomicBoolean(false);
    private Timer gameTimer, overTimer, drawTimer;
    
    @Scheduled(fixedDelay = 1000)
    public void broadcastLobbyTimer() {
        GameRoom lobby = gameService.getCurrentLobby();
        if (lobby != null && "WAITING".equals(lobby.getStatus()) && !isGameActive.get()) {
            int timer = lobby.getLobbyTimer();
            if (timer > 0) {
                lobby.setLobbyTimer(timer - 1);
                messagingTemplate.convertAndSend("/topic/lobbyTimer", lobby.getLobbyTimer());
                if (lobby.getLobbyTimer() <= 0) startNewGame();
            }
        }
    }
    
    private void startNewGame() {
        log.info("🎲 Starting new game round: {}", gameService.getGameRoundCounter());
        GameRoom lobby = gameService.getCurrentLobby();
        GameRoom playroom = gameService.getCurrentPlayroom();
        
        drawnNumbers.clear(); drawsList.clear();
        isGameActive.set(true); isGamePaused.set(false); hasWinner.set(false);
        stopAllTimers();
        
        playroom.setStatus("SHUFFLING");
        playroom.setGameRound(gameService.getGameRoundCounter());
        playroom.setGameTimer(180);
        playroom.setRewardAmount(0.0);
        playroom.setDraws("[]");
        playroom.setWinners("[]");
        playroom.getParticipants().clear();
        
        for (BingoCard card : gameService.getLockedCardsForCurrentGame()) {
            if (card.getLockedBy() != null) {
                Player player = card.getLockedBy();
                playroom.getParticipants().put(player.getId(),
                    new GameRoom.PlayerParticipation(player, card, false, false, LocalDateTime.now(), null));
                if (walletService.deductBet(player.getId(), card.getPrice()))
                    playroom.setRewardAmount(playroom.getRewardAmount() + card.getPrice());
            }
        }
        playroom.setActivePlayersCount(playroom.getParticipants().size());
        gameService.setCurrentPlayroom(playroom);
        
        lobby.setStatus("WAITING");
        lobby.setLobbyTimer(30);
        lobby.setActivePlayersCount(0);
        
        Map<String, Object> transitionPayload = new HashMap<>();
        transitionPayload.put("activePlayersCount", playroom.getActivePlayersCount());
        transitionPayload.put("rewardAmount", playroom.getRewardAmount());
        transitionPayload.put("status", "SHUFFLING");
        List<Map<String, Object>> playerCards = new ArrayList<>();
        for (Map.Entry<String, GameRoom.PlayerParticipation> entry : playroom.getParticipants().entrySet()) {
            Map<String, Object> pc = new HashMap<>();
            pc.put("userId", Long.parseLong(entry.getKey()));
            pc.put("card", entry.getValue().getCard());
            playerCards.add(pc);
        }
        transitionPayload.put("playerCards", playerCards);
        messagingTemplate.convertAndSend("/topic/playroom_transit", transitionPayload);
        
        new Timer().schedule(new TimerTask() {
            @Override public void run() {
                if (isGameActive.get() && !hasWinner.get()) startNumberDrawing();
            }
        }, 3000);
    }
    
    private void stopAllTimers() {
        if (gameTimer != null) { gameTimer.cancel(); gameTimer = null; }
        if (overTimer != null) { overTimer.cancel(); overTimer = null; }
        if (drawTimer != null) { drawTimer.cancel(); drawTimer = null; }
    }
    
    private void startNumberDrawing() {
        if (!isGameActive.get() || hasWinner.get()) return;
        GameRoom playroom = gameService.getCurrentPlayroom();
        playroom.setStatus("PLAYING");
        playroom.setGameTimer(180);
        
        Map<String, Object> statusPayload = new HashMap<>();
        statusPayload.put("status", "PLAYING");
        statusPayload.put("activePlayersCount", playroom.getActivePlayersCount());
        statusPayload.put("rewardAmount", playroom.getRewardAmount());
        messagingTemplate.convertAndSend("/topic/playroom_transit", statusPayload);
        
        drawTimer = new Timer();
        drawTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                if (isGameActive.get() && !isGamePaused.get() && !hasWinner.get()) drawNumber();
            }
        }, 0, 3000);
        
        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                if (!isGameActive.get() || hasWinner.get()) return;
                if (playroom.getGameTimer() > 0) {
                    playroom.setGameTimer(playroom.getGameTimer() - 1);
                    messagingTemplate.convertAndSend("/topic/gameTimer", playroom.getGameTimer());
                    if (playroom.getGameTimer() <= 0) { endGameWithNoWinner(); this.cancel(); }
                }
            }
        }, 0, 1000);
    }
    
    private void drawNumber() {
        if (!isGameActive.get() || isGamePaused.get() || hasWinner.get()) return;
        int number = bingoService.generateRandomNumber(drawnNumbers);
        drawnNumbers.add(number);
        drawsList.add(number);
        try { gameService.getCurrentPlayroom().setDraws(objectMapper.writeValueAsString(drawsList)); }
        catch(Exception e) {}
        messagingTemplate.convertAndSend("/topic/draws", new ArrayList<>(drawsList));
    }
    
    @Transactional
    public void claimBingo(String playerId) {
        log.info("🔔 BINGO claim from: {}", playerId);
        if (!isGameActive.get() || hasWinner.get()) return;
        
        GameRoom playroom = gameService.getCurrentPlayroom();
        GameRoom.PlayerParticipation participation = playroom.getParticipants().get(playerId);
        if (participation == null || participation.getHasClaimed() || participation.getIsDismissed()) return;
        
        isGamePaused.set(true);
        messagingTemplate.convertAndSend("/topic/game-paused", Map.of("playerId", playerId));
        
        Set<Integer> drawsSet = new HashSet<>(drawsList);
        int[][] grid = bingoService.parseGrid(participation.getCard().getGridData());
        List<Integer> winningNumbers = bingoService.checkForWinningPattern(grid, drawsSet);
        
        if (!winningNumbers.isEmpty()) {
            participation.setHasClaimed(true);
            participation.setWinningNumbers(winningNumbers);
            processWinner(playerId, participation, playroom);
        } else {
            processInvalidClaim(playerId, participation, playroom);
        }
    }
    
    private void processWinner(String playerId, GameRoom.PlayerParticipation participation, GameRoom playroom) {
        hasWinner.set(true); isGameActive.set(false); isGamePaused.set(false); stopAllTimers();
        double totalReward = playroom.getRewardAmount();
        double winnerAmount = totalReward - (totalReward * 0.10);
        Player winner = participation.getPlayer();
        walletService.addWinning(playerId, winnerAmount);
        bingoService.processWin(winner, participation.getCard(), participation.getWinningNumbers(), drawsList, winnerAmount, playroom.getGameRound());
        
        WinResult finalWinResult = WinResult.builder()
            .drawnNumbers(new ArrayList<>(drawsList))
            .winners(List.of(WinResult.WinnerInfo.builder()
                .playerId(winner.getId()).playerName(winner.getName()).card(participation.getCard())
                .winningNumbers(participation.getWinningNumbers()).winningAmount(winnerAmount).build()))
            .build();
        try { playroom.setWinners(objectMapper.writeValueAsString(finalWinResult)); } catch(Exception e) {}
        playroom.setStatus("COMPLETED"); playroom.setOverTimer(10);
        messagingTemplate.convertAndSend("/topic/game-over", finalWinResult);
        messagingTemplate.convertAndSendToUser(playerId, "/queue/victory", Map.of("amount", winnerAmount, "message", "You won!"));
        startOverTimer(playroom);
    }
    
    private void processInvalidClaim(String playerId, GameRoom.PlayerParticipation participation, GameRoom playroom) {
        participation.setIsDismissed(true);
        messagingTemplate.convertAndSendToUser(playerId, "/queue/dismissed", "dismissed");
        walletService.deductBet(playerId, participation.getCard().getPrice() * 0.05);
        boolean allDismissed = playroom.getParticipants().values().stream().allMatch(p -> p.getIsDismissed() || p.getHasClaimed());
        if (allDismissed) endGameWithNoWinner();
        else { isGamePaused.set(false); messagingTemplate.convertAndSend("/topic/game-resumed", Map.of("message", "Game resumed")); }
    }
    
    private void endGameWithNoWinner() {
        isGameActive.set(false); isGamePaused.set(false); hasWinner.set(false); stopAllTimers();
        GameRoom playroom = gameService.getCurrentPlayroom();
        playroom.setStatus("COMPLETED"); playroom.setOverTimer(10);
        for (Map.Entry<String, GameRoom.PlayerParticipation> entry : playroom.getParticipants().entrySet())
            if (!entry.getValue().getIsDismissed() && !entry.getValue().getHasClaimed())
                walletService.refundBet(entry.getKey(), entry.getValue().getCard().getPrice());
        messagingTemplate.convertAndSend("/topic/game-over", WinResult.builder().drawnNumbers(new ArrayList<>(drawsList)).winners(new ArrayList<>()).build());
        startOverTimer(playroom);
    }
    
    private void startOverTimer(GameRoom playroom) {
        if (overTimer != null) overTimer.cancel();
        overTimer = new Timer();
        overTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                if (playroom.getOverTimer() > 0) {
                    playroom.setOverTimer(playroom.getOverTimer() - 1);
                    messagingTemplate.convertAndSend("/topic/overTimer", playroom.getOverTimer());
                } else { resetAndStartNewRound(); this.cancel(); }
            }
        }, 0, 1000);
    }
    
    private void resetAndStartNewRound() {
        gameService.unlockAllCards();
        gameService.getCurrentPlayroom().getParticipants().clear();
        gameService.getCurrentPlayroom().setStatus("WAITING");
        gameService.getCurrentPlayroom().setActivePlayersCount(0);
        gameService.getCurrentPlayroom().setRewardAmount(0.0);
        gameService.getCurrentLobby().setLobbyTimer(30);
        gameService.getCurrentLobby().setStatus("WAITING");
        isGameActive.set(false); isGamePaused.set(false); hasWinner.set(false);
        drawnNumbers.clear(); drawsList.clear();
        gameService.incrementGameRound();
        messagingTemplate.convertAndSend("/topic/newGame", "New round starting!");
    }
    
    public void syncPlayerState(SyncStateRequest request) {
        messagingTemplate.convertAndSendToUser(request.getPlayerId(), "/queue/state", 
            gameService.getGameState(request.getPlayerId(), request.getPlayerName()));
    }
}
