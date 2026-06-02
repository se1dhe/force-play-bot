package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2HennaInstance;

//ccccccdd[dd]
public class GMHennaInfo extends L2GameServerPacket
{
	private int _count, _str, _con, _dex, _int, _wit, _men;
	private final L2HennaInstance[] _hennas = new L2HennaInstance[3];

	public GMHennaInfo(final L2Player cha)
	{
		_str = cha.getHennaStatSTR();
		_con = cha.getHennaStatCON();
		_dex = cha.getHennaStatDEX();
		_int = cha.getHennaStatINT();
		_wit = cha.getHennaStatWIT();
		_men = cha.getHennaStatMEN();

		_count = 0;
		for(int i = 0; i < 3; i++)
		{
			L2HennaInstance h = cha.getHenna(i + 1);
			if(h != null)
				_hennas[_count++] = h;
		}
	}

	// TODO [V] - такая структура?
	@Override
	protected void writeImpl()
	{
		writeH(_int);
		writeH(_str);
		writeH(_con);
		writeH(_men);
		writeH(_dex);
		writeH(_wit);
		writeH(0x00); //equip LUC
		writeH(0x00); //equip CHA
		writeD(3);
		writeD(_count);
		for(int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(_hennas[i].getSymbolId());
		}
		writeD(0x00);	// Premium symbol ID
		writeD(0x00);	// Premium symbol active
		writeD(0x00);	// Premium symbol left time
	}

	@Override
	protected void writeImplIT()
	{
		writeC(_int);
		writeC(_str);
		writeC(_con);
		writeC(_men);
		writeC(_dex);
		writeC(_wit);
		writeD(3);
		writeD(_count);
		for(int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(_hennas[i].getSymbolId());
		}
	}
}