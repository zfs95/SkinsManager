package com.example.SkinsManager.repository

import com.example.SkinsManager.model.OwnedProduct
import com.example.SkinsManager.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface OwnedProductRepository : JpaRepository<OwnedProduct, Long> {

    @Query("SELECT o FROM OwnedProduct o JOIN FETCH o.product WHERE o.product = :product")
    fun findOwnedProductByProduct(product: Product): OwnedProduct?

    @Query("SELECT o FROM OwnedProduct o JOIN FETCH o.product")
    fun findAllWithProduct(): List<OwnedProduct>
}