package com.mtgcs.user;

import com.mtgcs.security.DiscordOAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public record UserResponse(Long id, String discordId, String username, String avatarUrl) {}
}
