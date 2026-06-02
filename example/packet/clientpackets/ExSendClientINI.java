package l2p.gameserver.clientpackets;

public class ExSendClientINI extends L2GameClientPacket
{
	private int partNumber;
	private int partSize;
	private byte[] content;

	@Override
	protected void readImpl()
	{
		partNumber = readC();
		partSize = readH();
		content = new byte[partSize];
		readB(content);
	}

	@Override
	protected void runImpl()
	{
	}
}