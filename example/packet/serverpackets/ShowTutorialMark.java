package l2p.gameserver.serverpackets;

public class ShowTutorialMark extends L2GameServerPacket
{
	/**
	 * После клика по знаку вопроса клиент попросит html-ку с этим номером.
	 */
	private boolean _quest;
	private int _number;

	public ShowTutorialMark(boolean quest, int number)
	{
		_quest = quest;
		_number = number;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_quest);
		writeD(_number);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_number);
	}
}