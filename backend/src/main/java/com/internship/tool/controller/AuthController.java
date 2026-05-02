package com.internship.tool.controller;


import com.internship.tool.dto.AuthResponse;
import com.internship.tool.dto.LoginRequest;
import com.internship.tool.dto.RegisterRequest;
import com.internship.tool.entity.User;
import com.internship.tool.service.UserService;
import com.internship.tool.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ─── REGISTER ──────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Register request for: {}", request.getUsername());

        User user = userService.registerUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AuthResponse(
                        token,
                        user.getUsername(),
                        user.getRole(),
                        "Registration successful"
                ));
    }

    // ─── LOGIN ─────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request for: {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
            );

            User user = userService.findByUsername(
                    request.getUsername());

            String token = jwtUtil.generateToken(
                    user.getUsername(),
                    user.getRole()
            );

            return ResponseEntity.ok(new AuthResponse(
                    token,
                    user.getUsername(),
                    user.getRole(),
                    "Login successful"
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(
                            null, null, null,
                            "Invalid username or password"
                    ));
        }
    }

    // ─── REFRESH ───────────────────────────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(
                            null, null, null,
                            "Invalid token"
                    ));
        }

        String oldToken = authHeader.substring(7);
        String username = jwtUtil.extractUsername(oldToken);
        String role = jwtUtil.extractRole(oldToken);

        String newToken = jwtUtil.generateToken(username, role);

        return ResponseEntity.ok(new AuthResponse(
                newToken,
                username,
                role,
                "Token refreshed successfully"
        ));
    }
}