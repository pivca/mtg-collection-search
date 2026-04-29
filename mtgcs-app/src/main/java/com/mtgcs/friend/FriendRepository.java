package com.mtgcs.friend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findAllByOwnerId(Long ownerId);
    Optional<Friend> findByIdAndOwnerId(Long id, Long ownerId);
}
