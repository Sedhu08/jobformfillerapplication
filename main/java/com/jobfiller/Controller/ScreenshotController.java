package com.jobfiller.Controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobfiller.service.SettingsService;

@RestController
@RequestMapping("screenshots")
@CrossOrigin(origins = "*")
public class ScreenshotController {

    @Autowired
    private SettingsService settingsService;

    // ================================
    // GET ALL SCREENSHOTS
    // ================================
    @GetMapping
    public ResponseEntity<List<Map<String, String>>>
    getAllScreenshots() {

        String path = settingsService
                .getSettings()
                .getScreenshotPath();

        File folder = new File(path);
        List<Map<String, String>> screenshots =
                new ArrayList<>();

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles(
                    (dir, name) -> name.endsWith(".png"));

            if (files != null) {
                for (File file : files) {
                    screenshots.add(Map.of(
                            "name", file.getName(),
                            "path", file.getAbsolutePath(),
                            "size", String.valueOf(file.length()),
                            "date", String.valueOf(
                                    file.lastModified())
                    ));
                }
            }
        }
        return ResponseEntity.ok(screenshots);
    }

    // ================================
    // OPEN SCREENSHOTS FOLDER
    // ================================
    @GetMapping("/open-folder")
    public ResponseEntity<Map<String, String>>
    openFolder() {
        try {
            String path = settingsService
                    .getSettings()
                    .getScreenshotPath();
            Runtime.getRuntime().exec(
                    "explorer " + path);
            return ResponseEntity.ok(Map.of(
                    "message", "Folder opened!"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "error", e.getMessage()));
        }
    }
}
