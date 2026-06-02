package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2HennaInstance;
import l2p.gameserver.tables.HennaTable;

import java.util.ArrayList;
import java.util.List;

public class HennaUnequipList extends L2GameServerPacket
{
	private int _emptySlots;
	private int _adena;
	private List<L2HennaInstance> availHenna = new ArrayList<L2HennaInstance>(3);
	private boolean _free = false;

	public HennaUnequipList(L2Player player)
	{
		_adena = player.getAdena();
		_emptySlots = player.getHennaEmptySlots();
		if(player.hennaSet)
		{
			_free = true;
			String v = player.getVar("OlyHenna");
			if(v != null)
			{
				String[] ids = v.split(";");
				int c = 0;
				for(String id : ids)
					if(!id.isEmpty())
					{
						availHenna.add(new L2HennaInstance(HennaTable.getInstance().getTemplate(Integer.parseInt(id))));
						c++;
					}
			}
		}
		else
		{
			for(int i = 1; i <= 3; i++)
				if (player.getHenna(i) != null)
					availHenna.add(player.getHenna(i));
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeQ(_adena);
		writeD(_emptySlots);
		writeD(availHenna.size());
		for(L2HennaInstance henna : availHenna)
		{
			writeD(henna.getSymbolId()); //symbolid
			writeD(henna.getItemIdDye()); //itemid of dye
			writeQ(_free ? 0 : henna.getAmountDyeRequire() / 2);
			writeQ(_free ? 0 : henna.getPrice() / 5);
			writeD(1); //meet the requirement or not
			//writeD(0x00);
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_adena);
		writeD(_emptySlots);
		writeD(availHenna.size());
		for(L2HennaInstance henna : availHenna)
		{
			writeD(henna.getSymbolId()); //symbolid
			writeD(henna.getItemIdDye()); //itemid of dye
			writeD(_free ? 0 : henna.getAmountDyeRequire() / 2);
			writeD(_free ? 0 : henna.getPrice() / 5);
			writeD(1); //meet the requirement or not
		}
	}
}