package com.jobfiller.service;

// 3. SettingsService.java


import com.jobfiller.model.Settings;
import com.jobfiller.repositories.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    @Autowired
    private SettingsRepository settingsRepository;

    // ================================
    // GET SETTINGS
    // ================================
    public Settings getSettings() {
        return settingsRepository.findFirstBy()
                .orElseGet(() -> {
                    // Create default settings if not exists
                    Settings defaultSettings = new Settings();
                    defaultSettings.setTelegramEnabled(false);
                    defaultSettings.setDesktopNotifEnabled(true);
                    defaultSettings.setDefaultBrowser("CHROME");
                    defaultSettings.setHeadlessMode(false);
                    defaultSettings.setAutoSubmit(false);
                    defaultSettings.setFormFillDelay(1);
                    defaultSettings.setScreenshotPath(
                            "C:/jobfiller/screenshots");
                    defaultSettings.setErrorLogPath(
                            "C:/jobfiller/logs");
                    return settingsRepository.save(defaultSettings);
                });
    }

    // ================================
    // SAVE SETTINGS
    // ================================
    public Settings saveSettings(Settings settings) {
        return settingsRepository.save(settings);
    }

    // ================================
    // UPDATE TELEGRAM CONFIG
    // ================================
    public Settings updateTelegramConfig(String token,
                                         String chatId,
                                         boolean enabled) {
        Settings settings = getSettings();
        settings.setTelegramToken(token);
        settings.setTelegramChatId(chatId);
        settings.setTelegramEnabled(enabled);
        return settingsRepository.save(settings);
    }
}