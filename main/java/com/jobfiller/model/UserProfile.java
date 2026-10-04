package com.jobfiller.model;

import jakarta.persistence.*;

@Entity
@Table(name = "profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_name", nullable = false)
    private String profileName;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "experience", length = 1000)
    private String experience;

    @Column(name = "skills", length = 1000)
    private String skills;

    @Column(name = "education", length = 1000)
    private String education;

    @Column(name = "resume_path")
    private String resumePath;

    @Column(name = "cover_letter_path")
    private String coverLetterPath;

    @Column(name = "photo_path")
    private String photoPath;

    // ================================
    // CONSTRUCTORS
    // ================================
    public UserProfile() {}

    public UserProfile(Long id, String profileName,
            Boolean isDefault, String fullName,
            String email, String phone, String address,
            String linkedinUrl, String experience,
            String skills, String education,
            String resumePath, String coverLetterPath,
            String photoPath) {
        this.id = id;
        this.profileName = profileName;
        this.isDefault = isDefault;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.linkedinUrl = linkedinUrl;
        this.experience = experience;
        this.skills = skills;
        this.education = education;
        this.resumePath = resumePath;
        this.coverLetterPath = coverLetterPath;
        this.photoPath = photoPath;
    }

    // ================================
    // GETTERS & SETTERS
    // ================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProfileName() { return profileName; }
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getExperience() { return experience; }
    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getSkills() { return skills; }
    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getEducation() { return education; }
    public void setEducation(String education) {
        this.education = education;
    }

    public String getResumePath() { return resumePath; }
    public void setResumePath(String resumePath) {
        this.resumePath = resumePath;
    }

    public String getCoverLetterPath() {
        return coverLetterPath;
    }
    public void setCoverLetterPath(String coverLetterPath) {
        this.coverLetterPath = coverLetterPath;
    }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}