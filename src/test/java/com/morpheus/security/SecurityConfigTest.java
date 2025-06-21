package com.morpheus.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private SecurityConfig securityConfig;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        securityConfig = new SecurityConfig(jwtTokenProvider);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void jwtAuthFilter_validToken_setsAuthentication() throws IOException, ServletException {
        String token = "valid.jwt.token";
        String email = "user@example.com";
        List<String> roles = List.of("USER");

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getSubject(token)).thenReturn(email);
        when(jwtTokenProvider.getRoles(token)).thenReturn(roles);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        OncePerRequestFilter filter = securityConfig.jwtAuthFilter();

        filter.doFilter(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication, "A autenticação não deve ser nula para token válido");
        assertEquals(email, authentication.getPrincipal(), "O email do usuário autenticado está incorreto");
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")), "O usuário autenticado não possui a role esperada");
    }

    @Test
    void jwtAuthFilter_invalidToken_doesNotSetAuthentication() throws IOException, ServletException {
        String token = "invalid.jwt.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        OncePerRequestFilter filter = securityConfig.jwtAuthFilter();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "A autenticação deve ser nula para token inválido");
        verify(jwtTokenProvider).validateToken(token);
        verifyNoMoreInteractions(jwtTokenProvider);
    }

    @Test
    void jwtAuthFilter_noAuthorizationHeader_doesNotSetAuthentication() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        OncePerRequestFilter filter = securityConfig.jwtAuthFilter();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "A autenticação deve ser nula sem header Authorization");
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void jwtAuthFilter_exceptionInFilter_setsUnauthorizedStatus() throws IOException, ServletException {
        String token = "valid.jwt.token";
        when(jwtTokenProvider.validateToken(token)).thenThrow(new RuntimeException("Token error"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        OncePerRequestFilter filter = securityConfig.jwtAuthFilter();

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus(), "O status deve ser 401 em caso de exceção no filtro");
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "A autenticação deve ser nula em caso de exceção");
        verify(jwtTokenProvider).validateToken(token);
        verifyNoMoreInteractions(jwtTokenProvider);
    }
}
