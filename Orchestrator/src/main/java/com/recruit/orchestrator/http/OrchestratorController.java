package com.recruit.orchestrator.http;

import com.recruit.orchestrator.services.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@RequestMapping("/auth")
// Define a controller for the Orchestrator service
@RestController
public class OrchestratorController {


    //@Value("#{${service-keys}}")
    //private final Map<String, String> serviceKeys = new HashMap<>();

    private ArrayList<String> serviceTypes = new ArrayList<>(List.of(
            "gateway_api",
            "authentication",
            "cv_processing",
            "job_posting",
            "search",
            "recommendation"
    ));

    private final Map<String, Service> services = new HashMap<>();
    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${service-secret-key}")
    private String sharedSecret;

    @Value("${service-jwt-key}")
    private String jwtSecret;

    @PostMapping("/register")
    public ResponseEntity<String> register(ServerHttpRequest request, @RequestBody Map<String, String> authData) throws JSONException, UnknownHostException {

        String host = request.getHeaders().getHost().getHostName();
        if (host == null || host.isEmpty() || host.equals("unknown")) {
            host = InetAddress.getLocalHost().getHostAddress();
        }

        System.out.println(host);

        String serviceName = authData.get("serviceName");
        String sharedSecretKey = authData.get("sharedSecretKey");
        String port = authData.get("port");

        // Verify the authentication data
        if (!serviceTypes.contains(serviceName) || !sharedSecret.equals(sharedSecretKey)) {
            throw new UnauthorizedException("Invalid authentication data");
        }

        String serviceUUID = UUID.randomUUID().toString();

        // Generate a JWT token
        String token = Jwts.builder()
                .setSubject(serviceName)
                .setId(serviceUUID)
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 60 * 1000)) // 15 minutes
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
                .compact();

        Service service = new Service();

        service.setServiceName(serviceName);
        service.setUUID(serviceUUID);
        service.setToken(token);
        service.setUrl("https://"+host+":"+port);
        service.setStatus("OK");
        service.setLoad(0);

        services.put(serviceUUID, service);

        JSONObject response = new JSONObject();

        response.put("token", token);

        response.put("serviceUUID", serviceUUID);

        return ResponseEntity.ok(response.toString());
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@RequestBody Map<String, String> serviceData,
                                          @RequestHeader(value = "Service-Auth") String authorizationHeader) throws JSONException {
        String serviceName = serviceData.get("serviceName");
        String serviceUUID = serviceData.get("serviceUUID");

        // Verify the existing JWT token
        Service service = services.get(serviceUUID);

        String existingToken = service.getToken();

        if (existingToken == null || !existingToken.equals(authorizationHeader.replace("Bearer ", ""))) {
            throw new UnauthorizedException("Invalid or expired JWT token");
        }

        String newToken = Jwts.builder()
                .setSubject(serviceName)
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 60 * 1000))
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
                .compact();

        service.setToken(newToken);

        services.put(serviceUUID, service);

        JSONObject response = new JSONObject();

        response.put("token", newToken);

        // Return the new token to the service
        return ResponseEntity.ok(response.toString());
    }

    // Exception handler for unauthorized requests
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }
}
