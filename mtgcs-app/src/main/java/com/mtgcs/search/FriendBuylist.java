package com.mtgcs.search;

import java.util.List;

/** Cards assigned to a single friend by the search buylist planner. */
public record FriendBuylist(Long friendId, String friendName, List<String> cards) {
    public FriendBuylist {
        cards = List.copyOf(cards);
    }
}
