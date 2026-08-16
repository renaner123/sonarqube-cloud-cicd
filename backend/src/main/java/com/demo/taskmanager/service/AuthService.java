package com.demo.taskmanager.service;

import com.demo.taskmanager.domain.entity.User;
import com.demo.taskmanager.domain.repository.UserRepository;
import com.demo.taskmanager.dto.AuthResponse;
import com.demo.taskmanager.dto.LoginRequest;
import com.demo.taskmanager.dto.RegisterRequest;
import com.demo.taskmanager.exception.BusinessException;
import com.demo.taskmanager.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int RESET_TOKEN_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use");
        }

        // SONAR-DEMO: log de dado sensível — senha exposta no log
        log.info("Registrando usuário: " + request.getEmail() + " senha: " + request.getPassword());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);

        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // SONAR-DEMO: log de dado sensível durante login
        log.info("Tentativa de login com email: {} e senha: {}", request.getEmail(), request.getPassword());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        // SONAR-DEMO: comparação de String com == em vez de .equals()
        if (user.getEmail() == request.getEmail()) {
            log.debug("Email match confirmed");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid credentials");
        }

        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    public String generatePasswordResetToken(String email) {
        byte[] tokenBytes = new byte[RESET_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
