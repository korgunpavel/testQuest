package com.korgun.bankservice.controller;

import com.korgun.bankservice.dto.response.AuthResponse;
import com.korgun.bankservice.dto.request.LoginRequest;
import com.korgun.bankservice.dto.request.RegisterRequest;
import com.korgun.bankservice.entity.Role;
import com.korgun.bankservice.entity.RoleName;
import com.korgun.bankservice.entity.User;
import com.korgun.bankservice.repository.RoleRepository;
import com.korgun.bankservice.repository.UserRepository;
import com.korgun.bankservice.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userDetails.getUsername());

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("User with this email already exists");
        }
        Set<Role> userRoles = new HashSet<>();
        for (String roleStr : request.getRoles()) {
            RoleName roleName;
            try {
                roleName = RoleName.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity
                        .badRequest()
                        .body("Invalid role: " + roleStr);
            }

            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            userRoles.add(role);
        }

        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(userRoles)
                .build();

        userRepository.save(newUser);

        String jwt = jwtUtils.generateJwtToken(newUser.getEmail());

        return ResponseEntity.ok(new AuthResponse(jwt));
    }
}
