package com.morpheus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.morpheus.dto.LoginRequest;
import com.morpheus.dto.RegisterRequest;
import com.morpheus.dto.TokenResponse;
import com.morpheus.service.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean("authenticationService")
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve autenticar usuário e retornar token")
    void login() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user@email.com", "senha123");
        TokenResponse tokenResponse = new TokenResponse("fake-jwt-token");
        Mockito.when(authenticationService.authenticate(any(LoginRequest.class))).thenReturn(tokenResponse);

        org.springframework.test.web.servlet.MvcResult result = mockMvc
                .perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(tokenResponse)))
                .andReturn();
    }

    @Test
    @DisplayName("Deve registrar usuário e retornar mensagem de sucesso")
    void register() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("Usuário Teste", "user@email.com", "senha123");
        Mockito.doNothing().when(authenticationService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(registerRequest))).andExpect(status().isCreated()).andExpect(content().string("Usuário registrado com sucesso!"));
    }
}
