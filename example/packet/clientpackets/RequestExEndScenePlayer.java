package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class RequestExEndScenePlayer extends L2GameClientPacket
{
	private int _movieId;

	@Override
	protected void readImpl()
	{
		_movieId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(!activeChar.isInMovie() || activeChar.getMovieId() != _movieId)
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.leaveMovieMode();
	}
}