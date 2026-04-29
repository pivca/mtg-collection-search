package com.mtgcs.friend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFriendRequest(
        @NotBlank @Size(max = 128) String displayName,
        @Size(max = 64) String discordId
) {}
