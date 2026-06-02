package l2p.gameserver.serverpackets;

import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2PetInstance;
import l2p.gameserver.model.items.ItemInfo;

public class PetItemList extends AbstractItemListPacket
{
	private L2ItemInstance[] items;

	public PetItemList(L2PetInstance cha)
	{
		items = cha.getInventory().getItems();
	}

	@Override
	protected final void writeImpl()
	{
		writeH(items.length);

		for(L2ItemInstance temp : items)
			writeItemInfo(new ItemInfo(temp, false));
	}

	@Override
	protected final void writeImplIT()
	{
		writeH(items.length);

		for(L2ItemInstance temp : items)
		{
			writeH(temp.getItem().getType1()); // item type1
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getIntegerLimitedCount());
			writeH(temp.getItem().getType2()); // item type2
			writeH(0xff); // ?
			writeH(temp.isEquipped() ? 1 : 0);
			writeD(temp.getBodyPart()); // rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
			writeH(temp.getEnchantLevel()); // enchant level
			writeH(0x00); // ?
		}
	}
}