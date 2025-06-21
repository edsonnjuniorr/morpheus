package com.morpheus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.morpheus.dto.EventRequest;
import com.morpheus.dto.EventResponse;
import com.morpheus.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Corrige: Use o nome do bean como String para @MockitoBean
    @org.springframework.test.context.bean.override.mockito.MockitoBean("eventService")
    private EventService eventService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean("authentication")
    private Authentication authentication;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve listar eventos do usuário autenticado")
    void listEvents() throws Exception {
        EventResponse eventResponse = new EventResponse(1L, "Evento Teste", "Descrição teste", null, null, false);
        List<EventResponse> events = Collections.singletonList(eventResponse);
        Mockito.when(authentication.getPrincipal()).thenReturn("user@email.com");
        Mockito.when(eventService.listEvents("user@email.com")).thenReturn(events);

        mockMvc.perform(get("/events").principal(authentication)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve criar evento para usuário autenticado")
    void createEvent() throws Exception {
        EventRequest eventRequest = new EventRequest("Evento Teste", "Descrição teste", null, null);
        EventResponse eventResponse = new EventResponse(1L, "Evento Teste", "Descrição teste", null, null, false);
        Mockito.when(authentication.getPrincipal()).thenReturn("user@email.com");
        Mockito.when(eventService.createEvent(eq("user@email.com"), any(EventRequest.class))).thenReturn(eventResponse);

        mockMvc.perform(post("/events").principal(authentication).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(eventRequest))).andExpect(status().isCreated());
    }
}
