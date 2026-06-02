package l2p.gameserver.serverpackets;

import l2p.commons.net.nio.impl.SendablePacket;
import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.network.L2GameClient;

import l2p.gameserver.network.ServerPacketOpcodes;
import l2p.gameserver.serverpackets.updatetype.IUpdateTypeComponent;
import l2p.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class L2GameServerPacket extends SendablePacket<L2GameClient> implements IStaticPacket
{
	private static final Logger _log = LoggerFactory.getLogger(L2GameServerPacket.class);

	@Override
	public final boolean write()
	{
		if(Config.DEBUG_PACKETS || ArrayUtils.contains(Config.DEBUG_SERVER_PACKET_LIST, getClass().getSimpleName()))
			System.out.println("SERVER: " + getClass().getSimpleName());

		boolean it = isIT();
		if(it)
		{
			if(!canWriteIT())
				return false;
		}
		else
		{
			if(!canWrite())
				return false;
		}

		try
		{
			if(writeOpcodes())
			{
				if(it)
					writeImplIT();
				else
					writeImpl();
				return true;
			}
		}
		catch(Exception e)
		{
			Log.network("Client: " + getClient() + " - Failed writing: " + getType(), e);
		}
		return false;
	}

	protected ServerPacketOpcodes getOpcodes()
	{
		try
		{
			return ServerPacketOpcodes.valueOf(getClass().getSimpleName());
		}
		catch(Exception e)
		{
			Log.network("Cannot find serverpacket opcode: " + getClass().getSimpleName() + "!");
		}
		return null;
	}

	protected boolean writeOpcodes()
	{
		ServerPacketOpcodes opcodes = getOpcodes();
		if(opcodes == null)
			return false;

		boolean it = isIT();
		int opcode = opcodes.getId(it);
		if(opcode >= 0)
		{
			writeC(opcode);

			int exOpcode = opcodes.getExId(it);
			if(exOpcode >= 0)
				writeH(exOpcode);

			return true;
		}
		return false;
	}

	protected abstract void writeImpl();

	protected void writeImplIT()
	{
		writeImpl();
	}

	protected boolean canWrite()
	{
		return true;
	}

	protected boolean canWriteIT()
	{
		return canWrite();
	}

	protected boolean isIT()
	{
		return getClient().isITClient();
	}

	protected void writeDD(int[] values, boolean sendCount)
	{
		if(sendCount)
			getByteBuffer().putInt(values.length);
		for(int value : values)
			getByteBuffer().putInt(value);
	}

	protected void writeDD(int[] values)
	{
		writeDD(values, false);
	}

	protected void writeD(boolean b)
	{
		writeD(b ? 1 : 0);
	}

	protected void writeC(boolean b)
	{
		writeC(b ? 1 : 0);
	}

	protected void writeOptionalD(int value)
	{
		if(value >= Short.MAX_VALUE)
		{
			writeH(Short.MAX_VALUE);
			writeD(value);
		}
		else
			writeH(value);
	}

	/**
	 * @param masks
	 * @param type
	 * @return {@code true} if the mask contains the current update component type
	 */
	protected static boolean containsMask(int masks, IUpdateTypeComponent type)
	{
		return (masks & type.getMask()) == type.getMask();
	}

	public String getType()
	{
		return "[S] " + getClass().getSimpleName();
	}

	@Override
	public L2GameServerPacket packet(L2Player player)
	{
		return this;
	}
}