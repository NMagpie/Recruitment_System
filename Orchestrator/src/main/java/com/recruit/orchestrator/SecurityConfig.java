package com.recruit.orchestrator;

import com.recruit.orchestrator.http.TokenRefreshRequest;
import com.recruit.orchestrator.ssl.SslContextInitializer;
import lombok.Getter;
import lombok.Setter;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SslContextInitializer sslContextInitializer;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${service-secret-key}")
    private String serviceSharedSecret;

    @Value("${eureka-server-url}")
    private String eurekaServerUrl;

    @Getter
    @Setter
    private String token;

    @Getter
    @Setter
    private HttpHeaders headers = new HttpHeaders();

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .authorizeHttpRequests().requestMatchers("/**").permitAll();

        return http.build();
    }

    @Scheduled(fixedRate = 14 * 60 * 1000)
    public void refreshToken() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setSecretKey(serviceSharedSecret);
        request.setServiceName(serviceName);

        HttpEntity<TokenRefreshRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> responseEntity = restTemplate().exchange(
                    eurekaServerUrl + "/refresh_token",
                    HttpMethod.POST,
                    entity,
                    String.class);

        this.token = responseEntity.getBody();

        this.headers.set("Service-Auth", "Bearer " + responseEntity.getBody());
    }


    @Bean
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
