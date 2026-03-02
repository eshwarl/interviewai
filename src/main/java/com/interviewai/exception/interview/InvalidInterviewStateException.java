package com.interviewai.exception.interview;

public class InvalidInterviewStateException extends RuntimeException {
    public InvalidInterviewStateException(String message) {
        super(message);
    }
}