package com.example.SkinsManager.model

import jakarta.persistence.*

@Entity
@Table(name = "product")
data class Product(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val marketHashName: String,

    @Column(nullable = false)
    val currency: String,

    val suggestedPrice: Double? = null,
    val itemPage: String? = null,
    val marketPage: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val meanPrice: Double? = null,
    val medianPrice: Double? = null,
    val quantity: Int? = null,
    val createdAtApi: Long? = null,
    val updatedAtApi: Long? = null,

    @Column(nullable = false)
    val isActive: Boolean = true,

    val imageUrl: String? = null,
    val localImagePath: String? = null
)
