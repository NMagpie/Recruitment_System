package com.rectruit.authentication.http;

import com.rectruit.authentication.database.User;
import com.rectruit.authentication.database.UserRepository;
import com.rectruit.authentication.dtos.JwtResponseDTO;
import com.rectruit.authentication.dtos.LoginDTO;
import com.rectruit.authentication.jwt.JwtUtils;
import com.rectruit.authentication.saga_pattern.TransactionLog;
import com.rectruit.authentication.saga_pattern.TransactionService;
import com.rectruit.authentication.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.bson.Document;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager customAuthenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginDTO loginRequest) {

        Authentication authentication = customAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponseDTO(jwt, userDetails.getUsername(), roles));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {

        if (user.getRoles().contains("ADMIN"))
            return ResponseEntity.badRequest()
                    .body("Error: cannot create such user.");

        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        MappingMongoConverter converter = (MappingMongoConverter) mongoTemplate.getConverter();

        Document document = new Document();

        converter.write(user, document);

        TransactionLog transactionLog = transactionService.prepare_document(document, TransactionLog.Action.CREATE, "users");

        String transaction_id = transactionLog.get_id().toString();

        String document_id = transactionLog.getDocumentId();

        return ResponseEntity.ok("{\n\"status\": \"success\"," +
                "\n\"transaction_id\": \""+ transaction_id +"\"," +
                "\n\"user_id\": \""+ document_id +"\"\n}");
    }

    @GetMapping("/login")
    public ResponseEntity<?> logout(@RequestParam(value = "error", required = false) String error,
                                    @RequestParam(value = "logout", required = false) String logout) {
        if (logout != null) {
            return ResponseEntity.ok("Logged out successfully!");
        }

        if (error != null) {
            return ResponseEntity.ok("Error has appeared during logging out process!");
        }

        return ResponseEntity.ok("");
    }
}