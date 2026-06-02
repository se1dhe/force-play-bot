package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2ItemInstance.ItemClass;
import l2p.gameserver.model.items.ItemInfo;
import l2p.gameserver.templates.L2Weapon;

public class GMViewWarehouseWithdrawList extends AbstractItemListPacket
{
	private final int _type;
	private final L2ItemInstance[] _items;
	private final String _charName;
	private final int _money;

	public GMViewWarehouseWithdrawList(int type, L2Player cha)
	{
		_type = type;
		_charName = cha.getName();
		_money = cha.getAdena();
		_items = cha.getWarehouse().listItems(ItemClass.ALL);
	}

	public GMViewWarehouseWithdrawList(int type, L2Clan clan)
	{
		_type = type;
		_charName = clan.getLeaderName();
		_money = (int) clan.getAdenaCount();
		_items = clan.getWarehouse().listItems(ItemClass.ALL);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		if(_type == 1)
		{
			writeS(_charName);
			writeQ(_money);
			writeD(_items.length);
		}
		else if(_type == 2)
		{
			writeD(_items.length);
			writeD(_items.length);
			for(L2ItemInstance temp : _items)
			{
				writeItemInfo(new ItemInfo(temp, false));
				writeD(temp.getObjectId());
			}
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeS(_charName);
		writeD(_money);
		writeH(_items.length);

		for(L2ItemInstance temp : _items)
		{
			writeH(temp.getItem().getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getIntegerLimitedCount());
			writeH(temp.getItem().getType2ForPackets());
			writeH(temp.getCustomType1());
			if(temp.getItem().isEquipable())
			{
				writeD(temp.getItem().getBodyPart());
				writeH(temp.getEnchantLevel());
				writeH(temp.isWeapon() ? ((L2Weapon) temp.getItem()).getSoulShotCount() : 0);
				writeH(temp.isWeapon() ? ((L2Weapon) temp.getItem()).getSpiritShotCount() : 0);
			}
			writeD(temp.getObjectId());
			if(temp.getItem().isEquipable())
			{
				writeD(temp.isWeapon() && temp.isAugmented() ? 0x0000FFFF & temp.getAugmentation().getAugmentationId() : 0);
				writeD(temp.isWeapon() && temp.isAugmented() ? temp.getAugmentation().getAugmentationId() >> 16 : 0);
			}
		}
	}
}