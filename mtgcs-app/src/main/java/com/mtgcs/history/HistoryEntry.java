package com.mtgcs.history;

import com.mtgcs.user.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "history")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class HistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    /** Short human-readable summary (e.g. "Searched: Lightning Bolt, Counterspell") */
    @Column(name = "summary", nullable = false, length = 512)
    private String summary;

    /** Optional JSON payload for richer detail (request + response snapshot) */
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public HistoryEntry(AppUser user, ActionType actionType, String summary, String payload) {
        this.user = user;
        this.actionType = actionType;
        this.summary = summary;
        this.payload = payload;
        this.createdAt = Instant.now();
    }
}
