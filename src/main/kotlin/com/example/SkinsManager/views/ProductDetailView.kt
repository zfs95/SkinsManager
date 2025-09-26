package com.example.SkinsManager.views

import com.example.SkinsManager.dtos.OwnedProductDto
import com.example.SkinsManager.dtos.PurchaseDto
import com.example.SkinsManager.service.ProductService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.Route
import com.example.SkinsManager.components.DoubleToIntConverter
import com.example.SkinsManager.components.navbar.DashboardNavbar
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import java.time.LocalDate

@Route("product/:ownedProductId")
class ProductDetailView(
    private val productService: ProductService
) : VerticalLayout(), BeforeEnterObserver {

    private lateinit var ownedProduct: OwnedProductDto
    private lateinit var purchasesGrid: Grid<PurchaseDto>

    override fun beforeEnter(event: BeforeEnterEvent) {
        val idParam = event.routeParameters.get("ownedProductId")
            .orElseThrow { IllegalArgumentException("Missing product id") }
            .toLongOrNull() ?: throw IllegalArgumentException("Invalid product id")

        ownedProduct = productService.getOwnedProductDto(idParam)
        initView()
    }

    private fun initView() {
        removeAll()
        setSizeFull()
        isPadding = false
        isSpacing = false
        style.set("background-color", "#121212")

        // --- Navbar ---
        val navbar = DashboardNavbar(productService) { } // no refresh needed here
        add(navbar)

        // --- Content layout ---
        val content = VerticalLayout().apply {
            setWidthFull()
            style.set("padding", "20px")
            style.set("color", "white")
        }

// Product image
        val productImage = Image(
            ownedProduct.product.imageUrl ?: "https://img.icons8.com/liquid-glass/96/question-mark.png",
            ownedProduct.product.marketHashName
        ).apply {
            width = "80px"
            height = "80px"
            style.set("border-radius", "5px")
            style.set("object-fit", "cover")
        }

// Product name
        val title = H2("${ownedProduct.product.marketHashName} Details").apply {
            style.set("color", "white")
        }

// Horizontal layout with image + title
        val headerLayout = HorizontalLayout(productImage, title).apply {
            setAlignItems(FlexComponent.Alignment.CENTER)
            setSpacing(true)
            style.set("margin-bottom", "20px")
        }

// Add to content
        content.add(headerLayout)

        // Purchases grid
        purchasesGrid = Grid(PurchaseDto::class.java, false).apply {
            setWidthFull()
            addColumn { purchase -> "€${"%.2f".format(purchase.unitPrice)}" }
                .setHeader("Price")
            addColumn(PurchaseDto::amount).setHeader("Quantity")
            addColumn(PurchaseDto::date).setHeader("Date")
            addComponentColumn { purchase ->
                HorizontalLayout(
                    Button("Edit") { openEditPurchaseDialog(purchase) },
                    Button("Delete") { confirmDelete(purchase) }
                )
            }.setHeader("Actions")
            setItems(ownedProduct.purchases)
            addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
        }
        content.add(purchasesGrid)

        // Add purchase button
        val addButton = Button("Add Purchase") { openAddPurchaseDialog() }.apply {
            style.set("margin-top", "20px")
        }
        content.add(addButton)

        add(content)
    }

    private fun openAddPurchaseDialog() {
        val newPurchase = PurchaseDto()
        openPurchaseDialog("Add Purchase", newPurchase, isEdit = false)
    }

    private fun openEditPurchaseDialog(purchase: PurchaseDto) {
        openPurchaseDialog("Edit Purchase", purchase.copy(), isEdit = true)
    }

    private fun openPurchaseDialog(title: String, purchase: PurchaseDto, isEdit: Boolean) {
        val dialog = Dialog()
        dialog.width = "400px"
        dialog.headerTitle = title

        val priceField = NumberField("Price").apply { value = purchase.unitPrice }
        val quantityField = NumberField("Quantity").apply { value = purchase.amount.toDouble() }
        val dateField = DatePicker("Date").apply { value = purchase.date }

        val binder = Binder(PurchaseDto::class.java)
        binder.bean = purchase

        binder.forField(priceField)
            .bind({ it.unitPrice }, { bean, value -> bean.unitPrice = value ?: 0.0 })

        binder.forField(quantityField)
            .withConverter(DoubleToIntConverter())
            .bind({ it.amount }, { bean, value -> bean.amount = value ?: 0 })

        binder.forField(dateField)
            .bind({ it.date }, { bean, value -> bean.date = value ?: LocalDate.now() })

        val form = FormLayout(priceField, quantityField, dateField)
        dialog.add(form)

        val saveButton = Button("Save") {
            if (binder.validate().isOk) {
                val purchaseDto = binder.bean

                if (isEdit) {
                    // update existing
                    val updated = productService.updatePurchase(purchaseDto)
                    ownedProduct.purchases = ownedProduct.purchases.map {
                        if (it.id == updated.id) updated else it
                    }
                } else {
                    // add new
                    val saved = productService.addPurchase(ownedProduct.id, purchaseDto)
                    ownedProduct.purchases = ownedProduct.purchases + saved
                }

                purchasesGrid.setItems(ownedProduct.purchases)
                dialog.close()
                Notification.show(if (isEdit) "Purchase updated" else "Purchase added")
            }
        }

        val cancelButton = Button("Cancel") { dialog.close() }
        dialog.footer.add(HorizontalLayout(saveButton, cancelButton))
        dialog.open()
    }

    private fun confirmDelete(purchase: PurchaseDto) {
        val dialog = Dialog()
        dialog.headerTitle = "Confirm Delete"

        dialog.add("Are you sure you want to delete this purchase?")

        val confirm = Button("Delete") {
            try {
                productService.deletePurchase(purchase.id)
                ownedProduct.purchases = ownedProduct.purchases.filterNot { it.id == purchase.id }
                purchasesGrid.setItems(ownedProduct.purchases)
                Notification.show("Purchase deleted")
            } catch (e: Exception) {
                Notification.show("Error deleting purchase: ${e.message}")
            } finally {
                dialog.close()
            }
        }.apply { style.set("color", "red") }

        val cancel = Button("Cancel") { dialog.close() }

        dialog.footer.add(HorizontalLayout(confirm, cancel))
        dialog.open()
    }

}
