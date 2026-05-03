package com.mtgcs.scraper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.mtgcs.collection.SourceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DeckboxScraper implements CollectionScraper {

    private static final Pattern SET_ID_PATTERN = Pattern.compile("deckbox\\.org/sets/([^/?#]+)");

    @Override
    public SourceType sourceType() {
        return SourceType.DECKBOX;
    }

    /**
     * Uses a headless Chromium browser to:
     * 1. Navigate to the Deckbox collection page
     * 2. For each card name: type it into #filter_values_1, click "Apply Filters"
     * 3. Wait for the JS filter to update the table (#set_cards_table_details)
     * 4. Read rows (tr[id] with numeric id) and sum quantities from td.card_count
     */
    @Override
    public Map<String, List<ScrapedCard>> scrapeCards(String collectionUrl, Set<String> cardNames) throws IOException {
        Map<String, List<ScrapedCard>> results = new HashMap<>();

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions().setHeadless(true));
             Page page = browser.newPage()) {

            log.debug("Navigating to Deckbox collection: {}", collectionUrl);
            page.navigate(collectionUrl);
            page.waitForSelector("#set_cards_table_details");

            for (String cardName : cardNames) {
                log.debug("Filtering Deckbox for: {}", cardName);

                page.locator("#filter_values_1").fill(cardName);
                page.locator("button.btn-primary")
                        .filter(new Locator.FilterOptions().setHasText("Apply Filters"))
                        .click();
                page.waitForLoadState(LoadState.NETWORKIDLE);

                List<ElementHandle> rows = page.querySelectorAll(
                        "#set_cards_table_details tr[id]");

                List<ScrapedCard> cards = new ArrayList<>();

                for (ElementHandle row : rows) {
                    ElementHandle nameLink = row.querySelector("a.simple");
                    if (nameLink == null) continue;

                    if (!nameLink.textContent().trim().equalsIgnoreCase(cardName)) continue;

                    // Quantity
                    ElementHandle countTd = row.querySelector("td.card_count");
                    if (countTd == null) continue;
                    int qty;
                    try {
                        qty = Integer.parseInt(countTd.textContent().trim());
                    } catch (NumberFormatException ignored) {
                        qty = 1;
                    }
                    if (qty <= 0) continue;

                    // Card page URL — keep full href (printing + lang params identify this specific row)
                    String href = nameLink.getAttribute("href");
                    String rowCardPageUrl = (href != null && !href.isBlank()) ? href : null;

                    // Price for this row
                    String rowPrice = null;
                    ElementHandle priceTd = row.querySelector("td.center.minimum_width a");
                    if (priceTd != null) {
                        rowPrice = priceTd.textContent().trim();
                    }

                    // Details cell: edition, language, foil, setCode
                    ElementHandle detailsTd = row.querySelector("td.minimum_width:not(.center)");
                    String edition = null;
                    String language = null;
                    boolean foil = false;
                    String setCode = null;
                    if (detailsTd != null) {
                        ElementHandle editionSvg = detailsTd.querySelector("div.esym_svg svg[data-title]");
                        if (editionSvg != null) {
                            edition = editionSvg.getAttribute("data-title");
                            // Class is like "esym_woe  R" — extract the esym_xxx token
                            String svgClass = editionSvg.getAttribute("class");
                            if (svgClass != null) {
                                for (String cls : svgClass.split("\\s+")) {
                                    if (cls.startsWith("esym_") && cls.length() > 5) {
                                        setCode = cls.substring(5);
                                        break;
                                    }
                                }
                            }
                        }
                        ElementHandle langImg = detailsTd.querySelector("img.flag");
                        if (langImg != null) {
                            language = langImg.getAttribute("data-title");
                        }
                        foil = detailsTd.querySelector("img[data-title='Foil']") != null;
                    }

                    cards.add(new ScrapedCard(qty, rowPrice, rowCardPageUrl, edition, language, foil, setCode));
                }

                if (!cards.isEmpty()) {
                    results.put(cardName.toLowerCase(), cards);
                }
            }

        } catch (PlaywrightException e) {
            throw new IOException("Playwright error scraping " + collectionUrl + ": " + e.getMessage(), e);
        }

        return results;
    }

    private String extractSetId(String url) {
        Matcher matcher = SET_ID_PATTERN.matcher(url);
        if (matcher.find()) return matcher.group(1);
        throw new IllegalArgumentException("Cannot extract set ID from Deckbox URL: " + url);
    }
}


