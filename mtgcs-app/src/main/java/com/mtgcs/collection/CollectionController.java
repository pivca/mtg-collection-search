package com.mtgcs.collection;

import com.mtgcs.security.DiscordOAuth2User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;
    private final CollectionMapper collectionMapper;

    @GetMapping("/friends/{friendId}/collections")
    public List<CollectionResponse> list(
            @AuthenticationPrincipal DiscordOAuth2User principal,
            @PathVariable Long friendId) {
        return collectionService.listCollections(principal.getUserId(), friendId).stream()
                .map(collectionMapper::toResponse)
                .toList();
    }

    @PostMapping("/friends/{friendId}/collections")
    public ResponseEntity<CollectionResponse> add(
            @AuthenticationPrincipal DiscordOAuth2User principal,
            @PathVariable Long friendId,
            @Valid @RequestBody CreateCollectionRequest request) {
        Collection collection = collectionService.addCollection(principal.getUserId(), friendId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(collectionMapper.toResponse(collection));
    }

    @DeleteMapping("/collections/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal DiscordOAuth2User principal,
            @PathVariable Long id) {
        collectionService.deleteCollection(principal.getUserId(), id);
    }
}
