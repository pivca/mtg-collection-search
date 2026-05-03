package com.mtgcs.search;

import com.mtgcs.collection.Collection;
import com.mtgcs.collection.CollectionRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final CollectionRepository collectionRepository;
    private final ScraperRegistry scraperRegistry;

    public List<CardSearchResult> search(Long userId, List<String> cardNames) {
        // Build a normalised lookup set: lower-case card names -> original name
        Map<String, String> normalised = new HashMap<>();
        for (String name : cardNames) {
            normalised.put(name.toLowerCase().trim(), name.trim());
        }

        // Accumulate matches per original card name
        Map<String, List<CardMatch>> matchesByCard = new HashMap<>();
        for (String original : normalised.values()) {
            matchesByCard.put(original, new ArrayList<>());
        }

        // Fetch all collections across all user's friends
        List<Collection> collections = collectionRepository.findAllByFriendOwnerId(userId);

        for (Collection collection : collections) {
            CollectionScraper scraper = scraperRegistry.find(collection.getSourceType())
                    .orElse(null);

            if (scraper == null) {
                log.debug("No scraper registered for source type {}, skipping collection {}",
                        collection.getSourceType(), collection.getId());
                continue;
            }

            Map<String, List<ScrapedCard>> scraped;
            try {
                scraped = scraper.scrapeCards(collection.getSourceUrl(), normalised.keySet());
            } catch (IOException e) {
                log.warn("Failed to scrape collection {} ({}): {}",
                        collection.getId(), collection.getSourceUrl(), e.getMessage());
                continue;
            }

            for (Map.Entry<String, String> entry : normalised.entrySet()) {
                String key = entry.getKey();
                String originalName = entry.getValue();
                List<ScrapedCard> found = scraped.get(key);
                if (found != null) {
                    for (ScrapedCard sc : found) {
                        matchesByCard.get(originalName).add(new CardMatch(
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
                                sc.foil()
                        ));
                    }
                }
            }
        }

        return cardNames.stream()
                .map(name -> new CardSearchResult(name.trim(), matchesByCard.getOrDefault(name.trim(), List.of())))
                .toList();
    }
}
