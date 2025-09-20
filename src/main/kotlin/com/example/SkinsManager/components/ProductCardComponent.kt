package com.example.SkinsManager.components

import com.example.SkinsManager.model.OwnedProduct
import com.example.SkinsManager.service.ProductService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import kotlinx.coroutines.runBlocking

class ProductCard(
    private val ownedProduct: OwnedProduct,
    private val productService: ProductService,
    private val onDelete: () -> Unit // callback to refresh UI
) : VerticalLayout() {

    init {
        width = "200px"
        isPadding = true
        isSpacing = true
        style.set("border", "1px solid #333")
        style.set("border-radius", "10px")
        style.set("background-color", "#1e1e1e")
        style.set("color", "#fff")
        style.set("box-shadow", "0 2px 6px rgba(0,0,0,0.5)")
        alignItems = FlexComponent.Alignment.CENTER

        val product = ownedProduct.product

        val imageUrl = product.imageUrl
            ?: "https://www.vhv.rs/dpng/d/30-306353_csgo-awp-skins-list-hd-png-download.png"

        val image = Image(imageUrl, product.marketHashName).apply {
            width = "180px"
            height = "180px"
            style.set("border-radius", "5px")
            style.set("object-fit", "cover")
        }

        val deleteButton = Button("Delete").apply {
            style.set("background-color", "#f44336")
            style.set("color", "#fff")
            style.set("border", "none")
            style.set("border-radius", "5px")
            style.set("padding", "5px 12px")
            style.set("cursor", "pointer")
            addHoverEffect("#d32f2f", "#f44336")

            addClickListener {
                val confirm = Dialog().apply {
                    val text = Span("Are you sure you want to delete ${product.marketHashName}?")
                    val yes = Button("Yes") {
                        runBlocking { productService.deleteOwnedProduct(ownedProduct) }
                        Notification.show("${product.marketHashName} deleted")
                        onDelete()
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
            deleteButton
        )
    }

    private fun Button.addHoverEffect(hoverColor: String, normalColor: String) {
        element.setProperty("onmouseenter", "this.style.backgroundColor='$hoverColor'")
        element.setProperty("onmouseleave", "this.style.backgroundColor='$normalColor'")
    }
}
