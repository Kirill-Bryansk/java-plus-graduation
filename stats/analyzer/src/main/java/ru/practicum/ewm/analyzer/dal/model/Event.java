package ru.practicum.ewm.analyzer.dal.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Метаданные события (категория, дата проведения).
 * Заполняется из сообщений EventAvro.
 */
@Entity
@Table(name = "event", schema = "stats_analyzer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
}
