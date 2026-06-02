package l2p.gameserver.serverpackets;

import java.util.NoSuchElementException;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2ItemInstance.ItemClass;
import l2p.gameserver.model.items.ItemInfo;
import l2p.gameserver.model.items.Warehouse.WarehouseType;
import l2p.gameserver.templates.L2Item;

public class WareHouseWithdrawList extends AbstractItemListPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2; // final - 4?
	public static final int CASTLE = 3;
	public static final int FREIGHT = 4; // final - 1?

	private int _type;
	private int _money;
	private L2ItemInstance[] _items;
	private int _whType;
	private int _inventoryUsedSlots;
	private boolean can_writeImpl = false;

	public WareHouseWithdrawList(int type, L2Player cha, WarehouseType whType, ItemClass clss)
	{
		if(cha == null)
			return;

		_type = type;
		_money = cha.getAdena();
		_whType = whType.getPacketValue();
		cha.setUsingWarehouseType(whType);
		switch(whType)
		{
			case PRIVATE:
				_items = cha.getWarehouse().listItems(clss);
				break;
			case CLAN:
			case CASTLE:
				_items = cha.getClan().getWarehouse().listItems(clss);
				break;
			/*
			 case CASTLE:
			 items = _cha.getClan().getCastleWarehouse().listItems();
			 break;
			 */
			case FREIGHT:
				_items = cha.getFreight().listItems(clss);
				break;
			default:
				throw new NoSuchElementException("Invalid value of 'type' argument");
		}

		if(_items.length == 0)
		{
			cha.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_NOT_DEPOSITED_ANY_ITEMS_IN_YOUR_WAREHOUSE));
			return;
		}

		_inventoryUsedSlots = cha.getInventory().getSize();

		can_writeImpl = true;
	}

	@Override
	protected boolean canWrite()
	{
		if(!can_writeImpl)
			return false;
		return true;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		if(_type == 1)
		{
			if(_whType == WarehouseType.FREIGHT.ordinal())
				writeH(1);
			else
				writeH(_whType);

			writeQ(_money);
			writeD(_inventoryUsedSlots); //Количество занятых ячеек в инвентаре.
			writeD(_items.length);
		}
		else if(_type == 2)
		{
			writeH(0x00);
			writeD(_items.length);
			writeD(_items.length);
			for(L2ItemInstance item : _items)
			{
				writeItemInfo(new ItemInfo(item, false));
				writeD(item.getObjectId());
				writeD(0);
				writeD(0);
			}
		}
	}

	@Override
	protected boolean canWriteIT()
	{
		if(!can_writeImpl)
			return false;
		return _type == 2;
	}

	@Override
	protected final void writeImplIT()
	{
		writeH(_whType);
		writeD(_money);
		writeH(_items.length);
		for(L2ItemInstance temp : _items)
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
			writeH(0); // ?
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