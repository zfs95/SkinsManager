package com.example.SkinsManager.views

import com.example.SkinsManager.components.PortfolioOverviewCard
import com.example.SkinsManager.components.navbar.DashboardNavbar
import com.example.SkinsManager.dtos.PortfolioProduct
import com.example.SkinsManager.service.ProductService
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.Span
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

        val (summary, products) = productService.getPortfolioOverview()

        val overviewCard = PortfolioOverviewCard(summary)
        content.add(overviewCard)

        val grid = Grid(PortfolioProduct::class.java).apply {
            setWidthFull()
            addColumn(PortfolioProduct::productName).setHeader("Product")
            addColumn(PortfolioProduct::totalPaid)
                .setHeader("Paid (€)")
                .setFooter("Total: €${"%.2f".format(summary.totalCost)}")
            addColumn(PortfolioProduct::currentValue)
                .setHeader("Current (€)")
                .setFooter("Total: €${"%.2f".format(summary.currentValue)}")
            addComponentColumn { p ->
                val profitSpan = Span("€${"%.2f".format(p.profit)}").apply {
                    style.set("color", if (p.profit >= 0) "limegreen" else "red")
                }
                profitSpan
            }.setHeader("Profit/Loss")
            setItems(products)
            addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
        }

        content.add(grid)
        add(content)
    }
}

