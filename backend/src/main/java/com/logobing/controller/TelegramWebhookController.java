package com.logobing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logobing.service.TelegramBotController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/webhook/telegram")
@RequiredArgsConstructor
public class TelegramWebhookController {
    
    private final TelegramBotController botController;
    private final ObjectMapper mapper;
    
    @PostMapping
    public String handleWebhook(@RequestBody String payload) {
        try {
            Map<String, Object> update = mapper.readValue(payload, Map.class);
            Map<String, Object> message = (Map<String, Object>) update.get("message");
            
            if (message != null) {
                String chatId = String.valueOf(((Map<String, Object>) message.get("chat")).get("id"));
                String text = (String) message.get("text");
                Map<String, Object> from = (Map<String, Object>) message.get("from");
                String username = (String) from.get("username");
                String firstName = (String) from.get("first_name");
                String lastName = (String) from.get("last_name");
                
                if (text != null) {
                    botController.handleMessage(chatId, text, username, firstName, lastName);
                }
            }
            return "OK";
        } catch (Exception e) {
            log.error("Webhook error: {}", e.getMessage());
            return "ERROR";
        }
    }
    
    @GetMapping("/test")
    public String test() { return "✅ Webhook working"; }
}
