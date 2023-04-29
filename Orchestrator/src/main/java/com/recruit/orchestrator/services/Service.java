package com.recruit.orchestrator.services;

import lombok.Getter;
import lombok.Setter;

public class Service {


    @Getter
    @Setter
    String serviceName;

    @Getter
    @Setter
    String UUID;

    @Getter
    @Setter
    String url;

    @Getter
    @Setter
    int load;

    @Getter
    @Setter
    String status;

    @Getter
    @Setter
    String token;

}
