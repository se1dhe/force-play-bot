package l2p.gameserver.clientpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.Config;
import l2p.gameserver.instancemanager.CastleManorManager;
import l2p.gameserver.instancemanager.CastleManorManager.CropProcure;
import l2p.gameserver.model.L2Manor;
import l2p.gameserver.model.L2Object;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2ManorManagerInstance;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.serverpackets.StatusUpdate;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;

@SuppressWarnings("unused")
public class RequestBuyProcure extends L2GameClientPacket
{
    private int _listId;
    private int _count;
    private int[] _items;
    private GArray<CropProcure> _procureList = new GArray<CropProcure>();

    @Override
    protected void readImpl()
    {
        _listId = readD();
        _count = readD();
		if(_count > 500 || _count < 0)
        {
            _count = 0;
            return;
        }

        _items = new int[_count * 2];
        for(int i = 0; i < _count; i++)
        {
            long servise = readD();
            int itemId = readD();
            _items[i * 2 + 0] = itemId;
            long cnt;
            if(getClient().isITClient())
                cnt = readD();
            else
                cnt = readQ();
            if(cnt > Integer.MAX_VALUE || cnt < 1)
            {
                _count = 0;
                _items = null;
                return;
            }
            _items[i * 2 + 1] = (int) cnt;
        }
    }

    @Override
    protected void runImpl()
    {
        L2Player player = getClient().getActiveChar();
        if(player == null)
            return;

        if(_count < 1)
        {
            player.sendActionFailed();
            return;
        }
        if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
        {
            player.sendActionFailed();
            return;
        }
        if(!Config.ALLOW_MANOR)
        {
            player.sendMessage("Manor disabled.");
            player.sendActionFailed();
            return;
        }

        long subTotal = 0;
        int tax = 0;
        int slots = 0;
        int weight = 0;
        L2Object target = player.getTarget();
        L2ManorManagerInstance manor = target != null && (target instanceof L2ManorManagerInstance) ? (L2ManorManagerInstance) target : null;

        for(int i = 0; i < _count; i++)
        {
            int itemId = _items[i * 2 + 0];
            int count = _items[i * 2 + 1];
            int price = 0;
            if(count < 0 || count > Integer.MAX_VALUE)
            {
				sendPacket(new SystemMessage(SystemMessage.INCORRECT_ITEM_COUNT));
                return;
            }

            L2Item template = ItemTable.getInstance().getTemplate(L2Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getReward()));
            weight += count * template.getWeight();

            if(!template.isStackable())
                slots += count;
            else if(player.getInventory().getItemByItemId(itemId) == null)
                slots++;
        }
        if(!player.getInventory().validateWeight(weight))
        {
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT));
			return;
        }

        if(!player.getInventory().validateCapacity(slots))
        {
			sendPacket(new SystemMessage(SystemMessage.YOUR_INVENTORY_IS_FULL));
            return;
        }

        InventoryUpdate playerIU = new InventoryUpdate();
        _procureList = manor.getCastle().getCropProcure(CastleManorManager.PERIOD_CURRENT);

        for(int i = 0; i < _count; i++)
        {
            int itemId = _items[i * 2 + 0];
            int count = _items[i * 2 + 1];
            if(count < 0)
                count = 0;

            int rewradItemId = L2Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getReward());

            int rewradItemCount = 1; // L2Manor.getInstance().getRewardAmount(itemId,

			rewradItemCount = count / rewradItemCount;

            L2ItemInstance item = player.getInventory().addItem(rewradItemId, rewradItemCount, "<BuyProcure>");
            L2ItemInstance iteme = player.getInventory().destroyItemByItemId(itemId, count, true);

            if(item == null || iteme == null)
                continue;

            playerIU.addRemovedItem(iteme);
            if(item.getCount() > rewradItemCount)
                playerIU.addModifiedItem(item, false);
            else
                playerIU.addNewItem(item);

			SystemMessage sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
			sm.addItemName(rewradItemId);
			sm.addNumber(rewradItemCount);
			player.sendPacket(sm);
			sm = null;
        }

        player.sendPacket(playerIU);

		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
    }
}
