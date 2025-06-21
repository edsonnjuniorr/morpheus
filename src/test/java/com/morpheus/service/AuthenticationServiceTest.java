package com.morpheus.service;

import com.morpheus.dto.LoginRequest;
import com.morpheus.dto.RegisterRequest;
import com.morpheus.dto.TokenResponse;
import com.morpheus.exception.UserNotFoundException;
import com.morpheus.model.entity.User;
import com.morpheus.repository.UserRepository;
import com.morpheus.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;
    private final String rawPassword = "senha123";

    @BeforeEach
    void setUp() {
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        user = new User();
        user.setId(1L);
        user.setEmail("user@email.com");
        user.setPasswordHash(hashedPassword);
        user.setName("User Teste");
        user.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve autenticar e retornar token com sucesso")
    void login_success() {
        LoginRequest loginRequest = new LoginRequest(user.getEmail(), rawPassword);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(user.getEmail())).thenReturn("token123");
        TokenResponse response = authenticationService.login(loginRequest);
        assertNotNull(response);
        assertEquals("token123", response.token());
        verify(userRepository).findByEmail(user.getEmail());
        verify(jwtTokenProvider).generateToken(user.getEmail());
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException se usuário não existir no login")
    void login_userNotFound() {
        LoginRequest loginRequest = new LoginRequest("notfound@email.com", rawPassword);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> authenticationService.login(loginRequest));
        verify(userRepository).findByEmail(anyString());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException se senha estiver incorreta")
    void login_invalidPassword() {
        LoginRequest loginRequest = new LoginRequest(user.getEmail(), "senhaErrada");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class, () -> authenticationService.login(loginRequest));
        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    @DisplayName("Deve registrar usuário com sucesso")
    void register_success() {
        RegisterRequest registerRequest = new RegisterRequest("Novo Usuário", "novo@email.com", "novaSenha");
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        authenticationService.register(registerRequest);
        verify(userRepository).existsByEmail(registerRequest.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException se email já estiver cadastrado")
    void register_emailExists() {
        RegisterRequest registerRequest = new RegisterRequest("Usuário", user.getEmail(), "senha");
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> authenticationService.register(registerRequest));
        verify(userRepository).existsByEmail(registerRequest.email());
        verify(userRepository, never()).save(any(User.class));
    }
}