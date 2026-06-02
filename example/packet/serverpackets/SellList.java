package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.Inventory;

import java.util.TreeSet;

public class SellList extends L2GameServerPacket
{
	private int _money;
	private final TreeSet<L2ItemInstance> _selllist;

	public SellList(L2Player player)
	{
		_money = player.getAdena();

		_selllist = new TreeSet<L2ItemInstance>(Inventory.OrderComparator);
		for(L2ItemInstance item : player.getInventory().getItems())
			if(item.getItem().isSellable() && item.canBeTraded(player))
				_selllist.add(item);
	}

	@Override
	protected boolean canWrite()
	{
		return false;
	}

	@Override
	protected boolean canWriteIT()
	{
		return true;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_money);
		writeD(0x00);
		writeH(_selllist.size());

		for(L2ItemInstance item : _selllist)
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getIntegerLimitedCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeD(item.getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getCustomType2());
			writeH(0x00); // unknown
			writeD((int)(item.getItem().getReferencePrice() * Config.SELL_MOD));
		}
	}
}