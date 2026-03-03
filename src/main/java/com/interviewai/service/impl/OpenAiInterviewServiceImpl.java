package com.interviewai.service.impl;

import com.interviewai.model.*;
import com.interviewai.repository.*;
import com.interviewai.service.AiInterviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@Profile("prod")
@Transactional
public class OpenAiInterviewServiceImpl implements AiInterviewService {

    private final ChatClient chatClient;
    private final InterviewRepository interviewRepository;
    private final InterviewMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final CandidateProfileRepository profileRepository;

    // 🔒 Prevent parallel AI generation per interview
    private final Set<Long> activeInterviews = ConcurrentHashMap.newKeySet();

    public OpenAiInterviewServiceImpl(ChatClient.Builder builder,
                                      InterviewRepository interviewRepository,
                                      InterviewMessageRepository messageRepository,
                                      SimpMessagingTemplate messagingTemplate,
                                      CandidateProfileRepository profileRepository) {
        this.chatClient = builder.build();
        this.interviewRepository = interviewRepository;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
        this.profileRepository = profileRepository;
    }

    // =========================================================
    // 🔥 STREAM AI RESPONSE (Protected from parallel calls)
    // =========================================================
    @Override
    public String generateAiReply(Long interviewId, String candidateMessage, String userEmail) {

        // 🔒 Prevent duplicate AI calls
        if (!activeInterviews.add(interviewId)) {
            log.warn("⚠ AI already generating for interview {}", interviewId);
            return "busy";
        }

        try {

            Interview interview = interviewRepository.findByIdWithCandidate(interviewId)
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

            List<InterviewMessage> history =
                    messageRepository.findByInterviewOrderByCreatedAtAsc(interview);

            String resumeContext = getResumeContext(userEmail);

            StringBuilder context = new StringBuilder();
            context.append("""
                    You are a professional AI interviewer conducting a technical interview.
                    Ask one question at a time.
                    Keep responses concise and conversational.
                    Evaluate answers and ask follow-up questions.
                    Adjust difficulty based on responses.
                    Role being interviewed for: %s
                    %s
                    """.formatted(interview.getTitle(), resumeContext));

            for (InterviewMessage msg : history) {
                context.append("\n")
                        .append(msg.getSender())
                        .append(": ")
                        .append(msg.getMessage());
            }

            String streamTopic = "/topic/interview/" + interviewId + "/stream";
            StringBuilder fullResponse = new StringBuilder();

            chatClient.prompt()
                    .user(context.toString())
                    .stream()
                    .content()
                    .subscribe(
                            token -> {
                                messagingTemplate.convertAndSend(streamTopic, token);
                                fullResponse.append(token);
                            },
                            error -> {
                                log.error("Streaming error for interview {}: {}", interviewId, error.getMessage());
                                messagingTemplate.convertAndSend(streamTopic, "[ERROR] AI service unavailable.");
                                activeInterviews.remove(interviewId); // 🔓 unlock
                            },
                            () -> {
                                messageRepository.save(
                                        InterviewMessage.builder()
                                                .interview(interview)
                                                .sender("AI")
                                                .message(fullResponse.toString())
                                                .createdAt(LocalDateTime.now())
                                                .build()
                                );

                                messagingTemplate.convertAndSend(streamTopic, "[END]");
                                activeInterviews.remove(interviewId); // 🔓 unlock
                                log.info("Streaming complete for interview {}", interviewId);
                            }
                    );

            return "streaming";

        } catch (Exception ex) {
            activeInterviews.remove(interviewId); // 🔓 safety unlock
            throw ex;
        }
    }

    // =========================================================
    // 🔥 START INTERVIEW SESSION (Protected)
    // =========================================================
    @Override
    public String startInterviewSession(Long interviewId, String email) {

        if (!activeInterviews.add(interviewId)) {
            log.warn("⚠ AI greeting already generating for interview {}", interviewId);
            return "busy";
        }

        try {

            Interview interview = interviewRepository.findByIdWithCandidate(interviewId)
                    .orElseThrow(() -> new RuntimeException("Interview not found"));

            if (!interview.getCandidate().getEmail().equals(email)) {
                throw new RuntimeException("Unauthorized");
            }

            String resumeContext = getResumeContext(email);
            String streamTopic = "/topic/interview/" + interviewId + "/stream";
            StringBuilder fullGreeting = new StringBuilder();

            String prompt = """
                    You are starting a technical interview with %s.
                    Greet warmly and introduce yourself as an AI interviewer.
                    Mention the role: %s.
                    %s
                    Ask them to introduce themselves briefly.
                    Keep it short and professional.
                    """.formatted(
                    interview.getCandidate().getName(),
                    interview.getTitle(),
                    resumeContext.isEmpty() ? "" :
                            "You reviewed their resume. Mention 1 relevant skill."
            );

            chatClient.prompt()
                    .user(prompt)
                    .stream()
                    .content()
                    .subscribe(
                            token -> {
                                messagingTemplate.convertAndSend(streamTopic, token);
                                fullGreeting.append(token);
                            },
                            error -> {
                                log.error("Greeting stream error: {}", error.getMessage());
                                activeInterviews.remove(interviewId);
                            },
                            () -> {
                                messageRepository.save(
                                        InterviewMessage.builder()
                                                .interview(interview)
                                                .sender("AI")
                                                .message(fullGreeting.toString())
                                                .createdAt(LocalDateTime.now())
                                                .build()
                                );

                                messagingTemplate.convertAndSend(streamTopic, "[END]");
                                activeInterviews.remove(interviewId);
                            }
                    );

            return "streaming";

        } catch (Exception ex) {
            activeInterviews.remove(interviewId);
            throw ex;
        }
    }

    // =========================================================
    // 🔧 Resume Context Loader
    // =========================================================
    private String getResumeContext(String email) {
        try {
            return profileRepository
                    .findByUserEmailAndIsDeletedFalse(email)
                    .filter(p -> p.getResumeSummary() != null && !p.getResumeSummary().isEmpty())
                    .map(p -> "\nCandidate Resume Summary:\n" + p.getResumeSummary())
                    .orElse("");
        } catch (Exception e) {
            log.warn("Could not load resume context for {}: {}", email, e.getMessage());
            return "";
        }
    }
}