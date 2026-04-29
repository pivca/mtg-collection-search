package com.mtgcs.support;

import com.mtgcs.security.DiscordOAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

public final class TestPrincipal {

    private TestPrincipal() {}

    public static DiscordOAuth2User create(Long userId) {
        OAuth2User delegate = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("OAUTH2_USER")),
                Map.of("id", "discord-123", "username", "testuser"),
                "username"
        );
        return new DiscordOAuth2User(delegate, userId);
    }
}
