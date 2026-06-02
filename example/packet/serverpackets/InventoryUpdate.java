package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.ItemInfo;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class InventoryUpdate extends AbstractItemListPacket
{
	private final List<ItemInfo> _items = Collections.synchronizedList(new Vector<ItemInfo>());

	public InventoryUpdate()
	{}

	public InventoryUpdate(GArray<L2ItemInstance> items)
	{
		for(L2ItemInstance item : items)
			_items.add(new ItemInfo(item, false));
	}

	public InventoryUpdate addNewItem(L2ItemInstance item)
	{
		item.setLastChange(L2ItemInstance.ADDED);
		_items.add(new ItemInfo(item, false));
		return this;
	}

	public InventoryUpdate addModifiedItem(L2ItemInstance item, boolean oe)
	{
		item.setLastChange(L2ItemInstance.MODIFIED);
		_items.add(new ItemInfo(item, oe));
		return this;
	}

	public InventoryUpdate addRemovedItem(L2ItemInstance item)
	{
		item.setLastChange(L2ItemInstance.REMOVED);
		_items.add(new ItemInfo(item, false));
		return this;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0); // 140
		writeD(_items.size()); // 140
		writeD(_items.size()); // 140
		for(ItemInfo temp : _items)
		{
			writeH(temp.getLastChange());
			writeItemInfo(temp);
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeH(_items.size());
		for(ItemInfo temp : _items)
		{
			writeH(temp.getLastChange());
			writeH(temp.getType1()); // item type1
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getType2()); // item type2
			writeH(temp.getCustomType1());
			writeH(temp.isEquipped() ? 1 : 0);
			writeD(temp.getBodyPart()); // rev 415   slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
			writeH(temp.getEnchantLevel()); // enchant level or pet level
			writeH(temp.getCustomType2()); // Pet name exists or not shown in control item
			writeD(temp.getAugmentationId());
			writeD(temp.getShadowLifeTime()); //interlude FF FF FF FF
		}
	}
}