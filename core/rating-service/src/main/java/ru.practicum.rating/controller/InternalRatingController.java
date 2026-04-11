package ru.practicum.rating.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.contract.RatingOperations;
import ru.practicum.dto.rating.EventSearchByRatingParam;
import ru.practicum.rating.service.RatingInternalService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/rating")
public class InternalRatingController implements RatingOperations {

    private final RatingInternalService ratingInternalService;

    @Override
    @GetMapping
    public List<Long> getMostLikedEventIds(@RequestParam EventSearchByRatingParam param) {
        log.debug("GET /internal/rating: запрос популярных событий, limit={}", param.getLimit());
        return ratingInternalService.getMostLikedEventIds(param);
    }
}
