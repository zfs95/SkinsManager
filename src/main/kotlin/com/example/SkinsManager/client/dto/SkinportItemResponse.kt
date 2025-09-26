package com.example.SkinsManager.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SkinportItemResponse(
    val data: ItemData?
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ItemData(
        val item: ItemDetails?
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class ItemDetails(
            val image: String?
        )
    }
}
