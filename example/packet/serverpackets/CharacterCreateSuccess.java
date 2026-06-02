package l2p.gameserver.serverpackets;

public class CharacterCreateSuccess extends L2GameServerPacket
{
	public static final CharacterCreateSuccess STATIC_PACKET = new CharacterCreateSuccess();

	private CharacterCreateSuccess()
	{}

	@Override
	protected final void writeImpl()
	{
		writeD(0x01);
	}
}