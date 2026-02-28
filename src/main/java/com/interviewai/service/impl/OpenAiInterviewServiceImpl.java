package com.interviewai.service.impl;

import com.interviewai.model.*;
import com.interviewai.repository.*;
import com.interviewai.service.AiInterviewService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("prod") // ✅ Only active in production
public class OpenAiInterviewServiceImpl implements AiInterviewService {

    private final ChatClient chatClient;
    private final InterviewRepository interviewRepository;
    private final InterviewMessageRepository messageRepository;

    public OpenAiInterviewServiceImpl(ChatClient.Builder builder,
                                      InterviewRepository interviewRepository,
                                      InterviewMessageRepository messageRepository) {

        this.chatClient = builder.build();
        this.interviewRepository = interviewRepository;
        this.messageRepository = messageRepository;
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

        // Fetch conversation history
        List<InterviewMessage> history =
                messageRepository.findByInterviewOrderByCreatedAtAsc(interview);

        StringBuilder context = new StringBuilder();

        context.append("""
                You are a professional AI interviewer.
                Ask role-based technical questions.
                Maintain conversational tone.
                Analyze confidence based on response clarity.
                """);

        for (InterviewMessage msg : history) {
            context.append("\n")
                    .append(msg.getSender())
                    .append(": ")
                    .append(msg.getMessage());
        }

        String aiResponse = chatClient.prompt()
                .user(context.toString())
                .call()
                .content();

        // Save AI response
        messageRepository.save(
                InterviewMessage.builder()
                        .interview(interview)
                        .sender("AI")
                        .message(aiResponse)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return aiResponse;
    }

    @Override
    public String startInterviewSession(Long interviewId, String email) {

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found"));

        if (!interview.getCandidate().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        if (interview.getStatus() != InterviewStatus.IN_PROGRESS) {
            throw new RuntimeException("Interview must be IN_PROGRESS");
        }

        String greeting = "Good morning " + interview.getCandidate().getName() +
                ". Shall we begin the interview?";

        InterviewMessage aiMessage = InterviewMessage.builder()
                .interview(interview)
                .sender("AI")
                .message(greeting)
                .createdAt(LocalDateTime.now())
                .build();

        // ✅ FIXED: Save using messageRepository
        messageRepository.save(aiMessage);

        return greeting;
    }
}
