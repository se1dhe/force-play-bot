package l2p.gameserver.clientpackets;

import java.nio.BufferUnderflowException;

import l2p.commons.net.nio.impl.ReceivablePacket;
import l2p.gameserver.Config;
import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.serverpackets.L2GameServerPacket;

import l2p.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Packets received by the game server from clients
 */
public abstract class L2GameClientPacket extends ReceivablePacket<L2GameClient>
{
	private static final Logger _log = LoggerFactory.getLogger(L2GameClientPacket.class);

	@Override
	public final boolean read()
	{
		if(Config.DEBUG_PACKETS || ArrayUtils.contains(Config.DEBUG_CLIENT_PACKET_LIST, getClass().getSimpleName()))
			System.out.println("CLIENT: " + getClass().getSimpleName());

		try
		{
			readImpl();
			return true;
		}
		catch(BufferUnderflowException e)
		{
			Log.network("Client: " + _client + " - Failed reading: " + getType() + ". Buffer underflow!");
		}
		catch(Exception e)
		{
			Log.network("Client: " + _client + " - Failed reading: " + getType(), e);
		}

		_client.onPacketReadFail();

		return false;
	}

	protected abstract void readImpl() throws Exception;

	@Override
	public final void run()
	{
		L2GameClient client = getClient();
		try
		{
			runImpl();
		}
		catch(Exception e)
		{
			Log.network("Client: " + client + " - Failed running: " + getType(), e);
		}
	}

	protected abstract void runImpl() throws Exception;

	protected String readS(int len)
	{
		String ret = readS();
		return ret.length() > len ? ret.substring(0, len) : ret;
	}

	protected void sendPacket(L2GameServerPacket packet)
	{
		getClient().sendPacket(packet);
	}

	protected void sendPacket(L2GameServerPacket... packets)
	{
		getClient().sendPacket(packets);
	}

	public String getType()
	{
		return "[C] " + getClass().getSimpleName();
	}
}