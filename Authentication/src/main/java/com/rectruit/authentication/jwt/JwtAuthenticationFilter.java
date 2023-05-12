package com.rectruit.authentication.jwt;

import com.rectruit.authentication.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(2)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt) && (boolean) request.getAttribute("ServiceAuthenticated")) {
                String id = jwtUtils.getIdFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserById(id);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, "", userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (
                MalformedJwtException e) {
            //System.out.println("Invalid JWT token: " + e.getMessage());
            response.sendError(401, "Invalid JWT token: " + e.getMessage());
        } catch (
                ExpiredJwtException e) {
            //System.out.println("JWT token is expired: " + e.getMessage());
            response.sendError(401, "JWT token is expired: " + e.getMessage());
        } catch (
                UnsupportedJwtException e) {
            //System.out.println("JWT token is unsupported: " + e.getMessage());
            response.sendError(401, "JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            //System.out.println("JWT claims string is empty: " + e.getMessage());
            response.sendError(401, "JWT claims string is empty: " + e.getMessage());
        }
        catch (Exception e) {
            //System.out.println("Cannot set user authentication: " + e.getMessage());
            response.sendError(500, "Cannot set user authentication: " + e.getMessage());
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/");
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }

}

