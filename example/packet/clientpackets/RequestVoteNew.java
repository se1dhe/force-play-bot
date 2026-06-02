package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Object;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestVoteNew extends L2GameClientPacket
{
	private int _targetObjectId;

	@Override
	protected void readImpl()
	{
		_targetObjectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(!activeChar.getPlayerAccess().CanEvaluate)
			return;

		L2Object target = activeChar.getTarget();
		if(target == null || !target.isPlayer() || target.getObjectId() != _targetObjectId)
		{
			activeChar.sendPacket(Msg.TARGET_IS_INCORRECT);
			return;
		}

		if(target.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_RECOMMEND_YOURSELF);
			return;
		}

		L2Player targetPlayer = (L2Player) target;

		if(activeChar.getRecomLeft() <= 0)
		{
			activeChar.sendPacket(Msg.NO_MORE_RECOMMENDATIONS_TO_HAVE);
			return;
		}

		if(targetPlayer.getRecomHave() >= 255)
		{
			activeChar.sendPacket(Msg.YOU_NO_LONGER_RECIVE_A_RECOMMENDATION);
			return;
		}

		activeChar.giveRecom(targetPlayer);
		SystemMessage sm = new SystemMessage(SystemMessage.YOU_HAVE_RECOMMENDED);
		sm.addName(targetPlayer);
		sm.addNumber(activeChar.getRecomLeft());
		activeChar.sendPacket(sm);

		sm = new SystemMessage(SystemMessage.YOU_HAVE_BEEN_RECOMMENDED);
		sm.addName(activeChar);
		targetPlayer.sendPacket(sm);

		activeChar.sendUserInfo(false);
		targetPlayer.broadcastUserInfo(true);
	}
}