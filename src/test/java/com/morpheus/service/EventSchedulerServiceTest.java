package com.morpheus.service;

import com.morpheus.exception.RecoverableEventException;
import com.morpheus.model.entity.Event;
import com.morpheus.model.entity.User;
import com.morpheus.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class EventSchedulerServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventNotificationService eventNotificationService;

    @InjectMocks
    private EventSchedulerService eventSchedulerService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    void checkScheduledEvents_schedulerDisabled_doesNotQueryRepository() throws Exception {
        Field field = EventSchedulerService.class.getDeclaredField("schedulerEnabled");
        field.setAccessible(true);
        field.set(eventSchedulerService, false);
        eventSchedulerService.checkScheduledEvents();
        verify(eventRepository, never()).findByNotifiedFalseAndScheduledForBefore(any());
    }

    @Test
    void checkScheduledEvents_pendingEventsAreNotifiedAndSaved() {
        Event event = mock(Event.class);
        when(eventRepository.findByNotifiedFalseAndScheduledForBefore(any(LocalDateTime.class))).thenReturn(List.of(event));
        when(event.getUser()).thenReturn(null);
        when(event.getTitle()).thenReturn("Título teste");
        when(event.getId()).thenReturn(1L);
        try {
            Field field = EventSchedulerService.class.getDeclaredField("schedulerEnabled");
            field.setAccessible(true);
            field.set(eventSchedulerService, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertDoesNotThrow(() -> eventSchedulerService.checkScheduledEvents());
        verify(event, never()).setNotified(true);
        verify(eventRepository, never()).saveAll(any());
    }

    @Test
    void notifyEvents_emptyList_doesNotSaveAnything() throws Exception {
        Method method = EventSchedulerService.class.getDeclaredMethod("notifyAndMarkEvents", List.class);
        method.setAccessible(true);
        method.invoke(eventSchedulerService, Collections.emptyList());
        verify(eventRepository, never()).saveAll(any());
    }

    @Test
    void checkScheduledEvents_exceptionOnQuery_logsError() {
        when(eventRepository.findByNotifiedFalseAndScheduledForBefore(any(LocalDateTime.class))).thenThrow(new RuntimeException("Database error"));
        eventSchedulerService.checkScheduledEvents();
        verify(eventRepository, never()).saveAll(any());
    }

    @Test
    void notifyEvents_exceptionOnSave_logsError() throws Exception {
        Event event = mock(Event.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getName()).thenReturn("usuário");
        when(event.getUser()).thenReturn(user);
        when(event.getId()).thenReturn(1L);
        when(event.getScheduledFor()).thenReturn(LocalDateTime.now());
        when(event.getTitle()).thenReturn("Evento Teste");
        when(event.getUser()).thenReturn(user);
        when(event.getId()).thenReturn(1L);
        when(event.getScheduledFor()).thenReturn(java.time.LocalDateTime.now());
        when(event.getDescription()).thenReturn("Descrição do evento");
        when(event.getType()).thenReturn(null);
        when(event.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(event.getUpdatedAt()).thenReturn(LocalDateTime.now());
        doNothing().when(eventNotificationService).notifyEvent(event);
        doThrow(new RuntimeException("Error on save")).when(eventRepository).saveAll(any());
        Method method = EventSchedulerService.class.getDeclaredMethod("notifyAndMarkEvents", List.class);
        method.setAccessible(true);
        Exception exception = assertThrows(InvocationTargetException.class, () -> method.invoke(eventSchedulerService, List.of(event)));
        assertTrue(exception.getCause() instanceof RuntimeException);
        verify(eventRepository).saveAll(any());
    }

    @Test
    void notifyEvents_exceptionOnNotifyEvent_logsError() throws Exception {
        Event event = mock(Event.class);
        when(event.getUser()).thenThrow(new RuntimeException("Error accessing user"));
        Method method = EventSchedulerService.class.getDeclaredMethod("notifyAndMarkEvents", List.class);
        method.setAccessible(true);
        method.invoke(eventSchedulerService, List.of(event));
        verify(eventRepository, never()).saveAll(any());
    }

    @Test
    void notifyEvents_nullList_doesNotSaveAnything() throws Exception {
        Method method = EventSchedulerService.class.getDeclaredMethod("notifyAndMarkEvents", List.class);
        method.setAccessible(true);
        method.invoke(eventSchedulerService, (Object) null);
        verify(eventRepository, never()).saveAll(any());
    }

    @Test
    void notifyAndMarkEvents_notifiesAllEventsInParallel_andSaves() throws Exception {
        Event event1 = mock(Event.class);
        Event event2 = mock(Event.class);
        when(event1.getId()).thenReturn(1L);
        when(event1.getScheduledFor()).thenReturn(LocalDateTime.now());
        when(event2.getId()).thenReturn(2L);
        when(event2.getScheduledFor()).thenReturn(LocalDateTime.now());
        List<Event> events = List.of(event1, event2);
        doNothing().when(eventNotificationService).notifyEvent(any(Event.class));
        Method method = EventSchedulerService.class.getDeclaredMethod("notifyAndMarkEvents", List.class);
        method.setAccessible(true);
        int notifiedCount = (int) method.invoke(eventSchedulerService, events);
        verify(eventNotificationService, times(2)).notifyEvent(any(Event.class));
        verify(event1).setNotified(true);
        verify(event2).setNotified(true);
        verify(eventRepository).saveAll(anyList());
        assertEquals(2, notifiedCount);
    }

    @Test
    void notifyAndMarkEvents_eventInconsistente_lancaExcecao() throws Exception {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(null);
        when(event.getScheduledFor()).thenReturn(LocalDateTime.now());
        List<Event> events = List.of(event);
        Method method = EventSchedulerService.class.getDeclaredMethod("notifyAndMarkEvents", List.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(eventSchedulerService, List.of(event)));
        verify(eventNotificationService, never()).notifyEvent(any());
        verify(eventRepository, never()).saveAll(any());
    }

    @Test
    void notifyAndMarkEvents_recuperavelException_naoInterrompeProcessamento() throws Exception {
        Event event1 = mock(Event.class);
        Event event2 = mock(Event.class);
        when(event1.getId()).thenReturn(1L);
        when(event1.getScheduledFor()).thenReturn(LocalDateTime.now());
        when(event2.getId()).thenReturn(2L);
        when(event2.getScheduledFor()).thenReturn(LocalDateTime.now());
        doThrow(new RecoverableEventException("Falha recuperável")).when(eventNotificationService).notifyEvent(event1);
        doNothing().when(eventNotificationService).notifyEvent(event2);
        List<Event> events = List.of(event1, event2);
        Method method = EventSchedulerService.class.getDeclaredMethod("notifyAndMarkEvents", List.class);
        method.setAccessible(true);
        int notifiedCount = (int) method.invoke(eventSchedulerService, events);
        verify(eventNotificationService).notifyEvent(event1);
        verify(eventNotificationService).notifyEvent(event2);
        verify(event2).setNotified(true);
        verify(eventRepository).saveAll(anyList());
        assertEquals(1, notifiedCount);
    }

    @Test
    void notifyAndMarkEvents_erroCritico_naoInterrompeOutros() throws Exception {
        Event event1 = mock(Event.class);
        Event event2 = mock(Event.class);
        when(event1.getId()).thenReturn(1L);
        when(event1.getScheduledFor()).thenReturn(LocalDateTime.now());
        when(event2.getId()).thenReturn(2L);
        when(event2.getScheduledFor()).thenReturn(LocalDateTime.now());
        doThrow(new RuntimeException("Erro crítico")).when(eventNotificationService).notifyEvent(event1);
        doNothing().when(eventNotificationService).notifyEvent(event2);
        List<Event> events = List.of(event1, event2);
        Method method = EventSchedulerService.class.getDeclaredMethod("notifyAndMarkEvents", List.class);
        method.setAccessible(true);
        int notifiedCount = (int) method.invoke(eventSchedulerService, events);
        verify(eventNotificationService).notifyEvent(event1);
        verify(eventNotificationService).notifyEvent(event2);
        verify(event2).setNotified(true);
        verify(eventRepository).saveAll(anyList());
        assertEquals(1, notifiedCount);
    }

    @Test
    void notifyAndMarkEvents_listaVazia_ouNula_naoSalvaNada() throws Exception {
        Method method = EventSchedulerService.class.getDeclaredMethod("notifyAndMarkEvents", List.class);
        method.setAccessible(true);
        int notifiedCount1 = (int) method.invoke(eventSchedulerService, Collections.emptyList());
        int notifiedCount2 = (int) method.invoke(eventSchedulerService, (Object) null);
        verify(eventNotificationService, never()).notifyEvent(any());
        verify(eventRepository, never()).saveAll(any());
        assertEquals(0, notifiedCount1);
        assertEquals(0, notifiedCount2);
    }
}
