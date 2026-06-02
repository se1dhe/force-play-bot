package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.PackageSendableList;

public class RequestPackageSendableItemList extends L2GameClientPacket
{
	private int _characterObjectId;

	@Override
	public void readImpl()
	{
		_characterObjectId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || !activeChar.getPlayerAccess().UseWarehouse)
			return;

		if(Config.SERVICES_DISABLE_WH_DEPOSIT_LIST && activeChar.isTradeKeyBlocked())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Предмет нельзя положить и забрать со склада, отключите Lock." : "The item cannot be put and taken from the warehouse, turn off Lock.");
			activeChar.sendActionFailed();
			return;
		}

		activeChar.tempInventoryDisable();
		activeChar.isCommunityWh = false;
		activeChar.sendPacket(new PackageSendableList(1, activeChar, _characterObjectId));
		activeChar.sendPacket(new PackageSendableList(2, activeChar, _characterObjectId));
	}
}