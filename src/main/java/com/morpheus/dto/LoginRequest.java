package com.morpheus.dto;

public record LoginRequest(
        String email,
        String password
) {
}
