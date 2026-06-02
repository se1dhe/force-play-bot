package l2p.gameserver.serverpackets;

import java.util.Map;

import javolution.util.FastMap;
import l2p.commons.util.GArray;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.instancemanager.CastleManorManager.CropProcure;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.instances.L2ItemInstance;

public class SellListProcure extends L2GameServerPacket
{
	private int _money;
	private Map<L2ItemInstance, Integer> _sellList = new FastMap<L2ItemInstance, Integer>();
	private GArray<CropProcure> _procureList = new GArray<CropProcure>();
	private int _castle;

	public SellListProcure(L2Player player, int castleId)
	{
		_money = player.getAdena();
		_castle = castleId;
		_procureList = ResidenceHolder.getInstance().getResidence(Castle.class, _castle).getCropProcure(0);
		for(CropProcure c : _procureList)
		{
			L2ItemInstance item = player.getInventory().getItemByItemId(c.getId());
			if(item != null && c.getAmount() > 0)
				_sellList.put(item, c.getAmount());
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_money); // money
		writeQ(0x00); // lease ?
		writeH(_sellList.size()); // list size

		for(L2ItemInstance item : _sellList.keySet())
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeQ(_sellList.get(item)); // count
			writeH(item.getItem().getType2());
			writeH(0); // unknown
			writeQ(0); // price, u shouldnt get any adena for crops, only raw materials
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_money); // money
		writeD(0x00); // lease ?
		writeH(_sellList.size()); // list size

		for(L2ItemInstance item : _sellList.keySet())
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(_sellList.get(item)); // count
			writeH(item.getItem().getType2());
			writeH(0); // unknown
			writeD(0); // price, u shouldnt get any adena for crops, only raw materials
		}
	}
}
