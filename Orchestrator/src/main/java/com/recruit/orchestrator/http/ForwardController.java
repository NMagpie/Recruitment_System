package com.recruit.orchestrator.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.orchestrator.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

//@RestController
//public class ForwardController {
//
//    @Autowired
//    private LoadBalancerClient loadBalancer;
//
//    @Autowired
//    private RestTemplate restTemplate;
//
//    @Autowired
//    private SecurityConfig securityConfig;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    // AUTHENTICATION SERVICE ROUTES
//
//    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<String> user_login(@RequestBody Map<String, Object> user) {
//        try {
//            ServiceInstance authentication = loadBalancer.choose("AUTHENTICATION");
//
//            String authUrl = authentication.getUri().toString().replace("http://", "https://") + "/auth/login";
//            HttpHeaders authHeaders = new HttpHeaders();
//            authHeaders.setContentType(MediaType.APPLICATION_JSON);
//            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
//            HttpEntity<Map<String, Object>> authRequestEntity = new HttpEntity<>(user, authHeaders);
//            ResponseEntity<String> authResponseEntity = restTemplate.exchange(authUrl, HttpMethod.POST, authRequestEntity, String.class);
//
//            return new ResponseEntity<>(authResponseEntity.getBody(), HttpStatus.OK);
//        } catch (HttpClientErrorException | HttpServerErrorException e) {
//            String errorResponse = e.getMessage();
//            HttpStatusCode status = e.getStatusCode();
//            return new ResponseEntity<>(errorResponse, HttpStatusCode.valueOf(status.value()));
//        } catch (Exception e) {
//            String errorResponse = e.getMessage();
//            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
////    @PostMapping(value = "/user_logout", produces = MediaType.APPLICATION_JSON_VALUE)
////    public ResponseEntity<String> user_logout(@RequestHeader(value = "Authorization") String authorizationHeader) {
////        try {
////            ServiceInstance authentication = loadBalancer.choose("AUTHENTICATION");
////
////            String authUrl = authentication.getUri().toString().replace("http://", "https://") + "/logout";
////            HttpHeaders authHeaders = new HttpHeaders();
////            authHeaders.setContentType(MediaType.APPLICATION_JSON);
////            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
////            authHeaders.set("Authorization", authorizationHeader);
////            HttpEntity<String> authRequestEntity = new HttpEntity<>(authHeaders);
////            ResponseEntity<String> authResponseEntity = restTemplate.exchange(authUrl, HttpMethod.POST, authRequestEntity, String.class);
////
////            return new ResponseEntity<>(objectMapper.writeValueAsString(authResponseEntity.getBody()), HttpStatus.OK);
////        } catch (HttpClientErrorException | HttpServerErrorException e) {
////            String errorResponse = e.getMessage();
////            HttpStatusCode status = e.getStatusCode();
////            return new ResponseEntity<>(errorResponse, HttpStatusCode.valueOf(status.value()));
////        } catch (Exception e) {
////            String errorResponse = e.getMessage();
////            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
////        }
////    }
//
//    @GetMapping(value = "/refresh_token", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<String> refresh_token(@RequestHeader(value = "Authorization") String authorizationHeader) {
//        try {
//            ServiceInstance authentication = loadBalancer.choose("AUTHENTICATION");
//
//            String authUrl = authentication.getUri().toString().replace("http://", "https://") + "/refresh_token";
//            HttpHeaders authHeaders = new HttpHeaders();
//            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
//            authHeaders.set("Authorization", authorizationHeader);
//            HttpEntity<String> authRequestEntity = new HttpEntity<>(authHeaders);
//            ResponseEntity<HashMap> authResponseEntity = restTemplate.exchange(authUrl, HttpMethod.GET, authRequestEntity, HashMap.class);
//
//            return new ResponseEntity<>(objectMapper.writeValueAsString(authResponseEntity.getBody()), HttpStatus.OK);
//        } catch (HttpClientErrorException | HttpServerErrorException e) {
//            String errorResponse = e.getMessage();
//            HttpStatusCode status = e.getStatusCode();
//            return new ResponseEntity<>(errorResponse, HttpStatusCode.valueOf(status.value()));
//        } catch (Exception e) {
//            String errorResponse = e.getMessage();
//            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @GetMapping(value = "/user/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<String> get_user(@PathVariable("id") String id) {
//        try {
//            ServiceInstance authentication = loadBalancer.choose("AUTHENTICATION");
//
//            String authUrl = authentication.getUri().toString().replace("http://", "https://") + "/user/" + id;
//            HttpHeaders authHeaders = new HttpHeaders();
//            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
//            HttpEntity<String> authRequestEntity = new HttpEntity<>(authHeaders);
//            ResponseEntity<HashMap> authResponseEntity = restTemplate.exchange(authUrl, HttpMethod.GET, authRequestEntity, HashMap.class);
//
//            return new ResponseEntity<>(objectMapper.writeValueAsString(authResponseEntity.getBody()), HttpStatus.OK);
//        } catch (HttpClientErrorException | HttpServerErrorException e) {
//            String errorResponse = e.getMessage();
//            HttpStatusCode status = e.getStatusCode();
//            return new ResponseEntity<>(errorResponse, HttpStatusCode.valueOf(status.value()));
//        } catch (Exception e) {
//            String errorResponse = e.getMessage();
//            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    //RECOMMENDATION SERVICE ROUTES
//
//    @GetMapping(value = "/recommendation/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<String> get_recommendation(@PathVariable("id") String id) {
//        try {
//            ServiceInstance recommendation = loadBalancer.choose("RECOMMENDATION");
//
//            String recommendUrl = recommendation.getUri().toString().replace("http://", "https://") + "/recommendation/?_id=" + id;
//            HttpHeaders authHeaders = new HttpHeaders();
//            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
//            HttpEntity<String> recRequestEntity = new HttpEntity<>(authHeaders);
//            ResponseEntity<HashMap> recResponseEntity = restTemplate.exchange(recommendUrl, HttpMethod.GET, recRequestEntity, HashMap.class);
//
//            return new ResponseEntity<>(objectMapper.writeValueAsString(recResponseEntity.getBody()), HttpStatus.OK);
//        } catch (HttpClientErrorException | HttpServerErrorException e) {
//            String errorResponse = e.getMessage();
//            HttpStatusCode status = e.getStatusCode();
//            return new ResponseEntity<>(errorResponse, HttpStatusCode.valueOf(status.value()));
//        } catch (Exception e) {
//            String errorResponse = e.getMessage();
//            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    //CV PROCESSING SERVICE
//
//    @GetMapping(value = "/cv/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<String> get_cv(@PathVariable("id") String id) {
//        try {
//            ServiceInstance cv = loadBalancer.choose("CV_PROCESSING");
//
//            String cvUrl = cv.getUri().toString().replace("http://", "https://") + "/cv/info/" + id;
//            HttpHeaders authHeaders = new HttpHeaders();
//            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
//            HttpEntity<String> cvRequestEntity = new HttpEntity<>(authHeaders);
//            ResponseEntity<HashMap> cvResponseEntity = restTemplate.exchange(cvUrl, HttpMethod.GET, cvRequestEntity, HashMap.class);
//
//            return new ResponseEntity<>(objectMapper.writeValueAsString(cvResponseEntity.getBody()), HttpStatus.OK);
//        } catch (HttpClientErrorException | HttpServerErrorException e) {
//            String errorResponse = e.getMessage();
//            HttpStatusCode status = e.getStatusCode();
//            return new ResponseEntity<>(errorResponse, HttpStatusCode.valueOf(status.value()));
//        } catch (Exception e) {
//            String errorResponse = e.getMessage();
//            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @GetMapping(value = "/cv/download/{id}")
//    public ResponseEntity<?> download_cv(@PathVariable("id") String id) {
//        try {
//            ServiceInstance cv = loadBalancer.choose("CV_PROCESSING");
//
//            String cvUrl = cv.getUri().toString().replace("http://", "https://") + "/cv/download/" + id;
//            HttpHeaders authHeaders = new HttpHeaders();
//            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
//            HttpEntity<String> cvRequestEntity = new HttpEntity<>(authHeaders);
//            ResponseEntity<byte[]> cvResponseEntity = restTemplate.exchange(cvUrl, HttpMethod.GET, cvRequestEntity, byte[].class);
//
//            byte[] fileBytes = cvResponseEntity.getBody();
//            HttpHeaders responseHeaders = cvResponseEntity.getHeaders();
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.parseMediaType(responseHeaders.getFirst("Content-Type")));
//            headers.setContentDispositionFormData("attachment", responseHeaders.getFirst("Content-Disposition").substring(responseHeaders.getFirst("Content-Disposition").indexOf("=") + 1));
//
//            return new ResponseEntity<byte[]>(fileBytes, headers, HttpStatus.OK);
//
//        } catch (HttpClientErrorException | HttpServerErrorException e) {
//            String errorResponse = e.getMessage();
//            HttpStatusCode status = e.getStatusCode();
//            return new ResponseEntity<>(errorResponse, HttpStatusCode.valueOf(status.value()));
//        } catch (Exception e) {
//            String errorResponse = e.getMessage();
//            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    //JOB POSTING SERVICE
//
//    @GetMapping(value = "/job/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<String> get_job(@PathVariable("id") String id) {
//        try {
//            ServiceInstance job = loadBalancer.choose("JOB_POSTING");
//
//            String jobUrl = job.getUri().toString().replace("http://", "https://") + "/jobs/" + id;
//            HttpHeaders authHeaders = new HttpHeaders();
//            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
//            HttpEntity<String> jobRequestEntity = new HttpEntity<>(authHeaders);
//            ResponseEntity<HashMap> jobResponseEntity = restTemplate.exchange(jobUrl, HttpMethod.GET, jobRequestEntity, HashMap.class);
//
//            return new ResponseEntity<>(objectMapper.writeValueAsString(jobResponseEntity.getBody()), HttpStatus.OK);
//        } catch (HttpClientErrorException | HttpServerErrorException e) {
//            String errorResponse = e.getMessage();
//            HttpStatusCode status = e.getStatusCode();
//            return new ResponseEntity<>(errorResponse, HttpStatusCode.valueOf(status.value()));
//        } catch (Exception e) {
//            String errorResponse = e.getMessage();
//            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//}
