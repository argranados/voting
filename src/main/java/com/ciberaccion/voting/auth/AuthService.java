package com.ciberaccion.voting.auth;

import com.ciberaccion.voting.api.error.BadRequestException;
import com.ciberaccion.voting.domain.AppUser;
import com.ciberaccion.voting.domain.Role;
import com.ciberaccion.voting.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public String register(String username, String password, Role role) {

        if (userRepository.findByUsername(username).isPresent()) {
            throw new BadRequestException("El usuario ya existe: " + username);
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setCreatedAt(Instant.now());

        userRepository.save(user);
        log.info("Usuario registrado: username={}, role={}", username, role);

        return jwtService.generateToken(username, role.name());
    }

    public String login(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Usuario no existe: " + username));

        log.info("Login exitoso: username={}, role={}", username, user.getRole());
        return jwtService.generateToken(username, user.getRole().name());
    }
}