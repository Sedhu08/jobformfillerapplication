package com.jobfiller.Controller;

import com.jobfiller.service.SettingsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;

import com.jobfiller.model.Settings;
import com.jobfiller.service.TelegramService;
@RestController
@RequestMapping("settings")
@CrossOrigin(origins = "*")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private TelegramService telegramService;


    // GET SETTINGS

    @GetMapping
    public ResponseEntity<Settings> getSettings() {
        return ResponseEntity.ok(
                settingsService.getSettings());
    }


    // SAVE SETTINGS

    @PostMapping
    public ResponseEntity<Settings> saveSettings(
            @RequestBody Settings settings) {
        return ResponseEntity.ok(
                settingsService.saveSettings(settings));
    }


    // TEST TELEGRAM BOT

    @PostMapping("/telegram/test")
    public ResponseEntity<Map<String, Object>> testTelegram(
            @RequestBody Map<String, String> request) {

        String token  = request.get("token");
        String chatId = request.get("chatId");

        boolean success = telegramService
                .sendTestMessage(token, chatId);

        if (success) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "✅ Test message sent!"));
        } else {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "❌ Failed! Check token & chatId"));
        }
    }
}
