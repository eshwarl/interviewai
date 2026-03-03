
package com.interviewai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebRtcSignalingController {

    private final SimpMessagingTemplate messagingTemplate;

    // Candidate sends offer → broadcast to admin monitor
    @MessageMapping("/interview/{interviewId}/webrtc/offer")
    public void handleOffer(
            @DestinationVariable Long interviewId,
            @Payload String offerJson
    ) {
        log.info("WebRTC offer received for interview {}", interviewId);
        messagingTemplate.convertAndSend(
                "/topic/interview/" + interviewId + "/webrtc/offer",
                offerJson
        );
    }

    // Admin sends answer → broadcast to candidate
    @MessageMapping("/interview/{interviewId}/webrtc/answer")
    public void handleAnswer(
            @DestinationVariable Long interviewId,
            @Payload String answerJson
    ) {
        log.info("WebRTC answer received for interview {}", interviewId);
        messagingTemplate.convertAndSend(
                "/topic/interview/" + interviewId + "/webrtc/answer",
                answerJson
        );
    }

    // ICE candidates exchange
    @MessageMapping("/interview/{interviewId}/webrtc/ice")
    public void handleIceCandidate(
            @DestinationVariable Long interviewId,
            @Payload String iceJson
    ) {
        messagingTemplate.convertAndSend(
                "/topic/interview/" + interviewId + "/webrtc/ice",
                iceJson
        );
    }

    // Candidate notifies admin that stream is ready
    @MessageMapping("/interview/{interviewId}/webrtc/ready")
    public void handleReady(
            @DestinationVariable Long interviewId,
            @Payload String payload
    ) {
        log.info("Candidate camera ready for interview {}", interviewId);
        messagingTemplate.convertAndSend(
                "/topic/interview/" + interviewId + "/webrtc/ready",
                payload
        );
    }

    // 🔥 CANDIDATE RESPONSES - Broadcast to admin monitor
    @MessageMapping("/interview/{interviewId}/response")
    public void handleCandidateResponse(
            @DestinationVariable Long interviewId,
            @Payload String response
    ) {
        log.info("Candidate response received for interview {}: {}", interviewId, response);
        messagingTemplate.convertAndSend(
                "/topic/interview/" + interviewId + "/response",
                response
        );
    }

    // 🔥 ADMIN READY SIGNAL - Notify candidate that admin is monitoring
    @MessageMapping("/interview/{interviewId}/admin-ready")
    public void handleAdminReady(
            @DestinationVariable Long interviewId,
            @Payload String payload
    ) {
        log.info("Admin monitoring started for interview {}", interviewId);
        messagingTemplate.convertAndSend(
                "/topic/interview/" + interviewId + "/admin-ready",
                payload
        );
    }
}