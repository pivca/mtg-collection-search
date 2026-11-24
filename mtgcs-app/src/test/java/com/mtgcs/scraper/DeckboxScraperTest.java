package com.mtgcs.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeckboxScraperTest {

    @Test
    void parsesMatchingRowsWithPrintingDetails() throws IOException {
        Document document = fixture();

        List<ScrapedCard> cards = DeckboxScraper.parseCards(document, "sol ring");

        assertThat(cards).hasSize(2);
        assertThat(cards.getFirst()).isEqualTo(new ScrapedCard(
                8,
                "$7.43",
                "https://deckbox.org/mtg/Sol%20Ring?printing=98928&lang=en",
                "Media Inserts (Card #218)",
                "English",
                true,
                "star"));
        assertThat(cards.get(1).price()).isNull();
        assertThat(cards.get(1).cardPageUrl()).isEqualTo("https://deckbox.org/mtg/Sol%20Ring?printing=66303&lang=en");
    }

    @Test
    void ignoresRowsWithDifferentCardName() throws IOException {
        assertThat(DeckboxScraper.parseCards(fixture(), "Sol Talisman")).hasSize(1);
        assertThat(DeckboxScraper.parseCards(fixture(), "Black Lotus")).isEmpty();
    }

    @Test
    void readsPageCountFromPaginationControls() throws IOException {
        assertThat(DeckboxScraper.pageCount(fixture())).isEqualTo(2);
        assertThat(DeckboxScraper.pageCount(Jsoup.parse("<html><body></body></html>"))).isEqualTo(1);
    }

    private static Document fixture() throws IOException {
        try (InputStream in = DeckboxScraperTest.class.getResourceAsStream("/deckbox/set-filtered-sol-ring.html")) {
            return Jsoup.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8), "https://deckbox.org");
        }
    }
}
