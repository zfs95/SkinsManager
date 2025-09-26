package com.example.SkinsManager.config

import feign.Client
import feign.RequestInterceptor
import feign.RequestTemplate
import feign.okhttp.OkHttpClient
import okhttp3.OkHttpClient as OkHttp3Client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit


class FeignConfig {

    @Bean
    fun feignClient(): Client {
        val okHttpClient = OkHttp3Client.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(okhttp3.brotli.BrotliInterceptor) // handles br + gzip
            .build()
        return OkHttpClient(okHttpClient)
    }

    @Bean
    fun assetClientInterceptor(): RequestInterceptor {
        return RequestInterceptor { template: RequestTemplate ->
            template.header("Host", "skinport.com")
            template.header(
                "User-Agent",
                "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:143.0) Gecko/20100101 Firefox/143.0"
            )
            template.header("Accept", "application/json, text/plain, */*")
            template.header("Accept-Language", "en-US,en;q=0.5")
//            template.header("Accept-Encoding", "gzip, deflate")
            template.header("Referer", "https://skinport.com/item/mp9-goo-well-worn") // can override per request
            template.header("Connection", "keep-alive")
            template.header(
                "Cookie",
                "i18n=en; cf_clearance=1kgzCNUH83POX1Gbu3AWitbfRnAeICju0KfYzigg.eY-1758890675-1.2.1.1-5mPlsA2MvY5BMWGxC.y6DjKimpeUktZjcLac55IwTh.G.VLgJ3HgSoTwxT_rtdn6cQ6cP6lt5gkKq88yib2aMcqOAva0TKLHykm_7L6m.xuFKSNJJuy020lRDm3vzi2_p3oZlEY2MfUsX9BbpS4pmibjyR_fVfDHM8sWm9aBklkOZKzmcHOp3EzZ28TZBWFnB4sHj_Hr0qC9TFJbg3U.W45lVmVVJ5RndiS1vou_uIY; _csrf=u-pIiiQVdRxSLvq_bheuJY5O; __cf_bm=8_jEq31uO22IXOfAxskgij6IsGNuBvI43xEcVXYDYCE-1758890674-1.0.1.1-MO9xLh4aQJ2w4idWgixWDYzrSf22530xHpbVLviYimhf_RgvjcM8UuFgJvPotVpQgIO.7EmpUq9qVtpgqbWkn0hLjLZycam5nnipgOwnj7E"
            )
            template.header("Sec-Fetch-Dest", "empty")
            template.header("Sec-Fetch-Mode", "cors")
            template.header("Sec-Fetch-Site", "same-origin")
            template.header("If-None-Match", "W/\"2a4b1-bM0tJNX+DZ9vgOjD6jja0Yl2620\"")
        }
    }
}