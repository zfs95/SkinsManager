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
import java.text.SimpleDateFormat
import java.util.*

class ProductCard(
    private val ownedProduct: OwnedProduct,
    private val productService: ProductService,
    private val onDelete: () -> Unit
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
        style.set("display", "flex")
        style.set("flex-direction", "column")
        style.set("cursor", "pointer")
        alignItems = FlexComponent.Alignment.CENTER

        val product = ownedProduct.product

        val imageUrl = product.imageUrl
            ?: "https://img.icons8.com/liquid-glass/96/question-mark.png"

        val image = Image(imageUrl, product.marketHashName).apply {
            width = "180px"
            height = "180px"
            style.set("border-radius", "5px")
            style.set("object-fit", "cover")
        }

        // Card click navigates to detail page
        element.addEventListener("click") {
            ui.ifPresent { it.navigate("product/${ownedProduct.id}") }
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

            element.executeJs(
                """
                this.addEventListener('click', function(e) {
                    e.stopPropagation();
                });
                """.trimIndent()
            )

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

        // --- Image button ---
        val imageButton = Button("Image").apply {
            style.set("background-color", "#2196f3")
            style.set("color", "#fff")
            style.set("border", "none")
            style.set("border-radius", "5px")
            style.set("padding", "5px 12px")
            style.set("cursor", "pointer")
            addHoverEffect("#1976d2", "#2196f3")

            element.executeJs(
                """
                this.addEventListener('click', function(e) {
                    e.stopPropagation();
                });
                """.trimIndent()
            )

            addClickListener {
                val dialog = Dialog().apply {
                    val info = Span("Set image for ${product.marketHashName}:")
                    val manualButton = Button("Manual URL") {
                        val manualDialog = Dialog().apply {
                            val urlField = com.vaadin.flow.component.textfield.TextField("Image URL")
                            val save = Button("Save") {
                                val newUrl = urlField.value.trim()
                                if (newUrl.isNotEmpty()) {
                                    image.src = newUrl
                                    runBlocking { productService.updateProductImage(product.id, newUrl) }
                                    Notification.show("Image updated manually")
                                    close()
                                } else {
                                    Notification.show("Please enter a URL")
                                }
                            }
                            val cancel = Button("Cancel") { close() }
                            add(com.vaadin.flow.component.orderedlayout.HorizontalLayout(urlField))
                            footer.add(HorizontalLayout(save, cancel))
                        }
                        manualDialog.open()
                        close()
                    }

                    val autoButton = Button("Automatic Fetch") {
                        runBlocking {
                            val imageUrl = productService.fetchAndSaveProductImage(product.id)
                            if (imageUrl != null) {
                                image.src = imageUrl
                                Notification.show("Image fetched automatically")
                            } else {
                                Notification.show("Failed to fetch image")
                            }
                        }
                        close()
                    }

                    add(info)
                    add(HorizontalLayout(manualButton, autoButton).apply { isSpacing = true })
                }
                dialog.open()
            }
        }

        // Format updatedAt as readable date
        val updatedDate = product.updatedAt?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(it*1000))
        } ?: "-"

        // --- Calculate total owned for this product safely ---
        val totalOwned = runBlocking {
            productService.getPurchasesForOwnedProduct(ownedProduct.id)
                .sumOf { it.quantity }
        }

        // --- Content layout grows to push buttons to bottom ---
        val contentLayout = VerticalLayout().apply {
            isPadding = false
            isSpacing = true
            style.set("flex-grow", "1")
            style.set("background-color", "#1e1e1e") // match the card's background
            alignItems = FlexComponent.Alignment.CENTER

            add(
                image,
                Span("Product: ${product.marketHashName}"),
                Span("Updated: $updatedDate"),
                Span("Market qty: ${product.quantity ?: "-"}"),
                Span("Owned qty: $totalOwned"),
                Span("Current Price: €${product.minPrice ?: "-"}")
            )
        }

        val buttonLayout = HorizontalLayout(imageButton, deleteButton).apply {
            isSpacing = true
            width = "100%"
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        }

        add(contentLayout, buttonLayout)
    }

    private fun Button.addHoverEffect(hoverColor: String, normalColor: String) {
        element.setProperty("onmouseenter", "this.style.backgroundColor='$hoverColor'")
        element.setProperty("onmouseleave", "this.style.backgroundColor='$normalColor'")
    }
}
