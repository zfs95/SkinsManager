package com.example.SkinsManager.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "owned_product_folder")
data class OwnedProductFolder(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var imageUrl: String? = null,

    @OneToMany(mappedBy = "folder", orphanRemoval = false)
    val ownedProducts: MutableList<OwnedProduct> = mutableListOf()
)