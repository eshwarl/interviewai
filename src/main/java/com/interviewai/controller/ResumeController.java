package com.interviewai.controller;

import com.interviewai.model.CandidateProfile;
import com.interviewai.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    // =======================================
    // 🔥 Upload + Parse Resume
    // POST /api/resume/upload
    // =======================================
    @PostMapping("/upload")
    public ResponseEntity<CandidateProfile> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        String email = authentication.getName();

        log.info("Resume upload request from {}", email);

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (!file.getOriginalFilename().endsWith(".pdf")) {
            return ResponseEntity.badRequest().build();
        }

        CandidateProfile profile = resumeService.uploadAndParseResume(file, email);

        return ResponseEntity.ok(profile);
    }

    // =======================================
    // 🔥 Re-parse existing resume
    // POST /api/resume/reparse
    // =======================================
    @PostMapping("/reparse")
    public ResponseEntity<CandidateProfile> reparseResume(
            Authentication authentication) {

        String email = authentication.getName();

        log.info("Resume reparse request from {}", email);

        CandidateProfile profile = resumeService.reparseResume(email);

        return ResponseEntity.ok(profile);
    }

    // =======================================
    // 🔥 Get current resume summary
    // GET /api/resume/summary
    // =======================================
    @GetMapping("/summary")
    public ResponseEntity<String> getResumeSummary(
            Authentication authentication) {

        String email = authentication.getName();

        CandidateProfile profile = resumeService.reparseResume(email);

        if (profile.getResumeSummary() == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(profile.getResumeSummary());
    }
}