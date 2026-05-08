package com.logobing.controller;

import com.logobing.dto.ApiResponse;
import com.logobing.model.Player;
import com.logobing.service.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserRegistrationService registrationService;
    
    @PostMapping("/register")
    public ApiResponse<Player> register(@RequestBody Map<String, String> request) {
        try {
            Player player = registrationService.registerUser(
                request.get("telegramId"),
                request.get("username"),
                request.get("firstName"),
                request.get("lastName"),
                request.get("phoneNumber"),
                request.get("languageCode")
            );
            return ApiResponse.success("Registration successful", player, player.getWallet());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/profile")
    public ApiResponse<Player> getProfile(@RequestHeader("User-Id") String playerId) {
        Player player = registrationService.getRegisteredUser(playerId);
        if (player == null) return ApiResponse.error("Not registered");
        return ApiResponse.success("Profile", player, player.getWallet());
    }
    
    @GetMapping("/check-registration")
    public ApiResponse<Map<String, Boolean>> checkRegistration(@RequestParam String telegramId) {
        return ApiResponse.success("Status", Map.of("isRegistered", registrationService.isUserRegistered(telegramId)), null);
    }
}
