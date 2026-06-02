package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Object;

public class Attack extends L2GameServerPacket
{
	// For IT
	private static final int HITFLAG_USESS_IT = 0x10;
	private static final int HITFLAG_CRIT_IT = 0x20;
	private static final int HITFLAG_SHLD_IT = 0x40;
	private static final int HITFLAG_MISS_IT = 0x80;
	// For FF
	private static final int HITFLAG_MISS = 0x01;
	private static final int HITFLAG_SHLD = 0x02;
	private static final int HITFLAG_CRIT = 0x04;
	private static final int HITFLAG_USESS = 0x08;

	private class Hit
	{
		protected final int _targetId;
		protected final int _damage;
		protected int _flags, _flagsIT;

		Hit(L2Object target, int damage, boolean miss, boolean crit, boolean shld)
		{
			_targetId = target.getObjectId();
			_damage = damage;
			if(miss)
			{
				_flags = HITFLAG_MISS;
				_flagsIT = HITFLAG_MISS_IT;
				return;
			}
			if(_soulshot)
			{
				_flags = HITFLAG_USESS | _grade;
				_flagsIT = HITFLAG_USESS_IT | _grade;
			}
			if(crit)
			{
				_flags |= HITFLAG_CRIT;
				_flagsIT |= HITFLAG_CRIT_IT;
			}
			if(shld && (!target.isPlayer() || !target.isInOlympiadMode()))
			{
				_flags |= HITFLAG_SHLD;
				_flagsIT |= HITFLAG_SHLD_IT;
			}
		}
	}

	public final int _attackerId;
	public final boolean _soulshot;
	private final int _grade;
	private final int _x, _y, _z, _tx, _ty, _tz;
	private Hit[] hits;

	public Attack(L2Character attacker, L2Character target, boolean ss, int grade)
	{
		_attackerId = attacker.getObjectId();
		_soulshot = ss;
		_grade = grade;
		_x = attacker.getX();
		_y = attacker.getY();
		_z = attacker.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
		hits = new Hit[0];
	}

	/**
	 * Add this hit (target, damage, miss, critical, shield) to the Server-Client packet Attack.<BR><BR>
	 */
	public void addHit(L2Object target, int damage, boolean miss, boolean crit, boolean shld)
	{
		// Get the last position in the hits table
		int pos = hits.length;

		// Create a new Hit object
		Hit[] tmp = new Hit[pos + 1];

		// Add the new Hit object to hits table
		for(int i = 0; i < hits.length; i++)
			tmp[i] = hits[i];
		tmp[pos] = new Hit(target, damage, miss, crit, shld);
		hits = tmp;
	}

	/**
	 * Return True if the Server-Client packet Attack conatins at least 1 hit.<BR><BR>
	 */
	public boolean hasHits()
	{
		return hits.length > 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_attackerId);
		writeD(hits[0]._targetId);
		writeD(0x00);
		writeD(hits[0]._damage);
		writeD(hits[0]._flags);
		writeD(_soulshot ? _grade : 0x00);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeH(hits.length - 1);
		for(int i = 1; i < hits.length; i++)
		{
			writeD(hits[i]._targetId);
			writeD(hits[i]._damage);
			writeD(hits[i]._flags);
			writeD(_soulshot ? _grade : 0x00);
		}
		writeD(_tx);
		writeD(_ty);
		writeD(_tz);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_attackerId);
		writeD(hits[0]._targetId);
		writeD(hits[0]._damage);
		writeC(hits[0]._flagsIT);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeH(hits.length - 1);
		for(int i = 1; i < hits.length; i++)
		{
			writeD(hits[i]._targetId);
			writeD(hits[i]._damage);
			writeC(hits[i]._flagsIT);
		}
	}
}