package com.mtgcs.search;

import java.util.List;

public record SearchResponse(
        List<CardSearchResult> results,
        List<FriendBuylist> buylist,
        String formattedBuylist
) {
    public SearchResponse {
        results = List.copyOf(results);
        buylist = List.copyOf(buylist);
    }
}
