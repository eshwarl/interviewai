package com.interviewai.dto;

import lombok.Data;

import java.util.List;

@Data
public class CandidateProfileRequest {

    private String resumeUrl;
    private List<String> skills;
    private Integer experienceYears;
    private String targetRole;
    private String education;
    private String linkedinUrl;
    private String githubUrl;
}
