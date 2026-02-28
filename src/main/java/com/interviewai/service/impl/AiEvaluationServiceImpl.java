package com.interviewai.service.impl;

import com.interviewai.model.Interview;
import com.interviewai.model.InterviewMessage;
import com.interviewai.model.InterviewResult;
import com.interviewai.repository.InterviewMessageRepository;
import com.interviewai.repository.InterviewRepository;
import com.interviewai.repository.InterviewResultRepository;
import com.interviewai.service.AiEvaluationService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("prod")
public class AiEvaluationServiceImpl implements AiEvaluationService {

    private final InterviewRepository interviewRepository;
    private final InterviewMessageRepository messageRepository;
    private final InterviewResultRepository resultRepository;
    private final ChatClient chatClient;

    public AiEvaluationServiceImpl(
            InterviewRepository interviewRepository,
            InterviewMessageRepository messageRepository,
            InterviewResultRepository resultRepository,
            ChatClient.Builder builder) {

        this.interviewRepository = interviewRepository;
        this.messageRepository = messageRepository;
        this.resultRepository = resultRepository;
        this.chatClient = builder.build(); // 🔥 important
    }

    @Override
    public InterviewResult evaluateInterview(Long interviewId) {

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found"));

        List<InterviewMessage> history =
                messageRepository.findByInterviewOrderByCreatedAtAsc(interview);

        StringBuilder transcript = new StringBuilder();

        for (InterviewMessage msg : history) {
            transcript.append(msg.getSender())
                    .append(": ")
                    .append(msg.getMessage())
                    .append("\n");
        }

        String prompt = """
You are an expert technical interviewer.

Analyze the following interview transcript.

Give output STRICTLY in this format:

TechnicalScore: <number out of 10>
CommunicationScore: <number out of 10>
ConfidenceScore: <number out of 10>
Feedback: <short paragraph>

Transcript:
""" + transcript;

        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        int tech = extractScore(aiResponse, "TechnicalScore:");
        int comm = extractScore(aiResponse, "CommunicationScore:");
        int conf = extractScore(aiResponse, "ConfidenceScore:");

        InterviewResult result = InterviewResult.builder()
                .interview(interview)
                .technicalScore(tech)
                .communicationScore(comm)
                .confidenceScore(conf)
                .aiFeedback(aiResponse)
//                .createdAt(java.time.LocalDateTime.now())
                .build();

        return resultRepository.save(result);
    }

    private int extractScore(String text, String label) {
        try {
            int start = text.indexOf(label) + label.length();
            String number = text.substring(start).trim().split("\\n")[0];
            return Integer.parseInt(number.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}


