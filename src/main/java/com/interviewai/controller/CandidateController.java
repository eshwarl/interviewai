package com.interviewai.controller;

import com.interviewai.model.Interview;
import com.interviewai.model.InterviewResult;
import com.interviewai.service.AiEvaluationService;
import com.interviewai.service.InterviewService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class CandidateController {

    private final InterviewService interviewService;
    private final AiEvaluationService aiEvaluationService;

    public CandidateController(InterviewService interviewService, AiEvaluationService aiEvaluationService) {
        this.interviewService = interviewService;
        this.aiEvaluationService = aiEvaluationService;
    }

    // 1️⃣ Join Lobby using Passkey
    @PostMapping("/join")
    public Interview joinLobby(@RequestParam String passkey,
                               Authentication authentication) {

        String userEmail = authentication.getName();

        return interviewService.joinLobby(passkey, userEmail);
    }

    // 2️⃣ Start Interview
    @PostMapping("/start/{id}")
    public Interview startInterview(@PathVariable Long id,
                                    Authentication authentication) {

        String userEmail = authentication.getName();

        return interviewService.startInterview(id, userEmail);
    }

    // 3️⃣ End Interview
//    @PostMapping("/end/{id}")
//    public Interview endInterview(@PathVariable Long id,
//                                  Authentication authentication) {
//
//        String userEmail = authentication.getName();
//
//        return interviewService.endInterview(id, userEmail);
//
//
//    }
    @PostMapping("/{interviewId}/end")
    public InterviewResult endInterview(@PathVariable Long interviewId,
                                        Authentication authentication) {

        String email = authentication.getName();

        return interviewService.endInterview(interviewId, email);
    }


    // 4️⃣ View My Interviews
    @GetMapping("/my-interviews")
    public List<Interview> getMyInterviews(Authentication authentication) {

        String userEmail = authentication.getName();

        return interviewService.getCandidateInterviews(userEmail);
    }
}