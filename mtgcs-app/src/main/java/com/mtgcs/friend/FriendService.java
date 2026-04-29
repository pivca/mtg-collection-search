package com.mtgcs.friend;

import com.mtgcs.user.AppUser;
import com.mtgcs.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final FriendMapper friendMapper;

    public List<Friend> listFriends(Long userId) {
        return friendRepository.findAllByOwnerId(userId);
    }

    @Transactional
    public Friend createFriend(Long userId, CreateFriendRequest request) {
        AppUser owner = userRepository.getReferenceById(userId);
        Friend friend = new Friend(owner, request.displayName(), request.discordId());
        return friendRepository.save(friend);
    }

    @Transactional
    public Friend updateFriend(Long userId, Long friendId, UpdateFriendRequest request) {
        Friend friend = friendRepository.findByIdAndOwnerId(friendId, userId)
                .orElseThrow(() -> new FriendNotFoundException(friendId));
        friendMapper.updateFromRequest(request, friend);
        return friendRepository.save(friend);
    }

    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        Friend friend = friendRepository.findByIdAndOwnerId(friendId, userId)
                .orElseThrow(() -> new FriendNotFoundException(friendId));
        friendRepository.delete(friend);
    }
}
