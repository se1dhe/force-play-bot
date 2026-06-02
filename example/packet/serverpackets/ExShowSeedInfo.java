package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.instancemanager.CastleManorManager.SeedProduction;
import l2p.gameserver.model.L2Manor;

public class ExShowSeedInfo extends L2GameServerPacket
{
	private GArray<SeedProduction> _seeds;
	private int _manorId;

	public ExShowSeedInfo(int manorId, GArray<SeedProduction> seeds)
	{
		_manorId = manorId;
		_seeds = seeds;
		if(_seeds == null)
			_seeds = new GArray<SeedProduction>();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0);
		writeD(_manorId); // Manor ID
		writeD(0);
		writeD(_seeds.size());
		for(SeedProduction seed : _seeds)
		{
			writeD(seed.getId()); // Seed id
			writeQ(seed.getCanProduce()); // Left to buy
			writeQ(seed.getStartProduce()); // Started amount
			writeQ(seed.getPrice()); // Sell Price
			writeD(L2Manor.getInstance().getSeedLevel(seed.getId())); // Seed Level
			writeC(1); // reward 1 Type
			writeD(L2Manor.getInstance().getRewardItemBySeed(seed.getId(), 1)); // Reward 1 Type Item Id
			writeC(1); // reward 2 Type
			writeD(L2Manor.getInstance().getRewardItemBySeed(seed.getId(), 2)); // Reward 2 Type Item Id
		}
	}

	@Override
	protected void writeImplIT()
	{
		writeC(0);
		writeD(_manorId); // Manor ID
		writeD(0);
		writeD(_seeds.size());
		for(SeedProduction seed : _seeds)
		{
			writeD(seed.getId()); // Seed id
			writeD(seed.getCanProduce()); // Left to buy
			writeD(seed.getStartProduce()); // Started amount
			writeD(seed.getPrice()); // Sell Price
			writeD(L2Manor.getInstance().getSeedLevel(seed.getId())); // Seed Level
			writeC(1); // reward 1 Type
			writeD(L2Manor.getInstance().getRewardItemBySeed(seed.getId(), 1)); // Reward 1 Type Item Id
			writeC(1); // reward 2 Type
			writeD(L2Manor.getInstance().getRewardItemBySeed(seed.getId(), 2)); // Reward 2 Type Item Id
		}
	}
}