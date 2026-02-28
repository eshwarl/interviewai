package com.interviewai.service.impl;

import com.interviewai.model.*;
import com.interviewai.repository.*;
import com.interviewai.service.AiInterviewService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("dev")
public class MockAiInterviewServiceImpl implements AiInterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewMessageRepository messageRepository;

    public MockAiInterviewServiceImpl(InterviewRepository interviewRepository,
                                      InterviewMessageRepository messageRepository) {
        this.interviewRepository = interviewRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public String startInterviewSession(Long interviewId, String email) {

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found"));

        if (!interview.getCandidate().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        String greeting = "Welcome " + interview.getCandidate().getName() +
                ". This is a mock AI interview. Tell me about yourself.";

        messageRepository.save(
                InterviewMessage.builder()
                        .interview(interview)
                        .sender("AI")
                        .message(greeting)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return greeting;
    }

    @Override
    public String generateAiReply(Long interviewId, String candidateMessage, String userEmail) {

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found"));

        if (!interview.getCandidate().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        // Save candidate message
        messageRepository.save(
                InterviewMessage.builder()
                        .interview(interview)
                        .sender("CANDIDATE")
                        .message(candidateMessage)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // Fetch history
        List<InterviewMessage> history =
                messageRepository.findByInterviewOrderByCreatedAtAsc(interview);

        String reply;

        String msg = candidateMessage.toLowerCase();

        if (msg.contains("java")) {
            reply = "What are the differences between HashMap and ConcurrentHashMap?";
        } else if (msg.contains("react")) {
            reply = "Explain how Virtual DOM works.";
        } else if (msg.contains("database")) {
            reply = "What is indexing and why is it important?";
        } else {
            reply = "Can you explain that in more detail?";
        }

        // Save AI reply
        messageRepository.save(
                InterviewMessage.builder()
                        .interview(interview)
                        .sender("AI")
                        .message(reply)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return reply;
    }
}
