package com.jobfiller.Controller;

import com.jobfiller.model.UserProfile;
import com.jobfiller.service.FormFillerService;
import com.jobfiller.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for AI-powered form filling.
 *
 * POST /fill/start  — triggers Ollama-driven form filling in the background
 * GET  /fill/history — returns the full application history
 */
@RestController
@RequestMapping("fill")
@CrossOrigin(origins = "*")
public class FormFillerController {

    @Autowired
    private FormFillerService formFillerService;

    @Autowired
    private ProfileService profileService;

    // ================================
    // START AI FORM FILLING
    // ================================

    /**
     * Accepts a JSON body: { "url": "...", "profileName": "..." }
     * Launches the AI-driven Selenium session in a background thread and
     * returns immediately so the HTTP connection is not held open.
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startFilling(
            @RequestBody Map<String, String> request) {

        String url         = request.get("url");
        String profileName = request.get("profileName");

        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "URL is required"));
        }

        if (profileName == null || profileName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "profileName is required"));
        }

        UserProfile profile = profileService.getProfileByName(profileName)
                .orElse(null);

        if (profile == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Profile '" + profileName + "' not found"));
        }

        // Run the full browser session in a separate thread so the API responds
        // immediately while Ollama + Selenium work in the background.
        CompletableFuture.runAsync(() ->
                formFillerService.startFilling(url, profile,
                        msg -> System.out.println("[FILLER] " + msg)));

        return ResponseEntity.ok(Map.of(
                "status",  "started",
                "message", "AI form filling started! Check the console / Telegram for live status.",
                "url",     url,
                "profile", profileName
        ));
    }

    // ================================
    // GET APPLICATION HISTORY
    // ================================

    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        return ResponseEntity.ok(
                formFillerService.getHistoryService().getAllHistory());
    }
}
