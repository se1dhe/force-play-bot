package l2p.gameserver.serverpackets;

import java.util.ArrayList;

import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.ItemInfo;

public class PetInventoryUpdate extends AbstractItemListPacket
{
	private ArrayList<L2ItemInstance> _items;

	public PetInventoryUpdate()
	{
		_items = new ArrayList<L2ItemInstance>();
	}

	public PetInventoryUpdate(ArrayList<L2ItemInstance> items)
	{
		_items = items;
	}

	public PetInventoryUpdate addNewItem(L2ItemInstance item)
	{
		item.setLastChange(L2ItemInstance.ADDED);
		_items.add(item);
		return this;
	}

	public PetInventoryUpdate addModifiedItem(L2ItemInstance item)
	{
		item.setLastChange(L2ItemInstance.MODIFIED);
		_items.add(item);
		return this;
	}

	public PetInventoryUpdate addRemovedItem(L2ItemInstance item)
	{
		item.setLastChange(L2ItemInstance.REMOVED);
		_items.add(item);
		return this;
	}

	// TODO [V] - нужно?
	@Override
	protected final void writeImpl()
	{
		int count = _items.size();
		writeC(0); // 140
		writeD(count); // 140
		writeD(count); // 140
		for(L2ItemInstance temp : _items)
		{
			writeH(temp.getLastChange());
			writeItemInfo(new ItemInfo(temp, false));
		}
	}

	@Override
	protected final void writeImplIT()
	{
		int count = _items.size();
		writeH(count);
		for(L2ItemInstance temp : _items)
		{
			writeH(temp.getLastChange());
			writeH(temp.getItem().getType1()); // item type1
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getIntegerLimitedCount());
			writeH(temp.getItem().getType2()); // item type2
			writeH(0x00); // ?
			writeH(temp.isEquipped() ? 1 : 0);
			writeD(temp.getBodyPart()); // rev 415   slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
			writeH(temp.getEnchantLevel()); // enchant level
			writeH(0x00);
		}
	}
}