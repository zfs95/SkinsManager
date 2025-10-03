package com.example.SkinsManager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
class SkinsManagerApplication

fun main(args: Array<String>) {
	runApplication<SkinsManagerApplication>(*args)
}
