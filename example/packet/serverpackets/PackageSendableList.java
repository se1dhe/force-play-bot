package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.ItemInfo;
import l2p.gameserver.templates.L2Item;

public class PackageSendableList extends AbstractItemListPacket
{
	private int _type;
	private int player_obj_id;
	private int char_adena;
	private GArray<L2ItemInstance> _itemslist = new GArray<L2ItemInstance>();

	public PackageSendableList(int type, L2Player cha, int playerObjId)
	{
		_type = type;
		player_obj_id = playerObjId;
		char_adena = cha.getAdena();
		for(L2ItemInstance item : cha.getInventory().getItems())
		{
			if(item != null)
			{
				if(cha.isITClient())
				{
					if(item.canBeFreighted(cha))
						_itemslist.add(item);
				}
				else
				{
					if(item.getItem().isFreightable())
						_itemslist.add(item);
				}
			}
		}
	}

	@Override
	protected boolean canWrite()
	{
		if(player_obj_id == 0)
			return false;
		return true;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		if(_type == 1)
		{
			writeD(player_obj_id);
			writeQ(char_adena);
			writeD(_itemslist.size());
		}
		else if(_type == 2)
		{
			writeD(_itemslist.size());
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
		if(player_obj_id == 0)
			return false;
		return _type == 2;
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(player_obj_id);
		writeD(char_adena);
		writeD(_itemslist.size());
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
			writeH(0x00); // ?
			writeD(temp.getObjectId()); // some item identifier later used by client to answer (see RequestPackageSend) not item id nor object id maybe some freight system id??
		}
	}
}