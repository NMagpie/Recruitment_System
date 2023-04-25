package com.rectruit.authentication.jwt.service;

import com.rectruit.authentication.ssl.SslContextInitializer;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.util.HashMap;
import java.util.Map;

@Component
@EnableScheduling
public class JwtServiceUtils {

    @Value("${server.ssl.trust-store}")
    private String trustStore;
    @Value("${server.ssl.trust-store-password}")
    private String trustStorePassword;

    @Autowired
    private SslContextInitializer sslContextInitializer;

    @Value("${service-secret-key}")
    private String serviceSharedSecret;

    @Value("${register-url}")
    private String registerUrl;

    @Value("${refresh-url}")
    private String refreshUrl;

    @Value("${service-name}")
    private String serviceName;

    @Value("${server.port}")
    private String servicePort;

    private String serviceUUID;

    private String token;

    @Bean
    public void initializeToken() throws Exception {
        RestTemplate restTemplate = restTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("serviceName", serviceName);
        requestBody.put("sharedSecretKey", serviceSharedSecret);
        requestBody.put("port", servicePort);


        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(registerUrl, HttpMethod.POST, request, Map.class);
        Map<String, String> responseBody = response.getBody();

        serviceUUID = responseBody.get("serviceUUID");

        token = responseBody.get("token");

    }

    @Scheduled(fixedRate = 14 * 60 * 1000, initialDelay = 14 * 60 * 1000)
    public void refreshToken() throws Exception {
        RestTemplate restTemplate = restTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add("Service-Auth", "Bearer " + token);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("serviceName", serviceName);
        requestBody.put("serviceUUID", serviceUUID);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(refreshUrl, HttpMethod.POST, request, Map.class);
        Map<String, String> responseBody = response.getBody();

        token = responseBody.get("token");
    }

    public RestTemplate restTemplate() throws Exception {
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                sslContextInitializer.initSslContext(),
                (s, sslSession) -> true //REMOVE IF NEED HOSTNAME VERIFICATION
        );

        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(socketFactory).build();

        HttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }

}
