package com.interviewai.service.impl;

import com.interviewai.model.Interview;
import com.interviewai.model.InterviewResult;
import com.interviewai.repository.InterviewRepository;
import com.interviewai.repository.InterviewResultRepository;
import com.interviewai.service.AiEvaluationService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Profile("dev")
public class MockAiEvaluationServiceImpl implements AiEvaluationService {

    private final InterviewRepository interviewRepository;
    private final InterviewResultRepository resultRepository;

    public MockAiEvaluationServiceImpl(
            InterviewRepository interviewRepository,
            InterviewResultRepository resultRepository) {

        this.interviewRepository = interviewRepository;
        this.resultRepository = resultRepository;
    }

    @Override
    public InterviewResult evaluateInterview(Long interviewId) {

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found"));

        // 🔥 Generate realistic random scores (out of 10)
        Random random = new Random();

        int technical = 6 + random.nextInt(5);       // 6–10
        int communication = 5 + random.nextInt(5);   // 5–9
        int confidence = 5 + random.nextInt(5);      // 5–9

        String feedback = """
TechnicalScore: %d
CommunicationScore: %d
ConfidenceScore: %d
Feedback: Candidate demonstrated good understanding of core concepts. 
Needs improvement in structured communication and confidence handling.
""".formatted(technical, communication, confidence);

        InterviewResult result = InterviewResult.builder()
                .interview(interview)
                .technicalScore(technical)
                .communicationScore(communication)
                .confidenceScore(confidence)
                .aiFeedback(feedback)
//                .createdAt(LocalDateTime.now())
                .build();

        return resultRepository.save(result);
    }
}
