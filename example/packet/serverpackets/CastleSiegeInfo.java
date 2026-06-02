package l2p.gameserver.serverpackets;

import java.util.Calendar;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import l2p.gameserver.model.L2Alliance;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.entity.residence.ClanHall;
import l2p.gameserver.model.entity.residence.Residence;

public class CastleSiegeInfo extends L2GameServerPacket
{
	private long _startTime;
	private int _id, _ownerObjectId, _allyId;
	private boolean _isLeader;
	private String _ownerName = "NPC";
	private String _leaderName = StringUtils.EMPTY;
	private String _allyName = StringUtils.EMPTY;
	private int[] _nextTimeMillis = ArrayUtils.EMPTY_INT_ARRAY;

	public CastleSiegeInfo(Castle castle, L2Player player)
	{
		this((Residence)castle, player);

		CastleSiegeEvent siegeEvent = castle.getSiegeEvent();
		long siegeTimeMillis = castle.getSiegeDate().getTimeInMillis();
		if(siegeTimeMillis == 0)
			_nextTimeMillis = siegeEvent.getNextSiegeTimes();
		else
			_startTime = (int)(siegeTimeMillis / 1000);
	}

	public CastleSiegeInfo(ClanHall ch, L2Player player)
	{
		this((Residence)ch, player);

		_startTime = (int)(ch.getSiegeDate().getTimeInMillis() / 1000);
	}

	protected CastleSiegeInfo(Residence residence, L2Player player)
	{
		_id = residence.getId();
		_ownerObjectId = residence.getOwnerId();
		L2Clan owner = residence.getOwner();
		if(owner != null)
		{
			_isLeader = player.isGM() || (_ownerObjectId == player.getClanId() && player.isClanLeader());
			_ownerName = owner.getName();
			_leaderName = owner.getLeaderName();
			L2Alliance ally = owner.getAlliance();
			if(ally != null)
			{
				_allyId = ally.getAllyId();
				_allyName = ally.getAllyName();
			}
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_id);
		writeD(_isLeader ? 0x01 : 0x00);
		writeD(_ownerObjectId);
		writeS(_ownerName); // Clan Name
		writeS(_leaderName); // Clan Leader Name
		writeD(_allyId); // Ally ID
		writeS(_allyName); // Ally Name
		writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000));
		writeD((int) _startTime);
		if(_startTime == 0) //если ноль то идет цыкл
			writeDD(_nextTimeMillis, true);
	}
}