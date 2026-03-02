package com.interviewai.controller;

import com.interviewai.model.Interview;
import com.interviewai.model.InterviewResult;
import com.interviewai.repository.InterviewRepository;
import com.interviewai.repository.InterviewResultRepository;
import com.interviewai.service.InterviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final InterviewService interviewService;
    private final InterviewRepository interviewRepository;
    private final InterviewResultRepository interviewResultRepository;

    public AdminController(InterviewService interviewService,
                           InterviewRepository interviewRepository,
                           InterviewResultRepository interviewResultRepository) {
        this.interviewService = interviewService;
        this.interviewRepository = interviewRepository;
        this.interviewResultRepository = interviewResultRepository;
    }

    // 1. Schedule Interview
    @PostMapping("/schedule")
    public Interview scheduleInterview(
            @RequestParam String title,
            @RequestParam String candidateEmail,
            @RequestParam String scheduledTime,
            @RequestParam Integer durationMinutes,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        return interviewService.scheduleInterview(
                title, candidateEmail,
                LocalDateTime.parse(scheduledTime),
                durationMinutes, adminEmail
        );
    }

    // 2. Get All Interviews (newest first)
    @GetMapping("/interviews")
    public List<Interview> getAllInterviews() {
        return interviewRepository.findAllByOrderByCreatedAtDesc();
    }

    // 3. Get All Results
    @GetMapping("/results")
    public List<InterviewResult> getAllResults() {
        return interviewResultRepository.findAll();
    }

    // 4. Get Result by Interview ID
    @GetMapping("/results/{interviewId}")
    public InterviewResult getResultByInterview(@PathVariable Long interviewId) {
        return interviewResultRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new RuntimeException("Result not found for interview: " + interviewId));
    }
}