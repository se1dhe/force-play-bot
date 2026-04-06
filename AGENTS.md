# 🧠 AGENTS.md — ForcePlay Telegram Bot (Codex Execution Guide)

## 📌 ОБЩИЕ ПРАВИЛА

Работай СТРОГО по этому файлу и PROJECT.md  
Никакой самодеятельности вне архитектуры.

---

## 🏗 АРХИТЕКТУРА

### Тип:
👉 Модульный монолит (готовый к декомпозиции в микросервисы)

### Модули:

- bot — Telegram обработка
- core — бизнес логика
- integration — API Lineage серверов
- persistence — БД
- scheduler — cron задачи
- admin — админ функции (через Telegram)

---

## 🧱 СТРУКТУРА ПАКЕТОВ
com.forceplay.bot
├── config
├── handler
├── service
├── repository
├── model
├── integration
├── scheduler
├── util
├── api


---

## ⚙️ СТЕК

- Java 21
- Spring Boot 3.x
- TelegramBots 9.5 (rubenlagus)
- PostgreSQL
- Redis (обязательно)
- Lombok

---

## 📦 ОБЯЗАТЕЛЬНЫЕ ПРАВИЛА

### ❗ SOLID
- Никакой логики в handler
- Только routing

### ❗ HEXAGONAL
- Service = бизнес логика
- Integration = внешний API
- Repository = БД

---

## 🧠 DOMAIN MODEL

### User
- id
- telegramId
- language
- createdAt

### Account
- id
- externalAccountId
- serverName
- hwid
- userId

### Character
- id
- name
- accountId

### HWIDRequest
- id
- accountId
- newHwid
- status (PENDING/APPROVED/DENIED)
- expiresAt

### PromoCode
- id
- code
- serverName
- usedBy
- usedAt

### Referral
- id
- userId
- referredUserId

---

## 🌍 CONFIG FILES

### /messages.yml
- все тексты

### /servers.yml

servers:

name: x25_old
baseUrl: https://...
token: xxx

---

## 🔗 INTEGRATION API (Lineage)

### AUTH

Authorization: Bearer <token>


---

### LINK ACCOUNT

POST /api/{server}/account/link/request
POST /api/{server}/account/link/confirm

---

### HWID

POST /api/{server}/hwid/confirm

---

### TRADE KEY

POST /api/{server}/account/tradekey

---

### BONUS

POST /api/{server}/bonus/claim

---

### PROMO

POST /api/{server}/promo/redeem

---

### BOSSES

GET /api/{server}/bosses

---

### EVENTS

GET /api/{server}/events

---

## 🔄 REDIS (ОБЯЗАТЕЛЬНО)

Использовать для:

- HWID queue
- notifications
- rate limit
- idempotency

---

## ⏱ TTL

- HWID confirm: 2 минуты
- default: DENY

---

## 📡 BOT FLOW

### LINK FLOW

1. user вводит ник
2. bot → API request
3. game popup
4. confirm
5. сохранить account + characters

---

### HWID FLOW

1. новый HWID detected
2. API → bot
3. bot → Telegram
4. кнопки approve/deny
5. API confirm

---

## 🎁 BONUS FLOW

- check getChatMember(@forceplay)
- если подписан → reward

---

## 🎟 REFERRAL

- start=ref_xxx
- reward configurable (.env)

---

## 🎯 PROMO

- генерация в боте
- запись в БД
- отправка в API

---

## 📢 BROADCAST

- только ADMIN_IDS
- поддержка:
    - text
    - photo
    - video
    - document

---

## 🧠 MOCK API

ОБЯЗАТЕЛЬНО:

- FakeLineageApiService
- RealLineageApiService

через interface

---

## 🧪 ТЕСТЫ

- unit tests
- mock integration

---

## 🚫 ЗАПРЕЩЕНО

- логика в handler
- прямые вызовы repository из handler
- дублирование кода
- отсутствие интерфейсов

---

## 📌 ПРИОРИТЕТЫ РАЗРАБОТКИ

1. Core domain
2. Integration layer
3. Bot handlers
4. Redis
5. Scheduler
6. Admin функции

---

## 🧠 БУДУЩЕЕ

- переход на microservices
- web admin
- event-driven