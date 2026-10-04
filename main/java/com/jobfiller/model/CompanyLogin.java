package com.jobfiller.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_logins")
public class CompanyLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "portal_url")
    private String portalUrl;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "is_workday")
    private Boolean isWorkday = false;

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ================================
    // CONSTRUCTORS
    // ================================
    public CompanyLogin() {}

    public CompanyLogin(Long id, String companyName,
            String portalUrl, String email,
            String password, Boolean isWorkday,
            LocalDateTime lastUsed,
            LocalDateTime createdAt) {
        this.id = id;
        this.companyName = companyName;
        this.portalUrl = portalUrl;
        this.email = email;
        this.password = password;
        this.isWorkday = isWorkday;
        this.lastUsed = lastUsed;
        this.createdAt = createdAt;
    }

    // ================================
    // AUTO SET DATES
    // ================================
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        lastUsed  = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        lastUsed = LocalDateTime.now();
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

    public String getPortalUrl() { return portalUrl; }
    public void setPortalUrl(String portalUrl) {
        this.portalUrl = portalUrl;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getIsWorkday() { return isWorkday; }
    public void setIsWorkday(Boolean isWorkday) {
        this.isWorkday = isWorkday;
    }

    public LocalDateTime getLastUsed() { return lastUsed; }
    public void setLastUsed(LocalDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

