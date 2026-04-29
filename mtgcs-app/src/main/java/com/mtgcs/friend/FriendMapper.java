package com.mtgcs.friend;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface FriendMapper {

    FriendResponse toResponse(Friend friend);

    void updateFromRequest(UpdateFriendRequest request, @MappingTarget Friend friend);
}
