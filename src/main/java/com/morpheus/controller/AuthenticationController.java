package com.morpheus.controller;

import com.morpheus.dto.LoginRequest;
import com.morpheus.dto.RegisterRequest;
import com.morpheus.dto.TokenResponse;
import com.morpheus.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Realiza o login do usuário.
     *
     * @param loginRequest dados de login
     * @return token de autenticação
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest) {
        TokenResponse tokenResponse = authenticationService.authenticate(loginRequest);
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * Realiza o registro de um novo usuário.
     *
     * @param registerRequest dados de registro
     * @return mensagem de sucesso
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        authenticationService.register(registerRequest);
        return ResponseEntity.status(201).body("Usuário registrado com sucesso!");
    }
}
