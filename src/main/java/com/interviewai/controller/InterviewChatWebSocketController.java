package com.interviewai.controller;

import com.interviewai.service.AiInterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class InterviewChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final AiInterviewService aiInterviewService;

    // 🔹 Candidate sends message
    @MessageMapping("/interview/{interviewId}/message")
    public void handleCandidateMessage(
            @DestinationVariable Long interviewId,
            @Payload String message,
            Principal principal
    ) {

        String email = principal.getName();

        String aiResponse = aiInterviewService.generateAiReply(
                interviewId,
                message,
                email
        );

        messagingTemplate.convertAndSend(
                "/topic/interview/" + interviewId,
                aiResponse
        );
    }

    // 🔹 Start interview session (AI greeting)
    @MessageMapping("/interview/{interviewId}/start")
    public void startInterview(
            @DestinationVariable Long interviewId,
            Principal principal
    ) {

        String email = principal.getName();

        String greeting = aiInterviewService.startInterviewSession(
                interviewId,
                email
        );

        messagingTemplate.convertAndSend(
                "/topic/interview/" + interviewId,
                greeting
        );
    }
}