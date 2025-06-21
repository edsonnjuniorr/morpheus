package com.morpheus.dto;

import com.morpheus.model.enums.EventType;

import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String title,
        String description,
        EventType type,
        LocalDateTime scheduledFor,
        boolean notified
) {
}
