package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Object;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.scripts.Events;

public class Action extends L2GameClientPacket
{
	private int _objectId;
	private int _actionId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		readD();
		readD();
		readD();
		_actionId = readC();
	}

	@Override
	protected void runImpl()
	{
		if(_objectId <= 0)
			return;

		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		onAction(player, _objectId, _actionId == 1);
	}

	public static void onAction(L2Player player, int objectId, boolean shift)
	{
		if(player.inObserverMode())
		{
			if(player.isGM() && shift)
			{
				if(player.getTargetId() == objectId)
				{
					Events.onAction(player, player.getTarget(), true);
					return;
				}
				L2Object obj = L2ObjectsStorage.findObject(objectId);
				if(obj != null)
				{
					player.setTarget(obj);
					Events.onAction(player, obj, true);
					return;
				}
			}
			player.sendActionFailed();
			return;
		}
		if(player.isOutOfControl())
		{
			player.sendActionFailed();
			return;
		}
		if(player.isInStoreMode())
		{
			player.sendActionFailed();
			return;
		}
		L2Object obj = player.getVisibleObject(objectId);
		if(obj == null && ((obj = L2ObjectsStorage.getItemByObjId(objectId)) == null || !player.isInRange(obj, 1000)))
		{
			player.sendActionFailed();
			return;
		}
		obj.onAction(player, shift);
	}
}