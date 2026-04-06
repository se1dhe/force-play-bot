# 🚀 PROJECT.md — ForcePlay Telegram Bot

## 📌 ОПИСАНИЕ

Telegram бот для проекта ForcePlay.org  
Интеграция с игровыми серверами Lineage 2 (Pain-Team)

---

## 🎯 ЦЕЛЬ

Автоматизация взаимодействия игрока с сервером через Telegram:

- безопасность (HWID)
- удобство (TradeKey, аккаунт)
- вовлечение (рефералка, бонусы)
- информирование (боссы, события)

---

## 🌍 MULTI-SERVER

Поддержка нескольких серверов:

- x25_old
- x50_new
- future servers

---

## 🔐 ОСНОВНЫЕ ФУНКЦИИ

### 1. Привязка аккаунта
Через ник персонажа + подтверждение в игре

---

### 2. HWID защита
- 1 HWID на аккаунт
- новый → Telegram подтверждение

---

### 3. TradeKey
Смена через бота

---

### 4. Бонус за подписку
- канал: @forceplay
- разовый + ежедневный

---

### 5. Рассылка
- админ
- всем пользователям

---

### 6. Реферальная система
- ссылка
- reward configurable

---

### 7. Промокоды
- генерация
- выдача
- уникальность

---

### 8. Autofarm
- статус
- убийца
- revive через мини-игру

---

### 9. Боссы / ивенты
- уведомления

---

### 10. Утилиты
- телепорт в город
- другие команды

---

## 🔌 API

REST + JSON  
Bearer auth

---

## 🧠 ОСОБЕННОСТИ

- Multi-tenant (по server)
- idempotent операции
- rate limiting
- Redis queues

---

## 📦 ХРАНЕНИЕ

### PostgreSQL
- users
- accounts
- characters
- promo
- referrals

### Redis
- временные события
- очереди
- HWID TTL

---

## 🧪 MOCK

Полная mock реализация API для разработки без сервера

---

## 🌐 ЯЗЫКИ

- RU
- UA
- EN

---

## ⚙️ CONFIG

.env:
- BOT_TOKEN
- ADMIN_IDS
- REFERRAL_ENABLED
- REFERRAL_ITEM_ID
- REFERRAL_ITEM_COUNT

---

## 📈 РАСШИРЕНИЕ

- Web admin
- Analytics
- AI antifraud
- Event-driven architecture

---

## 🚀 ГОТОВНОСТЬ

Проект должен быть:

- масштабируемый
- отказоустойчивый
- production-ready