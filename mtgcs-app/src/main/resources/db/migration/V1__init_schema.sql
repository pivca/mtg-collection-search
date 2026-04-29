CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    discord_id  VARCHAR(64)  NOT NULL UNIQUE,
    username    VARCHAR(128) NOT NULL,
    avatar_url  VARCHAR(512),
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE friends (
    id            BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    discord_id    VARCHAR(64),
    display_name  VARCHAR(128) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_friends_owner ON friends(owner_user_id);

CREATE TABLE collections (
    id          BIGSERIAL PRIMARY KEY,
    friend_id   BIGINT       NOT NULL REFERENCES friends(id) ON DELETE CASCADE,
    source_type VARCHAR(32)  NOT NULL,
    source_url  VARCHAR(1024) NOT NULL,
    added_at    TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT chk_source_type CHECK (source_type IN ('DECKBOX', 'MOXFIELD'))
);

CREATE INDEX idx_collections_friend ON collections(friend_id);
