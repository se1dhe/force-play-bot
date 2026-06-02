package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2HennaInstance;
import l2p.gameserver.tables.HennaTable;
import l2p.gameserver.tables.ItemTable;

public class RequestHennaUnequip extends L2GameClientPacket
{
	private int _symbolId;

	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(player.hennaSet)
		{
			player.hennaSet = false;
			String v = player.getVar("OlyHenna");
			if(v != null)
			{
				int c = 0;
				boolean ns = false;
				String save = "";
				String[] ids = v.split(";");
				for(String id : ids)
				{
					if(!id.isEmpty())
					{
						int sid = Integer.parseInt(id);
						if(sid == _symbolId)
						{
							ns = true;
							player.sendMessage((player.isLangRus() ? "Удалено" : "Deleted") + ": " + ItemTable.getInstance().getTemplate(HennaTable.getInstance().getTemplate(sid).getDyeId()).getName());
						}
						else
						{
							if(c > 0)
								save += ";";

							save += id;
							c++;
						}
					}
				}
				if(ns)
				{
					if(save.isEmpty())
						player.unsetVar("OlyHenna");
					else
						player.setVar("OlyHenna", save);
				}
			}
			return;
		}
		for(int i = 1; i <= 3; i ++)
		{
			L2HennaInstance henna = player.getHenna(i);
			if(henna == null)
				continue;
			if(henna.getSymbolId() == _symbolId)
			{
				int price = henna.getPrice() / 5;
				if(player.getAdena() < price)
				{
					player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					break;
				}
				if(price > 0)
					player.reduceAdena(price, false);
				player.removeHenna(player.getActiveClassId(), i);

				player.sendPacket(Msg.THE_SYMBOL_HAS_BEEN_DELETED);
				break;
			}
		}
	}
}