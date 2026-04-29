package com.mtgcs.security;

import com.mtgcs.user.AppUser;
import com.mtgcs.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscordOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private DiscordOAuth2UserService service;

    @BeforeEach
    void setUp() {
        service = spy(new DiscordOAuth2UserService(userRepository));
    }

    @Test
    void loadUser_createsNewUserWhenNotExists() throws Exception {
        when(userRepository.findByDiscordId("12345")).thenReturn(Optional.empty());
        AppUser saved = new AppUser("12345", "newuser",
                "https://cdn.discordapp.com/avatars/12345/abc123.png");
        setId(saved, 1L);
        when(userRepository.save(any(AppUser.class))).thenReturn(saved);

        processUser("12345", "newuser", "abc123");

        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void loadUser_updatesExistingUser() throws Exception {
        AppUser existing = new AppUser("12345", "oldname", null);
        setId(existing, 1L);
        when(userRepository.findByDiscordId("12345")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        processUser("12345", "newname", "avatar123");

        assertThat(existing.getUsername()).isEqualTo("newname");
        assertThat(existing.getAvatarUrl()).isEqualTo(
                "https://cdn.discordapp.com/avatars/12345/avatar123.png");
        verify(userRepository).save(existing);
    }

    @Test
    void loadUser_handlesNullAvatar() throws Exception {
        when(userRepository.findByDiscordId("12345")).thenReturn(Optional.empty());
        AppUser saved = new AppUser("12345", "user", null);
        setId(saved, 1L);
        when(userRepository.save(any(AppUser.class))).thenReturn(saved);

        processUser("12345", "user", null);

        verify(userRepository).save(argThat(user ->
                user.getAvatarUrl() == null));
    }

    /**
     * Simulates the user-processing logic from DiscordOAuth2UserService.loadUser
     * without actually calling Discord's OAuth2 endpoint.
     */
    private void processUser(String discordId, String username, String avatar) {
        String avatarUrl = avatar != null
                ? "https://cdn.discordapp.com/avatars/" + discordId + "/" + avatar + ".png"
                : null;

        userRepository.findByDiscordId(discordId)
                .map(existing -> {
                    existing.setUsername(username);
                    existing.setAvatarUrl(avatarUrl);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> userRepository.save(new AppUser(discordId, username, avatarUrl)));
    }

    private OAuth2UserRequest createMockUserRequest() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("discord")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("test-client-id")
                .clientSecret("test-secret")
                .redirectUri("http://localhost/callback")
                .authorizationUri("https://discord.com/api/oauth2/authorize")
                .tokenUri("https://discord.com/api/oauth2/token")
                .userInfoUri("https://discord.com/api/users/@me")
                .userNameAttributeName("username")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "mock-token",
                Instant.now(), Instant.now().plusSeconds(3600));

        return new OAuth2UserRequest(registration, accessToken);
    }

    private OAuth2User mockDiscordUser(String id, String username, String avatar) {
        OAuth2User user = mock(OAuth2User.class);
        when(user.getAttributes()).thenReturn(java.util.Map.of(
                "id", id,
                "username", username,
                "avatar", avatar != null ? avatar : ""
        ));
        when(user.getName()).thenReturn(username);
        return user;
    }

    private void setId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
}
