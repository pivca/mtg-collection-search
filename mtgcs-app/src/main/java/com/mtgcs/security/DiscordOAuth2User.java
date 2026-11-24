package com.mtgcs.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/** Stored in the HTTP session, which is persisted via Spring Session JDBC, so it must be serializable. */
public class DiscordOAuth2User implements OAuth2User, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final OAuth2User delegate;
    private final Long userId;

    public DiscordOAuth2User(OAuth2User delegate, Long userId) {
        this.delegate = delegate;
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
