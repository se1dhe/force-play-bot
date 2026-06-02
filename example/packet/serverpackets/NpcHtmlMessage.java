package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.handler.INpcHtmlAppendHandler;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.scripts.Scripts;
import l2p.gameserver.scripts.Scripts.ScriptClassAndMethod;
import l2p.gameserver.utils.HtmlUtils;
import l2p.gameserver.utils.Strings;
import l2p.gameserver.utils.velocity.VelocityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the HTML parser in the client knowns these standard and non-standard tags and attributes
 * VOLUMN
 * UNKNOWN
 * UL
 * U
 * TT
 * TR
 * TITLE
 * TEXTCODE
 * TEXTAREA
 * TD
 * TABLE
 * SUP
 * SUB
 * STRIKE
 * SPIN
 * SELECT
 * RIGHT
 * PRE
 * P
 * OPTION
 * OL
 * MULTIEDIT
 * LI
 * LEFT
 * INPUT
 * IMG
 * I
 * HTML
 * H7
 * H6
 * H5
 * H4
 * H3
 * H2
 * H1
 * FONT
 * EXTEND
 * EDIT
 * COMMENT
 * COMBOBOX
 * CENTER
 * BUTTON
 * BR
 * BODY
 * BAR
 * ADDRESS
 * A
 * SEL
 * LIST
 * VAR
 * FORE
 * READONL
 * ROWS
 * VALIGN
 * FIXWIDTH
 * BORDERCOLORLI
 * BORDERCOLORDA
 * BORDERCOLOR
 * BORDER
 * BGCOLOR
 * BACKGROUND
 * ALIGN
 * VALU
 * READONLY
 * MULTIPLE
 * SELECTED
 * TYP
 * TYPE
 * MAXLENGTH
 * CHECKED
 * SRC
 * Y
 * X
 * QUERYDELAY
 * NOSCROLLBAR
 * IMGSRC
 * B
 * FG
 * SIZE
 * FACE
 * COLOR
 * DEFFON
 * DEFFIXEDFONT
 * WIDTH
 * VALUE
 * TOOLTIP
 * NAME
 * MIN
 * MAX
 * HEIGHT
 * DISABLED
 * ALIGN
 * MSG
 * LINK
 * HREF
 * ACTION
 */
public class NpcHtmlMessage extends L2GameServerPacket
{
	protected static final Logger _log = LoggerFactory.getLogger(NpcHtmlMessage.class);

	protected int _npcObjId;
	protected String _html;
	protected String _file = null;
	protected List<String> _replaces = new ArrayList<String>();
	private Map<String, Object> _variables;
	protected boolean have_appends = false;

	public NpcHtmlMessage(L2Player player, L2NpcInstance npc, String filename, int val)
	{
		_npcObjId = npc.getObjectId();

		player.setLastNpc(npc);

		List<ScriptClassAndMethod> appends = Scripts.dialogAppends.get(npc.getNpcId());
		List<INpcHtmlAppendHandler> appendHandlers = Scripts.npcHtmlAppends.get(npc.getNpcId());
		if(appends != null && appends.size() > 0 || appendHandlers != null && !appendHandlers.isEmpty())
		{
			have_appends = true;
			if(filename != null && filename.equalsIgnoreCase("npcdefault.htm"))
				setHtml(""); // контент задается скриптами через DialogAppend_
			else
				setFile(filename);

			String replaces = "";

			// Добавить в конец странички текст, определенный в скриптах.
			Object[] script_args = new Object[] { new Integer(val) };
			if(appends != null)
			{
				for(ScriptClassAndMethod append : appends)
				{
					Object obj = Scripts.getInstance().callScripts(player, append.className, append.methodName, script_args);
					if(obj != null)
						replaces += obj;
				}
			}

			if(appendHandlers != null)
			{
				for(INpcHtmlAppendHandler handler : appendHandlers)
					replaces += handler.getAppend(player, npc.getNpcId(), val);
			}

			if(!replaces.equals(""))
				replace("</body>", "\n" + Strings.bbParse(replaces) + "</body>");
		}
		else
			setFile(filename);

		replace("%npcId%", String.valueOf(npc.getNpcId()));
		replace("%npcname%", npc.getName());
		replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStr());
	}

	public NpcHtmlMessage(L2Player player, L2NpcInstance npc)
	{
		if(npc == null)
		{
			_npcObjId = 5;
			player.setLastNpc(null);
		}
		else
		{
			_npcObjId = npc.getObjectId();
			player.setLastNpc(npc);
		}
	}

	public NpcHtmlMessage(int npcObjId)
	{
		_npcObjId = npcObjId;
	}

	public final NpcHtmlMessage setHtml(String text)
	{
		if(!text.contains("<html>"))
			text = "<html><body>" + text + "</body></html>"; //<title>Message:</title> <br><br><br>
		_html = text;
		return this;
	}

	public final NpcHtmlMessage setFile(String file)
	{
		_file = file;
		return this;
	}

	public NpcHtmlMessage replace(String pattern, String value)
	{
		if(pattern == null || value == null)
			return this;
		_replaces.add(pattern);
		_replaces.add(value);
		return this;
	}

	public NpcHtmlMessage addVar(String name, Object value)
	{
		if(name == null)
			throw new IllegalArgumentException("Name can't be null!");
		if(value == null)
			throw new IllegalArgumentException("Value can't be null!");
		if(name.startsWith("${"))
			throw new IllegalArgumentException("Incorrect name: " + name);
		if(_variables == null)
			_variables = new HashMap<>(2);
		_variables.put(name, value);
		return this;
	}

	public Map<String, Object> getVariables()
	{
		return _variables;
	}

	private static final Pattern objectId = Pattern.compile("%objectId%");
	private static final Pattern playername = Pattern.compile("%playername%");

	public void processHtml(L2GameClient client)
	{
		L2Player player = client.getActiveChar();
		if(player == null)
			return;

		if(_file != null) //TODO может быть не очень хорошо сдесь это делать...
		{
			if((Config.GM_HTML) && (player.isGM()))
				player.sendMessage("HTML: " + _file);
			String content = HtmCache.getInstance().getNotNull(_file, player);
			content = VelocityUtils.evaluate(content, _variables);
			String content2 = HtmCache.getInstance().getNullable(_file, player);
			content2 = VelocityUtils.evaluate(content2, _variables);
			if(content2 == null)
				setHtml(have_appends && _file.endsWith(".htm") ? "" : content);
			else
				setHtml(content);
		}

		for(int i = 0; i < _replaces.size(); i += 2)
			_html = _html.replace(_replaces.get(i), _replaces.get(i + 1));

		if(_html == null)
		{
			L2NpcInstance npc = L2ObjectsStorage.getNpc(_npcObjId);
			_log.warn("NpcHtmlMessage, _html == null, npc: " + npc + ", file: " + _file);
			return;
		}

		Matcher m = objectId.matcher(_html);
		if(m != null)
			_html = m.replaceAll(String.valueOf(_npcObjId));

		_html = playername.matcher(_html).replaceAll(player.getName());

		if(!player.isITClient())
			_html = HtmlUtils.switchButtons(_html);

		_html = otherReplace(player, _html);

		player.getBypassStorage().parseHtml(_html, false);

		if(_html.length() > 8192)
			_html = "<html><body><center>Sorry, to long html.</center></body></html>";
	}

	private String otherReplace(L2Player activeChar, String dialog)
	{
		dialog = dialog.replace("%active_zone%", activeChar.getActiveZoneText(false));
		dialog = dialog.replace("%oly_end%", activeChar.getOlyEndText(false));
		dialog = dialog.replace("%nominate_time%", activeChar.getOlyValidateEndText(false));
		dialog = dialog.replace("%prem_end%", activeChar.getPremEndText(false));
		dialog = dialog.replace("%prem_buff_end%", activeChar.getPremBuffEndText(false));
		dialog = dialog.replace("%vip_end%", activeChar.getVipEndText(false));
		dialog = dialog.replace("%hero_end%", activeChar.getHeroEndText(false));
		for(int itemId : Config.ALT_RUNE_IDS_INFO)
			dialog = dialog.replace("%temporary_item_" + itemId + "%", activeChar.getTemporaryItemText(false, itemId));
		return dialog;
	}

	@Override
	protected void writeImpl()
	{
		if(_html != null)
		{
			writeD(_npcObjId);
			writeS(_html);
			writeD(0x00);
		}
	}
}