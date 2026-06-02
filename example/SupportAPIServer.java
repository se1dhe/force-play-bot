package services.telegram.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import l2.gameserver.utils.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP API сервер для Support API: привязка к Telegram, персонажи, инвентарь, платежи.
 * Порт настраивается через TelegramSupportApiPort в telegram_bot.properties (по умолчанию 8081).
 */
public class SupportAPIServer {

    private static HttpServer server;
    private static final Gson gson = new GsonBuilder().serializeNulls().create();
    private static final int DEFAULT_PORT = 8081;
    private static final String API_KEY_HEADER = "X-API-Key";

    static {
        Log.add("SupportAPIServer class loaded", "telegram");
    }

    private static int getPort() {
        try {
            Class<?> propsClass = Class.forName("l2.gameserver.handler.telegram.TelegramBotProperties");
            java.lang.reflect.Field field = propsClass.getField("TELEGRAM_SUPPORT_API_PORT");
            Integer port = (Integer) field.get(null);
            return port != null && port > 0 ? port : DEFAULT_PORT;
        } catch (Exception e) {
            return DEFAULT_PORT;
        }
    }

    private static String getBindAddress() {
        try {
            Class<?> propsClass = Class.forName("l2.gameserver.handler.telegram.TelegramBotProperties");
            java.lang.reflect.Field field = propsClass.getField("TELEGRAM_SUPPORT_API_BIND_ADDRESS");
            String addr = (String) field.get(null);
            return addr != null && !addr.isEmpty() ? addr.trim() : "0.0.0.0";
        } catch (Exception e) {
            return "0.0.0.0";
        }
    }

    public static void start() {
        if (server != null) {
            Log.add("SupportAPIServer: Server already started", "telegram");
            return;
        }
        try {
            int port = getPort();
            String bindAddr = getBindAddress();
            Log.add("SupportAPIServer: starting bootstrap on " + bindAddr + ":" + port, "telegram");
            server = HttpServer.create(new InetSocketAddress(bindAddr, port), 0);

            server.createContext("/support/link/start", new LinkStartHandler());
            server.createContext("/support/link/status", new LinkStatusHandler());
            server.createContext("/support/link/callback", new LinkCallbackHandler());
            server.createContext("/support/accounts", new AccountsHandler());
            server.createContext("/support/characters", new CharactersHandler());
            server.createContext("/support/payments", new PaymentsHandler());

            Log.add("SupportAPIServer: contexts registered [/support/link/start, /support/link/status, /support/link/callback, /support/accounts, /support/characters, /support/payments]", "telegram");

            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();
            Log.add("SupportAPIServer: Started on " + bindAddr + ":" + port, "telegram");
        } catch (IOException e) {
            Log.add("SupportAPIServer: Failed to start: " + e.getMessage(), "telegram");
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            Log.add("SupportAPIServer: Stopped", "telegram");
        }
    }

    private static boolean isValidApiKey(HttpExchange exchange) {
        String apiKey = exchange.getRequestHeaders().getFirst(API_KEY_HEADER);
        return apiKey != null && apiKey.equals(L2ServerToBotAPI.getApiKey());
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

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        java.io.InputStream is = exchange.getRequestBody();
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String getPathParam(String path, String prefix) {
        if (path == null || !path.startsWith(prefix)) return null;
        String rest = path.substring(prefix.length()).replaceFirst("^/", "");
        int slash = rest.indexOf('/');
        return slash >= 0 ? rest.substring(0, slash) : rest;
    }

    /** POST /support/link/start */
    static class LinkStartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Log.add("SupportAPIServer LinkStartHandler invoked: " + exchange.getRequestMethod() + " " + exchange.getRequestURI(), "telegram");
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            if (!isValidApiKey(exchange)) {
                sendError(exchange, 401, "Unauthorized");
                return;
            }
            try {
                String body = readRequestBody(exchange);
                @SuppressWarnings("unchecked")
                Map<String, Object> req = gson.fromJson(body, Map.class);
                String characterName = (String) req.get("character_name");
                if (characterName == null || characterName.isEmpty()) {
                    sendError(exchange, 400, "character_name is required");
                    return;
                }
                long telegramUserId = 0;
                Object tuidObj = req.get("telegram_user_id");
                if (tuidObj instanceof Number) {
                    telegramUserId = ((Number) tuidObj).longValue();
                }
                long expiresAt = 0;
                Object expObj = req.get("expires_at");
                if (expObj instanceof Number) {
                    long val = ((Number) expObj).longValue();
                    // если значение < 10^12 — это секунды, конвертируем в миллисекунды
                    expiresAt = val < 10000000000L ? val * 1000L : val;
                } else if (expObj instanceof String) {
                    try {
                        long val = Long.parseLong((String) expObj);
                        expiresAt = val < 10000000000L ? val * 1000L : val;
                    } catch (NumberFormatException ignored) {
                        try {
                            expiresAt = Instant.parse((String) expObj).toEpochMilli();
                        } catch (Exception ignoredIso) {}
                    }
                }
                if (expiresAt <= 0) {
                    expiresAt = System.currentTimeMillis() + 30 * 60 * 1000L; // 30 минут по умолчанию
                }
                String telegramUsername = req.get("telegram_username") != null ? String.valueOf(req.get("telegram_username")) : null;
                Log.add("SupportAPIServer link/start payload: character=" + characterName + ", telegram_user_id=" + telegramUserId + ", telegram_username=" + telegramUsername, "telegram");
                Map<String, Object> result = SupportAPIService.getLinkStartResponse(characterName, telegramUserId, telegramUsername, expiresAt);
                if (result == null) {
                    Log.add("SupportAPIServer link/start result: character not found or session create failed for " + characterName, "telegram");
                    sendError(exchange, 404, "Character not found");
                    return;
                }
                Log.add("SupportAPIServer link/start result: " + gson.toJson(result), "telegram");
                sendResponse(exchange, 200, result);
            } catch (Exception e) {
                Log.add("SupportAPIServer link/start error: " + e.getMessage(), "telegram");
                sendError(exchange, 500, "Internal server error");
            }
        }
    }

    /** GET /support/link/status/{session_id} */
    static class LinkStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Log.add("SupportAPIServer LinkStatusHandler invoked: " + exchange.getRequestMethod() + " " + exchange.getRequestURI(), "telegram");
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            if (!isValidApiKey(exchange)) {
                sendError(exchange, 401, "Unauthorized");
                return;
            }
            try {
                String path = exchange.getRequestURI().getPath();
                String sessionId = getPathParam(path, "/support/link/status");
                if (sessionId == null || sessionId.isEmpty()) {
                    sendError(exchange, 400, "session_id is required");
                    return;
                }
                Map<String, Object> result = SupportAPIService.getLinkStatus(sessionId);
                if (result == null) {
                    Log.add("SupportAPIServer link/status result: session not found, session_id=" + sessionId, "telegram");
                    sendError(exchange, 404, "Session not found");
                    return;
                }
                Log.add("SupportAPIServer link/status result: " + gson.toJson(result), "telegram");
                sendResponse(exchange, 200, result);
            } catch (Exception e) {
                Log.add("SupportAPIServer link/status error: " + e.getMessage(), "telegram");
                sendError(exchange, 500, "Internal server error");
            }
        }
    }

    /** POST /support/link/callback — принимает callback от L2 (игрок подтвердил). Ответ ok. */
    static class LinkCallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Log.add("SupportAPIServer LinkCallbackHandler invoked: " + exchange.getRequestMethod() + " " + exchange.getRequestURI(), "telegram");
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            if (!isValidApiKey(exchange)) {
                sendError(exchange, 401, "Unauthorized");
                return;
            }
            try {
                String body = readRequestBody(exchange);
                @SuppressWarnings("unchecked")
                Map<String, Object> req = gson.fromJson(body, Map.class);
                if (req == null) {
                    sendError(exchange, 400, "Request body is required");
                    return;
                }

                String sessionId = null;
                Object sessionObj = req.get("session_id");
                if (sessionObj != null) {
                    sessionId = String.valueOf(sessionObj);
                } else if (req.get("id") != null) {
                    sessionId = String.valueOf(req.get("id"));
                }

                if (sessionId == null || sessionId.isEmpty()) {
                    sendError(exchange, 400, "session_id is required");
                    return;
                }

                String status = req.get("status") != null ? String.valueOf(req.get("status")) : "confirmed";
                Log.add("SupportAPIServer link/callback payload: session_id=" + sessionId + ", status=" + status, "telegram");
                Map<String, Object> result = SupportAPIService.updateLinkSessionStatus(sessionId, status);
                if (result == null) {
                    Log.add("SupportAPIServer link/callback result: session not found or invalid status, session_id=" + sessionId + ", status=" + status, "telegram");
                    sendError(exchange, 404, "Session not found or status invalid");
                    return;
                }
                Log.add("SupportAPIServer link/callback result: " + gson.toJson(result), "telegram");
                sendResponse(exchange, 200, result);
            } catch (Exception e) {
                Log.add("SupportAPIServer link/callback error: " + e.getMessage(), "telegram");
                sendError(exchange, 500, "Internal server error");
            }
        }
    }

    /**
     * GET /support/accounts/{account_login}
     * GET /support/accounts/{account_login}/characters | /payments | /sanctions
     */
    static class AccountsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            if (!isValidApiKey(exchange)) {
                sendError(exchange, 401, "Unauthorized");
                return;
            }
            try {
                String path = exchange.getRequestURI().getPath();
                if (path.endsWith("/") && path.length() > 1) {
                    path = path.substring(0, path.length() - 1);
                }
                final String prefix = "/support/accounts/";
                if (!path.startsWith(prefix)) {
                    sendError(exchange, 404, "Not found");
                    return;
                }
                String sub = path.substring(prefix.length());
                if (sub.isEmpty()) {
                    sendError(exchange, 400, "account_login is required");
                    return;
                }
                int slash = sub.indexOf('/');
                String loginSegment = slash < 0 ? sub : sub.substring(0, slash);
                String tail = slash < 0 ? "" : sub.substring(slash);
                String accountLogin;
                try {
                    accountLogin = URLDecoder.decode(loginSegment, StandardCharsets.UTF_8.name());
                } catch (Exception e) {
                    accountLogin = loginSegment;
                }
                if (accountLogin.isEmpty()) {
                    sendError(exchange, 400, "account_login is required");
                    return;
                }

                if ("/characters".equals(tail)) {
                    Map<String, Object> result = SupportAPIService.getAccountCharacters(accountLogin);
                    if (result == null) {
                        sendError(exchange, 404, "Account not found");
                        return;
                    }
                    sendResponse(exchange, 200, result);
                } else if ("/payments".equals(tail)) {
                    Map<String, Object> result = SupportAPIService.getAccountPayments(accountLogin);
                    if (result == null) {
                        sendError(exchange, 404, "Account not found");
                        return;
                    }
                    sendResponse(exchange, 200, result);
                } else if ("/sanctions".equals(tail)) {
                    Map<String, Object> result = SupportAPIService.getAccountSanctions(accountLogin);
                    if (result == null) {
                        sendError(exchange, 404, "Account not found");
                        return;
                    }
                    sendResponse(exchange, 200, result);
                } else if (tail.isEmpty()) {
                    Map<String, Object> result = SupportAPIService.getAccount(accountLogin);
                    if (result == null) {
                        sendError(exchange, 404, "Account not found");
                        return;
                    }
                    sendResponse(exchange, 200, result);
                } else {
                    sendError(exchange, 404, "Not found");
                }
            } catch (Exception e) {
                Log.add("SupportAPIServer accounts error: " + e.getMessage(), "telegram");
                sendError(exchange, 500, "Internal server error");
            }
        }
    }

    /** GET /support/characters/{character_id} | /support/characters/{character_id}/inventory */
    static class CharactersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            if (!isValidApiKey(exchange)) {
                sendError(exchange, 401, "Unauthorized");
                return;
            }
            try {
                String path = exchange.getRequestURI().getPath();
                String charIdStr = getPathParam(path, "/support/characters");
                if (charIdStr == null || charIdStr.isEmpty()) {
                    sendError(exchange, 400, "character_id is required");
                    return;
                }
                int characterId = Integer.parseInt(charIdStr);
                if (path.contains("/inventory")) {
                    Map<String, Object> result = SupportAPIService.getCharacterInventory(characterId);
                    if (result == null) {
                        sendError(exchange, 404, "Character not found");
                        return;
                    }
                    sendResponse(exchange, 200, result);
                } else {
                    Map<String, Object> result = SupportAPIService.getCharacterFull(characterId);
                    if (result == null) {
                        sendError(exchange, 404, "Character not found");
                        return;
                    }
                    sendResponse(exchange, 200, result);
                }
            } catch (NumberFormatException e) {
                sendError(exchange, 400, "Invalid character_id");
            } catch (Exception e) {
                Log.add("SupportAPIServer characters error: " + e.getMessage(), "telegram");
                sendError(exchange, 500, "Internal server error");
            }
        }
    }

    /** GET /support/payments/{payment_id} */
    static class PaymentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            if (!isValidApiKey(exchange)) {
                sendError(exchange, 401, "Unauthorized");
                return;
            }
            try {
                String path = exchange.getRequestURI().getPath();
                String paymentIdStr = getPathParam(path, "/support/payments");
                if (paymentIdStr == null || paymentIdStr.isEmpty()) {
                    sendError(exchange, 400, "payment_id is required");
                    return;
                }
                int paymentId = Integer.parseInt(paymentIdStr);
                Map<String, Object> result = SupportAPIService.getPayment(paymentId);
                if (result == null) {
                    sendError(exchange, 404, "Payment not found");
                    return;
                }
                sendResponse(exchange, 200, result);
            } catch (NumberFormatException e) {
                sendError(exchange, 400, "Invalid payment_id");
            } catch (Exception e) {
                Log.add("SupportAPIServer payments error: " + e.getMessage(), "telegram");
                sendError(exchange, 500, "Internal server error");
            }
        }
    }
}
