package l2p.gameserver.serverpackets;

import java.util.HashMap;
import java.util.Map;

import l2p.commons.util.GCSArray;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.FixedRestartPointHolder;
import l2p.gameserver.instancemanager.ZoneManager;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Zone;
import l2p.gameserver.model.L2Zone.ZoneType;
import l2p.gameserver.model.base.RestartType;
import l2p.gameserver.model.entity.events.GlobalEvent;
import l2p.gameserver.model.instances.L2MonsterInstance;
import l2p.gameserver.templates.FixedRestartPointTemplate;
import org.apache.commons.lang3.ArrayUtils;

public class Die extends L2GameServerPacket
{
	private int _objectId;
	private boolean _fake;
	private boolean _sweepable;

	private Map<RestartType, Boolean> _types = new HashMap<RestartType, Boolean>();

	public Die(L2Character cha)
	{
		_objectId = cha.getObjectId();
		_fake = !cha.isDead();

		if(cha.isMonster())
			_sweepable = ((L2MonsterInstance) cha).isSweepActive();
		else if(cha.isPlayer())
		{
			L2Player player = (L2Player) cha;
			put(RestartType.FIXED, checkFixedRestart(player));
			put(RestartType.TO_VILLAGE, true);

			L2Clan clan = null;
			if(get(RestartType.TO_VILLAGE))
				clan = player.getClan();
			if(clan != null)
			{
				put(RestartType.TO_CLANHALL, clan.getHasHideout() > 0);
				put(RestartType.TO_CASTLE, clan.getHasCastle() > 0);
			}

			for(GlobalEvent e : cha.getEvents())
				e.checkRestartLocs(player, _types);
			if(Config.ALLOW_PVP_ZONES_MOD && ArrayUtils.contains(Config.PVP_ZONES_MOD, player.getZoneIndex(ZoneType.battle_zone)))
				put(RestartType.TO_FLAG, true);
		}
	}

	private boolean checkFixedRestart(L2Player player)
	{
		if(player.getPlayerAccess().ResurectFixed)
			return true;
		else if(Config.ENABLE_FIXED_RESTART_POINT)
		{
			L2Zone zone = ZoneManager.getInstance().getZoneById(ZoneType.battle_zone, Config.ID_FIXED_RESPAWN_ZONE, false);
			if(player.isInZone(zone))
			{
				return true;
			}
			else if(inFixedZone(player))
			{
				return true;
			}
		}
		else if(inFixedZone(player))
		{
			return true;
		}

		return false;
	}

	private boolean inFixedZone(L2Player player)
	{
		GCSArray<L2Zone> playerZones = player.getZones();
		if(playerZones.size() > 0)
		{
			for(L2Zone zone : playerZones)
			{
				FixedRestartPointTemplate template = FixedRestartPointHolder.getInstance().getFixedRestartPointByZoneId(zone.getId());
				if(template != null)
					return true;
			}
		}
		return false;
	}

	@Override
	protected boolean canWrite()
	{
		if(_fake)
			return false;
		return true;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(get(RestartType.TO_VILLAGE) ? 0x01 : 0x00); // to nearest village
		writeD(get(RestartType.TO_CLANHALL) ? 0x01 : 0x00); // to hide away
		writeD(get(RestartType.TO_CASTLE) ? 0x01 : 0x00); // to castle
		writeD(get(RestartType.TO_FLAG) ? 0x01 : 0x00);// to siege HQ
		writeD(_sweepable ? 0x01 : 0x00); // sweepable  (blue glow)
		writeD(get(RestartType.FIXED) ? 0x01 : 0x00);// FIXED
		writeD(0x00/*get(RestartType.TO_FORTRESS)*/);
		writeD(0x00);
		writeD(0x00/*get(RestartType.ADVENTURES_SONG)*/);
		writeC(0x00); // _hideDieAnimation
		writeD(0x00/*get(RestartType.AGATHION)*/);//agathion ress button
		writeD(0x00);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_objectId);
		writeD(0x01); // to nearest village
		writeD(get(RestartType.TO_CLANHALL) ? 0x01 : 0x00); // to hide away
		writeD(get(RestartType.TO_CASTLE) ? 0x01 : 0x00); // to castle
		writeD(get(RestartType.TO_FLAG) ? 0x01 : 0x00);// to siege HQ
		writeD(_sweepable ? 0x01 : 0x00); // sweepable  (blue glow)
		writeD(get(RestartType.FIXED) ? 0x01 : 0x00);// FIXED
	}

	private void put(RestartType t, boolean b)
	{
		_types.put(t, b);
	}

	private boolean get(RestartType t)
	{
		Boolean b = _types.get(t);
		return b != null && b;
	}
}