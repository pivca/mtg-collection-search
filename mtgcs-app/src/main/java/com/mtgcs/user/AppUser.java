package com.mtgcs.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discord_id", nullable = false, unique = true)
    private String discordId;

    @Setter
    @Column(nullable = false)
    private String username;

    @Setter
    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AppUser(String discordId, String username, String avatarUrl) {
        this.discordId = discordId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.createdAt = Instant.now();
    }
}
