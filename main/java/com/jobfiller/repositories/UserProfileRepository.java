package com.jobfiller.repositories;

import com.jobfiller.model.UserProfile;

import jakarta.persistence.criteria.From;
import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // Find profile by name
    Optional<UserProfile> findByProfileName(String profileName);

    // Find default profile
    Optional<UserProfile> findByIsDefaultTrue();

    // Get all profile names
    List<UserProfile> findAll();

    // Check if profile name exists
    boolean existsByProfileName(String profileName);


    void deleteByProfileName(String profileName);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserProfile p WHERE p.profileName= :profileName")
    void deleteByProfileNameQuery(@Param("profileName") String profileName);


    
}


