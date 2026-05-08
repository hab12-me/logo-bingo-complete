package com.logobing.controller;

import com.logobing.dto.ApiResponse;
import com.logobing.model.DepositRequest;
import com.logobing.model.Player;
import com.logobing.model.WithdrawRequest;
import com.logobing.repository.PlayerRepository;
import com.logobing.service.TelebirrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final PlayerRepository playerRepository;
    private final TelebirrService telebirrService;
    
    private boolean isAdmin(String playerId) {
        return playerId.equals("1765057062") || playerId.equals("1044688332") || playerId.equals("6499874707");
    }
    
    @GetMapping("/players")
    public ApiResponse<List<Player>> getAllPlayers(@RequestHeader("User-Id") String adminId) {
        if (!isAdmin(adminId)) return ApiResponse.error("Unauthorized");
        return ApiResponse.success("Players", playerRepository.findAll(), null);
    }
    
    @GetMapping("/deposits/pending")
    public ApiResponse<List<DepositRequest>> getPendingDeposits(@RequestHeader("User-Id") String adminId) {
        if (!isAdmin(adminId)) return ApiResponse.error("Unauthorized");
        return ApiResponse.success("Pending deposits", telebirrService.getPendingDeposits(), null);
    }
    
    @GetMapping("/withdrawals/pending")
    public ApiResponse<List<WithdrawRequest>> getPendingWithdrawals(@RequestHeader("User-Id") String adminId) {
        if (!isAdmin(adminId)) return ApiResponse.error("Unauthorized");
        return ApiResponse.success("Pending withdrawals", telebirrService.getPendingWithdrawals(), null);
    }
    
    @PostMapping("/deposit/approve/{requestId}")
    public ApiResponse<String> approveDeposit(@PathVariable String requestId, @RequestHeader("User-Id") String adminId) {
        if (!isAdmin(adminId)) return ApiResponse.error("Unauthorized");
        try {
            telebirrService.approveDeposit(requestId, Long.parseLong(adminId));
            return ApiResponse.success("Deposit approved", null, null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/deposit/reject/{requestId}")
    public ApiResponse<String> rejectDeposit(@PathVariable String requestId, @RequestParam String reason, @RequestHeader("User-Id") String adminId) {
        if (!isAdmin(adminId)) return ApiResponse.error("Unauthorized");
        try {
            telebirrService.rejectDeposit(requestId, reason, Long.parseLong(adminId));
            return ApiResponse.success("Deposit rejected", null, null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/withdraw/approve/{requestId}")
    public ApiResponse<String> approveWithdrawal(@PathVariable String requestId, @RequestParam String reference, @RequestHeader("User-Id") String adminId) {
        if (!isAdmin(adminId)) return ApiResponse.error("Unauthorized");
        try {
            telebirrService.approveWithdrawal(requestId, reference, Long.parseLong(adminId));
            return ApiResponse.success("Withdrawal approved", null, null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/withdraw/reject/{requestId}")
    public ApiResponse<String> rejectWithdrawal(@PathVariable String requestId, @RequestParam String reason, @RequestHeader("User-Id") String adminId) {
        if (!isAdmin(adminId)) return ApiResponse.error("Unauthorized");
        try {
            telebirrService.rejectWithdrawal(requestId, reason, Long.parseLong(adminId));
            return ApiResponse.success("Withdrawal rejected", null, null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats(@RequestHeader("User-Id") String adminId) {
        if (!isAdmin(adminId)) return ApiResponse.error("Unauthorized");
        return ApiResponse.success("Stats", Map.of(
            "totalPlayers", playerRepository.count(),
            "registeredPlayers", playerRepository.countRegisteredPlayers(),
            "totalDeposits", playerRepository.sumTotalDeposits(),
            "totalWithdrawals", playerRepository.sumTotalWithdrawals()
        ), null);
    }
}
