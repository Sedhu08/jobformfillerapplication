package com.jobfiller.service;

// 2. CompanyLoginService.java

import com.jobfiller.model.CompanyLogin;
import com.jobfiller.repositories.CompanyLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CompanyLoginService {

    @Autowired
    private CompanyLoginRepository loginRepository;


    // SAVE LOGIN

    public CompanyLogin saveLogin(CompanyLogin login) {
        // If login exists for URL update it
        Optional<CompanyLogin> existing =
                loginRepository.findByPortalUrl(login.getPortalUrl());

        if (existing.isPresent()) {
            CompanyLogin update = existing.get();
            update.setEmail(login.getEmail());
            update.setPassword(login.getPassword());
            update.setCompanyName(login.getCompanyName());
            update.setIsWorkday(login.getIsWorkday());
            return loginRepository.save(update);
        }
        return loginRepository.save(login);
    }


    // GET LOGIN BY URL

    public Optional<CompanyLogin> getLoginByUrl(String url) {
        // Extract base URL for matching
        // e.g. https://amazon.jobs/apply/123 → amazon.jobs
        String baseUrl = extractBaseUrl(url);
        return loginRepository.findByPortalUrl(baseUrl);
    }


    // GET ALL LOGINS

    public List<CompanyLogin> getAllLogins() {
        return loginRepository.findAll();
    }


    // DELETE LOGIN

    public void deleteLogin(String portalUrl) {
        loginRepository.deleteByPortalUrl(portalUrl);
    }


    // CHECK IF LOGIN EXISTS

    public boolean loginExists(String portalUrl) {
        return loginRepository.existsByPortalUrl(portalUrl);
    }


    // EXTRACT BASE URL

    private String extractBaseUrl(String url) {
        // https://amazon.jobs/apply/123
        // → amazon.jobs
        try {
            String withoutProtocol = url
                    .replace("https://", "")
                    .replace("http://", "");
            return withoutProtocol.split("/")[0];
        } catch (Exception e) {
            return url;
        }
    }
}
