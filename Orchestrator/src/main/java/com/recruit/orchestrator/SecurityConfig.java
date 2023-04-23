package com.recruit.orchestrator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    protected SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        http
                .cors().and()
                .csrf().disable()
                .httpBasic().and()
                .logout().disable();

        return http.build();
    }

}
