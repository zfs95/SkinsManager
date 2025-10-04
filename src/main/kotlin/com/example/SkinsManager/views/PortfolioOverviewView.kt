package com.example.SkinsManager.views

import com.example.SkinsManager.components.PortfolioOverviewCard
import com.example.SkinsManager.components.navbar.DashboardNavbar
import com.example.SkinsManager.dtos.PortfolioProduct
import com.example.SkinsManager.service.ProductService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route

@Route("overview")
class PortfolioOverviewView(
    private val productService: ProductService
) : VerticalLayout() {

    init {
        setSizeFull()
        style.set("background-color", "#121212")
        isPadding = false
        isSpacing = false

        // --- Navbar ---
        val navbar = DashboardNavbar(productService) { /* refresh callback if needed */ }
        add(navbar)

        // --- Content layout ---
        val content = VerticalLayout().apply {
            setWidthFull()
            style.set("padding", "20px")
            style.set("color", "white")
            isSpacing = true
        }

        // --- Status info labels ---
        val latestUpdate = productService.getLastestUpdate()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMM yyyy HH:mm")

        val lastUpdateLabel = Span("Last Update: ${latestUpdate?.updatedAt?.format(formatter) ?: "-"}").apply {
            style.set("color", "#ccc")
        }
        val productsUpdatedLabel = Span("Products Updated: ${latestUpdate?.updatedProducts ?: "-"}").apply {
            style.set("color", "#ccc")
        }
        val productsAddedLabel = Span("Products Added: ${latestUpdate?.addedProducts ?: "-"}").apply {
            style.set("color", "#ccc")
        }

        val statusLayout = HorizontalLayout(
            lastUpdateLabel,
            productsUpdatedLabel,
            productsAddedLabel
        ).apply {
            isSpacing = true
            setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER)
        }

        // --- Back button ---
        val backButton = Button("Back").apply {
            style.set("background-color", "#757575")
            style.set("color", "#fff")
            style.set("border-radius", "5px")
            style.set("padding", "5px 12px")
            addClickListener { ui.ifPresent { it.navigate("") } }
        }

        // --- Header layout: left = back button, right = status ---
        val headerLayout = HorizontalLayout(backButton, statusLayout).apply {
            setWidthFull()
            justifyContentMode = com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN
            alignItems = com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER
            style.set("margin-bottom", "20px")
        }

        content.add(headerLayout)

        // --- Portfolio overview card ---
        val (summary, products) = productService.getPortfolioOverview()
        val overviewCard = PortfolioOverviewCard(summary)
        content.add(overviewCard)

        // --- Portfolio products grid ---
        val grid = Grid<PortfolioProduct>().apply {
            setWidthFull()
            addColumn(PortfolioProduct::productName).setHeader("Product")
            addColumn(PortfolioProduct::totalPaid)
                .setHeader("Paid (€)")
                .setFooter("Total Paid: €${"%.2f".format(summary.totalCost)}")
            addColumn(PortfolioProduct::currentValue)
                .setHeader("Current Suggested Price(€)")
                .setFooter("Total Current Suggested Price: €${"%.2f".format(summary.currentValue)}")
            addComponentColumn { product: PortfolioProduct ->
                Span("€${"%.2f".format(product.profit)}").apply {
                    style.set("color", if (product.profit >= 0) "limegreen" else "red")
                }
            }.setHeader("Profit/Loss")
                .setComparator { a, b -> a.profit.compareTo(b.profit) }
                .setFooter("Total Profit: €${"%.2f".format(summary.profit)}")

            setItems(products)
            addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
        }

        content.add(grid)
        add(content)
    }
}
