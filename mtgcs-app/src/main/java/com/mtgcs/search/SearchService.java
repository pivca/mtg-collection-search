package com.mtgcs.search;

import com.mtgcs.collection.Collection;
import com.mtgcs.collection.CollectionRepository;
import com.mtgcs.history.ActionType;
import com.mtgcs.history.HistoryService;
import com.mtgcs.scraper.CollectionScraper;
import com.mtgcs.scraper.ScraperRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.mtgcs.scraper.ScrapedCard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final CollectionRepository collectionRepository;
    private final ScraperRegistry scraperRegistry;
    private final HistoryService historyService;

    public List<CardSearchResult> search(Long userId, List<String> cardNames) {
        // Build a normalised lookup: lower-case card name -> original name
        Map<String, String> normalised = new HashMap<>();
        for (String name : cardNames) {
            normalised.put(name.toLowerCase().trim(), name.trim());
        }

        List<Collection> collections = collectionRepository.findAllByFriendOwnerId(userId);

        // One task per collection — each returns Map<originalCardName, List<CardMatch>>
        List<Callable<Map<String, List<CardMatch>>>> tasks = new ArrayList<>();
        for (Collection collection : collections) {
            CollectionScraper scraper = scraperRegistry.find(collection.getSourceType()).orElse(null);
            if (scraper == null) {
                log.debug("No scraper for source type {}, skipping collection {}",
                        collection.getSourceType(), collection.getId());
                continue;
            }
            tasks.add(() -> scrapeCollection(scraper, collection, normalised));
        }

        // Initialise result map
        Map<String, List<CardMatch>> matchesByCard = new HashMap<>();
        for (String original : normalised.values()) {
            matchesByCard.put(original, new ArrayList<>());
        }

        // Run all scrapes in parallel using virtual threads, then merge
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Map<String, List<CardMatch>>>> futures = executor.invokeAll(tasks);
            for (Future<Map<String, List<CardMatch>>> future : futures) {
                try {
                    future.get().forEach((cardName, matches) ->
                            matchesByCard.computeIfAbsent(cardName, k -> new ArrayList<>()).addAll(matches));
                } catch (ExecutionException e) {
                    log.warn("Collection scrape task failed: {}", e.getCause().getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Search interrupted");
        }

        List<CardSearchResult> results = cardNames.stream()
                .map(name -> new CardSearchResult(name.trim(), matchesByCard.getOrDefault(name.trim(), List.of())))
                .toList();

        String summary = "Searched: " + String.join(", ", cardNames.stream().map(String::trim).toList());
        historyService.record(userId, ActionType.CARD_SEARCH, summary, results);

        return results;
    }

    /** Scrapes a single collection and returns matches grouped by original card name. */
    private Map<String, List<CardMatch>> scrapeCollection(CollectionScraper scraper,
                                                           Collection collection,
                                                           Map<String, String> normalised) {
        Map<String, List<ScrapedCard>> scraped;
        try {
            scraped = scraper.scrapeCards(collection.getSourceUrl(), normalised.keySet());
        } catch (IOException e) {
            log.warn("Failed to scrape collection {} ({}): {}",
                    collection.getId(), collection.getSourceUrl(), e.getMessage());
            return Map.of();
        }

        Map<String, List<CardMatch>> result = new HashMap<>();
        for (Map.Entry<String, String> entry : normalised.entrySet()) {
            List<ScrapedCard> found = scraped.get(entry.getKey());
            if (found == null) continue;
            String originalName = entry.getValue();
            List<CardMatch> matches = result.computeIfAbsent(originalName, k -> new ArrayList<>());
            for (ScrapedCard sc : found) {
                matches.add(new CardMatch(
                        collection.getFriend().getId(),
                        collection.getFriend().getDisplayName(),
                        collection.getId(),
                        collection.getSourceUrl(),
                        collection.getSourceType(),
                        sc.quantity(),
                        sc.price(),
                        sc.cardPageUrl(),
                        sc.edition(),
                        sc.language(),
                        sc.foil(),
                        sc.setCode()
                ));
            }
        }
        return result;
    }
}
