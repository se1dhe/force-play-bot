package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.dao.SiegeClanDAO;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2p.gameserver.model.entity.events.objects.SiegeClanObject;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.serverpackets.CastleSiegeDefenderList;

public class RequestConfirmSiegeWaitingList extends L2GameClientPacket
{
	private boolean _approved;
	private int _unitId;
	private int _clanId;

	@Override
	protected void readImpl()
	{
		_unitId = readD();
		_clanId = readD();
		_approved = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.getClan() == null)
			return;

		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _unitId);

		if(castle == null || player.getClan().getHasCastle() != castle.getId())
		{
			player.sendActionFailed();
			return;
		}

		CastleSiegeEvent siegeEvent = castle.getSiegeEvent();

		SiegeClanObject siegeClan = siegeEvent.getSiegeClan(CastleSiegeEvent.DEFENDERS_WAITING, _clanId);
		if(siegeClan == null)
			siegeClan = siegeEvent.getSiegeClan(CastleSiegeEvent.DEFENDERS, _clanId);

		if(siegeClan == null)
			return;

		if((player.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) != L2Clan.CP_CS_MANAGE_SIEGE)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_CASTLE_DEFENDER_LIST);
			return;
		}

		if(siegeEvent.isRegistrationOver())
		{
			player.sendPacket(Msg.THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATIONS_CANNOT_BE_ACCEPTED_OR_REJECTED);
			return;
		}

		int allSize = siegeEvent.getObjects(CastleSiegeEvent.DEFENDERS).size();
		if(allSize >= 20)
		{
			player.sendPacket(Msg.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_DEFENDER_SIDE);
			return;
		}

		siegeEvent.removeObject(siegeClan.getType(), siegeClan);

		if(_approved)
			siegeClan.setType(CastleSiegeEvent.DEFENDERS);
		else
			siegeClan.setType(CastleSiegeEvent.DEFENDERS_REFUSED);

		siegeEvent.addObject(siegeClan.getType(), siegeClan);

		SiegeClanDAO.getInstance().update(castle, siegeClan);

		player.sendPacket(new CastleSiegeDefenderList(castle));
	}
}