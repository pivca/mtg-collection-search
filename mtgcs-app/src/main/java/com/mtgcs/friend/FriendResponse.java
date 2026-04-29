package com.mtgcs.friend;

import java.time.Instant;

public record FriendResponse(
        Long id,
        String displayName,
        String discordId,
        Instant createdAt
) {}
