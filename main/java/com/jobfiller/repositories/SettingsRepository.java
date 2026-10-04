package com.jobfiller.repositories;

// 3. SettingsRepository.java


import com.jobfiller.model.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SettingsRepository
        extends JpaRepository<Settings, Long> {

    // Always get first settings record
    Optional<Settings> findFirstBy();
}
