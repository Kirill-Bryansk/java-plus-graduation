# Explore With Me — Микросервисная архитектура

Платформа для поиска и посещения мероприятий. Пользователи могут создавать события, подавать заявки на участие, оценивать мероприятия и просматривать статистику посещений.

##  Содержание

- [Архитектура](#архитектура)
- [Микросервисы](#микросервисы)
- [Инфраструктура](#инфраструктура)
- [Конфигурация](#конфигурация)
- [Запуск проекта](#запуск-проекта)
- [Внутренний API (межсервисное взаимодействие)](#внутренний-api-межсервисное-взаимодействие)
- [Внешний API](#внешний-api)
- [Технологический стек](#технологический-стек)
- [Структура проекта](#структура-проекта)
- [Maven-модули](#maven-модули)

---

## Архитектура

Проект реализован в виде **микросервисной архитектуры** с централизованной инфраструктурой:

```
                        ┌─────────────────┐
                        │   API Gateway   │  ← Единая точка входа (порт 8080)
                        │   (Spring Cloud │
                        │    Gateway)     │
                        └────────┬────────┘
                                 │
                ┌────────────────┼────────────────┐
                │                │                │
        ┌───────▼───────┐ ┌─────▼──────┐ ┌──────▼───────┐
        │ event-service │ │user-service│ │request-svc   │
        │               │ │            │ │              │
        │  Мероприятия  │ │Пользователи│ │   Заявки     │
        │  Категории    │ │            │ │              │
        │  Подборки     │ └────────────┘ └──────────────┘
        └───────┬───────┘                        ▲
                │                                │
        ┌───────▼───────┐                 ┌──────┴────────┐
        │rating-service │ ◄───────────────┤  Feign-клиенты│
        │               │   internal      │  (OpenFeign)  │
        │  Лайки/       │   HTTP/REST     └───────────────┘
        │  Дизлайки     │
        └───────┬───────┘
                │
        ┌───────▼───────┐      ┌──────────────────┐
        │ stats-server  │      │ Discovery Server │
        │               │      │    (Eureka)      │
        │  Статистика   │      │   Порт: 8761     │
        └───────────────┘      └──────────────────┘
                                      ▲
                                      │
                              ┌───────┴────────┐
                              │ Config Server  │
                              │  Порт: 8888    │
                              └────────────────┘
```

### Ключевые принципы

- **API Gateway** — единая точка входа для всех клиентов. Маршрутизирует запросы к нужному микросервису.
- **Service Discovery (Eureka)** — автоматическая регистрация и обнаружение сервисов.
- **Config Server** — централизованное управление конфигурацией всех микросервисов.
- **OpenFeign** — декларативные HTTP-клиенты для межсервисного взаимодействия.
- **Изолированные базы данных** — каждый сервис работает со своей схемой в PostgreSQL.

---

## Микросервисы

### event-service

**Назначение:** Управление мероприятиями, категориями и подборками.

**Функциональность:**
- CRUD мероприятий (создание, редактирование, удаление)
- Публикация/отклонение мероприятий (админ)
- Поиск и фильтрация мероприятий (публичный API)
- Управление категориями
- Управление подборками (компиляциями) событий
- Подсчёт подтверждённых заявок (через `request-service`)
- Подсчёт просмотров (через `stats-server`)
- Динамические запросы через QueryDSL

**База данных:** `ewmdb`, схема `core_events`  
**Таблицы:** `categories`, `events`, `compilations`, `compilations_events`

---

### user-service

**Назначение:** Управление пользователями.

**Функциональность:**
- Регистрация пользователей (админ)
- Просмотр списка пользователей (админ)
- Удаление пользователей (админ)

**База данных:** `ewmdb`, схема `core_users`  
**Таблицы:** `users`

---

### request-service

**Назначение:** Управление заявками на участие в мероприятиях.

**Функциональность:**
- Создание заявки на участие
- Отмена заявки пользователем
- Подтверждение/отклонение заявок (инициатор события)
- Просмотр заявок участника
- Просмотр заявок для события (инициатор)
- Подсчёт подтверждённых заявок (внутренний API для `event-service`)

**База данных:** `ewmdb`, схема `core_requests`  
**Таблицы:** `requests`

---

### rating-service

**Назначение:** Система рейтинга мероприятий (лайки/дизлайки).

**Функциональность:**
- Добавление рейтинга (лайк/дизлайк)
- Обновление рейтинга
- Удаление рейтинга
- Поиск мероприятий по рейтингу (внутренний API для `event-service`)

**База данных:** `ewmdb`, схема `core_rating`  
**Таблицы:** `ratings`

---

### stats-server

**Назначение:** Сбор и предоставление статистики посещений.

**Функциональность:**
- Фиксация обращений к эндпоинтам (`POST /hit`)
- Получение статистики по URI (`GET /stats`)
- Фильтрация по уникальным IP

**База данных:** `stats` (отдельная БД), схема `public`  
**Таблицы:** `stats`

---

## Инфраструктура

### gateway-server (порт 8080)

**API Gateway** на базе Spring Cloud Gateway. Единая точка входа для всех клиентов.

**Маршрутизация:**

| Префикс пути | Микросервис | Описание |
|---|---|---|
| `/admin/events/**` | event-service | Администрирование мероприятий |
| `/users/*/events/**` | event-service | Приватные мероприятия пользователя |
| `/events/**` | event-service | Публичный поиск мероприятий |
| `/admin/categories/**` | event-service | Администрирование категорий |
| `/categories/**` | event-service | Публичные категории |
| `/admin/compilations/**` | event-service | Администрирование подборок |
| `/compilations/**` | event-service | Публичные подборки |
| `/admin/users/**` | user-service | Администрирование пользователей |
| `/users/*/requests/**` | request-service | Заявки пользователя |
| `/users/*/events/*/requests/**` | request-service | Заявки на событие |
| `/users/*/events/*/ratings/**` | rating-service | Рейтинги мероприятий |
| `/stats/**`, `/hit/**` | stats-server | Статистика посещений |

---

### discovery-server (порт 8761)

**Eureka Server** — сервис обнаружения. Все микросервисы регистрируются при запуске и находят друг друга через Eureka.

- URL панели: `http://localhost:8761`
- Регистрация через `spring-cloud-starter-netflix-eureka-client`

---

### config-server (порт 8888)

**Config Server** — централизованная конфигурация. Хранит настройки всех микросервисов в файловой системе (`classpath:config/`).

**Структура конфигураций:**

```
infra/config-server/src/main/resources/config/
├── core/
│   ├── event-service/application.yaml
│   ├── user-service/application.yaml
│   ├── request-service/application.yaml
│   └── rating-service/application.yaml
├── stats/
│   └── stats-server/application.yaml
└── infra/
    └── gateway-server/application.yaml
```

Каждый микросервис при запуске подключается к Config Server через Eureka и загружает свою конфигурацию.

---

## Конфигурация

### Базы данных (Docker Compose)

| Сервис | Контейнер | Порт | БД | Пользователь |
|---|---|---|---|---|
| ewm-db | `ewm-db` | `6542:5432` | `ewmdb` | `user/password` |
| stats-db | `stats-db` | `6541:5432` | `stats` | `user/password` |

Оба сервиса на PostgreSQL 16.1 с healthcheck.

### Схемы базы данных ewmdb

| Схема | Микросервис | Таблицы |
|---|---|---|
| `core_events` | event-service | `categories`, `events`, `compilations`, `compilations_events` |
| `core_users` | user-service | `users` |
| `core_requests` | request-service | `requests` |
| `core_rating` | rating-service | `ratings` |

### Профили запуска

Каждый микросервис использует:
- `spring.config.import: configserver:` — импорт конфигурации из Config Server
- `spring.cloud.config.discovery.enabled: true` — обнаружение Config Server через Eureka
- `spring.cloud.config.fail-fast: true` + retry — устойчивый запуск
- `eureka.client.serviceUrl.defaultZone: http://localhost:8761/eureka/`

---

## Запуск проекта

### 1. Запуск баз данных

```bash
docker-compose up -d
```

### 2. Запуск инфраструктуры (последовательно)

```bash
# 1. Discovery Server (Eureka)
cd infra/discovery-server && mvn spring-boot:run

# 2. Config Server
cd infra/config-server && mvn spring-boot:run

# 3. Gateway Server
cd infra/gateway-server && mvn spring-boot:run
```

### 3. Запуск микросервисов

```bash
# Stats Server
cd stats/stats-server && mvn spring-boot:run

# Core микросервисы (в любом порядке)
cd core/event-service && mvn spring-boot:run
cd core/user-service && mvn spring-boot:run
cd core/request-service && mvn spring-boot:run
cd core/rating-service && mvn spring-boot:run
```

### 4. Проверка

- **Eureka Dashboard:** http://localhost:8761 — все сервисы должны быть в статусе `UP`
- **API Gateway:** http://localhost:8080 — единая точка входа для всех запросов
- **Swagger UI:** http://localhost:8080/swagger-ui.html (если подключён springdoc)

### 5. Тестирование

Запустите Postman-коллекцию из группового проекта на `http://localhost:8080`.

---

## Внутренний API (межсервисное взаимодействие)

Микросервисы взаимодействуют через **Feign-клиенты** (HTTP/REST). Контракты определены в модулях `interaction-api`.

### event-service → user-service (`UserClient`)

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/internal/users/{id}` | Получить пользователя по ID (`UserShortDto`) |
| `POST` | `/internal/users` | Пакетное получение пользователей по IDs (`Map<Long, UserShortDto>`) |
| `GET` | `/internal/users/check/{id}` | Проверить существование пользователя |

### event-service → request-service (`RequestClient`)

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/internal/requests/count/{eventId}` | Количество подтверждённых заявок для события |
| `POST` | `/internal/requests/count` | Пакетный подсчёт заявок для списка событий (`Map<Long, Integer>`) |

### event-service → rating-service (`RatingClient`)

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/internal/rating` | ID самых лайкнутых событий (`List<Long>`, параметр: `limit`) |

### event-service → event-service (внутренние)

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/internal/events/{id}` | Получить событие для внутреннего использования (`EventForRequestDto`) |
| `GET` | `/internal/events/check/user/{userId}/event/{eventId}` | Проверить, что пользователь — инициатор события |
| `GET` | `/internal/events/exists/{id}` | Проверить существование события |

### Общая ошибка

Все Feign-клиенты используют `CustomErrorDecoder`, который преобразует HTTP-ошибки в исключения:

| HTTP-код | Исключение |
|---|---|
| 404 | `NotFoundException` |
| 400 | `ValidationException` |
| Прочие | `RuntimeException` с сообщением из `ApiError` |

### DTO для межсервисного взаимодействия

```java
// UserShortDto — минимальная информация о пользователе
Long id;
String name;

// EventForRequestDto — данные события для обработки заявок
Long id;
Long initiatorId;
Integer participantLimit;
EventState state;       // PENDING, PUBLISHED, CANCELED
Boolean requestModeration;

// EventSearchByRatingParam — параметры поиска по рейтингу
int limit;
```

---

## Внешний API

### Explore With Me API (основной сервис)

- **Спецификация (OpenAPI 3.0.1):** [`ewm-main-service-spec.json`](ewm-main-service-spec.json)
- **Базовый URL:** `http://localhost:8080`
- **Описание:** Полный REST API для управления пользователями, мероприятиями, категориями, подборками и заявками.

### Stats Service API (статистика)

- **Спецификация (OpenAPI 3.0.1):** [`ewm-stats-service-spec.json`](ewm-stats-service-spec.json)
- **Базовый URL:** `http://localhost:9090` (напрямую) или `http://localhost:8080` (через Gateway)
- **Описание:** API для фиксации посещений и получения статистики.

---

## Технологический стек

| Категория | Технологии |
|---|---|
| **Язык** | Java 21 |
| **Фреймворк** | Spring Boot 3.4.3, Spring Cloud 2024.0.1 |
| **Микросервисы** | Spring Cloud Gateway, Eureka, Config Server, OpenFeign, Spring Retry |
| **БД** | PostgreSQL 16.1, H2 (тесты) |
| **ORM** | Spring Data JPA (Hibernate), QueryDSL 5.1.0 |
| **Кодогенерация** | Lombok 1.18.38, MapStruct 1.6.3 |
| **Сборка** | Maven 3.x |
| **Контейнеризация** | Docker Compose |
| **Качество кода** | Checkstyle, SpotBugs, JaCoCo |

---

## Структура проекта

```
explore-with-me/
├── pom.xml                          # Родительский POM (управление зависимостями)
├── docker-compose.yml               # Базы данных (PostgreSQL)
├── checkstyle.xml                   # Правила Checkstyle
│
├── infra/                           # Инфраструктурные сервисы
│   ├── pom.xml
│   ├── discovery-server/            # Eureka Server (порт 8761)
│   ├── config-server/               # Config Server (порт 8888)
│   │   └── src/main/resources/config/  # Конфигурации микросервисов
│   └── gateway-server/              # API Gateway (порт 8080)
│
├── core/                            # Основные микросервисы
│   ├── pom.xml                      # Parent для core-модулей
│   ├── interaction-api/             # Feign-клиенты и DTO для межсервисного общения
│   ├── event-service/               # Мероприятия, категории, подборки
│   ├── user-service/                # Пользователи
│   ├── request-service/             # Заявки на участие
│   └── rating-service/              # Рейтинг (лайки/дизлайки)
│
├── stats/                           # Сервис статистики
│   ├── pom.xml
│   ├── stats-interaction-api/       # DTO и контракты для Stats
│   ├── stats-client/                # Feign-клиент для Stats
│   └── stats-server/                # Сервис сбора статистики
│
├── ewm-main-service-spec.json       # OpenAPI спецификация основного API
└── ewm-stats-service-spec.json      # OpenAPI спецификация API статистики
```

---

## Maven-модули

### Зависимости между модулями

```
explore-with-me (root)
├── stats
│   ├── stats-interaction-api        # Независимый (DTO)
│   ├── stats-client ──────────────► stats-interaction-api
│   └── stats-server ──────────────► stats-interaction-api
│
├── infra
│   ├── discovery-server             # Независимый
│   ├── config-server                # Независимый
│   └── gateway-server               # Независимый
│
└── core
    ├── interaction-api              # Feign-клиенты, зависит от stats-client
    ├── event-service ──────────────► interaction-api, stats-client
    ├── user-service ───────────────► interaction-api
    ├── request-service ────────────► interaction-api
    └── rating-service ─────────────► interaction-api
```

### Управление зависимостями

- **Корневой `pom.xml`** — `dependencyManagement` для всех общих зависимостей (Lombok, MapStruct, QueryDSL, PostgreSQL, Spring Cloud и т.д.)
- **`core/pom.xml`** — `pluginManagement` с конфигурацией аннотационных процессоров (Lombok + MapStruct)
- **`core/event-service/pom.xml`** — дополнительно QueryDSL annotation processor (для генерации Q-классов)

---

## Надёжность и устойчивость

### Обработка сбоев

- **Spring Retry** — автоматические повторные попытки при подключении к Config Server
- **CustomErrorDecoder** — преобразование HTTP-ошибок Feign в бизнес-исключения
- **Fallback через Gateway** — при недоступности микросервиса Gateway возвращает ошибку, но не ломает остальные сервисы

### Изоляция данных

При недоступности `request-service` или `rating-service`, `event-service` может вернуть значения по умолчанию:
- `confirmedRequests = 0` — если сервис заявок недоступен
- Пустой список рейтинга — если сервис рейтинга недоступен

---

## Дополнительная информация

### Кодогенерация

| Инструмент | Назначение |
|---|---|
| **Lombok** | Геттеры, сеттеры, конструкторы, `equals/hashCode` |
| **MapStruct** | Маппинг Entity ↔ DTO |
| **QueryDSL** | Генерация Q-классов для типобезопасных запросов (только `event-service`) |

### Сгенерированные источники

QueryDSL Q-классы генерируются в `target/generated-sources/annotations/`.  
IntelliJ IDEA: `Mark Directory As → Generated Sources Root` или `Maven → Reload Project`.
