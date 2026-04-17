package ru.practicum.ewm.analyzer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.analyzer.service.UserActionProcessor;

/**
 * Запускает UserActionProcessor в отдельном потоке при старте приложения.
 * SimilarityProcessor запускается сам через @PostConstruct.
 */
@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {

    private final UserActionProcessor userActionProcessor;

    @Override
    public void run(String... args) {
        Thread thread = new Thread(userActionProcessor);
        thread.setName("kafka-user-action-thread");
        thread.setDaemon(false);
        thread.start();
    }
}
