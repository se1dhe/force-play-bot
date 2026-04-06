# ForcePlay Telegram Bot

Production-ready Telegram bot for ForcePlay.org built as a modular monolith on Java 21 and Spring Boot 3.

## Stack

- Java 21
- Spring Boot 3
- TelegramBots 9.5
- PostgreSQL
- Redis
- Flyway
- Lombok

## Modules inside the monolith

- `handler` for Telegram routing only
- `service` for business logic
- `integration` for Lineage API
- `repository` for persistence
- `scheduler` for cron jobs
- `api` for inbound REST hooks

## Quick start

1. Fill in `.env`.
2. Start the full stack in containers: `docker compose up -d --build`.
3. Bot API app will be available on `http://localhost:8081`.

## Implemented flows

- account link request and confirmation
- HWID approval/deny flow with Redis TTL
- promo generation and redeem
- referral registration via `/start ref_xxx`
- basic admin broadcast
- boss and event polling scheduler

## Configuration

- [messages.yml](/Users/se1dhe/projects/force-play-bot/messages.yml)
- [servers.yml](/Users/se1dhe/projects/force-play-bot/servers.yml)
- [.env](/Users/se1dhe/projects/force-play-bot/.env)

## Testing

Run `mvn test`.
