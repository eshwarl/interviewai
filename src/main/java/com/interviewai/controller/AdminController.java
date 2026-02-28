package com.interviewai.controller;

import com.interviewai.model.Interview;
import com.interviewai.service.InterviewService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final InterviewService interviewService;

    public AdminController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

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
                title,
                candidateEmail,
                LocalDateTime.parse(scheduledTime),
                durationMinutes,
                adminEmail
        );
    }
}
