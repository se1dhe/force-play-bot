package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.CrestCache;
import l2p.gameserver.model.L2Alliance;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;

public class RequestSetAllyCrest extends L2GameClientPacket
{
	private int _length;
	private byte[] _data;
	private static final byte[] CK = { 68, 68, 83, 32, 124, 0, 0, 0, 7, 16, 8, 0, 16, 0, 0, 0, 8, 0, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 4, 0, 0, 0, 68, 88, 84, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0 };

	@Override
	protected void readImpl()
	{
		_length = readD();
		if(_length == CrestCache.ALLY_CREST_SIZE && _length == _buf.remaining())
		{
			_data = new byte[_length];
			readB(_data);
		}
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_data != null)
		{
			if(_data.length != CrestCache.ALLY_CREST_SIZE)
				return;
			for(int i = 0; i < CK.length; i++)
				if (_data[i] != CK[i])
				{
					activeChar.sendMessage("Incorrect format for ally crest!");
					return;
				}
		}
		L2Alliance ally = activeChar.getAlliance();
		if(ally != null && activeChar.isAllyLeader())
		{
			L2Clan clan = activeChar.getClan();
			if(clan.NEXT_CREST > System.currentTimeMillis())
			{
				int sec = (int)((clan.NEXT_CREST - System.currentTimeMillis()) / 1000L);
				if(activeChar.isLangRus())
					activeChar.sendMessage("До повторной установки " + sec + " сек.");
				else
					activeChar.sendMessage("Set time remaining " + sec + " sec.");
				return;
			}
			clan.NEXT_CREST = System.currentTimeMillis() + 30000L;
			int crestId = 0;
			if(_data != null)
				crestId = CrestCache.getInstance().saveAllyCrest(ally.getAllyId(), _data);
			else if(ally.hasAllyCrest())
				CrestCache.getInstance().removeAllyCrest(ally.getAllyId());
			ally.setAllyCrestId(crestId);
			ally.broadcastAllyStatus(false);
		}
	}
}