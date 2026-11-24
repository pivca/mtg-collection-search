package com.mtgcs.scraper;

import com.mtgcs.collection.SourceType;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DeckboxScraper implements CollectionScraper {

    private static final String BASE_URL = "https://deckbox.org";
    private static final Pattern SET_ID_PATTERN = Pattern.compile("deckbox\\.org/sets/([^/?#]+)");
    private static final Pattern PAGE_COUNT_PATTERN = Pattern.compile("Page \\d+ of (\\d+)");
    /** Deckbox filter code: field "1" (Name) + operator "4" (Equals), followed by the Base64 value. */
    private static final String NAME_EQUALS_FILTER = "14";
    /** Caps concurrent requests to Deckbox across all searches. */
    private static final int MAX_CONCURRENT_REQUESTS = 8;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; mtgcs/1.0)";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    private final Semaphore requestPermits = new Semaphore(MAX_CONCURRENT_REQUESTS);

    @Override
    public SourceType sourceType() {
        return SourceType.DECKBOX;
    }

    /**
     * Fetches the server-rendered Deckbox collection page once per card name, using Deckbox's
     * URL filter ({@code ?f=14<base64 name>}) so only matching rows are returned.
     * Card lookups run in parallel on virtual threads; extra result pages are followed.
     */
    @Override
    public Map<String, List<ScrapedCard>> scrapeCards(String collectionUrl, Set<String> cardNames) throws IOException {
        String setId;
        try {
            setId = extractSetId(collectionUrl);
        } catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage(), e);
        }

        Map<String, List<ScrapedCard>> results = new HashMap<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Map<String, Future<List<ScrapedCard>>> futures = new LinkedHashMap<>();
            for (String cardName : cardNames) {
                futures.put(cardName, executor.submit(() -> scrapeCard(setId, cardName)));
            }
            try {
                for (Map.Entry<String, Future<List<ScrapedCard>>> entry : futures.entrySet()) {
                    List<ScrapedCard> cards = entry.getValue().get();
                    if (!cards.isEmpty()) {
                        results.put(entry.getKey().toLowerCase(), cards);
                    }
                }
            } catch (ExecutionException e) {
                executor.shutdownNow();
                Throwable cause = e.getCause();
                throw cause instanceof IOException io ? io
                        : new IOException("Error scraping " + collectionUrl + ": " + cause.getMessage(), cause);
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                throw new InterruptedIOException("Interrupted scraping " + collectionUrl);
            }
        }
        return results;
    }

    private List<ScrapedCard> scrapeCard(String setId, String cardName) throws IOException, InterruptedException {
        log.debug("Filtering Deckbox set {} for: {}", setId, cardName);
        Document firstPage = fetchPage(setId, cardName, 1);
        List<ScrapedCard> cards = new ArrayList<>(parseCards(firstPage, cardName));
        int pageCount = pageCount(firstPage);
        for (int page = 2; page <= pageCount; page++) {
            cards.addAll(parseCards(fetchPage(setId, cardName, page), cardName));
        }
        return cards;
    }

    private Document fetchPage(String setId, String cardName, int page) throws IOException, InterruptedException {
        String filter = NAME_EQUALS_FILTER
                + Base64.getEncoder().encodeToString(cardName.getBytes(StandardCharsets.UTF_8));
        URI uri = URI.create(BASE_URL + "/sets/" + URLEncoder.encode(setId, StandardCharsets.UTF_8)
                + "?f=" + URLEncoder.encode(filter, StandardCharsets.UTF_8)
                + "&p=" + page);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(REQUEST_TIMEOUT)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html")
                .GET()
                .build();

        HttpResponse<String> response;
        requestPermits.acquire();
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } finally {
            requestPermits.release();
        }

        if (response.statusCode() != 200) {
            // Deckbox redirects private or missing sets to the login/home page
            throw new IOException("Deckbox returned HTTP " + response.statusCode() + " for set " + setId);
        }
        Document document = Jsoup.parse(response.body(), BASE_URL);
        if (document.getElementById("set_cards_table_details") == null) {
            throw new IOException("Unexpected Deckbox page for set " + setId + ": card table not found");
        }
        return document;
    }

    /** Reads rows (tr[id]) of #set_cards_table_details whose card name equals {@code cardName}. */
    static List<ScrapedCard> parseCards(Document document, String cardName) {
        List<ScrapedCard> cards = new ArrayList<>();

        for (Element row : document.select("#set_cards_table_details tr[id]")) {
            Element nameLink = row.selectFirst("a.simple");
            if (nameLink == null) continue;

            if (!nameLink.text().trim().equalsIgnoreCase(cardName)) continue;

            // Quantity
            Element countTd = row.selectFirst("td.card_count");
            if (countTd == null) continue;
            int qty;
            try {
                qty = Integer.parseInt(countTd.text().trim());
            } catch (NumberFormatException ignored) {
                qty = 1;
            }
            if (qty <= 0) continue;

            // Card page URL — keep full href (printing + lang params identify this specific row)
            String href = nameLink.attr("href");
            String rowCardPageUrl = !href.isBlank() ? href : null;

            // Price for this row
            String rowPrice = null;
            Element priceLink = row.selectFirst("td.center.minimum_width a");
            if (priceLink != null) {
                rowPrice = priceLink.text().trim();
            }

            // Details cell: edition, language, foil, setCode
            Element detailsTd = row.selectFirst("td.minimum_width:not(.center)");
            String edition = null;
            String language = null;
            boolean foil = false;
            String setCode = null;
            if (detailsTd != null) {
                Element editionSvg = detailsTd.selectFirst("div.esym_svg svg[data-title]");
                if (editionSvg != null) {
                    edition = editionSvg.attr("data-title");
                    // Class is like "esym_woe  R" — extract the esym_xxx token
                    for (String cls : editionSvg.classNames()) {
                        if (cls.startsWith("esym_") && cls.length() > 5) {
                            setCode = cls.substring(5);
                            break;
                        }
                    }
                }
                Element langImg = detailsTd.selectFirst("img.flag");
                if (langImg != null) {
                    language = langImg.attr("data-title");
                }
                foil = detailsTd.selectFirst("img[data-title=Foil]") != null;
            }

            cards.add(new ScrapedCard(qty, rowPrice, rowCardPageUrl, edition, language, foil, setCode));
        }
        return cards;
    }

    /** Parses "Page X of N" from the pagination controls; defaults to 1. */
    static int pageCount(Document document) {
        Element controls = document.selectFirst(".pagination_controls");
        if (controls == null) return 1;
        Matcher matcher = PAGE_COUNT_PATTERN.matcher(controls.text());
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 1;
    }

    private String extractSetId(String url) {
        Matcher matcher = SET_ID_PATTERN.matcher(url);
        if (matcher.find()) return matcher.group(1);
        throw new IllegalArgumentException("Cannot extract set ID from Deckbox URL: " + url);
    }
}
