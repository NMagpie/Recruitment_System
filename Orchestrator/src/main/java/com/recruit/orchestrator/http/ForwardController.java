package com.recruit.orchestrator.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.orchestrator.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ForwardController {

    @Autowired
    private LoadBalancerClient loadBalancer;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SecurityConfig securityConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // AUTHENTICATION SERVICE

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> user_login(@RequestBody Map<String, Object> user) {
        try {
            ServiceInstance authentication = loadBalancer.choose("AUTHENTICATION");

            String authUrl = authentication.getUri().toString().replace("http://", "https://") + "/auth/login";
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setContentType(MediaType.APPLICATION_JSON);
            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
            HttpEntity<Map<String, Object>> authRequestEntity = new HttpEntity<>(user, authHeaders);
            ResponseEntity<String> authResponseEntity = restTemplate.exchange(authUrl, HttpMethod.POST, authRequestEntity, String.class);

            return Mono.just(authResponseEntity.getBody());
        } catch (Exception e) {
            return Mono.just(e.getMessage());
        }
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> user_logout(@RequestHeader(value = "Authorization") String authorizationHeader) {
        try {
            ServiceInstance authentication = loadBalancer.choose("AUTHENTICATION");

            String authUrl = authentication.getUri().toString().replace("http://", "https://") + "/logout";
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setContentType(MediaType.APPLICATION_JSON);
            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
            authHeaders.set("Authorization", authorizationHeader);
            HttpEntity<String> authRequestEntity = new HttpEntity<>("", authHeaders);
            ResponseEntity<String> authResponseEntity = restTemplate.exchange(authUrl, HttpMethod.POST, authRequestEntity, String.class);

            return Mono.just(objectMapper.writeValueAsString(authResponseEntity.getBody()));
        } catch (Exception e) {
            return Mono.just(e.getMessage());
        }
    }

    @GetMapping(value = "/refresh_token", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> refresh_token(@RequestHeader(value = "Authorization") String authorizationHeader) {
        try {
            ServiceInstance authentication = loadBalancer.choose("AUTHENTICATION");

            String authUrl = authentication.getUri().toString().replace("http://", "https://") + "/refresh_token";
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setContentType(MediaType.APPLICATION_JSON);
            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
            authHeaders.set("Authorization", authorizationHeader);
            HttpEntity<String> authRequestEntity = new HttpEntity<>(null, authHeaders);
            ResponseEntity<HashMap> authResponseEntity = restTemplate.exchange(authUrl, HttpMethod.GET, authRequestEntity, HashMap.class);

            return Mono.just(objectMapper.writeValueAsString(authResponseEntity.getBody()));
        } catch (Exception e) {
            return Mono.just(e.getMessage());
        }
    }

    @GetMapping(value = "/user/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> get_user(@PathVariable("id") String id) {
        try {
            ServiceInstance authentication = loadBalancer.choose("AUTHENTICATION");

            String authUrl = authentication.getUri().toString().replace("http://", "https://") + "/user/" + id;
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setContentType(MediaType.APPLICATION_JSON);
            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
            HttpEntity<String> authRequestEntity = new HttpEntity<>("", authHeaders);
            ResponseEntity<HashMap> authResponseEntity = restTemplate.exchange(authUrl, HttpMethod.GET, authRequestEntity, HashMap.class);

            return Mono.just(objectMapper.writeValueAsString(authResponseEntity.getBody()));
        } catch (Exception e) {
            return Mono.just(e.getMessage());
        }
    }

    //Recommendation

    @GetMapping(value = "/recommendation/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> get_recommendation(@PathVariable("id") String id) {
        try {
            ServiceInstance recommendation = loadBalancer.choose("RECOMMENDATION");

            String recommendUrl = recommendation.getUri().toString().replace("http://", "https://") + "/recommendation/?_id=" + id;
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setContentType(MediaType.APPLICATION_JSON);
            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
            HttpEntity<String> authRequestEntity = new HttpEntity<>("", authHeaders);
            ResponseEntity<HashMap> authResponseEntity = restTemplate.exchange(recommendUrl, HttpMethod.GET, authRequestEntity, HashMap.class);

            return Mono.just(objectMapper.writeValueAsString(authResponseEntity.getBody()));
        } catch (Exception e) {
            return Mono.just(e.getMessage());
        }
    }

}
