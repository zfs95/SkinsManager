package com.example.SkinsManager.views

import com.example.SkinsManager.components.ProductCard
import com.example.SkinsManager.components.navbar.DashboardNavbar
import com.example.SkinsManager.service.ProductService
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route

@Route("")
class DashboardView(
    private val productService: ProductService
) : VerticalLayout() {

    private val ownedProductsLayout = FlexLayout()

    init {
        setSizeFull()
        isPadding = false
        isSpacing = false
        style.set("background-color", "#121212") // page background

        // --- Navbar (refactored into its own component) ---
        val navbar = DashboardNavbar(productService) { refreshOwnedProducts() }
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
            val card = ProductCard(ownedProduct, productService) { refreshOwnedProducts() }
            ownedProductsLayout.add(card)
        }
    }
}
