package com.interviewai.exception.interview;

public class InterviewNotFoundException extends RuntimeException {
    public InterviewNotFoundException(Long id) {
        super("Interview not found with id: " + id);
    }
}