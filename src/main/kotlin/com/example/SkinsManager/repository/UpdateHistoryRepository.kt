package com.example.SkinsManager.repository

import com.example.SkinsManager.model.UpdateHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UpdateHistoryRepository: JpaRepository<UpdateHistory, Long> {
    @Query("SELECT u FROM UpdateHistory u ORDER BY u.updatedAt DESC")
    fun findLatestUpdate(): List<UpdateHistory>
}