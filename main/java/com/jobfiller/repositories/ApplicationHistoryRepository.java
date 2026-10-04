package com.jobfiller.repositories;


import com.jobfiller.model.ApplicationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationHistoryRepository extends JpaRepository<ApplicationHistory, Long> {

    // Check duplicate by URL
    boolean existsByFormUrl(String formUrl);

    // Find by company name
    Optional<ApplicationHistory> findByCompanyName(String companyName);

    // Find by URL
    Optional<ApplicationHistory> findByFormUrl(String formUrl);

    // Get all history
    List<ApplicationHistory> findAll();

    // Get all by status
    List<ApplicationHistory> findByStatus(String status);

    // Get all successful applications
    List<ApplicationHistory> findByStatusOrderByAppliedAtDesc(
            String status);
}
