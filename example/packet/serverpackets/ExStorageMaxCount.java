package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;

public class ExStorageMaxCount extends L2GameServerPacket
{
	private int _inventory;
	private int _warehouse;
	private int _clan;
	private int _freight;
	private int _privateSell;
	private int _privateBuy;
	private int _recipeDwarven;
	private int _recipeCommon;
	private int _inventoryExtraSlots;
	private int _questItemsLimit;

	public ExStorageMaxCount(L2Player player)
	{
		_inventory = player.getInventoryLimit();
		_warehouse = player.getWarehouseLimit();
		_clan = Config.WAREHOUSE_SLOTS_CLAN;
		_freight = player.getFreightLimit();
		_privateBuy = player.getPrivateBuyLimit();
		_privateSell = player.getPrivateSellLimit();
		_recipeDwarven = player.getDwarvenRecipeLimit();
		_recipeCommon = player.getCommonRecipeLimit();
		_inventoryExtraSlots = player.getBeltInventoryIncrease();
		_questItemsLimit = player.getQuestInventoryMaximum();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_inventory);
		writeD(_warehouse);
		writeD(_clan);
		writeD(_privateSell);
		writeD(_privateBuy);
		writeD(_recipeDwarven);
		writeD(_recipeCommon);
		writeD(_inventoryExtraSlots); // belt inventory slots increase count
		writeD(_questItemsLimit); //  quests list  by off 100 maximum
		writeD(40); // ??? 40 slots
		writeD(40); // ??? 40 slots
		writeD(0x64); // Artifact slots (Fixed)
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_inventory);
		writeD(_warehouse);
		writeD(_freight);
		writeD(_privateSell);
		writeD(_privateBuy);
		writeD(_recipeDwarven);
		writeD(_recipeCommon);
	}
}