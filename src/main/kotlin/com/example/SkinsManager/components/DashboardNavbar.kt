package com.example.SkinsManager.components

import com.example.SkinsManager.model.Product
import com.example.SkinsManager.service.ProductService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import kotlinx.coroutines.runBlocking

class DashboardNavbar(
    private val productService: ProductService,
    private val onProductAdded: () -> Unit // callback to refresh products
) : HorizontalLayout() {

    private val searchBox = ComboBox<Product>()
    private val addButton: Button
    private val updateButton: Button

    init {
        setWidthFull()
        isPadding = true
        isSpacing = true
        alignItems = FlexComponent.Alignment.CENTER
        justifyContentMode = JustifyContentMode.BETWEEN
        style.set("background", "linear-gradient(90deg, #1c1c1c, #111111)")
        style.set("box-shadow", "0 2px 4px rgba(0,0,0,0.5)")
        style.set("padding", "10px 20px")

        // Search box setup
        searchBox.setItemLabelGenerator { it.marketHashName }
        searchBox.placeholder = "Search products..."
        searchBox.isAllowCustomValue = false
        searchBox.width = "50%"
        searchBox.style.set("color", "#fff")
        searchBox.style.set("background-color", "#1c1c1c")
        searchBox.style.set("border", "1px solid #333")
        searchBox.style.set("border-radius", "5px")
        searchBox.style.set("padding", "5px 10px")
        searchBox.element.style.set("--vaadin-combo-box-text-field-input", "#aaa")

        searchBox.element.executeJs(
            """
                this.$.overlay.style.setProperty('--vaadin-combo-box-overlay-background-color', '#1c1c1c');
                this.$.overlay.style.setProperty('--vaadin-combo-box-overlay-text-color', '#fff');
                this.$.overlay.style.setProperty('--vaadin-combo-box-item-hover-background-color', '#333');
                this.$.overlay.style.setProperty('--vaadin-combo-box-item-selected-background-color', '#444');
            """.trimIndent()
        )

        // Live search provider
        searchBox.setDataProvider(
            { query, offset, limit ->
                productService.searchProducts(query)
                    .drop(offset)
                    .take(limit)
                    .stream()
            },
            { query -> productService.searchProducts(query).size }
        )

        // Add button
        addButton = Button("Add to Dashboard").apply {
            isEnabled = false
            style.set("background-color", "#2196f3")
            style.set("color", "#fff")
            style.set("border", "none")
            style.set("border-radius", "5px")
            style.set("padding", "8px 16px")
            style.set("cursor", "pointer")
            addHoverEffect("#1976d2", "#2196f3")
            addClickListener {
                searchBox.value?.let { selectedProduct ->
                    productService.addProductToDashboard(selectedProduct)
                    Notification.show("${selectedProduct.marketHashName} added to dashboard")
                    onProductAdded()
                    searchBox.clear()
                    isEnabled = false
                }
            }
        }

        // Enable addButton only when product selected
        searchBox.addValueChangeListener { event ->
            addButton.isEnabled = event.value != null
        }

        // Update button
        updateButton = Button("Update Products") {
            Notification.show("Updating products...")
            runBlocking { productService.updateAllProducts() }
            Notification.show("Products updated successfully!")
            onProductAdded()
        }.apply {
            style.set("background-color", "#ff5722")
            style.set("color", "#fff")
            style.set("border", "none")
            style.set("border-radius", "5px")
            style.set("padding", "8px 16px")
            style.set("cursor", "pointer")
            addHoverEffect("#e64a19", "#ff5722")
        }

        add(searchBox, addButton, updateButton)
    }

    private fun Button.addHoverEffect(hoverColor: String, normalColor: String) {
        element.setProperty("onmouseenter", "this.style.backgroundColor='$hoverColor'")
        element.setProperty("onmouseleave", "this.style.backgroundColor='$normalColor'")
    }
}
