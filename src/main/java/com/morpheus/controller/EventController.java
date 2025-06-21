package com.morpheus.controller;

import com.morpheus.dto.EventRequest;
import com.morpheus.dto.EventResponse;
import com.morpheus.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventResponse>> listEvents(Authentication authentication) {
        String userEmail = getUserEmail(authentication);
        List<EventResponse> events = eventService.listEvents(userEmail);
        return ResponseEntity.ok(events);
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest eventRequest, Authentication authentication) {
        String userEmail = getUserEmail(authentication);
        EventResponse createdEvent = eventService.createEvent(userEmail, eventRequest);
        return ResponseEntity.status(201).body(createdEvent);
    }

    /**
     * Obtém o e-mail do usuário autenticado.
     *
     * @param authentication informações de autenticação
     * @return e-mail do usuário
     * @throws IllegalArgumentException se a autenticação for inválida
     */
    private String getUserEmail(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("Authentication information is missing");
        }
        return (String) authentication.getPrincipal();
    }
}
