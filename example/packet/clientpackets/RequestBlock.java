package l2p.gameserver.clientpackets;

import java.util.Collection;

import l2p.gameserver.instancemanager.PlayerManager;
import l2p.gameserver.serverpackets.BlockList;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.model.L2Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestBlock extends L2GameClientPacket
{
	// format: cd(S)
	private static final Logger _log = LoggerFactory.getLogger(RequestBlock.class);

	private final static int BLOCK = 0;
	private final static int UNBLOCK = 1;
	private final static int BLOCKLIST = 2;
	private final static int ALLBLOCK = 3;
	private final static int ALLUNBLOCK = 4;

	private Integer _type;
	private String targetName = null;

	@Override
	protected void readImpl()
	{
		_type = readD(); //0x00 - block, 0x01 - unblock, 0x03 - allblock, 0x04 - allunblock

		if(_type == BLOCK || _type == UNBLOCK)
			targetName = readS(16);
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		switch(_type)
		{
			case BLOCK:
				activeChar.addToBlockList(targetName);
				break;
			case UNBLOCK:
				activeChar.removeFromBlockList(PlayerManager.getObjectIdByName(targetName));
				break;
			case BLOCKLIST:
				if(activeChar.isITClient())
				{
					Collection<String> blockList = activeChar.getBlockList();

					if(blockList != null)
					{
						activeChar.sendPacket(new SystemMessage(SystemMessage._IGNORE_LIST_));

						for(String name : blockList)
							activeChar.sendMessage(name);

						activeChar.sendPacket(new SystemMessage(SystemMessage.__EQUALS__));
					}
				}
				else
				{
					activeChar.sendPacket(new BlockList(activeChar));
				}
				break;
			case ALLBLOCK:
				activeChar.setBlockAll(true);
				activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_NOW_BLOCKING_EVERYTHING));
				activeChar.sendEtcStatusUpdate();
				break;
			case ALLUNBLOCK:
				activeChar.setBlockAll(false);
				activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_NO_LONGER_BLOCKING_EVERYTHING));
				activeChar.sendEtcStatusUpdate();
				break;
			default:
				_log.info("Unknown 0x0a block type: " + _type);
		}
	}
}