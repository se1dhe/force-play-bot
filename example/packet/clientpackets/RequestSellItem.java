package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2CastleChamberlainInstance;
import l2p.gameserver.model.instances.L2ClanHallManagerInstance;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2MercManagerInstance;
import l2p.gameserver.model.instances.L2MerchantInstance;
import l2p.gameserver.model.instances.L2NpcFriendInstance;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.serverpackets.ExBuySellList;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.Util;
import org.apache.commons.lang3.ArrayUtils;

public class RequestSellItem extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _listId;
	private int _count;
	private int[] _items; // count*3

	@Override
	public void readImpl()
	{
		_listId = readD();
		_count = readD();
		if(_count * (getClient().isITClient() ? 12 : 16) > _buf.remaining() || _count > Short.MAX_VALUE || _count <= 0)
		{
			_items = null;
			return;
		}
		_items = new int[_count * 3];
		for(int i = 0; i < _count; i++)
		{
			_items[i * 3 + 0] = readD();
			_items[i * 3 + 1] = readD();
			// TODO [V] - long
			_items[i * 3 + 2] = getClient().isITClient() ? readD() : (int) readQ();
			if(_items[i * 3 + 1] < 0)
			{
				_items = null;
				break;
			}
		}
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(_items == null || _count <= 0)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(Config.SERVICES_DISABLE_CHECK_FOR_SHOP && activeChar.isTradeKeyBlocked())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можете торговать, отключите Lock." : "You cannot trade, disable Lock.");
			activeChar.sendActionFailed();
			return;
		}

		boolean bbs = activeChar.getLastNpcId() == -1;
		L2NpcInstance npc = activeChar.getLastNpc();
		if(!bbs && (!L2NpcInstance.canBypassCheck(activeChar, npc) || !activeChar.checkLastNpc()))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && !bbs && activeChar.getKarma() > 0 && !ArrayUtils.contains(Config.ALT_GAME_KARMA_NPC, npc.getNpcId()))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(bbs && !Config.ALLOW_PVPCB_KARMA_SHOP && activeChar.getKarma() > 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(bbs)
			activeChar.setLastNpcId(-3);
		else if(!activeChar.isGM())
		{
			boolean isValidMerchant = npc instanceof L2ClanHallManagerInstance || npc instanceof L2MerchantInstance || npc instanceof L2MercManagerInstance || npc instanceof L2CastleChamberlainInstance || npc instanceof L2NpcFriendInstance;
			if(!isValidMerchant || !activeChar.isInActingRange(npc))
			{
				activeChar.sendActionFailed();
				return;
			}
		}
		if(!bbs)
			activeChar.turn(npc, 3000);

		for(int i = 0; i < _count; i++)
		{
			int objectId = _items[i * 3 + 0];
			int itemId = _items[i * 3 + 1];
			long cnt = _items[i * 3 + 2];

			if(cnt < 1)
				continue;

			L2ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
			if(item == null || !item.canBeTraded(activeChar) || !item.getItem().isSellable())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.THE_ATTEMPT_TO_SELL_HAS_FAILED));
				return;
			}

			if(item.getItemId() != itemId)
			{
				Util.handleIllegalPlayerAction(activeChar, "RequestSellItem[115] Fake packet", 0);
				continue;
			}

			if(item.getCount() < cnt)
			{
				Util.handleIllegalPlayerAction(activeChar, "RequestSellItem[121] Incorrect item count", 0);
				continue;
			}

			long price = (long)(cnt * item.getReferencePrice() * Config.SELL_MOD);

			if(price >= 0)
			{
				activeChar.addAdena(price);
				Log.LogItem(activeChar, Log.Sell, item);

				// If player sells the enchant scroll he is using, deactivate it
				if(activeChar.getEnchantScroll() != null && item.getObjectId() == activeChar.getEnchantScroll().getObjectId())
					activeChar.setEnchantScroll(null);

				activeChar.getInventory().destroyItem(item, cnt, true, "<DestroyItemSellItem>");
			}
		}

		if(!activeChar.isITClient())
			activeChar.sendPacket(new ExBuySellList.SellRefundList(activeChar, true));
		activeChar.updateStats();
		activeChar.sendItemList(true);
	}
}