package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.serverpackets.VersionCheck;
import l2p.gameserver.utils.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtocolVersion extends L2GameClientPacket
{
	private static Logger _log = LoggerFactory.getLogger(ProtocolVersion.class);
	private int _version;
	private byte[] _data;
	private byte[] _check;

	@Override
	protected void readImpl()
	{
			_version = readD();
	}

	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		if(_version == -2)
			client.closeNow(false);
		else if(!Config.AVAILABLE_PROTOCOL_REVISIONS.contains(_version))
		{
			_log.info("Wrong protocol revision: " + _version + ", client IP: " + client.getIpAddr());
			client.close(new VersionCheck(null));
		}
		else
		{
			client.setProtocolOk(true);
			client.setRevision(_version);
			sendPacket(new VersionCheck(client.enableCrypt()));
		}
	}
}