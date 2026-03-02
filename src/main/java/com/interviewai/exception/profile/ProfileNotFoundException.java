package com.interviewai.exception.profile;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(String email) {
        super("Candidate profile not found for: " + email);
    }
}