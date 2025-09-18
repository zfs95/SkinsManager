package com.example.SkinsManager.views

import com.example.SkinsManager.model.Product
import com.example.SkinsManager.service.ProductService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.*
import com.vaadin.flow.router.Route
import kotlinx.coroutines.runBlocking

@Route("")
class DashboardView(
    private val productService: ProductService
) : VerticalLayout() {

    private val searchBox = ComboBox<Product>()
    private val ownedProductsLayout = FlexLayout()

    init {
        setSizeFull()
        isPadding = false
        isSpacing = false
        style.set("background-color", "#121212") // page background

        // --- Navbar ---
        val navbar = HorizontalLayout().apply {
            setWidthFull()
            isPadding = true
            isSpacing = true
            alignItems = FlexComponent.Alignment.CENTER
            justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
            style.set("background", "linear-gradient(90deg, #1c1c1c, #111111)")
            style.set("box-shadow", "0 2px 4px rgba(0,0,0,0.5)")
            style.set("padding", "10px 20px")
        }

        // Update button
        val updateButton = Button("Update Products") {
            Notification.show("Updating products...")
            runBlocking { productService.updateAllProducts() }
            Notification.show("Products updated successfully!")
            refreshOwnedProducts()
        }.apply {
            style.set("background-color", "#ff5722")
            style.set("color", "#fff")
            style.set("border", "none")
            style.set("border-radius", "5px")
            style.set("padding", "8px 16px")
            style.set("cursor", "pointer")
            addHoverEffect()
        }

        // Search box
        searchBox.setItemLabelGenerator { it.marketHashName }
        searchBox.placeholder = "Search products..."
        searchBox.isAllowCustomValue = false
        searchBox.width = "50%"
        searchBox.style.set("color", "#fff")
        searchBox.style.set("background-color", "#1c1c1c")
        searchBox.style.set("border", "1px solid #333")
        searchBox.style.set("border-radius", "5px")
        searchBox.style.set("padding", "5px 10px")
        searchBox.element.style.set("--vaadin-combo-box-text-field-input", "#aaa") // placeholder visible

        // Dropdown overlay styling
        searchBox.element.executeJs(
            """
                this.$.overlay.style.setProperty('--vaadin-combo-box-overlay-background-color', '#1c1c1c');
                this.$.overlay.style.setProperty('--vaadin-combo-box-overlay-text-color', '#fff');
                this.$.overlay.style.setProperty('--vaadin-combo-box-item-hover-background-color', '#333');
                this.$.overlay.style.setProperty('--vaadin-combo-box-item-selected-background-color', '#444');
                """.trimIndent()
        )

        // Add-to-dashboard button
        val addButton = Button("Add to Dashboard").apply {
            isEnabled = false // Disabled until product selected
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
                    refreshOwnedProducts()
                    searchBox.clear()
                    isEnabled = false
                }
            }
        }

        // Enable addButton only when a product is selected
        searchBox.addValueChangeListener { event ->
            addButton.isEnabled = event.value != null
        }

        // Live search
        searchBox.setDataProvider(
            { query, offset, limit ->
                productService.searchProducts(query)
                    .drop(offset)
                    .take(limit)
                    .stream()
            },
            { query -> productService.searchProducts(query).size }
        )

        navbar.add(searchBox, addButton, updateButton)
        add(navbar)

        // --- Owned products layout ---
        ownedProductsLayout.apply {
            setWidthFull()
            style.set("gap", "20px")
            style.set("flex-wrap", "wrap")
            style.set("padding", "20px")
        }
        add(ownedProductsLayout)

        refreshOwnedProducts()
    }

    private fun refreshOwnedProducts() {
        ownedProductsLayout.removeAll()
        val ownedProducts = productService.getAllOwnedProducts()

        ownedProducts.forEach { ownedProduct ->
            val product = ownedProduct.product
            val card = VerticalLayout().apply {
                width = "200px"
                isPadding = true
                isSpacing = true
                style.set("border", "1px solid #333")
                style.set("border-radius", "10px")
                style.set("background-color", "#1e1e1e")
                style.set("color", "#fff")
                style.set("box-shadow", "0 2px 6px rgba(0,0,0,0.5)")
                alignItems = FlexComponent.Alignment.CENTER

                val imageUrl = product.imageUrl
                    ?: "https://www.vhv.rs/dpng/d/30-306353_csgo-awp-skins-list-hd-png-download.png"

                val image = Image(imageUrl, product.marketHashName).apply {
                    width = "180px"
                    height = "180px"
                    style.set("border-radius", "5px")
                    style.set("object-fit", "cover")
                }

                // --- Delete button ---
                val deleteButton = Button("Delete").apply {
                    style.set("background-color", "#f44336")
                    style.set("color", "#fff")
                    style.set("border", "none")
                    style.set("border-radius", "5px")
                    style.set("padding", "5px 12px")
                    style.set("cursor", "pointer")
                    addHoverEffect("#d32f2f", "#f44336")

                    addClickListener {
                        val confirm = com.vaadin.flow.component.dialog.Dialog().apply {
                            val text = Span("Are you sure you want to delete ${product.marketHashName}?")
                            val yes = Button("Yes") {
                                runBlocking {
                                    productService.deleteOwnedProduct(ownedProduct)
                                }
                                Notification.show("${product.marketHashName} deleted")
                                refreshOwnedProducts()
                                close()
                            }.apply { style.set("margin-right", "10px") }

                            val no = Button("No") { close() }

                            add(HorizontalLayout(text, yes, no))
                        }
                        confirm.open()
                    }
                }

                add(
                    image,
                    Span("Product: ${product.marketHashName}"),
                    Span("Currency: ${product.currency}"),
                    Span("Current Price: ${product.meanPrice ?: "-"}"),
                    deleteButton  // <-- add button here
                )
            }
            ownedProductsLayout.add(card)
        }
    }

    // Simple hover effect for buttons
    private fun Button.addHoverEffect(hoverColor: String = "#e64a19", normalColor: String = "#ff5722") {
        element.setProperty("onmouseenter", "this.style.backgroundColor='$hoverColor'")
        element.setProperty("onmouseleave", "this.style.backgroundColor='$normalColor'")
    }
}
