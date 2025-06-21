package com.morpheus.repository;

import com.morpheus.model.entity.Event;
import com.morpheus.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByUser(User user);

    List<Event> findByUserAndScheduledForBetween(User user, LocalDateTime start, LocalDateTime end);

    List<Event> findByScheduledForBetween(LocalDateTime start, LocalDateTime end);

    List<Event> findByNotifiedFalseAndScheduledForBefore(LocalDateTime time);
}
