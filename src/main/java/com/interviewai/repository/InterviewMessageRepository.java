package com.interviewai.repository;

import com.interviewai.model.Interview;
import com.interviewai.model.InterviewMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewMessageRepository extends JpaRepository<InterviewMessage, Long> {

    List<InterviewMessage> findByInterviewOrderByCreatedAtAsc(Interview interview);
}
