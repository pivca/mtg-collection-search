package com.mtgcs.collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    @Query("SELECT c FROM Collection c JOIN FETCH c.friend WHERE c.friend.id = :friendId")
    List<Collection> findAllByFriendId(@Param("friendId") Long friendId);

    @Query("SELECT c FROM Collection c JOIN FETCH c.friend WHERE c.friend.owner.id = :ownerId")
    List<Collection> findAllByFriendOwnerId(@Param("ownerId") Long ownerId);
}
