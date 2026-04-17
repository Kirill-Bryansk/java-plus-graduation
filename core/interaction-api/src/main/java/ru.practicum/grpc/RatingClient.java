package ru.practicum.grpc;


import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.grpc.config.FeignClientConfig;
import ru.practicum.contract.RatingOperations;

@FeignClient(name = "rating-service", path = "/internal/rating", configuration = FeignClientConfig.class)
public interface RatingClient extends RatingOperations {
}
