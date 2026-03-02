package com.interviewai.service.impl;

import com.interviewai.exception.interview.InterviewNotFoundException;
import com.interviewai.exception.interview.InvalidInterviewStateException;
import com.interviewai.exception.interview.UnauthorizedInterviewAccessException;
import com.interviewai.exception.user.UserNotFoundException;
import com.interviewai.model.*;
import com.interviewai.repository.InterviewRepository;
import com.interviewai.repository.UserRepository;
import com.interviewai.service.AiEvaluationService;
import com.interviewai.service.InterviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final AiEvaluationService aiEvaluationService;

    public InterviewServiceImpl(InterviewRepository interviewRepository,
                                UserRepository userRepository,
                                AiEvaluationService aiEvaluationService) {
        this.interviewRepository = interviewRepository;
        this.userRepository = userRepository;
        this.aiEvaluationService = aiEvaluationService;
    }

    // ==============================
    // 1️⃣ ADMIN – Schedule Interview
    // ==============================
    @Override
    public Interview scheduleInterview(String title,
                                       String candidateEmail,
                                       LocalDateTime scheduledTime,
                                       Integer durationMinutes,
                                       String adminEmail) {

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UserNotFoundException(adminEmail));

        if (admin.getRole() != Role.ADMIN) {
            throw new UnauthorizedInterviewAccessException();
        }

        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new UserNotFoundException(candidateEmail));

        if (candidate.getRole() != Role.USER) {
            throw new InvalidInterviewStateException("Selected user is not a candidate");
        }

        String passkey = generatePasskey();

        Interview interview = Interview.builder()
                .title(title)
                .admin(admin)
                .candidate(candidate)
                .scheduledTime(scheduledTime)
                .durationMinutes(durationMinutes)
                .passkey(passkey)
                .Status(InterviewStatus.SCHEDULED)
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Interview scheduled: {} for candidate: {}", title, candidateEmail);
        return interviewRepository.save(interview);
    }

    // ==============================
    // 2️⃣ CANDIDATE – Join Lobby
    // ==============================
    @Override
    public Interview joinLobby(String passkey, String userEmail) {

        Interview interview = interviewRepository.findByPasskey(passkey)
                .orElseThrow(() -> new InvalidInterviewStateException("Invalid passkey"));

        if (!interview.getCandidate().getEmail().equals(userEmail)) {
            throw new UnauthorizedInterviewAccessException();
        }

        if (interview.getStatus() != InterviewStatus.SCHEDULED) {
            throw new InvalidInterviewStateException(
                    "Interview is not in SCHEDULED state. Current state: " + interview.getStatus()
            );
        }

        interview.setStatus(InterviewStatus.LOBBY);

        log.info("Candidate {} joined lobby for interview {}", userEmail, interview.getId());
        return interviewRepository.save(interview);
    }

    // ==============================
    // 3️⃣ CANDIDATE – Start Interview
    // ==============================
    @Override
    public Interview startInterview(Long interviewId, String userEmail) {

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new InterviewNotFoundException(interviewId));

        if (!interview.getCandidate().getEmail().equals(userEmail)) {
            throw new UnauthorizedInterviewAccessException();
        }

        if (interview.getStatus() != InterviewStatus.LOBBY) {
            throw new InvalidInterviewStateException(
                    "Interview must be in LOBBY state to start. Current state: " + interview.getStatus()
            );
        }

        interview.setStatus(InterviewStatus.IN_PROGRESS);

        log.info("Interview {} started by {}", interviewId, userEmail);
        return interviewRepository.save(interview);
    }

    // ==============================
    // 4️⃣ CANDIDATE – End Interview
    // ==============================
    @Override
    public InterviewResult endInterview(Long interviewId, String userEmail) {

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new InterviewNotFoundException(interviewId));

        if (!interview.getCandidate().getEmail().equals(userEmail)) {
            throw new UnauthorizedInterviewAccessException();
        }

        if (interview.getStatus() != InterviewStatus.IN_PROGRESS) {
            throw new InvalidInterviewStateException(
                    "Interview must be IN_PROGRESS to end. Current state: " + interview.getStatus()
            );
        }

        interview.setStatus(InterviewStatus.COMPLETED);
        interviewRepository.save(interview);

        log.info("Interview {} completed by {}", interviewId, userEmail);

        // ✅ Trigger AI evaluation
        return aiEvaluationService.evaluateInterview(interviewId);
    }

    // ==============================
    // 5️⃣ CANDIDATE – View My Interviews
    // ==============================
    @Override
    @Transactional(readOnly = true)
    public List<Interview> getCandidateInterviews(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        return interviewRepository.findByCandidate(user);
    }

    // ==============================
    // Utility
    // ==============================
    private String generatePasskey() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}