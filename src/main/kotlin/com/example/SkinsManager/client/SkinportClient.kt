package com.example.SkinsManager.client

import com.example.SkinsManager.dtos.ProductDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import com.example.SkinsManager.config.FeignBrotliConfig
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "skinportClient", url = "\${services.skinport}", configuration = [FeignBrotliConfig::class])
interface SkinportClient {

    @GetMapping("/items",headers = ["Accept-Encoding: br"])
     fun getItems(
        @RequestParam("app_id") appId: Int,
        @RequestParam("currency") currency: String,
    ): Array<ProductDto>
}