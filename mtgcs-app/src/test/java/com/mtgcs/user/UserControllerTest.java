package com.mtgcs.user;

import com.mtgcs.security.DiscordOAuth2User;
import com.mtgcs.support.TestPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    private DiscordOAuth2User principal;

    @BeforeEach
    void setUp() {
        principal = TestPrincipal.create(1L);
    }

    @Test
    void me_returnsCurrentUser() throws Exception {
        AppUser user = new AppUser("discord-123", "testuser", "https://cdn.discordapp.com/avatars/123/abc.png");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                                .oauth2User(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.discordId").value("discord-123"));
    }

    @Test
    void me_returnsNotFoundWhenUserMissing() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                                .oauth2User(principal)))
                .andExpect(status().isNotFound());
    }

    @Test
    void me_redirectsToLoginWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().is3xxRedirection());
    }
}
