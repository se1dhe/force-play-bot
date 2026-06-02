package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.ItemInfo;

import java.util.List;

/**
 * @author VISTALL
 * @date 4:20/06.05.2011
 */
public class ExGMViewQuestItemList extends AbstractItemListPacket
{
	private int _size;
	private List<ItemInfo> _items;

	private int _limit;
	private String _name;

	public ExGMViewQuestItemList(L2Player player, List<ItemInfo> items, int size)
	{
		_items = items;
		_size = size;
		_name = player.getName();
		_limit = player.getQuestInventoryMaximum();
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_name);
		writeD(_limit);
		writeH(_size);
		for(ItemInfo temp : _items)
			if(temp.getItem().isQuest())
				writeItemInfo(temp);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}
