package l2p.gameserver.serverpackets;

import java.util.Map;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;

/**
 * Format: (c) d[dS]
 * d: list size
 * [
 *   d: char ID
 *   S: char Name
 * ]
 *
 * Пример с оффа:
 * C2 02 00 00 00 D0 33 08 00 43 00 4B 00 4A 00 49 00 41 00 44 00 75 00 4B 00 00 00 D0 A7 09 00 53 00 65 00 6B 00 61 00 73 00 00 00
 */
public class PackageToList extends L2GameServerPacket
{
	private Map<Integer, String> characters;

	public PackageToList(L2Player player)
	{
		characters = player.getAccountChars();
		if(characters.size() < 1)
		{
			characters = null;
			player.sendPacket(Msg.THAT_CHARACTER_DOES_NOT_EXIST);
			return;
		}
	}

	@Override
	protected boolean canWrite()
	{
		if(characters == null)
			return false;
		return true;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(characters.size());
		for(Integer char_id : characters.keySet())
		{
			writeD(char_id); // Character object id
			writeS(characters.get(char_id)); // Character name
		}
	}
}