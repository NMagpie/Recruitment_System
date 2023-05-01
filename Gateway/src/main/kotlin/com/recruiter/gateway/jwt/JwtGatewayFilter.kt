package com.recruiter.gateway.jwt

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import reactor.core.publisher.Mono

@Component
class JwtGatewayFilter(@Value("\${service-jwt-key}") jwtSecret: String) : GatewayFilter, Ordered {

    private var webFilter: WebFilter? = null

    init {
        webFilter = JwtAuthenticationFilter(jwtSecret)
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void>? {
        return webFilter?.filter(exchange) { exchange -> chain.filter(exchange) }
    }

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }
}

