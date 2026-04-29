package com.mtgcs.collection;

import com.mtgcs.friend.Friend;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "collections")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private Friend friend;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "source_url", nullable = false)
    private String sourceUrl;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    public Collection(Friend friend, SourceType sourceType, String sourceUrl) {
        this.friend = friend;
        this.sourceType = sourceType;
        this.sourceUrl = sourceUrl;
        this.addedAt = Instant.now();
    }
}
