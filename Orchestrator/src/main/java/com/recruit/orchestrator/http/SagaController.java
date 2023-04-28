package com.recruit.orchestrator.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.orchestrator.SecurityConfig;
import com.recruit.orchestrator.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HeaderParam;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class SagaController {
    @Autowired
    private LoadBalancerClient loadBalancer;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private JwtUtils jwtUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> register_user(@RequestBody Map<String, Object> user) {

        HashMap<ServiceInstance, Transaction> affectedServices = new HashMap<>();

        try {

            String userType = (String) user.get("userType");
            String location = (String) user.get("location");

            ServiceInstance authentication = loadBalancer.choose("AUTHENTICATION");
            ServiceInstance recommendation = loadBalancer.choose("RECOMMENDATION");

            // POST REQUEST TO AUTH

            String authUrl = authentication.getUri().toString().replace("http://", "https://") + "/auth/register";
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setContentType(MediaType.APPLICATION_JSON);
            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
            HttpEntity<Map<String, Object>> authRequestEntity = new HttpEntity<>(user, authHeaders);
            ResponseEntity<HashMap> authResponseEntity = restTemplate.exchange(authUrl, HttpMethod.POST, authRequestEntity, HashMap.class);

            affectedServices.put(authentication, new Transaction((String) authResponseEntity.getBody().get("transaction_id")));
            check_status(authResponseEntity.getBody().get("status"));

            String userId = (String) authResponseEntity.getBody().get("user_id");

            String token = (String) authResponseEntity.getBody().get("token");

            // POST REQUEST TO RECOMMEND

            String recUrl = recommendation.getUri().toString().replace("http://", "https://") + "/upload/";
            HttpHeaders recHeaders = new HttpHeaders();
            recHeaders.setContentType(MediaType.APPLICATION_JSON);
            recHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
            recHeaders.set("Authorization", "Bearer " + token);
            Map<String, String> recRequestBody = new HashMap<>();
            recRequestBody.put("_id", userId);
            recRequestBody.put("type", userType);
            recRequestBody.put("location", location);
            HttpEntity<Map<String, String>> recRequestEntity = new HttpEntity<>(recRequestBody, recHeaders);
            ResponseEntity<HashMap> recResponseEntity = restTemplate.exchange(recUrl, HttpMethod.POST, recRequestEntity, HashMap.class);

            affectedServices.put(recommendation, new Transaction((String) recResponseEntity.getBody().get("transaction_id")));
            check_status(recResponseEntity.getBody().get("status"));

            final_changes(affectedServices, true);

            HashMap<String, String> response = new HashMap<>();

            response.put("status", "USER SUCCESSFULLY CREATED!");
            response.put("user_id", userId);
            response.put("jwt", token);


            return Mono.just(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            final_changes(affectedServices, false);
            return Mono.just(e.getMessage());
        }
    }

    @PostMapping(value = "/upload_cv", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> upload_cv(@RequestHeader(value = "Authorization") String authHeader,
                                  @RequestParam("file") MultipartFile file,
                                  @RequestParam("candidate_name") String candidateName,
                                  @RequestParam("user_id") String userId) {

        HashMap<ServiceInstance, Transaction> affectedServices = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ServiceInstance cvProcessing = loadBalancer.choose("CV_PROCESSING");
            ServiceInstance search = loadBalancer.choose("SEARCH");
            ServiceInstance recommendation = loadBalancer.choose("RECOMMENDATION");

            // POST REQUEST TO CV_PROCESSING

            MultiValueMap<String, Object> cv_request = new LinkedMultiValueMap<>();
            cv_request.set("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
            cv_request.set("candidate_name", candidateName);
            cv_request.set("user_id", userId);

            String cvUrl = cvProcessing.getUri().toString().replace("http://", "https://") + "/cv/";

            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
            authHeaders.set("Authorization", authHeader);

            HttpEntity<MultiValueMap<String, Object>> cvRequestEntity = new HttpEntity<>(cv_request, authHeaders);
            ResponseEntity<String> cvResponseEntity = restTemplate.exchange(cvUrl, HttpMethod.POST, cvRequestEntity, String.class);

            Map<String, Object> cvResponseMap = objectMapper.readValue(cvResponseEntity.getBody(), new TypeReference<HashMap<String, Object>>() {});

            affectedServices.put(cvProcessing, new Transaction((String) cvResponseMap.get("transaction_id"), "/cv"));
            check_status(cvResponseMap.get("status"));

            HashMap<String, Object> data = (HashMap) cvResponseMap.get("data");

            // POST REQUEST TO RECOMMEND

            String recUrl = recommendation.getUri().toString().replace("http://", "https://") + "/tags/";
            HttpHeaders recHeaders = new HttpHeaders();
            recHeaders.setContentType(MediaType.APPLICATION_JSON);
            recHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
            recHeaders.set("Authorization", authHeader);

            //Map<String, Object> cvDataMap = objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> recRequest = new HashMap<>();
            recRequest.put("_id", data.get("user_id"));
            recRequest.put("tags", data.get("tags"));

            HttpEntity<Map<String, Object>> recRequestEntity = new HttpEntity<>(recRequest, recHeaders);
            ResponseEntity<HashMap> recResponseEntity = restTemplate.exchange(recUrl, HttpMethod.POST, recRequestEntity, HashMap.class);

            affectedServices.put(recommendation, new Transaction((String) recResponseEntity.getBody().get("transaction_id")));
            check_status(recResponseEntity.getBody().get("status"));

            // POST REQUEST TO SEARCH

            String searchUrl = search.getUri().toString().replace("http://", "https://") + "/cv/";

            HttpEntity<HashMap<String, Object>> searchRequestEntity = new HttpEntity<>(data, recHeaders);
            ResponseEntity<HashMap> searchResponseEntity = restTemplate.exchange(searchUrl, HttpMethod.POST, searchRequestEntity, HashMap.class);

            affectedServices.put(search, new Transaction((String) searchResponseEntity.getBody().get("transaction_id")));
            check_status(recResponseEntity.getBody().get("status"));

            final_changes(affectedServices, true);

            HashMap<String, String> response = new HashMap<>();

            response.put("status", "CV SUCCESSFULLY UPLOADED!");
            response.put("cd_id", (String) data.get("_id"));

            return Mono.just(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            final_changes(affectedServices, false);
            return Mono.just(e.getMessage());
        }
    }

    @PostMapping(value = "/upload_job", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> upload_cv(@RequestHeader(value = "Authorization") String authHeader,
                                  @RequestBody Map<String, Object> job) {

        HashMap<ServiceInstance, Transaction> affectedServices = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ServiceInstance jobPosting = loadBalancer.choose("JOB_POSTING");
            ServiceInstance search = loadBalancer.choose("SEARCH");
            ServiceInstance recommendation = loadBalancer.choose("RECOMMENDATION");

            // POST REQUEST TO JOB_POSTING

            String jobUrl = jobPosting.getUri().toString().replace("http://", "https://") + "/jobs/";

            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setContentType(MediaType.APPLICATION_JSON);
            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
            authHeaders.set("Authorization", authHeader);

            HttpEntity<Map<String, Object>> jobRequestEntity = new HttpEntity<>(job, authHeaders);
            ResponseEntity<String> jobResponseEntity = restTemplate.exchange(jobUrl, HttpMethod.POST, jobRequestEntity, String.class);

            Map<String, Object> cvResponseMap = objectMapper.readValue(jobResponseEntity.getBody(), new TypeReference<HashMap<String, Object>>() {});

            affectedServices.put(jobPosting, new Transaction((String) cvResponseMap.get("transaction_id")));
            check_status(cvResponseMap.get("status"));

            HashMap<String, Object> data = (HashMap) cvResponseMap.get("data");

            // POST REQUEST TO RECOMMEND

            String recUrl = recommendation.getUri().toString().replace("http://", "https://") + "/tags/";

            //Map<String, Object> cvDataMap = objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> recRequest = new HashMap<>();
            recRequest.put("_id", data.get("user_id"));
            recRequest.put("tags", data.get("tags"));

            HttpEntity<Map<String, Object>> recRequestEntity = new HttpEntity<>(recRequest, authHeaders);
            ResponseEntity<HashMap> recResponseEntity = restTemplate.exchange(recUrl, HttpMethod.POST, recRequestEntity, HashMap.class);

            affectedServices.put(recommendation, new Transaction((String) recResponseEntity.getBody().get("transaction_id")));
            check_status(recResponseEntity.getBody().get("status"));

            // POST REQUEST TO SEARCH

            String searchUrl = search.getUri().toString().replace("http://", "https://") + "/job/";

            HttpEntity<HashMap<String, Object>> searchRequestEntity = new HttpEntity<>(data, authHeaders);
            ResponseEntity<HashMap> searchResponseEntity = restTemplate.exchange(searchUrl, HttpMethod.POST, searchRequestEntity, HashMap.class);

            affectedServices.put(search, new Transaction((String) searchResponseEntity.getBody().get("transaction_id")));
            check_status(recResponseEntity.getBody().get("status"));

            final_changes(affectedServices, true);

            HashMap<String, String> response = new HashMap<>();

            response.put("status", "JOB SUCCESSFULLY UPLOADED!");
            response.put("job_id", (String) data.get("_id"));

            return Mono.just(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            final_changes(affectedServices, false);
            return Mono.just(e.getMessage());
        }
    }

    @GetMapping(value = "/search/{prefix}/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> search(HttpServletRequest request, @RequestHeader(value = "Authorization") String authHeader,
                               @RequestParam("q") List<String> queries,
                               @RequestParam("offset") long offset,
                               @PathVariable("prefix") String prefix) {
        if (!prefix.equals("job") && !prefix.equals("cv"))
            return Mono.just("Unknown prefix, choose one of those: job, cv");

        HashMap<ServiceInstance, Transaction> affectedServices = new HashMap<>();

        try {
            ServiceInstance search = loadBalancer.choose("SEARCH");
            ServiceInstance recommendation = loadBalancer.choose("RECOMMENDATION");

            // GET REQUEST TO SEARCH

            String queryString = queries.stream()
                    .map(query -> String.join("_", query.split(" ")))
                    .collect(Collectors.joining("&q="));

            String searchUrl = search.getUri().toString().replace("http://", "https://") +
                    "/search/" + prefix + "/?q=" + queryString + "&offset=" + offset;


            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setContentType(MediaType.APPLICATION_JSON);
            authHeaders.set("Service-Auth", "Bearer " + securityConfig.getToken());
            authHeaders.set("Authorization", authHeader);

            HttpEntity<String> searchRequestEntity = new HttpEntity<>(null, authHeaders);
            ResponseEntity<String> searchResponseEntity = restTemplate.exchange(searchUrl, HttpMethod.GET, searchRequestEntity, String.class);

            // POST REQUEST TO RECOMMENDATION

            String user_id = jwtUtils.getIdFromJwtToken(jwtUtils.parseJwt(request));

            String recUrl = recommendation.getUri().toString().replace("http://", "https://") + "/searches/";

            HashMap<String, Object> recRequestBody = new HashMap<>();
            recRequestBody.put("_id", user_id);
            recRequestBody.put("searches", queries);

            HttpEntity<HashMap> recRequestEntity = new HttpEntity<>(recRequestBody, authHeaders);
            ResponseEntity<HashMap> recResponseEntity = restTemplate.exchange(recUrl, HttpMethod.POST, recRequestEntity, HashMap.class);

            affectedServices.put(recommendation, new Transaction((String) recResponseEntity.getBody().get("transaction_id")));
            check_status(recResponseEntity.getBody().get("status"));

            final_changes(affectedServices, true);
            return Mono.just(searchResponseEntity.getBody());
        } catch (Exception e) {
            final_changes(affectedServices, false);
            return Mono.just(e.getMessage());
        }
    }

    private void final_changes(HashMap<ServiceInstance, Transaction> affectedServices, Boolean commit) {

        String action = (commit) ? "/success/" : "/rollback/";

        affectedServices.forEach( (service, transaction) -> {
            HashMap<String, String> request = new HashMap<>();
            request.put("id", transaction.getId());

            restTemplate.exchange(
                    service.getUri().toString().replace("http://", "https://") + transaction.getPrefix() + action,
                    HttpMethod.POST,
                    new HttpEntity<>(request, securityConfig.getHeaders()),
                    String.class);
        } );
    }

    private void check_status(Object status) throws Exception {
        if (!status.toString().equals("success"))
            throw new Exception("TRANSACTION FAILED, TRY AGAIN LATER");
    }

}

class MultipartInputStreamFileResource extends InputStreamResource {
    private final String filename;
    MultipartInputStreamFileResource(InputStream inputStream, String filename) {
        super(inputStream);
        this.filename = filename;
    }
    @Override
    public String getFilename() {
        return this.filename;
    }
    @Override
    public long contentLength() throws IOException {
        return -1; // we do not want to generally read the whole stream into memory ...
    }
}