package com.mtgcs.collection;

import com.mtgcs.config.GlobalExceptionHandler;
import com.mtgcs.friend.FriendNotFoundException;
import com.mtgcs.security.DiscordOAuth2User;
import com.mtgcs.support.TestPrincipal;
import com.mtgcs.friend.Friend;
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

@WebMvcTest(CollectionController.class)
@Import(GlobalExceptionHandler.class)
class CollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CollectionService collectionService;

    @MockitoBean
    private CollectionMapper collectionMapper;

    private DiscordOAuth2User principal;
    private AppUser owner;
    private Friend friend;

    @BeforeEach
    void setUp() {
        principal = TestPrincipal.create(1L);
        owner = new AppUser("123456", "testuser", null);
        friend = new Friend(owner, "Alice", null);
    }

    @Test
    void list_returnsCollections() throws Exception {
        Collection col = new Collection(friend, SourceType.DECKBOX, "https://deckbox.org/sets/123");
        CollectionResponse response = new CollectionResponse(
                1L, 10L, "Alice", SourceType.DECKBOX, "https://deckbox.org/sets/123", Instant.now());
        when(collectionService.listCollections(1L, 10L)).thenReturn(List.of(col));
        when(collectionMapper.toResponse(col)).thenReturn(response);

        mockMvc.perform(get("/api/friends/10/collections")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                                .oauth2User(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sourceType").value("DECKBOX"))
                .andExpect(jsonPath("$[0].friendDisplayName").value("Alice"));
    }

    @Test
    void list_returnsNotFoundWhenFriendMissing() throws Exception {
        when(collectionService.listCollections(1L, 99L)).thenThrow(new FriendNotFoundException(99L));

        mockMvc.perform(get("/api/friends/99/collections")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                                .oauth2User(principal)))
                .andExpect(status().isNotFound());
    }

    @Test
    void add_returnsCreated() throws Exception {
        Collection col = new Collection(friend, SourceType.DECKBOX, "https://deckbox.org/sets/123");
        CollectionResponse response = new CollectionResponse(
                1L, 10L, "Alice", SourceType.DECKBOX, "https://deckbox.org/sets/123", Instant.now());
        when(collectionService.addCollection(eq(1L), eq(10L), any(CreateCollectionRequest.class))).thenReturn(col);
        when(collectionMapper.toResponse(col)).thenReturn(response);

        mockMvc.perform(post("/api/friends/10/collections")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                                .oauth2User(principal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCollectionRequest(SourceType.DECKBOX, "https://deckbox.org/sets/123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceUrl").value("https://deckbox.org/sets/123"));
    }

    @Test
    void add_returnsBadRequestForInvalidUrl() throws Exception {
        mockMvc.perform(post("/api/friends/10/collections")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                                .oauth2User(principal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCollectionRequest(SourceType.DECKBOX, "https://example.com/bad"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/collections/1")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                                .oauth2User(principal))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_returnsNotFoundForMissingCollection() throws Exception {
        doThrow(new CollectionNotFoundException(99L)).when(collectionService).deleteCollection(1L, 99L);

        mockMvc.perform(delete("/api/collections/99")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                                .oauth2User(principal))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_redirectsToLoginWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/friends/10/collections"))
                .andExpect(status().is3xxRedirection());
    }
}
