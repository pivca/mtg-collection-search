package com.mtgcs.collection;

import java.time.Instant;

public record CollectionResponse(
        Long id,
        Long friendId,
        String friendDisplayName,
        SourceType sourceType,
        String sourceUrl,
        Instant addedAt
) {}
