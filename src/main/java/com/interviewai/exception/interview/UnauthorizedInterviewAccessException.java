package com.interviewai.exception.interview;

public class UnauthorizedInterviewAccessException extends RuntimeException {
    public UnauthorizedInterviewAccessException() {
        super("You are not authorized to access this interview");
    }
}