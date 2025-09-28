package com.example.SkinsManager.service

import com.example.SkinsManager.model.OwnedProductFolder
import com.example.SkinsManager.repository.OwnedProductFolderRepository
import com.example.SkinsManager.repository.OwnedProductRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class OwnedProductFolderService (
    val ownedProductRepository: OwnedProductRepository,
    val folderRepository: OwnedProductFolderRepository
) {

    @Transactional
    fun createFolder(name: String, imageUrl: String? = null): OwnedProductFolder {
        val folder = OwnedProductFolder(name = name, imageUrl = imageUrl)
        return folderRepository.save(folder)
    }


    @Transactional
    fun moveOwnedProductsToFolder(ownedProductIds: List<Long>, folderId: Long) {
        val folder = folderRepository.findById(folderId)
            .orElseThrow { IllegalArgumentException("Folder $folderId not found") }

        val ownedProducts = ownedProductRepository.findAllById(ownedProductIds)

        ownedProducts.forEach { it.folder = folder }
        ownedProductRepository.saveAll(ownedProducts)
    }

    @Transactional
    fun removeOwnedProductsFromFolder(ownedProductIds: List<Long>) {
        val ownedProducts = ownedProductRepository.findAllById(ownedProductIds)

        ownedProducts.forEach { it.folder = null }
        ownedProductRepository.saveAll(ownedProducts)
    }


    @Transactional
    fun deleteFolder(folderId: Long) {
        val folder = folderRepository.findById(folderId)
            .orElseThrow { IllegalArgumentException("Folder $folderId not found") }

        // Move products back to dashboard
        val ownedProducts = ownedProductRepository.findByFolder(folder)
        ownedProducts.forEach { it.folder = null }
        ownedProductRepository.saveAll(ownedProducts)

        // Delete the folder itself
        folderRepository.delete(folder)
    }

    @Transactional
    fun getAllFolders(): List<OwnedProductFolder> {
        return folderRepository.findAll()
    }


    @Transactional
    fun getFolderById(folderId: Long): OwnedProductFolder {
        return ownedProductRepository.findByIdWithOwnedProductsAndProducts(folderId)
            ?: throw IllegalArgumentException("Folder with id $folderId not found")
    }

    suspend fun updateFolder(folderId: Long, newName: String, newImageUrl: String?) {
        val folder = folderRepository.findById(folderId)
            .orElseThrow { IllegalArgumentException("Folder not found: $folderId") }

        folder.name = newName
        folder.imageUrl = newImageUrl

        folderRepository.save(folder)
    }
}