package com.example.SkinsManager.service

import com.example.SkinsManager.client.AssetClient
import com.example.SkinsManager.client.SkinportClient
import com.example.SkinsManager.dtos.HistoryDto
import com.example.SkinsManager.dtos.OwnedProductDto
import com.example.SkinsManager.dtos.PortfolioProduct
import com.example.SkinsManager.dtos.PortfolioSummary
import com.example.SkinsManager.dtos.ProductDto
import com.example.SkinsManager.dtos.PurchaseDto
import com.example.SkinsManager.model.OwnedProduct
import com.example.SkinsManager.model.OwnedProductFolder
import com.example.SkinsManager.model.Product
import com.example.SkinsManager.model.Purchase
import com.example.SkinsManager.repository.OwnedProductRepository
import com.example.SkinsManager.repository.ProductRepository
import com.example.SkinsManager.repository.PurchaseRepository
import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger

@Service
class ProductService(
    private val skinportClient: SkinportClient,
    private val productRepository: ProductRepository,
    private val ownedProductRepository: OwnedProductRepository,
    private val purchaseRepository: PurchaseRepository,
    private val assetClient: AssetClient,
    private val searchEngine: ProductSearchService
) {

    private val logger = Logger.getLogger(ProductService::class.java.name)

    /**
     * Fetch all products from Skinport API and upsert into database
     */
    @Transactional
    fun updateAllProducts() = runBlocking {
        val productsFromApi = skinportClient.getItems(appId = 730, currency = "EUR")

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
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt,
                isActive = true,
                slug = extractSlug(dto.itemPage)
            )
            val savedProduct = productRepository.save(newProduct)
            searchEngine.addOrUpdateProduct(savedProduct)
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
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt,
                isActive = true,
                slug = extractSlug(dto.itemPage)
            )
            val savedProduct = productRepository.save(updated)
            searchEngine.addOrUpdateProduct(savedProduct)
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
    @Transactional
    fun addProductToDashboard(product: Product): OwnedProduct {
        val existing = ownedProductRepository.findOwnedProductByProductId(product.id)
        if (existing != null) {
            return existing
        }

        val newOwned = OwnedProduct(
            product = product,
            addedAt = java.time.Instant.now()
        )
        return ownedProductRepository.save(newOwned)
    }


    /**
     * Search products from catalog by name (used for search bar)
     */
    fun searchProducts(query: String): List<Product> {
//        return productRepository.findProductsByMarketHashNameContainingIgnoreCase(query)
        val ids = searchEngine.search(query)
        return productRepository.findAllById(ids)
    }

    fun deleteOwnedProduct(ownedProduct: OwnedProduct) {
        ownedProductRepository.delete(ownedProduct)
    }

    fun getPurchasesForOwnedProduct(ownedProductId: Long): List<Purchase> {
        val ownedProduct = ownedProductRepository.findByIdWithProductAndPurchases(ownedProductId)
            ?: throw IllegalArgumentException("Owned product not found: $ownedProductId")

        return ownedProduct.purchases
    }

    @Transactional
    fun addPurchase(ownedProductId: Long, purchaseDto: PurchaseDto): PurchaseDto {
        val ownedProduct = ownedProductRepository.findByIdWithPurchases(ownedProductId)
            ?: throw IllegalArgumentException("Owned product not found: $ownedProductId")

        val purchase = Purchase(
            ownedProduct = ownedProduct,
            unitPrice = purchaseDto.unitPrice,
            quantity = purchaseDto.amount,
            purchaseDate = purchaseDto.date
        )

        val saved = purchaseRepository.save(purchase)
        return PurchaseDto(
            id = saved.id,
            unitPrice = saved.unitPrice,
            amount = saved.quantity,
            date = saved.purchaseDate
        )
    }


    fun updatePurchase(purchase: Purchase): Purchase {
        return purchaseRepository.save(purchase)
    }

    fun deletePurchase(purchase: Purchase) {
        purchaseRepository.delete(purchase)
    }

    @Transactional
    fun getOwnedProductDto(ownedProductId: Long): OwnedProductDto {
        val ownedProduct = ownedProductRepository.findByIdWithProductAndPurchases(ownedProductId)
            ?: throw IllegalArgumentException("OwnedProduct not found")

        // Map DB Product entity to ProductDto
        val productDto = ProductDto(
            marketHashName = ownedProduct.product.marketHashName,
            currency = ownedProduct.product.currency,
            suggestedPrice = ownedProduct.product.suggestedPrice,
            itemPage = ownedProduct.product.itemPage,
            marketPage = ownedProduct.product.marketPage,
            minPrice = ownedProduct.product.minPrice,
            maxPrice = ownedProduct.product.maxPrice,
            meanPrice = ownedProduct.product.meanPrice,
            medianPrice = ownedProduct.product.medianPrice,
            quantity = ownedProduct.product.quantity,
            createdAt = ownedProduct.product.createdAt,
            updatedAt = ownedProduct.product.updatedAt,
            imageUrl = ownedProduct.product.imageUrl
        )

        val purchasesDto = ownedProduct.purchases.map {
            PurchaseDto(
                id = it.id,
                date = it.purchaseDate,
                amount = it.quantity,
                unitPrice = it.unitPrice,
            )
        }

        return OwnedProductDto(
            id = ownedProduct.id,
            product = productDto,
            purchases = purchasesDto
        )
    }

    fun updatePurchase(purchaseDto: PurchaseDto): PurchaseDto {
        val purchase = purchaseRepository.findById(purchaseDto.id)
            .orElseThrow { IllegalArgumentException("Purchase not found: ${purchaseDto.id}") }

        val updated = purchase.copy(
            purchaseDate = purchaseDto.date,
            quantity = purchaseDto.amount,
            unitPrice = purchaseDto.unitPrice
        )

        val saved = purchaseRepository.save(updated)

        return PurchaseDto(
            id = saved.id,
            date = saved.purchaseDate,
            amount = saved.quantity,
            unitPrice = saved.unitPrice
        )
    }

    fun deletePurchase(purchaseId: Long) {
        val purchase = purchaseRepository.findById(purchaseId)
            .orElseThrow { IllegalArgumentException("Purchase not found: $purchaseId") }

        purchaseRepository.delete(purchase)
    }

    fun extractSlug(itemPage: String?): String? {
        return itemPage?.substringAfterLast("/")?.takeIf { it.isNotBlank() }
    }

    /**
     * Fetches image for a product using its slug and saves it to database.
     * Returns the URL if successful, null otherwise.
     */
    @Transactional
    fun fetchAndSaveProductImage(productId: Long): String? {
        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found") }

        val slug = product.slug ?: return null

        val response = assetClient.getItem(url = slug, "https://skinport.com/item/$slug")
        val imageId = response.data?.item?.image ?: return null

        val imageUrl = "https://community.steamstatic.com/economy/image/$imageId"
        val updatedProduct = product.copy(imageUrl = imageUrl)
        productRepository.save(updatedProduct)

        return imageUrl
    }

    @Transactional
    fun updateProductImage(productId: Long, imageUrl: String) {
        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found") }

        val updatedProduct = product.copy(imageUrl = imageUrl)
        productRepository.save(updatedProduct)
    }



    @Transactional(readOnly = true)
    fun getPortfolioOverview(): Pair<PortfolioSummary, List<PortfolioProduct>> {
        // Fetch all owned products with their purchases and product data
        val ownedProducts = ownedProductRepository.findAllWithProductAndPurchases()

        val products = ownedProducts.map { owned ->
            // Total paid for all purchases of this owned product
            val totalPaid = owned.purchases.sumOf { it.unitPrice * it.quantity }
            val totalQuantity = owned.purchases.sumOf { it.quantity }

            // Current price from the related Product entity
            // Using minPrice as "current market price"
            val currentUnitPrice = owned.product.minPrice ?: 0.0
            val currentValue = currentUnitPrice * totalQuantity

            PortfolioProduct(
                productName = owned.product.marketHashName,
                totalPaid = totalPaid,
                currentValue = currentValue,
                profit = currentValue - totalPaid
            )
        }

        val summary = PortfolioSummary(
            totalCost = products.sumOf { it.totalPaid },
            currentValue = products.sumOf { it.currentValue },
            profit = products.sumOf { it.profit }
        )

        return summary to products
    }

    @Transactional
    fun updateHistory(request: List<HistoryDto>) {
        val failures = mutableListOf<String>()
        val successes = mutableListOf<String>()

        for (dto in request) {
            try {
                // 1. Find product
                val product = productRepository.findProductByMarketHashName(dto.marketName)
                    ?: throw IllegalArgumentException("Product not found: ${dto.marketName}")

                // 2. Add product to dashboard (or reuse if already present)
                val ownedProduct = addProductToDashboard(product)

                // 3. Create a new purchase
                val purchase = Purchase(
                    ownedProduct = ownedProduct,
                    unitPrice = dto.displayPrice,
                    quantity = 1, // History items are single units, right? Adjust if needed
                    purchaseDate = java.time.LocalDate.now()
                )

                purchaseRepository.save(purchase)
                successes.add("Added purchase for ${dto.marketName} at €${dto.displayPrice}")

            } catch (ex: Exception) {
                failures.add("Failed for ${dto.marketName}: ${ex.message}")
                logger.warning("Failed for ${dto.marketName}: ${ex.message}")
            }
        }

        // Final summary log
        logger.info("History update completed")
        logger.info("Successes: ${successes.size}, Failures: ${failures.size}")
        successes.forEach { logger.info(it) }
        failures.forEach { logger.warning(it) }
    }

    @Transactional
    fun getOwnedProductsOnDashboard(): List<OwnedProduct> {
        return ownedProductRepository.findOwnedProductsOnDashboard()
    }

    fun findByIdWithOwnedProducts(folderId: Long): OwnedProductFolder? {
        return ownedProductRepository.findByIdWithOwnedProducts(folderId)
    }

}