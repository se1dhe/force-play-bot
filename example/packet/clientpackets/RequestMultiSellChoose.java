package l2p.gameserver.clientpackets;

import l2p.commons.math.SafeMath;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.MultiSellHolder;
import l2p.gameserver.data.xml.holder.MultiSellHolder.MultiSellListContainer;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.base.L2Augmentation;
import l2p.gameserver.model.base.MultiSellEntry;
import l2p.gameserver.model.base.MultiSellIngredient;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.StatusUpdate;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.tables.PetDataTable;
import l2p.gameserver.templates.L2Item;
import l2p.gameserver.templates.L2Weapon;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.Util;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.List;

public class RequestMultiSellChoose extends L2GameClientPacket
{
	// format: cdddhdddddddddd
	private static final Logger _log = LoggerFactory.getLogger(RequestMultiSellChoose.class);
	private int _listId;
	private int _entryId;
	private int _amount;
	private int _enchant = 0;
	private boolean _keepenchant = false;
	private boolean _maxEnchantFromIngredients = false;
	private boolean _extra = false;
	private boolean _notax = false;
	private MultiSellListContainer _list = null;
	private List<ItemData> _items = new ArrayList<ItemData>();

	private class ItemData
	{
		private final int _id;
		private final long _count;
		private final L2ItemInstance _item;

		public ItemData(int id, long count, L2ItemInstance item)
		{
			_id = id;
			_count = count;
			_item = item;
		}

		public int getId()
		{
			return _id;
		}

		public long getCount()
		{
			return _count;
		}

		public L2ItemInstance getItem()
		{
			return _item;
		}

		@Override
		public int hashCode()
		{
			return _id;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(!(obj instanceof ItemData))
				return false;

			ItemData i = (ItemData) obj;

			return _id == i._id && _count == i._count && _item == i._item;
		}
	}

	@Override
	protected void readImpl()
	{
		try
		{
			_listId = readD();
			_entryId = readD();
			// TODO [V] - long
			if(getClient().isITClient())
				_amount = readD();
			else
				_amount = (int) readQ();
		}
		catch(BufferUnderflowException e)
		{}
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_amount < 1)
		{
			activeChar.sendActionFailed();
			return;
		}
		_amount = Math.min(_amount, Config.MULTISELL_MAX_AMOUNT);

		boolean bbs = activeChar.getLastNpcId() == -1;
		boolean isItemAccess = Config.MULTISELL_ACCESS_ITEMS.containsValue(_listId);
		L2NpcInstance npc = activeChar.getLastNpc();
		if(!bbs && !isItemAccess && (!L2NpcInstance.canBypassCheck(activeChar, npc) || !activeChar.checkLastNpc()))
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "В подобных условиях запрещено!" : "Impossible under such conditions!");
			activeChar.sendActionFailed();
			return;
		}
		if(bbs && !ArrayUtils.contains(Config.CB_MULTISELLS, _listId))
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Запрещенный мультиселл!" : "Forbidden meltisell!");
			activeChar.sendActionFailed();
			return;
		}

		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && !bbs && !isItemAccess && activeChar.getKarma() > 0 && !ArrayUtils.contains(Config.ALT_GAME_KARMA_NPC, npc.getNpcId()))
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Избавьтесь от кармы!" : "You have a karma!");
			activeChar.sendActionFailed();
			return;
		}

		if(bbs && !Config.ALLOW_PVPCB_KARMA_SHOP && activeChar.getKarma() > 0)
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "С кармой это не канает!" : "You can't do it with karma!");
			activeChar.sendActionFailed();
			return;
		}

		_list = activeChar.getMultisell();

		// На всякий случай...
		if(_list == null)
		{
			activeChar.sendActionFailed();
			activeChar.setMultisell(null);
			return;
		}

		// Проверяем, не подменили ли id
		if(activeChar.getMultisell().getListId() != _listId)
		{
			Util.handleIllegalPlayerAction(activeChar, "RequestMultiSellChoose[110] Tried to buy from multisell: " + _listId, 1);
			activeChar.sendActionFailed();
			activeChar.setMultisell(null);
			return;
		}

		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "На олимпиаде это не канает!" : "At the Olympics to use the exchange is forbidden!");
			activeChar.sendActionFailed();
			activeChar.setMultisell(null);
			return;
		}

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		_keepenchant = _list.isKeepEnchant();
		_maxEnchantFromIngredients = _list.isMaxEnchantFromIngredients();
		_extra = _list.isExtra();
		_notax = _list.isNoTax();

		for(MultiSellEntry entry : _list.getEntries())
			if(entry.getEntryId() == _entryId)
			{
				doExchange(activeChar, entry);
				break;
			}
	}

	private void doExchange(L2Player activeChar, MultiSellEntry entry)
	{
		PcInventory inv = activeChar.getInventory();

		int totalAdenaCost = 0;
		L2NpcInstance merchant = activeChar.getLastNpc();
		Castle castle = merchant != null ? merchant.getCastle() : null;

		List<MultiSellIngredient> productId = entry.getProduction();

		if(_keepenchant)
		{
			if(_maxEnchantFromIngredients)
			{
				for(MultiSellIngredient ingr : entry.getIngredients())
				{
					if(ingr.getItemId() > 0 && ingr.getItemId() < 65336)
					{
						L2ItemInstance[] items = inv.getAllItemsById(ingr.getItemId());
						if(items != null)
						{
							for(L2ItemInstance item : items)
							{
								_enchant = Math.max(_enchant, item.getEnchantLevel());
							}
						}
					}
				}
			}
			else
			{
				for(MultiSellIngredient p : productId)
					_enchant = Math.max(_enchant, p.getItemEnchant());
			}
		}

		String itemIds = "";
		String counts = "";
		String dItemId = "";
		String dCount = "";

		inv.writeInvLock();
		try
		{
			int tax = SafeMath.mulAndCheck(entry.getTax(), _amount);

			int slots = inv.slotsLeft();
			if(slots == 0)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.THE_WEIGHT_AND_VOLUME_LIMIT_OF_INVENTORY_MUST_NOT_BE_EXCEEDED));
				return;
			}

			int req = 0;
			long totalLoad = 0;
			for(MultiSellIngredient i : productId)
			{
				if(i.getItemId() <= 0 || i.getItemId() >= 65336)
					continue;
				if(activeChar.itemLimM(i, _amount))
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Много берете! Нет места!" : "Take a lot! No space!");
					return;
				}
				totalLoad += ItemTable.getInstance().getTemplate(i.getItemId()).getWeight() * _amount;
				if(!ItemTable.getInstance().getTemplate(i.getItemId()).isStackable())
					req += _amount;
				else
					req++;
			}
			if(req > slots || !inv.validateWeight(totalLoad))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.THE_WEIGHT_AND_VOLUME_LIMIT_OF_INVENTORY_MUST_NOT_BE_EXCEEDED));
				return;
			}

			if(entry.getIngredients().size() == 0)
			{
				_log.warn("Ingredients list = 0 multisell id=:" + _listId + " player: " + activeChar.toString());
				activeChar.sendActionFailed();
				activeChar.setMultisell(null);
				return;
			}

			List<L2Augmentation> augmentations = new ArrayList<L2Augmentation>();
			List<Integer> enchants = _extra ? new ArrayList<Integer>() : null;

			// Перебор всех ингридиентов, проверка наличия и создание списка забираемого
			for(MultiSellIngredient ingridient : entry.getIngredients())
			{
				int ingridientItemId = ingridient.getItemId();
				long ingridientItemCount = ingridient.getItemCount();
				long total_amount = !ingridient.getMantainIngredient() ? ingridientItemCount * _amount : ingridientItemCount;

				if(total_amount <= 0 || total_amount > Integer.MAX_VALUE)
				{
					activeChar.sendActionFailed();
					return;
				}

				if(ingridientItemId > 0 && ingridientItemId < 65336 && !ItemTable.getInstance().getTemplate(ingridientItemId).isStackable())
				{
					for(int i = 0; i < ingridientItemCount * _amount; i++)
					{
						L2ItemInstance[] list = inv.getAllItemsById(ingridientItemId);
						// Если энчант имеет значение - то ищем вещи с точно таким энчантом
						if(_keepenchant)
						{
							L2ItemInstance itemToTake = null;

							if(_maxEnchantFromIngredients)
							{
								if(list == null || list.length == 0)
								{
									activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
									return;
								}

								for(L2ItemInstance itm : list)
								{
									boolean alreadyUsed = false;
									for(ItemData used : _items)
									{
										if(used.getItem() == itm)
										{
											alreadyUsed = true;
											break;
										}
									}

									if(alreadyUsed)
									{
										continue;
									}

									if(!itm.isShadowItem() && !itm.isTemporalItem() &&
											(itm.getCustomFlags() & L2ItemInstance.FLAG_NO_TRADE) != L2ItemInstance.FLAG_NO_TRADE)
									{
										if(itemToTake == null || itm.getEnchantLevel() > itemToTake.getEnchantLevel())
										{
											itemToTake = itm;
										}
									}
								}
							}
							else
							{
								for(L2ItemInstance itm : list)
								{
									if((itm.getEnchantLevel() == _enchant || itm.getItem().getType2() > 2 || (_extra && itm.getItem().getType2() < 3)) && !_items.contains(new ItemData(itm.getItemId(), itm.getCount(), itm)) && !itm.isShadowItem() && !itm.isTemporalItem() && (itm.getCustomFlags() & L2ItemInstance.FLAG_NO_TRADE) != L2ItemInstance.FLAG_NO_TRADE)
									{
										if(_extra && itm.getItem().getType2() < 3)
											enchants.add(itm.getEnchantLevel());
										itemToTake = itm;
										break;
									}
								}
							}

							if(itemToTake == null)
							{
								activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
								return;
							}

							if(!checkItem(itemToTake, activeChar))
							{
								activeChar.sendActionFailed();
								return;
							}

							if(itemToTake.getAugmentation() != null)
							{
								itemToTake.setWhFlag(true);
								augmentations.add(itemToTake.getAugmentation());
							}
							if(!ingridient.getMantainIngredient())
								_items.add(new ItemData(itemToTake.getItemId(), 1, itemToTake));
						}
						// Если энчант не обрабатывается берется вещь с наименьшим энчантом
						else
						{
							L2ItemInstance itemToTake = null;
							for(L2ItemInstance itm : list)
								if(!_items.contains(new ItemData(itm.getItemId(), itm.getCount(), itm)) && (itemToTake == null || itm.getEnchantLevel() < itemToTake.getEnchantLevel()) && !itm.isShadowItem() && !itm.isTemporalItem() && (itm.getCustomFlags() & L2ItemInstance.FLAG_NO_TRADE) != L2ItemInstance.FLAG_NO_TRADE && checkItem(itm, activeChar))
								{
									itemToTake = itm;
									if(itemToTake.getEnchantLevel() == 0)
										break;
								}

							if(itemToTake == null)
							{
								activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
								return;
							}
							if(itemToTake.getAugmentation() != null)
							{
								itemToTake.setWhFlag(true);
								augmentations.add(itemToTake.getAugmentation());
							}
							if(!ingridient.getMantainIngredient())
								_items.add(new ItemData(itemToTake.getItemId(), 1L, itemToTake));
						}
					}
				}
				else if(ingridientItemId == Config.ITEM_ID_CLAN_REPUTATION_SCORE)
				{
					if(activeChar.getClan() == null)
					{
						activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_NOT_A_CLAN_MEMBER));
						return;
					}

					if(activeChar.getClan().getReputationScore() < total_amount)
					{
						activeChar.sendPacket(Msg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
						return;
					}

					if(activeChar.getClan().getLeaderId() != activeChar.getObjectId())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_A_CLAN_LEADER).addString(activeChar.getName()));
						return;
					}
					if(!ingridient.getMantainIngredient())
						_items.add(new ItemData(ingridientItemId, total_amount, null));
				}
				else if(ingridientItemId == Config.ITEM_ID_PC_BANG_POINTS)
				{
					if(activeChar.getPcBangPoints() < total_amount)
					{
						activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_SHORT_OF_ACCUMULATED_POINTS));
						return;
					}
					if(!ingridient.getMantainIngredient())
						_items.add(new ItemData(ingridientItemId, total_amount, null));
				}
				else
				{
					if(ingridientItemId == 57)
						totalAdenaCost += ingridientItemCount * _amount;
					L2ItemInstance item = inv.getItemByItemId(ingridientItemId);

					if(item == null || item.getCount() < total_amount)
					{
						activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
						return;
					}

					if(!ingridient.getMantainIngredient())
						_items.add(new ItemData(item.getItemId(), total_amount, item));
				}

				if(activeChar.getAdena() < totalAdenaCost)
				{
					activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					return;
				}
			}

			for(ItemData id : _items)
			{
				long count = id.getCount();
				if(count > 0)
				{
					L2ItemInstance item = id.getItem();

					if(item != null)
					{
						Log.LogMultisell(activeChar, false, item, item.getItemId(), count, _listId);
						activeChar.sendPacket(SystemMessage.removeItems(item.getItemId(), count));
						inv.destroyItem(item, count, true, "<DestroyItemMultisellCoose>");
					}
					else if(id.getId() == Config.ITEM_ID_CLAN_REPUTATION_SCORE)
					{
						activeChar.getClan().incReputation((int) -count, false, "MultiSell" + _listId);
						activeChar.sendPacket(new SystemMessage(SystemMessage.S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_THE_CLAN_REPUTATION_SCORE).addNumber(count));
					}
					else if(id.getId() == Config.ITEM_ID_PC_BANG_POINTS)
					{
						activeChar.reducePcBangPoints((int)count);
					}
					dItemId += id.getId() + ";";
					dCount += id.getCount() + ";";
				}
			}

			if(tax > 0 && !_notax)
				if(castle != null)
				{
					activeChar.sendMessage("Tax: " + tax);
					if(merchant != null)
						castle.addToTreasury(tax, true, false);
				}

			for(MultiSellIngredient in : productId)
			{
				if(in.getItemId() == Config.ITEM_ID_CLAN_REPUTATION_SCORE)
				{
					activeChar.getClan().incReputation(in.getItemCount() * _amount, false, "MultiSell" + _listId);
					activeChar.sendPacket(new SystemMessage(SystemMessage.YOUR_CLAN_HAS_ADDED_1S_POINTS_TO_ITS_CLAN_REPUTATION_SCORE).addNumber(in.getItemCount() * _amount));
				}
				else if(in.getItemId() == Config.ITEM_ID_PC_BANG_POINTS)
				{
					activeChar.addPcBangPoints(in.getItemCount() * _amount, false);
				}
				else if(ItemTable.getInstance().getTemplate(in.getItemId()).isStackable())
				{
					L2ItemInstance product = ItemTable.getInstance().createItem(in.getItemId());
					double total = in.getItemCount() * _amount;

					if(total < 0 || total > Integer.MAX_VALUE)
					{
						activeChar.sendActionFailed();
						return;
					}

					product.setCount((long) total);
					activeChar.sendPacket(SystemMessage.obtainItems(product));
					inv.addItem(product, true, Config.LOG_IN_ITEMS_ALL_MULTISELLS, true, "<RequestMultisellStackable");
					Log.LogMultisell(activeChar, true, null, in.getItemId(), (long) total, _listId);
					if (Config.DONATE_MULTISELLS.contains(_listId)) {
						String act = "Multisell (list: " + _listId + ") (item: " + product + ") (x" + total + ") (npc:" + (merchant == null ? "null" : merchant.getTemplate().npcId) + ") #(player " + activeChar.getName() + ", account: " + activeChar.getAccountName() + ", ip: " + activeChar.getIP() + ", hwid: " + activeChar.getHWID() + ")";

						Log.donate("donate" + _listId, act.toString());
					}
				}
				else
				{
					int cnt = _amount * in.getItemCount();
					for(int i = 0; i < cnt; i++)
					{
						L2ItemInstance product = ItemTable.getInstance().createItem(in.getItemId());
						if(_keepenchant)
						{
							if(_extra)
							{
								if(_maxEnchantFromIngredients)
								{
									if(!enchants.isEmpty() && product.getItem().getType2() < 3)
										product.setEnchantLevel(enchants.remove(0));
									else if(product.getItem().getType2() < 3)
										product.setEnchantLevel(_enchant);
								}
								else
								{
									if(!enchants.isEmpty() && product.getItem().getType2() < 3)
										product.setEnchantLevel(enchants.remove(0));
								}
							}
							else
								product.setEnchantLevel(_enchant);
						}
						else if(product.getItem().getType2() < 3)
							product.setEnchantLevel(in.getItemEnchant());
						if(!augmentations.isEmpty() && product.getItem() instanceof L2Weapon && product.canBeEnchanted())
						{
							L2Augmentation augmentation = augmentations.remove(0);
							augmentation.setItem(product);
							product.setAugmentation(augmentation);
						}
						Log.LogMultisell(activeChar, true, product, product.getItemId(), product.getCount(), _listId);
						if (Config.DONATE_MULTISELLS.contains(_listId)) {
							String act = "Multisell (list: " + _listId + ") (item: " + product + ")(npc:" + (merchant == null ? "null" : merchant.getTemplate().npcId) + ") #(player " + activeChar.getName() + ", account: " + activeChar.getAccountName() + ", ip: " + activeChar.getIP() + ", hwid: " + activeChar.getHWID() + ")";

							Log.donate("donate" + _listId, act.toString());
						}
						activeChar.sendPacket(SystemMessage.obtainItems(product));
						inv.addItem(product, true, Config.LOG_IN_ITEMS_ALL_MULTISELLS, true, "<RequestMultisellStackable");
					}
				}
				itemIds += in.getItemId() + ";";
				counts += in.getItemCount() + ";";
			}
		}
		catch (ArithmeticException ae)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}
		finally
		{
			inv.writeInvUnlock();
		}

		activeChar.sendPacket(new StatusUpdate(activeChar).addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad()));

		if(_list == null || !_list.isShowAll()) // Если показывается только то, на что хватает материалов обновить окно у игрока
			MultiSellHolder.getInstance().SeparateAndSend(_listId, activeChar, castle == null ? 0 : castle.getTaxRate());
	}

	private static boolean checkItem(L2ItemInstance temp, L2Player activeChar)
	{
		if(temp == null)
		{
			activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
			return false;
		}

		if(temp.isHeroItem())
			return false;

		if(temp.isShadowItem())
			return false;

		if(temp.isTemporalItem())
			return false;

		if(PetDataTable.isPetControlItem(temp) && activeChar.isMounted())
			return false;

		if(activeChar.getPet() != null && temp.getObjectId() == activeChar.getPet().getControlItemId())
			return false;
		if(activeChar.getAgathion() != null && temp.getObjectId() == activeChar.getAgathion().getControlItemId())
			return false;

		if(temp.isEquipped())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Перед обменом, нужно снять предмет!" : "You must unequip item before exchange!");
			return false;
		}

		if(activeChar.getEnchantScroll() == temp)
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Перед обменом, нужно закрыть заточку!" : "You must close enchant before exchange!");
			return false;
		}

		return true;
	}
}