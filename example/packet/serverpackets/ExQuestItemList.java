package l2p.gameserver.serverpackets;

import l2p.gameserver.model.items.ItemInfo;

import java.util.List;

/**
 * @author VISTALL
 * @date 1:02/23.02.2011
 */
public class ExQuestItemList extends AbstractItemListPacket
{
	private int _type;
	private int _size;
	private List<ItemInfo> _items;

	public ExQuestItemList(int type, int size, List<ItemInfo> items)
	{
		_type = type;
		_size = size;
		_items = items;
	}

	@Override
	protected void writeImpl()
	{
		writeC(_type);
		if(_type == 1)
		{
			writeH(0x00);
			writeD(_size); // Total items
		}
		else if(_type == 2)
		{
			writeD(_size); // Total items
			writeD(_size); // Items in this page
			for(ItemInfo temp : _items)
			{
				if(!temp.getItem().isQuest())
					continue;

				writeItemInfo(temp);
			}
		}
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}
