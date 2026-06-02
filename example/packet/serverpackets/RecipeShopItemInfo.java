package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Object;
import l2p.gameserver.model.L2Player;

public class RecipeShopItemInfo extends L2GameServerPacket
{
	private int _recipeId, _shopId, curMp, maxMp;
	private int _success = 0xFFFFFFFF;
	private int _price;
	private boolean can_writeImpl = false;

	public RecipeShopItemInfo(int shopId, int recipeId, int price, int success, L2Player activeChar)
	{
		_recipeId = recipeId;
		_shopId = shopId;
		_price = price;
		_success = success;

		L2Object manufacturer = activeChar.getVisibleObject(_shopId);

		if(manufacturer == null)
			return;

		if(!manufacturer.isPlayer())
			return;

		curMp = (int) ((L2Player) manufacturer).getCurrentMp();
		maxMp = ((L2Player) manufacturer).getMaxMp();
		can_writeImpl = true;
	}

	@Override
	protected boolean canWrite()
	{
		return can_writeImpl;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_shopId);
		writeD(_recipeId);
		writeD(curMp);
		writeD(maxMp);
		writeD(_success);
		writeQ(_price);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_shopId);
		writeD(_recipeId);
		writeD(curMp);
		writeD(maxMp);
		writeD(_success);
	}
}