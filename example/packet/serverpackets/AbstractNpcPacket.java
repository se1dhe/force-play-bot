package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Alliance;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.serverpackets.updatetype.NpcInfoType;
import l2p.gameserver.skills.AbnormalEffect;
import l2p.gameserver.utils.Location;

public abstract class AbstractNpcPacket extends AbstractMaskPacket<NpcInfoType>
{
	// Flags
	private static final int IS_IN_COMBAT = 1 << 0;
	private static final int IS_ALIKE_DEAD = 1 << 1;
	private static final int IS_TARGETABLE = 1 << 2;
	private static final int IS_SHOW_NAME = 1 << 3;

	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x0C,
		(byte) 0x0C,
		(byte) 0x00,
		(byte) 0x00
	};

	protected boolean can_writeImpl = false;
	protected L2Character _cha;
	protected int _npcObjId, _npcId, incombat, dead, team, _showSpawnAnimation;
	protected int _runSpd, _walkSpd, _mAtkSpd, _pAtkSpd, _rhand, _lhand, running;
	protected int karma, pvp_flag, _abnormalEffect, clan_crest_id, clan_large_crest_id, ally_crest_id, _titleColor, clan_id, ally_id;
	protected float colHeight, colRadius, speed_move;
	protected boolean _isAttackable;
	protected int _enchantEffect;
	protected Location _loc;
	protected String _name = "", _title = "";
	protected boolean isFlying = false;

	protected int currentHp, currentMp, maxHp, maxMp;

	protected boolean _showName, _targetable;
	protected int _state;
	protected int _formId;
	protected boolean _isPet;
	protected double moveAnimMod;
	protected double atkSpeed;
	protected AbnormalEffect[] abnormalEffects = AbnormalEffect.EMPTY_ARRAY;

	private int _initSize = 0;
	private int _blockSize = 0;

	protected void setValues(L2Character cha, NpcInfoType... components)
	{
		currentHp = (int) cha.getCurrentHp();
		currentMp = (int) cha.getCurrentMp();
		maxHp = cha.getMaxHp();
		maxMp = cha.getMaxMp();

		colHeight = cha.getColHeight();
		colRadius = cha.getColRadius();
		_npcObjId = cha.getObjectId();
		_loc = cha.getLoc();
		_mAtkSpd = cha.getMAtkSpd();

		if(Config.SHOW_NPC_CREST && !cha.isSummon())
		{
			L2Clan clan = cha.getClan();
			L2Alliance alliance = clan == null ? null : clan.getAlliance();
			clan_id = clan == null ? 0 : clan.getClanId();
			clan_crest_id = clan == null ? 0 : clan.getCrestId();
			ally_id = alliance == null ? 0 : alliance.getAllyId();
			ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();
		}

		speed_move = cha.getMovementSpdMultiplier();
		_runSpd = cha.getTemplate().baseRunSpd;
		_walkSpd = cha.getTemplate().baseWalkSpd;
		karma = cha.getKarma();
		pvp_flag = cha.getPvpFlag();
		_pAtkSpd = cha.getPAtkSpd();
		running = cha.isRunning() ? 1 : 0;
		incombat = cha.isInCombat() ? 1 : 0;
		dead = cha.isAlikeDead() ? 1 : 0;
		_abnormalEffect = cha.getAbnormalEffectMask();
		team = cha.getTeam();
		_formId = cha.getFormId();
		isFlying = cha.isFlying() && !cha.isSummon();

		_targetable = cha.isTargetable();
		_showName = cha.isShowName();
		moveAnimMod = (double) cha.getRunSpeed() * 1.0 / (double) cha.getTemplate().baseRunSpd;
		atkSpeed = cha.getAttackSpeedMult();
		abnormalEffects = cha.getAbnormalEffects();
		_enchantEffect = cha.getEnchantEffect();

		addComponentType(components);

		can_writeImpl = true;
	}

	public AbstractNpcPacket update(boolean bl)
	{
		_showSpawnAnimation = bl ? 1 : 0;
		return this;
	}

	public AbstractNpcPacket updateTeam(int value)
	{
		team = value;
		return this;
	}

	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}

	@Override
	protected void onNewMaskAdded(NpcInfoType npcInfoType)
	{
		switch(npcInfoType)
		{
			case ATTACKABLE:
			case UNKNOWN1:
			{
				_initSize += npcInfoType.getBlockLength();
				break;
			}
			case TITLE:
			{
				_initSize += npcInfoType.getBlockLength() + _title.length() * 2;
				break;
			}
			case NAME:
			{
				_blockSize += npcInfoType.getBlockLength() + _name.length() * 2;
				break;
			}
			default:
			{
				_blockSize += npcInfoType.getBlockLength();
				break;
			}
		}
	}

	protected void writeData()
	{
		writeD(_npcObjId);
		writeC(_showSpawnAnimation); // // 0=teleported 1=default 2=summoned
		writeH(37); // mask_bits_37
		writeB(_masks);

		// Block 1
		writeC(_initSize);
		if(containsMask(NpcInfoType.ATTACKABLE))
			writeC(_isAttackable ? 1 : 0);

		if(containsMask(NpcInfoType.UNKNOWN1))
			writeD(0x00); // unknown

		if(containsMask(NpcInfoType.TITLE))
			writeS(_title);

		// Block 2
		writeH(_blockSize);

		if(containsMask(NpcInfoType.ID))
			writeD(_npcId + 1000000);

		if(containsMask(NpcInfoType.POSITION))
		{
			writeD(_loc.x);
			writeD(_loc.y);
			writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		}

		if(containsMask(NpcInfoType.HEADING))
			writeD(_loc.h);

		if(containsMask(NpcInfoType.UNKNOWN2))
			writeD(0x00); // Unknown

		if(containsMask(NpcInfoType.ATK_CAST_SPEED))
		{
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
		}

		if(containsMask(NpcInfoType.SPEED_MULTIPLIER))
		{
			writeCutF(moveAnimMod);
			writeCutF(atkSpeed);
		}

		if(containsMask(NpcInfoType.EQUIPPED))
		{
			writeD(_rhand);
			writeD(0x00); // Armor id?
			writeD(_lhand);
		}

		if(containsMask(NpcInfoType.ALIVE))
			writeC(dead == 0);

		if(containsMask(NpcInfoType.RUNNING))
			writeC(running);

		if(containsMask(NpcInfoType.SWIM_OR_FLY))
			writeC(isFlying ? 2 : 0);

		if(containsMask(NpcInfoType.TEAM))
			writeC(team);

		if(containsMask(NpcInfoType.ENCHANT))
			writeD(_enchantEffect);

		if(containsMask(NpcInfoType.FLYING))
			writeD(0);

		if(containsMask(NpcInfoType.CLONE))
			writeD(0x00); // Player ObjectId with Decoy

		if(containsMask(NpcInfoType.COLOR_EFFECT))
			writeD(_isPet);

		if(containsMask(NpcInfoType.DISPLAY_EFFECT))
			writeD(_state);

		if(containsMask(NpcInfoType.TRANSFORMATION))
			writeD(_formId);

		if(containsMask(NpcInfoType.CURRENT_HP))
			writeD(currentHp);

		if(containsMask(NpcInfoType.CURRENT_MP))
			writeD(currentMp);

		if(containsMask(NpcInfoType.MAX_HP))
			writeD(maxHp);

		if(containsMask(NpcInfoType.MAX_MP))
			writeD(maxMp);

		if(containsMask(NpcInfoType.SUMMONED))
			writeC(0x00); // 2 - do some animation on spawn

		if(containsMask(NpcInfoType.UNKNOWN12))
		{
			writeD(0x00);
			writeD(0x00);
		}

		if(containsMask(NpcInfoType.NAME))
			writeS(_name);

		if(containsMask(NpcInfoType.NAME_NPCSTRINGID))
			writeD(-1/*_nameNpcString.getId()*/); // NPCStringId for name

		if(containsMask(NpcInfoType.TITLE_NPCSTRINGID))
			writeD(-1/*_titleNpcString.getId()*/); // NPCStringId for title

		if(containsMask(NpcInfoType.PVP_FLAG))
			writeC(pvp_flag);

		if(containsMask(NpcInfoType.REPUTATION))
			writeD(karma);

		if(containsMask(NpcInfoType.CLAN))
		{
			writeD(clan_id);
			writeD(clan_crest_id);
			writeD(clan_large_crest_id);
			writeD(ally_id);
			writeD(ally_crest_id);
		}

		if(containsMask(NpcInfoType.VISUAL_STATE))
		{
			int statusMask = 0;
			if(incombat == 1)
				statusMask |= IS_IN_COMBAT;

			if(dead == 1)
				statusMask |= IS_ALIKE_DEAD;

			if(_targetable)
				statusMask |= IS_TARGETABLE;

			if(_showName)
				statusMask |= IS_SHOW_NAME;

			writeC(statusMask);
		}

		if(containsMask(NpcInfoType.ABNORMALS))
		{
			writeH(abnormalEffects.length);
			for(AbnormalEffect abnormalEffect : abnormalEffects)
				writeH(abnormalEffect.getClientId());
		}
	}
}

