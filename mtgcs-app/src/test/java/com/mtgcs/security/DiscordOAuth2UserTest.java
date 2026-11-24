package com.mtgcs.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.util.SerializationUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordOAuth2UserTest {

    @Test
    @SuppressWarnings("deprecation")
    void survivesSerializationForPersistentSessions() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("id", "123456789");
        attributes.put("username", "kasper");
        attributes.put("avatar", null);
        DiscordOAuth2User user = new DiscordOAuth2User(
                new DefaultOAuth2User(
                        List.of(new OAuth2UserAuthority(attributes), new SimpleGrantedAuthority("SCOPE_identify")),
                        attributes,
                        "username"),
                42L);

        DiscordOAuth2User restored = (DiscordOAuth2User) SerializationUtils.deserialize(SerializationUtils.serialize(user));

        assertThat(restored.getUserId()).isEqualTo(42L);
        assertThat(restored.getName()).isEqualTo("kasper");
        assertThat(restored.getAttributes()).containsEntry("id", "123456789");
        assertThat(restored.getAuthorities()).hasSize(2);
    }
}
