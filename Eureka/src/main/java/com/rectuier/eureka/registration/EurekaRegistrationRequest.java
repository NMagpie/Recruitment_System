package com.rectuier.eureka.registration;

import lombok.Getter;
import lombok.Setter;

public class EurekaRegistrationRequest {

    @Getter
    @Setter
    private String secretKey;

    @Getter
    @Setter
    private String appName;

    @Getter
    @Setter
    private String hostName;

    @Getter
    @Setter
    private String hostname;

    @Getter
    @Setter
    private String ipAddress;

    @Getter
    @Setter
    private int port;
}

