package com.example.SkinsManager.repository

import com.example.SkinsManager.model.OwnedProduct
import com.example.SkinsManager.model.OwnedProductFolder
import com.example.SkinsManager.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface OwnedProductRepository : JpaRepository<OwnedProduct, Long> {

    @Query("SELECT op FROM OwnedProduct op JOIN FETCH op.product WHERE op.id = :id")
    fun findWithProductById(@Param("id") id: Long): OwnedProduct?

    @Query("SELECT o FROM OwnedProduct o JOIN FETCH o.product")
    fun findAllWithProduct(): List<OwnedProduct>

    @Query("""
    SELECT op
    FROM OwnedProduct op
    LEFT JOIN FETCH op.purchases
    JOIN FETCH op.product
    WHERE op.id = :id
    """)
    fun findByIdWithProductAndPurchases(@Param("id") id: Long): OwnedProduct?

    @Query("""
        SELECT op
        FROM OwnedProduct op
        LEFT JOIN FETCH op.purchases
        WHERE op.id = :id
    """)
    fun findByIdWithPurchases(@Param("id") id: Long): OwnedProduct?

    @Query("SELECT o FROM OwnedProduct o JOIN FETCH o.product LEFT JOIN FETCH o.purchases")
    fun findAllWithProductAndPurchases(): List<OwnedProduct>

    fun findOwnedProductByProductId(productId: Long): OwnedProduct?

    fun findByFolderIsNull(): List<OwnedProduct> // dashboard Products
    fun findByFolder(folder: OwnedProductFolder): List<OwnedProduct>

    @Query("SELECT op FROM OwnedProduct op JOIN FETCH op.product WHERE op.folder IS NULL")
    fun findOwnedProductsOnDashboard(): List<OwnedProduct>

    @Query("SELECT f FROM OwnedProductFolder f LEFT JOIN FETCH f.ownedProducts WHERE f.id = :id")
    fun findByIdWithOwnedProducts(@Param("id") id: Long): OwnedProductFolder?

    @Query("""
    SELECT op 
    FROM OwnedProduct op
    JOIN FETCH op.product
    WHERE op.folder = :folder
""")
    fun findByFolderWithProduct(@Param("folder") folder: OwnedProductFolder): List<OwnedProduct>

    @Query("""
    SELECT f
    FROM OwnedProductFolder f
    LEFT JOIN FETCH f.ownedProducts op
    LEFT JOIN FETCH op.product
    WHERE f.id = :id
""")
    fun findByIdWithOwnedProductsAndProducts(@Param("id") id: Long): OwnedProductFolder?

}