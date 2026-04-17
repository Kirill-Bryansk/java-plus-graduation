package ru.practicum.grpc;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.grpc.config.FeignClientConfig;
import ru.practicum.contract.EventOperations;

@FeignClient(name = "event-service", path = "/internal/events", configuration = FeignClientConfig.class)
public interface EventClient extends EventOperations {

}