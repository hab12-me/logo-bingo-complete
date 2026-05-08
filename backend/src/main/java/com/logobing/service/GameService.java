package com.logobing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logobing.dto.ApiResponse;
import com.logobing.model.*;
import com.logobing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    
    private final PlayerRepository playerRepository;
    private final BingoCardRepository bingoCardRepository;
    private final GameRoomRepository gameRoomRepository;
    private final BingoService bingoService;
    private final ObjectMapper objectMapper;
    
    private GameRoom currentLobby;
    private GameRoom currentPlayroom;
    private int gameRoundCounter = 1;
    
    @PostConstruct
    public void initialize() {
        currentLobby = gameRoomRepository.findByRoomId("LOBBY").orElseGet(() -> createNewGameRoom("LOBBY"));
        currentPlayroom = gameRoomRepository.findByRoomId("PLAYROOM").orElseGet(() -> createNewGameRoom("PLAYROOM"));
        initializeBingoCards();
        log.info("✅ Game Service Initialized!");
    }
    
    private GameRoom createNewGameRoom(String roomId) {
        GameRoom room = new GameRoom();
        room.setRoomId(roomId);
        room.setGameRound(gameRoundCounter);
        room.setLobbyTimer(30);
        room.setGameTimer(180);
        room.setOverTimer(10);
        room.setRewardAmount(0.0);
        room.setActivePlayersCount(0);
        room.setStatus("WAITING");
        room.setDraws("[]");
        room.setWinners("[]");
        return gameRoomRepository.save(room);
    }
    
    private void initializeBingoCards() {
        if (bingoCardRepository.count() == 0) {
            for (int i = 1; i <= 500; i++) {
                BingoCard card = new BingoCard();
                card.setCardNumber(i);
                card.setGridData(bingoService.generateRandomGrid());
                card.setPrice(10.0);
                card.setIsAvailable(true);
                bingoCardRepository.save(card);
            }
            log.info("✅ 500 Bingo Cards Created!");
        }
    }
    
    @Transactional
    public ApiResponse<?> joinGame(String playerId, String playerName) {
        Player player = playerRepository.findById(playerId).orElseGet(() -> {
            Player np = new Player();
            np.setId(playerId);
            np.setUsername(playerName);
            np.setFirstName(playerName);
            np.setWallet(0.0);
            np.setIsAdmin(isAdminId(playerId));
            np.setIsActive(true);
            return playerRepository.save(np);
        });
        player.updateLastSeen();
        playerRepository.save(player);
        
        Optional<BingoCard> existingLockedCard = bingoCardRepository.findByLockedBy(player);
        Map<String, Object> response = new HashMap<>();
        response.put("player", player);
        response.put("lockedCard", existingLockedCard.orElse(null));
        response.put("gameRound", currentLobby.getGameRound());
        return ApiResponse.success("Joined successfully", response, player.getWallet());
    }
    
    private boolean isAdminId(String playerId) {
        return Arrays.asList("1765057062", "1044688332", "6499874707").contains(playerId);
    }
    
    @Transactional
    public ApiResponse<?> lockCard(String playerId, Long cardId) {
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new RuntimeException("Player not found"));
        BingoCard card = bingoCardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("Card not found"));
        
        Optional<BingoCard> existingLock = bingoCardRepository.findByLockedBy(player);
        if (existingLock.isPresent() && existingLock.get().getId().equals(cardId))
            return ApiResponse.success("Card already locked", card, player.getWallet());
        
        if (existingLock.isPresent()) {
            BingoCard previousCard = existingLock.get();
            previousCard.unlock();
            bingoCardRepository.save(previousCard);
        }
        
        if (!card.getIsAvailable() && card.getLockedBy() != null && !card.getLockedBy().getId().equals(playerId))
            return ApiResponse.error("Card locked by another player");
        
        card.lock(player);
        card.setOwner(player);
        bingoCardRepository.save(card);
        return ApiResponse.success("Card locked", card, player.getWallet());
    }
    
    public GameState getGameState(String playerId, String playerName) {
        GameState.GameStateBuilder stateBuilder = GameState.builder();
        
        if ("PLAYING".equals(currentPlayroom.getStatus()) || "COMPLETED".equals(currentPlayroom.getStatus())) {
            stateBuilder.roomId("PLAYROOM").gameRound(currentPlayroom.getGameRound())
                .gameTimer(currentPlayroom.getGameTimer()).overTimer(currentPlayroom.getOverTimer())
                .reward(currentPlayroom.getRewardAmount()).playerCount(currentPlayroom.getActivePlayersCount())
                .status(currentPlayroom.getStatus());
            try {
                List<Integer> draws = objectMapper.readValue(currentPlayroom.getDraws(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class));
                stateBuilder.draws(draws != null ? draws : new ArrayList<>());
            } catch(Exception e) { stateBuilder.draws(new ArrayList<>()); }
            
            Optional<Player> playerOpt = playerRepository.findById(playerId);
            if (playerOpt.isPresent()) {
                Player p = playerOpt.get();
                Optional<BingoCard> lockedCard = bingoCardRepository.findByLockedBy(p);
                stateBuilder.user(GameState.PlayerState.builder().player(p).card(lockedCard.orElse(null))
                        .wallet(p.getWallet()).dismissed(false).hasClaimed(false).build());
            }
            
            Map<String, Long> cardOwners = new HashMap<>();
            for (Map.Entry<String, GameRoom.PlayerParticipation> entry : currentPlayroom.getParticipants().entrySet())
                if (entry.getValue().getCard() != null)
                    cardOwners.put(String.valueOf(entry.getValue().getCard().getId()), Long.parseLong(entry.getKey()));
            stateBuilder.cardOwners(cardOwners);
            
            if ("COMPLETED".equals(currentPlayroom.getStatus()) && currentPlayroom.getWinners() != null) {
                try { stateBuilder.winResult(objectMapper.readValue(currentPlayroom.getWinners(), WinResult.class)); }
                catch(Exception e) {}
            }
        } else {
            stateBuilder.roomId("LOBBY").gameRound(currentLobby.getGameRound())
                .lobbyTimer(currentLobby.getLobbyTimer()).reward(currentLobby.getRewardAmount())
                .playerCount(currentLobby.getActivePlayersCount()).status(currentLobby.getStatus());
            
            Optional<Player> playerOpt = playerRepository.findById(playerId);
            if (playerOpt.isPresent()) {
                Player p = playerOpt.get();
                Optional<BingoCard> lockedCard = bingoCardRepository.findByLockedBy(p);
                stateBuilder.user(GameState.PlayerState.builder().player(p).card(lockedCard.orElse(null))
                        .wallet(p.getWallet()).dismissed(false).hasClaimed(false).build());
            }
            
            Map<String, Long> cardOwners = new HashMap<>();
            for (BingoCard card : bingoCardRepository.findByIsAvailableFalse())
                if (card.getLockedBy() != null)
                    cardOwners.put(String.valueOf(card.getId()), Long.parseLong(card.getLockedBy().getId()));
            stateBuilder.cardOwners(cardOwners);
        }
        return stateBuilder.build();
    }
    
    public GameConfig getGameConfig() {
        GameConfig config = new GameConfig();
        config.setNumberOfCards(500);
        config.setLobbyTimerSeconds(30);
        config.setGameTimerSeconds(180);
        config.setMinBetAmount(10.0);
        config.setMaxBetAmount(1000.0);
        config.setHouseFee(0.10);
        return config;
    }
    
    public GameRoom getCurrentLobby() { return currentLobby; }
    public GameRoom getCurrentPlayroom() { return currentPlayroom; }
    public void setCurrentPlayroom(GameRoom playroom) { this.currentPlayroom = playroom; }
    public int getGameRoundCounter() { return gameRoundCounter; }
    public void incrementGameRound() { gameRoundCounter++; }
    public List<BingoCard> getLockedCardsForCurrentGame() { return bingoCardRepository.findByIsAvailableFalse(); }
    
    @Transactional
    public void unlockAllCards() {
        for (BingoCard card : bingoCardRepository.findByIsAvailableFalse()) card.unlock();
        log.info("🔓 Unlocked all cards");
    }
}
