package com.interviewai.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // Admin who scheduled interview
    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    // Candidate who will attend
    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private String passkey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus Status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
