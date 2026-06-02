package l2p.gameserver.clientpackets;

import java.nio.BufferUnderflowException;

import l2p.gameserver.Config;
import l2p.gameserver.Shutdown;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.network.authcomm.AuthServerCommunication;
import l2p.gameserver.network.authcomm.SessionKey;
import l2p.gameserver.network.authcomm.gspackets.PlayerAuthRequest;
import l2p.gameserver.serverpackets.LoginFail;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthLogin extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(AuthLogin.class);
	private String _loginName;
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;
	private byte[] _hwid;
	private boolean _cond;

	@Override
	protected void readImpl()
	{
		try
		{
			_loginName = readS(32).toLowerCase();
		}
		catch(BufferUnderflowException e)
		{
			_cond = true;
			return;
		}
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
		if(!getClient().isITClient()) // TODO [V] - нужно?
		{
			readD();
			readQ();
			readD();
			if(Config.ACTIVE_AC)
			{
				_hwid = new byte[32];
				try
				{
					readB(_hwid);
				}
				catch(BufferUnderflowException e)
				{
					_cond = true;
					_log.error("Active Anticheat read hwid on Acc: " + _loginName + " IP: " + getClient().getIpAddr(), e);
				}
			}
		}
		else
		{
			if(Config.ACTIVE_AC)
			{
				_hwid = new byte[32];
				try
				{
					readD();
					readB(_hwid);
				}
				catch(BufferUnderflowException e)
				{
					_cond = true;
					_log.error("Active Anticheat read hwid on Acc: " + _loginName + " IP: " + getClient().getIpAddr(), e);
				}
			}
		}
	}

	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		if(_cond || !client.isProtocolOk())
		{
			client.closeNow(true);
			return;
		}
		if(Config.ACTIVE_AC)
			client.setHWID(Util.maskAA(_hwid));

		if(Config.LAME_GUARD || Config.ACTIVE_AC)
		{
			if(client.getHWID() == null)
			{
				_log.info("NULL HWID from IP: " + client.getIpAddr() + " Acc: " + _loginName);
				client.closeNow(true);
				return;
			}
			if(client.isHWIDBanned())
			{
				_log.info("Rejected banned HWID: " + client.getHWID() + " IP: " + client.getIpAddr() + " Acc: " + _loginName);
				client.closeNow(true);
				return;
			}
		}

		if(Shutdown.getInstance().getMode() != Shutdown.NONE && Shutdown.getInstance().getSeconds() <= 30)
		{
			client.closeNow(false);
			return;
		}

		if(AuthServerCommunication.getInstance().isShutdown())
		{
			client.close(LoginFail.SYSTEM_ERROR_LOGIN_LATER);
			return;
		}

		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);
		client.setSessionId(key);
		client.setLoginName(_loginName);

		if(Config.LOG_AUTH_LOGIN)
			Log.addLog("Client with IP " + client.getIpAddr() + " and HWID " + client.getHWID() + " auth into account " + client.getLoginName(), "auth_login");

		L2GameClient oldClient = AuthServerCommunication.getInstance().addWaitingClient(client);
		if(oldClient != null)
			oldClient.close(Msg.ServerClose);
		AuthServerCommunication.getInstance().sendPacket(new PlayerAuthRequest(client));
	}
}