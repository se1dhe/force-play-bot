package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.*;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.serverpackets.updatetype.NpcInfoType;

public class NpcInfo extends AbstractNpcPacket
{
	public NpcInfo(L2NpcInstance npc, L2Character attacker)
	{
		if(npc == null)
			return;

		_cha = npc;
		_npcId = npc.getTemplate().displayId != 0 ? npc.getTemplate().displayId : npc.getTemplate().npcId;
		_isAttackable = npc.isAutoAttackable(attacker);
		_rhand = npc.getRightHandItem();
		_lhand = npc.getLeftHandItem();
		if(npc.isAgathion())
		{
			_name = " ";
			_title = "";
			_titleColor = 0;
		}
		else
		{
			if(npc.isServerName())
				_name = npc.getName();
			if(npc.isServerTitle())
				_title = npc.getTitle();
			_titleColor = npc.isSummon() ? 1 : 0;
		}
		_showSpawnAnimation = npc.getSpawnAnimation();
		_isPet = false;
		setValues(npc, NpcInfoType.VALUES);
		if(attacker != null && attacker.isPlayer() && attacker.getPlayer().getVarB("DisableNpcAura", false))
		{
			if(team > 0)
				team = 0;
		}
	}

	public NpcInfo(L2Summon summon, L2Character attacker)
	{
		if(summon == null)
			return;
		L2Player player = summon.getPlayer();
		if(player != null && player.isInvisible())
			return;

		_cha = summon;
		_npcId = summon.getTemplate().npcId;
		_isAttackable = summon.isAutoAttackable(attacker);
		_rhand = 0;
		_lhand = 0;
		if(summon.isAgathion())
		{
			_name = " ";
			_title = "";
			_titleColor = 0;
		}
		else
		{
			if(summon.isPet())
				_name = _cha.getName();
			_title = summon.getTitle();
			_titleColor = summon.isSummon() ? 1 : 0;
		}
		_showSpawnAnimation = summon.getSpawnAnimation();
		setValues(summon, NpcInfoType.VALUES);
		can_writeImpl = true;
	}

	public NpcInfo(L2Summon summon, L2Character attacker, int showSpawnAnimation)
	{
		this(summon, attacker);
		_showSpawnAnimation = showSpawnAnimation;
	}

	@Override
	protected boolean canWrite()
	{
		return can_writeImpl;
	}

	@Override
	protected final void writeImpl()
	{
		writeData();
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_npcObjId);
		writeD(_npcId + 1000000); // npctype id c4
		writeD(_isAttackable ? 1 : 0);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ() + Config.CLIENT_Z_SHIFT);
		writeD(_loc.h);
		writeD(0x00);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd /*_swimRunSpd*//*0x32*/); // swimspeed
		writeD(_walkSpd/*_swimWalkSpd*//*0x32*/); // swimspeed
		writeD(_runSpd/*_flRunSpd*/);
		writeD(_walkSpd/*_flWalkSpd*/);
		writeD(_runSpd/*_flyRunSpd*/);
		writeD(_walkSpd/*_flyWalkSpd*/);
		writeF(speed_move);
		writeF(_pAtkSpd / 277.47833F);
		writeF(colRadius);
		writeF(colHeight);
		writeD(_rhand); // right hand weapon
		writeD(0);
		writeD(_lhand);
		writeC(1); // left hand weapon
		writeC(running);
		writeC(incombat);
		writeC(dead);
		writeC(_showSpawnAnimation); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
		writeS(_name);
		writeS(_title);
		writeD(_titleColor); // 0- светло зеленый титул(моб), 1 - светло синий(пет)/отображение текущего МП
		writeD(pvp_flag);
		writeD(karma); // hmm karma ??
		writeD(_abnormalEffect); // C2
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);
		writeC(isFlying ? 2 : 0); // C2
		writeC(team); // team aura 1-blue, 2-red
		writeF(colRadius); // тут что-то связанное с colRadius
		writeF(colHeight); // тут что-то связанное с colHeight
		writeD(0x00);
		writeD(isFlying ? 1 : 0);
	}

	@Override
	public String getType()
	{
		return super.getType() + (_cha != null ? " about " + _cha : "");
	}
}