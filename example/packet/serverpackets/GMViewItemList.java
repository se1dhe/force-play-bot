package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.items.ItemInfo;

import java.util.List;

public class GMViewItemList extends AbstractItemListPacket
{
	private int _type;
	private List<ItemInfo> _items;
	private L2Player _player;

	public GMViewItemList(int type, L2Player cha, List<ItemInfo> items)
	{
		_type = type;
		_items = items;
		_player = cha;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		if(_type == 1)
		{
			writeS(_player.getName());
			writeD(_player.getInventoryLimit());
			writeD(_items.size());
		}
		else if(_type == 2)
		{
			writeD(_items.size());
			writeD(_items.size());
			for(ItemInfo itemInfo : _items)
			{
				if(!itemInfo.getItem().isQuest())
					writeItemInfo(itemInfo);
			}
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeS(_player.getName());
		writeD(_player.getInventoryLimit()); //c4?
		writeH(1); // show window ??

		writeH(_items.size());

		for(ItemInfo temp : _items)
		{
			writeH(temp.getItem().getType1()); // item type1
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2()); // item type2
			writeH(temp.getCustomType1()); // ?
			writeH(temp.isEquipped() ? 1 : 0);
			writeD(temp.getBodyPart()); // rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
			writeH(temp.getEnchantLevel()); // enchant level
			writeH(temp.getCustomType2()); // ?
			writeD(temp.getAugmentationId());
			writeD(temp.getItem().isShadowItem() ? temp.getShadowLifeTime() : -1); //interlude
		}
	}
}