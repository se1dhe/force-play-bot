package l2p.gameserver.clientpackets;

import l2p.commons.lang.ArrayUtils;
import l2p.commons.util.GCSArray;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.AltRestartPointHolder;
import l2p.gameserver.data.xml.holder.FixedRestartPointHolder;
import l2p.gameserver.instancemanager.ZoneManager;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Zone;
import l2p.gameserver.model.L2Zone.ZoneType;
import l2p.gameserver.model.base.RestartType;
import l2p.gameserver.model.entity.events.GlobalEvent;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.entity.residence.ClanHall;
import l2p.gameserver.model.entity.residence.ResidenceFunction;
import l2p.gameserver.serverpackets.Die;
import l2p.gameserver.serverpackets.Revive;
import l2p.gameserver.tables.MapRegionTable;
import l2p.gameserver.tables.MapRegionTable.TeleportWhereType;
import l2p.gameserver.templates.AltRestartPointTemplate;
import l2p.gameserver.templates.FixedRestartPointTemplate;
import l2p.gameserver.utils.Location;

public class RequestRestartPoint extends L2GameClientPacket
{
	private RestartType _restartType;

	@Override
	protected void readImpl()
	{
		_restartType = ArrayUtils.valid(getClient().isITClient() ? RestartType.getValuesIt() : RestartType.VALUES, readD());
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(_restartType == null || activeChar == null)
			return;

		if(activeChar.isFakeDeath())
		{
			activeChar.breakFakeDeath();
			return;
		}

		if(!activeChar.isDead() || activeChar.isInOlympiadMode())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.noToTown)
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Недоступно в текущих условиях." : "You can't do it now.");
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFestivalParticipant())
		{
			activeChar.doRevive(true);
			return;
		}

		switch(_restartType)
		{
			case FIXED:
				FixedRestartPointTemplate fixedRestartPointsTemplate = getFixedZone(activeChar);
				if(activeChar.getPlayerAccess().ResurectFixed)
					activeChar.doRevive(100);
				else if(Config.ENABLE_FIXED_RESTART_POINT)
				{
					L2Zone zone = ZoneManager.getInstance().getZoneById(ZoneType.battle_zone, Config.ID_FIXED_RESPAWN_ZONE, false);
					if(activeChar.isInZone(zone))
					{
						activeChar.setPendingRevive(true);
						activeChar.teleToLocation(Rnd.get(Config.RESTART_FIXED_RND_LOCATION));
					}
					else if(fixedRestartPointsTemplate != null)
					{
						activeChar.setPendingRevive(true);
						activeChar.teleToLocation(fixedRestartPointsTemplate.getRndPoint());
					}
				}
				else if(fixedRestartPointsTemplate != null)
				{
					activeChar.setPendingRevive(true);
					activeChar.teleToLocation(fixedRestartPointsTemplate.getRndPoint());
				}
				else
					activeChar.sendPacket(Msg.ActionFail, new Die(activeChar));
				break;
			default:
				Location loc = null;

				for(GlobalEvent e : activeChar.getEvents())
					loc = e.getRestartLoc(activeChar, _restartType);

				if(loc == null)
					loc = defaultLoc(_restartType, activeChar);

				if(loc != null)
				{
					activeChar.broadcastPacket(new Revive(activeChar));
					activeChar.setPendingRevive(true);
					activeChar.teleToLocation(loc);
				}
				else
					activeChar.sendPacket(Msg.ActionFail, new Die(activeChar));
				break;
		}
	}

	private FixedRestartPointTemplate getFixedZone(L2Player player)
	{
		GCSArray<L2Zone> playerZones = player.getZones();
		if(!playerZones.isEmpty())
		{
			for(L2Zone zone : playerZones)
			{
				FixedRestartPointTemplate template = FixedRestartPointHolder.getInstance().getFixedRestartPointByZoneId(zone.getId());
				if(template != null)
					return template;
			}
		}
		return null;
	}

	// телепорт к флагу, не обрабатывается, по дефалту
	public static Location defaultLoc(RestartType restartType, L2Player activeChar)
	{
		Location loc = null;
		L2Clan clan = activeChar.getClan();

		switch(restartType)
		{
			case TO_CLANHALL:
				if(clan != null && clan.getHasHideout() != 0)
				{
					ClanHall clanHall = activeChar.getClanHall();
					loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.ClanHall);
					if(clanHall.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(clanHall.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
				}
				break;
			case TO_CASTLE:
				if(clan != null && clan.getHasCastle() != 0)
				{
					Castle castle = activeChar.getCastle();
					loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Castle);
					if(castle.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(castle.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
				}
				break;
			case TO_FLAG:
				if(Config.ALLOW_PVP_ZONES_MOD && org.apache.commons.lang3.ArrayUtils.contains(Config.PVP_ZONES_MOD, activeChar.getZoneIndex(ZoneType.battle_zone)))
				{
					L2Zone battle = activeChar.getZone(L2Zone.ZoneType.battle_zone);
					if(battle != null)
						loc = battle.getAdvSpawn();
				}
				break;
			case TO_VILLAGE:
			default:
				AltRestartPointTemplate altRestartPointsTemplate = getAltRestartTemplateZone(activeChar);
				if(altRestartPointsTemplate != null)
					loc = altRestartPointsTemplate.getRndPoint();
				else
					loc = MapRegionTable.getTeleToClosestTown(activeChar);
				break;
		}
		return loc;
	}

	private static AltRestartPointTemplate getAltRestartTemplateZone(L2Player player)
	{
		GCSArray<L2Zone> playerZones = player.getZones();
		if(!playerZones.isEmpty())
		{
			for(L2Zone zone : playerZones)
			{
				AltRestartPointTemplate template = AltRestartPointHolder.getInstance().getAltRestartPointByZoneId(zone.getId());
				if(template != null)
					return template;
			}
		}
		return null;
	}
}