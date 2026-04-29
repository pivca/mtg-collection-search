package com.mtgcs.friend;

import com.mtgcs.security.DiscordOAuth2User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final FriendMapper friendMapper;

    @GetMapping
    public List<FriendResponse> list(@AuthenticationPrincipal DiscordOAuth2User principal) {
        return friendService.listFriends(principal.getUserId()).stream()
                .map(friendMapper::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<FriendResponse> create(
            @AuthenticationPrincipal DiscordOAuth2User principal,
            @Valid @RequestBody CreateFriendRequest request) {
        Friend friend = friendService.createFriend(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(friendMapper.toResponse(friend));
    }

    @PutMapping("/{id}")
    public FriendResponse update(
            @AuthenticationPrincipal DiscordOAuth2User principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateFriendRequest request) {
        Friend friend = friendService.updateFriend(principal.getUserId(), id, request);
        return friendMapper.toResponse(friend);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal DiscordOAuth2User principal,
            @PathVariable Long id) {
        friendService.deleteFriend(principal.getUserId(), id);
    }
}
