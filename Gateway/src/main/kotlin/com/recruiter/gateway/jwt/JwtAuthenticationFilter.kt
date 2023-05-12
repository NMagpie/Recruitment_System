package com.recruiter.gateway.jwt

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
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
import java.nio.charset.Charset

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

    private fun validateJwtToken(authToken: String): Pair<Boolean, String> {
        return try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken)
            Pair(true, "")
        } catch (e: MalformedJwtException) {
            Pair(false, "Invalid JWT token: " + e.message)
        } catch (e: ExpiredJwtException) {
            Pair(false, "JWT token is expired: " + e.message)
        } catch (e: UnsupportedJwtException) {
            Pair(false, "JWT token is unsupported: " + e.message)
        } catch (e: IllegalArgumentException) {
            Pair(false, "JWT claims string is empty: " + e.message)
        }
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val token = parseJwt(exchange.request)
        if (token != null) {
            val userId = getUserId(token)

            val validationPair = validateJwtToken(token)

            return if (validationPair.first) {
                val auth: Authentication = UsernamePasswordAuthenticationToken(userId, null, null)
                chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
            } else {
                unauthorized(exchange, validationPair.second)
            }
        }
        return unauthorized(exchange)
    }

    private fun unauthorized(
        exchange: ServerWebExchange,
        errorMessage: String = "Unauthorized Access. Please provide a valid token."
    ): Mono<Void> {
        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        exchange.response.headers.set(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"Unauthorized\", charset=\"UTF-8\"")

        val buffer = exchange.response.bufferFactory().wrap(errorMessage.toByteArray(Charset.defaultCharset()))
        return exchange.response.writeWith(Mono.just(buffer))
    }
}