package com.interviewai.controller;

import com.interviewai.dto.CandidateProfileRequest;
import com.interviewai.model.CandidateProfile;
import com.interviewai.service.CandidateProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {

    private final CandidateProfileService candidateProfileService;

    public UserProfileController(CandidateProfileService candidateProfileService) {
        this.candidateProfileService = candidateProfileService;
    }

    // 🔹 CREATE PROFILE
    @PostMapping
    public CandidateProfile createProfile(
            @RequestBody CandidateProfileRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        return candidateProfileService.createProfile(request, userEmail);
    }

    // 🔹 GET PROFILE
    @GetMapping
    public CandidateProfile getProfile(Authentication authentication) {

        String userEmail = authentication.getName();
        return candidateProfileService.getProfile(userEmail);
    }

    // 🔹 UPDATE PROFILE
    @PutMapping
    public CandidateProfile updateProfile(
            @RequestBody CandidateProfileRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        return candidateProfileService.updateProfile(request, userEmail);
    }

    // 🔹 SOFT DELETE PROFILE
    @DeleteMapping
    public String deleteProfile(Authentication authentication) {

        String userEmail = authentication.getName();
        candidateProfileService.deleteProfile(userEmail);
        return "Profile deleted successfully";
    }
}
