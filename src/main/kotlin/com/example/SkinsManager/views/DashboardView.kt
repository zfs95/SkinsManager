package com.example.SkinsManager.views

import com.example.SkinsManager.components.FolderCard
import com.example.SkinsManager.components.ProductCard
import com.example.SkinsManager.components.navbar.DashboardNavbar
import com.example.SkinsManager.model.OwnedProduct
import com.example.SkinsManager.service.OwnedProductFolderService
import com.example.SkinsManager.service.ProductService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route

@Route("")
class DashboardView(
    private val productService: ProductService,
    private val folderService: OwnedProductFolderService
) : VerticalLayout() {

    private val ownedProductsLayout = FlexLayout()
    private val foldersLayout = FlexLayout()
    private val selectedProducts = mutableSetOf<OwnedProduct>()

    init {
        setSizeFull()
        isPadding = false
        isSpacing = false
        style.set("background-color", "#121212")

        // --- Navbar ---
        val navbar = DashboardNavbar(productService) { refreshDashboard() }
        add(navbar)

// --- Toolbar ---
        val toolbar = HorizontalLayout().apply {
            setWidthFull()
            style.set("padding", "10px 20px")
            isSpacing = true

            // Move to Folder button
            val moveButton = Button("Move to Folder").apply {
                style.set("background-color", "#2196f3") // blue
                style.set("color", "#fff")
                style.set("border-radius", "5px")
                style.set("padding", "5px 12px")
                addClickListener { openMoveDialog() }
            }

            // Create Folder button
            val createFolderButton = Button("Create Folder").apply {
                style.set("background-color", "#4caf50") // green
                style.set("color", "#fff")
                style.set("border-radius", "5px")
                style.set("padding", "5px 12px")
                addClickListener {
                    folderService.createFolder("New Folder")
                    refreshDashboard()
                }
            }

            add(moveButton, createFolderButton)
        }
        add(toolbar)

        // --- Folders layout ---
        foldersLayout.apply {
            setWidthFull()
            style.set("gap", "20px")
            style.set("flex-wrap", "wrap")
            style.set("padding", "20px")
        }
        add(foldersLayout)

        // --- Owned products layout ---
        ownedProductsLayout.apply {
            setWidthFull()
            style.set("gap", "20px")
            style.set("flex-wrap", "wrap")
            style.set("padding", "20px")
        }
        add(ownedProductsLayout)

        refreshDashboard()
    }

    private fun refreshDashboard() {
        // Refresh folders
        foldersLayout.removeAll()
        val folders = folderService.getAllFolders()
        folders.forEach { folder ->
            val card = FolderCard(
                folder,
                folderService,
                onOpen = { ui.ifPresent { it.navigate("folder/${folder.id}") } },
                onDelete = {
                    folderService.deleteFolder(it.id)
                    refreshDashboard()
                },
                // onUpdate = { refreshDashboard() } // refresh when folder edited
            )
            foldersLayout.add(card)
        }

        // Refresh products
        ownedProductsLayout.removeAll()
        selectedProducts.clear()
        val ownedProducts = productService.getOwnedProductsOnDashboard()

        ownedProducts.forEach { ownedProduct ->
            val card = ProductCard(
                ownedProduct,
                productService,
                onDelete = { refreshDashboard() }
            ) { product, selected ->
                if (selected) selectedProducts.add(product)
                else selectedProducts.remove(product)
            }
            ownedProductsLayout.add(card)
        }
    }

    private fun openMoveDialog() {
        val folders = folderService.getAllFolders()
        if (folders.isEmpty()) {
            Notification.show("No folders available")
            return
        }

        if (selectedProducts.isEmpty()) {
            Notification.show("No products selected")
            return
        }

        val dialog = Dialog().apply {
            add(Span("Move selected products to:"))
            val buttons = folders.map { folder ->
                Button(folder.name) {
                    folderService.moveOwnedProductsToFolder(
                        selectedProducts.map { it.id },
                        folder.id
                    )
                    selectedProducts.clear()
                    refreshDashboard()
                    close()
                }
            }
            add(VerticalLayout(*buttons.toTypedArray()))
        }
        dialog.open()
    }
}
