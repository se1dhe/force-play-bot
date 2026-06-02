package l2p.gameserver.serverpackets;

import java.util.TreeSet;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.model.items.ItemInfo;
import l2p.gameserver.model.items.Warehouse.WarehouseType;
import l2p.gameserver.templates.L2Item;

public class WareHouseDepositList extends AbstractItemListPacket
{
	private final int _type;
	private int _whtype;
	private int char_adena;
	private TreeSet<L2ItemInstance> _itemslist = new TreeSet<L2ItemInstance>(Inventory.OrderComparator);
	private int _depositedItemsCount;

	public WareHouseDepositList(int type, L2Player cha, WarehouseType whtype)
	{
		cha.setUsingWarehouseType(whtype);
		_type = type;
		_whtype = whtype.getPacketValue();
		char_adena = cha.getAdena();
		switch(whtype)
		{
			case PRIVATE:
				_depositedItemsCount = cha.getWarehouse().getSize();
				break;
			case FREIGHT:
				_depositedItemsCount = cha.getFreight().getSize();
				break;
			case CLAN:
				_depositedItemsCount = cha.getClan().getWarehouse().getSize();
				break;
		}
		for(L2ItemInstance item : cha.getInventory().getItems())
			if(item != null && item.canBeStored(cha, _whtype == 1))
				_itemslist.add(item);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		writeH(_whtype);
		if(_type == 1)
		{
			writeQ(char_adena);

			writeH(_depositedItemsCount); //Количество вещей которые уже есть в банке.
			writeD(0x00);
			writeD(0x00);
		}
		else if(_type == 2)
		{
			writeH(0);//TODO [Bonux]
			writeD(_itemslist.size());
			for(L2ItemInstance item : _itemslist)
			{
				writeItemInfo(new ItemInfo(item, false));
				writeD(item.getObjectId());
			}
		}
	}

	@Override
	protected boolean canWriteIT()
	{
		return _type == 2;
	}

	@Override
	protected final void writeImplIT()
	{
		writeH(_whtype);
		writeD(char_adena);
		writeH(_itemslist.size());
		for(L2ItemInstance temp : _itemslist)
		{
			L2Item item = temp.getItem();
			writeH(item.getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getIntegerLimitedCount());
			writeH(item.getType2());
			writeH(temp.getCustomType1());
			writeD(temp.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeH(0x00); // ? 200
			writeD(temp.getObjectId()); // return value for define item (object_id)
			if(temp.isAugmented())
			{
				writeD(temp.getAugmentationId() & 0x0000FFFF);
				writeD(temp.getAugmentationId() >> 16);
			}
			else
				writeQ(0x00);
		}
	}
}