package services;

import l2p.commons.dbcp.DbUtils;
import l2p.commons.util.GArray;
import l2p.gameserver.Config;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.database.mysql;
import l2p.gameserver.handler.AdminCommandHandler;
import l2p.gameserver.handler.IAdminCommandHandler;
import l2p.gameserver.handler.IVoicedCommandHandler;
import l2p.gameserver.handler.VoicedCommandHandler;
import l2p.gameserver.instancemanager.PlayerManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.utils.AutoBan;
import l2p.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Сервис установки пароля для защиты от левого трейда, дропа вещей, енчанта и тд
 */
public class TradeLock extends Functions implements IVoicedCommandHandler, IAdminCommandHandler, ScriptFile
{
	protected static Logger _log = LoggerFactory.getLogger(TradeLock.class);

	public static Map<String, Integer> TRADE_CHECKS = new HashMap<String, Integer>();

	/**
	 * Список войс команд
	 */
	private static final String[] _commandList = {"tradelock", "tradekey", "tradekeyset", "tradekeyreset", "tradekeyrecheck",
		"tradekeydel"
	};

	/**
	 * Список админ команд
	 */
	private static final String[] _adminCommands = {
		"admin_tradekey",
		"admin_cleartradekeys"
	};

	/**
	 * Метод проверки ключа
	 * @param param параметры, которые передаем
	 */
	public void tradeKeyCheck(String[] param)
	{
		L2Player player = getSelf();
		if(player == null)
			return;

		// Включен ли сервис
		if(!Config.SERVICES_TRADE_KEY)
		{
			player.sendMessage(!player.isLangRus() ? "Service disabled." : "Сервис отключен.");
			sendMainPage(player);
			return;
		}

		if(!player.isTradeKeyBlocked())
		{
			player.sendMessage(!player.isLangRus() ? "Lock пароль уже отключен." : "Lock password is already disabled.");
			sendMainPage(player);
			return;
		}

		try
		{
			// Проверяем, подходит ли пароль
			if(Config.TRADE_KEYS.get(player.getObjectId()).equals(param[0].trim()))
			{
				// Если да, то удаляем блокировку
				player.setTradeKeyBlocked(false);

				player.sendMessage(player.isLangRus() ? "Вы успешно ввели пароль." : "You have entered your password successfully.");
				sendMainPage(player);
				// Удаляем хвид с списка проверок на фейл, если была
				if(Config.TRADE_KEY_FAIL_BAN > 0 && TRADE_CHECKS.containsKey(player.getHWID()))
					TRADE_CHECKS.remove(player.getHWID());
			}
			else // Если не сходится
			{
				if(Config.TRADE_KEY_FAIL_BAN <= 0 && Config.TRADE_KEY_FAIL_KICK <= 0)
				{
					player.sendMessage(player.isLangRus() ? "Вы ввели неверный пароль." : "You entered an incorrect password.");
					sendMainPage(player);
				}
				else
				{

					// Включен ли конфиг на количество неудачных попыток к бану
					if(Config.TRADE_KEY_FAIL_BAN > 0)
					{
						int countChecks = TRADE_CHECKS.containsKey(player.getHWID()) ? TRADE_CHECKS.get(player.getHWID()) + 1 : 1;

						if(countChecks >= Config.TRADE_KEY_FAIL_BAN)
						{
							AutoBan.addHwidBan("", player.getHWID(), "Попытка брута Lock пароля " + player.getName(), 0L, "");
							TRADE_CHECKS.remove(player.getHWID());
							player.kick(false);
							return;
						}
						else
						{
							player.sendMessage(player.isLangRus() ? "Вы ввели неверный пароль." : "You entered an incorrect password.");
							sendMainPage(player);
						}

						TRADE_CHECKS.put(player.getHWID(), countChecks);
					}
					// Включен ли конфиг на количество неудачных попыток к кику с игры
					if(Config.TRADE_KEY_FAIL_KICK > 0)
					{
						player.tradeKeyFailCount++;
						if(player.tradeKeyFailCount >= Config.TRADE_KEY_FAIL_KICK)
						{
							player.kick(false);
						}
						else
						{
							player.sendMessage(player.isLangRus() ? "Вы ввели неверный пароль." : "You entered an incorrect password.");
							sendMainPage(player);
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			player.kick(false);
		}
	}

	@Override
	public boolean useVoicedCommand(String command, L2Player activeChar, String args)
	{
		// Байпас ключа
		if(command.equalsIgnoreCase("tradekey"))
		{
			// Проверка, включен ли сервис
			if(!Config.SERVICES_TRADE_KEY)
			{
				activeChar.sendMessage(!activeChar.isLangRus() ? "Service disabled." : "Сервис отключен.");
				sendMainPage(activeChar);
				return true;
			}

			// Подгрузка хтмл
			sendMainPage(activeChar);
			return true;
		}
		// Байпас установки ключа
		if(command.equalsIgnoreCase("tradekeyset"))
		{
			// Проверка, включен ли сервис
			if(Config.SERVICES_TRADE_KEY)
			{
				// Уже имеется в списке данный персонаж
				if(Config.TRADE_KEYS.containsKey(activeChar.getObjectId()))
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "На текущем персонаже уже установлен Lock пароль." : "Current character already have the Lock password.");
					sendMainPage(activeChar);
					return true;
				}

				if(activeChar.isTradeKeyBlocked())
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "На текущем персонаже уже включен Lock пароль." : "Current character already has a Lock password enabled.");
					sendMainPage(activeChar);
					return true;
				}

				// Имеются ли аргументы
				if(args != null && args.split(" ").length > 0 && !args.split(" ")[0].isEmpty() && !args.split(" ")[1].isEmpty())
				{
					// Введенный текст
					String param1 = args.split(" ")[0].trim();
					String param2 = args.split(" ")[1].trim();
					// Проходит ли по регулярным выражениям
					if(!Util.isMatchingRegexp(param1, "[A-Za-z0-9]{4,16}") || !Util.isMatchingRegexp(param2, "[A-Za-z0-9]{4,16}"))
					{
						activeChar.sendMessage(activeChar.isLangRus() ? "Необходимо вводить от 4 до 16 латинских символов" : "Allowed 4-16 latin symbols only");
						sendMainPage(activeChar);
						return true;
					}

					// Введены разные пароли
					if(!param1.equals(param2))
					{
						activeChar.sendMessage(activeChar.isLangRus() ? "У Вас не совпадают введенные пароли." : "Your entered passwords do not match");
						sendMainPage(activeChar);
						return true;
					}

					// Добавляем в список
					Config.TRADE_KEYS.put(activeChar.getObjectId(), param1);
					activeChar.setTradeKeyBlocked(true);

					// Сохраняем ли в базу
					mysql.set("REPLACE INTO `trade_keys` (`obj_Id`, `password`) VALUES ('" + activeChar.getObjectId() + "','" + param1 + "')");

					activeChar.sendMessage(activeChar.isLangRus() ? "Пароль успешно установлен." : "The password has been successfully set.");

					// Хтмл
					sendMainPage(activeChar);
				}
				else
				{
					activeChar.sendMessage(!activeChar.isLangRus() ? "You must to fill in all fields." : "Нужно заполнить все поля.");
					sendMainPage(activeChar);
				}
			}
			else
			{
				activeChar.sendMessage(!activeChar.isLangRus() ? "Service disabled." : "Сервис отключен.");
				sendMainPage(activeChar);
			}

			return true;
		}
		// Байпас смены ключа
		if(command.equalsIgnoreCase("tradekeyreset"))
		{
			// Проверка, включен ли сервис
			if(Config.SERVICES_TRADE_KEY)
			{
				// Уже имеется в списке данный персонаж
				if(!Config.TRADE_KEYS.containsKey(activeChar.getObjectId()))
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "На текущем персонаже не установлен Lock пароль." : "The current character does not have a Lock password set.");
					sendMainPage(activeChar);
					return true;
				}

				if(activeChar.isTradeKeyBlocked())
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можете сменить пароль когда он включен." : "You cannot change your password when it is on.");
					sendMainPage(activeChar);
					return true;
				}

				// Имеются ли аргументы
				if(args != null && args.split(" ").length > 0 && !args.split(" ")[0].isEmpty() && !args.split(" ")[1].isEmpty() && !args.split(" ")[2].isEmpty())
				{
					// Введенный текст
					String oldPassword = args.split(" ")[0].trim();
					String newPassword1 = args.split(" ")[1].trim();
					String newPassword2 = args.split(" ")[2].trim();

					if(!Config.TRADE_KEYS.containsValue(oldPassword))
					{
						activeChar.sendMessage(activeChar.isLangRus() ? "Ваш текущий пароль не соответствует введенному." : "Your current password does not match the one entered.");
						sendMainPage(activeChar);
						return true;
					}

					// Проходит ли по регулярным выражениям
					if(!Util.isMatchingRegexp(newPassword1, "[A-Za-z0-9]{4,16}") || !Util.isMatchingRegexp(newPassword2, "[A-Za-z0-9]{4,16}"))
					{
						activeChar.sendMessage((activeChar.isLangRus() ? "Необходимо вводить от 4 до 16 латинских символов." : "Allowed 4-16 latin symbols only."));
						sendMainPage(activeChar);
						return true;
					}

					// Введены разные пароли
					if(!newPassword1.equals(newPassword2))
					{
						activeChar.sendMessage(activeChar.isLangRus() ? "У Вас не совпадают введенные пароли." : "Your entered passwords do not match");
						sendMainPage(activeChar);
						return true;
					}

					// Добавляем в список
					Config.TRADE_KEYS.put(activeChar.getObjectId(), newPassword1);
					activeChar.setTradeKeyBlocked(true);

					// Сохраняем ли в базу
					mysql.set("UPDATE `trade_keys` SET `password`='" + newPassword1 + "' WHERE `obj_Id`='" + activeChar.getObjectId() + "' LIMIT 1");

					activeChar.sendMessage(activeChar.isLangRus() ? "Пароль успешно изменен." : "Password changed successfully.");

					// Хтмл
					String html = HtmCache.getInstance().getNotNull("command/tradeKey/char_key_reset.htm", activeChar);
					html = html.replaceFirst("%key%", newPassword1);
					html = html.replaceFirst("%stat%", getTradeKeyInfo2(activeChar, !activeChar.isLangRus()));
					show(html, activeChar);
				}
				else
				{
					activeChar.sendMessage(!activeChar.isLangRus() ? "You must to fill in all fields." : "Нужно заполнить все поля.");
					sendMainPage(activeChar);
				}
			}
			else
			{
				activeChar.sendMessage(!activeChar.isLangRus() ? "Service disabled." : "Сервис отключен.");
				sendMainPage(activeChar);
			}

			return true;
		}
		// Байпас отключения ключа
		if(command.equalsIgnoreCase("tradekeyrecheck"))
		{
			// Проверка, включен ли сервис
			// Включен ли сервис
			if(Config.SERVICES_TRADE_KEY)
			{
				// Нечего удалять
				if(!Config.TRADE_KEYS.containsKey(activeChar.getObjectId()))
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Пароль не установлен" : "Password not installed");
					sendMainPage(activeChar);
					return true;
				}

				// Если пароль не включен, то можем включить
				if(!activeChar.isTradeKeyBlocked())
				{
					activeChar.setTradeKeyBlocked(true);
					activeChar.sendMessage(activeChar.isLangRus() ? "Пароль включен." : "Password included.");
					sendMainPage(activeChar);
				}
			}
			else
				activeChar.sendMessage(!activeChar.isLangRus() ? "Service disabled." : "Сервис отключен.");

			return true;
		}
		// Байпас удаления ключа
		if(command.equalsIgnoreCase("tradekeydel"))
		{
			// Включен ли сервис
			if(Config.SERVICES_TRADE_KEY)
			{
				// Нечего удалять
				if(!Config.TRADE_KEYS.containsKey(activeChar.getObjectId()))
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Пароль не установлен" : "Password not installed");
					sendMainPage(activeChar);
					return true;
				}

				// Если включен пароль, то не можем удалить, пока не отключить
				if(activeChar.isTradeKeyBlocked())
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можете удалить пароль когда он включен." : "You cannot remove the password when it is enabled.");
					sendMainPage(activeChar);
					return true;
				}

				// Имеются ли аргументы
				if(args != null && args.split(" ").length > 0 && !args.split(" ")[0].isEmpty())
				{
					// Введенный текст
					String password = args.split(" ")[0].trim();

					if(!Config.TRADE_KEYS.containsValue(password))
					{
						activeChar.sendMessage(activeChar.isLangRus() ? "Ваш текущий пароль не соответствует введенному." : "Your current password does not match the one entered.");
						sendMainPage(activeChar);
						return true;
					}

					// Если есть в списке, то удаляем
					Config.TRADE_KEYS.remove(activeChar.getObjectId());
					//activeChar.setTradeKeyBlocked(false);
					activeChar.removeTradeKeyBlocked();
					// Удаляем из базы, если включен конфиг
					mysql.set("DELETE FROM `trade_keys` WHERE `obj_Id`=" + activeChar.getObjectId() + " LIMIT 1");
					activeChar.sendMessage(activeChar.isLangRus() ? "Lock пароль успешно удален." : "Lock password was successfully deleted.");

					sendMainPage(activeChar);
				}
				else
				{
					activeChar.sendMessage(!activeChar.isLangRus() ? "You need to enter a password." : "Нужно ввести пароль.");
					sendMainPage(activeChar);
				}
			}
			else
			{
				activeChar.sendMessage(!activeChar.isLangRus() ? "Service disabled." : "Сервис отключен.");
				sendMainPage(activeChar);
			}

			return true;
		}
		return false;
	}

	private void sendMainPage(L2Player activeChar)
	{
		if(Config.TRADE_KEYS.containsKey(activeChar.getObjectId()))
		{
			if(activeChar.isTradeKeyBlocked())
			{
				String html = HtmCache.getInstance().getNotNull("command/tradeKey/char_key_enabled.htm", activeChar);
				html = html.replaceFirst("%bgcolor%", getTradeKeyId(activeChar));
				html = html.replaceFirst("%stat%", getTradeKeyInfo(activeChar, !activeChar.isLangRus()));
				show(html, activeChar);
			}
			else
			{
				String html = HtmCache.getInstance().getNotNull("command/tradeKey/char_key_disabled.htm", activeChar);
				html = html.replaceFirst("%bgcolor%", getTradeKeyId(activeChar));
				html = html.replaceFirst("%stat%", getTradeKeyInfo(activeChar, !activeChar.isLangRus()));
				show(html, activeChar);
			}
		}
		else
		{
			String html = HtmCache.getInstance().getNotNull("command/tradeKey/char_key.htm", activeChar);
			html = html.replaceFirst("%bgcolor%", getTradeKeyId(activeChar));
			html = html.replaceFirst("%stat%", getTradeKeyInfo(activeChar, !activeChar.isLangRus()));
			show(html, activeChar);
		}
	}

	/**
	 * Метод вывода информации об установке ключа
	 *
	 * @param player персонаж
	 * @return информация
	 */
	private String getTradeKeyId(L2Player player)
	{
		if(Config.SERVICES_TRADE_KEY)
		{
			if(Config.TRADE_KEYS.containsKey(player.getObjectId()))
			{
				if(player.isTradeKeyBlocked())
					return "008000";
				else
					return "FF8C00";
			}
			else
				return "FF0000";
		}
		else
			return "FF8C00";
	}

	/**
	 * Метод вывода информации об установке ключа
	 *
	 * @param player персонаж
	 * @param en     английская ли
	 * @return информация
	 */
	private String getTradeKeyInfo(L2Player player, boolean en)
	{
		if(Config.SERVICES_TRADE_KEY)
		{
			if(Config.TRADE_KEYS.containsKey(player.getObjectId()))
			{
				if(player.isTradeKeyBlocked())
					return en ? "Lock installed" : "Lock установлен";
				else
					return en ? "Lock disabled" : "Lock отключен";
			}
			else
				return en ? "Lock missing" : "Lock отсутствует";
		}
		else
			return en ? "Lock disabled" : "Lock отключен";
	}

	/**
	 * Метод вывода информации об установке ключа
	 *
	 * @param player персонаж
	 * @param en     английская ли
	 * @return информация
	 */
	private String getTradeKeyInfo2(L2Player player, boolean en)
	{
		if(Config.SERVICES_TRADE_KEY)
		{
			if(Config.TRADE_KEYS.containsKey(player.getObjectId()))
			{
				if(player.isTradeKeyBlocked())
					return en ? "Lock system included!" : "Lock система включена!";
				else
					return en ? "Lock system included!" : "Lock система включена!";
			}
			else
			{
				return en ? "Lock missing" : "Lock отсутствует";
			}
		}
		else
			return en ? "Lock disabled" : "Lock отключен";
	}

	@Override
	public void onLoad()
	{
		if(Config.SERVICES_TRADE_KEY)
		{
			_log.info("Loaded Service: Trade Lock security.");

			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
			AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
			loadTradeLockData();
		}
	}

	/**
	 * Подгружаем информацию
	 */
	private void loadTradeLockData()
	{
		if(Config.SERVICES_TRADE_KEY)
		{
			if(!Config.TRADE_KEYS.isEmpty())
				Config.TRADE_KEYS.clear();
			Connection con = null;
			Statement statement = null;
			ResultSet rset = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.createStatement();
				rset = statement.executeQuery("SELECT * FROM `trade_keys`");
				while(rset.next())
					Config.TRADE_KEYS.put(rset.getInt("obj_Id"), rset.getString("password"));
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement, rset);
			}
			_log.info("Loaded " + Config.TRADE_KEYS.size() + " characters keys.");
		}
	}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	@Override
	public boolean useAdminCommand(String command, L2Player activeChar)
	{
		if(!activeChar.getPlayerAccess().Menu || !activeChar.getPlayerAccess().CanUseGMCommand)
			return false;

		if(command.startsWith("admin_tradekey"))
		{
			StringTokenizer st = new StringTokenizer(command);
			if(st.countTokens() > 1)
			{
				st.nextToken();
				String name = st.nextToken();
				int objId = PlayerManager.getObjectIdByName(name);
				if(objId <= 0)
					activeChar.sendMessage("Character " + name + " not exist.");
				else if(Config.TRADE_KEYS.containsKey(objId))
					activeChar.sendMessage(name + " key: " + Config.TRADE_KEYS.get(objId));
				else
					activeChar.sendMessage("Character " + name + " don't have the key.");
			}
		}
		else if(command.equals("admin_cleartradekeys"))
		{
			GArray<Integer> ids = new GArray<Integer>();
			for(int objId : Config.TRADE_KEYS.keySet())
				if(PlayerManager.getNameByObjectId(objId).isEmpty())
					ids.add(objId);
			if(!ids.isEmpty())
				for(int objId : ids)
					Config.TRADE_KEYS.remove(objId);
			activeChar.sendMessage("Characters keys cleared.");
		}
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}