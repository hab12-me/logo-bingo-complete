package com.logobing.service;

import com.logobing.model.Player;
import com.logobing.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistrationService {
    
    private final PlayerRepository playerRepository;
    private final TelegramBotService botService;
    
    @Transactional
    public Player registerUser(String telegramId, String username, String firstName, String lastName, String phoneNumber, String languageCode) {
        Player existing = playerRepository.findById(telegramId).orElse(null);
        
        if (existing != null && existing.getIsRegistered()) {
            return existing;
        }
        
        Player player;
        if (existing != null) {
            player = existing;
            player.completeRegistration(phoneNumber, firstName, lastName, languageCode);
        } else {
            player = new Player();
            player.setId(telegramId);
            player.setUsername(username != null ? username : "user_" + telegramId.substring(0, 4));
            player.setFirstName(firstName);
            player.setLastName(lastName);
            player.setPhoneNumber(phoneNumber);
            player.setLanguageCode(languageCode);
            player.setIsRegistered(true);
            player.setWallet(0.0);
            player.setIsAdmin(isAdminId(telegramId));
            player.setIsActive(true);
        }
        
        Player saved = playerRepository.save(player);
        botService.sendAdminMessage("📝 New user registered: " + firstName + " " + lastName + "\n📱 Phone: " + phoneNumber + "\n🆔 " + telegramId);
        log.info("✅ New user registered: {}", telegramId);
        return saved;
    }
    
    private boolean isAdminId(String telegramId) {
        return telegramId.equals("1765057062") || telegramId.equals("1044688332") || telegramId.equals("6499874707");
    }
    
    public Player getOrCreatePlayer(String telegramId, String username, String firstName, String languageCode) {
        return playerRepository.findById(telegramId).orElseGet(() -> {
            Player p = new Player();
            p.setId(telegramId);
            p.setUsername(username);
            p.setFirstName(firstName);
            p.setLanguageCode(languageCode);
            p.setIsRegistered(false);
            p.setWallet(0.0);
            return playerRepository.save(p);
        });
    }
    
    public boolean isUserRegistered(String telegramId) {
        return playerRepository.findById(telegramId).map(Player::getIsRegistered).orElse(false);
    }
    
    public Player getRegisteredUser(String telegramId) {
        return playerRepository.findById(telegramId).filter(Player::getIsRegistered).orElse(null);
    }
}
