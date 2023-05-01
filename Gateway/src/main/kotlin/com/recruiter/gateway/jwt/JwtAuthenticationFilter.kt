package com.recruiter.gateway.jwt

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class JwtAuthenticationFilter(private val jwtSecret: String) : WebFilter {

    private fun parseJwt(request: ServerHttpRequest): String? {
        val headerAuth = request.headers.getFirst("Authorization")
        return if (StringUtils.hasText(headerAuth) && headerAuth!!.startsWith("Bearer ")) {
            headerAuth.substring(7)
        } else null
    }

    private fun getUserId(token: String?): String {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).body.subject
    }

    private fun validateJwtToken(authToken: String?): Boolean {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken)
            return true
        } catch (e: MalformedJwtException) {
            println("Invalid JWT token: " + e.message)
        } catch (e: ExpiredJwtException) {
            println("JWT token is expired: " + e.message)
        } catch (e: UnsupportedJwtException) {
            println("JWT token is unsupported: " + e.message)
        } catch (e: IllegalArgumentException) {
            println("JWT claims string is empty: " + e.message)
        }
        return false
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val token = parseJwt(exchange.request)
        if (token != null) {
            val userId = getUserId(token)
            if (validateJwtToken(token)) {
                val auth: Authentication = UsernamePasswordAuthenticationToken(userId, null, null)
                return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
            }
        }
        return unauthorized(exchange)
    }

    private fun unauthorized(exchange: ServerWebExchange): Mono<Void> {
        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        exchange.response.headers.set(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"Unauthorized\", charset=\"UTF-8\"")
        return exchange.response.setComplete()
    }
}