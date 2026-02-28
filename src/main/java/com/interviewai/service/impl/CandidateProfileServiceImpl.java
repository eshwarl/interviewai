package com.interviewai.service.impl;

import com.interviewai.dto.CandidateProfileRequest;
import com.interviewai.model.CandidateProfile;
import com.interviewai.model.User;
import com.interviewai.repository.CandidateProfileRepository;
import com.interviewai.repository.UserRepository;
import com.interviewai.service.CandidateProfileService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CandidateProfileServiceImpl implements CandidateProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;

    public CandidateProfileServiceImpl(
            CandidateProfileRepository candidateProfileRepository,
            UserRepository userRepository) {

        this.candidateProfileRepository = candidateProfileRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CandidateProfile createProfile(CandidateProfileRequest request, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (candidateProfileRepository.findByUserAndIsDeletedFalse(user).isPresent()) {
            throw new RuntimeException("Profile already exists");
        }

        CandidateProfile profile = CandidateProfile.builder()
                .user(user)
                .resumeUrl(request.getResumeUrl())
                .skills(request.getSkills())
                .experienceYears(request.getExperienceYears())
                .targetRole(request.getTargetRole())
                .education(request.getEducation())
                .linkedinUrl(request.getLinkedinUrl())
                .githubUrl(request.getGithubUrl())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        return candidateProfileRepository.save(profile);
    }

    @Override
    public CandidateProfile getProfile(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return candidateProfileRepository.findByUserAndIsDeletedFalse(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    @Override
    public CandidateProfile updateProfile(CandidateProfileRequest request, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CandidateProfile profile = candidateProfileRepository
                .findByUserAndIsDeletedFalse(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setResumeUrl(request.getResumeUrl());
        profile.setSkills(request.getSkills());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setTargetRole(request.getTargetRole());
        profile.setEducation(request.getEducation());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setGithubUrl(request.getGithubUrl());
        profile.setUpdatedAt(LocalDateTime.now());

        return candidateProfileRepository.save(profile);
    }

    @Override
    public void deleteProfile(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CandidateProfile profile = candidateProfileRepository
                .findByUserAndIsDeletedFalse(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setIsDeleted(true);
        profile.setUpdatedAt(LocalDateTime.now());

        candidateProfileRepository.save(profile);
    }
}
