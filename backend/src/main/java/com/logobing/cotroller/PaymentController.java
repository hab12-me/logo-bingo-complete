package com.logobing.controller;

import com.logobing.dto.ApiResponse;
import com.logobing.dto.DepositRequestDto;
import com.logobing.dto.WithdrawRequestDto;
import com.logobing.model.DepositRequest;
import com.logobing.model.Transaction;
import com.logobing.model.WithdrawRequest;
import com.logobing.service.PaymentService;
import com.logobing.service.TelebirrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    
    private final TelebirrService telebirrService;
    private final PaymentService paymentService;
    
    @PostMapping("/deposit/request")
    public ApiResponse<DepositRequest> requestDeposit(
            @RequestHeader("User-Id") String playerId,
            @Valid @RequestBody DepositRequestDto request) {
        try {
            DepositRequest deposit = telebirrService.createDepositRequest(
                playerId, request.getAmount(), request.getTransactionId(), request.getSenderPhoneNumber());
            return ApiResponse.success("Deposit submitted", deposit, null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/deposit/status/{requestId}")
    public ApiResponse<DepositRequest> getDepositStatus(@PathVariable String requestId) {
        DepositRequest request = telebirrService.getDepositRequest(requestId);
        if (request == null) return ApiResponse.error("Not found");
        return ApiResponse.success("Status", request, null);
    }
    
    @PostMapping("/withdraw/request")
    public ApiResponse<WithdrawRequest> requestWithdraw(
            @RequestHeader("User-Id") String playerId,
            @Valid @RequestBody WithdrawRequestDto request) {
        try {
            WithdrawRequest withdraw = telebirrService.createWithdrawRequest(
                playerId, request.getAmount(), request.getRecipientPhoneNumber(), request.getRecipientName());
            return ApiResponse.success("Withdrawal submitted", withdraw, null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/withdraw/status/{requestId}")
    public ApiResponse<WithdrawRequest> getWithdrawStatus(@PathVariable String requestId) {
        WithdrawRequest request = telebirrService.getWithdrawRequest(requestId);
        if (request == null) return ApiResponse.error("Not found");
        return ApiResponse.success("Status", request, null);
    }
    
    @GetMapping("/transactions")
    public ApiResponse<List<Transaction>> getTransactions(@RequestHeader("User-Id") String playerId) {
        return ApiResponse.success("Transactions", paymentService.getUserTransactionHistory(playerId), null);
    }
    
    @GetMapping("/wallet")
    public ApiResponse<Map<String, Object>> getWallet(@RequestHeader("User-Id") String playerId) {
        return ApiResponse.success("Wallet", paymentService.getWalletSummary(playerId), null);
    }
    
    @GetMapping("/telebirr-info")
    public ApiResponse<Map<String, String>> getTelebirrInfo() {
        return ApiResponse.success("Telebirr Info", Map.of(
            "phoneNumber", "0931721793",
            "accountName", "Logo Bing Bingo",
            "minimumDeposit", "10",
            "minimumWithdrawal", "10"
        ), null);
    }
}
