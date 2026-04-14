package ru.practicum.ewm.analyzer.dal.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Действие пользователя с мероприятием.
 * Хранит максимальный вес взаимодействия.
 */
@Entity
@Table(name = "user_action", schema = "stats_analyzer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Double weight;

    @Column(name = "action_timestamp", nullable = false)
    private Instant actionTimestamp;
}
