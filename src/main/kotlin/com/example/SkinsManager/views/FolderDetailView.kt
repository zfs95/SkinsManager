package com.example.SkinsManager.views

import com.example.SkinsManager.components.ProductCard
import com.example.SkinsManager.components.navbar.DashboardNavbar
import com.example.SkinsManager.model.OwnedProduct
import com.example.SkinsManager.service.OwnedProductFolderService
import com.example.SkinsManager.service.ProductService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
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
    }

    override fun setParameter(event: BeforeEvent, folderId: Long) {
        currentFolderId = folderId
        refreshFolder(folderId)
    }

    private fun refreshFolder(folderId: Long) {
        val folder = folderService.getFolderById(folderId) ?: return

        removeAll()  // remove previous content to refresh
        add(DashboardNavbar(productService) { refreshFolder(currentFolderId) })

        // --- Top header with folder name and image ---
        val folderImage = Image(
            folder.imageUrl ?: "https://img.icons8.com/fluency/96/folder-invoices.png",
            folder.name
        ).apply {
            width = "80px"
            height = "80px"
            style.set("object-fit", "cover")
            style.set("border-radius", "10px")
        }

        val folderName = Span(folder.name).apply {
            style.set("font-size", "24px")
            style.set("color", "#fff")
        }

        val headerLayout = HorizontalLayout(folderImage, folderName).apply {
            setWidthFull()
            isSpacing = true
            alignItems = FlexComponent.Alignment.CENTER
            style.set("padding", "20px")
        }

        add(headerLayout)

        // --- Toolbar with Back and Move buttons ---
        val toolbar = HorizontalLayout().apply {
            setWidthFull()
            style.set("padding", "10px 20px")
            isSpacing = true

            // Back button
            val backButton = Button("Back").apply {
                style.set("background-color", "#757575")
                style.set("color", "#fff")
                style.set("border-radius", "5px")
                style.set("padding", "5px 12px")
                addClickListener { ui.ifPresent { it.navigate("") } }
            }

            // Move to Dashboard button
            val moveToDashboardButton = Button("Move to Dashboard").apply {
                style.set("background-color", "#2196f3")
                style.set("color", "#fff")
                style.set("border-radius", "5px")
                style.set("padding", "5px 12px")
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
                }
            }

            var sortAscending = true

// Sort by Name button
            val sortByNameButton = Button("Sort by Name ↑").apply {
                style.set("background-color", "#4caf50")
                style.set("color", "#fff")
                style.set("border-radius", "5px")
                style.set("padding", "5px 12px")

                addClickListener {
                    // Remove current cards
                    folderProductsLayout.removeAll()

                    // Sort products based on current flag
                    val sortedProducts = if (sortAscending) {
                        folder.ownedProducts.sortedBy { it.product.marketHashName.lowercase() }
                    } else {
                        folder.ownedProducts.sortedByDescending { it.product.marketHashName.lowercase() }
                    }

                    // Add sorted cards
                    sortedProducts.forEach { owned ->
                        folderProductsLayout.add(
                            ProductCard(owned, productService, onDelete = { refreshFolder(currentFolderId) }) { product, selected ->
                                if (selected) selectedProducts.add(product)
                                else selectedProducts.remove(product)
                            }
                        )
                    }

                    // Toggle sort order and update button text
                    sortAscending = !sortAscending
                    text = if (sortAscending) "Sort by Name ↑" else "Sort by Name ↓"
                }
            }


            add(backButton, moveToDashboardButton, sortByNameButton)
        }

        add(toolbar)

        // --- Flex layout for product cards ---
        folderProductsLayout.removeAll()
        folderProductsLayout.apply {
            setWidthFull()
            style.set("gap", "20px")
            style.set("flex-wrap", "wrap")
            style.set("padding", "20px")
        }

        folder.ownedProducts.forEach { owned ->
            folderProductsLayout.add(
                ProductCard(owned, productService, onDelete = { refreshFolder(folderId) }) { product, selected ->
                    if (selected) selectedProducts.add(product)
                    else selectedProducts.remove(product)
                }
            )
        }

        add(folderProductsLayout)
    }
}
