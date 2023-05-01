package com.recruiter.gateway

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono


@Component
class HeaderGatewayFilter : GatewayFilter, Ordered {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void>? {

        val request = exchange.request.mutate()
            .header("Service-Auth", "Bearer $token").build()

        val mutatedExchange = exchange.mutate().request(request).build()

        return chain.filter(mutatedExchange)
    }

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }

    companion object {
        var token : String = ""
    }
}