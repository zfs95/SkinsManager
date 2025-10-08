package com.example.SkinsManager.dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProductDto(
    @JsonProperty("market_hash_name")
    val marketHashName: String,

    val currency: String,
    @JsonProperty("suggested_price")
    val suggestedPrice: Double?,
    @JsonProperty("item_page")
    val itemPage: String?,
    @JsonProperty("market_page")
    val marketPage: String?,
    @JsonProperty("min_price")
    val minPrice: Double?,
    @JsonProperty("max_price")
    val maxPrice: Double?,
    @JsonProperty("mean_price")
    val meanPrice: Double?,
    @JsonProperty("median_price")
    val medianPrice: Double?,
    val quantity: Int?,
    @JsonProperty("created_at")
    val createdAt: Long?,
    @JsonProperty("updated_at")
    val updatedAt: Long?,
    val imageUrl: String? = null
)
