package com.mtgcs.search;

import com.mtgcs.security.DiscordOAuth2User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping
    public List<CardSearchResult> search(
            @AuthenticationPrincipal DiscordOAuth2User principal,
            @Valid @RequestBody CardSearchRequest request) {
        return searchService.search(principal.getUserId(), request.cardNames());
    }
}
