package com.interviewai.controller;

import com.interviewai.exception.interview.InterviewNotFoundException;
import com.interviewai.model.Interview;
import com.interviewai.model.InterviewResult;
import com.interviewai.repository.InterviewResultRepository;
import com.interviewai.service.AiEvaluationService;
import com.interviewai.service.InterviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class CandidateController {

    private final InterviewService interviewService;
    private final AiEvaluationService aiEvaluationService;
    private final InterviewResultRepository interviewResultRepository;

    public CandidateController(InterviewService interviewService,
                               AiEvaluationService aiEvaluationService,
                               InterviewResultRepository interviewResultRepository) {
        this.interviewService = interviewService;
        this.aiEvaluationService = aiEvaluationService;
        this.interviewResultRepository = interviewResultRepository;
    }

    // 1️⃣ Join Lobby using Passkey
    @PostMapping("/join")
    public Interview joinLobby(@RequestParam String passkey,
                               Authentication authentication) {
        String userEmail = authentication.getName();
        return interviewService.joinLobby(passkey, userEmail);
    }

    // 2️⃣ Start Interview
//    @PostMapping("/start/{id}")
//    public Interview startInterview(@PathVariable Long id,
//                                    Authentication authentication) {
//        String userEmail = authentication.getName();
//        return interviewService.startInterview(id, userEmail);
//    }
    // 2️⃣ Start Interview
    @PostMapping("/{interviewId}/start")
    public Interview startInterview(@PathVariable Long interviewId,
                                    Authentication authentication) {

        String userEmail = authentication.getName();
        return interviewService.startInterview(interviewId, userEmail);
    }


    // 3️⃣ End Interview
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

    // 5️⃣ 🔥 Get Interview Results by Interview ID
    @GetMapping("/results/{interviewId}")
    public InterviewResult getResults(@PathVariable Long interviewId,
                                      Authentication authentication) {
        log.info("Fetching results for interview: {} by user: {}", interviewId, authentication.getName());

        return interviewResultRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new InterviewNotFoundException(interviewId));
    }
}