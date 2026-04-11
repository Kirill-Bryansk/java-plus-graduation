package ru.practicum.rating.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.rating.EventSearchByRatingParam;
import ru.practicum.rating.repository.RatingRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingInternalServiceImpl implements RatingInternalService {

    private final RatingRepository ratingRepository;

    @Override
    public List<Long> getMostLikedEventIds(EventSearchByRatingParam param) {
        log.info("Получение популярных событий: limit={}", param.getLimit());
        List<Long> result = ratingRepository.findMostLikedEvents(PageRequest.of(0, param.getLimit()));
        log.info("Найдено {} популярных событий", result.size());
        return result;
    }
}
