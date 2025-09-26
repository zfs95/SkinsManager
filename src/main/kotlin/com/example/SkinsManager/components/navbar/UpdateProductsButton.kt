package com.example.SkinsManager.components.navbar

import com.example.SkinsManager.service.ProductService
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import kotlinx.coroutines.runBlocking

class UpdateProductsButton(
    private val productService: ProductService,
    private val onProductUpdated: () -> Unit
) : Button("Update Products") {

    init {
        style.set("background-color", "#ff5722")
        style.set("color", "#fff")
        style.set("border", "none")
        style.set("border-radius", "5px")
        style.set("padding", "8px 16px")
        style.set("cursor", "pointer")
        addHoverEffect("#e64a19", "#ff5722")

        addClickListener {
            showConfirmationDialog()
        }
    }

    private fun showConfirmationDialog() {
        val dialog = Dialog()
        dialog.width = "350px"

        val message = Span("Do you want to manually start updating all products?")

        val yesButton = Button("Yes") {
            // Close dialog immediately
            dialog.close()

            // Start update asynchronously
            Thread {
                runBlocking {
                    productService.updateAllProducts()
                }
                // Show success notification on the UI thread
                UI.getCurrent().access {
                    Notification.show("Products updated successfully!")
                    onProductUpdated()
                }
            }.start()

            Notification.show("Updating products...") // immediate feedback
        }.apply {
            style.set("background-color", "#4caf50")
            style.set("color", "white")
            style.set("border-radius", "5px")
        }

        val noButton = Button("No") { dialog.close() }.apply {
            style.set("background-color", "#f44336")
            style.set("color", "white")
            style.set("border-radius", "5px")
        }

        val buttonsLayout = HorizontalLayout(yesButton, noButton)
        dialog.add(message, buttonsLayout)
        dialog.open()
    }


    private fun addHoverEffect(hoverColor: String, normalColor: String) {
        element.setProperty("onmouseenter", "this.style.backgroundColor='$hoverColor'")
        element.setProperty("onmouseleave", "this.style.backgroundColor='$normalColor'")
    }
}
