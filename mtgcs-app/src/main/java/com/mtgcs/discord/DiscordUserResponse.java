package com.mtgcs.discord;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordUserResponse(
        String id,
        String username,
        @JsonProperty("global_name") String globalName,
        String avatar
) {
    public String avatarUrl() {
        if (avatar == null) return null;
        return "https://cdn.discordapp.com/avatars/" + id + "/" + avatar + ".png?size=128";
    }

    public String displayName() {
        return globalName != null ? globalName : username;
    }
}
