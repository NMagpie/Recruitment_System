package com.rectuier.eureka.registration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class RefreshController {

    @Value("${eureka.server.secret-key}")
    private String secretKey;

    @Value("${eureka.server.secret-jwt-key}")
    private String secretJWTKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    @PostMapping("/refresh_token")
    public ResponseEntity<String> refreshToken(@RequestBody TokenRefreshRequest request) {

        String receivedSecretKey = request.getSecretKey();

        String serviceName = request.getServiceName();

        if (receivedSecretKey.equals(secretKey)) {
            String newToken = Jwts.builder()
                    .setSubject(serviceName)
                    .setExpiration(new Date(System.currentTimeMillis() + expiration * 60 * 1000)) // 15 minutes
                    .signWith(SignatureAlgorithm.HS256, secretJWTKey)
                    .compact();
            return ResponseEntity.ok(newToken);
        } else {
            return ResponseEntity.badRequest().body("Invalid secret key");
        }
    }
}
