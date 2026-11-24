package com.mtgcs.search;

import com.mtgcs.collection.Collection;
import com.mtgcs.collection.CollectionRepository;
import com.mtgcs.history.ActionType;
import com.mtgcs.history.HistoryService;
import com.mtgcs.scraper.CollectionScraper;
import com.mtgcs.scraper.ScrapedCard;
import com.mtgcs.scraper.ScraperRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final CollectionRepository collectionRepository;
    private final ScraperRegistry scraperRegistry;
    private final HistoryService historyService;

    public SearchResponse search(Long userId, List<String> cardNames) {
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

        List<FriendBuylist> buylist = createBuylist(results);
        String formattedBuylist = formatBuylist(buylist);
        SearchResponse response = new SearchResponse(results, buylist, formattedBuylist);
        String summary = "Searched: " + String.join(", ", cardNames.stream().map(String::trim).toList());
        historyService.record(userId, ActionType.CARD_SEARCH, summary, response);

        return response;
    }

    /** Greedily assigns each found card to the friend who covers the most remaining cards. */
    static List<FriendBuylist> createBuylist(List<CardSearchResult> results) {
        Map<String, CardSearchResult> uniqueResults = new LinkedHashMap<>();
        for (CardSearchResult result : results) {
            if (result.matches().isEmpty()) {
                continue;
            }
            String key = result.cardName().trim().toLowerCase(Locale.ROOT);
            CardSearchResult existing = uniqueResults.get(key);
            uniqueResults.put(key, existing == null
                    ? result
                    : new CardSearchResult(
                            existing.cardName(),
                            Stream.concat(existing.matches().stream(), result.matches().stream()).toList()));
        }
        Set<String> remaining = new LinkedHashSet<>(uniqueResults.keySet());
        List<FriendKey> friends = uniqueResults.values().stream()
                .flatMap(result -> result.matches().stream())
                .map(m -> new FriendKey(m.friendId(), m.friendName()))
                .distinct()
                .sorted(friendTieBreaker())
                .toList();
        List<FriendBuylist> output = new ArrayList<>();
        while (!remaining.isEmpty()) {
            FriendKey selected = friends.stream().sorted(Comparator
                    .comparingInt((FriendKey friend) -> countCovered(friend, uniqueResults, remaining))
                    .reversed()
                    .thenComparing(friendTieBreaker()))
                    .findFirst()
                    .orElse(null);
            if (selected == null || countCovered(selected, uniqueResults, remaining) == 0) {
                break;
            }
            List<String> cards = new ArrayList<>();
            for (String key : new ArrayList<>(remaining)) {
                if (uniqueResults.get(key).matches().stream().anyMatch(m -> sameFriend(selected, m))) {
                    cards.add(uniqueResults.get(key).cardName().trim());
                    remaining.remove(key);
                }
            }
            output.add(new FriendBuylist(selected.id(), selected.name(), cards));
        }
        return output;
    }

    private static Comparator<FriendKey> friendTieBreaker() {
        return Comparator.comparing(FriendKey::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(FriendKey::id);
    }

    private static int countCovered(
            FriendKey friend,
            Map<String, CardSearchResult> results,
            Set<String> remaining) {
        return (int) remaining.stream()
                .filter(index -> results.get(index).matches().stream().anyMatch(match -> sameFriend(friend, match)))
                .count();
    }

    private static boolean sameFriend(FriendKey friend, CardMatch match) {
        return Objects.equals(friend.id(), match.friendId());
    }

    static String formatBuylist(List<FriendBuylist> buylist) {
        return buylist.stream()
                .map(friend -> friend.friendName() + "\n" + friend.cards().stream()
                        .map(card -> "- " + card)
                        .collect(Collectors.joining("\n")))
                .collect(Collectors.joining("\n\n"));
    }

    private record FriendKey(Long id, String name) {}

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
