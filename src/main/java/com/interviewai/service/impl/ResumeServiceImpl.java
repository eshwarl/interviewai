package com.interviewai.service.impl;

import com.interviewai.model.CandidateProfile;
import com.interviewai.model.User;
import com.interviewai.repository.CandidateProfileRepository;
import com.interviewai.repository.UserRepository;
import com.interviewai.service.ResumeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@Profile("prod")
public class ResumeServiceImpl implements ResumeService {

    private final CandidateProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ChatClient chatClient;

    public ResumeServiceImpl(
            CandidateProfileRepository profileRepository,
            UserRepository userRepository,
            ChatClient.Builder builder) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.chatClient = builder.build();
    }

    // =======================================
    // 🔥 UPLOAD + EXTRACT + AI PARSE + SAVE
    // =======================================
    @Override
    public CandidateProfile uploadAndParseResume(MultipartFile file, String email) {

        // Step 1 — Get user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2 — Get or create candidate profile
        CandidateProfile profile = profileRepository
                .findByUserAndIsDeletedFalse(user)
                .orElse(CandidateProfile.builder()
                        .user(user)
                        .isDeleted(false)
                        .createdAt(LocalDateTime.now())
                        .build());

        // Step 3 — Extract text from PDF
        String extractedText = extractTextFromPdf(file);
        log.info("Extracted {} characters from resume for {}", extractedText.length(), email);

        // Step 4 — AI parse resume
        String summary = parseResumeWithAi(extractedText);
        log.info("AI parsed resume for {}", email);

        // Step 5 — Extract skills from AI response
        List<String> skills = extractSkillsFromSummary(summary);

        // Step 6 — Save everything to DB
        profile.setResumeText(extractedText);
        profile.setResumeSummary(summary);
        profile.setSkills(skills);
        profile.setUpdatedAt(LocalDateTime.now());

        return profileRepository.save(profile);
    }

    // =======================================
    // 🔥 RE-PARSE EXISTING RESUME
    // =======================================
    @Override
    public CandidateProfile reparseResume(String email) {

        CandidateProfile profile = profileRepository
                .findByUserEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (profile.getResumeText() == null || profile.getResumeText().isEmpty()) {
            throw new RuntimeException("No resume uploaded yet. Please upload a PDF first.");
        }

        String summary = parseResumeWithAi(profile.getResumeText());
        List<String> skills = extractSkillsFromSummary(summary);

        profile.setResumeSummary(summary);
        profile.setSkills(skills);
        profile.setUpdatedAt(LocalDateTime.now());

        return profileRepository.save(profile);
    }

    // =======================================
    // 🔧 EXTRACT TEXT FROM PDF using PDFBox
    // =======================================
    private String extractTextFromPdf(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to read PDF file. Please upload a valid PDF.");
        }
    }
    // =======================================
    // 🤖 PARSE RESUME WITH GROQ AI
    // =======================================
    private String parseResumeWithAi(String resumeText) {

        // Truncate if too long (Groq has token limits)
        String truncated = resumeText.length() > 4000
                ? resumeText.substring(0, 4000)
                : resumeText;

        String prompt = """
                You are a resume parser. Analyze this resume and extract key information.
                
                Return STRICTLY in this format:
                
                SUMMARY: <2-3 sentence professional summary>
                SKILLS: <comma separated list of technical skills>
                EXPERIENCE: <X years>
                CURRENT_ROLE: <most recent job title>
                EDUCATION: <highest degree and institution>
                STRENGTHS: <3 key strengths based on experience>
                GAPS: <potential skill gaps for a software engineer role>
                
                Resume:
                """ + truncated;

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    // =======================================
    // 🔧 EXTRACT SKILLS LIST FROM AI RESPONSE
    // =======================================
    private List<String> extractSkillsFromSummary(String summary) {
        try {
            // Find the SKILLS line and extract comma-separated values
            String[] lines = summary.split("\n");
            for (String line : lines) {
                if (line.startsWith("SKILLS:")) {
                    String skillsStr = line.replace("SKILLS:", "").trim();
                    return Arrays.stream(skillsStr.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract skills from summary: {}", e.getMessage());
        }
        return List.of();
    }
}