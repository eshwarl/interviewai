package com.interviewai.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "candidate_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String resumeUrl;

    // 🔥 NEW — raw text extracted from PDF
    @Column(columnDefinition = "TEXT")
    private String resumeText;

    // 🔥 NEW — AI parsed summary
    @Column(columnDefinition = "TEXT")
    private String resumeSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> skills;

    private Integer experienceYears;

    private String targetRole;

    private String education;

    private String linkedinUrl;

    private String githubUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}
