package l2p.gameserver.serverpackets;

import l2p.gameserver.clientpackets.Say2C;
import l2p.gameserver.model.L2Player;

public class Say2 extends L2GameServerPacket
{
	// Flags
	private static final int IS_FRIEND = 1 << 0;
	private static final int IS_CLAN_MEMBER = 1 << 1;
	private static final int IS_MENTEE_OR_MENTOR = 1 << 2;
	private static final int IS_ALLIANCE_MEMBER = 1 << 3;
	private static final int IS_GM = 1 << 4;

	private int _objectId, _textType;
	private String _charName, _text;
	private int _charId = 0;

	private int _mask;
	private int _charLevel = -1;
	private int _npcString = -1;

	public Say2(int objectId, int messageType, String charName, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_text = text;
	}

	public Say2(int objectId, int messageType, int charId, int id)
	{
		_objectId = objectId;
		_textType = messageType;
		_charId = charId;
		_npcString = id;
	}

	public void setSenderInfo(L2Player sender, L2Player receiver)
	{
		_charLevel = sender.getLevel();

		if(receiver.getFriendList().getList().containsKey(sender.getObjectId()))
			_mask |= IS_FRIEND;

		if(receiver.getClanId() > 0 && receiver.getClanId() == sender.getClanId())
			_mask |= IS_CLAN_MEMBER;

		//if (receiver.getMenteeList().getMentor() == sender.getObjectId() || sender.getMenteeList().getMentor() == receiver.getObjectId())
		//	_mask |= IS_MENTEE_OR_MENTOR;

		if(receiver.getAllyId() > 0 && receiver.getAllyId() == sender.getAllyId())
			_mask |= IS_ALLIANCE_MEMBER;

		// Does not shows level
		if(sender.isGM())
			_mask |= IS_GM;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_textType);
		switch(_textType)
		{
			case Say2C.SYSTEM_MESSAGE:
				writeD(_charId);
				writeD(_npcString);
				break;
			case Say2C.TELL:
				writeS(_charName);
				writeD(-1);
				writeS(_text);
				writeC(_mask);
				if((_mask & IS_GM) == 0)
					writeC(_charLevel);
				break;
			case Say2C.CLAN:
			case Say2C.ALLIANCE:
				writeS(_charName);
				writeD(-1);
				writeS(_text);
				writeC(0);
				break;
			default:
				writeS(_charName);
				writeD(-1);
				writeS(_text);
				break;
		}

		if(_charId == 0)
		{
			L2Player player = getClient().getActiveChar();
			if(player != null)
				player.broadcastSnoop(_textType, _charName, _text);
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_objectId);
		writeD(_textType);
		if(_charId > 0)
			writeD(_charId);
		else
			writeS(_charName);
		if(_npcString > -1)
			writeD(_npcString);
		else
			writeS(_text);

		if(_charId == 0)
		{
			L2Player player = getClient().getActiveChar();
			if(player != null)
				player.broadcastSnoop(_textType, _charName, _text);
		}
	}
}