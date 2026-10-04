package com.jobfiller.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobfiller.model.CompanyLogin;
import com.jobfiller.model.UserProfile;
import com.jobfiller.model.Settings;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class FormFillerService {

    // ================================
    // DEPENDENCIES
    // ================================
    @Autowired
    private CompanyLoginService loginService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private ApplicationHistoryService historyService;

    @Autowired
    private TelegramService telegramService;

    /**
     * Spring AI ChatClient backed by Ollama.
     * Model is configured via spring.ai.ollama.chat.model in application.properties.
     */
    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ObjectMapper objectMapper;

    // ================================
    // SESSION STATE (per-request)
    // ================================
    private WebDriver driver;
    private WebDriverWait wait;
    private Consumer<String> statusLogger;
    private Settings settings;

    // ================================
    // PUBLIC ENTRY POINT
    // ================================

    /**
     * Main method called by the controller.
     * Opens the browser, logs in if credentials exist, fills the form using
     * Ollama AI to decide which fields to fill and with what values, then
     * records the result in history.
     *
     * @param url          The job application URL
     * @param profile      The candidate's profile loaded from DB
     * @param statusLogger A consumer that streams live status messages back to the caller
     */
    public void startFilling(String url, UserProfile profile,
                             Consumer<String> statusLogger) {
        this.statusLogger = statusLogger;
        this.settings = settingsService.getSettings();

        try {
            log("🚀 Starting AI-powered form filler...");
            log("🌐 Target URL: " + url);

            // Guard: skip if already applied
            if (historyService.isDuplicate(url)) {
                log("⚠️ Duplicate detected - skipping...");
                handleDuplicate(url);
                return;
            }

            // 1. Boot stealth browser
            setupBrowser();

            // 2. Navigate to the job page
            log("🔄 Loading page...");
            driver.get(url);
            waitForPageLoad();
            humanDelay(1500, 2500);

            // 3. Handle login if credentials are saved for this domain
            handleLogin(url);
            humanDelay(1000, 2000);

            // 4. Handle CAPTCHA if present
            handleCaptcha();

            // 5. Let Ollama fill the form
            fillGenericForm(profile);

            // 6. Take a screenshot as proof
            String screenshotPath = takeScreenshot(extractCompanyName(url));

            // 7. Record success (jobTitle is unknown from the URL alone, left empty)
            historyService.saveSuccess(
                    extractCompanyName(url),
                    "",
                    url,
                    profile.getProfileName(),
                    screenshotPath);

            telegramService.sendSuccess(
                    extractCompanyName(url),
                    "",
                    url);

            log("🎉 Successfully applied to: " + extractCompanyName(url));

        } catch (Exception e) {
            handleError(url, profile, e);
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception ignored) {}
            }
        }
    }

    // ================================
    // AI FORM FILLING CORE
    // ================================

    /**
     * Uses Ollama (via Spring AI) to analyse the visible form HTML and produce
     * a JSON map of { "cssSelector": "valueToFill" } actions.
     * The map is then executed field-by-field via Selenium.
     */
    private void fillGenericForm(UserProfile profile) {
        try {
            log("🤖 Ollama AI is analysing the page structure...");

            // Step 1 — Capture only the visible interactive elements from the page
            String htmlContext = extractVisibleHtml();

            // Step 2 — Build a structured prompt for the local Ollama model
            String prompt = buildFormFillingPrompt(profile, htmlContext);

            // Step 3 — Call Ollama via Spring AI ChatClient
            log("🧠 Sending prompt to Ollama...");
            String aiResponse = chatClient.prompt(prompt).call().content();
            log("📩 Ollama responded.");

            // Step 4 — Parse the JSON plan returned by the model
            Map<String, String> fillPlan = parseAiResponse(aiResponse);

            if (fillPlan.isEmpty()) {
                log("⚠️ Ollama returned an empty fill plan. Skipping form fill.");
                return;
            }

            log("✅ AI generated fill plan with " + fillPlan.size() + " action(s).");

            // Step 5 — Execute each action in sequence
            for (Map.Entry<String, String> entry : fillPlan.entrySet()) {
                String selector = entry.getKey();
                String value    = entry.getValue();
                executeAction(selector, value);
                humanDelay(300, 700);
            }

        } catch (Exception e) {
            log("❌ AI form filling failed: " + e.getMessage());
            throw new RuntimeException("AI form filling failed", e);
        }
    }

    /**
     * Builds the detailed prompt sent to Ollama.
     * The prompt includes the full user profile context and the raw HTML of
     * visible form elements so the model can map profile fields to selectors.
     */
    private String buildFormFillingPrompt(UserProfile profile, String htmlContext) {
        return "You are an expert job application bot. Your task is to fill out a job application form.\n\n" +
               "=== CANDIDATE PROFILE ===\n" +
               buildProfileDescription(profile) + "\n\n" +
               "=== FORM HTML (visible elements only) ===\n" +
               htmlContext + "\n\n" +
               "=== INSTRUCTIONS ===\n" +
               "1. Analyse the HTML above and map the candidate profile fields to the correct form elements.\n" +
               "2. Return ONLY a valid JSON object. No markdown, no explanation, just JSON.\n" +
               "3. Each key must be a valid CSS selector that uniquely identifies the element.\n" +
               "4. Each value must be the exact text/value to enter into that element.\n" +
               "5. For <select> elements, use the exact visible option text.\n" +
               "6. For checkboxes or radio buttons that should be selected, use value 'CLICK_ACTION'.\n" +
               "7. For the final submit button, also use 'CLICK_ACTION' as the value.\n" +
               "8. Skip any fields that the candidate profile does not have data for.\n" +
               "9. Prefer id-based selectors (e.g. #firstName) for accuracy.\n\n" +
               "Example output:\n" +
               "{\"#firstName\": \"John\", \"#email\": \"john@example.com\", \"button[type='submit']\": \"CLICK_ACTION\"}\n\n" +
               "JSON output:";
    }

    /**
     * Converts the UserProfile entity into a human-readable text block for the AI prompt.
     */
    private String buildProfileDescription(UserProfile profile) {
        return String.format(
            "Full Name    : %s\n" +
            "Email        : %s\n" +
            "Phone        : %s\n" +
            "Address      : %s\n" +
            "LinkedIn URL : %s\n" +
            "Skills       : %s\n" +
            "Experience   : %s\n" +
            "Education    : %s",
            nullSafe(profile.getFullName()),
            nullSafe(profile.getEmail()),
            nullSafe(profile.getPhone()),
            nullSafe(profile.getAddress()),
            nullSafe(profile.getLinkedinUrl()),
            nullSafe(profile.getSkills()),
            nullSafe(profile.getExperience()),
            nullSafe(profile.getEducation())
        );
    }

    private String nullSafe(String value) {
        return value != null ? value : "N/A";
    }

    // ================================
    // ACTION EXECUTOR
    // ================================

    /**
     * Executes a single fill action. If value is CLICK_ACTION, it clicks the
     * element. Otherwise it types into input/textarea or selects on <select>.
     */
    private void executeAction(String selector, String value) {
        try {
            WebElement element = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));

            if (!element.isDisplayed()) {
                log("⏭️ Element not visible, skipping: " + selector);
                return;
            }

            if ("CLICK_ACTION".equalsIgnoreCase(value)) {
                log("🖱️ Clicking: " + selector);
                humanScroll();
                humanClick(element);
                waitForPageLoad();
            } else {
                fillElementBasedOnType(element, value);
            }

        } catch (Exception e) {
            log("⚠️ Could not act on selector [" + selector + "]: " + e.getMessage());
        }
    }

    // ================================
    // ELEMENT INTERACTION HELPERS
    // ================================

    /**
     * Detects the element type and fills it appropriately — supports
     * input, textarea, and select elements.
     */
    private void fillElementBasedOnType(WebElement element, String value) {
        String tagName = element.getTagName().toLowerCase();

        if ("select".equals(tagName)) {
            humanClick(element);
            org.openqa.selenium.support.ui.Select dropdown =
                    new org.openqa.selenium.support.ui.Select(element);
            dropdown.selectByVisibleText(value);
            log("✅ Dropdown selected: " + value);
        } else {
            humanClick(element);
            element.clear();
            humanType(element, value);
            log("✅ Typed into [" + element.getAttribute("id") + "]: " + value);
        }
    }

    /**
     * Extracts the outerHTML of all visible interactive elements and their
     * associated labels so the AI has enough context to identify each field.
     */
    private String extractVisibleHtml() {
        List<WebElement> elements = driver.findElements(
                By.cssSelector("input, select, textarea, label, [placeholder]"));

        StringBuilder sb = new StringBuilder();
        for (WebElement el : elements) {
            try {
                if (el.isDisplayed()) {
                    sb.append(el.getAttribute("outerHTML")).append("\n");
                }
            } catch (Exception ignored) {}
        }
        return sb.toString();
    }

    /**
     * Parses the JSON map returned by Ollama.
     * Strips any markdown code fences the model may have added around the JSON.
     */
    private Map<String, String> parseAiResponse(String content) {
        try {
            int start = content.indexOf("{");
            int end   = content.lastIndexOf("}");
            if (start == -1 || end == -1 || start >= end) {
                log("⚠️ No valid JSON found in AI response.");
                return new HashMap<>();
            }
            String json = content.substring(start, end + 1);
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log("⚠️ Failed to parse AI JSON. Raw response: " + content);
            return new HashMap<>();
        }
    }

    // ================================
    // BROWSER SETUP
    // ================================

    private void setupBrowser() {
        String  browser  = settings.getDefaultBrowser();
        boolean headless = settings.getHeadlessMode();

        switch (browser) {
            case "CHROME" -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments(
                        "--disable-blink-features=AutomationControlled",
                        "--disable-infobars",
                        "--disable-dev-shm-usage",
                        "--no-sandbox",
                        "--start-maximized",
                        "--disable-notifications",
                        "--lang=en-US",
                        "--disable-popup-blocking",
                        "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
                );
                options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
                options.setExperimentalOption("useAutomationExtension", false);
                if (headless) options.addArguments("--headless");
                driver = new ChromeDriver(options);
                ((JavascriptExecutor) driver).executeScript(
                        "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
            }
            case "FIREFOX" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions options = new FirefoxOptions();
                options.addArguments("--width=1920", "--height=1080");
                if (headless) options.addArguments("--headless");
                driver = new FirefoxDriver(options);
            }
            case "EDGE" -> {
                WebDriverManager.edgedriver().setup();
                driver = new EdgeDriver();
            }
            default -> {
                WebDriverManager.chromedriver().setup();
                driver = new ChromeDriver();
            }
        }

        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        log("🌐 Browser ready: " + browser);
    }

    // ================================
    // LOGIN HANDLING
    // ================================

    private void handleLogin(String url) {
        try {
            loginService.getLoginByUrl(url).ifPresent(login -> {
                log("🔑 Found saved credentials for: " + login.getCompanyName());
                fillLoginForm(login);
            });
        } catch (Exception e) {
            log("⚠️ No saved login for this URL: " + e.getMessage());
        }
    }

    private void fillLoginForm(CompanyLogin login) {
        try {
            String loginPrompt =
                "You are filling a login form. Given the HTML below, return a JSON map of " +
                "{\"cssSelector\": \"value\"} to fill the email/username and password fields, " +
                "then click submit. Use 'CLICK_ACTION' for buttons.\n\n" +
                "EMAIL: " + login.getEmail() + "\n" +
                "PASSWORD: " + login.getPassword() + "\n\n" +
                "FORM HTML:\n" + extractVisibleHtml() + "\n\nJSON output:";

            String aiResponse = chatClient.prompt(loginPrompt).call().content();
            Map<String, String> loginPlan = parseAiResponse(aiResponse);

            for (Map.Entry<String, String> entry : loginPlan.entrySet()) {
                executeAction(entry.getKey(), entry.getValue());
                humanDelay(400, 900);
            }

            waitForPageLoad();
            humanDelay(2000, 3000);
            log("✅ Login completed via AI.");

        } catch (Exception e) {
            log("❌ AI login failed: " + e.getMessage());
        }
    }

    // ================================
    // CAPTCHA HANDLING
    // ================================

    private void handleCaptcha() {
        try {
            boolean captchaFound = !driver.findElements(By.cssSelector(
                    "iframe[src*='recaptcha'], iframe[src*='hcaptcha'], " +
                    ".g-recaptcha, .h-captcha")).isEmpty();

            if (!captchaFound) return;

            log("⚠️ CAPTCHA detected! Please solve it manually in the browser window.");
            log("⏳ Waiting up to 2 minutes...");

            int waited = 0;
            while (waited < 120) {
                Thread.sleep(3000);
                waited += 3;
                boolean stillPresent = !driver.findElements(By.cssSelector(
                        "iframe[src*='recaptcha'], iframe[src*='hcaptcha'], " +
                        ".g-recaptcha, .h-captcha")).isEmpty();

                if (!stillPresent) {
                    log("✅ CAPTCHA solved! Continuing...");
                    return;
                }
                log("⏳ Waiting for CAPTCHA... (" + waited + "s)");
            }
            log("⚠️ CAPTCHA timeout. Continuing anyway...");

        } catch (Exception e) {
            log("⚠️ CAPTCHA check error: " + e.getMessage());
        }
    }

    // ================================
    // SCREENSHOT
    // ================================

    private String takeScreenshot(String companyName) {
        try {
            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String fileName = companyName + "_" + timestamp + ".png";
            String filePath = settings.getScreenshotPath() + "/" + fileName;

            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshot, new File(filePath));
            log("📸 Screenshot saved: " + fileName);
            return filePath;

        } catch (Exception e) {
            log("⚠️ Screenshot failed: " + e.getMessage());
            return null;
        }
    }

    // ================================
    // ERROR HANDLING
    // ================================

    private void handleError(String url, UserProfile profile, Exception e) {
        log("❌ Error: " + e.getMessage());
        String screenshotPath = takeScreenshot("ERROR");

        historyService.saveFailure(
                extractCompanyName(url), url,
                profile.getProfileName(), e.getMessage(), screenshotPath);

        telegramService.sendFailure(extractCompanyName(url), e.getMessage());
        writeErrorLog(url, e);
    }

    private void handleDuplicate(String url) {
        historyService.getPreviousApplication(url).ifPresent(prev -> {
            log("⚠️ Already applied to: " + prev.getCompanyName());
            log("📅 Applied on: " + prev.getAppliedAt());
            log("📊 Status: " + prev.getStatus());
            telegramService.sendDuplicate(
                    prev.getCompanyName(),
                    prev.getAppliedAt().toString(),
                    prev.getStatus());
        });
    }

    private void writeErrorLog(String url, Exception e) {
        try {
            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String logPath = settings.getErrorLogPath() + "/error_" + timestamp + ".log";
            String content = "Timestamp : " + timestamp + "\n" +
                             "URL       : " + url       + "\n" +
                             "Error     : " + e.getMessage() + "\n" +
                             "StackTrace: " + e.toString();
            FileUtils.writeStringToFile(new File(logPath), content, "UTF-8");
            log("📝 Error log saved.");
        } catch (Exception ex) {
            log("⚠️ Error log write failed: " + ex.getMessage());
        }
    }

    // ================================
    // HUMAN BEHAVIOUR SIMULATION
    // ================================

    private void humanType(WebElement field, String text) {
        for (char c : text.toCharArray()) {
            field.sendKeys(String.valueOf(c));
            try {
                Thread.sleep(50 + (int) (Math.random() * 100)); // 50-150ms per keystroke
            } catch (InterruptedException ignored) {}
        }
    }

    private void humanClick(WebElement element) {
        try {
            new Actions(driver)
                    .moveToElement(element)
                    .pause(Duration.ofMillis(200 + (int) (Math.random() * 300)))
                    .click()
                    .perform();
        } catch (Exception e) {
            element.click(); // fallback
        }
    }

    private void humanScroll() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "window.scrollBy(0, " + (100 + (int) (Math.random() * 300)) + ")");
            humanDelay(400, 900);
        } catch (Exception ignored) {}
    }

    private void humanDelay(int minMs, int maxMs) {
        try {
            Thread.sleep(minMs + (int) (Math.random() * (maxMs - minMs)));
        } catch (InterruptedException ignored) {}
    }

    // ================================
    // PAGE UTILITIES
    // ================================

    private void waitForPageLoad() {
        try {
            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState")
                            .equals("complete"));
        } catch (Exception ignored) {}
    }

    private String extractCompanyName(String url) {
        try {
            String withoutProtocol = url.replace("https://", "").replace("http://", "");
            return withoutProtocol.split("/")[0].split("\\.")[0];
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private void log(String message) {
        if (statusLogger != null) {
            statusLogger.accept(message);
        }
        System.out.println(message);
    }

    // ================================
    // CONTROLLER HELPER
    // ================================

    public ApplicationHistoryService getHistoryService() {
        return historyService;
    }
}
