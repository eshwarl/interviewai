//package com.interviewai.service.impl;
//
//import com.interviewai.model.*;
//import com.interviewai.repository.*;
//import com.interviewai.service.AiInterviewService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.context.annotation.Profile;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Flux;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
//@Service
//@Profile("prod")
//public class OpenAiInterviewServiceImpl implements AiInterviewService {
//
//    private final ChatClient chatClient;
//    private final InterviewRepository interviewRepository;
//    private final InterviewMessageRepository messageRepository;
//    private final SimpMessagingTemplate messagingTemplate;
//    private final CandidateProfileRepository profileRepository; // 🔥 NEW
//
//    public OpenAiInterviewServiceImpl(ChatClient.Builder builder,
//                                      InterviewRepository interviewRepository,
//                                      InterviewMessageRepository messageRepository,
//                                      SimpMessagingTemplate messagingTemplate,
//                                      CandidateProfileRepository profileRepository) { // 🔥 NEW
//        this.chatClient = builder.build();
//        this.interviewRepository = interviewRepository;
//        this.messageRepository = messageRepository;
//        this.messagingTemplate = messagingTemplate;
//        this.profileRepository = profileRepository;
//    }
//
//    // =======================================
//    // 🔥 REAL-TIME STREAMING AI REPLY
//    // =======================================
//    @Override
//    public String generateAiReply(Long interviewId, String candidateMessage, String userEmail) {
//
//        Interview interview = interviewRepository.findById(interviewId)
//                .orElseThrow(() -> new RuntimeException("Interview not found"));
//
//        if (!interview.getCandidate().getEmail().equals(userEmail)) {
//            throw new RuntimeException("Unauthorized");
//        }
//
//        // ✅ Save candidate message
//        messageRepository.save(
//                InterviewMessage.builder()
//                        .interview(interview)
//                        .sender("CANDIDATE")
//                        .message(candidateMessage)
//                        .createdAt(LocalDateTime.now())
//                        .build()
//        );
//
//        // ✅ Build conversation history
//        List<InterviewMessage> history =
//                messageRepository.findByInterviewOrderByCreatedAtAsc(interview);
//
//        // 🔥 Get resume context if available
//        String resumeContext = getResumeContext(userEmail);
//
//        // ✅ Build full prompt with resume context
//        StringBuilder context = new StringBuilder();
//        context.append("""
//                You are a professional AI interviewer conducting a technical interview.
//                Ask one question at a time.
//                Keep responses concise and conversational.
//                Evaluate the candidate's answers and ask follow-up questions.
//                Adjust difficulty based on their responses.
//                Role being interviewed for: %s
//                %s
//                """.formatted(interview.getTitle(), resumeContext));
//
//        for (InterviewMessage msg : history) {
//            context.append("\n")
//                    .append(msg.getSender())
//                    .append(": ")
//                    .append(msg.getMessage());
//        }
//
//        String streamTopic = "/topic/interview/" + interviewId + "/stream";
//        StringBuilder fullResponse = new StringBuilder();
//
//        // 🔥 STREAM tokens word by word via WebSocket
//        Flux<String> stream = chatClient.prompt()
//                .user(context.toString())
//                .stream()
//                .content();
//
//        stream.subscribe(
//                token -> {
//                    messagingTemplate.convertAndSend(streamTopic, token);
//                    fullResponse.append(token);
//                },
//                error -> {
//                    log.error("Streaming error for interview {}: {}", interviewId, error.getMessage());
//                    messagingTemplate.convertAndSend(streamTopic, "[ERROR] AI service unavailable.");
//                },
//                () -> {
//                    messageRepository.save(
//                            InterviewMessage.builder()
//                                    .interview(interview)
//                                    .sender("AI")
//                                    .message(fullResponse.toString())
//                                    .createdAt(LocalDateTime.now())
//                                    .build()
//                    );
//                    messagingTemplate.convertAndSend(streamTopic, "[END]");
//                    log.info("Streaming complete for interview {}", interviewId);
//                }
//        );
//
//        return "streaming";
//    }
//
//    // =======================================
//    // 🔥 START INTERVIEW WITH STREAMING GREETING
//    // =======================================
//    @Override
//    public String startInterviewSession(Long interviewId, String email) {
//
//        Interview interview = interviewRepository.findById(interviewId)
//                .orElseThrow(() -> new RuntimeException("Interview not found"));
//
//        if (!interview.getCandidate().getEmail().equals(email)) {
//            throw new RuntimeException("Unauthorized");
//        }
//
//        if (interview.getStatus() != InterviewStatus.IN_PROGRESS &&
//                interview.getStatus() != InterviewStatus.LOBBY) {
//            throw new RuntimeException("Interview must be in LOBBY or IN_PROGRESS state");
//        }
//
//        // 🔥 Get resume context for personalized greeting
//        String resumeContext = getResumeContext(email);
//
//        String streamTopic = "/topic/interview/" + interviewId + "/stream";
//        StringBuilder fullGreeting = new StringBuilder();
//
//        String prompt = """
//                You are starting a technical interview with %s.
//                Greet them warmly, introduce yourself as an AI interviewer.
//                Mention the role they are interviewing for: %s.
//                %s
//                Ask them to introduce themselves briefly.
//                Keep it short, friendly and professional.
//                """.formatted(
//                interview.getCandidate().getName(),
//                interview.getTitle(),
//                resumeContext.isEmpty() ? "" : "You have reviewed their resume. Mention 1 specific skill from their background to make them feel comfortable."
//        );
//
//        // 🔥 Stream greeting word by word
//        chatClient.prompt()
//                .user(prompt)
//                .stream()
//                .content()
//                .subscribe(
//                        token -> {
//                            messagingTemplate.convertAndSend(streamTopic, token);
//                            fullGreeting.append(token);
//                        },
//                        error -> {
//                            log.error("Greeting stream error: {}", error.getMessage());
//                        },
//                        () -> {
//                            messageRepository.save(
//                                    InterviewMessage.builder()
//                                            .interview(interview)
//                                            .sender("AI")
//                                            .message(fullGreeting.toString())
//                                            .createdAt(LocalDateTime.now())
//                                            .build()
//                            );
//                            messagingTemplate.convertAndSend(streamTopic, "[END]");
//                        }
//                );
//
//        return "streaming";
//    }
//
//    // =======================================
//    // 🔧 HELPER — Get resume context from DB
//    // =======================================
//    private String getResumeContext(String email) {
//        try {
//            return profileRepository
//                    .findByUserEmailAndIsDeletedFalse(email)
//                    .filter(p -> p.getResumeSummary() != null && !p.getResumeSummary().isEmpty())
//                    .map(p -> "\nCandidate Resume Summary:\n" + p.getResumeSummary())
//                    .orElse("");
//        } catch (Exception e) {
//            log.warn("Could not load resume context for {}: {}", email, e.getMessage());
//            return "";
//        }
//    }
//}


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
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public String generateAiReply(Long interviewId, String candidateMessage, String userEmail) {

        Interview interview = interviewRepository.findByIdWithCandidate(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found"));

        if (!interview.getCandidate().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }

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
                Evaluate the candidate's answers and ask follow-up questions.
                Adjust difficulty based on their responses.
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
                            log.info("Streaming complete for interview {}", interviewId);
                        }
                );

        return "streaming";
    }

    @Override
    public String startInterviewSession(Long interviewId, String email) {

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
                Greet them warmly, introduce yourself as an AI interviewer.
                Mention the role they are interviewing for: %s.
                %s
                Ask them to introduce themselves briefly.
                Keep it short, friendly and professional.
                """.formatted(
                interview.getCandidate().getName(),
                interview.getTitle(),
                resumeContext.isEmpty() ? "" :
                        "You have reviewed their resume. Mention 1 specific skill from their background."
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
                        error -> log.error("Greeting stream error: {}", error.getMessage()),
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
                        }
                );

        return "streaming";
    }

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