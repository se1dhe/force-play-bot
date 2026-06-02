package l2p.gameserver.serverpackets;

import java.util.TreeSet;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.model.items.ItemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//0x2e TradeStart   d h (h dddhh dhhh)
public class TradeStart extends AbstractItemListPacket
{
	private static Logger _log = LoggerFactory.getLogger(TradeStart.class);

	private static final int IS_FRIEND = 1 << 0;
	private static final int CLAN_MEMBER = 1 << 1;
	private static final int IS_MENTEE_OR_MENTOR = 1 << 2;
	private static final int ALLY_MEMBER = 1 << 3;

	private int _type;
	private TreeSet<L2ItemInstance> _tradelist = new TreeSet<L2ItemInstance>(Inventory.OrderComparator);
	private boolean can_writeImpl = false;
	private int requester_obj_id;
	private int requester_level;

	private int _flags = 0;

	public TradeStart(int type, L2Player me, L2Player other)
	{
		if(me == null)
			return;

		_type = type;
		requester_obj_id = other.getObjectId();
		requester_level = other.getLevel();

		L2ItemInstance[] inventory = me.getInventory().getItems();
		for(L2ItemInstance item : inventory)
		{
			if(item != null)
			{
				if(item.getItemId() == 4037)
				{
					int tradeResultId = item.canBeTradedAlt(me);
					if(tradeResultId == 0)
						_tradelist.add(item);
					else
						_log.info("Result fail player [" + me.getName() + ":" + me.getObjectId() + "] COL id=" + tradeResultId);
				}
				else
				{
					if(item.canBeTraded(me))
						_tradelist.add(item);
				}
			}
		}

		if(me.getFriendList().getList().containsKey(other.getObjectId()))
			_flags |= IS_FRIEND;

		if(me.getClan() != null && me.getClan() == other.getClan())
			_flags |= CLAN_MEMBER;

		//if(me.getMenteeList().getMentor() == other.getObjectId() || other.getMenteeList().getMentor() == me.getObjectId())
		//	_flags |= IS_MENTEE_OR_MENTOR;

		if(me.getAlliance() != null && me.getAlliance() == other.getAlliance())
			_flags |= ALLY_MEMBER;

		can_writeImpl = true;
	}

	@Override
	protected boolean canWrite()
	{
		return can_writeImpl;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		if(_type == 1)
		{
			writeD(requester_obj_id);
			writeH(_flags);
			writeD(_tradelist.size());
		}
		else if(_type == 2)
		{
			writeD(_tradelist.size());
			writeD(_tradelist.size());
			for(L2ItemInstance item : _tradelist)
				writeItemInfo(new ItemInfo(item, false));
		}
	}

	@Override
	protected boolean canWriteIT()
	{
		if(can_writeImpl)
			return _type == 1;
		else
			return false;
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(requester_obj_id);
		writeH(_tradelist.size()); //count??

		for(L2ItemInstance temp : _tradelist)
		{
			writeH(temp.getItem().getType1()); // item type1
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getIntegerLimitedCount());
			writeH(temp.getItem().getType2()); // item type2
			writeH(temp.getCustomType1());
			writeD(temp.getBodyPart()); // rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
			writeH(temp.getEnchantLevel()); // enchant level
			writeH(temp.getCustomType2());
			writeH(0x00);
		}
	}
}