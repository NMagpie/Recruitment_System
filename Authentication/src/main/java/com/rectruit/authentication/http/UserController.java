package com.rectruit.authentication.http;

import com.rectruit.authentication.database.User;
import com.rectruit.authentication.database.UserRepository;
import com.rectruit.authentication.dtos.JwtResponseDTO;
import com.rectruit.authentication.jwt.JwtUtils;
import com.rectruit.authentication.service.UserDetailsServiceImpl;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/refresh_token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String authToken = jwtUtils.parseJwt(request);
        String username = jwtUtils.getUsernameFromJwtToken(authToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                userDetails.getAuthorities()
        );



        String jwt = jwtUtils.generateJwtToken(authentication);

        return ResponseEntity.ok(new JwtResponseDTO(
                jwt,
                userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        ));
    }

    @GetMapping("/user/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    public ResponseEntity<?> getUser(@PathVariable("id") String id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            optionalUser.get().setPassword("");
            return ResponseEntity.ok(optionalUser.get());
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
    @RolesAllowed("ADMIN")
    public ResponseEntity<?> updateUser(@PathVariable("id") String id, @RequestBody User user) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
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
