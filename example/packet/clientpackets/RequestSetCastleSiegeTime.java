package l2p.gameserver.clientpackets;

import java.nio.BufferUnderflowException;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.serverpackets.CastleSiegeInfo;

public class RequestSetCastleSiegeTime extends L2GameClientPacket
{
	private int _id, _time;
	private boolean _fail;

	@Override
	protected void readImpl()
	{
		try
		{
			_id = readD();
			_time = readD();
		}
		catch (BufferUnderflowException e)
		{
			_fail = true;
		}
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(_fail)
		{
			player.sendActionFailed();
			return;
		}

		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _id);
		if(castle == null)
			return;

		if(player.getClan().getHasCastle() != castle.getId())
			return;

		if((player.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) != L2Clan.CP_CS_MANAGE_SIEGE)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_SIEGE_TIME);
			return;
		}

		CastleSiegeEvent siegeEvent = castle.getSiegeEvent();

		siegeEvent.setNextSiegeByOwner(_time);

		player.sendPacket(new CastleSiegeInfo(castle, player));
	}
}