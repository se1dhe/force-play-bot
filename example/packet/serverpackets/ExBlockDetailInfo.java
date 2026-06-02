package l2p.gameserver.serverpackets;

/**
 * @author Bonux
 **/
// TODO [V] - нужно?
public class ExBlockDetailInfo extends L2GameServerPacket
{
	private String _name;
	private String _memo;

	public ExBlockDetailInfo(String name, String memo)
	{
		_name = name;
		_memo = memo;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_name);
		writeS(_memo);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}