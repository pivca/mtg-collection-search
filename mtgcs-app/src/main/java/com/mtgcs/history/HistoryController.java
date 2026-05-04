package com.mtgcs.history;

import com.mtgcs.security.DiscordOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public List<HistoryEntryResponse> get(@AuthenticationPrincipal DiscordOAuth2User principal) {
        return historyService.getHistory(principal.getUserId());
    }
}
