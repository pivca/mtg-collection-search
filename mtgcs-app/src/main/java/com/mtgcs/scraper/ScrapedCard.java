package com.mtgcs.scraper;

/**
 * Enriched result for a single card row in a collection.
 *
 * @param quantity    number of copies in this row
 * @param price       price for this printing, null if not available
 * @param cardPageUrl card page URL for this printing, null if not available
 * @param edition     edition name (e.g. "Wilds of Eldraine (Card #34)"), null if not available
 * @param language    language (e.g. "English"), null if not available
 * @param foil        true if this copy is foil
 * @param setCode     Scryfall/MTG set code (e.g. "woe", "dmr"), null if not available
 */
public record ScrapedCard(int quantity, String price, String cardPageUrl, String edition, String language, boolean foil, String setCode) {}
