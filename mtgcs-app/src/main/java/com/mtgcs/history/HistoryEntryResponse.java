package com.mtgcs.history;

import java.time.Instant;

public record HistoryEntryResponse(
        Long id,
        ActionType actionType,
        String summary,
        String payload,
        Instant createdAt
) {}
