package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlockList extends L2GameServerPacket
{
	private List<String[]> _blockList = new ArrayList<String[]>();

	public BlockList(L2Player player)
	{
		Collection<String> blockList = player.getBlockList();
		for(String block : blockList)
			_blockList.add(new String[]{block, ""});
	}

	@Override
	protected void writeImpl()
	{
		writeD(_blockList.size());
		for(String[] block : _blockList)
		{
			writeS(block[0]);
			writeS(block[1]);
		}
	}
}