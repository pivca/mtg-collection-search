package com.mtgcs.collection;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CollectionMapper {

    @Mapping(source = "friend.id", target = "friendId")
    @Mapping(source = "friend.displayName", target = "friendDisplayName")
    CollectionResponse toResponse(Collection collection);
}
