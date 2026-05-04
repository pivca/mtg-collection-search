package com.mtgcs.collection;

import com.mtgcs.friend.Friend;
import com.mtgcs.friend.FriendNotFoundException;
import com.mtgcs.friend.FriendRepository;
import com.mtgcs.history.ActionType;
import com.mtgcs.history.HistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final FriendRepository friendRepository;
    private final HistoryService historyService;

    public CollectionService(CollectionRepository collectionRepository, FriendRepository friendRepository, HistoryService historyService) {
        this.collectionRepository = collectionRepository;
        this.friendRepository = friendRepository;
        this.historyService = historyService;
    }

    public List<Collection> listCollections(Long userId, Long friendId) {
        Friend friend = friendRepository.findByIdAndOwnerId(friendId, userId)
                .orElseThrow(() -> new FriendNotFoundException(friendId));
        return collectionRepository.findAllByFriendId(friend.getId());
    }

    @Transactional
    public Collection addCollection(Long userId, Long friendId, CreateCollectionRequest request) {
        Friend friend = friendRepository.findByIdAndOwnerId(friendId, userId)
                .orElseThrow(() -> new FriendNotFoundException(friendId));

        validateUrlMatchesSourceType(request.sourceType(), request.sourceUrl());

        Collection collection = new Collection(friend, request.sourceType(), request.sourceUrl());
        Collection saved = collectionRepository.save(collection);
        historyService.record(userId, ActionType.COLLECTION_ADDED,
                "Added collection for " + friend.getDisplayName() + " (" + request.sourceType() + ")", null);
        return saved;
    }

    @Transactional
    public void deleteCollection(Long userId, Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectionNotFoundException(collectionId));

        if (!collection.getFriend().getOwner().getId().equals(userId)) {
            throw new CollectionNotFoundException(collectionId);
        }

        String label = collection.getFriend().getDisplayName() + " (" + collection.getSourceType() + ")";
        collectionRepository.delete(collection);
        historyService.record(userId, ActionType.COLLECTION_DELETED, "Removed collection for " + label, null);
    }

    private void validateUrlMatchesSourceType(SourceType sourceType, String url) {
        boolean valid = switch (sourceType) {
            case DECKBOX -> url.contains("deckbox.org");
            case MOXFIELD -> url.contains("moxfield.com");
        };
        if (!valid) {
            throw new IllegalArgumentException("URL does not match source type " + sourceType);
        }
    }
}
