package commands.voiced;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.ScheduledFuture;

import l2p.commons.dbcp.DbUtils;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.database.mysql;
import l2p.gameserver.handler.IVoicedCommandHandler;
import l2p.gameserver.handler.VoicedCommandHandler;
import l2p.gameserver.instancemanager.PlayerManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.network.authcomm.AuthServerCommunication;
import l2p.gameserver.network.authcomm.gspackets.ChangeAllowedHwid;
import l2p.gameserver.network.authcomm.gspackets.ChangeAllowedIp;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.utils.Files;
import l2p.gameserver.utils.Util;

public class Security extends Functions implements IVoicedCommandHandler, ScriptFile
{
	private String[] _commandList = { "lock", "lockIp", "unlockIp", "lockchar", "lockchar2", "lockacc", "lockacc2", "charkey", "charkeyset", "charkeydel" };

	private static String defaultPage = "scripts/commands/voiced/lock.htm";
	private static ScheduledFuture<?> _saveTask;
	private static final String DTF = "yyyy-MM-dd_HH-mm-ss";

	private static void showDefaultPage(L2Player activeChar)
	{
		boolean en = !activeChar.isLangRus();
		String html = HtmCache.getInstance().getNotNull(defaultPage, activeChar);
		html = html.replace("%ip_block%", IpBlockStatus(en));
		html = html.replace("%hwid_block%", HwidBlockStatus(en));
		html = html.replace("%hwid_block_char%", HwidBlockCharStatus(en));
		html = html.replace("%char_key%", CharKey(activeChar.getObjectId(), en));
		html = html.replace("%curIP%", activeChar.getIP());
		show(html, activeChar);
	}

	public boolean useVoicedCommand(String command, L2Player activeChar, String args)
	{
		if(command.equalsIgnoreCase("lock"))
		{
			showDefaultPage(activeChar);
			return true;
		}

		if(command.equalsIgnoreCase("unlockIp"))
		{
			AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedIp(activeChar.getAccountName(), ""));
			if(!activeChar.isLangRus())
				activeChar.sendMessage("Account unlocked.");
			else
				activeChar.sendMessage("Ограничение входа по IP адресу снято.");

			String html = HtmCache.getInstance().getNotNull("scripts/commands/voiced/lock_ip.html", activeChar);
			html = html.replaceFirst("%curIP%", activeChar.getIP());
			show(html, activeChar);
			return true;
		}
		if(command.equalsIgnoreCase("lockIp"))
		{
			if(!Config.SERVICES_LOCK_ACCOUNT_IP)
			{
				activeChar.sendMessage(!activeChar.isLangRus() ? "Service disabled." : "Сервис отключен.");
				return true;
			}

			AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedIp(activeChar.getAccountName(), activeChar.getIP()));
			if(!activeChar.isLangRus())
				activeChar.sendMessage("Account locked.");
			else
				activeChar.sendMessage("Активировано. Разрешенный IP: " + activeChar.getIP());

			String html = HtmCache.getInstance().getNotNull("scripts/commands/voiced/lock_ip.html", activeChar);
			html = html.replaceFirst("%curIP%", activeChar.getIP());
			show(html, activeChar);
			return true;
		}

		if(command.equalsIgnoreCase("lockchar"))
		{
			if(Config.SERVICES_LOCK_CHAR_HWID)
			{
				if(!activeChar.getHWID().isEmpty())
				{
					if(!activeChar.lockChar1.isEmpty())
					{
						if(activeChar.lockChar2.isEmpty())
							mysql.set("DELETE FROM `hwid_locks` WHERE `obj_Id`=" + activeChar.getObjectId() + " LIMIT 1");
						else
							mysql.set("REPLACE INTO `hwid_locks` (obj_Id, Lock1, Lock2) values(" + activeChar.getObjectId() + ",'','" + activeChar.lockChar2 + "')");
						activeChar.lockChar1 = "";
						if(!activeChar.isLangRus())
							activeChar.sendMessage("Your char unlocked from PC #1.");
						else
							activeChar.sendMessage("Привязка персонажа к железу #1 убрана.");
					}
					else
					{
						mysql.set("REPLACE INTO `hwid_locks` (obj_Id, Lock1, Lock2) values(" + activeChar.getObjectId() + ",'" + activeChar.getHWID() + "','" + activeChar.lockChar2 + "')");
						activeChar.lockChar1 = activeChar.getHWID();
						if(!activeChar.isLangRus())
							activeChar.sendMessage("Your char locked to PC #1 successfully.");
						else
							activeChar.sendMessage("Ваш персонаж успешно привязан к железу #1.");
					}
				}
				else
					activeChar.sendMessage(!activeChar.isLangRus() ? "Your PC is not identified! Lock impossible." : "Ваше железо не определено! Привязка невозможна.");
			}
			else
				activeChar.sendMessage(!activeChar.isLangRus() ? "Service disabled." : "Сервис отключен.");

			return true;
		}
		if(command.equalsIgnoreCase("lockchar2"))
		{
			if(Config.SERVICES_LOCK_CHAR_HWID)
			{
				String hwid = "";
				if(args != null && args.split(" ").length > 0 && !args.split(" ")[0].isEmpty())
					hwid = PlayerManager.getLastHWIDByName(args.split(" ")[0]);
				else
				{
					if(!activeChar.lockChar2.isEmpty())
					{
						if(activeChar.lockChar1.isEmpty())
							mysql.set("DELETE FROM `hwid_locks` WHERE `obj_Id`=" + activeChar.getObjectId() + " LIMIT 1");
						else
							mysql.set("REPLACE INTO `hwid_locks` (obj_Id, Lock1, Lock2) values(" + activeChar.getObjectId() + ",'" + activeChar.lockChar1 + "','')");
						activeChar.lockChar2 = "";
						if(!activeChar.isLangRus())
							activeChar.sendMessage("Your char unlocked from PC #2.");
						else
							activeChar.sendMessage("Привязка персонажа к железу #2 убрана.");
					}
					else
						activeChar.sendMessage(!activeChar.isLangRus() ? "Not locked." : "Привязка отсутствует.");
					return true;
				}
				if(!hwid.isEmpty())
				{
					mysql.set("REPLACE INTO `hwid_locks` (obj_Id, Lock1, Lock2) values(" + activeChar.getObjectId() + ",'" + activeChar.lockChar1 + "','" + hwid + "')");
					activeChar.lockChar2 = hwid;
					if(!activeChar.isLangRus())
						activeChar.sendMessage("Your char locked to PC #2 successfully.");
					else
						activeChar.sendMessage("Ваш персонаж успешно привязан к железу #2.");
				}
				else
					activeChar.sendMessage(!activeChar.isLangRus() ? "Your PC is not identified! Lock impossible." : "Ваше железо не определено! Привязка невозможна.");
			}
			else
				activeChar.sendMessage(!activeChar.isLangRus() ? "Service disabled." : "Сервис отключен.");

			return true;
		}

		if(command.equalsIgnoreCase("lockacc"))
		{
			if(Config.SERVICES_LOCK_ACC_HWID)
			{
				if(!activeChar.getHWID().isEmpty())
				{
					if(!activeChar.getNetConnection().getAllowedHwid().isEmpty())
					{
						AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedHwid(activeChar.getAccountName(), "", activeChar.getNetConnection().getAllowedHwidSecond()));
						activeChar.getNetConnection().setAllowedHwid("");
						if(activeChar.getNetConnection().getAllowedHwidSecond().isEmpty())
							activeChar.getNetConnection().setLockedHWID(false);

						if(!activeChar.isLangRus())
							activeChar.sendMessage("Your account unlocked from PC #1.");
						else
							activeChar.sendMessage("Привязка аккаунта к железу #1 убрана.");
					}
					else
					{
						AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedHwid(activeChar.getAccountName(), activeChar.getHWID(), activeChar.getNetConnection().getAllowedHwidSecond()));
						activeChar.getNetConnection().setAllowedHwid(activeChar.getHWID());
						activeChar.getNetConnection().setLockedHWID(true);

						if(!activeChar.isLangRus())
							activeChar.sendMessage("Your account locked to PC #1 successfully.");
						else
							activeChar.sendMessage("Ваш аккаунт успешно привязан к железу #1.");
					}
				}
				else
					activeChar.sendMessage(!activeChar.isLangRus() ? "Your PC is not identified! Lock impossible." : "Ваше железо не определено! Привязка невозможна.");
			}
			else
				activeChar.sendMessage(!activeChar.isLangRus() ? "Service disabled." : "Сервис отключен.");
			return true;
		}
		if(command.equalsIgnoreCase("lockacc2"))
		{
			if(Config.SERVICES_LOCK_ACC_HWID)
			{
				String hwid = "";
				if(args != null && args.split(" ").length > 0 && !args.split(" ")[0].isEmpty())
					hwid = PlayerManager.getLastHWIDByName(args.split(" ")[0]);
				else
				{
					if(!activeChar.getNetConnection().getAllowedHwidSecond().isEmpty())
					{
						AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedHwid(activeChar.getAccountName(), activeChar.getNetConnection().getAllowedHwid(), ""));
						activeChar.getNetConnection().setAllowedHwidSecond("");
						if(activeChar.getNetConnection().getAllowedHwid().isEmpty())
							activeChar.getNetConnection().setLockedHWID(false);
						if(!activeChar.isLangRus())
							activeChar.sendMessage("Your account unlocked from PC #2.");
						else
							activeChar.sendMessage("Привязка аккаунта к железу #2 убрана.");
					}
					else
						activeChar.sendMessage(!activeChar.isLangRus() ? "Not locked." : "Привязка отсутствует.");
					return true;
				}
				if(!hwid.isEmpty())
				{
					AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedHwid(activeChar.getAccountName(), activeChar.getNetConnection().getAllowedHwid(), hwid));
					activeChar.getNetConnection().setAllowedHwidSecond(hwid);
					activeChar.getNetConnection().setLockedHWID(true);
					if(!activeChar.isLangRus())
						activeChar.sendMessage("Your account locked to PC #2 successfully.");
					else
						activeChar.sendMessage("Ваш аккаунт успешно привязан к железу #2.");
				}
				else
					activeChar.sendMessage(!activeChar.isLangRus() ? "Your PC is not identified! Lock impossible." : "Ваше железо не определено! Привязка невозможна.");
			}
			else
				activeChar.sendMessage(!activeChar.isLangRus() ? "Service disabled." : "Сервис отключен.");

			return true;
		}
		if(command.equalsIgnoreCase("charkey"))
		{
			if(!Config.SERVICES_CHAR_KEY)
			{
				activeChar.sendMessage(!activeChar.isLangRus() ? "Service disabled." : "Сервис отключен.");
				return true;
			}

			String html = HtmCache.getInstance().getNotNull("scripts/commands/voiced/charKey/char_key.html", activeChar);
			html = html.replaceFirst("%stat%", CharKey(activeChar.getObjectId(), !activeChar.isLangRus()));
			show(html, activeChar);
			return true;
		}
		if(command.equalsIgnoreCase("charkeyset"))
		{
			if(Config.SERVICES_CHAR_KEY)
			{
				if(Config.CHAR_KEYS.containsKey(activeChar.getObjectId()))
				{
					show("<br>" + (activeChar.isLangRus() ? "На текущем персонаже уже установлен ключ. Для установки нового ключа, удалите текущий ( команда" : "Current character already have the key. To install the new key, remove the current ( command") + " <font color=\"LEVEL\">.charkeydel</font> )<br><br><br><a action=\"bypass -h user_charkey\">" + (activeChar.isLangRus() ? "Назад" : "Back") + "</a>", activeChar);
					return true;
				}
				if(args != null && args.split(" ").length > 0 && !args.split(" ")[0].isEmpty())
				{
					String param = args.split(" ")[0].trim();
					if(!Util.isMatchingRegexp(param, "[A-Za-z0-9]{4,16}"))
					{
						show("<br>" + (activeChar.isLangRus() ? "Необходимо вводить от 4 до 16 латинских символов" : "Allowed 4-16 latin symbols only") + ".<br><br><br><a action=\"bypass -h user_charkey\">" + (activeChar.isLangRus() ? "Назад" : "Back") + "</a>", activeChar);
						return true;
					}
					Config.CHAR_KEYS.put(activeChar.getObjectId(), param);
					if(Config.CHAR_KEY_SAVE_DB && Config.CHAR_KEY_SAVE_DELAY == 0)
						mysql.set("REPLACE INTO `char_keys` (`obj_Id`, `password`) VALUES ('" + activeChar.getObjectId() + "','" + param + "')");

					if(activeChar.isKeyForced())
					{
						activeChar.setKeyForced(false);
						activeChar.sendMessage(new CustomMessage("l2p.KeyUnFrozen", activeChar));
					}
					String html = HtmCache.getInstance().getNotNull("scripts/commands/voiced/charKey/char_key_set.html", activeChar);
					html = html.replaceFirst("%key%", param);
					show(html, activeChar);
				}
				else
					activeChar.sendMessage(!activeChar.isLangRus() ? "You must to fill in all fields." : "Нужно заполнить все поля.");
			}
			else
				activeChar.sendMessage(!activeChar.isLangRus() ? "Service disabled." : "Сервис отключен.");

			return true;
		}
		if(command.equalsIgnoreCase("charkeydel"))
		{
			if(Config.SERVICES_CHAR_KEY)
			{
				if(!Config.CHAR_KEYS.containsKey(activeChar.getObjectId()))
				{
					show("<br>" + (activeChar.isLangRus() ? "Ключ не установлен" : "Key not installed") + ".<br><br><br><a action=\"bypass -h user_charkey\">" + (activeChar.isLangRus() ? "Назад" : "Back") + "</a>", activeChar);
					return true;
				}
				Config.CHAR_KEYS.remove(activeChar.getObjectId());
				if(Config.CHAR_KEY_SAVE_DB)
					mysql.set("DELETE FROM `char_keys` WHERE `obj_Id`=" + activeChar.getObjectId() + " LIMIT 1");
				show("<br>" + (activeChar.isLangRus() ? "Ключ персонажа успешно удален" : "Key deleted successfully") + "!<br><br><br><a action=\"bypass -h user_charkey\">" + (activeChar.isLangRus() ? "Назад" : "Back") + "</a>", activeChar);
			}
			else
				activeChar.sendMessage(!activeChar.isLangRus() ? "Service disabled." : "Сервис отключен.");

			return true;
		}
		return false;
	}

	private static String IpBlockStatus(boolean en)
	{
		if(Config.SERVICES_LOCK_ACCOUNT_IP)
			return en ? "Allowed" : "Разрешена";
		else
			return en ? "Disabled" : "Запрещена";
	}

	private static String HwidBlockStatus(boolean en)
	{
		if(Config.SERVICES_LOCK_ACC_HWID)
			return en ? "Allowed" : "Разрешена";
		else
			return en ? "Disabled" : "Запрещена";
	}

	private static String HwidBlockCharStatus(boolean en)
	{
		if(Config.SERVICES_LOCK_CHAR_HWID)
			return en ? "Allowed" : "Разрешена";
		else
			return en ? "Disabled" : "Запрещена";
	}

	private static String CharKey(int id, boolean en)
	{
		if(Config.SERVICES_CHAR_KEY)
		{
			if(Config.CHAR_KEYS.containsKey(id))
				return en ? "Installed" : "Установлена";
			else
				return en ? "Not installed" : "Не установлена";
		}
		else
			return en ? "Disabled" : "Запрещена";
	}

	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
		if(Config.SERVICES_CHAR_KEY)
		{
			if(Config.CHAR_KEY_SAVE_DB)
			{
				if(!Config.CHAR_KEYS.isEmpty())
					Config.CHAR_KEYS.clear();
				Connection con = null;
				Statement statement = null;
				ResultSet rset = null;
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.createStatement();
					rset = statement.executeQuery("SELECT * FROM `char_keys`");
					while(rset.next())
						Config.CHAR_KEYS.put(rset.getInt("obj_Id"), rset.getString("password"));
				}
				catch(Exception e)
				{
					_log.error("", e);
				}
				finally
				{
					DbUtils.closeQuietly(con, statement, rset);
				}
				_log.info("Loaded " + Config.CHAR_KEYS.size() + " characters keys.");
			}
			else
			{
				File file = new File(Config.DATAPACK_ROOT, "data/char_keys.txt");
				if(file.exists())
				{
					if(!Config.CHAR_KEYS.isEmpty())
						Config.CHAR_KEYS.clear();
					LineNumberReader lnr = null;

					try
					{
						lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
						String line;
						while((line = lnr.readLine()) != null)
						{
							StringTokenizer st = new StringTokenizer(line, "\n");
							if(st.hasMoreTokens())
							{
								String[] tab = st.nextToken().split("\t");
								if(tab.length > 1)
									Config.CHAR_KEYS.put(Integer.parseInt(tab[0]), tab[1].trim());
							}
						}
						_log.info("Loaded " + Config.CHAR_KEYS.size() + " characters keys.");
					}
					catch(IOException e1)
					{
						_log.error("Error reading char_keys " + e1);
					}
					finally
					{
						try
						{
							if(lnr != null)
								lnr.close();
						}
						catch(Exception e)
						{}
					}
				}
			}

			if(_saveTask != null)
			{
				_saveTask.cancel(false);
				_saveTask = null;
			}
			if(Config.CHAR_KEY_SAVE_DELAY > 0)
				_saveTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new saveTask(), Config.CHAR_KEY_SAVE_DELAY * 60000L, Config.CHAR_KEY_SAVE_DELAY * 60000L);
		}
	}

	public void onReload()
	{}

	public void onShutdown()
	{
		if(_saveTask != null)
		{
			_saveTask.cancel(false);
			_saveTask = null;
		}
		save();
	}

	public static void save()
	{
		if(Config.SERVICES_CHAR_KEY)
		{
			if(!Config.CHAR_KEYS.isEmpty())
				if(Config.CHAR_KEY_SAVE_DB)
				{
					for(int i : Config.CHAR_KEYS.keySet())
						mysql.set("REPLACE INTO `char_keys` (`obj_Id`, `password`) VALUES ('" + i + "','" + Config.CHAR_KEYS.get(i) + "')");
				}
				else
				{
					File file = new File(Config.DATAPACK_ROOT, "data/char_keys.txt");
					if(file.exists())
					{
						if(Config.CHAR_KEY_BACKUP)
							Files.copyFile("data/char_keys.txt", "data/backup/char_keys_" + new SimpleDateFormat(DTF).format(new Date()) + ".txt");
						file.delete();
					}
					try
					{
						file.createNewFile();
					}
					catch(IOException e)
					{
						_log.error("Creating char_keys failed: " + e);
						return;
					}
					FileWriter save = null;
					try
					{
						save = new FileWriter(file, true);
						for (int i : Config.CHAR_KEYS.keySet())
							save.write(i + "\t" + Config.CHAR_KEYS.get(i) + "\n");
					}
					catch(IOException e)
					{
						_log.error("Saving char_keys failed: " + e);
						e.printStackTrace();
					}
					finally
					{
						try
						{
							if(save != null)
								save.close();
						}
						catch(Exception e)
						{}
					}
				}
		}
	}

	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	private static class saveTask implements Runnable
	{
		@Override
		public void run()
		{
			save();
		}
	}
}