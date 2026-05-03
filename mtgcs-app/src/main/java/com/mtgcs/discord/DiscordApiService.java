package com.mtgcs.discord;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Service
public class DiscordApiService {

    private final RestClient restClient;

    public DiscordApiService(@Value("${app.discord-bot-token}") String botToken) {
        this.restClient = RestClient.builder()
                .baseUrl("https://discord.com/api/v10")
                .defaultHeader("Authorization", "Bot " + botToken)
                .build();
    }

    public Optional<DiscordUserResponse> findUser(String discordId) {
        if (discordId == null || !discordId.matches("\\d{17,20}")) {
            throw new IllegalArgumentException("Invalid Discord ID: must be a 17-20 digit snowflake.");
        }
        try {
            DiscordUserResponse user = restClient.get()
                    .uri("/users/{id}", discordId)
                    .retrieve()
                    .body(DiscordUserResponse.class);
            return Optional.ofNullable(user);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND || e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return Optional.empty();
            }
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new IllegalStateException("Discord bot token is invalid or missing required permissions.", e);
            }
            throw e;
        }
    }
}
