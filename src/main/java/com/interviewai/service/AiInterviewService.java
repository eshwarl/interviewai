package com.interviewai.service;

public interface AiInterviewService {

    String generateAiReply(Long interviewId, String candidateMessage, String userEmail);
    public String startInterviewSession(Long interviewId, String email);

}

