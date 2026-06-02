package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2ManufactureItem;
import l2p.gameserver.model.L2ManufactureList;
import l2p.gameserver.model.L2Player;

public class RecipeShopSellList extends L2GameServerPacket
{
	public int obj_id, curMp, maxMp, buyer_adena;
	private L2ManufactureList createList;

	public RecipeShopSellList(L2Player buyer, L2Player manufacturer)
	{
		obj_id = manufacturer.getObjectId();
		curMp = (int) manufacturer.getCurrentMp();
		maxMp = manufacturer.getMaxMp();
		buyer_adena = buyer.getAdena();
		createList = manufacturer.getCreateList();
	}

	@Override
	protected boolean canWrite()
	{
		if(createList == null)
			return false;
		return true;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(obj_id);
		writeD(curMp); //Creator's MP
		writeD(maxMp); //Creator's MP
		writeQ(buyer_adena); //Buyer Adena
		int count = createList.size();
		writeD(count);
		L2ManufactureItem temp;
		for(int i = 0; i < count; i++)
		{
			temp = createList.getList().get(i);
			writeD(temp.getRecipeId());
			writeD(0x00); //unknown
			writeQ(temp.getCost());
			writeQ(0);
			writeQ(0);
			writeC(0);
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(obj_id);
		writeD(curMp); //Creator's MP
		writeD(maxMp); //Creator's MP
		writeD(buyer_adena); //Buyer Adena
		int count = createList.size();
		writeD(count);
		L2ManufactureItem temp;
		for(int i = 0; i < count; i++)
		{
			temp = createList.getList().get(i);
			writeD(temp.getRecipeId());
			writeD(0x00); //unknown
			writeD(temp.getCost());
		}
	}
}