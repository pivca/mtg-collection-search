package com.mtgcs.friend;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FriendMapper {

    FriendResponse toResponse(Friend friend);

    void updateFromRequest(UpdateFriendRequest request, @MappingTarget Friend friend);
}
