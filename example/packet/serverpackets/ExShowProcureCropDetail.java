package l2p.gameserver.serverpackets;

import javolution.util.FastMap;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.instancemanager.CastleManorManager;
import l2p.gameserver.instancemanager.CastleManorManager.CropProcure;
import l2p.gameserver.model.entity.residence.Castle;

public class ExShowProcureCropDetail extends L2GameServerPacket
{
	private int _cropId;
	private FastMap<Integer, CropProcure> _castleCrops;

	public ExShowProcureCropDetail(int cropId)
	{
		_cropId = cropId;
		_castleCrops = new FastMap<Integer, CropProcure>();

		for(Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
		{
			CropProcure cropItem = c.getCrop(_cropId, CastleManorManager.PERIOD_CURRENT);
			if(cropItem != null && cropItem.getAmount() > 0)
				_castleCrops.put(c.getId(), cropItem);
		}
	}

	@Override
	public void writeImpl()
	{
		writeD(_cropId); // crop id
		writeD(_castleCrops.size()); // size

		for(int manorId : _castleCrops.keySet())
		{
			CropProcure crop = _castleCrops.get(manorId);
			writeD(manorId); // manor name
			writeQ(crop.getAmount()); // buy residual
			writeQ(crop.getPrice()); // buy price
			writeC(crop.getReward()); // reward type
		}
	}

	@Override
	public void writeImplIT()
	{
		writeD(_cropId); // crop id
		writeD(_castleCrops.size()); // size

		for(int manorId : _castleCrops.keySet())
		{
			CropProcure crop = _castleCrops.get(manorId);
			writeD(manorId); // manor name
			writeD(crop.getAmount()); // buy residual
			writeD(crop.getPrice()); // buy price
			writeC(crop.getReward()); // reward type
		}
	}
}