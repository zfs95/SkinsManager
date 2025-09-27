package com.example.SkinsManager.components.navbar

import com.example.SkinsManager.service.ProductService
import com.example.SkinsManager.views.DashboardView
import com.example.SkinsManager.views.PortfolioOverviewView
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.router.RouterLink
import java.text.NumberFormat
import java.util.*

class DashboardNavbar(
    private val productService: ProductService,
    private val onProductAdded: () -> Unit
) : HorizontalLayout() {

    private val searchBox = SearchBox(productService)
    private val addButton = AddProductButton(searchBox, productService, onProductAdded)
    private val updateButton = UpdateProductsButton(productService, onProductAdded)

    // Home button
    private val homeButton = RouterLink("", DashboardView::class.java).apply {
        element.style.set("margin-right", "20px")
        add(Icon(VaadinIcon.HOME).apply { style.set("color", "white") })
    }

    // Portfolio button
    private val portfolioButton = Button("Portfolio") {
        UI.getCurrent().navigate(PortfolioOverviewView::class.java)
    }.apply {
        style.set("background-color", "limegreen")
        style.set("color", "white")
        style.set("border-radius", "5px")
        style.set("padding", "5px 15px")
        icon = Icon(VaadinIcon.CHART_LINE)
        icon.element.style.set("margin-right", "5px")
    }

    // Fetch portfolio overview
    private val portfolioOverview = productService.getPortfolioOverview().first

    // Format numbers
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)

    // Total paid
    private val paidSpan = Span("Paid: ${currencyFormatter.format(portfolioOverview.totalCost)}").apply {
        style.set("color", "white")
        style.set("font-size", "0.9em")
    }

    // Current stash value
    private val currentSpan = Span("Current market: ${currencyFormatter.format(portfolioOverview.currentValue)}").apply {
        style.set("color", "white")
        style.set("font-size", "0.9em")
    }

    // Profit
    private val profitSpan = Span("Profit: ${currencyFormatter.format(portfolioOverview.profit)}").apply {
        style.set("font-size", "0.9em")
        style.set("color", if (portfolioOverview.profit >= 0) "limegreen" else "red")
    }

    init {
        setWidthFull()
        isPadding = true
        isSpacing = true
        alignItems = FlexComponent.Alignment.CENTER
        justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
        style.set("background", "linear-gradient(90deg, #1c1c1c, #111111)")
        style.set("box-shadow", "0 2px 4px rgba(0,0,0,0.5)")
        style.set("padding", "10px 20px")


        // Make the portfolio info spans bold
        paidSpan.style.set("font-weight", "bold")
        currentSpan.style.set("font-weight", "bold")
        // Profit color stays conditional (green/red)
        profitSpan.style.set("font-weight", "bold")

// Left part: search + add + update + spacing + portfolio info
        val leftGroup = HorizontalLayout(searchBox, addButton, updateButton).apply {
            isSpacing = true
            alignItems = FlexComponent.Alignment.CENTER
        }

// Extra spacing wrapper for portfolio info
        val portfolioInfoLayout = HorizontalLayout(paidSpan, currentSpan, profitSpan).apply {
            isSpacing = true
            style.set("margin-left", "20px") // this pushes it away from the buttons
            alignItems = FlexComponent.Alignment.CENTER
        }

// Right part: home + portfolio
        val rightGroup = HorizontalLayout(homeButton, portfolioButton).apply {
            isSpacing = true
            alignItems = FlexComponent.Alignment.CENTER
        }

// Add both groups
        add(leftGroup, portfolioInfoLayout, rightGroup)
        expand(leftGroup) // push the rightGroup to the far right
    }
}
