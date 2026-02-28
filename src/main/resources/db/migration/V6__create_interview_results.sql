CREATE TABLE interview_results (
    id BIGSERIAL PRIMARY KEY,
    interview_id BIGINT UNIQUE NOT NULL,
    technical_score INT,
    communication_score INT,
    confidence_score INT,
    ai_feedback TEXT,
    CONSTRAINT fk_interview
        FOREIGN KEY(interview_id)
        REFERENCES interviews(id)
        ON DELETE CASCADE
);
