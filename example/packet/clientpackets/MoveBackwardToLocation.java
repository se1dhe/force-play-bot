package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.instancemanager.ZoneBuilderManager;
import l2p.gameserver.instancemanager.ZoneManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Zone;
import l2p.gameserver.model.ObservePoint;
import l2p.gameserver.serverpackets.StopMove;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.MapUtils;
import l2p.gameserver.utils.Util;

import java.nio.BufferUnderflowException;

public class MoveBackwardToLocation extends L2GameClientPacket
{
	private Location _targetLoc = new Location();
	private Location _originLoc = new Location();
	private int _moveMovement;

	@Override
	public void readImpl()
	{
		_targetLoc.x = readD();
		_targetLoc.y = readD();
		_targetLoc.z = readD();
		_originLoc.x = readD();
		_originLoc.y = readD();
		_originLoc.z = readD();
		try
		{
			_moveMovement = readD();
		}
		catch (BufferUnderflowException e)
		{
			if(Config.L2WALKER_PROTECTION)
			{
				L2Player player = getClient().getActiveChar();
				if(player != null)
					Util.handleIllegalPlayerAction(player, "trying to use L2Walker", 1);
			}
		}
	}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(_moveMovement == 0 && !Config.ALLOW_KEYBOARD_MOVE)
		{
			player.sendActionFailed();
			return;
		}

		if(player.isTeleporting())
		{
			player.sendActionFailed();
			return;
		}
		if(player.inObserverMode())
		{
			ObservePoint observer = player.getObservePoint();
			if(observer != null)
				observer.moveToLocation(_targetLoc, 0, false);
			return;
		}
		if(player.isBlocked())
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_MOVE_IN_A_FROZEN_STATE_PLEASE_WAIT_A_MOMENT));
			player.sendActionFailed();
			return;
		}

		if(player.isOutOfControl())
		{
			player.sendActionFailed();
			return;
		}
		if(player.isInVehicle() && (player.getDistance(_targetLoc.x, _targetLoc.y) > 500|| Math.abs(_targetLoc.z - player.getZ()) > 200))
		{
			player.sendPacket(new StopMove(player));
			player.sendActionFailed();
			return;
		}
		player.setActive();
		player.closeEnchant();

		if(player.getTeleMode() > 0)
		{
			if(player.getTeleMode() == 1)
				player.setTeleMode(0);
			player.sendActionFailed();
			player.teleToLocation(_targetLoc);
			return;
		}
		double dx = _targetLoc.x - player.getX();
		double dy = _targetLoc.y - player.getY();
		if(dx * dx + dy * dy > 98010000)
		{
			player.sendActionFailed();
			return;
		}

		int tx = MapUtils.regionX(_targetLoc.getX());
		int ty = MapUtils.regionY(_targetLoc.getY());
		if(tx == 26 && ty == 14)
		{
			boolean targetLocNoMove = ZoneManager.getInstance().getZoneByTypeAndLoc(L2Zone.ZoneType.no_move, _targetLoc);
			if(targetLocNoMove)
			{
				player.sendActionFailed();
				return;
			}
		}

		if(_moveMovement != 0)
		{
			if(player.isGM() && player.isZoneBuilderEnable())
			{
				ZoneBuilderManager zoneBuilderManager = ZoneBuilderManager.getInstance();
				zoneBuilderManager.addZonePoint(player, _targetLoc, true);
				zoneBuilderManager.visualizeZone(player);
				zoneBuilderManager.displayZonePanel(player, player.getZoneBuilderPage());
				player.sendActionFailed();
				return;
			}
		}

		if(Config.DEST_LNR && (_targetLoc.x != _originLoc.x || _targetLoc.y != _originLoc.y || _targetLoc.z != _originLoc.z) && Util.getDistance(_originLoc, _targetLoc) > 15)
			player.getListeners().onDest(_targetLoc.x, _targetLoc.y, _targetLoc.z);
		player.moveBackwardToLocationForPacket(_targetLoc, _moveMovement != 0 && !player.noPathFind);
	}
}