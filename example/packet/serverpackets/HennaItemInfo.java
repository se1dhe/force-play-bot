package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2HennaInstance;

public class HennaItemInfo extends L2GameServerPacket
{
	private int char_adena, _str, _con, _dex, _int, _wit, _men;
	private L2HennaInstance _henna;
	private boolean _free = false;

	public HennaItemInfo(L2HennaInstance henna, L2Player player)
	{
		_henna = henna;
		char_adena = player.getAdena();
		if(player.hennaSet)
			_free = true;
		else
		{
			_str = player.getSTR();
			_dex = player.getDEX();
			_con = player.getCON();
			_int = player.getINT();
			_wit = player.getWIT();
			_men = player.getMEN();
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_henna.getSymbolId()); //symbol Id
		writeD(_henna.getItemIdDye()); //item id of dye
		writeQ(_free ? 0 : _henna.getAmountDyeRequire()); // total amount of dye require
		writeQ(_free ? 0 : _henna.getPrice()); //total amount of aden require to draw symbol
		writeD(1); //able to draw or not 0 is false and 1 is true
		writeQ(char_adena);
		writeD(_int); //current INT
		writeH(_free ? 0 : _int + _henna.getStatINT()); //equip INT
		writeD(_str); //current STR
		writeH(_free ? 0 : _str + _henna.getStatSTR()); //equip STR
		writeD(_con); //current CON
		writeH(_free ? 0 : _con + _henna.getStatCON()); //equip CON
		writeD(_men); //current MEM
		writeH(_free ? 0 : _men + _henna.getStatMEN()); //equip MEM
		writeD(_dex); //current DEX
		writeH(_free ? 0 : _dex + _henna.getStatDEX()); //equip DEX
		writeD(_wit); //current WIT
		writeH(_free ? 0 : _wit + _henna.getStatWIT()); //equip WIT
		writeD(0x00); //current LUC
		writeH(0x00); //equip LUC
		writeD(0x00); //current CHA
		writeH(0x00); //equip CHA
		writeD(0x00); // period
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_henna.getSymbolId()); //symbol Id
		writeD(_henna.getItemIdDye()); //item id of dye
		writeD(_free ? 0 : _henna.getAmountDyeRequire()); // total amount of dye require
		writeD(_free ? 0 : _henna.getPrice()); //total amount of aden require to draw symbol
		writeD(1); //able to draw or not 0 is false and 1 is true
		writeD(char_adena);
		writeD(_int); //current INT
		writeC(_free ? 0 : _int + _henna.getStatINT()); //equip INT
		writeD(_str); //current STR
		writeC(_free ? 0 : _str + _henna.getStatSTR()); //equip STR
		writeD(_con); //current CON
		writeC(_free ? 0 : _con + _henna.getStatCON()); //equip CON
		writeD(_men); //current MEM
		writeC(_free ? 0 : _men + _henna.getStatMEN()); //equip MEM
		writeD(_dex); //current DEX
		writeC(_free ? 0 : _dex + _henna.getStatDEX()); //equip DEX
		writeD(_wit); //current WIT
		writeC(_free ? 0 : _wit + _henna.getStatWIT()); //equip WIT
	}
}