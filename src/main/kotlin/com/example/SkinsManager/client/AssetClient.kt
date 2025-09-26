package com.example.SkinsManager.client

import com.example.SkinsManager.client.dto.SkinportItemResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam


@FeignClient(
    name = "assetClient",
    url = "https://skinport.com",
    configuration = [com.example.SkinsManager.config.FeignConfig::class]
)
interface AssetClient {
    @GetMapping("/api/item?appid=730")
    fun getItem(
        @RequestParam url: String,
        @RequestHeader("Referer") referer: String
    ): SkinportItemResponse


}