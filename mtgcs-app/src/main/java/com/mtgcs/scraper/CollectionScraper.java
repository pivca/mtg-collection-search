package com.mtgcs.scraper;

import com.mtgcs.collection.SourceType;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CollectionScraper {

    SourceType sourceType();

    /**
     * Searches for specific cards in a collection.
     * Returns a map of lower-cased card name -> enriched ScrapedCard (qty, price, imageUrl).
     */
    Map<String, List<ScrapedCard>> scrapeCards(String collectionUrl, Set<String> cardNames) throws IOException;
}
