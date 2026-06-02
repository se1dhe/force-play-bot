package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Macro;
import l2p.gameserver.model.base.MacroUpdateType;

public class SendMacroList extends L2GameServerPacket
{
	private final MacroUpdateType _type;
	private final int _rev;
	private final int _count;
	private final L2Macro _macro;

	public SendMacroList(MacroUpdateType macroUpdateType, int count, L2Macro macro)
	{
		_rev = 0;
		_type = macroUpdateType;
		_count = count;
		_macro = macro;
	}

	public SendMacroList(int rev, int count, L2Macro macro)
	{
		_rev = rev;
		_type = MacroUpdateType.ADD;
		_count = count;
		_macro = macro;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type.getId()); //unknown
		writeD(_type != MacroUpdateType.LIST ? _macro.id : 0); //Macro ID
		writeC(_count); //count of Macros
		writeC(_macro != null ? 1 : 0); //checked
		if(_macro != null && _type != MacroUpdateType.DELETE)
		{
			writeD(_macro.id); //Macro ID
			writeS(_macro.name); //Macro Name
			writeS(_macro.descr); //Desc
			writeS(_macro.acronym); //acronym
			writeD(_macro.icon); //icon
			writeC(_macro.commands.length); //count
			for(int i = 0; i < _macro.commands.length; i++)
			{
				L2Macro.L2MacroCmd cmd = _macro.commands[i];
				writeC(i + 1); //i of count
				writeC(cmd.type); //type  1 = skill, 3 = action, 4 = shortcut
				writeD(cmd.d1); // skill id
				writeC(cmd.d2); // shortcut id
				writeS(cmd.cmd); // command name
			}
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_rev); // macro change revision (changes after each macro edition)
		writeC(0); //unknown
		writeC(_count); //count of Macros
		writeC(_macro != null ? 1 : 0); //unknown

		if(_macro != null)
		{
			writeD(_macro.id); //Macro ID
			writeS(_macro.name); //Macro Name
			writeS(_macro.descr); //Desc
			writeS(_macro.acronym); //acronym
			writeC(_macro.icon); //icon

			writeC(_macro.commands.length); //count

			for(int i = 0; i < _macro.commands.length; i++)
			{
				L2Macro.L2MacroCmd cmd = _macro.commands[i];
				writeC(i + 1); //i of count
				writeC(cmd.type); //type  1 = skill, 3 = action, 4 = shortcut
				writeD(cmd.d1); // skill id
				writeC(cmd.d2); // shortcut id
				writeS(cmd.cmd); // command name
			}
		}
	}
}
