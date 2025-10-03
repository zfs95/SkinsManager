package com.example.SkinsManager.config

import com.example.SkinsManager.service.ProductService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UpdateScheduler(
    private val productService: ProductService
) {

    @Scheduled(cron = "0 0 9 * * ?", zone = "Europe/Berlin")   // 09:00
    @Scheduled(cron = "0 0 15 * * ?", zone = "Europe/Berlin")  // 15:00
    @Scheduled(cron = "0 0 23 * * ?", zone = "Europe/Berlin")  // 23:00
    @Scheduled(cron = "0 0 3 * * ?", zone = "Europe/Berlin")   // 03:00
    @Scheduled(cron = "0 30 5 * * ?", zone = "Europe/Berlin")  // 05:30
    @Scheduled(cron = "0 30 7 * * ?", zone = "Europe/Berlin")  // 07:30
    fun runUpdate() {
        productService.updateAllProducts()
    }
}