package com.interviewai.controller;

import com.interviewai.service.AiInterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class InterviewChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final AiInterviewService aiInterviewService;

    // =======================================
    // 🔥 Candidate sends a message
    // Frontend sends to: /app/interview/{id}/message
    // AI streams back to: /topic/interview/{id}/stream
    // =======================================
    @MessageMapping("/interview/{interviewId}/message")
    public void handleCandidateMessage(
            @DestinationVariable Long interviewId,
            @Payload String message,
            Principal principal
    ) {
        String email = principal.getName();

        log.info("Message received from {} for interview {}: {}", email, interviewId, message);

        // 🔥 This now streams tokens via WebSocket internally
        aiInterviewService.generateAiReply(interviewId, message, email);
    }

    // =======================================
    // 🔥 Candidate starts interview session
    // Frontend sends to: /app/interview/{id}/start
    // AI streams greeting to: /topic/interview/{id}/stream
    // =======================================
    @MessageMapping("/interview/{interviewId}/start")
    public void startInterview(
            @DestinationVariable Long interviewId,
            Principal principal
    ) {
        String email = principal.getName();

        log.info("Interview {} started by {}", interviewId, email);

        // 🔥 Streams greeting word by word
        aiInterviewService.startInterviewSession(interviewId, email);
    }

    // =======================================
    // 🔥 Handle errors gracefully
    // =======================================
    @MessageExceptionHandler
    public void handleException(
            Exception ex,
            @DestinationVariable Long interviewId
    ) {
        log.error("WebSocket error for interview {}: {}", interviewId, ex.getMessage());

        messagingTemplate.convertAndSend(
                "/topic/interview/" + interviewId + "/stream",
                "[ERROR] Something went wrong. Please try again."
        );
    }
}