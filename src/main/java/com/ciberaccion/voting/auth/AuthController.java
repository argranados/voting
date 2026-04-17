// src/main/java/com/ciberaccion/voting/auth/AuthController.java
package com.ciberaccion.voting.auth;

import com.ciberaccion.voting.domain.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody RegisterRequest request) {
        String token = authService.register(request.username(), request.password(), request.role());
        return Map.of("token", token);
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.username(), request.password());
        return Map.of("token", token);
    }

    // Records como DTOs internos del controller — simples y concisos
    record RegisterRequest(
            @NotBlank String username,
            @NotBlank String password,
            @NotNull Role role
    ) {}

    record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}
}