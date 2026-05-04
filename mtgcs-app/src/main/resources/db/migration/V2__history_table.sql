CREATE TABLE history (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action_type VARCHAR(32)  NOT NULL,
    summary     VARCHAR(512) NOT NULL,
    payload     TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_history_user_created ON history(user_id, created_at DESC);
