package l2p.gameserver.serverpackets;

import l2p.gameserver.model.instances.L2ItemInstance;

public class ExPutItemResultForVariationCancel extends L2GameServerPacket
{
	private final int _itemObjId;
	private final int _itemId;
	private final int _itemAug1;
	private final int _itemAug2;
	private final int _price;

	public ExPutItemResultForVariationCancel(L2ItemInstance item, int price)
	{
		_itemObjId = item.getObjectId();
		_itemId = item.getItemId();
		_price = price;
		_itemAug1 = (short) item.getAugmentation().getAugmentationId();
		_itemAug2 = item.getAugmentation().getAugmentationId() >> 16;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_itemObjId);
		writeD(_itemId);
		writeD(_itemAug1);
		writeD(_itemAug2);
		writeQ(_price);
		writeD(0x01);
	}
}