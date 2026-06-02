package l2p.gameserver.serverpackets;

public class AskJoinParty extends L2GameServerPacket
{
	private String _requestorName;
	private int _itemDistribution;

	public AskJoinParty(String requestorName, int itemDistribution)
	{
		_requestorName = requestorName;
		_itemDistribution = itemDistribution;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_requestorName);
		writeD(_itemDistribution);
	}
}