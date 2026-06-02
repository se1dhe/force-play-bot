package l2p.gameserver.serverpackets;

/**
 * Author: VISTALL
 */
// TODO [V] - надо?
public class ExSubPledgetSkillAdd extends L2GameServerPacket
{
	private int _type, _id, _level;

	public ExSubPledgetSkillAdd(int type, int id, int level)
	{
		_type = type;
		_id = id;
		_level = level;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_type);
		writeD(_id);
		writeD(_level);
	}
}