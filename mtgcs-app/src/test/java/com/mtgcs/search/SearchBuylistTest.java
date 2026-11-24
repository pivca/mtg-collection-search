package com.mtgcs.search;

import com.mtgcs.collection.SourceType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SearchBuylistTest {
    @Test
    void greedyAssignmentPrefersFriendCoveringMostCardsAndExcludesUnmatched() {
        List<CardSearchResult> results = List.of(
                result("Bolt", match(1L, "Alice", 1)),
                result("Counterspell", match(1L, "Alice", 2), match(2L, "Bob", 2)),
                result("Wrath", match(1L, "Alice", 3), match(2L, "Bob", 3)),
                result("Sol Ring", match(2L, "Bob", 4)),
                result("Black Lotus"));

        List<FriendBuylist> buylist = SearchService.createBuylist(results);

        assertThat(buylist).extracting(FriendBuylist::friendName).containsExactly("Alice", "Bob");
        assertThat(buylist.getFirst().cards()).containsExactly("Bolt", "Counterspell", "Wrath");
        assertThat(buylist.get(1).cards()).containsExactly("Sol Ring");
    }

    @Test
    void tieIsDeterministicAndFormattingUsesEachCardNameOnce() {
        CardMatch first = match(1L, "Alice", 1);
        CardMatch second = match(1L, "Alice", 2);
        List<FriendBuylist> buylist = SearchService.createBuylist(List.of(
                result("Bolt", first, second), result("Wrath", match(2L, "Bob", 1))));

        assertThat(buylist.getFirst().friendName()).isEqualTo("Alice");
        assertThat(buylist.getFirst().cards()).containsExactly("Bolt");
        assertThat(SearchService.formatBuylist(buylist)).isEqualTo("Alice\n- Bolt\n\nBob\n- Wrath");
    }

    @Test
    void duplicateCardNamesCountOnceWhenPrioritisingFriends() {
        List<CardSearchResult> results = List.of(
                result(" Bolt ", match(1L, "Alice", 1)),
                result("bolt", match(1L, "Alice", 2)),
                result("Wrath", match(2L, "Bob", 3)),
                result("Counterspell", match(2L, "Bob", 4)));

        List<FriendBuylist> buylist = SearchService.createBuylist(results);

        assertThat(buylist).extracting(FriendBuylist::friendName).containsExactly("Bob", "Alice");
        assertThat(buylist.getFirst().cards()).containsExactly("Wrath", "Counterspell");
        assertThat(buylist.get(1).cards()).containsExactly("Bolt");
        assertThat(SearchService.formatBuylist(buylist))
                .isEqualTo("Bob\n- Wrath\n- Counterspell\n\nAlice\n- Bolt");
    }

    private static CardSearchResult result(String name, CardMatch... matches) {
        return new CardSearchResult(name, List.of(matches));
    }

    private static CardMatch match(Long friendId, String friendName, int collectionId) {
        return new CardMatch(friendId, friendName, (long) collectionId, "url", SourceType.DECKBOX,
                1, "2.00", null, "Alpha", "English", true, null);
    }
}
