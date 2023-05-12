package com.recruiter.gateway

import io.netty.handler.ssl.SslContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
@EnableWebFluxSecurity
class GatewayConfiguration {

    @Autowired
    lateinit var jwtGatewayFilter: GatewayFilter

    @Autowired
    lateinit var sslContext: SslContext

    @Bean
    @LoadBalanced
    fun webClient(): WebClient {

        val httpClient = HttpClient.create()
            .secure { sslSpec -> sslSpec.sslContext(sslContext) }

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }

    @Bean
    fun httpClient(): HttpClient {

        return HttpClient.create().secure { sslContextSpec ->
            sslContextSpec.sslContext(sslContext)
        }

    }

    @Bean
    fun gatewayRoutes(builder: RouteLocatorBuilder): RouteLocator {

        return builder.routes()
            // AUTHENTICATION ROUTES
            .route { r ->
                r.path("/login")
                    .and()
                    .method(HttpMethod.POST)
                    .filters { f ->
                        defaultFilter(f, false)
                        f.rewritePath("/login", "/auth/login")
                    }
                    .uri("lb://AUTHENTICATION")
            }
            .route { r ->
                r.path("/refresh_token")
                    .and()
                    .method(HttpMethod.GET)
                    .filters { f ->
                        defaultFilter(f, true)
                    }
                    .uri("lb://AUTHENTICATION")
            }

            .route { r ->
                r.path("/user/**")
                    .and()
                    .method(HttpMethod.GET)
                    .filters { f ->
                        defaultFilter(f, true)
                    }
                    .uri("lb://AUTHENTICATION")
            }

            // RECOMMENDATION ROUTES

            .route { r ->
                r.path("/recommendation/**")
                    .and()
                    .method(HttpMethod.GET)
                    .filters { f ->
                        defaultFilter(f, true)
                        f.rewritePath("/recommendation/", "/recommendation/?_id=")
                    }
                    .uri("lb://recommendation")
            }

            // CV PROCESSING ROUTES

            .route { r ->
                r.path("/cv/download/**")
                    .and()
                    .method(HttpMethod.GET)
                    .filters { f ->
                        f.rewritePath("http://", "https://")
                        f.filter(HeaderGatewayFilter())
                    }
                    .uri("lb://CV-PROCESSING")
            }

            .route { r ->
                r.path("/cv/**")
                    .and()
                    .method(HttpMethod.GET)
                    .filters { f ->
                        defaultFilter(f, true)
                    }
                    .uri("lb://CV-PROCESSING")
            }

            // JOB POSTING ROUTES

            .route { r ->
                r.path("/job/**")
                    .and()
                    .method(HttpMethod.GET)
                    .filters { f ->
                        defaultFilter(f, true)
                        f.rewritePath("/job/", "/jobs/")
                    }
                    .uri("lb://JOB-POSTING")
            }

            // SAGA ROUTES

            .route { r ->
                r.path("/register")
                    .and()
                    .method(HttpMethod.POST)
                    .filters { f ->
                        defaultFilter(f, false)
                    }
                    .uri("lb://ORCHESTRATOR")
            }

            .route { r ->
                r.path("/upload_cv")
                    .and()
                    .method(HttpMethod.POST)
                    .filters { f ->
                        defaultFilter(f, true)
                    }
                    .uri("lb://ORCHESTRATOR")
            }

            .route { r ->
                r.path("/upload_job")
                    .and()
                    .method(HttpMethod.POST)
                    .filters { f ->
                        defaultFilter(f, true)
                    }
                    .uri("lb://ORCHESTRATOR")
            }

            .route { r ->
                r.path("/search/**")
                    .and()
                    .method(HttpMethod.GET)
                    .filters { f ->
                        defaultFilter(f, true)
                    }
                    .uri("lb://ORCHESTRATOR")
            }

            .route { r ->
                r.path("/delete_cv/**")
                    .and()
                    .method(HttpMethod.DELETE)
                    .filters { f ->
                        defaultFilter(f, true)
                    }
                    .uri("lb://ORCHESTRATOR")
            }

            .route { r ->
                r.path("/delete_job/**")
                    .and()
                    .method(HttpMethod.DELETE)
                    .filters { f ->
                        defaultFilter(f, true)
                    }
                    .uri("lb://ORCHESTRATOR")
            }

            .build()
    }

    fun defaultFilter(f: GatewayFilterSpec, auth: Boolean): GatewayFilterSpec {
        if (auth)
            f.filter(jwtGatewayFilter)
        f.rewritePath("http://", "https://")
        f.addRequestHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        f.filter(HeaderGatewayFilter())
        return f
    }

    @Bean
    fun configure(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.csrf().disable().cors().disable()

        return http.build()
    }
}