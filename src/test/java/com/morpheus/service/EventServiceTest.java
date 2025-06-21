package com.morpheus.service;

import com.morpheus.dto.EventRequest;
import com.morpheus.dto.EventResponse;
import com.morpheus.exception.EventNotFoundException;
import com.morpheus.exception.UnauthorizedEventAccessException;
import com.morpheus.exception.UserNotFoundException;
import com.morpheus.model.entity.Event;
import com.morpheus.model.entity.User;
import com.morpheus.repository.EventRepository;
import com.morpheus.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @InjectMocks
    private EventService eventService;

    private User user;
    private Event event;
    private EventRequest eventRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@email.com");
        event = new Event();
        event.setId(1L);
        event.setUser(user);
        event.setTitle("Evento Teste");
        event.setDescription("Descrição");
        event.setScheduledFor(LocalDateTime.now());
        event.setNotified(false);
        eventRequest = new EventRequest("Evento Teste", "Descrição", com.morpheus.model.enums.EventType.MEETING, LocalDateTime.now().plusDays(1));
    }

    @Test
    @DisplayName("Deve criar evento com sucesso")
    void createEvent_success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        EventResponse response = eventService.createEvent(user.getEmail(), eventRequest);
        assertNotNull(response);
        verify(userRepository).findByEmail(user.getEmail());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException ao criar evento com usuário inexistente")
    void createEvent_userNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> eventService.createEvent("notfound@email.com", eventRequest));
        verify(userRepository).findByEmail(anyString());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("Deve listar eventos do usuário")
    void listEvents_success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(eventRepository.findByUser(user)).thenReturn(Collections.singletonList(event));
        var responses = eventService.listEvents(user.getEmail());
        assertEquals(1, responses.size());
        assertEquals(event.getId(), responses.get(0).id());
        verify(userRepository).findByEmail(user.getEmail());
        verify(eventRepository).findByUser(user);
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException ao listar eventos de usuário inexistente")
    void listEvents_userNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> eventService.listEvents("notfound@email.com"));
        verify(userRepository).findByEmail(anyString());
        verify(eventRepository, never()).findByUser(any(User.class));
    }

    @Test
    @DisplayName("Deve atualizar evento com sucesso")
    void updateEvent_success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        EventResponse response = eventService.updateEvent(event.getId(), user.getEmail(), eventRequest);
        assertNotNull(response);
        verify(userRepository).findByEmail(user.getEmail());
        verify(eventRepository).findById(event.getId());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException ao atualizar evento com usuário inexistente")
    void updateEvent_userNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> eventService.updateEvent(1L, "notfound@email.com", eventRequest));
        verify(userRepository).findByEmail(anyString());
        verify(eventRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Deve lançar EventNotFoundException ao atualizar evento inexistente")
    void updateEvent_eventNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(EventNotFoundException.class, () -> eventService.updateEvent(99L, user.getEmail(), eventRequest));
        verify(userRepository).findByEmail(user.getEmail());
        verify(eventRepository).findById(anyLong());
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedEventAccessException ao atualizar evento de outro usuário")
    void updateEvent_unauthorized() {
        User otherUser = new User();
        otherUser.setId(2L);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        event.setUser(otherUser);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        assertThrows(UnauthorizedEventAccessException.class, () -> eventService.updateEvent(event.getId(), user.getEmail(), eventRequest));
        verify(userRepository).findByEmail(user.getEmail());
        verify(eventRepository).findById(event.getId());
    }
}
