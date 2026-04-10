package ru.yandex.practicum;

import com.netflix.appinfo.InstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.server.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Слушатель событий Eureka-сервера.
 * <p>
 * Отслеживает ключевые события жизненного цикла сервера и реестра,
 * а также регистрацию/отключение клиентов, выводя информацию в лог.
 */
@Component
public class EurekaRussianEventListener {

    private static final Logger log = LoggerFactory.getLogger(EurekaRussianEventListener.class);

    /**
     * Обрабатывает событие запуска Eureka-сервера.
     *
     * @param event событие {@link EurekaServerStartedEvent}
     */
    @EventListener
    public void onStart(EurekaServerStartedEvent event) {
        log.info("✅ Eureka-сервер запущен");
    }

    /**
     * Обрабатывает событие доступности реестра Eureka для регистрации клиентов.
     *
     * @param event событие {@link EurekaRegistryAvailableEvent}
     */
    @EventListener
    public void onRegistryAvailable(EurekaRegistryAvailableEvent event) {
        log.info("📋 Реестр Eureka доступен для регистрации клиентов");
    }

    /**
     * Обрабатывает событие регистрации нового клиента в Eureka.
     * Логирует имя приложения, хост, порт и идентификатор клиента.
     *
     * @param event событие {@link EurekaInstanceRegisteredEvent}
     */
    @EventListener
    public void onInstanceRegistered(EurekaInstanceRegisteredEvent event) {
        InstanceInfo info = event.getInstanceInfo();
        log.info("🟢 Клиент зарегистрирован: приложение='{}', хост='{}', порт={}, ID='{}'",
                info.getAppName(), info.getHostName(), info.getPort(), info.getId());
    }

    /**
     * Обрабатывает событие отключения (отмены регистрации) клиента.
     * Логирует предупреждение с именем приложения и идентификатором клиента.
     *
     * @param event событие {@link EurekaInstanceCanceledEvent}
     */
    @EventListener
    public void onInstanceCanceled(EurekaInstanceCanceledEvent event) {
        log.warn("🔴 Клиент отключился: приложение='{}', ID='{}'", event.getAppName(), event.getServerId());
    }
}