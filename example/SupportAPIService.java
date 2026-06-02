package services.telegram.api;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.dao.CharacterDAO;
import l2.gameserver.dao.CharacterVariablesDAO;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.database.TelegramDBFactory;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.ItemInstance.ItemLocation;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.utils.Log;
import services.telegram.handlers.TelegramLinkRequestProcessor;

import java.sql.*;
import java.util.*;

/**
 * Сервис для Support API: персонажи, инвентарь, привязка, платежи.
 */
public class SupportAPIService {

    static {
        Log.add("SupportAPIService class loaded", "telegram");
    }

    /**
     * По character_name создаёт сессию привязки в {@code telegram_link_requests} и возвращает данные для ответа link/start.
     * Если персонаж онлайн — показывает окно подтверждения через {@link TelegramLinkRequestProcessor}.
     */
    public static Map<String, Object> getLinkStartResponse(String characterName, long telegramUserId, String telegramUsername, long expiresAtMs) {
        Log.add("SupportAPIService.getLinkStartResponse called: character=" + characterName + ", telegram_user_id=" + telegramUserId + ", telegram_username=" + telegramUsername, "telegram");
        int charId = CharacterDAO.getInstance().getObjectIdByName(characterName);
        if (charId <= 0) return null;

        String accountLogin = CharacterDAO.getInstance().getAccountNameByObjectId(charId);
        if (accountLogin == null) return null;

        // Создаём сессию в telegram_link_requests
        int sessionId = createLinkSession(telegramUserId, telegramUsername, charId, characterName, expiresAtMs);
        if (sessionId <= 0) {
            Log.add("SupportAPIService.getLinkStartResponse: failed to create session for " + characterName, "telegram");
            return null;
        }

        Player player = GameObjectsStorage.getPlayer(charId);
        boolean isOnline = player != null && player.isOnline() && !player.isInOfflineMode();

        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("status", "prompt_sent");
        result.put("session_id", String.valueOf(sessionId));
        result.put("account_login", accountLogin);
        result.put("character_id", charId);
        result.put("character_name", characterName);
        result.put("is_online", isOnline);

        if (isOnline && player != null) {
            TelegramLinkRequestProcessor.getInstance().tryShowPendingLinkRequestForPlayer(player, sessionId);
            Log.add("SupportAPIService.getLinkStartResponse: online player, prompt requested for session=" + sessionId, "telegram");
        }
        return result;
    }

    /**
     * Вставляет строку в {@code telegram_link_requests} со статусом 'pending'.
     * @return auto-increment id новой строки, или -1 при ошибке.
     */
    private static int createLinkSession(long telegramId, String telegramUsername, int characterId, String characterName, long expiresAtMs) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = TelegramDBFactory.getConnection();
            stmt = con.prepareStatement(
                "INSERT INTO `telegram_link_requests` (`telegram_id`, `telegram_username`, `character_id`, `character_name`, `status`, `expires_at`, `created_at`) " +
                "VALUES (?, ?, ?, ?, 'pending', ?, NOW())",
                Statement.RETURN_GENERATED_KEYS
            );
            stmt.setLong(1, telegramId);
            stmt.setString(2, telegramUsername);
            stmt.setInt(3, characterId);
            stmt.setString(4, characterName);
            stmt.setLong(5, expiresAtMs);
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                Log.add("SupportAPIService: created link session id=" + id + " for char=" + characterName + " tg=" + telegramId, "telegram");
                return id;
            }
            return -1;
        } catch (Exception e) {
            Log.add("SupportAPIService.createLinkSession error: " + e.getMessage(), "telegram");
            return -1;
        } finally {
            DbUtils.closeQuietly(con, stmt, rs);
        }
    }

    /**
     * Получает статус сессии привязки по session_id (или id из telegram_link_requests).
     * Читает из БД бота (TelegramDBFactory).
     */
    public static Map<String, Object> getLinkStatus(String sessionId) {
        Log.add("SupportAPIService.getLinkStatus called: session_id=" + sessionId, "telegram");
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = TelegramDBFactory.getConnection();
            // session_id в URL — только числовой id строки telegram_link_requests (не UUID)
            int id = -1;
            try {
                id = Integer.parseInt(sessionId);
            } catch (NumberFormatException ignored) {}
            if (id < 0) return null;

            stmt = con.prepareStatement(
                "SELECT id, telegram_id, character_id, character_name, status, expires_at " +
                "FROM telegram_link_requests WHERE id = ?"
            );
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (!rs.next()) return null;

            String accountLogin = CharacterDAO.getInstance().getAccountNameByObjectId(rs.getInt("character_id"));

            Map<String, Object> result = new HashMap<>();
            result.put("ok", true);
            result.put("session_id", String.valueOf(rs.getInt("id")));
            result.put("status", rs.getString("status"));
            result.put("telegram_user_id", rs.getLong("telegram_id"));
            result.put("account_login", accountLogin != null ? accountLogin : "");
            result.put("character_id", rs.getInt("character_id"));
            result.put("character_name", rs.getString("character_name"));
            long exp = rs.getLong("expires_at");
            result.put("expires_at", formatIso8601(exp > 10000000000L ? exp : exp * 1000L));
            Log.add("SupportAPIService.getLinkStatus result: session_id=" + sessionId + ", status=" + result.get("status") + ", account_login=" + result.get("account_login"), "telegram");
            return result;
        } catch (Exception e) {
            Log.add("SupportAPIService.getLinkStatus error: " + e.getMessage(), "telegram");
            return null;
        } finally {
            DbUtils.closeQuietly(con, stmt, rs);
        }
    }

    /**
     * Обновляет статус сессии привязки в {@code telegram_link_requests} и возвращает актуальный payload.
     * Допустимые статусы: pending, confirmed, rejected, expired.
     */
    public static Map<String, Object> updateLinkSessionStatus(String sessionId, String status) {
        Log.add("SupportAPIService.updateLinkSessionStatus called: session_id=" + sessionId + ", status=" + status, "telegram");
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (sessionId == null || sessionId.isEmpty()) return null;

            int id;
            try {
                id = Integer.parseInt(sessionId);
            } catch (NumberFormatException e) {
                return null;
            }

            String normalizedStatus = normalizeLinkStatus(status);
            if (normalizedStatus == null) return null;

            con = TelegramDBFactory.getConnection();

            stmt = con.prepareStatement("SELECT id FROM telegram_link_requests WHERE id = ?");
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (!rs.next()) return null;
            DbUtils.closeQuietly(rs, stmt, null);
            rs = null;
            stmt = null;

            stmt = con.prepareStatement("UPDATE telegram_link_requests SET status = ? WHERE id = ?");
            stmt.setString(1, normalizedStatus);
            stmt.setInt(2, id);
            stmt.executeUpdate();

            Log.add("SupportAPIService.updateLinkSessionStatus: session=" + id + " status=" + normalizedStatus, "telegram");
            return getLinkStatus(String.valueOf(id));
        } catch (Exception e) {
            Log.add("SupportAPIService.updateLinkSessionStatus error: " + e.getMessage(), "telegram");
            return null;
        } finally {
            DbUtils.closeQuietly(con, stmt, rs);
        }
    }

    /**
     * Запись из {@code accounts} (без пароля). {@code null}, если логина нет в БД.
     */
    public static Map<String, Object> getAccount(String accountLogin) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            stmt = con.prepareStatement(
                "SELECT `login`, `lastactive`, `accessLevel`, `lastIP`, `lastHWID`, `lastServerId`, `ban_expire`, `allow_ip`, `l2email` " +
                "FROM `accounts` WHERE `login` = ?"
            );
            stmt.setString(1, accountLogin);
            rs = stmt.executeQuery();
            if (!rs.next()) return null;

            String loginVal = rs.getString("login");
            long lastActiveSec = rs.getLong("lastactive");
            if (rs.wasNull()) lastActiveSec = 0;
            int banExpire = rs.getInt("ban_expire");
            long nowSec = System.currentTimeMillis() / 1000L;

            Integer accessLevel = null;
            Object al = rs.getObject("accessLevel");
            if (al != null) accessLevel = ((Number) al).intValue();
            Integer lastServerId = null;
            Object sid = rs.getObject("lastServerId");
            if (sid != null) lastServerId = ((Number) sid).intValue();
            String lastIP = rs.getString("lastIP");
            String lastHWID = rs.getString("lastHWID");
            String allowIp = rs.getString("allow_ip");
            String l2email = rs.getString("l2email");

            DbUtils.closeQuietly(rs, stmt, null);
            rs = null;
            stmt = null;

            int characterCount = 0;
            stmt = con.prepareStatement("SELECT COUNT(*) FROM `characters` WHERE `account_name` = ? AND `deletetime` = 0");
            stmt.setString(1, accountLogin);
            rs = stmt.executeQuery();
            if (rs.next()) characterCount = rs.getInt(1);
            DbUtils.closeQuietly(rs, stmt, null);
            rs = null;
            stmt = null;

            Map<String, Object> result = new HashMap<>();
            result.put("ok", true);
            result.put("account_login", loginVal);
            result.put("last_active_at", formatIso8601(lastActiveSec * 1000L));
            result.put("access_level", accessLevel);
            result.put("last_ip", lastIP);
            result.put("last_hwid", lastHWID);
            result.put("last_server_id", lastServerId);
            result.put("ban_expire_unix", banExpire);
            result.put("ban_expire_at", banExpire > 0 ? formatIso8601(banExpire * 1000L) : null);
            result.put("account_banned", banExpire > nowSec);
            result.put("allow_ip", allowIp != null ? allowIp : "");
            result.put("email", l2email);
            result.put("character_count", characterCount);
            return result;
        } catch (Exception e) {
            Log.add("SupportAPIService.getAccount error: " + e.getMessage(), "telegram");
            return null;
        } finally {
            DbUtils.closeQuietly(con, stmt, rs);
        }
    }

    /**
     * Санкции по аккаунту и персонажам (БД + переменная {@code jailed}).
     */
    public static Map<String, Object> getAccountSanctions(String accountLogin) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            stmt = con.prepareStatement(
                "SELECT `ban_expire`, `accessLevel` FROM `accounts` WHERE `login` = ?"
            );
            stmt.setString(1, accountLogin);
            rs = stmt.executeQuery();
            if (!rs.next()) return null;

            int banExpire = rs.getInt("ban_expire");
            long nowSec = System.currentTimeMillis() / 1000L;
            Integer accAccess = null;
            Object al = rs.getObject("accessLevel");
            if (al != null) accAccess = ((Number) al).intValue();
            DbUtils.closeQuietly(rs, stmt, null);
            rs = null;
            stmt = null;

            Map<String, Object> accountBlock = new HashMap<>();
            accountBlock.put("access_level", accAccess);
            accountBlock.put("ban_expire_unix", banExpire);
            accountBlock.put("ban_expire_at", banExpire > 0 ? formatIso8601(banExpire * 1000L) : null);
            accountBlock.put("banned", banExpire > nowSec);

            stmt = con.prepareStatement(
                "SELECT `obj_Id`, `char_name`, `accesslevel`, `karma`, `nochannel`, `deletetime` " +
                "FROM `characters` WHERE `account_name` = ? ORDER BY `obj_Id` ASC"
            );
            stmt.setString(1, accountLogin);
            rs = stmt.executeQuery();

            List<Map<String, Object>> chars = new ArrayList<>();
            while (rs.next()) {
                int objId = rs.getInt("obj_Id");
                long noChSec = rs.getLong("nochannel");
                boolean chatBanActive = noChSec > nowSec;

                String jailed = CharacterVariablesDAO.getInstance().getVar(objId, "jailed");
                long jailEndMs = 0;
                boolean jailActive = false;
                if (jailed != null && !jailed.isEmpty()) {
                    int idx = jailed.indexOf(';');
                    try {
                        jailEndMs = idx > 0 ? Long.parseLong(jailed.substring(0, idx)) : Long.parseLong(jailed);
                        jailActive = jailEndMs > System.currentTimeMillis();
                    } catch (NumberFormatException ignored) {}
                }

                int charAcc = rs.getInt("accesslevel");
                boolean charAccNull = rs.wasNull();

                Map<String, Object> row = new HashMap<>();
                row.put("character_id", objId);
                row.put("character_name", rs.getString("char_name"));
                row.put("access_level", charAccNull ? null : charAcc);
                row.put("karma", rs.getInt("karma"));
                row.put("chat_ban_until_unix", noChSec);
                row.put("chat_ban_until_at", noChSec > 0 ? formatIso8601(noChSec * 1000L) : null);
                row.put("chat_ban_active", chatBanActive);
                row.put("jail_active", jailActive);
                row.put("jail_end_at", jailEndMs > 0 ? formatIso8601(jailEndMs) : null);
                long delTime = rs.getLong("deletetime");
                row.put("delete_scheduled", delTime > 0);
                row.put("delete_at", delTime > 0 ? formatIso8601(delTime * 1000L) : null);
                chars.add(row);
            }

            Map<String, Object> out = new HashMap<>();
            out.put("ok", true);
            out.put("account_login", accountLogin);
            out.put("account", accountBlock);
            out.put("characters", chars);
            return out;
        } catch (Exception e) {
            Log.add("SupportAPIService.getAccountSanctions error: " + e.getMessage(), "telegram");
            return null;
        } finally {
            DbUtils.closeQuietly(con, stmt, rs);
        }
    }

    /**
     * Список персонажей аккаунта. Порядок — по возрастанию {@code character_id}; {@code is_main} у персонажа с минимальным id.
     * {@code null} — нет записи в {@code accounts}. Пустой {@code characters} — аккаунт есть, персонажей нет.
     */
    public static Map<String, Object> getAccountCharacters(String accountLogin) {
        if (!accountExists(accountLogin)) return null;

        Map<Integer, String> chars = CharacterDAO.getInstance().listCharactersByAccountName(accountLogin);
        if (chars == null || chars.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("ok", true);
            empty.put("account_login", accountLogin);
            empty.put("characters", new ArrayList<Map<String, Object>>());
            return empty;
        }

        List<Integer> ids = new ArrayList<>(chars.keySet());
        Collections.sort(ids);
        int mainId = ids.get(0);

        List<Map<String, Object>> list = new ArrayList<>();
        for (int charId : ids) {
            String name = chars.get(charId);
            Map<String, Object> charData = getCharacterBasic(charId, name, accountLogin);
            if (charData != null) {
                charData.put("is_main", charId == mainId);
                list.add(charData);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("account_login", accountLogin);
        result.put("characters", list);
        return result;
    }

    /**
     * Полная информация о персонаже.
     */
    public static Map<String, Object> getCharacterFull(int characterId) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            stmt = con.prepareStatement(
                "SELECT c.obj_Id, c.char_name, c.account_name, c.sex, c.clanid, c.online, c.lastAccess, " +
                "cs.class_id, cs.level " +
                "FROM characters c " +
                "LEFT JOIN character_subclasses cs ON c.obj_Id = cs.char_obj_id AND cs.active = 1 " +
                "WHERE c.obj_Id = ?"
            );
            stmt.setInt(1, characterId);
            rs = stmt.executeQuery();
            if (!rs.next()) return null;

            Map<String, Object> result = new HashMap<>();
            result.put("ok", true);
            result.put("character_id", rs.getInt("obj_Id"));
            result.put("account_login", rs.getString("account_name"));
            result.put("character_name", rs.getString("char_name"));
            result.put("class_id", rs.getInt("class_id"));
            result.put("class_name", getClassName(rs.getInt("class_id")));
            result.put("level", rs.getInt("level"));
            result.put("race", "Human");
            result.put("sex", rs.getInt("sex") == 0 ? "female" : "male");
            int clanId = rs.getInt("clanid");
            result.put("clan_id", clanId);
            result.put("clan_name", clanId > 0 ? getClanName(con, clanId) : null);
            result.put("alliance_id", 0);
            result.put("alliance_name", null);
            result.put("is_online", rs.getInt("online") == 1);
            long lastAccess = rs.getLong("lastAccess");
            result.put("last_login_at", formatIso8601(lastAccess > 0 ? lastAccess * 1000L : 0));
            result.put("location", getCharacterLocation(characterId));
            return result;
        } catch (Exception e) {
            Log.add("SupportAPIService.getCharacterFull error: " + e.getMessage(), "telegram");
            return null;
        } finally {
            DbUtils.closeQuietly(con, stmt, rs);
        }
    }

    /**
     * Инвентарь персонажа. Для онлайн - из памяти, для офлайн - из БД.
     * {@code null} — нет строки в {@code characters} с таким {@code obj_Id}.
     */
    public static Map<String, Object> getCharacterInventory(int characterId) {
        if (!characterExists(characterId)) return null;

        Player player = GameObjectsStorage.getPlayer(characterId);
        List<Map<String, Object>> items = new ArrayList<>();
        long adena = 0;

        if (player != null && player.isOnline()) {
            for (ItemInstance item : player.getInventory().getItems()) {
                if (item == null) continue;
                if (item.getItemId() == 57) {
                    adena = item.getCount();
                    continue;
                }
                items.add(itemToMap(item));
            }
            if (adena == 0) {
                ItemInstance adenaItem = player.getInventory().getItemByItemId(57);
                if (adenaItem != null) adena = adenaItem.getCount();
            }
        } else {
            return getCharacterInventoryFromDb(characterId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("character_id", characterId);
        result.put("adena", adena);
        result.put("items", items);
        return result;
    }

    private static Map<String, Object> getCharacterInventoryFromDb(int characterId) {
        Connection con = null;
        CallableStatement cstmt = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            cstmt = con.prepareCall("{CALL lip_LoadItemsByOwner(?)}");
            cstmt.setInt(1, characterId);
            rs = cstmt.executeQuery();

            List<Map<String, Object>> items = new ArrayList<>();
            long adena = 0;

            while (rs.next()) {
                int itemTypeId = rs.getInt("item_type");
                if (itemTypeId == 57) {
                    adena = rs.getLong("amount");
                    continue;
                }
                Map<String, Object> im = new HashMap<>();
                im.put("object_id", rs.getInt("item_id"));
                im.put("item_id", itemTypeId);
                ItemTemplate tpl = ItemHolder.getInstance().getTemplate(itemTypeId);
                im.put("item_name", tpl != null ? tpl.getName() : "Unknown");
                im.put("icon", tpl != null ? tpl.getIcon() : "");
                im.put("count", rs.getLong("amount"));
                im.put("enchant", rs.getInt("enchant"));
                String locStr = rs.getString("location");
                boolean equipped = "PAPERDOLL".equals(locStr);
                im.put("equipped", equipped);
                im.put("location", locStr != null ? locStr : "INVENTORY");
                im.put("slot", equipped ? "weapon" : "inventory");
                items.add(im);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("ok", true);
            result.put("character_id", characterId);
            result.put("adena", adena);
            result.put("items", items);
            return result;
        } catch (Exception e) {
            Log.add("SupportAPIService.getCharacterInventoryFromDb error: " + e.getMessage(), "telegram");
            return null;
        } finally {
            DbUtils.closeQuietly(con, cstmt, rs);
        }
    }

    private static Map<String, Object> itemToMap(ItemInstance item) {
        Map<String, Object> m = new HashMap<>();
        m.put("object_id", item.getObjectId());
        m.put("item_id", item.getItemId());
        m.put("item_name", item.getName());
        m.put("icon", item.getTemplate() != null ? item.getTemplate().getIcon() : "");
        m.put("count", item.getCount());
        m.put("enchant", item.getEnchantLevel());
        m.put("equipped", item.isEquipped());
        m.put("location", item.getLocation() == ItemLocation.PAPERDOLL ? "PAPERDOLL" : "INVENTORY");
        m.put("slot", item.isEquipped() ? getSlotName(item.getBodyPart()) : "inventory");
        if (item.getItemId() == 57) m.put("slot", "currency");
        return m;
    }

    private static String getSlotName(int bodyPart) {
        switch (bodyPart) {
            case 0: return "underwear";
            case 1: return "r-ear";
            case 2: return "l-ear";
            case 3: return "neck";
            case 4: return "r-finger";
            case 5: return "l-finger";
            case 6: return "head";
            case 7: return "r-hand";
            case 8: return "l-hand";
            case 9: return "gloves";
            case 10: return "chest";
            case 11: return "legs";
            case 12: return "feet";
            case 13: return "back";
            case 14: return "lr-hand";
            case 15: return "hair";
            case 16: return "hair2";
            case 17: return "r-bracelet";
            case 18: return "l-bracelet";
            case 19: return "weapon";
            default: return "weapon";
        }
    }

    /**
     * Платежи аккаунта (из items_delayed). {@code null} — нет записи в {@code accounts}.
     * Пустой {@code payments} — аккаунт есть, отложенных выдач нет.
     */
    public static Map<String, Object> getAccountPayments(String accountLogin) {
        if (!accountExists(accountLogin)) return null;

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            stmt = con.prepareStatement(
                "SELECT i.payment_id, i.owner_id, i.item_id, i.count, i.enchant_level, i.payment_status, i.description, " +
                "c.char_name, c.account_name " +
                "FROM items_delayed i " +
                "JOIN characters c ON i.owner_id = c.obj_Id " +
                "WHERE c.account_name = ? " +
                "ORDER BY i.payment_id DESC LIMIT 100"
            );
            stmt.setString(1, accountLogin);
            rs = stmt.executeQuery();

            List<Map<String, Object>> payments = new ArrayList<>();
            while (rs.next()) {
                payments.add(paymentRowToMap(rs));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("ok", true);
            result.put("account_login", accountLogin);
            result.put("payments", payments);
            return result;
        } catch (Exception e) {
            Log.add("SupportAPIService.getAccountPayments error: " + e.getMessage(), "telegram");
            return null;
        } finally {
            DbUtils.closeQuietly(con, stmt, rs);
        }
    }

    /**
     * Один платёж по ID.
     */
    public static Map<String, Object> getPayment(int paymentId) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            stmt = con.prepareStatement(
                "SELECT i.payment_id, i.owner_id, i.item_id, i.count, i.enchant_level, i.payment_status, i.description, " +
                "c.char_name, c.account_name " +
                "FROM items_delayed i " +
                "JOIN characters c ON i.owner_id = c.obj_Id " +
                "WHERE i.payment_id = ?"
            );
            stmt.setInt(1, paymentId);
            rs = stmt.executeQuery();
            if (!rs.next()) return null;

            Map<String, Object> result = paymentRowToMap(rs);
            result.put("ok", true);
            return result;
        } catch (Exception e) {
            Log.add("SupportAPIService.getPayment error: " + e.getMessage(), "telegram");
            return null;
        } finally {
            DbUtils.closeQuietly(con, stmt, rs);
        }
    }

    private static Map<String, Object> paymentRowToMap(ResultSet rs) throws SQLException {
        int itemId = rs.getInt("item_id");
        ItemTemplate tpl = ItemHolder.getInstance().getTemplate(itemId);
        String itemName = tpl != null ? tpl.getName() : "Unknown";
        String icon = tpl != null ? tpl.getIcon() : "";
        int paymentStatus = rs.getInt("payment_status");

        Map<String, Object> m = new HashMap<>();
        m.put("payment_id", rs.getInt("payment_id"));
        m.put("character_id", rs.getInt("owner_id"));
        m.put("character_name", rs.getString("char_name"));
        m.put("item_id", itemId);
        m.put("item_name", itemName);
        m.put("icon", icon);
        m.put("count", rs.getInt("count"));
        m.put("enchant", rs.getInt("enchant_level"));
        m.put("status", "paid");
        m.put("delivery_status", paymentStatus == 1 ? "delivered" : "processing");
        m.put("delivery_error", null);
        m.put("product_name", "Item");
        m.put("amount", 0);
        m.put("currency", "RUB");
        m.put("provider", "delayed");
        m.put("created_at", null);
        m.put("updated_at", null);
        m.put("can_retry_delivery", paymentStatus == 0);
        m.put("note", rs.getString("description"));
        return m;
    }

    private static Map<String, Object> getCharacterBasic(int charId, String charName, String accountLogin) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            stmt = con.prepareStatement(
                "SELECT c.clanid, c.online, c.lastAccess, cs.class_id, cs.level " +
                "FROM characters c " +
                "LEFT JOIN character_subclasses cs ON c.obj_Id = cs.char_obj_id AND cs.active = 1 " +
                "WHERE c.obj_Id = ?"
            );
            stmt.setInt(1, charId);
            rs = stmt.executeQuery();
            if (!rs.next()) return null;

            Map<String, Object> m = new HashMap<>();
            m.put("character_id", charId);
            m.put("character_name", charName);
            m.put("class_id", rs.getInt("class_id"));
            m.put("class_name", getClassName(rs.getInt("class_id")));
            m.put("level", rs.getInt("level"));
            int clanId = rs.getInt("clanid");
            m.put("clan_id", clanId);
            m.put("clan_name", clanId > 0 ? getClanName(con, clanId) : null);
            m.put("alliance_id", 0);
            m.put("alliance_name", null);
            m.put("is_online", rs.getInt("online") == 1);
            m.put("last_login_at", formatIso8601(rs.getLong("lastAccess") * 1000L));
            return m;
        } catch (Exception e) {
            return null;
        } finally {
            DbUtils.closeQuietly(con, stmt, rs);
        }
    }

    private static Map<String, Object> getCharacterLocation(int charId) {
        Player p = GameObjectsStorage.getPlayer(charId);
        if (p != null && p.isOnline()) {
            Map<String, Object> loc = new HashMap<>();
            loc.put("x", (int) p.getX());
            loc.put("y", (int) p.getY());
            loc.put("z", (int) p.getZ());
            loc.put("region", p.getReflection().getName() != null ? p.getReflection().getName() : "World");
            return loc;
        }
        Map<String, Object> loc = new HashMap<>();
        loc.put("x", 0);
        loc.put("y", 0);
        loc.put("z", 0);
        loc.put("region", "Unknown");
        return loc;
    }

    private static String getClanName(Connection con, int clanId) {
        try (PreparedStatement ps = con.prepareStatement("SELECT clan_name FROM clan_data WHERE clan_id = ?")) {
            ps.setInt(1, clanId);
            try (ResultSet r = ps.executeQuery()) {
                return r.next() ? r.getString("clan_name") : null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /** Есть ли логин в {@code accounts}. */
    private static boolean accountExists(String login) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            ps = con.prepareStatement("SELECT 1 FROM `accounts` WHERE `login` = ? LIMIT 1");
            ps.setString(1, login);
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            Log.add("SupportAPIService.accountExists error: " + e.getMessage(), "telegram");
            return false;
        } finally {
            DbUtils.closeQuietly(con, ps, rs);
        }
    }

    /** Есть ли персонаж в {@code characters}. */
    private static boolean characterExists(int objectId) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            ps = con.prepareStatement("SELECT 1 FROM `characters` WHERE `obj_Id` = ? LIMIT 1");
            ps.setInt(1, objectId);
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            Log.add("SupportAPIService.characterExists error: " + e.getMessage(), "telegram");
            return false;
        } finally {
            DbUtils.closeQuietly(con, ps, rs);
        }
    }

    private static String normalizeLinkStatus(String status) {
        if (status == null) return "confirmed";
        String value = status.trim().toLowerCase(java.util.Locale.ROOT);
        if (value.isEmpty()) return "confirmed";
        if ("approved".equals(value) || "approve".equals(value) || "success".equals(value) || "ok".equals(value)) {
            return "confirmed";
        }
        if ("declined".equals(value) || "decline".equals(value) || "deny".equals(value)) {
            return "rejected";
        }
        if ("pending".equals(value) || "confirmed".equals(value) || "rejected".equals(value) || "expired".equals(value)) {
            return value;
        }
        return null;
    }

    private static String getClassName(int classId) {
        String[] names = {
            "Fighter", "Warrior", "Gladiator", "Warlord", "Knight", "Paladin", "Dark Avenger", "Treasure Hunter",
            "Hawkeye", "Mystic", "Wizard", "Sorcerer", "Necromancer", "Warlock", "Cleric", "Bishop", "Prophet",
            "Elf Fighter", "Elf Knight", "Temple Knight", "Sword Singer", "Elf Scout", "Plains Walker", "Silver Ranger",
            "Elf Mystic", "Elf Wizard", "Spellsinger", "Elemental Summoner", "Oracle", "Elder",
            "Dark Fighter", "Palus Knight", "Shillien Knight", "Blade Dancer", "Assassin", "Fortune Seeker",
            "Dark Mystic", "Dark Wizard", "Spellhowler", "Phantom Summoner", "Shillien Elder",
            "Orc Fighter", "Orc Raider", "Destroyer", "Monk", "Tyrant", "Orc Mystic", "Orc Shaman",
            "Overlord", "Warcryer", "Dwarf Fighter", "Scavenger", "Bounty Hunter", "Artisan", "Warsmith",
            "Duelist", "Dreadnought", "Phoenix Knight", "Hell Knight", "Sagittarius", "Adventurer",
            "Archmage", "Soultaker", "Arcana Lord", "Cardinal", "Hierophant", "Eva's Templar", "Sword Muse",
            "Wind Rider", "Moonlight Sentinel", "Mystic Muse", "Elemental Master", "Eva's Saint", "Shillien Templar",
            "Spectral Dancer", "Ghost Hunter", "Ghost Sentinel", "Storm Screamer", "Spectral Master", "Shillien Saint",
            "Titan", "Grand Khavatari", "Dominator", "Doom Cryer", "Fortune Seeker", "Maestro"
        };
        return classId >= 0 && classId < names.length ? names[classId] : "Unknown";
    }

    private static String formatIso8601(long millis) {
        if (millis <= 0) return "1970-01-01T00:00:00Z";
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
            .format(new java.util.Date(millis));
    }
}
