package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.model.L2Object;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2World;
import l2p.gameserver.model.base.Transaction;
import l2p.gameserver.model.base.Transaction.TransactionType;
import l2p.gameserver.serverpackets.SendTradeRequest;
import l2p.gameserver.serverpackets.SystemMessage;

public class TradeRequest extends L2GameClientPacket
{
	//Format: cd
	private int _objectId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInFightClub())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isTradeBannedByGM())
		{
			activeChar.sendMessage("You can't use trade.");
			activeChar.sendActionFailed();
			return;
		}

		if(!activeChar.getPlayerAccess().UseTrade)
		{
			activeChar.sendMessage("You can't use trade.");
			activeChar.sendActionFailed();
			return;
		}

		if(Config.SERVICES_DISABLE_TRADE_REQUEST && activeChar.isTradeKeyBlocked())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Нельзя торговать, отключите Lock." : "Cannot be traded, disable Lock.");
			activeChar.sendActionFailed();
			return;
		}

		if((activeChar.getPvpFlag() > 0 || activeChar.isInCombat()) && !activeChar.isGM())
		{
			activeChar.sendMessage("You can't trade in combat or PvP flag.");
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isDead())
		{
			activeChar.sendActionFailed();
			return;
		}

		L2Object target = L2World.getAroundObjectById(activeChar, _objectId);

		if(target == null || !target.isPlayer() || target.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendPacket(Msg.TARGET_IS_INCORRECT);
			return;
		}

		L2Player pcTarget = (L2Player) target;

		if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE || pcTarget.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(pcTarget.isTradeBannedByGM())
		{
			activeChar.sendMessage("Your target can't use trade.");
			activeChar.sendActionFailed();
			return;
		}

		if(!pcTarget.getPlayerAccess().UseTrade)
		{
			activeChar.sendMessage("Your target can't use trade.");
			activeChar.sendActionFailed();
			return;
		}

		if(Config.SERVICES_DISABLE_TRADE_REQUEST && pcTarget.isTradeKeyBlocked())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Нельзя торговать, в цели включен Lock." : "Cannot be traded, the target has Lock enabled.");
			activeChar.sendActionFailed();
			return;
		}

		if((pcTarget.getPvpFlag() > 0 || pcTarget.isInCombat()) && !activeChar.isGM())
		{
			activeChar.sendMessage("Your target can't trade in combat or PvP flag.");
			activeChar.sendActionFailed();
			return;
		}

		if(pcTarget.getTeam() != 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(pcTarget.isInOlympiadMode() || activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(Msg.TARGET_IS_INCORRECT);
			return;
		}

		if(pcTarget.getTradeRefusal())
		{
			activeChar.sendMessage("Your target in trade refusal mode.");
			return;
		}
		if(pcTarget.isBlockAll())
		{
			activeChar.sendMessage("Your target blocks all.");
			return;
		}
		if(pcTarget.isInBlockList(activeChar))
		{
			activeChar.sendPacket(Msg.YOU_HAVE_BEEN_BLOCKED_FROM_THE_CONTACT_YOU_SELECTED);
			return;
		}

		if(activeChar.isInTransaction())
		{
			activeChar.sendPacket(Msg.ALREADY_TRADING);
			return;
		}

		if(pcTarget.isInTransaction())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addString(pcTarget.getName()));
			return;
		}

		if(GeoEngine.noPath(activeChar, pcTarget))
		{
			activeChar.sendPacket(Msg.CANNOT_SEE_TARGET);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		new Transaction(TransactionType.TRADE_REQUEST, activeChar, pcTarget, 10000);
		pcTarget.sendPacket(new SendTradeRequest(activeChar.getObjectId()));
		activeChar.sendPacket(new SystemMessage(SystemMessage.REQUEST_S1_FOR_TRADE).addString(pcTarget.getName()));
	}
}