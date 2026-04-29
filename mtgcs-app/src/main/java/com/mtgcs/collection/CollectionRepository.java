package com.mtgcs.collection;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findAllByFriendId(Long friendId);
    List<Collection> findAllByFriendOwnerId(Long ownerId);
}
