package com.mtgcs.user;

import com.mtgcs.security.DiscordOAuth2User;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal DiscordOAuth2User principal) {
        return userRepository.findById(principal.getUserId())
                .map(user -> ResponseEntity.ok(new UserResponse(
                        user.getId(),
                        user.getDiscordId(),
                        user.getUsername(),
                        user.getAvatarUrl()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<UserResponse> search(
            @RequestParam String username,
            @AuthenticationPrincipal DiscordOAuth2User principal) {
        if (username.isBlank() || username.length() < 2) return List.of();
        return userRepository
                .findByUsernameContainingIgnoreCase(username.strip(), PageRequest.of(0, 10))
                .stream()
                .filter(u -> !u.getId().equals(principal.getUserId()))
                .map(u -> new UserResponse(u.getId(), u.getDiscordId(), u.getUsername(), u.getAvatarUrl()))
                .toList();
    }

    public record UserResponse(Long id, String discordId, String username, String avatarUrl) {}
}
