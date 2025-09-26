package com.example.SkinsManager.dtos

import java.time.LocalDate
import java.time.LocalDateTime

data class OwnedProductDto(
    val id: Long,
    val product: ProductDto,      // reuse your API DTO
    var purchases: List<PurchaseDto>
)

data class PurchaseDto(
    var id: Long = 0,
    var date: LocalDate = LocalDate.now(),
    var amount: Int = 0,
    var unitPrice: Double = 0.0
)