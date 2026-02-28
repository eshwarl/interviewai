CREATE TABLE interview_messages (
    id BIGSERIAL PRIMARY KEY,

    interview_id BIGINT NOT NULL,

    sender VARCHAR(20) NOT NULL,

    message TEXT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_interview_message
        FOREIGN KEY (interview_id)
        REFERENCES interviews(id)
        ON DELETE CASCADE
);
