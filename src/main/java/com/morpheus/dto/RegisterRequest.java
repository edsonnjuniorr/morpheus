package com.morpheus.dto;

public record RegisterRequest(
    String name,
    String email,
    String password
) {}

