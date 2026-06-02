package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.ExPutEnchantSupportItemResult;

public class RequestExTryToPutEnchantSupportItem extends L2GameClientPacket
{
	private int _itemId;
	private int _catalystId;

	@Override
	protected void readImpl()
	{
		_catalystId = readD();
		_itemId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		PcInventory inventory = player.getInventory();
		L2ItemInstance itemToEnchant = inventory.getItemByObjectId(_itemId);
		L2ItemInstance catalyst = inventory.getItemByObjectId(_catalystId);

		if(itemToEnchant == null || catalyst == null)
		{
			player.sendPacket(ExPutEnchantSupportItemResult.FAIL);
			return;
		}

		/*EnchantCatalyzer enchantCatalyzer = EnchantItemHolder.getInstance().getEnchantCatalyzer(catalyst.getItemId());
		if(enchantCatalyzer == null || !enchantCatalyzer.isUsableWith(itemToEnchant))
		{
			player.sendPacket(ExPutEnchantSupportItemResult.FAIL);
			return;
		}*/

		player.sendPacket(ExPutEnchantSupportItemResult.FAIL);
	}
}