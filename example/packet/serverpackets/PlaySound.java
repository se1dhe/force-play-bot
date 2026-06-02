package l2p.gameserver.serverpackets;

import l2p.gameserver.utils.Location;

public class PlaySound extends L2GameServerPacket
{
	public static final L2GameServerPacket SIEGE_VICTORY = new PlaySound("Siege_Victory");
	public static final L2GameServerPacket B04_S01 = new PlaySound("B04_S01");
	public static final L2GameServerPacket HB01 = new PlaySound(PlaySound.Type.MUSIC, "HB01", 0, 0, null);

	public enum Type
	{
		SOUND,
		MUSIC,
		VOICE
	}

	private int _type;
	private String _soundFile;
	private int _hasCenterObject;
	private int _objectId;
	private Location _loc = new Location();

	public PlaySound(String soundFile)
	{
		this(0, soundFile, 0, 0, null);
	}

	public PlaySound(Type type, String soundFile, int c, int objectId, Location loc)
	{
		this(type.ordinal(), soundFile, c, objectId, loc);
	}

	public PlaySound(int type, String soundFile, int c, int objectId, Location loc)
	{
		_type = type;
		_soundFile = soundFile;
		_hasCenterObject = c;
		_objectId = objectId;
		if(loc != null)
			_loc = loc;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_type); //0 for quest and ship, c4 toturial = 2
		writeS(_soundFile);
		writeD(_hasCenterObject); //0 for quest; 1 for ship;
		writeD(_objectId); //0 for quest; objectId of ship
		writeD(_loc.x); //x
		writeD(_loc.y); //y
		writeD(_loc.z); //z
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_type); //0 for quest and ship, c4 toturial = 2
		writeS(_soundFile);
		writeD(_hasCenterObject); //0 for quest; 1 for ship;
		writeD(_objectId); //0 for quest; objectId of ship
		writeD(_loc.x); //x
		writeD(_loc.y); //y
		writeD(_loc.z); //z
		writeD(_loc.h);
	}
}