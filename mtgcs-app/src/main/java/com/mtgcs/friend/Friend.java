package com.mtgcs.friend;

import com.mtgcs.user.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "friends")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private AppUser owner;

    @Setter
    @Column(name = "discord_id")
    private String discordId;

    @Setter
    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Friend(AppUser owner, String displayName, String discordId) {
        this.owner = owner;
        this.displayName = displayName;
        this.discordId = discordId;
        this.createdAt = Instant.now();
    }
}
