package com.example.SkinsManager.components.navbar

import com.example.SkinsManager.service.ProductService
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class UpdateProductsButton(
    private val productService: ProductService,
    private val onProductUpdated: () -> Unit
) : Button("Update Products"), CoroutineScope {

    // Define a CoroutineScope tied to a background dispatcher
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job

    init {
        style.set("background-color", "#ff5722")
        style.set("color", "#fff")
        style.set("border", "none")
        style.set("border-radius", "5px")
        style.set("padding", "8px 16px")
        style.set("cursor", "pointer")
        addHoverEffect("#e64a19", "#ff5722")

        addClickListener { showConfirmationDialog() }
    }

    private fun showConfirmationDialog() {
        val dialog = Dialog()
        dialog.width = "350px"

        val message = Span("Do you want to manually start updating all products?")

        val yesButton = Button("Yes") {
            dialog.close()
            Notification.show("Updating products...")

            // Capture the current UI for later use
            val ui = UI.getCurrent() ?: return@Button

            // Launch a coroutine on the Default dispatcher for long-running work
            launch {
                try {
                    productService.updateAllProducts() // heavy operation

                    // Switch back to UI thread to update notifications
                    ui.access {
                        Notification.show("Products updated successfully!")
                        onProductUpdated()
                    }
                } catch (e: Exception) {
                    ui.access {
                        Notification.show("Failed to update products: ${e.message}")
                    }
                }
            }
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

        dialog.add(message, HorizontalLayout(yesButton, noButton))
        dialog.open()
    }

    private fun addHoverEffect(hoverColor: String, normalColor: String) {
        element.setProperty("onmouseenter", "this.style.backgroundColor='$hoverColor'")
        element.setProperty("onmouseleave", "this.style.backgroundColor='$normalColor'")
    }

    // Cancel the scope when the button/component is detached
    override fun onDetach(detachEvent: DetachEvent?) {
        super.onDetach(detachEvent)
        job.cancel()
    }
}
