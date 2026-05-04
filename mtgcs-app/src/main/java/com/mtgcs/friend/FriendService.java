package com.mtgcs.friend;

import com.mtgcs.history.ActionType;
import com.mtgcs.history.HistoryService;
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
    private final HistoryService historyService;

    public List<Friend> listFriends(Long userId) {
        return friendRepository.findAllByOwnerId(userId);
    }

    @Transactional
    public Friend createFriend(Long userId, CreateFriendRequest request) {
        AppUser owner = userRepository.getReferenceById(userId);
        Friend friend = new Friend(owner, request.displayName(), request.discordId());
        Friend saved = friendRepository.save(friend);
        historyService.record(userId, ActionType.FRIEND_CREATED, "Added friend: " + saved.getDisplayName(), null);
        return saved;
    }

    @Transactional
    public Friend updateFriend(Long userId, Long friendId, UpdateFriendRequest request) {
        Friend friend = friendRepository.findByIdAndOwnerId(friendId, userId)
                .orElseThrow(() -> new FriendNotFoundException(friendId));
        friendMapper.updateFromRequest(request, friend);
        Friend saved = friendRepository.save(friend);
        historyService.record(userId, ActionType.FRIEND_UPDATED, "Updated friend: " + saved.getDisplayName(), null);
        return saved;
    }

    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        Friend friend = friendRepository.findByIdAndOwnerId(friendId, userId)
                .orElseThrow(() -> new FriendNotFoundException(friendId));
        String name = friend.getDisplayName();
        friendRepository.delete(friend);
        historyService.record(userId, ActionType.FRIEND_DELETED, "Removed friend: " + name, null);
    }
}
