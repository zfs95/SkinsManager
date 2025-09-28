package com.example.SkinsManager.views

import com.example.SkinsManager.components.ProductCard
import com.example.SkinsManager.components.navbar.DashboardNavbar
import com.example.SkinsManager.model.OwnedProduct
import com.example.SkinsManager.service.OwnedProductFolderService
import com.example.SkinsManager.service.ProductService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.Route
import kotlinx.coroutines.runBlocking

@Route("folder")
class FolderDetailView(
    private val folderService: OwnedProductFolderService,
    private val productService: ProductService
) : VerticalLayout(), HasUrlParameter<Long> {

    private val folderProductsLayout = FlexLayout()
    private val selectedProducts = mutableSetOf<OwnedProduct>()
    private var currentFolderId: Long = 0

    init {
        setSizeFull()
        isPadding = false
        isSpacing = false
        style.set("background-color", "#121212")

        // --- Navbar ---
        val navbar = DashboardNavbar(productService) { refreshFolder(currentFolderId) }
        add(navbar)

        // --- Move to Dashboard button ---
        val moveToDashboardButton = Button("Move to Dashboard").apply {
            style.set("margin", "10px 20px")
            addClickListener {
                if (selectedProducts.isEmpty()) {
                    Notification.show("No products selected")
                    return@addClickListener
                }
                runBlocking {
                    folderService.removeOwnedProductsFromFolder(selectedProducts.map { it.id })
                }
                selectedProducts.clear()
                refreshFolder(currentFolderId)
                selectedProducts.clear()
                refreshFolder(currentFolderId)
            }
        }
        add(moveToDashboardButton)

        // --- Flex layout for product cards ---
        folderProductsLayout.apply {
            setWidthFull()
            style.set("gap", "20px")          // space between cards
            style.set("flex-wrap", "wrap")    // allow wrapping
            style.set("padding", "20px")      // padding around the layout
        }
        add(folderProductsLayout)
    }

    override fun setParameter(event: BeforeEvent, folderId: Long) {
        currentFolderId = folderId
        refreshFolder(folderId)
    }

    private fun refreshFolder(folderId: Long) {
        val folder = folderService.getFolderById(folderId) ?: return

        folderProductsLayout.removeAll()
        selectedProducts.clear()

        folder.ownedProducts.forEach { owned ->
            folderProductsLayout.add(
                ProductCard(owned, productService, onDelete = { refreshFolder(folderId) }) { product, selected ->
                    if (selected) selectedProducts.add(product)
                    else selectedProducts.remove(product)
                }
            )
        }
    }
}
