package ru.ifmo.ctd.mekhanikov.crawler.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class UsersResponse {

    @Getter
    @Setter
    @JsonProperty("min_position")
    private long minPosition;

    @Getter
    @Setter
    @JsonProperty("has_more_items")
    private boolean hasMoreItems;

    @Getter
    @Setter
    @JsonProperty("items_html")
    private String itemsHtml;

    @Getter
    @Setter
    @JsonProperty("new_latent_count")
    private int newLatentCount;
}
