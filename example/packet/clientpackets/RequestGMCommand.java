package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2World;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.ItemInfo;
import l2p.gameserver.serverpackets.*;
import l2p.gameserver.tables.ClanTable;

import java.util.ArrayList;
import java.util.List;

public class RequestGMCommand extends L2GameClientPacket
{
	private String _targetName;
	private int _command;

	@Override
	public void readImpl()
	{
		_targetName = readS();
		_command = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;
		L2Player target = L2World.getPlayer(_targetName);
		L2Clan clan = null;
		if(target == null && (_command != 6 || (clan = ClanTable.getInstance().getClanByName(_targetName)) == null))
			return;

		if(!player.getPlayerAccess().CanViewChar || player.isKeyBlocked())
			return;

		switch(_command)
		{
			case 1:
				sendPacket(new GMViewCharacterInfo(target));
				sendPacket(new GMHennaInfo(target));
				break;
			case 2:
				if(target.getClan() != null)
					sendPacket(new GMViewPledgeInfo(target.getClan(), target));
				break;
			case 3:
				sendPacket(new GMViewSkillInfo(target));
				break;
			case 4:
				sendPacket(new GMViewQuestInfo(target));
				break;
			case 5:
				L2ItemInstance[] items = target.getInventory().getItems();
				int questSize = 0;
				List<ItemInfo> itemInfoList = new ArrayList<ItemInfo>();
				for(L2ItemInstance item : items)
				{
					if(item.getItem().isQuest())
						questSize++;
					itemInfoList.add(new ItemInfo(item, false));
				}
				sendPacket(new GMViewItemList(1, target, itemInfoList));
				if(!getClient().isITClient())
					sendPacket(new GMViewItemList(2, target, itemInfoList));
				sendPacket(new ExGMViewQuestItemList(target, itemInfoList, questSize));
				sendPacket(new GMHennaInfo(target));
				break;
			case 6:
				if(target != null)
				{
					sendPacket(new GMViewWarehouseWithdrawList(1, target));
					if(!getClient().isITClient())
						sendPacket(new GMViewWarehouseWithdrawList(2, target));
				}
				else
				{
					sendPacket(new GMViewWarehouseWithdrawList(1, clan));
					if(!getClient().isITClient())
						sendPacket(new GMViewWarehouseWithdrawList(2, clan));
				}
				break;
		}
	}
}
