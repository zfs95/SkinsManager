package com.example.SkinsManager.components

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.example.SkinsManager.model.OwnedProductFolder
import com.example.SkinsManager.service.OwnedProductFolderService
import kotlinx.coroutines.runBlocking
import java.text.NumberFormat
import java.util.*

class FolderCard(
    private val folder: OwnedProductFolder,
    private val folderService: OwnedProductFolderService,
    private val onOpen: (OwnedProductFolder) -> Unit,
    private val onDelete: (OwnedProductFolder) -> Unit,
    private val onUpdate: ((OwnedProductFolder) -> Unit)? = null // optional callback
) : VerticalLayout() {

    init {
        width = "200px"
        style.set("border", "1px solid #555")
        style.set("border-radius", "10px")
        style.set("background-color", "#2a2a2a")
        style.set("color", "#fff")
        style.set("cursor", "pointer")
        alignItems = FlexComponent.Alignment.CENTER
        isSpacing = false

        val image = Image(
            folder.imageUrl ?: "https://img.icons8.com/fluency/96/folder-invoices.png",
            folder.name
        ).apply {
            width = "120px"
            height = "120px"
            style.set("object-fit", "cover")
        }

        val title = Span(folder.name).apply {
            style.set("font-weight", "bold")
            style.set("margin-top", "10px")
        }

        // --- Folder overview (only showing profit, keeping others commented) ---
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
        val overview = folderService.getFolderOverview(folder)

        // val paidSpan = Span("Paid: ${currencyFormatter.format(overview.totalCost)}").apply {
        //     style.set("font-size", "12px")
        // }
        // val currentSpan = Span("Current: ${currencyFormatter.format(overview.currentValue)}").apply {
        //     style.set("font-size", "12px")
        // }

        val profitSpan = Span("Profit: ${currencyFormatter.format(overview.profit)}").apply {
            style.set("font-size", "12px")
            style.set("font-weight", "bold")
            style.set("color", if (overview.profit >= 0) "limegreen" else "red")
        }

        val overviewLayout = VerticalLayout(
            // paidSpan,
            // currentSpan,
            profitSpan
        ).apply {
            isSpacing = false
            alignItems = FlexComponent.Alignment.CENTER
            style.set("background", "transparent")
            style.set("padding", "0px")
            style.set("margin", "0px")
        }

        // --- Card click opens folder ---
        element.addEventListener("click") {
            onOpen(folder)
        }

        // --- Buttons ---
        val deleteButton = Button("Delete").apply {
            style.set("background-color", "#f44336")
            style.set("color", "#fff")
            style.set("border-radius", "5px")
            style.set("padding", "5px 12px")
            style.set("cursor", "pointer")

            element.executeJs("""
                this.addEventListener('click', function(e) { e.stopPropagation(); });
            """.trimIndent())

            addClickListener {
                val confirm = Dialog().apply {
                    add(Span("Delete folder ${folder.name}?"))
                    add(
                        HorizontalLayout(
                            Button("Yes") {
                                onDelete(folder)
                                close()
                            },
                            Button("No") { close() }
                        )
                    )
                }
                confirm.open()
            }
        }

        val editButton = Button("Edit").apply {
            style.set("background-color", "#2196f3")
            style.set("color", "#fff")
            style.set("border-radius", "5px")
            style.set("padding", "5px 12px")
            style.set("cursor", "pointer")

            element.executeJs("""
                this.addEventListener('click', function(e) { e.stopPropagation(); });
            """.trimIndent())

            addClickListener {
                val dialog = Dialog().apply {
                    val nameField = TextField("Folder Name").apply { value = folder.name }
                    val imageField = TextField("Image URL").apply { value = folder.imageUrl ?: "" }
                    val saveButton = Button("Save") {
                        runBlocking {
                            folderService.updateFolder(
                                folder.id,
                                nameField.value,
                                imageField.value.ifBlank { null }
                            )
                        }
                        title.text = nameField.value
                        image.src = imageField.value.ifBlank { "https://img.icons8.com/fluency/96/folder-invoices.png" }
                        onUpdate?.invoke(folder)
                        close()
                    }
                    val cancelButton = Button("Cancel") { close() }

                    add(nameField, imageField)
                    add(HorizontalLayout(saveButton, cancelButton))
                }
                dialog.open()
            }
        }

        val buttonLayout = HorizontalLayout(editButton, deleteButton).apply {
            isSpacing = true
            width = "100%"
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        }

        // --- Assemble card ---
        add(image, title, overviewLayout, buttonLayout)
    }
}
