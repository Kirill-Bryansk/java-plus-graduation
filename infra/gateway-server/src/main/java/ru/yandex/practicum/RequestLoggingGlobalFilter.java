package ru.yandex.practicum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Глобальный фильтр логирования всех входящих запросов через Gateway.
 * Работает с наивысшим приоритетом (HIGHEST_PRECEDENCE).
 * В DEBUG-режиме логирует метод, путь, query-параметры и IP клиента.
 * В TRACE-режиме дополнительно логирует все заголовки запроса.
 */
@Component
public class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String remoteAddr = request.getRemoteAddress() != null ? request.getRemoteAddress().toString() : "unknown";

        if (log.isDebugEnabled()) {
            log.debug("""
                     Входящий запрос:
                       метод     = {}
                       путь      = {}
                       параметры = {}
                       IP        = {}""",
                    method, path, query != null ? query : "нет", remoteAddr);
        }

        // Логирование заголовков на уровне TRACE смотри logback
        if (log.isTraceEnabled()) {
            log.trace("   Заголовки:");
            request.getHeaders().forEach((name, values) ->
                    log.trace("      {} = {}", name, String.join(", ", values)));
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}