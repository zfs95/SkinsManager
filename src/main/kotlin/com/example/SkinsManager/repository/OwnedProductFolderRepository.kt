package com.example.SkinsManager.repository

import com.example.SkinsManager.model.OwnedProductFolder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface OwnedProductFolderRepository : JpaRepository<OwnedProductFolder, Long> {
    fun findByName(name: String): OwnedProductFolder?
    @Query("SELECT f FROM OwnedProductFolder f LEFT JOIN FETCH f.ownedProducts WHERE f.id = :id")
    fun findByIdWithOwnedProducts(@Param("id") id: Long): OwnedProductFolder?
}