package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.instancemanager.CastleManorManager.CropProcure;
import l2p.gameserver.model.L2Manor;

public class ExShowCropInfo extends L2GameServerPacket
{
	private GArray<CropProcure> _crops;
	private int _manorId;

	public ExShowCropInfo(int manorId, GArray<CropProcure> crops)
	{
		_manorId = manorId;
		_crops = crops;
		if(_crops == null)
			_crops = new GArray<CropProcure>();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0);
		writeD(_manorId); // Manor ID
		writeD(0);
		writeD(_crops.size());
		for(CropProcure crop : _crops)
		{
			writeD(crop.getId()); // Crop id
			writeQ(crop.getAmount()); // Buy residual
			writeQ(crop.getStartAmount()); // Buy
			writeQ(crop.getPrice()); // Buy price
			writeC(crop.getReward()); // Reward
			writeD(L2Manor.getInstance().getSeedLevelByCrop(crop.getId())); // Seed Level
			writeC(1); // rewrad 1 Type
			writeD(L2Manor.getInstance().getRewardItem(crop.getId(), 1)); // Rewrad 1 Type Item Id
			writeC(1); // rewrad 2 Type
			writeD(L2Manor.getInstance().getRewardItem(crop.getId(), 2)); // Rewrad 2 Type Item Id
		}
	}

	@Override
	protected void writeImplIT()
	{
		writeC(0);
		writeD(_manorId); // Manor ID
		writeD(0);
		writeD(_crops.size());
		for(CropProcure crop : _crops)
		{
			writeD(crop.getId()); // Crop id
			writeD(crop.getAmount()); // Buy residual
			writeD(crop.getStartAmount()); // Buy
			writeD(crop.getPrice()); // Buy price
			writeC(crop.getReward()); // Reward
			writeD(L2Manor.getInstance().getSeedLevelByCrop(crop.getId())); // Seed Level
			writeC(1); // rewrad 1 Type
			writeD(L2Manor.getInstance().getRewardItem(crop.getId(), 1)); // Rewrad 1 Type Item Id
			writeC(1); // rewrad 2 Type
			writeD(L2Manor.getInstance().getRewardItem(crop.getId(), 2)); // Rewrad 2 Type Item Id
		}
	}
}