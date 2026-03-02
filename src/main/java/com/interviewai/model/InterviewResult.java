package com.interviewai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "interview_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 Only expose id and title from Interview — prevent circular reference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "admin", "candidate", "passkey"})
    private Interview interview;

    private Integer technicalScore;
    private Integer communicationScore;
    private Integer confidenceScore;

    @Column(columnDefinition = "TEXT")
    private String aiFeedback;
}