package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.instancemanager.PlayerManager;
import l2p.gameserver.serverpackets.ExIsCharNameCreatable;
import l2p.gameserver.utils.Util;

public class RequestCharacterNameCreatable extends L2GameClientPacket
{
	private String _charName;

	@Override
	protected void readImpl()
	{
		_charName = readS();
	}

	@Override
	protected void runImpl()
	{
		if(PlayerManager.accountCharNumber(getClient().getLoginName()) >= 8)
		{
			sendPacket(ExIsCharNameCreatable.TOO_MANY_CHARACTERS);
			return;
		}

		if(_charName == null || _charName.isEmpty())
		{
			sendPacket(ExIsCharNameCreatable.ENTER_CHAR_NAME__MAX_16_CHARS);
			return;
		}

		if(!Util.isMatchingRegexp(_charName, Config.CNAME_TEMPLATE))
		{
			sendPacket(ExIsCharNameCreatable.WRONG_NAME);
			return;
		}

		if(PlayerManager.getObjectIdByName(_charName) > 0)
		{
			sendPacket(ExIsCharNameCreatable.NAME_ALREADY_EXISTS);
			return;
		}
		sendPacket(ExIsCharNameCreatable.SUCCESS);
	}
}