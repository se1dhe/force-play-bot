package l2p.gameserver.clientpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.instancemanager.CastleManorManager;
import l2p.gameserver.instancemanager.CastleManorManager.CropProcure;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.residence.Castle;

public class RequestSetCrop extends L2GameClientPacket
{
	private int _size, _manorId;

	private int[] _items; // _size*4

	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_size = readD();
		if(_size * (getClient().isITClient() ? 13 : 21) > _buf.remaining() || _size > Short.MAX_VALUE || _size <= 0)
		{
			_size = 0;
			return;
		}
		_items = new int[_size * 4];
		for(int i = 0; i < _size; i++)
		{
			int itemId = readD();
			_items[i * 4 + 0] = itemId;
			// TODO [V] - long
			int sales = getClient().isITClient() ? readD() : (int) readQ();
			_items[i * 4 + 1] = sales;
			// TODO [V] - long
			int price = getClient().isITClient() ? readD() : (int) readQ();
			_items[i * 4 + 2] = price;
			int type = readC();
			_items[i * 4 + 3] = type;
		}
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _size < 1)
			return;

		if(!Config.ALLOW_MANOR)
		{
			activeChar.sendMessage("Manor disabled.");
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getClan() == null)
		{
			activeChar.sendActionFailed();
			return;
		}
			
		Castle caslte = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
		if(caslte == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		GArray<CropProcure> crops = new GArray<CropProcure>();
		for(int i = 0; i < _size; i++)
		{
			int id = _items[i * 4 + 0];
			int sales = _items[i * 4 + 1];
			int price = _items[i * 4 + 2];
			int type = _items[i * 4 + 3];
			if(id > 0)
			{
				CropProcure s = CastleManorManager.getInstance().getNewCropProcure(id, sales, type, price, sales);
				crops.add(s);
			}
		}

		caslte.setCropProcure(crops, CastleManorManager.PERIOD_NEXT);
		if(Config.MANOR_SAVE_ALL_ACTIONS)
			caslte.saveCropData(CastleManorManager.PERIOD_NEXT);
	}
}