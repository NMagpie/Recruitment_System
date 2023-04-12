package com.rectruit.authentication.http;

import com.rectruit.authentication.database.User;
import com.rectruit.authentication.database.UserRepository;
import com.rectruit.authentication.dtos.JwtResponseDTO;
import com.rectruit.authentication.dtos.LoginDTO;
import com.rectruit.authentication.jwt.JwtUtils;
import com.rectruit.authentication.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully!");
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