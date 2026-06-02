package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.tables.ClanTable;

public class ExShowCastleInfo extends L2GameServerPacket
{
	private List<CastleInfo> _infos = Collections.emptyList();

	public ExShowCastleInfo()
	{
		String ownerName;
		int id, tax, nextSiege;

		List<Castle> castles = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		_infos = new ArrayList<CastleInfo>(castles.size());
		for(Castle castle : castles)
		{
			ownerName = ClanTable.getInstance().getClanName(castle.getOwnerId());
			id = castle.getId();
			tax = castle.getTaxPercent();
			nextSiege = castle.getSiegeEvent() != null ? (int) (castle.getSiegeDate().getTimeInMillis() / 1000) : 0;
			_infos.add(new CastleInfo(ownerName, id, tax, nextSiege, castle.getSiegeEvent().isInProgress()));
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_infos.size());
		for(CastleInfo info : _infos)
		{
			writeD(info._id);
			writeS(info._ownerName);
			writeD(info._tax);
			writeD(info._nextSiege);
			writeC(info._siegeInProgress ? 1 : 0);
			writeC(0);
		}
		_infos.clear();
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}

	private static class CastleInfo
	{
		public String _ownerName;
		public int _id, _tax, _nextSiege;
		public boolean _siegeInProgress;

		public CastleInfo(String ownerName, int id, int tax, int nextSiege, boolean siegeInProgress)
		{
			_ownerName = ownerName;
			_id = id;
			_tax = tax;
			_nextSiege = nextSiege;
			_siegeInProgress = siegeInProgress;
		}
	}
}