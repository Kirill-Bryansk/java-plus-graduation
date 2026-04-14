package ru.practicum.grpc;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.grpc.config.FeignClientConfig;
import ru.practicum.contract.RequestOperations;

@FeignClient(name = "request-service", path = "/internal/requests", configuration = FeignClientConfig.class)
public interface RequestClient extends RequestOperations {
}