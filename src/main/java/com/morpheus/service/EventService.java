package com.morpheus.service;

import com.morpheus.dto.EventRequest;
import com.morpheus.dto.EventResponse;
import com.morpheus.model.entity.Event;
import com.morpheus.model.entity.User;
import com.morpheus.repository.EventRepository;
import com.morpheus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

import com.morpheus.exception.UserNotFoundException;
import com.morpheus.exception.EventNotFoundException;
import com.morpheus.exception.UnauthorizedEventAccessException;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public EventResponse createEvent(String email, EventRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado para o e-mail: " + email));

        Event event = mapToEvent(request, user);
        event = eventRepository.save(event);

        return mapToEventResponse(event);
    }

    private Event mapToEvent(EventRequest request, User user) {
        Event event = new Event();
        event.setUser(user);
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setType(request.type());
        event.setScheduledFor(request.scheduledFor());
        event.setNotified(false);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        return event;
    }

    private EventResponse mapToEventResponse(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Evento não pode ser nulo");
        }
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getType(),
                event.getScheduledFor(),
                event.isNotified()
        );
    }

    public List<EventResponse> listEvents(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado para o e-mail: " + email));
        return eventRepository.findByUser(user)
                .stream()
                .map(this::mapToEventResponse)
                .toList();
    }

    public EventResponse updateEvent(Long eventId, String userEmail, EventRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado para o e-mail: " + userEmail));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Evento não encontrado para o id: " + eventId));
        checkUserAuthorization(event, user);
        updateEventFields(event, request);
        log.info("📝 Evento [{}] atualizado pelo usuário [{}]", event.getTitle(), user.getEmail());
        eventRepository.save(event);
        return mapToEventResponse(event);
    }

    private void checkUserAuthorization(Event event, User user) {
        if (!event.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedEventAccessException("Usuário não autorizado a acessar este evento");
        }
    }

    private void updateEventFields(Event event, EventRequest request) {
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setType(request.type());
        event.setScheduledFor(request.scheduledFor());
        event.setUpdatedAt(LocalDateTime.now());
    }

    public void deleteEvent(Long eventId, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);

        Event event = eventRepository.findById(eventId).orElseThrow(EventNotFoundException::new);

        if (!event.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedEventAccessException();
        }

        eventRepository.delete(event);
        log.info("🗑️ Evento [{}] excluído pelo usuário [{}]", event.getTitle(), user.getEmail());
    }

}
