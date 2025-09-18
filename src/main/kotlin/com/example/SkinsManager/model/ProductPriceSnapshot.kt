package com.example.SkinsManager.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "product_price_snapshot")
data class ProductPriceSnapshot(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false)
    val snapshotTime: Instant = Instant.now(),

    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val meanPrice: Double? = null,
    val medianPrice: Double? = null,
    val quantity: Int? = null
)
