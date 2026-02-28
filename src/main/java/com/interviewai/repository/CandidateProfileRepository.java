package com.interviewai.repository;

import com.interviewai.model.CandidateProfile;
import com.interviewai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateProfileRepository
        extends JpaRepository<CandidateProfile, Long> {

    Optional<CandidateProfile> findByUser(User user);
    Optional<CandidateProfile> findByUserAndIsDeletedFalse(User user);

}
