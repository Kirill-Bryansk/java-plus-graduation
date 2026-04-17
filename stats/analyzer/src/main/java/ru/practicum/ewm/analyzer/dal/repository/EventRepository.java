package ru.practicum.ewm.analyzer.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.analyzer.dal.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
