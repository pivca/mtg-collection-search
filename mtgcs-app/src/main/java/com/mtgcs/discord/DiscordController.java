package com.mtgcs.discord;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discord")
public class DiscordController {

    private final DiscordApiService discordApiService;

    public DiscordController(DiscordApiService discordApiService) {
        this.discordApiService = discordApiService;
    }

    @GetMapping("/users/{discordId}")
    public ResponseEntity<DiscordUserLookupResponse> lookupUser(@PathVariable String discordId) {
        return discordApiService.findUser(discordId)
                .map(u -> ResponseEntity.ok(new DiscordUserLookupResponse(
                        u.id(),
                        u.username(),
                        u.displayName(),
                        u.avatarUrl()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    public record DiscordUserLookupResponse(
            String id,
            String username,
            String displayName,
            String avatarUrl
    ) {}
}
