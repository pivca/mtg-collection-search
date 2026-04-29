package com.mtgcs.friend;

import com.mtgcs.config.GlobalExceptionHandler;
import com.mtgcs.security.DiscordOAuth2User;
import com.mtgcs.support.TestPrincipal;
import com.mtgcs.user.AppUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FriendController.class)
@Import(GlobalExceptionHandler.class)
class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FriendService friendService;

    @MockitoBean
    private FriendMapper friendMapper;

    private DiscordOAuth2User principal;
    private AppUser owner;

    @BeforeEach
    void setUp() {
        principal = TestPrincipal.create(1L);
        owner = new AppUser("123456", "testuser", null);
    }

    @Test
    void list_returnsFriends() throws Exception {
        Friend friend = new Friend(owner, "Alice", "111");
        FriendResponse response = new FriendResponse(1L, "Alice", "111", Instant.now());
        when(friendService.listFriends(1L)).thenReturn(List.of(friend));
        when(friendMapper.toResponse(friend)).thenReturn(response);

        mockMvc.perform(get("/api/friends")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                                .oauth2User(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].displayName").value("Alice"))
                .andExpect(jsonPath("$[0].discordId").value("111"));
    }

    @Test
    void list_redirectsToLoginWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/friends"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void create_returnsCreated() throws Exception {
        Friend friend = new Friend(owner, "Bob", null);
        FriendResponse response = new FriendResponse(2L, "Bob", null, Instant.now());
        when(friendService.createFriend(eq(1L), any(CreateFriendRequest.class))).thenReturn(friend);
        when(friendMapper.toResponse(friend)).thenReturn(response);

        mockMvc.perform(post("/api/friends")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                                .oauth2User(principal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateFriendRequest("Bob", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.displayName").value("Bob"));
    }

    @Test
    void create_returnsBadRequestForBlankName() throws Exception {
        mockMvc.perform(post("/api/friends")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                                .oauth2User(principal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateFriendRequest("", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_returnsUpdatedFriend() throws Exception {
        Friend friend = new Friend(owner, "Alice Updated", "999");
        FriendResponse response = new FriendResponse(1L, "Alice Updated", "999", Instant.now());
        when(friendService.updateFriend(eq(1L), eq(1L), any(UpdateFriendRequest.class))).thenReturn(friend);
        when(friendMapper.toResponse(friend)).thenReturn(response);

        mockMvc.perform(put("/api/friends/1")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                                .oauth2User(principal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateFriendRequest("Alice Updated", "999"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Alice Updated"));
    }

    @Test
    void update_returnsNotFoundForMissingFriend() throws Exception {
        when(friendService.updateFriend(eq(1L), eq(99L), any(UpdateFriendRequest.class)))
                .thenThrow(new FriendNotFoundException(99L));

        mockMvc.perform(put("/api/friends/99")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                                .oauth2User(principal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateFriendRequest("x", null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/friends/1")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                                .oauth2User(principal))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_returnsNotFoundForMissingFriend() throws Exception {
        doThrow(new FriendNotFoundException(99L)).when(friendService).deleteFriend(1L, 99L);

        mockMvc.perform(delete("/api/friends/99")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                                .oauth2User(principal))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
