package com.example.SkinsManager.components.navbar

import com.example.SkinsManager.model.Product
import com.example.SkinsManager.service.ProductService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.notification.Notification

class AddProductButton(
    private val searchBox: SearchBox,
    private val productService: ProductService,
    private val onProductAdded: () -> Unit
) : Button("Add to Dashboard") {

    init {
        isEnabled = false
        style.set("background-color", "#2196f3")
        style.set("color", "#fff")
        style.set("border", "none")
        style.set("border-radius", "5px")
        style.set("padding", "8px 16px")
        style.set("cursor", "pointer")
        addHoverEffect("#1976d2", "#2196f3")

        addClickListener {
            searchBox.value?.let { selectedProduct: Product ->
                productService.addProductToDashboard(selectedProduct)
                Notification.show("${selectedProduct.marketHashName} added to dashboard")
                onProductAdded()
                searchBox.clear()
                isEnabled = false
            }
        }

        // Enable addButton only when product selected
        searchBox.addValueChangeListener { event ->
            isEnabled = event.value != null
        }
    }

    private fun addHoverEffect(hoverColor: String, normalColor: String) {
        element.setProperty("onmouseenter", "this.style.backgroundColor='$hoverColor'")
        element.setProperty("onmouseleave", "this.style.backgroundColor='$normalColor'")
    }
}