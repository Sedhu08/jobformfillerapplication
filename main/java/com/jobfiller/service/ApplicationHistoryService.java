package com.jobfiller.service;

import com.jobfiller.model.ApplicationHistory;
import com.jobfiller.repositories.ApplicationHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationHistoryService {

    @Autowired
    private ApplicationHistoryRepository historyRepository;

    // ================================
    // SAVE APPLICATION
    // ================================
    public ApplicationHistory saveApplication(
            ApplicationHistory history) {
        return historyRepository.save(history);
    }

    // ================================
    // CHECK DUPLICATE
    // ================================
    public boolean isDuplicate(String formUrl) {
        return historyRepository.existsByFormUrl(formUrl);
    }

    // ================================
    // GET PREVIOUS APPLICATION
    // ================================
    public Optional<ApplicationHistory> getPreviousApplication(
            String formUrl) {
        return historyRepository.findByFormUrl(formUrl);
    }

    // ================================
    // GET ALL HISTORY
    // ================================
    public List<ApplicationHistory> getAllHistory() {
        return historyRepository.findAll();
    }

    // ================================
    // GET SUCCESSFUL APPLICATIONS
    // ================================
    public List<ApplicationHistory> getSuccessfulApplications() {
        return historyRepository
                .findByStatusOrderByAppliedAtDesc("SUCCESS");
    }

    // ================================
    // GET FAILED APPLICATIONS
    // ================================
    public List<ApplicationHistory> getFailedApplications() {
        return historyRepository
                .findByStatusOrderByAppliedAtDesc("FAILED");
    }

    // ================================
    // SAVE SUCCESS
    // ================================
    public void saveSuccess(String companyName,
                            String jobTitle,
                            String formUrl,
                            String profileUsed,
                            String screenshotPath) {
        ApplicationHistory history = new ApplicationHistory();
        history.setCompanyName(companyName);
        history.setJobTitle(jobTitle);
        history.setFormUrl(formUrl);
        history.setProfileUsed(profileUsed);
        history.setStatus("SUCCESS");
        history.setScreenshotPath(screenshotPath);
        historyRepository.save(history);
    }

    // ================================
    // SAVE FAILURE
    // ================================
    public void saveFailure(String companyName,
                            String formUrl,
                            String profileUsed,
                            String errorMessage,
                            String screenshotPath) {
        ApplicationHistory history = new ApplicationHistory();
        history.setCompanyName(companyName);
        history.setFormUrl(formUrl);
        history.setProfileUsed(profileUsed);
        history.setStatus("FAILED");
        history.setErrorMessage(errorMessage);
        history.setScreenshotPath(screenshotPath);
        historyRepository.save(history);
    }
}
