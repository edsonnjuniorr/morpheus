package com.morpheus.service;

import com.morpheus.dto.LoginRequest;
import com.morpheus.dto.RegisterRequest;
import com.morpheus.dto.TokenResponse;
import com.morpheus.exception.UserNotFoundException;
import com.morpheus.model.entity.User;
import com.morpheus.repository.UserRepository;
import com.morpheus.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenResponse authenticate(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(UserNotFoundException::new);
        if (!BCrypt.checkpw(loginRequest.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        String token = jwtTokenProvider.generateToken(user.getEmail());
        return new TokenResponse(token);
    }

    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new IllegalArgumentException("Email já cadastrado.");
        }
        User user = new User();
        user.setName(registerRequest.name());
        user.setEmail(registerRequest.email());
        user.setPasswordHash(BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt()));
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest loginRequest) {
        return authenticate(loginRequest);
    }
}
