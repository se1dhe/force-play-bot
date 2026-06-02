package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Alliance;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.network.ServerPacketOpcodes;
import l2p.gameserver.skills.AbnormalEffect;
import l2p.gameserver.skills.effects.EffectCubic;
import l2p.gameserver.tables.ClanTable;
import l2p.gameserver.templates.polymorphed.PolymorphedData;
import l2p.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PolyMorphedNpcInfo extends L2GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(PolyMorphedNpcInfo.class);

	private static final Location _fLoc = new Location();

	private L2NpcInstance _cha;
	private Inventory _inv;
	private int _mAtkSpd, _pAtkSpd;
	private int _runSpd, _walkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd, _swimSpd;
	private Location _loc, _fishLoc;
	private String _name, _title;
	private int _objId, _race, _sex, base_class, pvp_flag, karma, rec_have;
	private float speed_move, speed_atack, col_radius, col_height;
	private int hair_style, hair_color, face, abnormal_effect;
	private int clan_id, clan_crest_id, large_clan_crest_id, ally_id, ally_crest_id, class_id, maxCp, curCP;
	private int plg_class, pledge_type, cw_level;
	private int _nameColor, _title_color;
	private byte _sit, _run, _combat, _dead, _invis, mount_type, private_store, rec_left, _enchant;
	private byte _team, _noble, _hero, _fishing, partyRoom;
	private int currentCp, maxHp, maxMp, currentHp, currentMp;
	private List<EffectCubic> cubics;
	private int[] pdol;
	private boolean can_writeImpl = false;
	private AbnormalEffect[] abnormalEffects;

	public PolyMorphedNpcInfo(L2NpcInstance cha)
	{
		if((this._cha = cha) == null || _cha.isInvisible() || !_cha.isVisible())
			return;

		final PolymorphedData _polymorphedData = cha.getPolymorphedData();
		_inv = _polymorphedData.getInventory();
		if(_inv == null)
			return;
		_objId = cha.getObjectId();
		_name = _polymorphedData.getName();
		_title = _polymorphedData.getTitle();
		_title_color = _polymorphedData.getRndTitleColor();
		if(_polymorphedData.getClanId() > 0)
		{
			final L2Clan clan = ClanTable.getInstance().getClan(_polymorphedData.getClanId());
			final L2Alliance alliance = clan == null ? null : clan.getAlliance();
			clan_id = clan == null ? 0 : clan.getClanId();
			clan_crest_id = clan == null ? 0 : clan.getCrestId();
			large_clan_crest_id = clan == null ? 0 : clan.getCrestLargeId();
			ally_id = alliance == null ? 0 : alliance.getAllyId();
			ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();
		}
		else
		{
			final L2Clan clan = cha.getClan();
			final L2Alliance alliance = clan == null ? null : clan.getAlliance();
			clan_id = clan == null ? 0 : clan.getClanId();
			clan_crest_id = clan == null ? 0 : clan.getCrestId();
			large_clan_crest_id = clan == null ? 0 : clan.getCrestLargeId();
			ally_id = alliance == null ? 0 : alliance.getAllyId();
			ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();
		}
		cw_level = 0;
		_enchant = (byte) Math.min(127, _polymorphedData.getWeaponEnchant());
		mount_type = 0;
		_runSpd = _cha.getTemplate().baseRunSpd;
		_walkSpd = _cha.getTemplate().baseWalkSpd;
		speed_move = _cha.getMovementSpdMultiplier();

		if(_loc == null)
			_loc = _cha.getLoc();

		_mAtkSpd = _cha.getMAtkSpd();
		_pAtkSpd = _cha.getPAtkSpd();
		_flRunSpd = 0;
		_flWalkSpd = 0;
		if(_cha.isFlying())
		{
			_flyRunSpd = _runSpd;
			_flyWalkSpd = _walkSpd;
		}
		else
		{
			_flyRunSpd = 0;
			_flyWalkSpd = 0;
		}
		_swimSpd = _cha.getSwimSpd();
		_race = _polymorphedData.getRace();
		_sex = _polymorphedData.getSex();
		base_class = _polymorphedData.getClassId();
		pvp_flag = _cha.getPvpFlag();
		karma = _cha.getKarma();
		speed_atack = _cha.getAttackSpeedMult();
		col_radius = _polymorphedData.getCollisionRadius();
		col_height = _polymorphedData.getCollisionHeight();
		hair_style = _polymorphedData.getHairStyle();
		hair_color = _polymorphedData.getHairColor();
		face = _polymorphedData.getFace();
		_sit = _cha.isSitting() ? (byte) 0 : (byte) 1; // standing = 1 sitting = 0
		_run = _cha.isRunning() ? (byte) 1 : (byte) 0; // running = 1 walking = 0
		_combat = _cha.isInCombat() ? (byte) 1 : (byte) 0;
		_dead = _cha.isAlikeDead() && !_cha.isPendingRevive() ? (byte) 1 : (byte) 0;
		_invis = _cha.isInvisible() ? (byte) 1 : (byte) 0; // invisible = 1 visible = 0
		private_store = 0; // 1 - sellshop
		cubics = new ArrayList<>();
		abnormal_effect = _cha.getAbnormalEffectMask();
		rec_left = 0;
		rec_have = _polymorphedData.getRecomHave();
		class_id = _polymorphedData.getClassId();
		maxCp = _cha.getMaxCp();
		curCP = (int) _cha.getCurrentCp();
		_team = (byte) _cha.getTeam(); // team circle around feet 1 = Blue, 2 = red
		_noble = (byte) _polymorphedData.getNoble(); // 0x01: symbol on char menu ctrl+I
		_hero = (byte) _polymorphedData.getHero();
		_fishing = (byte) 0;
		_fishLoc = _fLoc;
		_nameColor = _polymorphedData.getRndNameColor();
		plg_class = 0;
		pledge_type = 0;
		partyRoom = (byte) 0;
		maxHp = _cha.getMaxHp();
		maxMp = _cha.getMaxMp();
		currentCp = (int)_cha.getCurrentCp();
		currentHp = (int)_cha.getCurrentHp();
		currentMp = (int)_cha.getCurrentMp();
		abnormalEffects = _cha.getAbnormalEffects();
		can_writeImpl = true;
	}

	@Override
	protected ServerPacketOpcodes getOpcodes()
	{
		return ServerPacketOpcodes.CharInfo;
	}

	@Override
	protected final void writeImpl()
	{
		if(_objId == 0)
		{
			return;
		}
		writeC(0x00);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(0); // clanBoatObjectId
		writeD(_objId);
		writeS(_name);
		writeH(_race);
		writeC(_sex);
		writeD(base_class);

		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DHAIR, true));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD, true));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND, true));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND, true));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES, true));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST, true));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS, true));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET, true));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_BACK, true));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND, true));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DHAIR, true));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR, true));

		for(int i = 0; i < 3; i++)
		{
			writeD(0x00);
			writeD(0x00);
		}

		writeC(0);

		for(int i = 0; i < 9; i++)
		{
			writeD(0x00);
		}

		writeC(pvp_flag);
		writeD(-karma);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);

		writeH(_runSpd);
		writeH(_walkSpd);
		writeH(_swimSpd);
		writeH(_swimSpd);
		writeH(_flRunSpd);
		writeH(_flWalkSpd);
		writeH(_flyRunSpd);
		writeH(_flyWalkSpd);
		writeF(speed_move);
		writeF(speed_atack);
		writeF(col_radius);
		writeF(col_height);
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		writeS(_title);
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);
		writeC(_sit);
		writeC(_run);
		writeC(_combat);
		writeC(_dead);
		writeC(0x0);
		writeC(mount_type);
		writeC(private_store);
		writeH(cubics.size());
		cubics.stream().mapToInt(cubic -> (cubic == null) ? 0 : cubic.getId()).forEach(this::writeH);
		writeC(partyRoom);
		writeC(0x0);
		writeH(rec_have);
		writeD(0);
		writeD(class_id);
		writeD(0);
		writeC(_enchant);
		writeC(_team);
		writeD(large_clan_crest_id);
		writeC(_noble);
		writeC(_hero);
		writeC(_fishing);
		writeD(_fishLoc.x);
		writeD(_fishLoc.y);
		writeD(_fishLoc.z);
		writeD(_nameColor);
		writeD(_loc.h);
		writeC(plg_class);
		writeH(pledge_type);
		writeD(_title_color);
		writeC(cw_level);
		writeD(0);
		writeD(0);
		writeD(0);
		writeC(0);
		writeD(currentCp);
		writeD(maxHp);
		writeD(currentHp);
		writeD(maxMp);
		writeD(currentMp);
		writeC(0);
		writeD(abnormalEffects.length);
		for(AbnormalEffect abnormalEffect : abnormalEffects)
			writeH(abnormalEffect.getClientId());
		writeC(0);
		writeC(1);
		writeC(0);
	}

	@Override
	protected final void writeImplIT()
	{
		int[] value;
		if(!can_writeImpl)
			return;

		value = new int[7];
		value[0] = _inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD);
		value[1] = _inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES);
		value[2] = _inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST);
		value[3] = _inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS);
		value[4] = _inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET);
		value[5] = _inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR);
		value[6] = _inv.getPaperdollItemId(Inventory.PAPERDOLL_DHAIR);
		pdol = value;

		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(_loc.h); //?
		writeD(_objId);
		writeS(_name);
		writeD(_race);
		writeD(_sex);
		writeD(base_class);
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));
		writeD(pdol[0]);
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(pdol[1]);
		writeD(pdol[2]);
		writeD(pdol[3]);
		writeD(pdol[4]);
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(pdol[5]);
		writeD(pdol[6]);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LHAND));
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeD(pvp_flag);
		writeD(karma);

		writeD(_mAtkSpd);
		writeD(_pAtkSpd);

		writeD(pvp_flag);
		writeD(karma);

		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimSpd);
		writeD(_swimSpd);
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(speed_move); // _cha.getProperMultiplier()
		writeF(speed_atack); // _cha.getAttackSpeedMultiplier()
		writeF(col_radius);
		writeF(col_height);
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		writeS(_title);
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);

		writeD(0);

		writeC(_sit);
		writeC(_run);
		writeC(_combat);
		writeC(_dead);
		writeC(_invis);
		writeC(mount_type); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		writeC(private_store);
		writeH(cubics.size());
		for(EffectCubic cubic : cubics)
			writeH(cubic == null ? 0 : cubic.getId());
		writeC(partyRoom); // find party members
		writeD(abnormal_effect);
		writeC(rec_left);
		writeH(rec_have);
		writeD(class_id);
		writeD(maxCp);
		writeD(curCP);
		writeC(_enchant);

		writeC(_team);
		writeD(large_clan_crest_id);
		writeC(_noble);
		writeC(_hero);

		writeC(_fishing);
		writeD(_fishLoc.getX());
		writeD(_fishLoc.getY());
		writeD(_fishLoc.getZ());

		writeD(_nameColor);
		writeD(_loc.h);
		writeD(plg_class);
		writeD(pledge_type);
		writeD(_title_color);
		writeD(cw_level);
	}
}