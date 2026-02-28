package com.interviewai.repository;

import com.interviewai.model.InterviewResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewResultRepository
        extends JpaRepository<InterviewResult, Long> {
}
