package com.mtgcs.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByDiscordId(String discordId);
    List<AppUser> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
}
