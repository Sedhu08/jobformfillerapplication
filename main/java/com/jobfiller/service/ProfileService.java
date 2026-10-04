package com.jobfiller.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobfiller.model.UserProfile;
import com.jobfiller.repositories.UserProfileRepository;

@Service
public class ProfileService {

    @Autowired
    private UserProfileRepository profileRepository;

    // ================================
    // SAVE PROFILE
    // ================================
    public UserProfile saveProfile(UserProfile profile) {
        // If marked as default, remove default from others
        if (profile.getIsDefault()) {
            profileRepository.findByIsDefaultTrue()
                    .ifPresent(existing -> {
                        existing.setIsDefault(false);
                        profileRepository.save(existing);
                    });
        }
        return profileRepository.save(profile);
    }

    // ================================
    // GET ALL PROFILES
    // ================================
    public List<UserProfile> getAllProfiles() {
        return profileRepository.findAll();
    }

    // ================================
    // GET PROFILE BY NAME
    // ================================
    public Optional<UserProfile> getProfileByName(
            String profileName) {
        return profileRepository.findByProfileName(profileName);
    }

    // ================================
    // GET DEFAULT PROFILE
    // ================================
    public Optional<UserProfile> getDefaultProfile() {
        return profileRepository.findByIsDefaultTrue();
    }

    // ================================
    // UPDATE PROFILE
    // ================================
    public UserProfile updateProfile(UserProfile profile) {
        return profileRepository.save(profile);
    }

    // ================================
    // DELETE PROFILE
    // ================================
    @Transactional
    public void deleteProfile(String ProfileName) {
          profileRepository.deleteByProfileNameQuery(ProfileName);

    }

    // ================================
    // DUPLICATE PROFILE
    // ================================
    public UserProfile duplicateProfile(String profileName) {
        Optional<UserProfile> existing =
                profileRepository.findByProfileName(profileName);

        if (existing.isPresent()) {
            UserProfile copy = new UserProfile();
            UserProfile original = existing.get();

            // Copy all fields
            copy.setProfileName(original.getProfileName() + " (Copy)");
            copy.setFullName(original.getFullName());
            copy.setEmail(original.getEmail());
            copy.setPhone(original.getPhone());
            copy.setAddress(original.getAddress());
            copy.setLinkedinUrl(original.getLinkedinUrl());
            copy.setExperience(original.getExperience());
            copy.setSkills(original.getSkills());
            copy.setEducation(original.getEducation());
            copy.setResumePath(original.getResumePath());
            copy.setCoverLetterPath(original.getCoverLetterPath());
            copy.setPhotoPath(original.getPhotoPath());
            copy.setIsDefault(false);

            return profileRepository.save(copy);
        }
        throw new RuntimeException("Profile not found: " + profileName);
    }

    // ================================
    // CHECK IF PROFILE EXISTS
    // ================================
    public boolean profileExists(String profileName) {
        return profileRepository.existsByProfileName(profileName);
    }
}
