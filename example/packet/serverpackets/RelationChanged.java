package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;

import l2p.gameserver.model.L2Playable;
import l2p.gameserver.model.L2Player;

public class RelationChanged extends L2GameServerPacket
{
	public static final int RELATION_PVP_FLAG = 0x00002; // pvp flag
	public static final int RELATION_HAS_KARMA = 0x00004; // karma
	public static final int RELATION_LEADER = 0x00080; // true if is clan leader
	public static final int RELATION_INSIEGE = 0x00200; // true if in siege
	public static final int RELATION_ATTACKER = 0x00400; // true when attacker
	public static final int RELATION_ALLY = 0x00800; // blue siege icon, cannot have if red
	public static final int RELATION_ENEMY = 0x01000; // true when red icon, doesn't matter with blue
	public static final int RELATION_MUTUAL_WAR = 0x08000; // double fist
	public static final int RELATION_1SIDED_WAR_IT = 0x10000; // single fist

	public static final int RELATION_DECLARED_WAR = 0x4000;

	// Masks
	public static final byte SEND_DEFAULT = (byte) 0x01;
	public static final byte SEND_ONE = (byte) 0x02;
	public static final byte SEND_MULTI = (byte) 0x04;

	private byte _mask = (byte) 0x00;

	protected final List<RelationChangedData> _data;

	protected RelationChanged(int s)
	{
		_data = new ArrayList<RelationChangedData>(s);
	}

	protected void add(RelationChangedData data)
	{
		_data.add(data);

		if(_data.size() > 1)
			_mask |= SEND_MULTI;
		else if(_data.size() == 1)
			_mask |= SEND_ONE;
	}

	@Override
	protected void writeImpl()
	{
		writeC(_mask);
		if((_mask & SEND_MULTI) == SEND_MULTI)
		{
			writeH(_data.size());
			for(RelationChangedData data : _data)
			{
				writeD(data.charObjId);
				writeD(data.relation);
				writeC(data.isAutoAttackable ? 1 : 0);
				writeD(-data.karma);
				writeC(data.pvpFlag);
			}
		}
		else if((_mask & SEND_ONE) == SEND_ONE)
			writeRelation(_data.get(0));
		else if((_mask & SEND_DEFAULT) == SEND_DEFAULT)
			writeD(_data.get(0).charObjId);
	}

	private void writeRelation(RelationChangedData data)
	{
		writeD(data.charObjId);
		writeD(data.relation);
		writeC(data.isAutoAttackable ? 1 : 0);
		writeD(-data.karma);
		writeC(data.pvpFlag);
	}

	@Override
	protected void writeImplIT()
	{
		for(RelationChangedData d : _data)
		{
			writeD(d.charObjId);
			writeD(d.relation);
			writeD(d.isAutoAttackable ? 1 : 0);
			writeD(d.karma);
			writeD(d.pvpFlag);
		}
	}

	static class RelationChangedData
	{
		public final int charObjId;
		public final boolean isAutoAttackable;
		public final int relation, karma, pvpFlag;

		public RelationChangedData(L2Playable cha, boolean _isAutoAttackable, int _relation)
		{
			isAutoAttackable = _isAutoAttackable;
			relation = _relation;
			charObjId = cha.getObjectId();
			karma = cha.getKarma();
			pvpFlag = cha.getPvpFlag();
		}
	}

	/**
	 * @param targetPlayable игрок, отношение к которому изменилось
	 * @param activeChar игрок, которому будет отослан пакет с результатом
	 */
	public static L2GameServerPacket update(L2Player sendTo, L2Playable targetPlayable, L2Player activeChar)
	{
		if(sendTo == null || targetPlayable == null || activeChar == null)
			return null;

		L2Player targetPlayer = targetPlayable.getPlayer();

		int relation = targetPlayer== null ? 0 : targetPlayer.getRelation(activeChar);

		RelationChanged pkt = new RelationChanged(1);

		pkt.add(new RelationChangedData(targetPlayable, targetPlayable.isAutoAttackable(activeChar), relation));

		return pkt;
	}
}