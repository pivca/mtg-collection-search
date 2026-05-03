package com.mtgcs.search;

import com.mtgcs.collection.SourceType;

public record CardMatch(
        Long friendId,
        String friendName,
        Long collectionId,
        String sourceUrl,
        SourceType sourceType,
        int quantity,
        String price,
        String cardPageUrl,
        String edition,
        String language,
        boolean foil
) {}
