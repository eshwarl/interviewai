package com.interviewai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public AiEvaluationServiceImpl(
            InterviewRepository interviewRepository,
            InterviewMessageRepository messageRepository,
            InterviewResultRepository resultRepository,
            ChatClient.Builder builder,
            ObjectMapper objectMapper) {

        this.interviewRepository = interviewRepository;
        this.messageRepository = messageRepository;
        this.resultRepository = resultRepository;
        this.chatClient = builder.build();
        this.objectMapper = objectMapper;
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

Return ONLY valid JSON in this exact format:

{
  "technicalScore": number (0-10),
  "communicationScore": number (0-10),
  "confidenceScore": number (0-10),
  "feedback": "short paragraph"
}

Do NOT include markdown.
Do NOT include explanation.
Only raw JSON.

Transcript:
""" + transcript;

        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        try {
            JsonNode node = objectMapper.readTree(aiResponse);

            int tech = node.get("technicalScore").asInt();
            int comm = node.get("communicationScore").asInt();
            int conf = node.get("confidenceScore").asInt();
            String feedback = node.get("feedback").asText();

            InterviewResult result = InterviewResult.builder()
                    .interview(interview)
                    .technicalScore(tech)
                    .communicationScore(comm)
                    .confidenceScore(conf)
                    .aiFeedback(feedback)
                    .build();

            return resultRepository.save(result);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response: " + aiResponse);
        }
    }
}