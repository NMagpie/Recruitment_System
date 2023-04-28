package com.rectruit.authentication.jwt;

import com.rectruit.authentication.service.UserDetailsServiceImpl;
import lombok.Getter;
import lombok.Setter;

public class FilterUtils {
    @Getter
    @Setter
    private static JwtUtils jwtUtils;

    @Getter
    @Setter
    private static UserDetailsServiceImpl userDetailsService;

    @Getter
    @Setter
    private static String jwtServiceSecret;
}
