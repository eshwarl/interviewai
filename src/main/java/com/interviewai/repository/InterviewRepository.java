package com.interviewai.repository;

import com.interviewai.model.Interview;
import com.interviewai.model.InterviewMessage;
import com.interviewai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface InterviewRepository extends JpaRepository<Interview, Long> {
    static void save(InterviewMessage aiMessage) {
    }

    Optional<Interview> findByPasskey(String passkey);

    List<Interview> findByCandidate(User candidate);
}
