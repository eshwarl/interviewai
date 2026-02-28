package com.interviewai.model;

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

    @OneToOne
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    private Integer technicalScore;
    private Integer communicationScore;
    private Integer confidenceScore;

    @Column(columnDefinition = "TEXT")
    private String aiFeedback;
}
