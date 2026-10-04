package com.jobfiller.model;

import jakarta.persistence.*;

@Entity
@Table(name = "settings")
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_token")
    private String telegramToken;

    @Column(name = "telegram_chat_id")
    private String telegramChatId;

    @Column(name = "telegram_enabled")
    private Boolean telegramEnabled = false;

    @Column(name = "desktop_notif_enabled")
    private Boolean desktopNotifEnabled = true;

    @Column(name = "screenshot_path")
    private String screenshotPath =
            "C:/jobfiller/screenshots";

    @Column(name = "error_log_path")
    private String errorLogPath =
            "C:/jobfiller/logs";

    @Column(name = "default_browser")
    private String defaultBrowser = "CHROME";

    @Column(name = "headless_mode")
    private Boolean headlessMode = false;

    @Column(name = "auto_submit")
    private Boolean autoSubmit = false;

    @Column(name = "form_fill_delay")
    private Integer formFillDelay = 1;

    // ================================
    // CONSTRUCTORS
    // ================================
    public Settings() {}

    public Settings(Long id, String telegramToken,
            String telegramChatId, Boolean telegramEnabled,
            Boolean desktopNotifEnabled, String screenshotPath,
            String errorLogPath, String defaultBrowser,
            Boolean headlessMode, Boolean autoSubmit,
            Integer formFillDelay) {
        this.id = id;
        this.telegramToken = telegramToken;
        this.telegramChatId = telegramChatId;
        this.telegramEnabled = telegramEnabled;
        this.desktopNotifEnabled = desktopNotifEnabled;
        this.screenshotPath = screenshotPath;
        this.errorLogPath = errorLogPath;
        this.defaultBrowser = defaultBrowser;
        this.headlessMode = headlessMode;
        this.autoSubmit = autoSubmit;
        this.formFillDelay = formFillDelay;
    }

    // ================================
    // GETTERS & SETTERS
    // ================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTelegramToken() {
        return telegramToken;
    }
    public void setTelegramToken(String telegramToken) {
        this.telegramToken = telegramToken;
    }

    public String getTelegramChatId() {
        return telegramChatId;
    }
    public void setTelegramChatId(String telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public Boolean getTelegramEnabled() {
        return telegramEnabled;
    }
    public void setTelegramEnabled(Boolean telegramEnabled) {
        this.telegramEnabled = telegramEnabled;
    }

    public Boolean getDesktopNotifEnabled() {
        return desktopNotifEnabled;
    }
    public void setDesktopNotifEnabled(
            Boolean desktopNotifEnabled) {
        this.desktopNotifEnabled = desktopNotifEnabled;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }
    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    public String getErrorLogPath() { return errorLogPath; }
    public void setErrorLogPath(String errorLogPath) {
        this.errorLogPath = errorLogPath;
    }

    public String getDefaultBrowser() {
        return defaultBrowser;
    }
    public void setDefaultBrowser(String defaultBrowser) {
        this.defaultBrowser = defaultBrowser;
    }

    public Boolean getHeadlessMode() { return headlessMode; }
    public void setHeadlessMode(Boolean headlessMode) {
        this.headlessMode = headlessMode;
    }

    public Boolean getAutoSubmit() { return autoSubmit; }
    public void setAutoSubmit(Boolean autoSubmit) {
        this.autoSubmit = autoSubmit;
    }

    public Integer getFormFillDelay() {
        return formFillDelay;
    }
    public void setFormFillDelay(Integer formFillDelay) {
        this.formFillDelay = formFillDelay;
    }
}