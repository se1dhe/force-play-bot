package l2p.gameserver.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import l2p.commons.dbcp.DbUtils;
import l2p.gameserver.Config;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.instancemanager.PlayerManager;
import l2p.gameserver.model.CharSelectInfo;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.tables.CharTemplateTable;
import l2p.gameserver.templates.L2PlayerTemplate;
import l2p.gameserver.utils.AutoBan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacterSelectionInfo extends L2GameServerPacket
{
	// d (SdSddddddddddffdQdddddddddddddddddddddddddddddddddddddddffdddchhd)
	private static Logger _log = LoggerFactory.getLogger(CharacterSelectionInfo.class);

	private String _loginName;

	private int _sessionId;

	private CharSelectInfo[] _characterPackages;

	public CharacterSelectionInfo(String loginName, int sessionId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo(loginName);
	}

	public CharSelectInfo[] getCharInfo()
	{
		return _characterPackages;
	}

	@Override
	protected final void writeImpl()
	{
		int size = _characterPackages != null ? _characterPackages.length : 0;

		writeD(size);
		int countCharacters = 0x07;
		writeD(countCharacters); // Максимальное количество персонажей на сервере
		writeC(countCharacters == size ? 0x01 : 0x00); // 0x00 - Разрешить, 0x01 - запретить. Разрешает или запрещает создание игроков
		writeC(0x01); // 0x00 = can't play, 1=can play free until level 85, 2=100% free play
		writeD(0x02); // 0x01 - Выводит окно, что нужно купить игру, что создавать более 2х чаров. 0х02 - обычное лобби.
		writeC(0x00); // 0x01 - Предлогает купить ПА.
		writeC(0x00); // Balthus Knight

		long lastAccess = -1L;
		int lastUsed = -1;
		for(int i = 0; i < size; i++)
			if(lastAccess < _characterPackages[i].getLastAccess() && _characterPackages[i].getDeleteTimer() <= 0)
			{
				lastAccess = _characterPackages[i].getLastAccess();
				lastUsed = i;
			}
		for(int i = 0; i < size; i++)
		{
			CharSelectInfo charInfoPackage = _characterPackages[i];
			int	augment = Math.max(0, charInfoPackage.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));

			writeS(charInfoPackage.getName());
			writeD(charInfoPackage.getCharId()); // ?
			writeS(_loginName);
			writeD(_sessionId);
			writeD(charInfoPackage.getClanId());
			writeD(0x00); // ??

			writeD(charInfoPackage.getSex());
			writeD(charInfoPackage.getRace());
			writeD(charInfoPackage.getBaseClassId());

			writeD(Config.REQUEST_ID); // active ??

			writeD(charInfoPackage.getX());
			writeD(charInfoPackage.getY());
			writeD(charInfoPackage.getZ());

			writeF(charInfoPackage.getCurrentHp());
			writeF(charInfoPackage.getCurrentMp());

			writeQ(charInfoPackage.getSp());
			writeQ(charInfoPackage.getExp());
			int lvl = charInfoPackage.getLevel();
			writeF(Experience.getExpPercent(lvl, charInfoPackage.getExp()));
			writeD(lvl);

			writeD(charInfoPackage.getKarma());
			writeD(charInfoPackage.getPk());
			writeD(charInfoPackage.getPvP());

			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);

			writeD(0x00); // unk Ertheia
			writeD(0x00); // unk Ertheia

			for(int paperdollId : Inventory.PAPERDOLL_ORDER_GK)
				writeD(charInfoPackage.getPaperdollItemId(paperdollId));

			writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_RHAND)); //Внешний вид оружия (ИД Итема).
			writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_LHAND)); //Внешний вид щита (ИД Итема).
			writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_GLOVES)); //Внешний вид перчаток (ИД Итема).
			writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_CHEST)); //Внешний вид верха (ИД Итема).
			writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_LEGS)); //Внешний вид низа (ИД Итема).
			writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_FEET)); //Внешний вид ботинок (ИД Итема).
			writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_BACK));
			writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_HAIR)); //Внешний вид шляпы (ИД итема).
			writeD(charInfoPackage.getPaperdollVisualId(Inventory.PAPERDOLL_DHAIR)); //Внешний вид маски (ИД итема).

			writeH(charInfoPackage.getPaperdollEnchantEffect(Inventory.PAPERDOLL_CHEST));
			writeH(charInfoPackage.getPaperdollEnchantEffect(Inventory.PAPERDOLL_LEGS));
			writeH(charInfoPackage.getPaperdollEnchantEffect(Inventory.PAPERDOLL_HEAD));
			writeH(charInfoPackage.getPaperdollEnchantEffect(Inventory.PAPERDOLL_GLOVES));
			writeH(charInfoPackage.getPaperdollEnchantEffect(Inventory.PAPERDOLL_FEET));

			writeD(charInfoPackage.getHairStyle());
			writeD(charInfoPackage.getHairColor());
			writeD(charInfoPackage.getFace());

			writeF(charInfoPackage.getMaxHp()); // hp max
			writeF(charInfoPackage.getMaxMp()); // mp max

			writeD(charInfoPackage.getAccessLevel() > -100 ? charInfoPackage.getDeleteTimer() : -1);
			writeD(charInfoPackage.getClassId());
			writeD(i == lastUsed ? 1 : 0);

			writeC(Math.min(charInfoPackage.getPaperdollEnchantEffect(Inventory.PAPERDOLL_RHAND), 127));
			writeQ(augment);

			final int weaponId = charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND);
			if(weaponId == 8190) // Transform id (на оффе отображаются только КВ трансформации или вообще не отображаются ;)
				writeD(301);
			else if(weaponId == 8689)
				writeD(302);
			else
				writeD(0);

			//TODO: Pet info?
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeF(0x00);
			writeF(0x00);

			writeD(charInfoPackage.getVitalityPoints());
			writeD(0); // Vitality
			writeD(0); // Vitality

			writeD(charInfoPackage.getAccessLevel() == -100 ? 0 : 1);
			writeC(0); // Chaos Festival
			writeC(charInfoPackage.getCurrentHero());
			writeC(0x01/*charInfoPackage.getHairAccessoryPriority()*/); // show hair accessory if enabled
		}
	}

	@Override
	protected final void writeImplIT()
	{
		int size = _characterPackages != null ? _characterPackages.length : 0;

		writeD(size);

		long lastAccess = -1L;
		int lastUsed = -1;
		for(int i = 0; i < size; i++)
			if(lastAccess < _characterPackages[i].getLastAccess() && _characterPackages[i].getDeleteTimer() <= 0)
			{
				lastAccess = _characterPackages[i].getLastAccess();
				lastUsed = i;
			}
		for(int i = 0; i < size; i++)
		{
			CharSelectInfo charInfoPackage = _characterPackages[i];
			int	augment = Math.max(0, charInfoPackage.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));

			writeS(charInfoPackage.getName());
			writeD(charInfoPackage.getCharId()); // ?
			writeS(_loginName);
			writeD(_sessionId);
			writeD(charInfoPackage.getClanId());
			writeD(0x00); // ??

			writeD(charInfoPackage.getSex());
			writeD(charInfoPackage.getRace());
			writeD(charInfoPackage.getBaseClassId());

			writeD(0x01); // active ??

			writeD(0x00);
			writeD(0x00);
			writeD(0x00);

			writeF(charInfoPackage.getCurrentHp());
			writeF(charInfoPackage.getCurrentMp());

			writeD(charInfoPackage.getSp());
			writeQ(charInfoPackage.getExp());
			writeD(charInfoPackage.getLevel());
			writeD(charInfoPackage.getKarma());

			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);

			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_UNDER));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_DHAIR));

			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_UNDER));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_REAR));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_NECK));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));

			writeD(charInfoPackage.getHairStyle());
			writeD(charInfoPackage.getHairColor());
			writeD(charInfoPackage.getFace());

			writeF(charInfoPackage.getMaxHp()); // hp max
			writeF(charInfoPackage.getMaxMp()); // mp max

			writeD(charInfoPackage.getAccessLevel() > -100 ? charInfoPackage.getDeleteTimer() : -1);
			writeD(charInfoPackage.getClassId());
			writeD(i == lastUsed ? 1 : 0);
			writeC(Math.min(charInfoPackage.getPaperdollEnchantEffect(Inventory.PAPERDOLL_RHAND), 127));
			writeD(augment);
		}
	}

	public static CharSelectInfo[] loadCharacterSelectInfo(String loginName)
	{
		CharSelectInfo charInfopackage;
		List<CharSelectInfo> characterList = new ArrayList<CharSelectInfo>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM characters AS c LEFT JOIN character_subclasses AS cs ON (c.obj_Id=cs.char_obj_id AND cs.active=1) WHERE account_name=? LIMIT 7");
			statement.setString(1, loginName);
			rset = statement.executeQuery();
			while(rset.next()) // fills the package
			{
				charInfopackage = restoreChar(rset);
				if(charInfopackage != null)
					characterList.add(charInfopackage);
			}
		}
		catch(Exception e)
		{
			_log.error("could not restore charinfo:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return characterList.toArray(new CharSelectInfo[characterList.size()]);
	}

	private static int restoreBaseClassId(int objId)
	{
		int classId = 0;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT class_id FROM character_subclasses WHERE char_obj_id=? AND isBase=1");
			statement.setInt(1, objId);
			rset = statement.executeQuery();
			while(rset.next())
				classId = rset.getInt("class_id");
		}
		catch(Exception e)
		{
			_log.error("could not restore base class id:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return classId;
	}

	private static CharSelectInfo restoreChar(ResultSet chardata)
	{
		CharSelectInfo charInfopackage = null;
		try
		{
			int objectId = chardata.getInt("obj_Id");
			int classid = chardata.getInt("class_id");
			int baseClassId = classid;
			boolean useBaseClass = chardata.getInt("isBase") > 0;
			if(!useBaseClass)
				baseClassId = restoreBaseClassId(objectId);
			boolean female = chardata.getInt("sex") == 1;
			L2PlayerTemplate templ = CharTemplateTable.getInstance().getTemplate(baseClassId, female);
			if(templ == null)
			{
				_log.warn("restoreChar fail | templ == null | objectId: " + objectId + " | classid: " + baseClassId + " | female: " + female);
				return null;
			}
			String name = chardata.getString("char_name");
			charInfopackage = new CharSelectInfo(objectId, name);
			charInfopackage.setLevel(chardata.getInt("level"));
			charInfopackage.setMaxHp(chardata.getInt("maxHp"));
			charInfopackage.setCurrentHp(chardata.getDouble("curHp"));
			charInfopackage.setMaxMp(chardata.getInt("maxMp"));
			charInfopackage.setCurrentMp(chardata.getDouble("curMp"));

			charInfopackage.setFace(chardata.getInt("face"));
			charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
			charInfopackage.setHairColor(chardata.getInt("haircolor"));
			charInfopackage.setSex(female ? 1 : 0);

			charInfopackage.setExp(chardata.getLong("exp"));
			charInfopackage.setSp(chardata.getInt("sp"));
			charInfopackage.setClanId(chardata.getInt("clanid"));

			charInfopackage.setX(chardata.getInt("x"));
			charInfopackage.setY(chardata.getInt("y"));
			charInfopackage.setZ(chardata.getInt("z"));
			charInfopackage.setKarma(chardata.getInt("karma"));
			charInfopackage.setPvP(chardata.getInt("pvpkills"));
			charInfopackage.setPk(chardata.getInt("pkkills"));
			charInfopackage.setRace(templ.race.ordinal());
			charInfopackage.setClassId(classid);
			charInfopackage.setBaseClassId(baseClassId);
			long deletetime = chardata.getLong("deletetime");
			int deletedays = 0;
			if(Config.DELETE_DAYS > 0)
				if(deletetime > 0)
				{
					deletetime = (int) (System.currentTimeMillis() / 1000 - deletetime);
					deletedays = (int) (deletetime / 3600 / 24);
					if(deletedays >= Config.DELETE_DAYS)
					{
						PlayerManager.deleteFromClan(objectId, charInfopackage.getClanId());
						PlayerManager.deleteCharByObjId(objectId, true);
						return null;
					}
					deletetime = Config.DELETE_DAYS * 3600 * 24 - deletetime;
				}
				else
					deletetime = 0;
			charInfopackage.setDeleteTimer((int) deletetime);
			charInfopackage.setLastAccess(chardata.getLong("lastAccess") * 1000L);
			charInfopackage.setAccessLevel(chardata.getInt("accesslevel"));

			if(charInfopackage.getAccessLevel() < 0 && !AutoBan.isBanned(objectId))
				charInfopackage.setAccessLevel(0);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}

		return charInfopackage;
	}
}