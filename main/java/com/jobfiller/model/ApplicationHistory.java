// ApplicationHistory.java
package com.jobfiller.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "application_history")
public class ApplicationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "form_url")
    private String formUrl;

    @Column(name = "profile_used")
    private String profileUsed;

    @Column(name = "status")
    private String status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "screenshot_path")
    private String screenshotPath;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    // ================================
    // CONSTRUCTORS
    // ================================
    public ApplicationHistory() {}

    public ApplicationHistory(Long id, String companyName,
            String jobTitle, String formUrl,
            String profileUsed, String status,
            String errorMessage, String screenshotPath,
            LocalDateTime appliedAt) {
        this.id = id;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.formUrl = formUrl;
        this.profileUsed = profileUsed;
        this.status = status;
        this.errorMessage = errorMessage;
        this.screenshotPath = screenshotPath;
        this.appliedAt = appliedAt;
    }

    // ================================
    // AUTO SET DATE
    // ================================
    @PrePersist
    public void prePersist() {
        appliedAt = LocalDateTime.now();
    }

    // ================================
    // GETTERS & SETTERS
    // ================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getFormUrl() { return formUrl; }
    public void setFormUrl(String formUrl) {
        this.formUrl = formUrl;
    }

    public String getProfileUsed() { return profileUsed; }
    public void setProfileUsed(String profileUsed) {
        this.profileUsed = profileUsed;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }
    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}
