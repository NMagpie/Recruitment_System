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
    private String userId;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private List<String> roles;

    public JwtResponseDTO(String token, String userId, String username, List<String> roles) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.roles = roles;
    }
}
