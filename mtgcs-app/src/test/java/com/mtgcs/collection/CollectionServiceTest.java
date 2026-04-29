package com.mtgcs.collection;

import com.mtgcs.friend.Friend;
import com.mtgcs.friend.FriendNotFoundException;
import com.mtgcs.friend.FriendRepository;
import com.mtgcs.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private FriendRepository friendRepository;

    @InjectMocks
    private CollectionService collectionService;

    private AppUser owner;
    private Friend friend;

    @BeforeEach
    void setUp() throws Exception {
        owner = new AppUser("123456", "testuser", null);
        setId(owner, 1L);
        friend = new Friend(owner, "Alice", null);
        setId(friend, 10L);
    }

    @Test
    void listCollections_returnsCollectionsForFriend() {
        when(friendRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(friend));
        Collection col = new Collection(friend, SourceType.DECKBOX, "https://deckbox.org/sets/123");
        when(collectionRepository.findAllByFriendId(10L)).thenReturn(List.of(col));

        List<Collection> result = collectionService.listCollections(1L, 10L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getSourceUrl()).isEqualTo("https://deckbox.org/sets/123");
    }

    @Test
    void listCollections_throwsWhenFriendNotFound() {
        when(friendRepository.findByIdAndOwnerId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionService.listCollections(1L, 99L))
                .isInstanceOf(FriendNotFoundException.class);
    }

    @Test
    void addCollection_savesValidDeckboxCollection() {
        when(friendRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(friend));
        when(collectionRepository.save(any(Collection.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateCollectionRequest request = new CreateCollectionRequest(
                SourceType.DECKBOX, "https://deckbox.org/sets/123");

        Collection result = collectionService.addCollection(1L, 10L, request);

        assertThat(result.getSourceType()).isEqualTo(SourceType.DECKBOX);
        assertThat(result.getSourceUrl()).isEqualTo("https://deckbox.org/sets/123");
        verify(collectionRepository).save(any(Collection.class));
    }

    @Test
    void addCollection_savesValidMoxfieldCollection() {
        when(friendRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(friend));
        when(collectionRepository.save(any(Collection.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateCollectionRequest request = new CreateCollectionRequest(
                SourceType.MOXFIELD, "https://www.moxfield.com/users/someone/collection");

        Collection result = collectionService.addCollection(1L, 10L, request);

        assertThat(result.getSourceType()).isEqualTo(SourceType.MOXFIELD);
    }

    @Test
    void addCollection_throwsWhenUrlDoesNotMatchSourceType() {
        when(friendRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(friend));

        CreateCollectionRequest request = new CreateCollectionRequest(
                SourceType.DECKBOX, "https://www.moxfield.com/users/someone/collection");

        assertThatThrownBy(() -> collectionService.addCollection(1L, 10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match source type");
    }

    @Test
    void addCollection_throwsWhenFriendNotFound() {
        when(friendRepository.findByIdAndOwnerId(99L, 1L)).thenReturn(Optional.empty());

        CreateCollectionRequest request = new CreateCollectionRequest(
                SourceType.DECKBOX, "https://deckbox.org/sets/123");

        assertThatThrownBy(() -> collectionService.addCollection(1L, 99L, request))
                .isInstanceOf(FriendNotFoundException.class);
    }

    @Test
    void deleteCollection_deletesOwnedCollection() {
        Collection col = new Collection(friend, SourceType.DECKBOX, "https://deckbox.org/sets/123");
        when(collectionRepository.findById(5L)).thenReturn(Optional.of(col));

        collectionService.deleteCollection(1L, 5L);

        verify(collectionRepository).delete(col);
    }

    @Test
    void deleteCollection_throwsWhenNotFound() {
        when(collectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionService.deleteCollection(1L, 99L))
                .isInstanceOf(CollectionNotFoundException.class);
    }

    @Test
    void deleteCollection_throwsWhenNotOwned() throws Exception {
        AppUser otherOwner = new AppUser("999", "other", null);
        setId(otherOwner, 2L);
        Friend otherFriend = new Friend(otherOwner, "Other", null);
        Collection col = new Collection(otherFriend, SourceType.DECKBOX, "https://deckbox.org/sets/123");
        when(collectionRepository.findById(5L)).thenReturn(Optional.of(col));

        assertThatThrownBy(() -> collectionService.deleteCollection(1L, 5L))
                .isInstanceOf(CollectionNotFoundException.class);
    }

    private void setId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
}
