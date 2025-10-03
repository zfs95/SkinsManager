package com.example.SkinsManager.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "update_history")
data class UpdateHistory (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,  // auto-incremented unique ID

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "added_products", nullable = false)
    val addedProducts: Int = 0,

    @Column(name = "updated_products", nullable = false)
    val updatedProducts: Int = 0,

    @Column(name = "total_products", nullable = false)
    val totalProducts: Int = 0
)