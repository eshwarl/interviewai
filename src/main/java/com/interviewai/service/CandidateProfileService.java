package com.interviewai.service;

import com.interviewai.dto.CandidateProfileRequest;
import com.interviewai.model.CandidateProfile;

public interface CandidateProfileService {

    CandidateProfile createProfile(CandidateProfileRequest request, String userEmail);

    CandidateProfile getProfile(String userEmail);

    CandidateProfile updateProfile(CandidateProfileRequest request, String userEmail);

    void deleteProfile(String userEmail);
}
