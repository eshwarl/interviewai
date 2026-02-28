CREATE TABLE interviews (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,

    admin_id BIGINT NOT NULL,
    candidate_id BIGINT NOT NULL,

    scheduled_time TIMESTAMP NOT NULL,
    duration_minutes INT NOT NULL,

    passkey VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_admin FOREIGN KEY (admin_id) REFERENCES users(id),
    CONSTRAINT fk_candidate FOREIGN KEY (candidate_id) REFERENCES users(id)
);
