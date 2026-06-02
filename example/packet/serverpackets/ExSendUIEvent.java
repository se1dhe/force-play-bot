package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux
 **/
public class ExSendUIEvent extends L2GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ExSendUIEvent.class);
	private int _objectId;
	private int _eventId;
	private int _param1;
	private int _param2;
	private String _param3;
	private String _param4;
	private String _param5;

	public ExSendUIEvent(L2Player player, int eventId, int param1, int param2)
	{
		_objectId = player.getObjectId();
		_eventId = eventId;
		_param1 = param1;
		_param2 = param2;
	}

	public ExSendUIEvent(L2Player player, int isHide, boolean isIncrease, int startTime, int endTime)
	{
		_objectId = player.getObjectId();
		_eventId = isHide;
		_param1 = 0;
		_param2 = 0;
	}

	public ExSendUIEvent(int objectId, int type, int param1, int param2, String param3, String param4, String param5) {
		_objectId = objectId;
		_eventId = type;
		_param1 = param1;
		_param2 = param2;
		_param3 = param3;
		_param4 = param4;
		_param5 = param5;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeD(_eventId);
		writeD(_param1);
		writeD(_param2);
		writeS(_param3);
		writeS(_param4);
		writeS(_param5);
		writeS("");
		writeS("");
		writeD(-1);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}

	@Override
	protected void writeImplIT()
	{
		writeD(_objectId);
		writeD(_eventId);
		writeD(_param1);
		writeD(_param2);
		writeS("");
		writeS("");
		writeS("");
		writeS("");
		writeS("");
	}
}