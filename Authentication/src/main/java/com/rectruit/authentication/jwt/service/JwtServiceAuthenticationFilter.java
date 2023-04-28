package com.rectruit.authentication.jwt.service;

import com.rectruit.authentication.jwt.FilterUtils;
import com.rectruit.authentication.jwt.JwtUtils;
import com.rectruit.authentication.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Order(1)
public class JwtServiceAuthenticationFilter extends OncePerRequestFilter {

    @Value("${service-jwt-key}")
    private String jwtServiceSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        boolean nextFilter = nextFilterPath(request);

        try {
            String jwt = parseJwt(request);
            if (jwt != null && validateJwtToken(jwt) && request.getAttribute("ServiceAuthenticated") == null) {

                if (!nextFilter) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            "orchestrator", "", List.of(new SimpleGrantedAuthority("ROLE_ORCHESTRATOR")));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else
                    request.setAttribute("ServiceAuthenticated", true);

            }
        } catch (Exception e) {
            System.out.println("Cannot set service authentication: " + e.getMessage());
        }

        chain.doFilter(request, response);
    }

    private boolean nextFilterPath(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/user/");
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Service-Auth");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtServiceSecret).parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.out.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string is empty: " + e.getMessage());
        }

        return false;

    }
}