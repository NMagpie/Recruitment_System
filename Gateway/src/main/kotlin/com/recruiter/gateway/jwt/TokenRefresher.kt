package com.recruiter.gateway.jwt

import com.recruiter.gateway.GatewayConfiguration
import com.recruiter.gateway.HeaderGatewayFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import kotlin.time.times

@Component
class WebClientInitializer {

    @Value("\${eureka-server-url}")
    lateinit var eurekaServerUrl : String

    @Value("\${spring.application.name}")
    lateinit var serviceName: String

    @Value("\${service-secret-key}")
    lateinit var serviceSharedSecret: String

    @Autowired
    lateinit var webClient: WebClient

    @Autowired
    lateinit var discoveryClient: DiscoveryClient

    @Scheduled(fixedRate = 14 * 60 * 1000)
    fun refreshToken() {

        val request = TokenRefreshRequest(serviceSharedSecret, serviceName)

        webClient
            .post()
            .uri("$eurekaServerUrl/refresh_token")
            .bodyValue(request)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(String::class.java)
            .subscribe { responseBody ->

                HeaderGatewayFilter.token = responseBody
            }
    }

}

data class TokenRefreshRequest(val secretKey: String, val serviceName: String)