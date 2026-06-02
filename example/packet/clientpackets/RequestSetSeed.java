package l2p.gameserver.clientpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.instancemanager.CastleManorManager;
import l2p.gameserver.instancemanager.CastleManorManager.SeedProduction;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.residence.Castle;

public class RequestSetSeed extends L2GameClientPacket
{
	private int _size;
	private int _manorId;
	private int[] _items; // _size*3

	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_size = readD();
		if(_size * (getClient().isITClient() ? 12 : 20) > _buf.remaining() || _size > Short.MAX_VALUE || _size <= 0)
		{
			_size = 0;
			return;
		}
		_items = new int[_size * 3];
		for(int i = 0; i < _size; i++)
		{
			int itemId = readD();
			_items[i * 3 + 0] = itemId;
			// TODO [V] - long
			int sales = getClient().isITClient() ? readD() : (int) readQ();
			_items[i * 3 + 1] = sales;
			// TODO [V] - long
			int price = getClient().isITClient() ? readD() : (int) readQ();
			_items[i * 3 + 2] = price;
		}
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(!Config.ALLOW_MANOR)
		{
			player.sendMessage("Manor disabled.");
			player.sendActionFailed();
			return;
		}

		if(_size < 1 || player.getClan() == null)
		{
			player.sendActionFailed();
			return;
		}

		Castle caslte = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
		if(caslte == null)
		{
			player.sendActionFailed();
			return;
		}

		GArray<SeedProduction> seeds = new GArray<SeedProduction>();
		for(int i = 0; i < _size; i++)
		{
			int id = _items[i * 3 + 0];
			int sales = _items[i * 3 + 1];
			int price = _items[i * 3 + 2];
			if(id > 0)
			{
				SeedProduction s = CastleManorManager.getInstance().getNewSeedProduction(id, sales, price, sales);
				seeds.add(s);
			}
		}

		caslte.setSeedProduction(seeds, CastleManorManager.PERIOD_NEXT);
		if(Config.MANOR_SAVE_ALL_ACTIONS)
			caslte.saveSeedData(CastleManorManager.PERIOD_NEXT);
	}
}
