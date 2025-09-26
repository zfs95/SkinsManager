package com.example.SkinsManager.components.navbar

import com.example.SkinsManager.service.ProductService
import com.example.SkinsManager.views.DashboardView
import com.example.SkinsManager.views.PortfolioOverviewView
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.router.RouterLink

class DashboardNavbar(
    private val productService: ProductService,
    private val onProductAdded: () -> Unit
) : HorizontalLayout() {

    private val searchBox = SearchBox(productService)
    private val addButton = AddProductButton(searchBox, productService, onProductAdded)
    private val updateButton = UpdateProductsButton(productService, onProductAdded)
    val homeButton = RouterLink("", DashboardView::class.java).apply {
        element.style.set("margin-right", "20px")
        add(Icon(VaadinIcon.HOME).apply {
            style.set("color", "white")
        })
    }
    val portfolioButton = Button("Portfolio") {
        UI.getCurrent().navigate(PortfolioOverviewView::class.java)
    }.apply {
        style.set("background-color", "limegreen")
        style.set("color", "white")
        style.set("border-radius", "5px")
        style.set("padding", "5px 15px")
        icon = Icon(VaadinIcon.CHART_LINE)
        icon.element.style.set("margin-right", "5px")
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

        add(searchBox, addButton, updateButton,homeButton, portfolioButton)
    }
}