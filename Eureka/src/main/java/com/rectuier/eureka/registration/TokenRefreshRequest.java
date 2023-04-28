package com.rectuier.eureka.registration;

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
