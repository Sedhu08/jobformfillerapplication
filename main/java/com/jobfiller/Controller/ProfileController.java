package com.jobfiller.Controller;

import com.jobfiller.model.UserProfile;
import com.jobfiller.service.ProfileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("profiles")
@CrossOrigin(origins = "*") // Allow Electron to call
public class ProfileController {

        @Autowired
        private ProfileService profileService;

        // GET ALL PROFILES

        @GetMapping
        public ResponseEntity<List<UserProfile>> getAllProfiles() {
                return ResponseEntity.ok(
                                profileService.getAllProfiles());
        }

        // GET PROFILE BY NAME

        @GetMapping("{profileName}")
        public ResponseEntity<UserProfile> getProfile(
                        @PathVariable String profileName) {
                return profileService
                                .getProfileByName(profileName)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        // GET DEFAULT PROFILE

        @GetMapping("default")
        public ResponseEntity<UserProfile> getDefaultProfile() {
                return profileService
                                .getDefaultProfile()
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        // SAVE PROFILE

        @PostMapping
        public ResponseEntity<UserProfile> saveProfile(
                        @RequestBody UserProfile profile) {
                return ResponseEntity.ok(
                                profileService.saveProfile(profile));
        }

        // UPDATE PROFILE

        @PutMapping("{id}")
        public ResponseEntity<UserProfile> updateProfile(
                        @PathVariable Long id,
                        @RequestBody UserProfile profile) {
                profile.setId(id);
                return ResponseEntity.ok(
                                profileService.updateProfile(profile));
        }

        // DELETE PROFILE

        @DeleteMapping("{profileName}")
        public ResponseEntity<Void> deleteProfile(
                        @PathVariable String profileName) {
                profileService.deleteProfile(profileName);
                return ResponseEntity.ok().build();
        }

        // DUPLICATE PROFILE

        @PostMapping("{profileName}/duplicate")
        public ResponseEntity<UserProfile> duplicateProfile(
                        @PathVariable String profileName) {
                return ResponseEntity.ok(
                                profileService.duplicateProfile(profileName));
        }
}
