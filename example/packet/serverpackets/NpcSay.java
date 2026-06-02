package l2p.gameserver.serverpackets;

import l2p.gameserver.model.instances.L2NpcInstance;

public class NpcSay extends L2GameServerPacket
{
	int _objId;
	int _type;
	int _id;
	String _text;

	// TODO не найден номер
	public NpcSay(L2NpcInstance npc, int chatType, String text)
	{
		_objId = npc.getObjectId();
		_type = chatType;
		_text = text;
		_id = npc.getNpcId();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objId); // object_id отсылающего
		writeD(_type); // Тип чата, 0 = tell, 1 = shout, 2 = pm, 3 = party... Совпадает с Say2
		writeD(1000000 + _id); // npc id от кого отправлен пакет, клиент получает по нему имя.
		writeD(-1);
		writeS(_text); // текст для отправки.
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_objId); // object_id отсылающего
		writeD(_type); // Тип чата, 0 = tell, 1 = shout, 2 = pm, 3 = party... Совпадает с Say2
		writeD(1000000 + _id); // npc id от кого отправлен пакет, клиент получает по нему имя.
		writeS(_text); // текст для отправки.
	}
}