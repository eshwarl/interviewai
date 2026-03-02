package com.interviewai.service;

import com.interviewai.model.CandidateProfile;
import org.springframework.web.multipart.MultipartFile;


public interface ResumeService {

    // 🔥 Upload PDF → extract text → AI parse → save to profile
    CandidateProfile uploadAndParseResume(MultipartFile file, String email);

    // 🔥 Re-parse existing resume with AI (if resume already uploaded)
    CandidateProfile reparseResume(String email);
}