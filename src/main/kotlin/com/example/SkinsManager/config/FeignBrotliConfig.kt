package com.example.SkinsManager.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Client
import feign.codec.Decoder
import feign.jackson.JacksonDecoder
import feign.okhttp.OkHttpClient as FeignOkHttpClient
import okhttp3.OkHttpClient
import okhttp3.brotli.BrotliInterceptor
import okhttp3.logging.HttpLoggingInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FeignBrotliConfig {

    @Bean
    fun feignOkHttpClient(): Client {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(BrotliInterceptor)   // fixed: removed ()
            .addInterceptor(logging)
            .build()

        return FeignOkHttpClient(okHttpClient)
    }


    @Bean
    fun objectMapper(): ObjectMapper {
        return jacksonObjectMapper() // Kotlin support included
    }

    @Bean
    fun feignDecoder(objectMapper: ObjectMapper): Decoder {
        return JacksonDecoder(objectMapper)
    }
}
