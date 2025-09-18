package com.example.SkinsManager.repository

import com.example.SkinsManager.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findProductByMarketHashName(marketHashName: String): Product?

    fun findProductsByMarketHashNameContainingIgnoreCase(query: String): List<Product>
}