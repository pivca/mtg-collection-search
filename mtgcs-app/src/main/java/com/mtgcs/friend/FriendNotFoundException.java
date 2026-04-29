package com.mtgcs.friend;

public class FriendNotFoundException extends RuntimeException {
    public FriendNotFoundException(Long id) {
        super("Friend not found: " + id);
    }
}
