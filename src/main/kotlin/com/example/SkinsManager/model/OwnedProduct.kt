package com.example.SkinsManager.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "owned_product", uniqueConstraints = [UniqueConstraint(columnNames = ["product_id"])])
data class OwnedProduct(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false)
    val addedAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "ownedProduct", cascade = [CascadeType.ALL], orphanRemoval = true)
    val purchases: MutableList<Purchase> = mutableListOf()
)
