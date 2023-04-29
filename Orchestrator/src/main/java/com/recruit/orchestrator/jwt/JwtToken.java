package com.recruit.orchestrator.http;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class JwtToken {

    @Getter
    @Setter
    private String token;

    @Getter
    @Setter
    private String refresh_token;

    @Getter
    @Setter
    private Date expiration;
}
