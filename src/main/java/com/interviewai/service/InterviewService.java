package com.interviewai.service;

import com.interviewai.model.Interview;
import com.interviewai.model.InterviewResult;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewService {

    Interview scheduleInterview(
            String title,
            String candidateEmail,
            LocalDateTime scheduledTime,
            Integer durationMinutes,
            String adminEmail
    );

    Interview joinLobby(String passkey, String userEmail);

    Interview startInterview(Long interviewId, String userEmail);
    InterviewResult endInterview(Long interviewId, String userEmail);


    List<Interview> getCandidateInterviews(String userEmail);
}