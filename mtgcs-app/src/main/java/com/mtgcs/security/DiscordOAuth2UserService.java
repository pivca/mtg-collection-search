package com.mtgcs.security;

import com.mtgcs.user.AppUser;
import com.mtgcs.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class DiscordOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(DiscordOAuth2UserService.class);

    private final UserRepository userRepository;

    public DiscordOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String discordId = String.valueOf(attributes.get("id"));
        String username = (String) attributes.get("username");
        String avatar = (String) attributes.get("avatar");
        String avatarUrl = avatar != null
                ? "https://cdn.discordapp.com/avatars/" + discordId + "/" + avatar + ".png"
                : null;

        AppUser user = userRepository.findByDiscordId(discordId)
                .map(existing -> {
                    existing.setUsername(username);
                    existing.setAvatarUrl(avatarUrl);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    log.info("Creating new user for Discord ID: {} ({})", discordId, username);
                    return userRepository.save(new AppUser(discordId, username, avatarUrl));
                });

        return new DiscordOAuth2User(oAuth2User, user.getId());
    }
}
