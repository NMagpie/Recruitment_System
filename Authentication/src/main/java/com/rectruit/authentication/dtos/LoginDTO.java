package com.rectruit.authentication.dtos;

import lombok.Getter;
import lombok.Setter;

public class LoginDTO {
    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String password;
}