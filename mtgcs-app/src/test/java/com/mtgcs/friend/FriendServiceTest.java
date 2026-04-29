package com.mtgcs.friend;

import com.mtgcs.user.AppUser;
import com.mtgcs.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendMapper friendMapper;

    @InjectMocks
    private FriendService friendService;

    private AppUser owner;

    @BeforeEach
    void setUp() {
        owner = new AppUser("123456", "testuser", null);
    }

    @Test
    void listFriends_returnsFriendsForUser() {
        Friend friend = new Friend(owner, "Alice", null);
        when(friendRepository.findAllByOwnerId(1L)).thenReturn(List.of(friend));

        List<Friend> result = friendService.listFriends(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDisplayName()).isEqualTo("Alice");
    }

    @Test
    void listFriends_returnsEmptyWhenNone() {
        when(friendRepository.findAllByOwnerId(1L)).thenReturn(List.of());

        List<Friend> result = friendService.listFriends(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void createFriend_savesAndReturnsFriend() {
        when(userRepository.getReferenceById(1L)).thenReturn(owner);
        when(friendRepository.save(any(Friend.class))).thenAnswer(inv -> inv.getArgument(0));

        Friend result = friendService.createFriend(1L, new CreateFriendRequest("Bob", "99999"));

        assertThat(result.getDisplayName()).isEqualTo("Bob");
        assertThat(result.getDiscordId()).isEqualTo("99999");
        verify(friendRepository).save(any(Friend.class));
    }

    @Test
    void updateFriend_updatesExistingFriend() {
        Friend friend = new Friend(owner, "Alice", null);
        when(friendRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(friend));
        when(friendRepository.save(any(Friend.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateFriendRequest request = new UpdateFriendRequest("Alice Updated", "11111");
        Friend result = friendService.updateFriend(1L, 10L, request);

        verify(friendMapper).updateFromRequest(request, friend);
        verify(friendRepository).save(friend);
    }

    @Test
    void updateFriend_throwsWhenNotFound() {
        when(friendRepository.findByIdAndOwnerId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.updateFriend(1L, 99L, new UpdateFriendRequest("x", null)))
                .isInstanceOf(FriendNotFoundException.class);
    }

    @Test
    void deleteFriend_deletesExistingFriend() {
        Friend friend = new Friend(owner, "Alice", null);
        when(friendRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(friend));

        friendService.deleteFriend(1L, 10L);

        verify(friendRepository).delete(friend);
    }

    @Test
    void deleteFriend_throwsWhenNotFound() {
        when(friendRepository.findByIdAndOwnerId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.deleteFriend(1L, 99L))
                .isInstanceOf(FriendNotFoundException.class);
    }
}
