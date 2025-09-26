package com.example.SkinsManager.components.navbar

import com.example.SkinsManager.model.Product
import com.example.SkinsManager.service.ProductService
import com.vaadin.flow.component.combobox.ComboBox

class SearchBox(private val productService: ProductService) : ComboBox<Product>() {

    init {
        setItemLabelGenerator { it.marketHashName }
        placeholder = "Search products..."
        isAllowCustomValue = false
        width = "50%"
        style.set("color", "#fff")
        style.set("background-color", "#1c1c1c")
        style.set("border", "1px solid #333")
        style.set("border-radius", "5px")
        style.set("padding", "5px 10px")
        element.style.set("--vaadin-combo-box-text-field-input", "#aaa")

        element.executeJs(
            """
                this.$.overlay.style.setProperty('--vaadin-combo-box-overlay-background-color', '#1c1c1c');
                this.$.overlay.style.setProperty('--vaadin-combo-box-overlay-text-color', '#fff');
                this.$.overlay.style.setProperty('--vaadin-combo-box-item-hover-background-color', '#333');
                this.$.overlay.style.setProperty('--vaadin-combo-box-item-selected-background-color', '#444');
            """.trimIndent()
        )

        // Live search provider
        setDataProvider(
            { query, offset, limit ->
                productService.searchProducts(query)
                    .drop(offset)
                    .take(limit)
                    .stream()
            },
            { query -> productService.searchProducts(query).size }
        )
    }
}