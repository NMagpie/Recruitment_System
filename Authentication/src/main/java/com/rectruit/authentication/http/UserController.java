package com.rectruit.authentication.http;

import com.rectruit.authentication.database.User;
import com.rectruit.authentication.database.UserRepository;
import com.rectruit.authentication.dtos.JwtResponseDTO;
import com.rectruit.authentication.jwt.FilterUtils;
import com.rectruit.authentication.jwt.JwtUtils;
import com.rectruit.authentication.service.UserDetailsServiceImpl;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;

@RestController
public class UserController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserRepository userRepository;

    //@Value("${service-jwt-key}")
    //private String jwtServiceSecret;

    public UserController(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService, UserRepository userRepository, @Value("${service-jwt-key}") String jwtServiceSecret) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        FilterUtils.setJwtUtils(jwtUtils);
        FilterUtils.setUserDetailsService(userDetailsService);
        FilterUtils.setJwtServiceSecret(jwtServiceSecret);
    }

    @GetMapping("/refresh_token")
    public ResponseEntity<?> refreshToken(@RequestHeader(value = "Authorization") String authorizationHeader) {
        String authToken = jwtUtils.parseJwt(authorizationHeader);
        String id = jwtUtils.getIdFromJwtToken(authToken);

        UserDetails userDetails = userDetailsService.loadUserById(id);

        String jwt = jwtUtils.generateJwtToken(id);

        return ResponseEntity.ok(new JwtResponseDTO(
                jwt,
                id,
                userDetails.getUsername(),
                null,
                null,
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        ));
    }

    @GetMapping("/user/{id}")
    //@RolesAllowed({"ADMIN", "USER"})
    public ResponseEntity<?> getUser(@PathVariable("id") String id) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            user.set_id(new ObjectId(id));

            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/user")
    @RolesAllowed("ADMIN")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        userRepository.save(user);

        return ResponseEntity.ok("User created successfully!");
    }

    @PutMapping("/user/{id}")
    //@RolesAllowed("ADMIN")
    public ResponseEntity<?> updateUser(@RequestHeader(value = "Authorization") String authorizationHeader, @PathVariable("id") String id, @RequestBody User user) {

        String authToken = jwtUtils.parseJwt(authorizationHeader);
        String _id = jwtUtils.getIdFromJwtToken(authToken);

        Optional<User> optionalUser = userRepository.findById(_id);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();

            if (!existingUser.get_id().toString().equals(_id))
                return ResponseEntity.badRequest().body("Id and credentials do not coincide.");

            existingUser.setUsername(user.getUsername());
            existingUser.setPassword(user.getPassword());
            userRepository.save(existingUser);

            return ResponseEntity.ok("User updated successfully!");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/user/{id}")
    @RolesAllowed("ADMIN")
    public ResponseEntity<?> deleteUser(@PathVariable("id") String id) {
        userRepository.deleteById(id);

        return ResponseEntity.ok("User deleted successfully!");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authToken = jwtUtils.parseJwt(request);
        jwtUtils.invalidateJwtToken(authToken);

        return ResponseEntity.ok("Logout successful!");
    }
}
