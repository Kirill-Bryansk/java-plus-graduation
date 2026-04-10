package ru.yandex.practicum;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
public class ConfigRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ConfigRequestLoggingFilter.class);
    private static final Pattern CONFIG_PATTERN = Pattern.compile("^/([^/]+)(?:/([^/]+)(?:/([^/]+))?)?$");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        var matcher = CONFIG_PATTERN.matcher(path);
        if (matcher.matches()) {
            // Логирую запросы к Config Server. Паттерн: /{app}/{profile}[/{label}].
            // При отсутствии profile -> "default", label -> "main".
            String application = matcher.group(1);
            String profile = matcher.group(2) != null ? matcher.group(2) : "default";
            String label = matcher.group(3) != null ? matcher.group(3) : "main";

            log.debug("""
                     Запрос конфигурации от клиента:
                       приложение = {}
                       профиль    = {}
                       метка      = {}
                       IP клиента = {}""",
                    application, profile, label, request.getRemoteAddr());
        }
        filterChain.doFilter(request, response);
    }
}