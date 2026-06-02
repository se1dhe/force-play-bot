package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.ItemInfo;

public class ItemList extends AbstractItemListPacket
{
	private final int _type;
	private final L2ItemInstance[] _items;
	private final boolean _showWindow;
	private final boolean _oe;

	public ItemList(int type, L2Player cha, boolean showWindow)
	{
		_type = type;
		_items = cha.getInventory().getItems();
		_showWindow = showWindow;
		_oe = cha.isEnchantLimit();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		if(_type == 1)
		{
			writeH(_showWindow ? 1 : 0);
			writeH(0);
			writeD(_items.length); // Total items
		}
		else if(_type == 2)
		{
			writeD(_items.length);	// Total items
			writeD(_items.length);	// Items in this page
			for(L2ItemInstance temp : _items)
			{
				if(temp.getItem().isQuest())
					continue;

				writeItemInfo(new ItemInfo(temp, false));
			}
		}
	}

	@Override
	protected boolean canWriteIT()
	{
		return _type == 1;
	}

	@Override
	protected final void writeImplIT()
	{
		writeH(_showWindow ? 1 : 0);

		writeH(_items.length);
		for(L2ItemInstance temp : _items)
		{
			writeH(temp.getItem().getType1()); // item type1
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getIntegerLimitedCount());
			writeH(temp.getItem().getType2()); // item type2
			writeH(temp.getCustomType1()); // item type3
			writeH(temp.isEquipped() ? 1 : 0);
			writeD(temp.getBodyPart()); // rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
			writeH(_oe ? temp.getEnchantLevel2() : temp.getEnchantLevel()); // enchant level
			writeH(temp.getCustomType2()); // item type3
			writeD(temp.getAugmentationId());
			writeD(temp.isShadowItem() ? temp.getLifeTimeRemaining() : -1);
		}
	}
}