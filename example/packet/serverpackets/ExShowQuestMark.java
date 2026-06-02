package l2p.gameserver.serverpackets;

public class ExShowQuestMark extends L2GameServerPacket
{
	private int _questId, _cond;

	public ExShowQuestMark(int questId, int cond)
	{
		_questId = questId;
		_cond = cond;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_questId);
		writeD(_cond);
	}

	@Override
	protected void writeImplIT()
	{
		writeD(_questId);
	}
}