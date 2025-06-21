package com.morpheus.service;

import com.morpheus.model.entity.Event;
import com.morpheus.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.RecoverableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSchedulerService {

    private final EventRepository eventRepository;
    private final EventNotificationService eventNotificationService;

    @Value("${morpheus.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Value("${morpheus.scheduler.lookback.minutes:5}")
    private int lookbackMinutes;

    /**
     * Executa a verificação de eventos agendados a cada minuto, se habilitado.
     * Busca eventos pendentes, notifica e marca como notificados.
     * Em caso de erro, loga informações detalhadas para diagnóstico.
     *
     * @throws RuntimeException se ocorrer erro crítico durante a verificação
     */
    @Scheduled(cron = "${morpheus.scheduler.cron:0 * * * * *}")
    public void checkScheduledEvents() {
        if (!schedulerEnabled) {
            log.debug("Scheduler desabilitado por configuração.");
            return;
        }

        final LocalDateTime now = LocalDateTime.now();
        try {
            final List<Event> pendingEvents = eventRepository.findByNotifiedFalseAndScheduledForBefore(now);
            log.info("Verificando eventos agendados até {}. Encontrados: {}", now, pendingEvents.size());
            int notifiedCount = notifyAndMarkEvents(pendingEvents);
            log.info("Total de eventos notificados nesta execução: {}", notifiedCount);
        } catch (Exception e) {
            log.error("Erro ao verificar eventos agendados: {}", e.getMessage(), e);
            throw new RuntimeException("Erro crítico ao processar eventos agendados", e);
        }
    }

    /**
     * Notifica eventos pendentes e marca como notificados de forma atômica.
     * Em caso de erro individual, loga e continua o processamento dos demais.
     *
     * @param events lista de eventos a notificar
     * @return quantidade de eventos notificados com sucesso
     * @throws IllegalArgumentException se algum evento estiver inconsistente
     * @throws RuntimeException         se ocorrer erro crítico ao salvar
     */
    @Transactional
    private int notifyAndMarkEvents(List<Event> events) {
        if (events == null || events.isEmpty()) {
            log.debug("Nenhum evento pendente para notificar.");
            return 0;
        }
        List<Event> notifiedEvents = new ArrayList<>();
        events.parallelStream().forEach(event -> {
            try {
                if (event == null || event.getId() == null || event.getScheduledFor() == null) {
                    log.warn("Evento inconsistente encontrado e ignorado: {}", event);
                    return;
                }
                eventNotificationService.notifyEvent(event);
                event.setNotified(true);
                synchronized (notifiedEvents) {
                    notifiedEvents.add(event);
                }
                log.debug("Evento [{}] notificado com sucesso.", event.getId());
            } catch (RecoverableException e) {
                log.warn("Erro recuperável ao notificar evento [{}]: {}", event != null ? event.getId() : null, e.getMessage());
            } catch (IllegalArgumentException e) {
                log.warn("Evento inconsistente encontrado e ignorado: {}", event);
            } catch (Exception e) {
                log.error("Erro ao notificar evento [{}]: {}", event != null ? event.getId() : null, e.getMessage(), e);
            }
        });
        if (!notifiedEvents.isEmpty()) {
            try {
                eventRepository.saveAll(notifiedEvents);
                log.info("Eventos notificados salvos com sucesso. Total: {}", notifiedEvents.size());
            } catch (Exception e) {
                log.error("Erro ao salvar eventos notificados: {}", e.getMessage(), e);
                throw new RuntimeException("Erro ao salvar eventos notificados", e);
            }
        } else {
            log.debug("Nenhum evento foi notificado, nada a salvar.");
        }
        return notifiedEvents.size();
    }
}
