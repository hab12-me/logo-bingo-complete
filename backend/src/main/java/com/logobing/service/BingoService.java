package com.logobing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logobing.model.*;
import com.logobing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class BingoService {
    
    private final GameHistoryRepository gameHistoryRepository;
    private final PlayerRepository playerRepository;
    private final ObjectMapper objectMapper;
    
    private static final int GRID_SIZE = 5;
    private static final int MAX_NUMBER = 75;
    
    public String generateRandomGrid() {
        int[][] grid = new int[GRID_SIZE][GRID_SIZE];
        for (int col = 0; col < GRID_SIZE; col++) {
            int start = col * 15 + 1;
            int end = start + 14;
            List<Integer> numbers = new ArrayList<>();
            for (int i = start; i <= end; i++) numbers.add(i);
            Collections.shuffle(numbers);
            for (int row = 0; row < GRID_SIZE; row++) {
                if (row == 2 && col == 2) grid[row][col] = 0;
                else grid[row][col] = numbers.get(row);
            }
        }
        try { return objectMapper.writeValueAsString(grid); }
        catch (Exception e) { return "[[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0]]"; }
    }
    
    public int[][] parseGrid(String gridData) {
        try { return objectMapper.readValue(gridData, int[][].class); }
        catch (Exception e) { return new int[GRID_SIZE][GRID_SIZE]; }
    }
    
    public List<Integer> checkForWinningPattern(int[][] cardGrid, Set<Integer> drawnNumbers) {
        for (int row = 0; row < GRID_SIZE; row++) {
            List<Integer> rowNumbers = new ArrayList<>();
            boolean rowComplete = true;
            for (int col = 0; col < GRID_SIZE; col++) {
                int number = cardGrid[row][col];
                if (number != 0 && !drawnNumbers.contains(number)) { rowComplete = false; break; }
                if (number != 0) rowNumbers.add(number);
            }
            if (rowComplete && !rowNumbers.isEmpty()) return rowNumbers;
        }
        for (int col = 0; col < GRID_SIZE; col++) {
            List<Integer> colNumbers = new ArrayList<>();
            boolean colComplete = true;
            for (int row = 0; row < GRID_SIZE; row++) {
                int number = cardGrid[row][col];
                if (number != 0 && !drawnNumbers.contains(number)) { colComplete = false; break; }
                if (number != 0) colNumbers.add(number);
            }
            if (colComplete && !colNumbers.isEmpty()) return colNumbers;
        }
        List<Integer> diag1Numbers = new ArrayList<>();
        boolean diag1Complete = true;
        for (int i = 0; i < GRID_SIZE; i++) {
            int number = cardGrid[i][i];
            if (number != 0 && !drawnNumbers.contains(number)) { diag1Complete = false; break; }
            if (number != 0) diag1Numbers.add(number);
        }
        if (diag1Complete && !diag1Numbers.isEmpty()) return diag1Numbers;
        
        List<Integer> diag2Numbers = new ArrayList<>();
        boolean diag2Complete = true;
        for (int i = 0; i < GRID_SIZE; i++) {
            int number = cardGrid[i][GRID_SIZE - 1 - i];
            if (number != 0 && !drawnNumbers.contains(number)) { diag2Complete = false; break; }
            if (number != 0) diag2Numbers.add(number);
        }
        if (diag2Complete && !diag2Numbers.isEmpty()) return diag2Numbers;
        return new ArrayList<>();
    }
    
    public int generateRandomNumber(Set<Integer> alreadyCalled) {
        int number;
        do { number = ThreadLocalRandom.current().nextInt(1, MAX_NUMBER + 1); }
        while (alreadyCalled.contains(number));
        return number;
    }
    
    @Transactional
    public void processWin(Player player, BingoCard card, List<Integer> winningNumbers, List<Integer> draws, double rewardAmount, int gameRound) {
        player.setGamesWon(player.getGamesWon() + 1);
        player.setGamesPlayed(player.getGamesPlayed() + 1);
        player.addToWallet(rewardAmount);
        playerRepository.save(player);
        
        GameHistory history = new GameHistory();
        history.setPlayer(player); history.setCard(card); history.setGameRound(gameRound);
        history.setBetAmount(card.getPrice()); history.setWinAmount(rewardAmount); history.setIsWinner(true);
        try { history.setDraws(objectMapper.writeValueAsString(draws));
              history.setWinningNumbers(objectMapper.writeValueAsString(winningNumbers)); } catch(Exception e) {}
        gameHistoryRepository.save(history);
        log.info("🏆 WIN: {} won {} ETB", player.getName(), rewardAmount);
    }
}
