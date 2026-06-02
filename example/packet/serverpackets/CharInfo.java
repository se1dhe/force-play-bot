package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.configuration.*;
import l2p.gameserver.instancemanager.CursedWeaponsManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.base.Race;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.skills.AbnormalEffect;
import l2p.gameserver.skills.effects.EffectCubic;
import l2p.gameserver.utils.Location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharInfo extends L2GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CharInfo.class);

	private static final Location _fLoc = new Location();

	private L2Player _cha;
	private L2Player _receiver;
	private Inventory _inv;
	private int _mAtkSpd, _pAtkSpd;
	private int _runSpd, _walkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd, _swimSpd;
	private Location _loc, _fishLoc;
	private String _name, _title;
	private int _objId, _race, _sex, base_class, pvp_flag, karma, rec_have;
	private float speed_move, speed_atack, col_radius, col_height;
	private int hair_style, hair_color, face, abnormal_effect;
	private int clan_id, clan_crest_id, large_clan_crest_id, ally_id, ally_crest_id, class_id;
	private int plg_class, pledge_type, clan_rep_score, cw_level, mount_id;
	private int _nameColor, _title_color;
	private byte _sit, _run, _combat, _dead, _invis, mount_type, private_store, rec_left, _enchant, _armorSetEnchant;
	private byte _team, _noble, _hero, _fishing, partyRoom;
	private EffectCubic[] cubics;
	private int maxCp,maxHp, maxMp, curHp, curMp, curCp;
	private int _transform, _agathion;
	private AbnormalEffect[] abnormalEffects;
	private int clanLeader;
	private boolean can_writeImpl = false;
	private boolean is_at_special_event;

	public CharInfo(L2Player cha, L2Player receiver)
	{
		if(cha == null)
		{
			System.out.println("CharInfo: cha is null!");
			Thread.dumpStack();
			return;
		}

		if(receiver == null)
			return;

		if(cha.isInvisible())
			return;

		if(cha.isDeleting())
			return;

		if(receiver.getObjectId() == _objId)
		{
			_log.error("You cant send CIPacket about his character to active user!!!");
			return;
		}

		_cha = cha;
		_receiver = receiver;

		is_at_special_event = _cha.inLH;

		if(_cha.isCursedWeaponEquipped())
		{
			_name = _cha.getName();
			_title = "";
			clan_id = 0;
			clan_crest_id = 0;
			ally_id = 0;
			ally_crest_id = 0;
			large_clan_crest_id = 0;
			cw_level = CursedWeaponsManager.getInstance().getLevel(_cha.getCursedWeaponEquippedId());
		}
		else
		{
			_name = getName();
			if(_cha.getPrivateStoreType() != 0)
				_title = "";
			else if(!_cha.isConnected() && !_cha.isFashion)
			{
				_title = "DISCONNECTED";
				_title_color = 255;
			}
			else
			{
				_title = getTitle();
				_title_color = getTitleColor();
			}
			if(Config.EVENT_DEATHMATCH_HIDE_CLAN && _cha.inDeathMatch)
			{
				clan_id = 0;
				clan_crest_id = 0;
				ally_id = 0;
				ally_crest_id = 0;
				large_clan_crest_id = 0;
			}
			else if(Config.EVENT_DECISIVEDEATH_HIDE_CLAN && _cha.inDecisiveDeath)
			{
				clan_id = 0;
				clan_crest_id = 0;
				ally_id = 0;
				ally_crest_id = 0;
				large_clan_crest_id = 0;
			}
			else if(ConfigCaptureCastle.CAPTURE_CASTLE_HIDE_CLAN_ALY_INFO && _cha.isInCaptureCastleEvent())
			{
				clan_id = 0;
				clan_crest_id = 0;
				ally_id = 0;
				ally_crest_id = 0;
				large_clan_crest_id = 0;
			}
			else if(ConfigBossHunting.BOSS_HUNTING_HIDE_CLAN_ALY_INFO && _cha.isInBossHunting())
			{
				clan_id = 0;
				clan_crest_id = 0;
				ally_id = 0;
				ally_crest_id = 0;
				large_clan_crest_id = 0;
			}
			else if(ConfigBattleGround.BATTLE_GROUND_HIDE_CLAN && _cha.inBattleGround)
			{
				clan_id = 0;
				clan_crest_id = 0;
				ally_id = 0;
				ally_crest_id = 0;
				large_clan_crest_id = 0;
			}
			else if(FightClubConfig.FC_HIDE_CLAN_ALY_INFO && _cha.isInFightClub())
			{
				clan_id = 0;
				clan_crest_id = 0;
				ally_id = 0;
				ally_crest_id = 0;
				large_clan_crest_id = 0;
			}
			else
			{
				clan_id = _cha.getClanId();
				clan_crest_id = _cha.getClanCrestId();
				ally_id = _cha.getAllyId();
				ally_crest_id = _cha.getAllyCrestId();
				large_clan_crest_id = _cha.getClanCrestLargeId();
			}
			cw_level = 0;
		}

		_inv = _cha.getInventory();
		if(_inv == null)
			return;
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
			_runSpd = _cha.getTemplate().baseRunSpd;
		}
		_walkSpd = _cha.getTemplate().baseWalkSpd;
		speed_move = _cha.getMovementSpdMultiplier();

		if(_cha.isInVehicle())
			_loc = _cha.getInVehiclePosition();
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
		_objId = _cha.getObjectId();
		_race = _cha.getBaseTemplate().race.ordinal();
		_sex = _cha.getSex();
		base_class = _cha.getBaseClassId();
		pvp_flag = _cha.getPvpFlag();
		karma = _cha.getKarma();
		speed_atack = _cha.getAttackSpeedMult();
		col_radius = _cha.getColRadius();
		col_height = _cha.getColHeight();
		hair_style = _cha.getHairStyle();
		hair_color = _cha.getHairColor();
		face = _cha.getFace();
		if(clan_id > 0 && _cha.getClan() != null)
			clan_rep_score = _cha.getClan().getReputationScore();
		else
			clan_rep_score = 0;
		_sit = _cha.isSitting() ? (byte) 0 : (byte) 1; // standing = 1 sitting = 0
		_run = _cha.isRunning() ? (byte) 1 : (byte) 0; // running = 1 walking = 0
		_combat = _cha.isInCombat() ? (byte) 1 : (byte) 0;
		_dead = _cha.isAlikeDead() && !_cha.isPendingRevive() ? (byte) 1 : (byte) 0;
		_invis = _cha.isInvisible() ? (byte) 1 : (byte) 0; // invisible = 1 visible = 0
		private_store = _cha.inObserverMode() ? 7 : (byte) _cha.getPrivateStoreType(); // 1 - sellshop
		cubics = _cha.getCubics().toArray(new EffectCubic[_cha.getCubics().size()]);
		abnormal_effect = _cha.getAbnormalEffectMask();
		rec_left = 0;
		rec_have = _cha.isGM() ? 0 : _cha.getRecomHave();
		class_id = _cha.getClassId().getId();
		maxCp = _cha.getMaxCp();
		maxHp = _cha.getMaxHp();
		maxMp = _cha.getMaxMp();
		curCp = (int) _cha.getCurrentCp();
		curHp = (int)_cha.getCurrentHp();
		curMp = (int)_cha.getCurrentMp();
		abnormalEffects = _cha.getVisualAbnormalEffects(receiver);
		if(Config.ALLOW_PARTY_MEMBERS_AURA)
		{
			if(_cha.getTeam() != 0)
				_team = (byte) _cha.getTeam(); // team circle around feet 1 = Blue, 2 = red
			else
			{
				if(_cha.isInParty() && receiver.isInParty() && _cha.getParty().containsMember(receiver) && receiver.isVisiblePartyAura())
				{
					if(_cha.getParty().isLeader(_cha))
						_team = 2;
					else
						_team = 1;
				}
				else
					_team = (byte) _cha.getTeam(); // team circle around feet 1 = Blue, 2 = red
			}
		}
		else
			_team = (byte) _cha.getTeam(); // team circle around feet 1 = Blue, 2 = red
		_noble = _cha.isNoble() ? (byte) 1 : (byte) 0; // 0x01: symbol on char menu ctrl+I
		if(receiver.getVarB("DisableHeroAura", false))
		{
			_hero = (byte) 0;
		}
		else
		{
			if (receiver.isITClient())
				_hero = _cha.isHero() ? (byte) 1 : (byte) 0;
			else
				_hero = _cha.isHero() ? (byte) 2 : (byte) 0;
		}
		_fishing = _cha.isFishing() ? (byte) 1 : (byte) 0;
		_fishLoc = _cha.getFishing() != null ? _cha.getFishLoc() : _fLoc;
		_nameColor = getNameColor(); // New C5
		plg_class = _cha.getPledgeClass();
		pledge_type = _cha.getPledgeType();
		partyRoom = _cha.getPartyRoom() != null ? (byte) 1 : (byte) 0;
		_transform = _cha.getTransformation();
		_agathion = _cha.getAgathionId();
		clanLeader = _cha.isClanLeader() ? 0x40 : 0x00;
		can_writeImpl = true;
	}

	private int getNameColor()
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

	private int getTitleColor()
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

	private String getName()
	{
		if(Config.EVENT_DEATHMATCH_HIDE_NAME && _cha.inDeathMatch)
			return "DeadMatch";
		else if(Config.EVENT_DECISIVEDEATH_HIDE_NAME && _cha.inDecisiveDeath)
			return "EpicFight";
		else if(ConfigCaptureCastle.CAPTURE_CASTLE_HIDE_NAME && _cha.isInCaptureCastleEvent())
			return "CaptureCastle";
		else if(ConfigSquidGame.SquidGame_HideName && _cha.inSquidGame)
			return _cha.squidGameName;
		else if(ConfigBossHunting.BOSS_HUNTING_HIDE_NAME && _cha.isInBossHunting())
			return "BossHunting";
		else if(ConfigBattleGround.BATTLE_GROUND_HIDE_NAME && _cha.inBattleGround)
			return "BattleGround";
		else if(_cha.isInStriderRace() && _cha.getTransformationName() != null && !_cha.getTransformationName().isEmpty())
			return _cha.getTransformationName();
		else if(_cha.isInFightClub() && FightClubConfig.FC_BLUE_TEAM_NAME_IN_NAME && FightClubConfig.FC_RED_TEAM_NAME_IN_NAME)
			return _cha.getEventName();
		else
			return _cha.getName();
	}

	private String getTitle()
	{
		if(_cha.inTvT && Config.TvT_ShowKills)
			return (_cha.isLangRus() ? "Убийств: " : "Kills: ") + _cha.eventKills;
		else if(_cha.inDeathMatch && Config.EVENT_DEATHMATCH_SHOW_KILLS)
			return (_cha.isLangRus() ? "Убийств: " : "Kills: ") + _cha.eventKills;
		else if(_cha.inKoreanTvT && ConfigKoreanTvT.KOREAN_TVT_SHOW_KILLS)
			return (_cha.isLangRus() ? "Убийств: " : "Kills: ") + _cha.eventKills;
		else if(_cha.inDecisiveDeath && Config.EVENT_DECISIVEDEATH_SHOW_KILLS)
			return "";
		else if(_cha.isInCaptureCastleEvent() && ConfigCaptureCastle.CAPTURE_CASTLE_EnableKillsInTitle)
			return (_cha.isLangRus() ? "Убийств: " : "Kills: ") + _cha.eventKills;
		else if(_cha.isInCaptureCastleEvent() && ConfigCaptureCastle.CAPTURE_CASTLE_HIDE_TITLE)
			return "";
		else if(_cha.isInBossHunting() && ConfigBossHunting.BOSS_HUNTING_SHOW_KILLS)
			return (_cha.isLangRus() ? "Убийств: " : "Kills: ") + _cha.eventKills;
		else if(_cha.isInBossHunting() && ConfigBossHunting.BOSS_HUNTING_HIDE_TITLE)
			return "";
		else if(_cha.inSquidGame && ConfigSquidGame.SquidGame_ShowPlace && _cha.eventKills > 0)
			return (_cha.isLangRus() ? "Место: " : "Place: ") + _cha.eventKills;
		else if(_cha.inBattleGround && ConfigBattleGround.BATTLE_GROUND_SHOW_KILLS)
			return "BattleGround";
		else if(_cha.isInFightClub() && FightClubConfig.FC_BLUE_TEAM_NAME_IN_TITLE && FightClubConfig.FC_RED_TEAM_NAME_IN_TITLE)
			return _cha.getEventTitle();
		else if(Config.SERVICES_ANI_TITLE_ENABLED && _cha.getTransformationTitle() != null)
			return _cha.getEventTitle();
		else
			return _cha.getTitle();
	}

	@Override
	protected boolean canWrite()
	{
		return can_writeImpl;
	}

	@Override
	protected final void writeImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.equals(_cha))
		{
			_log.error("You can't send CharInfo about his character to active user!!!");
			Thread.dumpStack();
			return;
		}

		if(activeChar.noHeroAure)
			_hero = 0;

		writeC(0x00);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(_loc.h); //?
		writeD(_objId);
		if(is_at_special_event)
		{
			writeS("Player");
			writeH(Race.dwarf.ordinal());
			writeC(1);
		}
		else
		{
			writeS(_name);
			writeH(_race);
			writeC(_sex);
		}
		writeD(base_class);
		if(is_at_special_event)
		{
			writeD(0);
			writeD(0);
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
			writeD(0);
			writeD(6408);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(0);
			writeD(0);
		}
		else
		{
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_UNDER, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_BACK, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DHAIR, _receiver.isVisibleFakeCostume()));
		}

		for(int paperdollId : PAPERDOLL_ORDER_AUGMENT)
		{
			writeD(_inv.getPaperdollAugmentationId1(paperdollId));
			writeD(_inv.getPaperdollAugmentationId2(paperdollId));
		}

		writeC(_armorSetEnchant);	// Armor Enchant Effect

		for(int paperdollId : PAPERDOLL_ORDER_VISUAL_ID_GK)
			writeD(_inv.getPaperdollVisualItemId(paperdollId));

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

		writeF(speed_move); // _cha.getProperMultiplier()
		writeF(speed_atack); // _cha.getAttackSpeedMultiplier()
		if(is_at_special_event)
		{
			writeF(9);
			writeF(18);
		}
		else
		{
			writeF(col_radius);
			writeF(col_height);
		}
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		if(is_at_special_event)
		{
			writeS("");
		}
		else
		{
			writeS(_title);
		}
		if(is_at_special_event)
		{
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
		}
		else
		{
			writeD(clan_id);
			writeD(clan_crest_id);
			writeD(ally_id);
			writeD(ally_crest_id);
		}

		writeC(_sit);
		writeC(_run);
		writeC(_combat);
		writeC(_dead);
		writeC(_invis);
		writeC(mount_type); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		writeC(private_store);
		writeH(cubics.length);
		for(EffectCubic cubic : cubics)
			writeH(cubic == null ? 0 : cubic.getId());
		writeC(partyRoom); // find party members
		writeC(0x00); // Is Flying transform
		writeH(rec_have);
		writeD(mount_id);
		writeD(class_id);
		writeD(0x00);
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
		writeC(plg_class);
		writeH(pledge_type);
		writeD(_title_color);
		writeC(cw_level);
		writeD(clan_rep_score);
		writeD(_transform);
		writeD(_agathion);

		writeC(clanLeader);	// UNK

		writeD(curCp);
		writeD(curHp);
		writeD(maxHp);
		writeD(curMp);
		writeD(maxMp);

		writeC(0x00);	// UNK

		writeD(abnormalEffects.length);
		for(AbnormalEffect abnormal : abnormalEffects)
			writeH(abnormal.getClientId());

		writeC(0x00);	// Festival chaos
		writeC(0x01/*_showHeadAccessories*/);
		writeC(0x00); // Abilities points
	}

	@Override
	protected final void writeImplIT()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.equals(_cha))
		{
			_log.error("You can't send CharInfo about his character to active user!!!");
			Thread.dumpStack();
			return;
		}
		if(activeChar.noHeroAure)
			_hero = 0;

		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(_loc.h); //?
		writeD(_objId);
		if(is_at_special_event)
		{
			writeS("Player");
			writeD(Race.dwarf.ordinal());
			writeD(1);
		}
		else
		{
			writeS(_name);
			writeD(_race);
			writeD(_sex);
		}
		writeD(base_class);
		if(is_at_special_event)
		{
			writeD(0);
			writeD(0);
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
			writeD(0);
			writeD(6408);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(0);
			writeD(0);
		}
		else
		{
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_UNDER, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_BACK, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR, _receiver.isVisibleFakeCostume()));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DHAIR, _receiver.isVisibleFakeCostume()));
		}
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
		if(is_at_special_event)
		{
			writeF(9);
			writeF(18);
		}
		else
		{
			writeF(col_radius);
			writeF(col_height);
		}
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		if(is_at_special_event)
		{
			writeS("");
		}
		else
		{
			writeS(_title);
		}
		if(is_at_special_event)
		{
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
		}
		else
		{
			writeD(clan_id);
			writeD(clan_crest_id);
			writeD(ally_id);
			writeD(ally_crest_id);
		}

		writeD(0);

		writeC(_sit);
		writeC(_run);
		writeC(_combat);
		writeC(_dead);
		writeC(_invis);
		writeC(mount_type); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		writeC(private_store);
		writeH(cubics.length);
		for(EffectCubic cubic : cubics)
			writeH(cubic == null ? 0 : cubic.getId());
		writeC(partyRoom); // find party members
		writeD(abnormal_effect);
		writeC(rec_left);
		writeH(rec_have);
		writeD(class_id);
		writeD(maxCp);
		writeD(curCp);
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

	public static final int[] PAPERDOLL_ORDER_VISUAL_ID_GK = {
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_LHAND,
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_GLOVES,
		Inventory.PAPERDOLL_CHEST,
		Inventory.PAPERDOLL_LEGS,
		Inventory.PAPERDOLL_FEET,
		Inventory.PAPERDOLL_DHAIR,
		Inventory.PAPERDOLL_HAIR
	};

	public static int[] PAPERDOLL_ORDER_AUGMENT = new int[]{
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_LHAND,
		Inventory.PAPERDOLL_RHAND
	};
}