package com.mtgcs.scraper;

import com.mtgcs.collection.SourceType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ScraperRegistry {

    private final Map<SourceType, CollectionScraper> scrapers;

    public ScraperRegistry(List<CollectionScraper> scraperList) {
        scrapers = new EnumMap<>(SourceType.class);
        for (CollectionScraper scraper : scraperList) {
            scrapers.put(scraper.sourceType(), scraper);
        }
    }

    public Optional<CollectionScraper> find(SourceType sourceType) {
        return Optional.ofNullable(scrapers.get(sourceType));
    }
}
