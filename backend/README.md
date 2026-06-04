# PANSIN ACCESS - Backend

Smart Vault Monitoring System for Bank BJB.

## Stack

- Java 21 LTS, Spring Boot 3.5
- PostgreSQL 16, Redis 7
- Apache Kafka, Eclipse Mosquitto (MQTT 3.1.1)
- WebSocket (STOMP/SockJS)
- MediaMTX integration for live streaming and snapshots
- Spring Security + JWT (access + refresh) + OTP via WhatsApp/Email
- Flyway migrations (manual run, not auto-applied)
- Prometheus / Grafana / Spring Actuator
- Apache POI + OpenPDF for reports

## Project structure

```
backend/
  pom.xml
  Dockerfile
  docker-compose.yml
  docker/
    nginx.conf
    prometheus.yml
  src/main/java/com/bjb/pansin/
    common/                # config, security, exceptions, utils, base entity
    modules/               # auth, user, role, permission, organization, branch,
                           # vault, device, fingerprint, mqtt, websocket,
                           # snapshot, livestream, alarm, audit, heartbeat,
                           # maintenance, monitoring, notification, report
    events/                # cross-module event bridges (WS, MQTT, Kafka)
    PansinApplication.java
  src/main/resources/
    application.yml
    application-dev.yml
    application-prod.yml
    db/migration/V1..V7    # Flyway scripts (do not auto-run)
```

## Quick start (local with Docker Compose)

1. Copy `.env.example` to `.env` and adjust values (especially `JWT_SECRET`).
2. Bring up the infra:
   ```
   docker compose up -d postgres redis mosquitto kafka zookeeper
   ```
3. Build the API image:
   ```
   docker compose build pansin-api
   ```
4. Apply migrations manually (Flyway is intentionally disabled at startup):
   ```
   docker compose run --rm pansin-api java -jar app.jar \
     --spring.flyway.enabled=true --spring.flyway.baseline-on-migrate=true
   ```
   or run them with `psql` directly from `src/main/resources/db/migration`.
5. Start everything:
   ```
   docker compose up -d
   ```
6. The API listens on `http://localhost:8080` (or via Nginx on `:80`).

## Run from source (dev)

```
mvn -B -ntp spring-boot:run
```

Default profile is `dev`. Override with `SPRING_PROFILES_ACTIVE=prod`.

## Default credentials (seeded on first boot)

- `superadmin` / `Pansin@2024!`

Change immediately after first login. Disable seeding with `app.seed.enabled=false`.

## Key endpoints

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/otp/request`
- `POST /api/v1/auth/otp/verify`
- `POST /api/v1/auth/logout`
- `GET  /api/v1/users` and friends
- `POST /api/v1/vaults/{id}/open` / `close`
- `GET  /api/v1/reports/access-log?format=pdf|excel|csv`
- WebSocket: `/ws` (STOMP topics: `/topic/vault`, `/topic/device`, `/topic/alarm`, `/topic/livestream`)
- MQTT broker: `tcp://emqx:1883`, topics: `vault/open`, `vault/close`, `vault/alarm`, `fingerprint/scan`, `device/heartbeat`, ...
- Swagger: `http://localhost:8080/swagger-ui.html`
- Prometheus metrics: `http://localhost:8080/actuator/prometheus`

## Notes

- Flyway is configured but `spring.flyway.enabled` defaults to `false` to satisfy the no-auto-migration requirement. Run migrations explicitly.
- All entities extend `BaseEntity` (UUID PK, created/updated/deleted/by columns).
- Refresh token rotation, access token blacklist, login-attempt lockouts, and OTP cooldown are enforced via Redis.
- Vault sessions exceeding `app.vault.session.max-duration-seconds` automatically raise an `AlarmTriggeredEvent` (SESSION_TIMEOUT) which fans out to WebSocket, MQTT, and Kafka.
