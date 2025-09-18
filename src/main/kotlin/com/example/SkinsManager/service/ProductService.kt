package com.example.SkinsManager.service

import com.example.SkinsManager.client.SkinportClient
import com.example.SkinsManager.dtos.ProductDto
import com.example.SkinsManager.model.OwnedProduct
import com.example.SkinsManager.model.Product
import com.example.SkinsManager.repository.OwnedProductRepository
import com.example.SkinsManager.repository.ProductRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger

@Service
class ProductService(
    private val skinportClient: SkinportClient,
    private val productRepository: ProductRepository,
    private val ownedProductRepository: OwnedProductRepository
) {

    private val logger = Logger.getLogger(ProductService::class.java.name)

    /**
     * Fetch all products from Skinport API and upsert into database
     */
    @Transactional
    fun updateAllProducts() = runBlocking {
        val productsFromApi = skinportClient.getItems(appId = 730, currency = "EUR")

//        val logChannel = Channel<String>(Channel.UNLIMITED)
//
//        // Launch background coroutine for logging
//        val logJob = launch {
//            for (msg in logChannel) {
//                logger.info(msg)
//            }
//        }

        val addedCount = AtomicInteger(0)
        val updatedCount = AtomicInteger(0)

        // Process in parallel using coroutines
        coroutineScope {
            productsFromApi.map { dto ->
                async {
                    processProduct(dto, addedCount, updatedCount)
                }
            }.awaitAll()  // Wait for all coroutines to finish
        }

        logger.info("Products added: ${addedCount.get()}")
        logger.info("Products updated: ${updatedCount.get()}")
    }

    /**
     * Upsert a single product safely
     */
    suspend fun processProduct(dto: ProductDto, addedCount: AtomicInteger, updatedCount: AtomicInteger) {
        // Validate required fields
        if (dto.marketHashName.isBlank() || dto.currency.isBlank()) return

        // Check if product exists (unique by marketHashName)
        val existing = productRepository.findProductByMarketHashName(dto.marketHashName)

        if (existing == null) {
            // Insert new product
            val newProduct = Product(
                marketHashName = dto.marketHashName,
                currency = dto.currency,
                suggestedPrice = dto.suggestedPrice,
                itemPage = dto.itemPage,
                marketPage = dto.marketPage,
                minPrice = dto.minPrice,
                maxPrice = dto.maxPrice,
                meanPrice = dto.meanPrice,
                medianPrice = dto.medianPrice,
                quantity = dto.quantity,
                createdAtApi = dto.createdAt,
                updatedAtApi = dto.updatedAt,
                isActive = true
            )
            productRepository.save(newProduct)
            addedCount.incrementAndGet()
            logger.info("Added product: ${dto.marketHashName}")
        } else {
            // Update existing product
            val updated = existing.copy(
                currency = dto.currency,
                suggestedPrice = dto.suggestedPrice,
                itemPage = dto.itemPage,
                marketPage = dto.marketPage,
                minPrice = dto.minPrice,
                maxPrice = dto.maxPrice,
                meanPrice = dto.meanPrice,
                medianPrice = dto.medianPrice,
                quantity = dto.quantity,
                createdAtApi = dto.createdAt,
                updatedAtApi = dto.updatedAt,
                isActive = true
            )
            productRepository.save(updated)
            updatedCount.incrementAndGet()
            logger.info("Updated product: ${dto.marketHashName}")
        }
    }
    //here down grey area
    fun getAllOwnedProducts(): List<OwnedProduct>{
        return ownedProductRepository.findAllWithProduct()
    }

    /**
     * Add a product to dashboard if not already added
     */
    fun addProductToDashboard(product: Product): OwnedProduct {
        val existing = ownedProductRepository.findOwnedProductByProduct(product)
        if (existing != null) return existing

        val ownedProduct = OwnedProduct(product = product)
        return ownedProductRepository.save(ownedProduct)
    }

    /**
     * Search products from catalog by name (used for search bar)
     */
    fun searchProducts(query: String): List<Product> {
        return productRepository.findProductsByMarketHashNameContainingIgnoreCase(query)
    }

    fun deleteOwnedProduct(ownedProduct: OwnedProduct) {
        ownedProductRepository.delete(ownedProduct)
    }
}