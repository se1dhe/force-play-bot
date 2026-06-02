package l2p.gameserver.serverpackets;

import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.ItemInfo;

public class TradeOwnAdd extends AbstractItemListPacket
{
	private int _type;
	private L2ItemInstance temp;
	private int _amount;

	public TradeOwnAdd(int type, L2ItemInstance x, int amount)
	{
		_type = type;
		temp = x;
		_amount = amount;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		if(_type == 1)
			writeD(1);	// Count
		else
		{
			writeD(1);	// Count
			writeD(1);
			writeItemInfo(new ItemInfo(temp, false), _amount);
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
		writeH(1); // item count

		writeH(temp.getItem().getType1()); // item type1
		writeD(temp.getObjectId());
		writeD(temp.getItemId());
		writeD(_amount);
		writeH(temp.getItem().getType2()); // item type2
		writeH(0x00); // ?

		writeD(temp.getBodyPart()); // rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
		writeH(temp.getEnchantLevel()); // enchant level
		writeH(0x00); // ?
		writeH(0x00);
	}
}