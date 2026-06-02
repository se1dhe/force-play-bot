# Game Server API Spec

Этот файл описывает минимальный набор endpoint'ов и обязанностей на стороне game server, чтобы Telegram bot работал строго по текущему ТЗ.

## Общие правила

- Все endpoint'ы game server должны принимать `Authorization: Bearer <token>`.
- Бот выбирает `baseUrl` и path-шаблоны из корневого [servers.yml](/Users/se1dhe/projects/force-play-bot/servers.yml).
- Все даты и времена лучше возвращать уже в человекочитаемом виде либо в ISO-8601.
- Все ошибки game server должны возвращать понятный `message`, который можно безопасно показать пользователю в Telegram.

## 1. Привязка аккаунта

### POST `/api/{server}/account/link/request`

Назначение:
- Принять ник персонажа и Telegram ID.
- Проверить, что персонаж существует и может быть привязан.
- Создать временный запрос на привязку.
- На стороне L2J показать игроку игровой popup/confirm.

Request:
```json
{
  "characterName": "se1dhe",
  "telegramId": 1259547081
}
```

Response:
```json
{
  "ok": true,
  "status": "prompt_sent",
  "requestId": "a127d115-72b0-4832-ac66-8be090448756",
  "serverName": "x25_old",
  "characterName": "se1dhe",
  "message": null
}
```

Game server должен:
- найти персонажа по имени
- определить аккаунт персонажа
- сохранить pending request
- показать игроку подтверждение в игре

Если запрос создать нельзя, game server должен вернуть `ok: false` и понятный `message`:
```json
{
  "ok": false,
  "status": "not_found",
  "requestId": null,
  "serverName": "x25_old",
  "characterName": "se1dhe",
  "message": "Персонаж не найден"
}
```

Бот принимает как camelCase, так и snake_case поля (`requestId`/`request_id`, `serverName`/`server_name`, `characterName`/`character_name`).

### POST `/api/{server}/account/link/confirm`

Назначение:
- Подтвердить pending request после игрового confirm.
- Вернуть данные аккаунта и всех связанных персонажей этого аккаунта.

Request:
```json
{
  "requestId": "a127d115-72b0-4832-ac66-8be090448756",
  "telegramId": 1259547081
}
```

Response:
```json
{
  "ok": true,
  "externalAccountId": "account_123",
  "serverName": "x25_old",
  "hwid": "HWID-ABC-123",
  "linkedCharacters": [
    {
      "externalCharacterId": 1001,
      "name": "MainChar"
    },
    {
      "externalCharacterId": 1002,
      "name": "Spoiler"
    }
  ]
}
```

Game server должен:
- проверить, что request существует и подтверждён в игре
- вернуть account ID
- вернуть текущий account HWID
- вернуть список персонажей аккаунта с их `obj_Id`

Если игрок ещё не подтвердил запрос в игровом окне, game server должен вернуть `ok: false`:
```json
{
  "ok": false,
  "requestId": "a127d115-72b0-4832-ac66-8be090448756",
  "externalAccountId": null,
  "hwid": null,
  "linkedCharacters": [],
  "telegramUserId": 1259547081,
  "message": "Запрос не найден, истёк или ещё не подтверждён в игре"
}
```

Бот принимает как camelCase, так и snake_case поля (`externalAccountId`/`external_account_id`, `linkedCharacters`/`linked_characters`, `externalCharacterId`/`external_character_id`).

### POST `/api/internal/link/confirmed`

Это endpoint на стороне Telegram bot, который вызывает game server после того, как игрок нажал YES в игровом confirm.

Назначение:
- Уведомить бота, что request подтверждён в игре.
- Бот после этого сам вызовет `/api/{server}/account/link/confirm`, заберёт account/characters и сохранит привязку.

Request:
```json
{
  "telegramUserId": 1259547081,
  "requestId": "a127d115-72b0-4832-ac66-8be090448756",
  "serverName": "x25_old"
}
```

Response:
- `202 Accepted`, если уведомление принято и привязка сохранена.
- `400 Bad Request`, если payload неполный или game server ещё не отдаёт подтверждённую привязку.

Config на стороне game server:
```properties
TelegramBotLinkConfirmedEndpoint = /api/internal/link/confirmed
```

## 2. Отвязка HWID

### POST `/api/{server}/hwid/unlink`

Назначение:
- Отвязать HWID у выбранного персонажа.

Request:
```json
{
  "externalAccountId": "account_123",
  "externalCharacterId": 1001
}
```

Response:
```json
{
  "unlinked": true,
  "message": "HWID отвязан"
}
```

Game server должен:
- проверить, что персонаж принадлежит аккаунту
- снять HWID lock/привязку у персонажа
- при необходимости очистить записи в `hwid_locks` или аналогичном механизме

## 3. Подтверждение входа с нового HWID

Это уже не endpoint на стороне game server для Telegram-меню, а двусторонний flow.

### Game server -> Bot

Game server должен вызвать bot webhook при входе с неизвестного HWID:
- текущий bot endpoint уже существует на стороне бота
- GS должен передать:

```json
{
  "serverName": "x25_old",
  "externalCharacterId": 1001,
  "slot": "PRIMARY",
  "newHwid": "HWID-NEW-999"
}
```

### Bot -> Game server

### POST `/api/{server}/hwid/confirm`

Назначение:
- Получить решение Telegram-пользователя: пустить вход или запретить.

Request:
```json
{
  "serverName": "x25_old",
  "externalAccountId": "account_123",
  "externalCharacterId": 1001,
  "slot": "PRIMARY",
  "newHwid": "HWID-NEW-999",
  "approved": true
}
```

Response:
- можно возвращать `200 OK` с пустым телом

Game server должен:
- если `approved=true`, добавить новый HWID в нужный слот и впустить игрока
- если `approved=false`, отклонить вход
- поддержать default deny после истечения TTL

## 4. Смена Trade Key

### POST `/api/{server}/account/tradekey`

Назначение:
- Установить новый Trade Key у выбранного персонажа.

Request:
```json
{
  "externalAccountId": "account_123",
  "externalCharacterId": 1001,
  "password": "TEST1234"
}
```

Response:
```json
{
  "success": true,
  "message": "Trade key обновлен"
}
```

Game server должен:
- проверить, что персонаж принадлежит аккаунту
- провалидировать пароль
- обновить trade key персонажа
- вернуть понятный текст результата

## 5. Бонус за подписку

### POST `/api/{server}/bonus/claim`

Назначение:
- После проверки подписки на Telegram-канал бот передаёт запрос на выдачу бонуса выбранному персонажу.

Request:
```json
{
  "externalAccountId": "account_123",
  "externalCharacterId": 1001,
  "telegramId": 1259547081,
  "itemId": 57,
  "itemCount": 10
}
```

Response:
```json
{
  "success": true,
  "message": "Бонус выдан"
}
```

Game server должен:
- проверить, что персонаж принадлежит аккаунту
- проверить, что бонус ещё не был выдан повторно, если это ограниченный бонус
- добавить предмет в инвентарь персонажа

## 6. Рефералы

Отдельного GS endpoint сейчас не требуется, если счётчик и реферальные связи живут в bot БД.

Если в будущем награда за реферала должна выдаваться в игру, понадобится endpoint вида:

### POST `/api/{server}/referral/reward`

Request:
```json
{
  "externalAccountId": "account_123",
  "externalCharacterId": 1001,
  "telegramId": 1259547081,
  "itemId": 57,
  "itemCount": 1
}
```

Response:
```json
{
  "success": true,
  "message": "Реферальная награда выдана"
}
```

## 7. Автофарм

## 7. Профиль персонажа

### POST `/api/{server}/characters/profile`

Назначение:
- Вернуть игровые данные для карточки персонажа в Telegram-боте.
- Эти данные показываются в разделе `Мои персонажи`.

Request:
```json
{
  "externalAccountId": "account_123",
  "externalCharacterId": 1001
}
```

Response:
```json
{
  "level": 78,
  "className": "Paladin",
  "pvp": 142,
  "pk": 3,
  "clanName": "Immortal",
  "online": true
}
```

Game server должен:
- проверить, что персонаж принадлежит аккаунту
- вернуть `level`
- вернуть человекочитаемое название класса
- вернуть `pvp`
- вернуть `pk`
- вернуть `clanName`, если персонаж состоит в клане
- вернуть `online`

Источники данных со стороны ГС:
- `characters`
- runtime `Player`, если персонаж онлайн
- `ClanTable` / `clan_data`, если нужно определить название клана

## 8. Автофарм

### POST `/api/{server}/autofarm/status`

Назначение:
- Вернуть статус автофарма выбранного персонажа.

Request:
```json
{
  "externalAccountId": "account_123",
  "externalCharacterId": 1001
}
```

Response:
```json
{
  "activeAutofarm": true,
  "autofarming": false,
  "alive": false,
  "killerName": "EnemyPK",
  "message": "Персонаж мертв, автофарм остановлен"
}
```

Game server должен:
- определить, доступен ли автофарм вообще
- определить, запущен ли он прямо сейчас
- определить, жив ли персонаж
- если мёртв, вернуть ник убийцы

Практически это должно опираться на:
- `player.getFarmSystem().isActiveAutofarm()`
- `player.getFarmSystem().isAutofarming()`
- `player.isDead()` / `player.isAlikeDead()`

### POST `/api/internal/autofarm/death`

Назначение:
- Это inbound webhook на стороне бота.
- Game server должен вызывать его в момент смерти персонажа на автофарме.
- Бот по этому событию отправляет уведомление владельцу персонажа, если у него включена настройка `Автофарм — убили`.

Request:
```json
{
  "serverName": "x25_old",
  "externalCharacterId": 1001,
  "killerName": "EnemyPK",
  "dedupeKey": "autofarm-death:x25_old:1001:2026-04-08T01:15:00"
}
```

Response:
- `202 Accepted`

Game server должен:
- вызывать endpoint сразу после фиксации смерти персонажа на автофарме
- вызывать его только если смерть произошла во время `player.getFarmSystem().isAutofarming() == true`
- передавать `serverName`
- передавать `externalCharacterId`
- передавать `killerName`, если он известен
- передавать стабильный `dedupeKey`, чтобы бот не отправлял дубли при повторной доставке webhook

### POST `/api/{server}/autofarm/revive`

Назначение:
- После успешной викторины воскресить персонажа на месте и снова запустить автофарм.

Request:
```json
{
  "externalAccountId": "account_123",
  "externalCharacterId": 1001
}
```

Response:
```json
{
  "success": true,
  "message": "Персонаж воскрешен на месте, автофарм снова запущен"
}
```

Game server должен:
- воскресить персонажа
- если `player.getFarmSystem().isActiveAutofarm()` возвращает `true`, снова вызвать `startFarmTask()`
- вернуть итоговый статус

## 9. Функции -> В город

### POST `/api/{server}/functions/town`

Назначение:
- Телепортировать выбранного персонажа в город.

Request:
```json
{
  "externalAccountId": "account_123",
  "externalCharacterId": 1001
}
```

Response:
```json
{
  "success": true,
  "message": "Ваш персонаж был телепортирован в город"
}
```

Game server должен:
- проверить, можно ли сейчас делать телепорт
- выполнить телепорт в город
- вернуть статус операции

## 10. Анонсы РБ и ивентов

Это inbound вызовы от game server к боту.

### POST `/api/internal/announcements`

Game server должен уметь отправлять bot webhook при:
- анонсе спавна РБ
- начале регистрации на ивент
- рестарте сервера

Рекомендуемый payload:
```json
{
  "type": "BOSS_SPAWN",
  "serverName": "x25_old",
  "title": "Queen Ant",
  "scheduledAt": "2026-04-07 21:00",
  "dedupeKey": "boss:queen-ant:2026-04-07-2100"
}
```

или

```json
{
  "type": "EVENT_REGISTRATION",
  "serverName": "x25_old",
  "title": "TvT",
  "scheduledAt": "2026-04-07 20:30",
  "dedupeKey": "event:tvt:2026-04-07-2030"
}
```

или

```json
{
  "type": "SERVER_RESTART",
  "serverName": "x25_old",
  "title": "Рестарт сервера",
  "scheduledAt": "2026-04-07 22:00",
  "dedupeKey": "restart:x25_old:2026-04-07-2200"
}
```

Game server должен:
- вызывать bot endpoint сразу в момент игрового анонса
- передавать понятное название РБ/ивента
- передавать `dedupeKey`, чтобы бот не слал дубли

## 11. Что требуется со стороны исходников L2J

Минимально нужны следующие точки интеграции:

- Link request storage:
  - сохранить pending request на привязку
  - показать popup/confirm игроку в игре
  - подтвердить request и вернуть данные аккаунта/персонажей

- HWID layer:
  - определить, что вход идёт с нового HWID
  - вызвать bot webhook
  - дождаться решения approve/deny
  - уметь отвязать HWID у персонажа

- Trade Key layer:
  - API для смены пароля Trade Key у персонажа

- Bonus reward layer:
  - API для выдачи предметов выбранному персонажу

- Character profile layer:
  - API профиля персонажа
  - доступ к level / class / pvp / pk / clan / online

- Autofarm layer:
  - API статуса автофарма
  - API revive + resume autofarm
  - доступ к убийце персонажа
  - доступ к `AutoFarmContext`
  - использование `isActiveAutofarm()`
  - использование `isAutofarming()`
  - возобновление через `startFarmTask()`

- Town function layer:
  - API телепорта персонажа в город

- Announcement hooks:
  - хук на announce spawn raid boss
  - хук на announce registration event

## 12. Что бот ожидает как бизнес-правила

- Все действия идут строго через `server + account + character`.
- Бот не должен вычислять игровые состояния сам.
- Game server является источником истины для:
  - HWID
  - Trade Key
  - Autofarm status
  - revive
  - town teleport
  - item rewards
  - boss/event announcements
