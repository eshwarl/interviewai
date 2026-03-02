package com.interviewai.service.impl;

import com.interviewai.model.CandidateProfile;
import com.interviewai.model.User;
import com.interviewai.repository.CandidateProfileRepository;
import com.interviewai.repository.UserRepository;
import com.interviewai.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Profile("dev")
@RequiredArgsConstructor
public class MockResumeServiceImpl implements ResumeService {

    private final CandidateProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Override
    public CandidateProfile uploadAndParseResume(MultipartFile file, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CandidateProfile profile = profileRepository
                .findByUserAndIsDeletedFalse(user)
                .orElse(CandidateProfile.builder()
                        .user(user)
                        .isDeleted(false)
                        .createdAt(LocalDateTime.now())
                        .build());

        // 🔥 Mock data for dev testing
        profile.setResumeText("Mock resume text for " + email);
        profile.setResumeSummary("""
                SUMMARY: Experienced software engineer with 3 years of experience.
                SKILLS: Java, Spring Boot, React, PostgreSQL, Docker
                EXPERIENCE: 3 years
                CURRENT_ROLE: Software Engineer
                EDUCATION: B.Tech Computer Science
                STRENGTHS: Problem solving, Backend development, System design
                GAPS: Kubernetes, Cloud architecture
                """);
        profile.setSkills(List.of("Java", "Spring Boot", "React", "PostgreSQL", "Docker"));
        profile.setUpdatedAt(LocalDateTime.now());

        log.info("Mock resume uploaded for {}", email);
        return profileRepository.save(profile);
    }

    @Override
    public CandidateProfile reparseResume(String email) {
        return profileRepository
                .findByUserEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }
}
