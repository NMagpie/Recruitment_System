package com.rectruit.authentication.jwt.service;

import lombok.Getter;
import lombok.Setter;

public class TokenRefreshRequest {

    @Getter
    @Setter
    private String secretKey;


    @Getter
    @Setter
    private String serviceName;
}