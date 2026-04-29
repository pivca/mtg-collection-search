package com.mtgcs.collection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCollectionRequest(
        @NotNull SourceType sourceType,

        @NotBlank
        @Size(max = 1024)
        @Pattern(
                regexp = "^https://(www\\.)?(deckbox\\.org|moxfield\\.com)/.+$",
                message = "URL must be a valid Deckbox or Moxfield link"
        )
        String sourceUrl
) {}
