package com.mtgcs.collection;

public class CollectionNotFoundException extends RuntimeException {
    public CollectionNotFoundException(Long id) {
        super("Collection not found: " + id);
    }
}
