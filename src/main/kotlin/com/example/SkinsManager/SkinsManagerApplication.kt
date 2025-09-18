package com.example.SkinsManager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class SkinsManagerApplication

fun main(args: Array<String>) {
	runApplication<SkinsManagerApplication>(*args)
}
