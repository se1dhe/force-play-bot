package l2p.gameserver.serverpackets;

// TODO [V] - найти куда добавить?
public class ExSubPledgeSkillAdd extends L2GameServerPacket
{
	private int _type, _id, _level;

	public ExSubPledgeSkillAdd(int type, int id, int level)
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

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}