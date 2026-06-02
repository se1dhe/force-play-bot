package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.instances.L2HennaInstance;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.HennaTable;
import l2p.gameserver.tables.HennaTreeTable;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Henna;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHennaEquip extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestHennaEquip.class);
	private int _symbolId;

	@Override
	public void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.isOutOfControl())
		{
			player.sendActionFailed();
			return;
		}

		L2Henna template = HennaTable.getInstance().getTemplate(_symbolId);
		if(template == null)
			return;

		L2HennaInstance temp = new L2HennaInstance(template);

		boolean cheater = true;
		for(L2HennaInstance h : HennaTreeTable.getInstance().getAvailableHenna(player.hennaSet ? ClassId.values()[player.getBaseClassId()] : player.getClassId()))
			if(h.getSymbolId() == temp.getSymbolId())
			{
				cheater = false;
				break;
			}

		if(cheater)
		{
			player.sendPacket(new SystemMessage(SystemMessage.THE_SYMBOL_CANNOT_BE_DRAWN));
			return;
		}

		if(player.hennaSet)
		{
			player.hennaSet = false;
			if(player.isInOlympiadMode())
			{
				player.sendMessage(player.isLangRus() ? "Недоступно в Олимпиаде." : "Unavailable in Olympiad.");
				player.sendActionFailed();
				return;
			}
			String he = player.getVar("OlyHenna");
			if((he != null) && (he.split(";").length >= 3))
			{
				player.sendMessage(player.isLangRus() ? "У Вас заняты все ячейки." : "All slots used.");
				player.sendActionFailed();
				return;
			}
			player.setVar("OlyHenna", he != null ? he + ";" + temp.getSymbolId() : String.valueOf(temp.getSymbolId()));
			player.sendMessage("Установлено: " + ItemTable.getInstance().getTemplate(temp.getItemIdDye()).getName());
			return;
		}
		PcInventory inventory = player.getInventory();
		L2ItemInstance item = inventory.getItemByItemId(temp.getItemIdDye());
		if(item != null && item.getIntegerLimitedCount() >= temp.getAmountDyeRequire() && player.getAdena() >= temp.getPrice() && player.addHenna(temp))
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_HAS_DISAPPEARED).addString(temp.getName()));
			player.sendPacket(new SystemMessage(SystemMessage.THE_SYMBOL_HAS_BEEN_ADDED));
			inventory.reduceAdena(temp.getPrice());
			if(inventory.destroyItemByItemId(temp.getItemIdDye(), temp.getAmountDyeRequire(), true) == null)
				_log.info("RequestHennaEquip[50]: Item not found!!!");
		}
		else
			player.sendPacket(new SystemMessage(SystemMessage.THE_SYMBOL_CANNOT_BE_DRAWN));
	}
}