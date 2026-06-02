package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Summon;
import l2p.gameserver.skills.AbnormalEffect;
import l2p.gameserver.utils.Location;

public class MyPetSummonInfo extends L2GameServerPacket
{
	private static final int IS_UNK_FLAG_1 = 1 << 0;
	private static final int IS_UNK_FLAG_2 = 1 << 1;
	private static final int IS_RUNNING = 1 << 2;
	private static final int IS_IN_COMBAT = 1 << 3;
	private static final int IS_ALIKE_DEAD = 1 << 4;
	private static final int IS_RIDEABLE = 1 << 5;

	private int _runSpd, _walkSpd, MAtkSpd, PAtkSpd, pvp_flag, karma, weapon, armor;
	private int _type, obj_id, npc_id, runing, incombat, dead, _sp, level, _abnormalEffect;
	private int curFed, maxFed, curHp, maxHp, curMp, maxMp, curLoad, maxLoad;
	private int PAtk, PDef, MAtk, MDef, Accuracy, Evasion, Crit, team, sps, ss, _spawnAnimation;;
	private Location _loc;
	private float col_redius, col_height, speed_move;
	private long exp, exp_this_lvl, exp_next_lvl;
	private String _name, title;
	private boolean owner, rideable;
	private AbnormalEffect[] _abnormalEffects;
	private int _flags;
	private int _transformId;

	public MyPetSummonInfo(L2Summon summon)
	{
		_type = summon.getSummonType();
		obj_id = summon.getObjectId();
		npc_id = summon.getTemplate().npcId;
		_loc = summon.getLoc();
		MAtkSpd = summon.getMAtkSpd();
		PAtkSpd = summon.getPAtkSpd();
		speed_move = 1/*summon.getMovementSpeedMultiplier()*/;
		_runSpd = summon.getRunSpeed();
		_walkSpd = summon.getTemplate().baseWalkSpd;
		col_redius = summon.getColRadius();
		col_height = summon.getColHeight();
		weapon = summon.getWeapon();
		armor = summon.getArmor();
		owner = summon.getPlayer() != null;
		runing = 1;
		incombat = summon.isInCombat() ? 1 : 0;
		dead = summon.isAlikeDead() ? 1 : 0;
		_name = summon.getName().equalsIgnoreCase(summon.getTemplate().name) ? "" : summon.getName();
		title = summon.getTitle();
		pvp_flag = summon.getPvpFlag();
		karma = summon.getKarma();
		curFed = summon.getCurrentFed();
		maxFed = summon.getMaxMeal();
		curHp = (int) summon.getCurrentHp();
		maxHp = summon.getMaxHp();
		curMp = (int) summon.getCurrentMp();
		maxMp = summon.getMaxMp();
		_sp = summon.getSp();
		level = summon.getLevel();
		exp = summon.getExp();
		exp_this_lvl = summon.getExpForThisLevel();
		exp_next_lvl = summon.getExpForNextLevel();
		curLoad = summon.isPet() ? summon.getInventory().getTotalWeight() : 0;
		maxLoad = summon.getMaxLoad();
		PAtk = summon.getPAtk(null);
		PDef = summon.getPDef(null);
		MAtk = summon.getMAtk(null, null);
		MDef = summon.getMDef(null, null);
		Accuracy = summon.getAccuracy();
		Evasion = summon.getEvasionRate(null);
		Crit = summon.getCriticalHit(null, null);
		_abnormalEffect = summon.getAbnormalEffectMask();
		rideable = summon.isMountable();
		team = summon.getTeam();
		ss = summon.getSoulshotConsumeCount();
		sps = summon.getSpiritshotConsumeCount();
		_spawnAnimation = summon.getSpawnAnimation();
		_abnormalEffects = summon.getAbnormalEffects();
		_transformId = summon.getFormId();

		_flags |= IS_UNK_FLAG_2;

		if(summon.isRunning())
			_flags |= IS_RUNNING;

		if(summon.isInCombat())
			_flags |= IS_IN_COMBAT;

		if(summon.isAlikeDead())
			_flags |= IS_ALIKE_DEAD;

		if(rideable)
			_flags |= IS_RIDEABLE;
	}

	public MyPetSummonInfo(L2Summon summon, int animation)
	{
		this(summon);
		_spawnAnimation = animation;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		writeD(obj_id);
		writeD(npc_id + 1000000);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
		writeD(_loc.h);
		writeD(MAtkSpd);
		writeD(PAtkSpd);
		writeH(_runSpd);
		writeH(_walkSpd);
		writeH(_runSpd);
		writeH(_walkSpd);
		writeH(_runSpd);
		writeH(_walkSpd);
		writeH(_runSpd);
		writeH(_walkSpd);
		writeF(speed_move);
		writeF(1/*_cha.getProperMultiplier()*/);
		writeF(col_redius);
		writeF(col_height);
		writeD(0/*_rhand*/); // right hand weapon
		writeD(0);
		writeD(0/*_lhand*/); // left hand weapon
		writeC(_spawnAnimation); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
		writeD(-1);
		writeS(_name);
		writeD(-1);
		writeS(title);
		writeC(pvp_flag); //0=white, 1=purple, 2=purpleblink, if its greater then karma = purple
		writeD(karma); // hmm karma ??
		writeD(curFed); // how fed it is
		writeD(maxFed); //max fed it can be
		writeD(curHp); //current hp
		writeD(maxHp); // max hp
		writeD(curMp); //current mp
		writeD(maxMp); //max mp
		writeQ(_sp); //sp
		writeC(level);// lvl
		writeQ(exp);
		writeQ(exp_this_lvl); // 0%  absolute value
		writeQ(exp_next_lvl); // 100% absoulte value
		writeD(curLoad); //weight
		writeD(maxLoad); //max weight it can carry
		writeD(PAtk);//patk
		writeD(PDef);//pdef
		writeD(Accuracy); // P. Accuracy
		writeD(Evasion); // P. Evasion
		writeD(Crit); // P. Critical
		writeD(MAtk);//matk
		writeD(MDef);//mdef
		writeD(Accuracy);//accuracy
		writeD(Evasion);//evasion
		writeD(Crit);//critical
		writeD(_runSpd);//speed
		writeD(PAtkSpd);//atkspeed
		writeD(MAtkSpd);//casting speed
		writeC(0x00);//unk
		writeC(team); // team aura (1 = blue, 2 = red)
		writeC(ss);
		writeC(sps);
		writeD(0x00);
		writeD(_transformId); // transform id
		writeC(0x00); // sum points
		writeC(0x00); // max sum points

		writeH(_abnormalEffects.length);
		for(AbnormalEffect abnormal : _abnormalEffects)
			writeH(abnormal.getClientId());

		writeC(_flags);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_type);
		writeD(obj_id);
		writeD(npc_id + 1000000);
		writeD(0); // 1=attackable
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
		writeD(_loc.h);
		writeD(0);
		writeD(MAtkSpd);
		writeD(PAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeF(speed_move);
		writeF(1/*_cha.getProperMultiplier()*/);
		writeF(col_redius);
		writeF(col_height);
		writeD(weapon);
		writeD(armor);
		writeD(0); // left hand weapon
		writeC(owner ? 1 : 0); // name above char 1=true ... ??
		writeC(runing); // running=1
		writeC(incombat); // attacking 1=true
		writeC(dead); // dead 1=true
		writeC(_spawnAnimation); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
		writeS(_name);
		writeS(title);
		writeD(1);
		writeD(pvp_flag); //0=white, 1=purple, 2=purpleblink, if its greater then karma = purple
		writeD(karma); // hmm karma ??
		writeD(curFed); // how fed it is
		writeD(maxFed); //max fed it can be
		writeD(curHp); //current hp
		writeD(maxHp); // max hp
		writeD(curMp); //current mp
		writeD(maxMp); //max mp
		writeD(_sp); //sp
		writeD(level);// lvl
		writeQ(exp);
		writeQ(exp_this_lvl); // 0%  absolute value
		writeQ(exp_next_lvl); // 100% absoulte value
		writeD(curLoad); //weight
		writeD(maxLoad); //max weight it can carry
		writeD(PAtk);//patk
		writeD(PDef);//pdef
		writeD(MAtk);//matk
		writeD(MDef);//mdef
		writeD(Accuracy);//accuracy
		writeD(Evasion);//evasion
		writeD(Crit);//critical
		writeD(_runSpd);//speed
		writeD(PAtkSpd);//atkspeed
		writeD(MAtkSpd);//casting speed
		writeD(_abnormalEffect); //c2  abnormal visual effect... bleed=1; poison=2; bleed?=4;
		writeH(rideable ? 1 : 0);
		writeC(0); // c2
		writeH(0); // ??
		writeC(team); // team aura (1 = blue, 2 = red)
		writeD(ss);
		writeD(sps);
	}
}