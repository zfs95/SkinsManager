package com.example.SkinsManager.components

import com.example.SkinsManager.dtos.PortfolioSummary
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class PortfolioOverviewCard(
    private val portfolioSummary: PortfolioSummary
) : VerticalLayout() {

    init {
        style.set("background-color", "#1e1e1e")
        style.set("padding", "20px")
        style.set("border-radius", "10px")
        style.set("color", "#fff")
        width = "300px"

        val title = H2("Portfolio Overview").apply {
            style.set("color", "white")
        }
        add(title)

        val totalCost = Span("Total Paid: €${"%.2f".format(portfolioSummary.totalCost)}")
        val currentValue = Span("Current Suggested Price Value: €${"%.2f".format(portfolioSummary.currentValue)}")

        val profit = Span("Profit: €${"%.2f".format(portfolioSummary.profit)}").apply {
            if (portfolioSummary.profit >= 0) {
                style.set("color", "limegreen")
            } else {
                style.set("color", "red")
            }
        }

        add(totalCost, currentValue, profit)
    }
}
