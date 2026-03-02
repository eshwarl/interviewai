package com.interviewai.controller;

import com.interviewai.service.AiInterviewService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/interview")
public class AiInterviewController {

    private final AiInterviewService aiInterviewService;

    public AiInterviewController(AiInterviewService aiInterviewService) {
        this.aiInterviewService = aiInterviewService;
    }

    @PostMapping("/{interviewId}/chat")
    public String chatWithAi(@PathVariable Long interviewId,
                             @RequestBody String message,
                             Authentication authentication) {

        String email = authentication.getName();

        return aiInterviewService.generateAiReply(interviewId, message, email);
    }
//    @PostMapping("/{interviewId}/start")
//    public String startInterview(@PathVariable Long interviewId,
//                                 Authentication authentication) {
//
//        String email = authentication.getName();
//
//        return aiInterviewService.startInterviewSession(interviewId, email);
//    }

}
