package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.utils.Location;

public class GMViewCharacterInfo extends L2GameServerPacket
{
    private Location _loc;
    private PcInventory _inv;
    private int obj_id, _race, _sex, class_id, pvp_flag, karma, level, mount_type;
    private int _str, _con, _dex, _int, _wit, _men, _sp;
    private int curHp, maxHp, curMp, maxMp, curCp, maxCp, curLoad, maxLoad, rec_left, rec_have;
    private int _patk, _patkspd, _pdef, evasion, accuracy, crit, _matk, _matkspd;
    private int _mdef, hair_style, hair_color, face, gm_commands;
    private int clan_id, clan_crest_id, ally_id, title_color;
    private int noble, hero, private_store, name_color, pk_kills, pvp_kills;
    private int _runSpd, _walkSpd, _swimSpd, DwarvenCraftLevel, running, pledge_class;
    private String _name, title;
    private long _exp;
    private float move_speed, attack_speed, col_radius, col_height;
    private double _expPercent;

    public GMViewCharacterInfo(final L2Player cha)
    {
        _inv = cha.getInventory();
        _loc = cha.getLoc();
        obj_id = cha.getObjectId();
        _name = cha.getName();
        _race = cha.getRace().ordinal();
        _sex = cha.getSex();
        class_id = cha.getClassId().getId();
        level = cha.getLevel();
        _exp = cha.getExp();
        _str = cha.getSTR();
        _dex = cha.getDEX();
        _con = cha.getCON();
        _int = cha.getINT();
        _wit = cha.getWIT();
        _men = cha.getMEN();
        curHp = (int) cha.getCurrentHp();
        maxHp = cha.getMaxHp();
        curMp = (int) cha.getCurrentMp();
        maxMp = cha.getMaxMp();
        _sp = cha.getSp();
        curLoad = cha.getCurrentLoad();
        maxLoad = cha.getMaxLoad();
        _patk = cha.getPAtk(null);
        _patkspd = cha.getPAtkSpd();
        _pdef = cha.getPDef(null);
        evasion = cha.getEvasionRate(null);
        accuracy = cha.getAccuracy();
        crit = cha.getCriticalHit(null, null);
        _matk = cha.getMAtk(null, null);
        _matkspd = cha.getMAtkSpd();
        _mdef = cha.getMDef(null, null);
        pvp_flag = cha.getPvpFlag();
        karma = cha.getKarma();
        move_speed = cha.getMovementSpdMultiplier();
        _runSpd = (int)(cha.getRunSpeed() / move_speed);
        _walkSpd = (int)(cha.getWalkSpeed() / move_speed);
        _swimSpd = cha.getSwimSpd();
        attack_speed = cha.getAttackSpeedMult();
        mount_type = cha.getMountEngine().getMountType();
        col_radius = cha.getColRadius();
        col_height = cha.getColHeight();
        hair_style = cha.getHairStyle();
        hair_color = cha.getHairColor();
        face = cha.getFace();
        gm_commands = cha.isGM() ? 1 : 0;
        title = cha.getTitle();
        _expPercent = Experience.getExpPercent(cha.getLevel(), cha.getExp());
        clan_id = cha.getClanId();
        clan_crest_id = cha.getClanCrestId(); // clan crest
        ally_id = cha.getAllyId();
        private_store = cha.inObserverMode() ? 7 : cha.getPrivateStoreType();
        DwarvenCraftLevel = Math.max(cha.getSkillLevel(L2Skill.SKILL_CREATE_COMMON), 0);
        pk_kills = cha.getPkKills();
        pvp_kills = cha.getPvpKills();
        rec_left = cha.getRecomLeft(); // c2 recommendations remaining
        rec_have = cha.getRecomHave(); // c2 recommendations received
        curCp = (int) cha.getCurrentCp();
        maxCp = cha.getMaxCp();
        running = cha.isRunning() ? 0x01 : 0x00;
        pledge_class = cha.getPledgeClass();
        noble = cha.isNoble() ? 1 : 0; // 0x01: symbol on char menu ctrl+I
        hero = cha.isHero() ? 1 : 0; // 0x01: Hero Aura and symbol
        name_color = cha.getNameColor();
        title_color = cha.getTitleColor();
    }

    @Override
    protected final void writeImpl()
    {
        writeD(_loc.getX());
        writeD(_loc.getY());
        writeD(_loc.getZ());
        writeD(_loc.h);
        writeD(obj_id);
        writeS(_name);
        writeD(_race);
        writeD(_sex);
        writeD(class_id);
        writeD(level);
        writeQ(_exp);
        writeF(_expPercent);
        writeD(_str);
        writeD(_dex);
        writeD(_con);
        writeD(_int);
        writeD(_wit);
        writeD(_men);
        writeD(0x00);
        writeD(0x00);
        writeD(maxHp);
        writeD(curHp);
        writeD(maxMp);
        writeD(curMp);
        writeQ(_sp);
        writeD(curLoad);
        writeD(maxLoad);
        writeD(0x28);

        for(int paperdollId : Inventory.PAPERDOLL_ORDER_GK)
		    writeD(_inv.getPaperdollObjectId(paperdollId));

        for(int paperdollId : Inventory.PAPERDOLL_ORDER_GK)
            writeD(_inv.getPaperdollItemId(paperdollId));

        // TODO [V] - так?
        for(int i = 0; i < 11; i++)
        {
            writeD(0x00);
            writeD(0x00);
        }

        writeC(0x00);
        writeC(0x00);
        writeD(0x00);
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
        writeD(evasion);
        writeD(accuracy);
        writeD(crit);
        writeD(pvp_flag);
        writeD(karma);
        writeD(_runSpd);
        writeD(_walkSpd);
        writeD(_swimSpd); // swimspeed
        writeD(_swimSpd); // swimspeed
        writeD(_runSpd);
        writeD(_walkSpd);
        writeD(_runSpd);
        writeD(_walkSpd);
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
        writeC(mount_type);
        writeC(private_store);
        writeC(DwarvenCraftLevel); // _cha.getDwarvenCraftLevel() > 0 ? 1 : 0
        writeD(pk_kills);
        writeD(pvp_kills);
        writeH(rec_left);
        writeH(rec_have); // Blue value for name (0 = white, 255 = pure blue)
        writeD(class_id);
        writeD(0x00); // special effects? circles around player...
        writeD(maxCp);
        writeD(curCp);
        writeC(running); // changes the Speed display on Status Window
        writeC(321);
        writeD(pledge_class); // changes the text above CP on Status Window
        writeC(noble);
        writeC(hero);
        writeD(name_color);
        writeD(title_color);

        writeH(-1);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);

        writeD(0x00);
        writeD(0x00);

        writeD(0);
        writeD(0);
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
        writeD(_sex);
        writeD(class_id);
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
        writeD(0x28);

		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_DHAIR));
        writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
        writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
        writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
        writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
        writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
        writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
        writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
        writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
        writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
        writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
        writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
        writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
        writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
        writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
        writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_DHAIR));

		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_REAR));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_NECK));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));

        for(int i = 0; i < 34; i++)
            writeH(0);

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
        writeD(_runSpd);
        writeD(_walkSpd);
        writeD(_runSpd);
        writeD(_walkSpd);
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
        writeC(mount_type);
        writeC(private_store);
        writeC(DwarvenCraftLevel); // _cha.getDwarvenCraftLevel() > 0 ? 1 : 0
        writeD(pk_kills);
        writeD(pvp_kills);
        writeH(rec_left);
        writeH(rec_have); // Blue value for name (0 = white, 255 = pure blue)
        writeD(class_id);
        writeD(0x00); // special effects? circles around player...
        writeD(maxCp);
        writeD(curCp);
        writeC(running); // changes the Speed display on Status Window
        writeC(321);
        writeD(pledge_class); // changes the text above CP on Status Window
        writeC(noble);
        writeC(hero);
        writeD(name_color);
        writeD(title_color);
    }
}