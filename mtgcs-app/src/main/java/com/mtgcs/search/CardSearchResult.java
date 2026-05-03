package com.mtgcs.search;

import java.util.List;

public record CardSearchResult(
        String cardName,
        List<CardMatch> matches
) {}
