package com.example.SkinsManager.repository

import com.example.SkinsManager.model.OwnedProduct
import com.example.SkinsManager.model.Purchase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PurchaseRepository : JpaRepository<Purchase, Long>{
    fun findByOwnedProduct(ownedProduct: OwnedProduct): List<Purchase>
}