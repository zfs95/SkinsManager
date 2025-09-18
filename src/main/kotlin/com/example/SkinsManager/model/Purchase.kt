package com.example.SkinsManager.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "purchase")
data class Purchase(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owned_product_id", nullable = false)
    val ownedProduct: OwnedProduct,

    @Column(nullable = false)
    val purchaseDate: LocalDate,

    @Column(nullable = false)
    val quantity: Int,

    @Column(nullable = false)
    val unitPrice: Double
)
