package l2p.gameserver.clientpackets;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.model.base.Transaction;
import l2p.gameserver.model.base.Transaction.TransactionType;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.serverpackets.TradeOtherAdd;
import l2p.gameserver.serverpackets.TradeOwnAdd;
import l2p.gameserver.serverpackets.TradeUpdate;

public class AddTradeItem extends L2GameClientPacket
{
	//Format: cddd

	@SuppressWarnings("unused")
	private int _tradeId, _objectId, _amount;

	@Override
	protected void readImpl()
	{
		_tradeId = readD(); // 1 ?
		_objectId = readD();
		// TODO [V] - long есть
		_amount = (int) (getClient().isITClient() ? readD() : readQ());
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(activeChar == null || _amount < 1)
			return;

		Transaction transaction = activeChar.getTransaction();

		if(transaction == null)
			return;

		if(!transaction.isValid() || !transaction.isTypeOf(TransactionType.TRADE))
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}

		if(activeChar.isOutOfControl())
		{
			transaction.cancel();
			activeChar.sendActionFailed();
			return;
		}

		L2Player requestor = transaction.getOtherPlayer(activeChar);

		if(transaction.isConfirmed(activeChar) || transaction.isConfirmed(requestor))
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_MOVE_ADDITIONAL_ITEMS_BECAUSE_TRADE_HAS_BEEN_CONFIRMED, Msg.ActionFail);
			return;
		}

		if(requestor.isOutOfControl())
		{
			transaction.cancel();
			requestor.sendActionFailed();
			return;
		}

		L2ItemInstance InvItem = activeChar.getInventory().getItemByObjectId(_objectId);

		if(InvItem == null || !InvItem.canBeTraded(activeChar))
		{
			activeChar.sendPacket(Msg.THIS_ITEM_CANNOT_BE_TRADED_OR_SOLD);
			return;
		}

		int InvItemCount = InvItem.getIntegerLimitedCount();

		TradeItem tradeItem = getItem(_objectId, transaction.getExchangeList(activeChar));

		int realCount = Math.min(_amount, InvItemCount);
		int leaveCount = InvItemCount - realCount;

		if(tradeItem == null)
		{
			// добавляем новую вещь в список
			tradeItem = new TradeItem(InvItem);
			tradeItem.setCount(realCount);
			transaction.getExchangeList(activeChar).add(tradeItem);
		}
		else
		{
			// меняем количество уже имеющегося
			if(!InvItem.canBeTraded(activeChar))
				return;
			int TradeItemCount = tradeItem.getCount();
			if(InvItemCount == TradeItemCount) // мы уже предлогаем всё что имеем
				return;

			try
			{
				if(_amount + TradeItemCount >= InvItemCount)
					realCount = InvItemCount - TradeItemCount;
			}
			catch(ArithmeticException e)
			{
				activeChar.sendPacket(Msg.SYSTEM_ERROR, Msg.ActionFail);
				return;
			}

			tradeItem.setCount(realCount + TradeItemCount);
			leaveCount = InvItemCount - realCount - TradeItemCount;
		}

		if(Config.ENABLE_TRADEABLE_AUGMENT_ITEMS)
		{
			if(InvItem.isAugmented())
			{
				L2Skill skill = InvItem.getAugmentation().getAugmentSkill();
				if(skill != null)
				{
					if(skill.isActive())
					{
						String sn = "";
						if(InvItem.getAugmentation().getBoni().getStats() != null)
						{
							for(int i = 0; i < InvItem.getAugmentation().getBoni().getStats().length; i++)
							{
								if(activeChar.isLangRus())
									sn += getStatName(activeChar, InvItem.getAugmentation().getBoni().getStats()[i].getValue(), (int) InvItem.getAugmentation().getBoni().getValues()[i]);
								else
									sn += getStatName(activeChar, InvItem.getAugmentation().getBoni().getStats()[i].getValue(), (int) InvItem.getAugmentation().getBoni().getValues()[i]);
							}
						}

						if(activeChar.isLangRus())
							activeChar.sendMessage("Вы добавили предмет в торговлю " + InvItem.getName() +" с аугментацией Активный: " + skill.getName() + " уровень " + skill.getLevel() + sn);
						else
							activeChar.sendMessage("You have added an item to trade " + InvItem.getName() +" with augmentation Active: " + skill.getName() + " level " + skill.getLevel() + sn);

						if(requestor.isLangRus())
							requestor.sendMessage(activeChar.getName() + " добавил предмет в торговлю " + InvItem.getName() +" с аугментацией Активный: " + skill.getName() + " уровень " + skill.getLevel() + sn);
						else
							requestor.sendMessage(activeChar.getName() + " added item to trade " + InvItem.getName() +" with augmentation Active: " + skill.getName() + " level " + skill.getLevel() + sn);
					}
					else if(skill.isOnAction())
					{
						String sn = "";
						if(InvItem.getAugmentation().getBoni().getStats() != null)
						{
							for(int i = 0; i < InvItem.getAugmentation().getBoni().getStats().length; i++)
							{
								if(activeChar.isLangRus())
									sn += getStatName(activeChar, InvItem.getAugmentation().getBoni().getStats()[i].getValue(), (int) InvItem.getAugmentation().getBoni().getValues()[i]);
								else
									sn += getStatName(activeChar, InvItem.getAugmentation().getBoni().getStats()[i].getValue(), (int) InvItem.getAugmentation().getBoni().getValues()[i]);
							}
						}

						if(activeChar.isLangRus())
							activeChar.sendMessage("Вы добавили предмет в торговлю " + InvItem.getName() + " с аугментацией Шансовый: " + skill.getName() + " уровень " + skill.getLevel() + sn);
						else
							activeChar.sendMessage("You have added an item to trade " + InvItem.getName() + " with augmentation Chance: " + skill.getName() + " level " + skill.getLevel() + sn);

						if(requestor.isLangRus())
							requestor.sendMessage(activeChar.getName() + " добавил предмет в торговлю " + InvItem.getName() + " с аугментацией Шансовый: " + skill.getName() + " уровень " + skill.getLevel() + sn);
						else
							requestor.sendMessage(activeChar.getName() + " added item to trade " + InvItem.getName() + " with augmentation Chance: " + skill.getName() + " level " + skill.getLevel() + sn);
					}
					else if(skill.isPassive())
					{
						String sn = "";
						if(InvItem.getAugmentation().getBoni().getStats() != null)
						{
							for(int i = 0; i < InvItem.getAugmentation().getBoni().getStats().length; i++)
							{
								if(activeChar.isLangRus())
									sn += getStatName(activeChar, InvItem.getAugmentation().getBoni().getStats()[i].getValue(), (int) InvItem.getAugmentation().getBoni().getValues()[i]);
								else
									sn += getStatName(activeChar, InvItem.getAugmentation().getBoni().getStats()[i].getValue(), (int) InvItem.getAugmentation().getBoni().getValues()[i]);
							}
						}

						if(activeChar.isLangRus())
							activeChar.sendMessage("Вы добавили предмет в торговлю " + InvItem.getName() + " с аугментацией Пасивный: " + skill.getName() + " уровень " + skill.getLevel() + sn);
						else
							activeChar.sendMessage("You have added an item to trade " + InvItem.getName() + " with augmentation Passive: " + skill.getName() + " level " + skill.getLevel() + sn);

						if(requestor.isLangRus())
							requestor.sendMessage(activeChar.getName() + " добавил предмет в торговлю " + InvItem.getName() + " с аугментацией Пасивный: " + skill.getName() + " уровень " + skill.getLevel() + sn);
						else
							requestor.sendMessage(activeChar.getName() + " added item to trade " + InvItem.getName() + " with augmentation Passive: " + skill.getName() + " level " + skill.getLevel() + sn);
					}
					else
					{
						String sn = "";
						if(InvItem.getAugmentation().getBoni().getStats() != null)
						{
							for(int i = 0; i < InvItem.getAugmentation().getBoni().getStats().length; i++)
							{
								if(activeChar.isLangRus())
									sn += getStatName(activeChar, InvItem.getAugmentation().getBoni().getStats()[i].getValue(), (int) InvItem.getAugmentation().getBoni().getValues()[i]);
								else
									sn += getStatName(activeChar, InvItem.getAugmentation().getBoni().getStats()[i].getValue(), (int) InvItem.getAugmentation().getBoni().getValues()[i]);
							}
						}
						if(activeChar.isLangRus())
							activeChar.sendMessage("Вы добавили предмет в торговлю " + InvItem.getName() + " с аугментацией " + skill.getName() + " уровень " + skill.getLevel() + sn);
						else
							activeChar.sendMessage("You have added an item to trade " + InvItem.getName() + " with augmentation " + skill.getName() + " level " + skill.getLevel() + sn);

						if(requestor.isLangRus())
							requestor.sendMessage(activeChar.getName() + " добавил предмет в торговлю " + InvItem.getName() + " с аугментацией " + skill.getName() + " уровень " + skill.getLevel() + sn);
						else
							requestor.sendMessage(activeChar.getName() + " added item to trade " + InvItem.getName() + " with augmentation " + skill.getName() + " level " + skill.getLevel() + sn);
					}
				}
				else if(InvItem.getAugmentation().getBoni().getStats() != null)
				{
					String sn = "";
					for(int i = 0; i < InvItem.getAugmentation().getBoni().getStats().length; i++)
					{
						if(activeChar.isLangRus())
							sn += getStatName(activeChar, InvItem.getAugmentation().getBoni().getStats()[i].getValue(), (int) InvItem.getAugmentation().getBoni().getValues()[i]);
						else
							sn += getStatName(activeChar, InvItem.getAugmentation().getBoni().getStats()[i].getValue(), (int) InvItem.getAugmentation().getBoni().getValues()[i]);
					}
					if(activeChar.isLangRus())
						activeChar.sendMessage("Вы добавили предмет в торговлю " + InvItem.getName() + sn);
					else
						activeChar.sendMessage("You have added an item to trade " + InvItem.getName() + sn);

					if(requestor.isLangRus())
						requestor.sendMessage(activeChar.getName() + " добавил предмет в торговлю " + InvItem.getName() + sn);
					else
						requestor.sendMessage(activeChar.getName() + " added item to trade " + InvItem.getName() + sn);
				}
				else
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Вы добавили предмет в торговлю " + InvItem.getName() + " с аугментацией без скила.");
					else
						activeChar.sendMessage("You have added an item to trade " + InvItem.getName() + " with augmentation without skill.");

					if(requestor.isLangRus())
						requestor.sendMessage(activeChar.getName() + " добавил предмет в торговлю " + InvItem.getName() + " с аугментацией без скила.");
					else
						requestor.sendMessage(activeChar.getName() + " added item to trade " + InvItem.getName() + " with augmentation without skill.");
				}
			}
		}

		activeChar.sendPacket(new TradeOwnAdd(1, InvItem, realCount), new TradeOwnAdd(2, InvItem, realCount));
		activeChar.sendPacket(new TradeUpdate(1, InvItem, leaveCount), new TradeUpdate(2, InvItem, leaveCount));
		requestor.sendPacket(new TradeOtherAdd(1, InvItem, realCount), new TradeOtherAdd(2, InvItem, realCount));
	}

	private static String getStatName(L2Player player, String s, int v)
	{
		String sn = "";
		if(s.equals("CON"))
		{
			if(player.isLangRus())
				sn += " [Модификатор " + "CON+" + v + "]";
			else
				sn += " [Modifier " + "CON+" + v + "]";
		}
		else if(s.equals("STR"))
		{
			if(player.isLangRus())
				sn += " [Модификатор " + "STR+" + v + "]";
			else
				sn += " [Modifier " + "STR+" + v + "]";
		}
		else if(s.equals("DEX"))
		{
			if(player.isLangRus())
				sn += " [Модификатор " + "DEX+" + v + "]";
			else
				sn += " [Modifier " + "DEX+" + v + "]";
		}
		else if(s.equals("INT"))
		{
			if(player.isLangRus())
				sn += " [Модификатор " + "INT+" + v + "]";
			else
				sn += " [Modifier " + "INT+" + v + "]";
		}
		else if(s.equals("MEN"))
		{
			if(player.isLangRus())
				sn += " [Модификатор " + "MEN+" + v + "]";
			else
				sn += " [Modifier " + "MEN+" + v + "]";
		}
		else if(s.equals("WIT"))
		{
			if(player.isLangRus())
				sn += " [Модификатор " + "WIT+" + v + "]";
			else
				sn += " [Modifier " + "WIT+" + v + "]";
		}
		return sn;
	}

	private static TradeItem getItem(int objId, ConcurrentLinkedQueue<TradeItem> collection)
	{
		for(TradeItem item : collection)
			if(item.getObjectId() == objId)
				return item;
		return null;
	}
}