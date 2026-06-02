import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PainTeamApi {

    /*
     *
     * 1. Поднять HTTP-сервер и закрыть его Bearer-токеном.
     * 2. Реализовать сценарий привязки:
     *    - getLinkStartResponse(...)
     *    - confirmLink(...)
     *    - таблица telegram_link_requests
     *    - подтверждение в игре через TelegramLinkRequestProcessor
     * 3. Реализовать сценарий HWID:
     *    - unlinkHwid(...)
     *    - confirmHwid(...)
     *    - исходящий вызов notifyBotAboutNewHwid(...)
     * 4. Реализовать сценарий Trade Key:
     *    - changeTradeKey(...)
     * 5. Реализовать бонус:
     *    - claimBonus(...)
     *    - выдача itemId/itemCount выбранному персонажу
     * 6. Реализовать профиль персонажа:
     *    - getCharacterProfile(...)
     *    - уровень / класс / pvp / pk / клан / онлайн
     * 7. Реализовать автофарм:
     *    - getAutofarmStatus(...)
     *    - reviveAutofarm(...)
     * 8. Реализовать функцию "В город":
     *    - teleportToTown(...)
     * 9. Подключить исходящие вызовы анонсов:
     *    - notifyBotAnnouncement(...)
     * 10. Подключить исходящий вызов смерти на автофарме:
     *    - notifyBotAutofarmDeath(...)
     */

    private static HttpServer server;
    private static final Gson gson = new GsonBuilder().serializeNulls().create();
    private static final int DEFAULT_PORT = 8081;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String API_TOKEN = "5ecbc7d903b47f0c0118d27910686d4eb9202ef30348ca19a0fab1c422a492a6";
    private static final String BOT_BASE_URL = "http://127.0.0.1:8080";
    private static final String BOT_HWID_ENDPOINT = "/api/internal/hwid";
    private static final String BOT_ANNOUNCEMENT_ENDPOINT = "/api/internal/announcements";
    private static final String BOT_AUTOFARM_DEATH_ENDPOINT = "/api/internal/autofarm/death";

    public static void start(int port) throws IOException {
        if (server != null) {
            return;
        }

        server = HttpServer.create(new InetSocketAddress(port > 0 ? port : DEFAULT_PORT), 0);
        server.createContext("/api", new ApiHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!"POST".equals(exchange.getRequestMethod())) {
                    sendError(exchange, 405, "Method not allowed");
                    return;
                }

                String authorization = exchange.getRequestHeaders().getFirst(AUTHORIZATION_HEADER);
                if (!isValidBearerToken(authorization)) {
                    sendError(exchange, 401, "Unauthorized");
                    return;
                }

                String path = exchange.getRequestURI().getPath();
                String body = readRequestBody(exchange);
                @SuppressWarnings("unchecked")
                Map<String, Object> request = gson.fromJson(body, Map.class);
                if (request == null) {
                    request = new HashMap<>();
                }

                Map<String, Object> response = route(path, authorization, request);
                if (response == null) {
                    sendError(exchange, 404, "Endpoint not found");
                    return;
                }

                sendResponse(exchange, 200, response);
            } catch (IllegalArgumentException exception) {
                sendError(exchange, 400, exception.getMessage());
            } catch (Exception exception) {
                sendError(exchange, 500, "Internal server error");
            }
        }
    }

    private static Map<String, Object> route(String path, String authorization, Map<String, Object> request) {
        String[] pathParts = path.split("/");
        if (pathParts.length < 4) {
            return null;
        }

        String serverName = pathParts[2];
        String resource = "/" + joinFrom(pathParts, 3);

        // ПРИВЯЗКА: старт запроса из Telegram-бота
        if ("/account/link/request".equals(resource)) {
            String characterName = stringValue(request.get("characterName"), request.get("character_name"));
            long telegramUserId = longValue(request.get("telegramId"), request.get("telegram_user_id"));
            String telegramUsername = optionalString(request.get("telegramUsername"), request.get("telegram_username"));
            long expiresAtMs = longValueOptional(request.get("expiresAt"), request.get("expires_at"), System.currentTimeMillis() + 30 * 60 * 1000L);
            Map<String, Object> response = getLinkStartResponse(characterName, telegramUserId, telegramUsername, expiresAtMs);
            response.put("server_name", serverName);
            return response;
        }

        // ПРИВЯЗКА: подтверждение уже одобренного запроса в игре
        if ("/account/link/confirm".equals(resource)) {
            String requestId = stringValue(request.get("requestId"), request.get("request_id"));
            long telegramUserId = longValue(request.get("telegramId"), request.get("telegram_user_id"));
            Map<String, Object> response = confirmLink(requestId, telegramUserId);
            response.put("server_name", serverName);
            return response;
        }

        // HWID: отвязка HWID у выбранного персонажа
        if ("/hwid/unlink".equals(resource)) {
            String externalAccountId = stringValue(request.get("externalAccountId"), request.get("external_account_id"));
            long externalCharacterId = longValue(request.get("externalCharacterId"), request.get("external_character_id"));
            return unlinkHwid(externalAccountId, externalCharacterId);
        }

        // HWID: разрешить или запретить вход с нового HWID
        if ("/hwid/confirm".equals(resource)) {
            String externalAccountId = stringValue(request.get("externalAccountId"), request.get("external_account_id"));
            long externalCharacterId = longValue(request.get("externalCharacterId"), request.get("external_character_id"));
            String slot = stringValue(request.get("slot"));
            String newHwid = stringValue(request.get("newHwid"), request.get("new_hwid"));
            boolean approved = booleanValue(request.get("approved"));
            return confirmHwid(externalAccountId, externalCharacterId, slot, newHwid, approved);
        }

        // ТРЕЙД КЕЙ: установить выбранному персонажу новый пароль
        if ("/account/tradekey".equals(resource)) {
            String externalAccountId = stringValue(request.get("externalAccountId"), request.get("external_account_id"));
            long externalCharacterId = longValue(request.get("externalCharacterId"), request.get("external_character_id"));
            String password = stringValue(request.get("password"));
            return changeTradeKey(externalAccountId, externalCharacterId, password);
        }

        // БОНУС: выдать награду выбранному персонажу после проверки подписки
        if ("/bonus/claim".equals(resource)) {
            String externalAccountId = stringValue(request.get("externalAccountId"), request.get("external_account_id"));
            long externalCharacterId = longValue(request.get("externalCharacterId"), request.get("external_character_id"));
            long telegramUserId = longValue(request.get("telegramId"), request.get("telegram_user_id"));
            long itemId = longValue(request.get("itemId"), request.get("item_id"));
            int itemCount = (int) longValue(request.get("itemCount"), request.get("item_count"));
            return claimBonus(externalAccountId, externalCharacterId, telegramUserId, itemId, itemCount);
        }

        // ПЕРСОНАЖ: получить игровую карточку для Telegram-бота
        if ("/characters/profile".equals(resource)) {
            String externalAccountId = stringValue(request.get("externalAccountId"), request.get("external_account_id"));
            long externalCharacterId = longValue(request.get("externalCharacterId"), request.get("external_character_id"));
            return getCharacterProfile(externalAccountId, externalCharacterId);
        }

        // АВТОФАРМ: получить текущий статус
        if ("/autofarm/status".equals(resource)) {
            String externalAccountId = stringValue(request.get("externalAccountId"), request.get("external_account_id"));
            long externalCharacterId = longValue(request.get("externalCharacterId"), request.get("external_character_id"));
            return getAutofarmStatus(externalAccountId, externalCharacterId);
        }

        // АВТОФАРМ: воскресить персонажа и возобновить автофарм
        if ("/autofarm/revive".equals(resource)) {
            String externalAccountId = stringValue(request.get("externalAccountId"), request.get("external_account_id"));
            long externalCharacterId = longValue(request.get("externalCharacterId"), request.get("external_character_id"));
            return reviveAutofarm(externalAccountId, externalCharacterId);
        }

        // ФУНКЦИИ: отправить выбранного персонажа в город
        if ("/functions/town".equals(resource)) {
            String externalAccountId = stringValue(request.get("externalAccountId"), request.get("external_account_id"));
            long externalCharacterId = longValue(request.get("externalCharacterId"), request.get("external_character_id"));
            return teleportToTown(externalAccountId, externalCharacterId);
        }

        return null;
    }

    /**
     * Старт привязки аккаунта по нику персонажа.
     *
     * ГС должен:
     * - найти персонажа по имени
     * - определить account_name
     * - создать ожидающий запрос
     * - если персонаж онлайн, показать окно подтверждения в игре
     */
    public static Map<String, Object> getLinkStartResponse(String characterName, long telegramUserId, String telegramUsername, long expiresAtMs) {
        // 1) Найти персонажа:
        // int charId = CharacterDAO.getInstance().getObjectIdByName(characterName);
        //
        // 2) Получить логин аккаунта:
        // String accountLogin = CharacterDAO.getInstance().getAccountNameByObjectId(charId);
        //
        // 3) Создать ожидающий запрос:
        // int requestId = createTelegramLinkSession(telegramUserId, telegramUsername, charId, characterName, expiresAtMs);
        //
        // 4) Если персонаж онлайн, показать подтверждение в игре:
        // Player player = GameObjectsStorage.getPlayer(charId);
        // TelegramLinkRequestProcessor.getInstance().tryShowPendingLinkRequestForPlayer(player, requestId);
        //
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("status", "prompt_sent");
        result.put("request_id", "TODO_GENERATED_REQUEST_ID");
        result.put("account_login", "account_login");
        result.put("character_id", 1001);
        result.put("character_name", characterName);
        result.put("telegram_user_id", telegramUserId);
        result.put("telegram_username", telegramUsername);
        result.put("expires_at", expiresAtMs);
        return result;
    }

    /**
     * Подтверждение привязки после игрового confirm.
     *
     * ГС должен:
     * - проверить запрос
     * - вернуть идентификатор аккаунта
     * - вернуть текущий HWID
     * - вернуть список персонажей аккаунта
     */
    public static Map<String, Object> confirmLink(String requestId, long telegramUserId) {
        // 1) Прочитать ожидающий запрос из telegram_link_requests:
        // SELECT * FROM telegram_link_requests WHERE id = requestId
        //
        // 2) Проверить, что статус запроса уже "confirmed".
        // Для обновления статуса используется логика наподобие:
        // updateLinkSessionStatus(requestId, "confirmed")
        // из example/SupportAPIService.java
        //
        // 3) Получить account_login для character_id:
        // CharacterDAO.getInstance().getAccountNameByObjectId(characterId)
        //
        // 4) Получить всех персонажей аккаунта из characters:
        // SELECT obj_Id, char_name FROM characters WHERE account_name = ? AND deletetime = 0
        //
        // 5) Взять текущий HWID аккаунта из accounts.lastHWID
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("request_id", requestId);
        result.put("external_account_id", "account_login");
        result.put("hwid", "HWID-ABC-123");
        result.put("linked_characters", List.of(
                linkedCharacter(1001, "MainChar"),
                linkedCharacter(1002, "Spoiler"),
                linkedCharacter(1003, "Buffer")
        ));
        result.put("telegram_user_id", telegramUserId);
        return result;
    }

    /**
     * Отвязка HWID персонажа.
     *
     * - очистить HWID lock персонажа
     * - при необходимости обновить серверное security state
     */
    public static Map<String, Object> unlinkHwid(String externalAccountId, long externalCharacterId) {
        //  именно отвязка на уровне персонажа.
        //
        // - Security.java
        // - db/hwid_locks.sql
        //
        // Реальная реализация должна:
        // 1) Проверить, что персонаж externalCharacterId принадлежит externalAccountId:
        //    SELECT obj_Id FROM characters WHERE obj_Id = ? AND account_name = ?
        //
        // 2) Очистить HWID lock персонажа в hwid_locks:
        //    DELETE FROM hwid_locks WHERE obj_Id = ?
        // или, если используете слоты Lock1/Lock2 частично:
        //    REPLACE INTO hwid_locks (obj_Id, Lock1, Lock2) VALUES (?, '', '')
        //
        // 3) При необходимости очистить поля времени выполнения player.lockChar1 / player.lockChar2,
        //    если персонаж онлайн, по аналогии с Security.java voiced-командами lockchar/lockchar2.
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("external_account_id", externalAccountId);
        result.put("external_character_id", externalCharacterId);
        result.put("unlinked", true);
        result.put("message", "HWID отвязан");
        return result;
    }

    /**
     * Подтверждение входа с нового HWID.
     *
     * - если approved=true -> разрешить новый HWID и завершить вход
     * - если approved=false -> отклонить вход
     */
    public static Map<String, Object> confirmHwid(String externalAccountId, long externalCharacterId, String slot, String newHwid, boolean approved) {
        //
        // - packet/AuthLogin.java -> client.setHWID(...)
        // - Security.java -> AuthServerCommunication + ChangeAllowedHwid
        //
        // Если approved = true:
        // 1) Найти account_name по externalCharacterId:
        //    CharacterDAO.getInstance().getAccountNameByObjectId((int) externalCharacterId)
        //
        // 2) Обновить разрешённый HWID на стороне авторизации:
        //    AuthServerCommunication.getInstance().sendPacket(
        //        new ChangeAllowedHwid(accountName, primaryHwid, secondaryHwid)
        //    );
        //
        // 3) Разрешить завершение ожидающего входа.
        //
        // Если approved = false:
        // 1) Пометить ожидающий вход как denied
        // 2) Разорвать сессию / не впускать игрока
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("external_account_id", externalAccountId);
        result.put("external_character_id", externalCharacterId);
        result.put("slot", slot);
        result.put("new_hwid", newHwid);
        result.put("approved", approved);
        return result;
    }

    /**
     * Смена Trade Key.
     *
     * - ключ хранится на персонаже
     * - пароль 4-16 [A-Za-z0-9]
     */
    public static Map<String, Object> changeTradeKey(String externalAccountId, long externalCharacterId, String password) {
        // - TradeLock.java
        // - db/trade_keys.sql
        //
        // 1) Проверить владельца персонажа:
        //    SELECT obj_Id FROM characters WHERE obj_Id = ? AND account_name = ?
        //
        // 2) Провалидировать пароль по regex из TradeLock.java:
        //    [A-Za-z0-9]{4,16}
        //
        // 3) Сохранить пароль:
        //    REPLACE INTO trade_keys (obj_Id, password) VALUES (?, ?)
        //
        // 4) Если персонаж онлайн:
        //    Config.TRADE_KEYS.put(player.getObjectId(), password);
        //    player.setTradeKeyBlocked(true);
        //
        // То есть логика должна быть совместима с TradeLock.loadTradeLockData()
        // и проверками во время выполнения player.isTradeKeyBlocked() в клиентских пакетах.
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("external_account_id", externalAccountId);
        result.put("external_character_id", externalCharacterId);
        result.put("success", true);
        result.put("message", "Trade key обновлен");
        return result;
    }

    /**
     * Выдача бонуса за подписку.
     *
     * ГС должен:
     * - проверить target character
     * - выдать itemId/itemCount
     */
    public static Map<String, Object> claimBonus(String externalAccountId, long externalCharacterId, long telegramUserId, long itemId, int itemCount) {
        // - SupportAPIService.java импортирует ItemHolder / ItemTemplate / ItemInstance
        //
        // Реализация на ГС:
        // 1) Проверить, что character принадлежит account
        // 2) Найти игрока онлайн:
        //    Player player = GameObjectsStorage.getPlayer((int) externalCharacterId)
        // 3) Выдать предмет стандартным серверным способом:
        //    player.addItem(itemId, itemCount) / Functions.addItem(...) / аналог вашего ядра
        // 4) Если игрок оффлайн:
        //    создать ItemInstance и положить в инвентарь через DAO/DB
        //
        // Здесь источником истины по выдаче предмета должен быть именно игровой сервер.
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("external_account_id", externalAccountId);
        result.put("external_character_id", externalCharacterId);
        result.put("telegram_user_id", telegramUserId);
        result.put("item_id", itemId);
        result.put("item_count", itemCount);
        result.put("success", true);
        result.put("message", "Бонус выдан");
        return result;
    }

    /**
     * Игровой профиль персонажа для карточки в Telegram-боте.
     *
     * ГС должен вернуть:
     * - уровень
     * - название класса
     * - PvP
     * - PK
     * - название клана, если есть
     * - онлайн ли персонаж
     */
    public static Map<String, Object> getCharacterProfile(String externalAccountId, long externalCharacterId) {
        // Ожидаемая реализация на ГС:
        // 1) Проверить, что персонаж externalCharacterId принадлежит externalAccountId:
        //    SELECT obj_Id, account_name FROM characters WHERE obj_Id = ? AND account_name = ?
        //
        // 2) Прочитать базовые поля из characters:
        //    - level
        //    - base_class / classid / class_index (зависит от вашего ядра)
        //    - pvpKills
        //    - pkKills
        //    - online
        //
        // 3) Определить человекочитаемое название класса:
        //    ClassId.VALUES[classId].toString() / getName() / ваш mapper
        //
        // 4) Определить клан:
        //    - если персонаж онлайн -> player.getClan() / player.getClan().getName()
        //    - если оффлайн -> clanid из characters + ClanTable.getInstance().getClan(clanId)
        //
        // 5) Вернуть данные в компактном виде для карточки Telegram.
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("external_account_id", externalAccountId);
        result.put("external_character_id", externalCharacterId);
        result.put("level", 78);
        result.put("className", "Paladin");
        result.put("pvp", 142);
        result.put("pk", 3);
        result.put("clanName", "Immortal");
        result.put("online", true);
        return result;
    }

    /**
     * Статус автофарма.
     *
     * ГС должен вернуть:
     * - доступен ли автофарм вообще
     * - запущен ли он прямо сейчас
     * - жив ли персонаж
     * - killerName если персонаж мёртв
     */
    public static Map<String, Object> getAutofarmStatus(String externalAccountId, long externalCharacterId) {
        //
        // Ожидаемая реализация:
        // 1) Проверить владельца персонажа
        // 2) Найти Player по obj_Id:
        //    Player player = GameObjectsStorage.getPlayer((int) externalCharacterId)
        // 3) Получить AutoFarmContext:
        //    AutoFarmContext farmSystem = player.getFarmSystem();
        // 4) Получить:
        //    - farmSystem.isActiveAutofarm() -> куплен / доступен ли автофарм
        //    - farmSystem.isAutofarming() -> крутится ли фарм в эту секунду
        //    - player.isDead() / player.isAlikeDead() -> жив ли персонаж
        //    - если мёртв, кто убийца
        //
        // Если killer хранится в вашем PvP/death manager,
        // нужно брать последнее имя убийцы оттуда.
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("external_account_id", externalAccountId);
        result.put("external_character_id", externalCharacterId);
        result.put("activeAutofarm", true);
        result.put("autofarming", false);
        result.put("alive", false);
        result.put("killerName", "EnemyPK");
        result.put("message", "Персонаж мертв, автофарм остановлен");
        return result;
    }

    /**
     * Воскрешение персонажа и возобновление автофарма.
     */
    public static Map<String, Object> reviveAutofarm(String externalAccountId, long externalCharacterId) {
        // Ожидаемая реализация на ГС:
        // 1) Найти Player по obj_Id
        // 2) Воскресить на месте:
        //    player.doRevive() / player.setCurrentHpMp(...)
        // 3) Забрать AutoFarmContext:
        //    AutoFarmContext farmSystem = player.getFarmSystem();
        // 4) Если автофарм персонажу доступен:
        //    if (farmSystem.isActiveAutofarm()) {
        //        farmSystem.startFarmTask();
        //    }
        //
        // Важно: бот здесь только даёт право на воскрешение после викторины.
        // Само действие полностью находится на стороне ГС.
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("external_account_id", externalAccountId);
        result.put("external_character_id", externalCharacterId);
        result.put("success", true);
        result.put("message", "Персонаж воскрешен на месте, автофарм снова запущен");
        return result;
    }

    /**
     * Телепорт в город.
     */
    public static Map<String, Object> teleportToTown(String externalAccountId, long externalCharacterId) {
        // Ожидаемая реализация на ГС:
        // 1) Найти Player по obj_Id
        // 2) Проверить, что телепорт допустим
        // 3) Вызвать серверный телепорт:
        //    player.teleToLocation(...)
        // либо аналогичный метод отправки в town return point
        //
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("external_account_id", externalAccountId);
        result.put("external_character_id", externalCharacterId);
        result.put("success", true);
        result.put("message", "Ваш персонаж был телепортирован в город");
        return result;
    }

    /**
     * Исходящий вызов из ГС в бот при входе с нового HWID.
     *
     * Бот ожидает:
     * - serverName
     * - externalCharacterId
     * - slot
     * - newHwid
     */
    public static int notifyBotAboutNewHwid(String serverName, long externalCharacterId, String slot, String newHwid) throws IOException {
        // Этот метод должен вызываться в пайплайне авторизации и входа,
        // когда ГС понимает, что вход идёт с нового HWID.
        //
        // - packet/AuthLogin.java
        Map<String, Object> payload = new HashMap<>();
        payload.put("serverName", serverName);
        payload.put("externalCharacterId", externalCharacterId);
        payload.put("slot", slot);
        payload.put("newHwid", newHwid);
        return postToBot(BOT_HWID_ENDPOINT, payload);
    }

    /**
     * Исходящий вызов из ГС в бот для анонсов РБ и ивентов.
     *
     * Тип:
     * - BOSS_SPAWN
     * - EVENT_REGISTRATION
     */
    public static int notifyBotAnnouncement(String type, String serverName, String title, String scheduledAt, String dedupeKey) throws IOException {
        // Этот метод должен вызываться в точке игрового анонса.
        //
        // - BossAnnounce.java -> announceToAll(...)
        // - TvT.java -> sayToAll(...) / анонс начала регистрации
        //
        // В момент, когда ГС уже решил анонсировать событие игрокам,
        // он должен параллельно дернуть бот через этот метод.
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);
        payload.put("serverName", serverName);
        payload.put("title", title);
        payload.put("scheduledAt", scheduledAt);
        payload.put("dedupeKey", dedupeKey);
        return postToBot(BOT_ANNOUNCEMENT_ENDPOINT, payload);
    }

    /**
     * Исходящий вызов из ГС в бот при смерти персонажа на автофарме.
     *
     * Бот ожидает:
     * - serverName
     * - externalCharacterId
     * - killerName
     * - dedupeKey
     */
    public static int notifyBotAutofarmDeath(String serverName, long externalCharacterId, String killerName, String dedupeKey) throws IOException {
        // Этот метод должен вызываться сразу после фиксации смерти персонажа на автофарме.
        //
        // Ожидаемая точка вызова на ГС:
        // - там, где auto farm manager / death listener понимает,
        //   что персонаж умер именно во время farmSystem.isAutofarming() == true
        //
        // Что делает бот:
        // - находит владельца персонажа
        // - проверяет включена ли настройка "Автофарм — убили"
        // - отправляет Telegram-уведомление
        //
        // Что должен передать ГС:
        // - serverName
        // - externalCharacterId
        // - killerName, если убийца известен
        // - dedupeKey, чтобы бот не отправлял дубль при повторной доставке
        Map<String, Object> payload = new HashMap<>();
        payload.put("serverName", serverName);
        payload.put("externalCharacterId", externalCharacterId);
        payload.put("killerName", killerName);
        payload.put("dedupeKey", dedupeKey);
        return postToBot(BOT_AUTOFARM_DEATH_ENDPOINT, payload);
    }

    private static boolean isValidBearerToken(String authorization) {
        return ("Bearer " + API_TOKEN).equals(authorization);
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toString(StandardCharsets.UTF_8);
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        String json = gson.toJson(response);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("ok", false);
        error.put("error", message);
        sendResponse(exchange, statusCode, error);
    }

    private static String joinFrom(String[] parts, int index) {
        StringBuilder builder = new StringBuilder();
        for (int i = index; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                if (builder.length() > 0) {
                    builder.append('/');
                }
                builder.append(parts[i]);
            }
        }
        return builder.toString();
    }

    private static String stringValue(Object... candidates) {
        for (Object candidate : candidates) {
            if (candidate != null) {
                String value = String.valueOf(candidate).trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }
        }
        throw new IllegalArgumentException("Отсутствует обязательное строковое поле");
    }

    private static String optionalString(Object... candidates) {
        for (Object candidate : candidates) {
            if (candidate != null) {
                String value = String.valueOf(candidate).trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    private static long longValue(Object... candidates) {
        for (Object candidate : candidates) {
            if (candidate instanceof Number number) {
                return number.longValue();
            }
            if (candidate != null) {
                String value = String.valueOf(candidate).trim();
                if (!value.isEmpty()) {
                    return Long.parseLong(value);
                }
            }
        }
        throw new IllegalArgumentException("Отсутствует обязательное числовое поле");
    }

    private static long longValueOptional(Object candidateOne, Object candidateTwo, long defaultValue) {
        try {
            return longValue(candidateOne, candidateTwo);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private static boolean booleanValue(Object candidate) {
        if (candidate instanceof Boolean value) {
            return value;
        }
        if (candidate != null) {
            return Boolean.parseBoolean(String.valueOf(candidate));
        }
        throw new IllegalArgumentException("Отсутствует обязательное логическое поле");
    }

    private static Map<String, Object> linkedCharacter(long externalCharacterId, String name) {
        Map<String, Object> result = new HashMap<>();
        result.put("external_character_id", externalCharacterId);
        result.put("name", name);
        return result;
    }

    private static int postToBot(String endpoint, Map<String, Object> payload) throws IOException {
        URL url = new URL(BOT_BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        byte[] body = gson.toJson(payload).getBytes(StandardCharsets.UTF_8);
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(body);
        }

        int responseCode = connection.getResponseCode();
        connection.disconnect();
        return responseCode;
    }
}
