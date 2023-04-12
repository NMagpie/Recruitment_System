package com.rectruit.authentication.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class JwtResponseDTO {
    @Getter
    @Setter
    private String token;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private List<String> roles;

    public JwtResponseDTO(String token, String username, List<String> roles) {
        this.token = token;
        this.username = username;
        this.roles = roles;
    }
}
