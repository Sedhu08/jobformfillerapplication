package com.jobfiller.repositories;

import com.jobfiller.model.CompanyLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyLoginRepository
        extends JpaRepository<CompanyLogin, Long> {

    // Find login by portal URL
    Optional<CompanyLogin> findByPortalUrl(String portalUrl);

    // Find login by company name
    Optional<CompanyLogin> findByCompanyName(String companyName);

    // Check if login exists for URL
    boolean existsByPortalUrl(String portalUrl);

    // Get all saved logins
    List<CompanyLogin> findAll();

    // Delete by portal URL
    void deleteByPortalUrl(String portalUrl);
}
