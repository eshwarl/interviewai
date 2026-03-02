package com.interviewai.exception;

import com.interviewai.exception.auth.InvalidCredentialsException;
import com.interviewai.exception.auth.UserAlreadyExistsException;
import com.interviewai.exception.interview.InterviewNotFoundException;
import com.interviewai.exception.interview.InvalidInterviewStateException;
import com.interviewai.exception.interview.UnauthorizedInterviewAccessException;
import com.interviewai.exception.profile.ProfileNotFoundException;
import com.interviewai.exception.resume.ResumeParseException;
import com.interviewai.exception.user.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ Auth exceptions
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // ✅ User exceptions
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ✅ Interview exceptions
    @ExceptionHandler(InterviewNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleInterviewNotFound(InterviewNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidInterviewStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidState(InvalidInterviewStateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedInterviewAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedAccess(UnauthorizedInterviewAccessException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // ✅ Profile exceptions
    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProfileNotFound(ProfileNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ✅ Resume exceptions
    @ExceptionHandler(ResumeParseException.class)
    public ResponseEntity<Map<String, Object>> handleResumeParse(ResumeParseException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    // ✅ File too large
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds 5MB limit");
    }

    // ✅ Catch all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again.");
    }

    // 🔧 Helper — builds consistent JSON response
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}