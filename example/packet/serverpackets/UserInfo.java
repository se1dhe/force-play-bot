package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.configuration.*;
import l2p.gameserver.instancemanager.CursedWeaponsManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.entity.events.GlobalEvent;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.updatetype.UserInfoType;
import l2p.gameserver.skills.effects.EffectCubic;
import l2p.gameserver.templates.L2Item;
import l2p.gameserver.utils.Location;

public class UserInfo extends AbstractMaskPacket<UserInfoType>
{
	private L2Player player;
	private int _runSpd, _walkSpd, _swimSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd, _relation;
	private float move_speed, attack_speed, col_radius, col_height;
	private PcInventory _inv;
	private Location _loc, _fishLoc;
	private int obj_id, vehicle_obj_id, _race, sex, base_class, level, curCp, maxCp, _weaponFlag;
	private long _exp;
	private int curHp, maxHp, curMp, maxMp, curLoad, maxLoad, rec_left, rec_have;
	private int _str, _con, _dex, _int, _wit, _men, _sp, ClanPrivs, InventoryLimit;
	private int _patk, _patkspd, _pdef, evasion, accuracy, crit, _matk, _matkspd;
	private int _mdef, pvp_flag, karma, hair_style, hair_color, face, gm_commands;
	private int clan_id, _isClanLeader, clan_crest_id, ally_id, ally_crest_id, large_clan_crest_id;
	private int pk_kills, pvp_kills, class_id;
	private int AbnormalEffect, mount_id, cw_level;
	private int name_color, pledge_class, pledge_type, title_color, running, _moveType;
	private byte mount_type, private_store, can_crystalize, _enchant, _armorSetEnchant;
	private byte team, noble, hero, fishing, partyRoom;
	private String _name, title;
	private EffectCubic[] cubics;
	private double _expPercent;
	private boolean can_writeImpl = false;

	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00
	};

	private int _initSize = 5;

	public UserInfo(L2Player _cha)
	{
		this(_cha, true);
	}

	public UserInfo(L2Player _cha, boolean addAll)
	{
		player = _cha;
		if(_cha.isCursedWeaponEquipped())
		{
			_name = _cha.getName();
			clan_crest_id = 0;
			ally_crest_id = 0;
			large_clan_crest_id = 0;
			cw_level = CursedWeaponsManager.getInstance().getLevel(_cha.getCursedWeaponEquippedId());
		}
		else
		{
			_name = getName(_cha);

			if(Config.EVENT_DEATHMATCH_HIDE_CLAN && _cha.inDeathMatch)
			{
				clan_crest_id = 0;
				ally_crest_id = 0;
				large_clan_crest_id = 0;
			}
			else if(Config.EVENT_DECISIVEDEATH_HIDE_CLAN && _cha.inDecisiveDeath)
			{
				clan_crest_id = 0;
				ally_crest_id = 0;
				large_clan_crest_id = 0;
			}
			else if(ConfigCaptureCastle.CAPTURE_CASTLE_HIDE_CLAN_ALY_INFO && _cha.isInCaptureCastleEvent())
			{
				clan_crest_id = 0;
				ally_crest_id = 0;
				large_clan_crest_id = 0;
			}
			else if(ConfigBattleGround.BATTLE_GROUND_HIDE_CLAN && _cha.inBattleGround)
			{
				clan_crest_id = 0;
				ally_crest_id = 0;
				large_clan_crest_id = 0;
			}
			else if(ConfigBossHunting.BOSS_HUNTING_HIDE_CLAN_ALY_INFO && _cha.isInBossHunting())
			{
				clan_crest_id = 0;
				ally_crest_id = 0;
				large_clan_crest_id = 0;
			}
			else if(FightClubConfig.FC_HIDE_CLAN_ALY_INFO && _cha.isInFightClub())
			{
				clan_crest_id = 0;
				ally_crest_id = 0;
				large_clan_crest_id = 0;
			}
			else
			{
				clan_crest_id = _cha.getClanCrestId();
				ally_crest_id = _cha.getAllyCrestId();
				large_clan_crest_id = _cha.getClanCrestLargeId();
			}
			cw_level = 0;
		}

		if(_cha.isMounted())
		{
			_enchant = 0;
			_armorSetEnchant = 0;
			mount_id = _cha.getMountEngine().getMountNpcId() + 1000000;
			mount_type = (byte) _cha.getMountEngine().getMountType();
			_runSpd = _cha.getMountEngine().getMountSpeed();
		}
		else
		{
			_enchant = (byte) _cha.getEnchantEffect2();
			_armorSetEnchant = (byte) _cha.getArmorSetEnchantLevel();
			mount_id = 0;
			mount_type = 0;
			_runSpd = _cha.isInOlympiadObserverMode() ? 120 : _cha.getTemplate().baseRunSpd;
		}
		_walkSpd = _cha.getTemplate().baseWalkSpd;
		move_speed = _cha.isInOlympiadObserverMode() ? Config.OLYMPIAD_SPECTATING_SPEED * 1.0F / 120.0F : _cha.getMovementSpdMultiplier();
		_weaponFlag = _cha.getPhysicalAttackRange();
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
		_inv = _cha.getInventory();
		_relation = _cha.isClanLeader() ? 0x40 : 0;
		for(GlobalEvent e : _cha.getEvents())
			_relation = e.getUserRelation(_cha, _relation);

		_loc = _cha.getLoc();
		obj_id = _cha.getObjectId();
		vehicle_obj_id = _cha.isInVehicle() ? _cha.getVehicle().getBoatId() : 0x00;
		_race = _cha.getRace().ordinal();
		sex = _cha.getSex();
		base_class = _cha.getBaseClassId();
		level = _cha.getLevel();
		_exp = _cha.getExp();
		_expPercent = Experience.getExpPercent(_cha.getLevel(), _cha.getExp());
		_str = _cha.getSTR();
		_dex = _cha.getDEX();
		_con = _cha.getCON();
		_int = _cha.getINT();
		_wit = _cha.getWIT();
		_men = _cha.getMEN();
		curHp = (int) _cha.getCurrentHp();
		maxHp = _cha.getMaxHp();
		curMp = (int) _cha.getCurrentMp();
		maxMp = _cha.getMaxMp();
		curLoad = _cha.getCurrentLoad();
		maxLoad = _cha.getMaxLoad();
		_sp = _cha.getSp();
		_patk = _cha.getPAtk(null);
		_patkspd = _cha.getPAtkSpd();
		_pdef = _cha.getPDef(null);
		evasion = _cha.getEvasionRate(null);
		accuracy = _cha.getAccuracy();
		crit = _cha.getCriticalHit(null, null);
		_matk = _cha.getMAtk(null, null);
		_matkspd = _cha.getMAtkSpd();
		_mdef = _cha.getMDef(null, null);
		pvp_flag = _cha.getPvpFlag(); // 0=white, 1=purple, 2=purpleblink
		karma = _cha.getKarma();
		attack_speed = _cha.getAttackSpeedMult();
		col_radius = _cha.getColRadius();
		col_height = _cha.getColHeight();
		hair_style = _cha.getHairStyle();
		hair_color = _cha.getHairColor();
		face = _cha.getFace();
		gm_commands = _cha.isGM() || _cha.getPlayerAccess().CanUseGMCommand ? 1 : 0;
		// builder level активирует в клиенте админские команды
		title = getTitle(_cha);
		clan_id = _cha.getClanId();
		_isClanLeader = _cha.isClanLeader() ? 1 : 0;
		ally_id = _cha.getAllyId();
		private_store = (byte) _cha.getPrivateStoreType();
		can_crystalize = _cha.getSkillLevel(L2Skill.SKILL_CRYSTALLIZE) > 0 ? (byte) 1 : (byte) 0;
		pk_kills = _cha.getPkKills();
		pvp_kills = _cha.getPvpKills();
		cubics = _cha.getCubics().toArray(new EffectCubic[_cha.getCubics().size()]);
		AbnormalEffect = _cha.getAbnormalEffectMask();
		ClanPrivs = _cha.getClanPrivileges();
		rec_left = _cha.getRecomLeft(); //c2 recommendations remaining
		rec_have = _cha.getPlayerAccess().IsGM ? 0 : _cha.getRecomHave(); //c2 recommendations received
		InventoryLimit = _cha.getInventoryLimit();
		class_id = _cha.getClassId().getId();
		maxCp = _cha.getMaxCp();
		curCp = (int) _cha.getCurrentCp();
		team = (byte) _cha.getTeam(); //team circle around feet 1= Blue, 2 = red
		noble = _cha.isNoble() ? (byte) 1 : (byte) 0;
		if(_cha.getVarB("DisableHeroAura", false))
		{
			hero = (byte) 0;
		}
		else
		{
			if(_cha.isITClient())
				hero = !_cha.noHeroAure && _cha.isHero() ? (byte) 1 : (byte) 0; //0x01: Hero Aura and symbol
			else
				hero = !_cha.noHeroAure && _cha.isHero() ? (byte) 2 : (byte) 0; //0x01: Hero Aura and symbol
		}
		fishing = _cha.isFishing() ? (byte) 1 : (byte) 0; // Fishing Mode
		_fishLoc = _cha.getFishLoc();
		name_color = getNameColor(_cha);
		running = _cha.isRunning() ? 0x01 : 0x00; //changes the Speed display on Status Window
		_moveType = _cha.isInWater() ? 0x01 : 0x00;
		pledge_class = _cha.getPledgeClass();
		pledge_type = _cha.getPledgeType();
		title_color = getTitleColor(_cha);
		partyRoom = _cha.getPartyRoom() != null ? (byte) 1 : (byte) 0;

		can_writeImpl = true;

		if(addAll)
			addComponentType(UserInfoType.values());
	}

	private int getNameColor(L2Player _cha)
	{
		if(Config.EVENT_DEATHMATCH_HIDE_NAME_COLOR && _cha.inDeathMatch)
			return 0x0080FF;
		else if(Config.EVENT_DECISIVEDEATH_HIDE_NAME_COLOR && _cha.inDecisiveDeath)
			return 0x0080FF;
		else if(ConfigCaptureCastle.CAPTURE_CASTLE_HIDE_NAME_COLOR && _cha.isInCaptureCastleEvent())
			return 0xFFFFFF;
		else if(ConfigBossHunting.BOSS_HUNTING_HIDE_NAME_COLOR && _cha.isInBossHunting())
			return 0xFFFFFF;
		else if(ConfigBattleGround.BATTLE_GROUND_HIDE_NAME_COLOR && _cha.inBattleGround)
			return 0x000000;
		else if(FightClubConfig.FC_BLUE_TEAM_COLOR_NAME > 0 && FightClubConfig.FC_RED_TEAM_COLOR_NAME > 0 && _cha.isInFightClub())
			return _cha.getEventNameColor();
		else
			return _cha.getNameColor();
	}

	private int getTitleColor(L2Player _cha)
	{
		if(Config.EVENT_DEATHMATCH_HIDE_TITLE_COLOR && _cha.inDeathMatch)
			return 0x0099FF;
		else if(Config.EVENT_DECISIVEDEATH_HIDE_TITLE_COLOR && _cha.inDecisiveDeath)
			return 0x0099FF;
		else if(ConfigCaptureCastle.CAPTURE_CASTLE_HIDE_TITLE_COLOR && _cha.isInCaptureCastleEvent())
			return 0xFFFF77;
		else if(ConfigBossHunting.BOSS_HUNTING_HIDE_TITLE_COLOR && _cha.isInBossHunting())
			return 0xFFFF77;
		else if(ConfigBattleGround.BATTLE_GROUND_HIDE_TITLE_COLOR && _cha.inBattleGround)
			return 0x0000FF;
		else if(FightClubConfig.FC_BLUE_TEAM_COLOR_TITLE > 0 && FightClubConfig.FC_RED_TEAM_COLOR_TITLE > 0 && _cha.isInFightClub())
			return _cha.getEventTitleColor();
		else
			return _cha.getTitleColor();
	}

	private String getName(L2Player player)
	{
		if(Config.EVENT_DEATHMATCH_HIDE_NAME && player.inDeathMatch)
			return "DeadMatch";
		else if(Config.EVENT_DECISIVEDEATH_HIDE_NAME && player.inDecisiveDeath)
			return "EpicFight";
		else if(ConfigCaptureCastle.CAPTURE_CASTLE_HIDE_NAME && player.isInCaptureCastleEvent())
			return "CaptureCastle";
		else if(ConfigBossHunting.BOSS_HUNTING_HIDE_NAME && player.isInBossHunting())
			return "BossHunting";
		else if(ConfigSquidGame.SquidGame_HideName && player.inSquidGame)
			return player.squidGameName;
		else if(player.isInStriderRace() && player.getTransformationName() != null && !player.getTransformationName().isEmpty())
			return player.getTransformationName();
		else if(player.isInFightClub() && FightClubConfig.FC_BLUE_TEAM_NAME_IN_NAME && FightClubConfig.FC_RED_TEAM_NAME_IN_NAME)
			return player.getEventName();
		else
			return player.getName();
	}

	private String getTitle(L2Player player)
	{
		if(player.isInvisible())
			return "Invisible";
		else if(player.inTvT && Config.TvT_ShowKills)
			return (player.isLangRus() ? "Убийств: " : "Kills: ") + player.eventKills;
		else if(player.inDeathMatch && Config.EVENT_DEATHMATCH_SHOW_KILLS)
			return (player.isLangRus() ? "Убийств: " : "Kills: ") + player.eventKills;
		else if(player.inDecisiveDeath && Config.EVENT_DECISIVEDEATH_SHOW_KILLS)
			return (player.isLangRus() ? "Убийств: " : "Kills: ") + player.eventKills;
		else if(player.isInCaptureCastleEvent() && ConfigCaptureCastle.CAPTURE_CASTLE_EnableKillsInTitle)
			return (player.isLangRus() ? "Убийств: " : "Kills: ") + player.eventKills;
		else if(player.inKoreanTvT && ConfigKoreanTvT.KOREAN_TVT_SHOW_KILLS)
			return (player.isLangRus() ? "Убийств: " : "Kills: ") + player.eventKills;
		else if(player.isInCaptureCastleEvent() && ConfigCaptureCastle.CAPTURE_CASTLE_HIDE_TITLE)
			return "";
		else if(player.isInBossHunting() && ConfigBossHunting.BOSS_HUNTING_SHOW_KILLS)
			return (player.isLangRus() ? "Убийств: " : "Kills: ") + player.eventKills;
		else if(player.isInBossHunting() && ConfigBossHunting.BOSS_HUNTING_HIDE_TITLE)
			return "";
		else if(player.inSquidGame && ConfigSquidGame.SquidGame_ShowPlace && player.eventKills > 0)
			return (player.isLangRus() ? "Место: " : "Place: ") + player.eventKills;
		else if(player.isInFightClub() && FightClubConfig.FC_BLUE_TEAM_NAME_IN_TITLE && FightClubConfig.FC_RED_TEAM_NAME_IN_TITLE)
			return player.getEventTitle();
		else if(player.inBattleGround && ConfigBattleGround.BATTLE_GROUND_SHOW_KILLS)
		{
			if(ConfigBattleGround.BATTLE_GROUND_DEATH_COUNTS > 0)
				return "Kills: " + player.eventKills + " Life: " + player.eventDeaths;
			else
				return "Kills: " + player.eventKills;
		}
		else if(Config.SERVICES_ANI_TITLE_ENABLED && player.getTransformationTitle() != null)
			return player.getEventTitle();
		else
			return player.getTitle();
	}

	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}

	@Override
	protected void onNewMaskAdded(UserInfoType component)
	{
		calcBlockSize(component);
	}

	private void calcBlockSize(UserInfoType type)
	{
		switch(type)
		{
			case BASIC_INFO:
			{
				_initSize += type.getBlockLength() + (_name.length() * 2);
				break;
			}
			case CLAN:
			{
				_initSize += type.getBlockLength() + (title.length() * 2);
				break;
			}
			default:
			{
				_initSize += type.getBlockLength();
				break;
			}
		}
	}

	@Override
	protected boolean canWrite()
	{
		return can_writeImpl;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(obj_id);

		writeD(_initSize);
		writeH(UserInfoType.values().length);
		writeB(_masks);

		if(containsMask(UserInfoType.RELATION))
			writeD(_relation);

		if(containsMask(UserInfoType.BASIC_INFO))
		{
			writeH(UserInfoType.BASIC_INFO.getBlockLength() + (_name.length() * 2));
			writeString(_name);
			writeC(gm_commands);
			writeC(_race);
			writeC(sex);
			writeD(base_class);
			writeD(class_id);
			writeC(level);
		}

		if(containsMask(UserInfoType.BASE_STATS))
		{
			writeH(UserInfoType.BASE_STATS.getBlockLength());
			writeH(_str);
			writeH(_dex);
			writeH(_con);
			writeH(_int);
			writeH(_wit);
			writeH(_men);
			writeH(0x00); // LUC
			writeH(0x00); // CHA
		}

		if(containsMask(UserInfoType.MAX_HPCPMP))
		{
			writeH(UserInfoType.MAX_HPCPMP.getBlockLength());
			writeD(maxHp);
			writeD(maxMp);
			writeD(maxCp);
		}

		if(containsMask(UserInfoType.CURRENT_HPMPCP_EXP_SP))
		{
			writeH(UserInfoType.CURRENT_HPMPCP_EXP_SP.getBlockLength());
			writeD(curHp);
			writeD(curMp);
			writeD(curCp);
			writeQ(_sp);
			writeQ(_exp);
			writeF(_expPercent);
		}

		if(containsMask(UserInfoType.ENCHANTLEVEL))
		{
			writeH(UserInfoType.ENCHANTLEVEL.getBlockLength());
			writeC(_enchant);
			writeC(_armorSetEnchant); // enchant armor effect
		}

		if(containsMask(UserInfoType.APPAREANCE))
		{
			writeH(UserInfoType.APPAREANCE.getBlockLength());
			writeD(hair_style);
			writeD(hair_color);
			writeD(face);
			writeC(0x01);  //переключения прически/головного убора
		}

		if(containsMask(UserInfoType.STATUS))
		{
			writeH(UserInfoType.STATUS.getBlockLength());
			writeC(mount_type);
			writeC(private_store);
			writeC(can_crystalize);
			writeC(0x00);
		}

		if(containsMask(UserInfoType.STATS))
		{
			writeH(UserInfoType.STATS.getBlockLength());
			writeH(_weaponFlag);
			writeD(_patk);
			writeD(_patkspd);
			writeD(_pdef);
			writeD(evasion);
			writeD(accuracy);
			writeD(crit);
			writeD(_matk);
			writeD(_matkspd);
			writeD(_patkspd);
			writeD(evasion);
			writeD(_mdef);
			writeD(accuracy);
			writeD(crit);
		}

		if(containsMask(UserInfoType.ELEMENTALS))
		{
			writeH(UserInfoType.ELEMENTALS.getBlockLength());
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
		}

		if(containsMask(UserInfoType.POSITION))
		{
			writeH(UserInfoType.POSITION.getBlockLength());
			writeD(_loc.getX());
			writeD(_loc.getY());
			writeD(_loc.getZ());
			writeD(vehicle_obj_id);
		}

		if(containsMask(UserInfoType.SPEED))
		{
			writeH(UserInfoType.SPEED.getBlockLength());
			writeH(_runSpd);
			writeH(_walkSpd);
			writeH(_swimSpd);
			writeH(_swimSpd);
			writeH(_flRunSpd);
			writeH(_flWalkSpd);
			writeH(_flyRunSpd);
			writeH(_flyWalkSpd);
		}

		if(containsMask(UserInfoType.MULTIPLIER))
		{
			writeH(UserInfoType.MULTIPLIER.getBlockLength());
			writeF(move_speed);
			writeF(attack_speed);
		}

		if(containsMask(UserInfoType.COL_RADIUS_HEIGHT))
		{
			writeH(UserInfoType.COL_RADIUS_HEIGHT.getBlockLength());
			writeF(col_radius);
			writeF(col_height);
		}

		if(containsMask(UserInfoType.ATK_ELEMENTAL))
		{
			writeH(UserInfoType.ATK_ELEMENTAL.getBlockLength());
			writeC(-1);
			writeH(0x00);
		}

		if(containsMask(UserInfoType.CLAN))
		{
			writeH(UserInfoType.CLAN.getBlockLength() + (title.length() * 2));
			writeString(title);
			writeH(pledge_type);
			writeD(clan_id);
			writeD(large_clan_crest_id);
			writeD(clan_crest_id);
			writeD(ClanPrivs);
			writeC(_isClanLeader);
			writeD(ally_id);
			writeD(ally_crest_id);
			writeC(partyRoom);
		}

		if(containsMask(UserInfoType.SOCIAL))
		{
			writeH(UserInfoType.SOCIAL.getBlockLength());
			writeC(pvp_flag);
			writeD(-Math.min(karma, 999999));
			writeC(noble);
			writeC(hero);
			writeC(pledge_class);
			writeD(pk_kills);
			writeD(pvp_kills);
			writeH(rec_left);
			writeH(rec_have);
		}

		if(containsMask(UserInfoType.VITA_FAME))
		{
			writeH(UserInfoType.VITA_FAME.getBlockLength());
			writeD(0x00);
			writeC(0x00); // Vita Bonus
			writeD(0x00);
			writeD(0x00); // raid points
		}

		if(containsMask(UserInfoType.SLOTS))
		{
			writeH(UserInfoType.SLOTS.getBlockLength());
			writeC(0x00/*talismans*/);
			writeC(0x00/*_jewelsLimit*/);
			writeC(team);
			writeC(0x00); // (1 = Red, 2 = White, 3 = White Pink) dotted ring on the floor
			writeC(0x00);
			writeC(0x00);
			writeC(0x00);
			writeC(0x00/*_activeMainAgathionSlot*/);	// 140 PROTOCOL
			writeC(0x00/*_subAgathionsLimit*/);	// 140 PROTOCOL
			writeC(0x00);	// SEVEN SIGNS
		}

		if(containsMask(UserInfoType.MOVEMENTS))
		{
			writeH(UserInfoType.MOVEMENTS.getBlockLength());
			writeC(_moveType);
			writeC(running);
		}

		if(containsMask(UserInfoType.COLOR))
		{
			writeH(UserInfoType.COLOR.getBlockLength());
			writeD(name_color);
			writeD(title_color);
		}

		if(containsMask(UserInfoType.INVENTORY_LIMIT))
		{
			writeH(UserInfoType.INVENTORY_LIMIT.getBlockLength());
			writeH(0x00);
			writeH(0x00);
			writeH(InventoryLimit);
			writeC(0); // hide title - 1, 0 - no
		}

		if(containsMask(UserInfoType.UNK_3))
		{
			writeH(UserInfoType.UNK_3.getBlockLength());
			writeD(0x00);
			writeH(0x00);
			writeC(0);
		}

		if(containsMask(UserInfoType.ATT_SPIRITS)) // 152
		{
			writeH(UserInfoType.ATT_SPIRITS.getBlockLength());
			writeD(-1);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
		writeD(_loc.h);
		writeD(obj_id);
		writeS(_name);
		writeD(_race);
		writeD(sex);
		writeD(base_class);
		writeD(level);
		writeQ(_exp);
		writeD(_str);
		writeD(_dex);
		writeD(_con);
		writeD(_int);
		writeD(_wit);
		writeD(_men);
		writeD(maxHp);
		writeD(curHp);
		writeD(maxMp);
		writeD(curMp);
		writeD(_sp);
		writeD(curLoad);
		writeD(maxLoad);
		writeD(_weaponFlag); // unknown. В снифе бывает 0х28 и 0х14
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_UNDER, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_REAR, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_LEAR, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_NECK, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_RFINGER, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_LFINGER, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_HEAD, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_RHAND, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_LHAND, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_GLOVES, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_CHEST, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_LEGS, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_FEET, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_BACK, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_RHAND, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_HAIR, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoObjectId(Inventory.PAPERDOLL_DHAIR, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_UNDER, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_REAR, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_LEAR, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_NECK, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_RFINGER, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_LFINGER, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_HEAD, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_RHAND, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_LHAND, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_GLOVES, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_CHEST, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_LEGS, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_FEET, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_BACK, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_RHAND, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_HAIR, player.isVisibleFakeCostume()));
		writeD(_inv.getPaperdollUserInfoItemId(Inventory.PAPERDOLL_DHAIR, player.isVisibleFakeCostume()));
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
		writeD(_patk);
		writeD(_patkspd);
		writeD(_pdef);
		writeD(evasion);
		writeD(accuracy);
		writeD(crit);
		writeD(_matk);
		writeD(_matkspd);
		writeD(_patkspd);
		writeD(_mdef);
		writeD(pvp_flag);
		writeD(karma);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimSpd); // swimspeed
		writeD(_swimSpd); // swimspeed
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(move_speed);
		writeF(attack_speed);
		writeF(col_radius);
		writeF(col_height);
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		writeD(gm_commands);
		writeS(title);
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);
		// 0x40 leader rights
		// siege flags: attacker - 0x180 sword over name, defender - 0x80 shield, 0xC0 crown (|leader), 0x1C0 flag (|leader)
		writeD(_relation);
		writeC(mount_type); // mount type
		writeC(private_store);
		writeC(can_crystalize);
		writeD(pk_kills);
		writeD(pvp_kills);
		writeH(cubics.length);
		for(EffectCubic cubic : cubics)
			writeH(cubic == null ? 0 : cubic.getId());
		writeC(partyRoom);
		writeD(AbnormalEffect);
		writeC(0x00); //1-find party members
		writeD(ClanPrivs);
		writeH(rec_left);
		writeH(rec_have);
		writeD(mount_id);
		writeH(InventoryLimit);
		writeD(class_id);
		writeD(0x00); // special effects? circles around player...
		writeD(maxCp);
		writeD(curCp);
		writeC(_enchant);
		writeC(team);
		writeD(large_clan_crest_id);
		writeC(noble);
		writeC(hero);
		writeC(fishing);
		writeD(_fishLoc.getX());
		writeD(_fishLoc.getY());
		writeD(_fishLoc.getZ());
		writeD(name_color);
		writeC(running);
		writeD(pledge_class);
		writeD(pledge_type);
		writeD(title_color);
		writeD(cw_level);
	}
}