package com.interviewai.repository;

import com.interviewai.model.InterviewResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterviewResultRepository extends JpaRepository<InterviewResult, Long> {

    // 🔥 Find result by interview ID
    Optional<InterviewResult> findByInterviewId(Long interviewId);
}