package com.example.SkinsManager

import com.example.SkinsManager.dtos.HistoryDto
import com.example.SkinsManager.service.ProductService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/update")
class Controller(private val productService: ProductService) {

    @PostMapping
    fun update(@RequestBody request: List<HistoryDto>): ResponseEntity<Unit> {
        productService.updateHistory(request)
        return ResponseEntity.ok().build()
    }
}