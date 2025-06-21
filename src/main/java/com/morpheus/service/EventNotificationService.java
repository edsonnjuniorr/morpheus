package com.morpheus.service;

import com.morpheus.model.entity.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventNotificationService {
    public void notifyEvent(Event event) {
        if (event == null) {
            log.warn("Tentativa de notificar evento nulo.");
            return;
        }
        String userEmail = event.getUser() != null ? event.getUser().getEmail() : "desconhecido";
        log.info("Notificando evento: [{}] para usuário [{}]", event.getTitle(), userEmail);
    }
}
