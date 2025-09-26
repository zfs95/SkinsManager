package com.example.SkinsManager.dtos

data class PortfolioSummary(
    val totalCost: Double,
    val currentValue: Double,
    val profit: Double
)

data class PortfolioProduct(
    val productName: String,
    val totalPaid: Double,
    val currentValue: Double,
    val profit: Double
)