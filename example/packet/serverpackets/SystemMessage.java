package l2p.gameserver.serverpackets;

import l2p.commons.util.GCArray;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.tables.SkillTree;

public class SystemMessage extends L2GameServerPacket
{
	// d d (d S/d d/d dd)
	// |--------------> 0 - String 1-number 2-textref npcname (1000000-1002655) 3-textref itemname 4-textref skills 5-??
	private static final int TYPE_HP_CHANGE = 16;
	private static final int TYPE_ZONE_NAME = 7;
	private static final int TYPE_LONG = 6;
	private static final int TYPE_SKILL_NAME = 4;
	private static final int TYPE_ITEM_NAME = 3;
	private static final int TYPE_NPC_NAME = 2;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_TEXT = 0;
	private int _messageId;
	private GCArray<Arg> args = new GCArray<Arg>();

	public static final int YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER = 0;
	public static final int THE_SERVER_WILL_BE_DISCONNECTED_IN_S1_SECONDS_PLEASE_EXIT = 1;
	public static final int S1_DOES_NOT_EXIST = 2;
	public static final int S1_IS_NOT_LOGGED_IN = 3;
	public static final int YOU_CANNOT_ASK_YOURSELF_TO_APPLY_TO_A_CLAN = 4;
	public static final int S1_ALREADY_EXISTS = 5;
	public static final int YOU_ALREADY_BELONG_TO_S1 = 7;
	public static final int YOU_ARE_WORKING_WITH_ANOTHER_CLAN = 8;
	public static final int S1_IS_NOT_A_CLAN_LEADER = 9;
	public static final int S1_IS_WORKING_WITH_ANOTHER_CLAN = 10;
	public static final int THERE_ARE_NO_APPLICANTS_FOR_THIS_CLAN = 11;
	public static final int APPLICANT_INFORMATION_IS_INCORRECT = 12;
	public static final int UNABLE_TO_DISPERSE_YOUR_CLAN_HAS_REQUESTED_TO_PARTICIPATE_IN_A_CASTLE_SIEGE = 13;
	public static final int UNABLE_TO_DISPERSE_YOUR_CLAN_OWNS_ONE_OR_MORE_CASTLES_OR_HIDEOUTS = 14;
	public static final int YOU_ARE_IN_SIEGE = 15;
	public static final int YOU_ARE_NOT_IN_SIEGE = 16;
	public static final int CASTLE_SIEGE_HAS_BEGUN = 17;
	public static final int CASTLE_SIEGE_IS_OVER = 18;
	public static final int THE_CASTELLAN_HAS_BEEN_CHANGED = 19;
	public static final int THE_GATE_IS_BEING_OPENED = 20;
	public static final int THE_GATE_IS_BEING_DESTROYED = 21;
	public static final int YOUR_TARGET_IS_OUT_OF_RANGE = 22;
	public static final int NOT_ENOUGH_HP = 23;
	public static final int NOT_ENOUGH_MP = 24;
	public static final int REJUVENATING_HP = 25;
	public static final int REJUVENATING_MP = 26;
	public static final int CASTING_HAS_BEEN_INTERRUPTED = 27;
	public static final int YOU_HAVE_OBTAINED_S1_ADENA = 28;
	public static final int YOU_HAVE_OBTAINED_S2_S1 = 29;
	public static final int YOU_HAVE_OBTAINED_S1 = 30;
	public static final int YOU_CANNOT_MOVE_WHILE_SITTING = 31;
	public static final int YOU_ARE_NOT_CAPABLE_OF_COMBAT_MOVE_TO_THE_NEAREST_RESTART_POINT = 32;
	public static final int YOU_CANNOT_MOVE_WHEN_USING_MAGIC = 33;
	public static final int WELCOME_TO_THE_WORLD_OF_LINEAGE_II = 34;
	public static final int YOU_HIT_FOR_S1_DAMAGE = 35;
	public static final int S1_HIT_YOU_FOR_S2_DAMAGE = 36;
	// 36 public static final int S1_HIT_YOU_FOR_S2_DAMAGE = 37;
	public static final int THE_TGS2002_EVENT_BEGINS = 38;
	public static final int THE_TGS2002_EVENT_IS_OVER_THANK_YOU_VERY_MUCH = 39;
	public static final int THIS_IS_THE_TGS_DEMO_THE_CHARACTER_WILL_IMMEDIATELY_BE_RESTORED = 40;
	public static final int GETTING_READY_TO_SHOOT_ARROWS = 41;
	public static final int AVOIDED_S1S_ATTACK = 42;
	public static final int MISSED_TARGET = 43;
	public static final int CRITICAL_HIT = 44;
	public static final int YOU_HAVE_EARNED_S1_EXPERIENCE = 45;
	public static final int USE_S1 = 46;
	public static final int USING_S1 = 47;
	public static final int S1_IS_NOT_AVAILABLE_AT_THIS_TIME_BEING_PREPARED_FOR_REUSE = 48;
	public static final int YOU_HAVE_EQUIPPED_YOUR_S1 = 49;
	public static final int TARGET_CAN_NOT_BE_FOUND = 50;
	public static final int YOU_CANNOT_USE_THIS_ON_YOURSELF = 51;
	public static final int EARNED_S1_ADENA = 52;
	public static final int EARNED_S2_S1_s = 53;
	public static final int EARNED_S1 = 54;
	public static final int FAILED_TO_PICK_UP_S1_ADENA = 55;
	public static final int FAILED_TO_PICK_UP_S1 = 56;
	public static final int FAILED_TO_PICK_UP_S2_S1_S = 57;
	public static final int FAILED_TO_EARN_S1_ADENA = 58;
	public static final int FAILED_TO_EARN_S1 = 59;
	public static final int FAILED_TO_EARN_S2_S1_S = 60;
	public static final int NOTHING_HAPPENED = 61;
	public static final int S1_HAS_BEEN_SUCCESSFULLY_ENCHANTED = 62;
	public static final int _S1_S2_HAS_BEEN_SUCCESSFULLY_ENCHANTED = 63;
	public static final int THE_ENCHANTMENT_HAS_FAILED_YOUR_S1_HAS_BEEN_CRYSTALLIZED = 64;
	public static final int THE_ENCHANTMENT_HAS_FAILED_YOUR__S1_S2_HAS_BEEN_CRYSTALLIZED = 65;
	public static final int S1_HAS_INVITED_YOU_TO_HIS_HER_PARTY_DO_YOU_ACCEPT_THE_INVITATION = 66;
	public static final int S1_HAS_INVITED_YOU_TO_THE_S2_CLAN_DO_YOU_WANT_TO_JOIN = 67;
	public static final int WITHDRAW_FROM_THE_S1_CLAN_DO_YOU_WANT_TO_CONTINUE = 68;
	public static final int EXPEL_S1_FROM_THE_CLAN_DO_YOU_WANT_TO_CONTINUE = 69;
	public static final int DISPERSE_THE_S1_CLAN_DO_YOU_WANT_TO_CONTINUE = 70;
	public static final int HOW_MANY_S1_S_DO_YOU_WANT_TO_DISCARD = 71;
	public static final int HOW_MANY_S1_S_DO_YOU_WANT_TO_MOVE = 72;
	public static final int HOW_MANY_S1_S_DO_YOU_WANT_TO_DESTROY = 73;
	public static final int DESTROY_S1_DO_YOU_WANT_TO_CONTINUE = 74;
	public static final int ID_DOES_NOT_EXIST = 75;
	public static final int INCORRECT_PASSWORD = 76;
	public static final int YOU_CANNOT_CREATE_ANOTHER_CHARACTER_PLEASE_DELETE_THE_EXISTING_CHARACTER_AND_TRY_AGAIN = 77;
	public static final int DO_YOU_WANT_TO_DELETE_S1 = 78;
	public static final int NAME_ALREADY_EXISTS = 79;
	public static final int YOUR_TITLE_CANNOT_EXCEED_16_CHARACTERS_IN_LENGHT = 80;
	public static final int PLEASE_SELECT_YOUR_RACE = 81;
	public static final int PLEASE_SELECT_YOUR_OCCUPATION = 82;
	public static final int PLEASE_SELECT_YOUR_GENDER = 83;
	public static final int YOU_CANNOT_ATTACK_IN_THE_PEACE_ZONE = 84;
	public static final int YOU_CANNOT_ATTACK_THE_TARGET_IN_THE_PEACE_ZONE = 85;
	public static final int PLEASE_INSERT_YOUR_ID = 86;
	public static final int PLEASE_INSERT_YOUR_PASSWORD = 87;
	public static final int PROTOCOL_VERSION_IS_DIFFERENT_PLEASE_QUIT_THE_PROGRAM = 88;
	public static final int PROTOCOL_VERSION_IS_DIFFERENT_PLEASE_CONTINUE = 89;
	public static final int UNABLE_TO_CONNECT_TO_SERVER = 90;
	public static final int PLEASE_SELECT_YOUR_HAIRSTYLE = 91;
	public static final int THE_EFFECT_OF_S1_HAS_WORN_OFF = 92;
	public static final int NOT_ENOUGH_SP = 93;
	public static final int _2002_2005_COPYRIGHT_NCSOFT_CORPORATION_ALL_RIGHTS_RESERVED = 94;
	public static final int YOU_EARNED_S1_EXP_AND_S2_SP = 95;
	public static final int YOU_HAVE_INCREASED_YOUR_LEVEL = 96;
	public static final int THIS_ITEM_CANNOT_BE_MOVED = 97;
	public static final int THIS_ITEM_CANNOT_BE_DISCARDED = 98;
	public static final int THIS_ITEM_CANNOT_BE_TRADED_OR_SOLD = 99;
	public static final int S1_REQUESTS_A_TRADE_DO_YOU_WANT_TO_TRADE = 100;
	public static final int YOU_CANNOT_LOGOUT_WHILE_IN_COMBAT = 101;
	public static final int YOU_CANNOT_RESTART_WHILE_IN_COMBAT = 102;
	public static final int ID_IS_LOGGED_IN = 103;
	public static final int YOU_CANNOT_USE_EQUIPMENT_WHEN_USING_OTHER_SKILLS_OR_MAGIC = 104;
	public static final int YOU_HAVE_INVITED_S1_TO_YOUR_PARTY = 105;
	public static final int YOU_HAVE_JOINED_S1S_PARTY = 106;
	public static final int S1_HAS_JOINED_THE_PARTY = 107;
	public static final int S1_HAS_LEFT_THE_PARTY = 108;
	public static final int INCORRECT_TARGET = 109;
	public static final int YOU_CAN_FEEL_S1S_EFFECT = 110;
	public static final int SHIELD_DEFENSE_HAS_SUCCEEDED = 111;
	public static final int NOT_ENOUGH_ARROWS = 112;
	public static final int S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS = 113;
	public static final int ENTER_THE_SHADOW_OF_THE_WORLD_TREE = 114;
	public static final int EXIT_THE_SHADOW_OF_THE_WORLD_TREE = 115;
	public static final int ENTERING_THE_PEACE_ZONE = 116;
	public static final int EXITING_THE_PEACE_ZONE = 117;
	public static final int REQUEST_S1_FOR_TRADE = 118;
	public static final int S1_DENIED_YOUR_REQUEST_FOR_TRADE = 119;
	public static final int BEGIN_TRADING_WITH_S1 = 120;
	public static final int S1_CONFIRMED_TRADE = 121;
	public static final int YOU_CANNOT_MOVE_ADDITIONAL_ITEMS_BECAUSE_TRADE_HAS_BEEN_CONFIRMED = 122;
	public static final int TRADE_HAS_BEEN_SUCCESSFUL = 123;
	public static final int S1_CANCELED_THE_TRADE = 124;
	public static final int QUIT_GAME_DO_YOU_WANT_TO_CONTINUE = 125;
	public static final int RESTART_THE_GAME_DO_YOU_WANT_TO_CONTINUE = 126;
	public static final int YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER_PLEASE_LOGIN_AGAIN = 127;
	public static final int YOU_HAVE_FAILED_TO_CREATE_A_CHARACTER = 128;
	public static final int YOUR_INVENTORY_IS_FULL = 129;
	public static final int YOUR_WAREHOUSE_IS_FULL = 130;
	public static final int S1_HAS_LOGGED_IN = 131;
	public static final int S1_HAS_BEEN_ADDED_TO_YOUR_FRIEND_LIST = 132;
	public static final int S1_HAS_BEEN_REMOVED_FROM_YOUR_FRIEND_LIST = 133;
	public static final int PLEASE_CHECK_YOUR_FRIEND_LIST_AGAIN = 134;
	public static final int S1_DID_NOT_REPLY_TO_YOUR_INVITATION_PARTY_INVITATION_HAS_BEEN_CANCELLED = 135;
	public static final int YOU_DID_NOT_REPLY_TO_S1S_INVITATION_JOINING_HAS_BEEN_CANCELLED = 136;
	public static final int THERE_ARE_NO_MORE_ITEMS_IN_THE_SHORTCUT = 137;
	public static final int DESIGNATE_SHORTCUT = 138;
	public static final int S1_WAS_UNAFFECTED_BY_S2 = 139;
	public static final int SKILL_WAS_REMOVED_DUE_TO_LACK_OF_MP = 140;
	public static final int IF_TRADE_IS_CONFIRMED_THE_ITEM_CANNOT_BE_MOVED_AGAIN = 141;
	public static final int ALREADY_TRADING = 142;
	public static final int S1_IS_TRADING_WITH_ANOTHER_PERSON = 143;
	public static final int TARGET_IS_INCORRECT = 144;
	public static final int TARGET_IS_NOT_FOUND_IN_THE_GAME = 145;
	public static final int CHATTING_IS_PERMITTED = 146;
	public static final int CHATTING_IS_PROHIBITED = 147;
	public static final int YOU_CANNOT_USE_QUEST_ITEMS = 148;
	public static final int YOU_CANNOT_PICK_UP_OR_USE_ITEMS_WHILE_TRADING = 149;
	public static final int YOU_CANNOT_DISCARD_OR_DESTROY_ITEMS_WHILE_TRADING = 150;
	public static final int TOO_FAR_TO_DISCARD = 151;
	public static final int YOU_HAVE_INVITED_WRONG_TARGET = 152;
	public static final int S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER = 153;
	public static final int ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS = 154;
	public static final int PARTY_IS_FULL = 155;
	public static final int DRAIN_WAS_ONLY_HALF_SUCCESSFUL = 156;
	public static final int YOU_RESISTED_S1S_DRAIN = 157;
	public static final int ATTACK_FAILED = 158;
	public static final int RESISTED_AGAINST_S1S_MAGIC = 159;
	public static final int S1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED = 160;
	public static final int THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE = 161;
	public static final int WAREHOUSE_IS_TOO_FAR = 162;
	public static final int YOU_CANNOT_DESTROY_IT_BECAUSE_THE_NUMBER_IS_INCORRECT = 163;
	public static final int WAITING_FOR_ANOTHER_REPLY = 164;
	public static final int YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST = 165;
	public static final int FRIEND_LIST_IS_NOT_READY_YET_PLEASE_REGISTER_AGAIN_LATER = 166;
	public static final int S1_IS_ALREADY_ON_YOUR_FRIEND_LIST = 167;
	public static final int S1_HAS_REQUESTED_TO_BECOME_FRIENDS = 168;
	public static final int ACCEPT_FRIENDSHIP_0_1__1_TO_ACCEPT_0_TO_DENY = 169;
	public static final int THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME = 170;
	public static final int S1_IS_NOT_ON_YOUR_FRIEND_LIST = 171;
	public static final int YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION = 172;
	// 173 копия 172
	// public static final int YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION = 173;
	public static final int THE_PERSONS_INVENTORY_IS_FULL = 174;
	public static final int HP_WAS_FULLY_RECOVERED_AND_SKILL_WAS_REMOVED = 175;
	public static final int THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE = 176;
	public static final int MESSAGE_REFUSAL_MODE = 177;
	public static final int MESSAGE_ACCEPTANCE_MODE = 178;
	public static final int YOU_CANNOT_DISCARD_ITEMS_HERE = 179;
	public static final int YOU_HAVE_S1_DAY_S_LEFT_UNTIL_DELETION_DO_YOU_WANT_TO_CANCEL_DELETION = 180;
	public static final int CANNOT_SEE_TARGET = 181;
	public static final int DO_YOU_WANT_TO_QUIT_THE_CURRENT_QUEST = 182;
	public static final int THERE_ARE_TOO_MANY_USERS_ON_THE_SERVER_PLEASE_TRY_AGAIN_LATER = 183;
	public static final int PLEASE_TRY_AGAIN_LATER = 184;
	public static final int SELECT_USER_TO_INVITE_TO_YOUR_PARTY = 185;
	public static final int SELECT_USER_TO_INVITE_TO_YOUR_CLAN = 186;
	public static final int SELECT_USER_TO_EXPEL = 187;
	public static final int CREATE_CLAN_NAME = 188;
	public static final int CLAN_HAS_BEEN_CREATED = 189;
	public static final int YOU_HAVE_FAILED_TO_CREATE_A_CLAN = 190;
	public static final int CLAN_MEMBER_S1_HAS_BEEN_EXPELLED = 191;
	public static final int YOU_HAVE_FAILED_TO_EXPEL_S1_FROM_THE_CLAN = 192;
	public static final int CLAN_HAS_DISPERSED = 193;
	public static final int YOU_HAVE_FAILED_TO_DISPERSE_THE_CLAN = 194;
	public static final int ENTERED_THE_CLAN = 195;
	public static final int S1_REFUSED_TO_JOIN_THE_CLAN = 196;
	public static final int WITHDRAWN_FROM_THE_CLAN = 197;
	public static final int YOU_HAVE_FAILED_TO_WITHDRAW_FROM_THE_S1_CLAN = 198;
	public static final int YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS = 199;
	public static final int YOU_HAVE_WITHDRAWN_FROM_THE_PARTY = 200;
	public static final int S1_WAS_EXPELLED_FROM_THE_PARTY = 201;
	public static final int YOU_HAVE_BEEN_EXPELLED_FROM_THE_PARTY = 202;
	public static final int THE_PARTY_HAS_DISPERSED = 203;
	public static final int INCORRECT_NAME_PLEASE_TRY_AGAIN = 204;
	public static final int INCORRECT_CHARACTER_NAME_PLEASE_ASK_THE_GM = 205;
	public static final int ENTER_NAME_OF_CLAN_TO_DECLARE_WAR_ON = 206;
	public static final int S2_OF_THE_S1_CLAN_REQUESTS_DECLARATION_OF_WAR_DO_YOU_ACCEPT = 207;
	public static final int PLEASE_INCLUDE_FILE_TYPE_WHEN_ENTERING_FILE_PATH = 208;
	public static final int THE_SIZE_OF_THE_IMAGE_FILE_IS_DIFFERENT_PLEASE_ADJUST_TO_16_12 = 209;
	public static final int CANNOT_FIND_FILE_PLEASE_ENTER_PRECISE_PATH = 210;
	public static final int CAN_ONLY_REGISTER_16_12_SIZED_BMP_FILES_OF_256_COLORS = 211;
	public static final int YOU_ARE_NOT_A_CLAN_MEMBER = 212;
	public static final int NOT_WORKING_PLEASE_TRY_AGAIN_LATER = 213;
	public static final int TITLE_HAS_CHANGED = 214;
	public static final int WAR_WITH_THE_S1_CLAN_HAS_BEGUN = 215;
	public static final int WAR_WITH_THE_S1_CLAN_HAS_ENDED = 216;
	public static final int YOU_HAVE_WON_THE_WAR_OVER_THE_S1_CLAN = 217;
	public static final int YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN = 218;
	public static final int YOUR_CLAN_LEADER_HAS_DIEDYOU_HAVE_BEEN_DEFEATED_BY_THE_S1_CLAN = 219;
	public static final int YOU_HAVE_S1_MINUTES_LEFT_UNTIL_THE_CLAN_WAR_ENDS = 220;
	public static final int THE_TIME_LIMIT_FOR_THE_CLAN_WAR_IS_UPWAR_WITH_THE_S1_CLAN_IS_OVER = 221;
	public static final int S1_HAS_JOINED_THE_CLAN = 222;
	public static final int S1_HAS_WITHDRAWN_FROM_THE_CLAN = 223;
	public static final int S1_DID_NOT_RESPOND_INVITATION_TO_THE_CLAN_HAS_BEEN_CANCELLED = 224;
	public static final int YOU_DIDNT_RESPOND_TO_S1S_INVITATION_JOINING_HAS_BEEN_CANCELLED = 225;
	public static final int THE_S1_CLAN_DID_NOT_RESPOND_WAR_PROCLAMATION_HAS_BEEN_REFUSED = 226;
	public static final int CLAN_WAR_HAS_BEEN_REFUSED_BECAUSE_YOU_DID_NOT_RESPOND_TO_S1_CLANS_WAR_PROCLAMATION = 227;
	public static final int REQUEST_TO_END_WAR_HAS_BEEN_DENIED = 228;
	public static final int YOU_ARE_NOT_QUALIFIED_TO_CREATE_A_CLAN = 229;
	public static final int YOU_MUST_WAIT_10_DAYS_BEFORE_CREATING_A_NEW_CLAN = 230;
	public static final int AFTER_A_CLAN_MEMBER_IS_DISMISSED_FROM_A_CLAN_THE_CLAN_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_ACCEPTING_A_NEW_MEMBER = 231;
	public static final int AFTER_LEAVING_OR_HAVING_BEEN_DISMISSED_FROM_A_CLAN_YOU_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_JOINING_ANOTHER_CLAN = 232;
	public static final int THE_ACADEMY_ROYAL_GUARD_ORDER_OF_KNIGHTS_IS_FULL_AND_CANNOT_ACCEPT_NEW_MEMBERS_AT_THIS_TIME = 233;
	public static final int THE_TARGET_MUST_BE_A_CLAN_MEMBER = 234;
	public static final int YOU_CANNOT_TRANSFER_YOUR_RIGHTS = 235;
	public static final int ONLY_THE_CLAN_LEADER_IS_ENABLED = 236;
	public static final int CANNOT_FIND_CLAN_LEADER = 237;
	public static final int NOT_JOINED_IN_ANY_CLAN = 238;
	public static final int THE_CLAN_LEADER_CANNOT_WITHDRAW = 239;
	public static final int CURRENTLY_INVOLVED_IN_CLAN_WAR = 240;
	public static final int LEADER_OF_THE_S1_CLAN_IS_NOT_LOGGED_IN = 241;
	public static final int SELECT_TARGET = 242;
	public static final int CANNOT_PROCLAIM_WAR_ON_ALLIED_CLANS = 243;
	public static final int UNQUALIFIED_TO_REQUEST_DECLARATION_OF_CLAN_WAR = 244;
	public static final int _5_DAYS_HAS_NOT_PASSED_SINCE_YOU_WERE_REFUSED_WAR_DO_YOU_WANT_TO_CONTINUE = 245;
	public static final int THE_OTHER_CLAN_IS_CURRENTLY_AT_WAR = 246;
	public static final int YOU_HAVE_ALREADY_BEEN_AT_WAR_WITH_THE_S1_CLAN_5_DAYS_MUST_PASS_BEFORE_YOU_CAN_PROCLAIM_WAR_AGAIN = 247;
	public static final int YOU_CANNOT_PROCLAIM_WAR_THE_S1_CLAN_DOES_NOT_HAVE_ENOUGH_MEMBERS = 248;
	public static final int DO_YOU_WISH_TO_SURRENDER_TO_THE_S1_CLAN = 249;
	public static final int YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN_YOU_ARE_LEAVING_THE_CLAN_WAR = 250;
	public static final int YOU_CANNOT_PROCLAIM_WAR_YOU_ARE_AT_WAR_WITH_ANOTHER_CLAN = 251;
	public static final int ENTER_THE_NAME_OF_CLAN_TO_SURRENDER_TO = 252;
	public static final int ENTER_THE_NAME_OF_CLAN_TO_REQUEST_END_OF_WAR = 253;
	public static final int CLAN_LEADER_CANNOT_SURRENDER_PERSONALLY = 254;
	public static final int THE_S1_CLAN_HAS_REQUESTED_TO_END_WAR_DO_YOU_AGREE = 255;
	public static final int ENTER_NAME = 256;
	public static final int DO_YOU_PROPOSE_TO_THE_S1_CLAN_TO_END_THE_WAR = 257;
	public static final int NOT_INVOLVED_IN_CLAN_WAR = 258;
	public static final int SELECT_CLAN_MEMBERS_FROM_LIST = 259;
	public static final int FAME_LEVEL_HAS_DECREASED_5_DAYS_HAVE_NOT_PASSED_SINCE_YOU_WERE_REFUSED_WAR = 260;
	public static final int CLAN_NAME_IS_INCORRECT = 261;
	public static final int CLAN_NAMES_LENGTH_IS_INCORRECT = 262;
	public static final int DISPERSION_HAS_ALREADY_BEEN_REQUESTED = 263;
	public static final int YOU_CANNOT_DISSOLVE_A_CLAN_WHILE_ENGAGED_IN_A_WAR = 264;
	public static final int YOU_CANNOT_DISSOLVE_A_CLAN_DURING_A_SIEGE_OR_WHILE_PROTECTING_A_CASTLE = 265;
	public static final int YOU_CANNOT_DISSOLVE_A_CLAN_WHILE_OWNING_A_CLAN_HALL_OR_CASTLE = 266;
	public static final int NO_REQUESTS_FOR_DISPERSION = 267;
	public static final int PLAYER_ALREADY_BELONGS_TO_A_CLAN = 268;
	public static final int YOU_CANNOT_EXPEL_YOURSELF = 269;
	public static final int YOU_HAVE_ALREADY_SURRENDERED = 270;
	public static final int TITLE_ENDOWMENT_IS_ONLY_POSSIBLE_WHEN_CLANS_SKILL_LEVELS_ARE_ABOVE_3 = 271;
	public static final int CLAN_CREST_REGISTRATION_IS_ONLY_POSSIBLE_WHEN_CLANS_SKILL_LEVELS_ARE_ABOVE_3 = 272;
	public static final int PROCLAMATION_OF_CLAN_WAR_IS_ONLY_POSSIBLE_WHEN_CLANS_SKILL_LEVELS_ARE_ABOVE_3 = 273;
	public static final int CLANS_SKILL_LEVEL_HAS_INCREASED = 274;
	public static final int CLAN_HAS_FAILED_TO_INCREASE_SKILL_LEVEL = 275;
	public static final int YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_LEARN_SKILLS = 276;
	public static final int YOU_HAVE_EARNED_S1 = 277;
	public static final int YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_SKILLS = 278;
	public static final int YOU_DO_NOT_HAVE_ENOUGH_ADENA = 279;
	public static final int YOU_DO_NOT_HAVE_ANY_ITEMS_TO_SELL = 280;
	public static final int YOU_DO_NOT_HAVE_ENOUGH_CUSTODY_FEES = 281;
	public static final int YOU_HAVE_NOT_DEPOSITED_ANY_ITEMS_IN_YOUR_WAREHOUSE = 282;
	public static final int YOU_HAVE_ENTERED_A_COMBAT_ZONE = 283;
	public static final int YOU_HAVE_LEFT_A_COMBAT_ZONE = 284;
	public static final int CLAN_S1_HAS_SUCCEEDED_IN_ENGRAVING_THE_RULER = 285;
	public static final int YOUR_BASE_IS_BEING_ATTACKED = 286;
	public static final int THE_OPPONENT_CLAN_HAS_BEGUN_TO_ENGRAVE_THE_RULER = 287;
	public static final int THE_CASTLE_GATE_HAS_BEEN_BROKEN_DOWN = 288;
	public static final int SINCE_A_HEADQUARTERS_ALREADY_EXISTS_YOU_CANNOT_BUILD_ANOTHER_ONE = 289;
	public static final int YOU_CANNOT_SET_UP_A_BASE_HERE = 290;
	public static final int CLAN_S1_IS_VICTORIOUS_OVER_S2S_CASTLE_SIEGE = 291;
	public static final int S1_HAS_ANNOUNCED_THE_CASTLE_SIEGE_TIME = 292;
	public static final int THE_REGISTRATION_TERM_FOR_S1_HAS_ENDED = 293;
	public static final int YOU_CANNOT_SUMMON_A_BASE_BECAUSE_YOU_ARE_NOT_IN_BATTLE = 294;
	public static final int S1S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED = 295;
	public static final int YOU_RECEIVED_S1_DAMAGE_FROM_TAKING_A_HIGH_FALL = 296;
	public static final int YOU_RECEIVED_S1_DAMAGE_BECAUSE_YOU_WERE_UNABLE_TO_BREATHE = 297;
	public static final int YOU_HAVE_DROPPED_S1 = 298;
	public static final int S1_HAS_OBTAINED_S3_S2 = 299;
	public static final int S1_HAS_OBTAINED_S2 = 300;
	public static final int S2_S1_HAS_DISAPPEARED = 301;
	public static final int S1_HAS_DISAPPEARED = 302;
	public static final int SELECT_ITEM_TO_ENCHANT = 303;
	public static final int CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME = 304;
	public static final int THE_PLAYER_DECLINED_TO_JOIN_YOUR_PARTY = 305;
	public static final int YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER = 306;
	public static final int YOU_HAVE_FAILED_TO_TRADE_WITH_THE_WAREHOUSE = 307;
	public static final int FAILED_TO_JOIN_THE_CLAN = 308;
	public static final int SUCCEEDED_IN_EXPELLING_A_CLAN_MEMBER = 309;
	public static final int FAILED_TO_EXPEL_A_CLAN_MEMBER = 310;
	public static final int CLAN_WAR_HAS_BEEN_ACCEPTED = 311;
	public static final int CLAN_WAR_HAS_BEEN_REFUSED = 312;
	public static final int THE_CEASE_WAR_REQUEST_HAS_BEEN_ACCEPTED = 313;
	public static final int FAILED_TO_SURRENDER = 314;
	public static final int FAILED_TO_PERSONALLY_SURRENDER = 315;
	public static final int FAILED_TO_WITHDRAW_FROM_THE_PARTY = 316;
	public static final int FAILED_TO_EXPEL_A_PARTY_MEMBER = 317;
	public static final int FAILED_TO_DISPERSE_THE_PARTY = 318;
	public static final int YOU_ARE_UNABLE_TO_UNLOCK_THE_DOOR = 319;
	public static final int YOU_HAVE_FAILED_TO_UNLOCK_THE_DOOR = 320;
	public static final int IT_IS_NOT_LOCKED = 321;
	public static final int PLEASE_DECIDE_ON_THE_SALES_PRICE = 322;
	public static final int YOUR_FORCE_HAS_INCREASED_TO_S1_LEVEL = 323;
	public static final int YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_ = 324;
	public static final int THE_CORPSE_HAS_ALREADY_DISAPPEARED = 325;
	public static final int SELECT_TARGET_FROM_LIST = 326;
	public static final int YOU_CANNOT_EXCEED_80_CHARACTERS = 327;
	public static final int PLEASE_INPUT_TITLE_USING_LESS_THAN_128_CHARACTERS = 328;
	public static final int PLEASE_INPUT_CONTENTS_USING_LESS_THAN_3000_CHARACTERS = 329;
	public static final int A_ONE_LINE_RESPONSE_MAY_NOT_EXCEED_128_CHARACTERS = 330;
	public static final int YOU_HAVE_ACQUIRED_S1_SP = 331;
	public static final int DO_YOU_WANT_TO_BE_RESTORED = 332;
	public static final int YOU_HAVE_RECEIVED_S1_DAMAGE_BY_CORES_BARRIER = 333;
	public static final int PLEASE_ENTER_STORE_MESSAGE = 334;
	public static final int S1_IS_ABORTED = 335;
	public static final int S1_IS_CRYSTALLIZED_DO_YOU_WANT_TO_CONTINUE = 336;
	public static final int SOULSHOT_DOES_NOT_MATCH_WEAPON_GRADE = 337;
	public static final int NOT_ENOUGH_SOULSHOTS = 338;
	public static final int CANNOT_USE_SOULSHOTS = 339;
	public static final int PRIVATE_STORE_UNDER_WAY = 340;
	public static final int NOT_ENOUGH_MATERIALS = 341;
	public static final int POWER_OF_THE_SPIRITS_ENABLED = 342;
	public static final int SWEEPER_FAILED_TARGET_NOT_SPOILED = 343;
	public static final int POWER_OF_THE_SPIRITS_DISABLED = 344;
	public static final int CHAT_ENABLED = 345;
	public static final int CHAT_DISABLED = 346;
	// 347 какого-то хрена не отображается в клиенте, 351 вместо него
	// public static final int INCORRECT_ITEM_COUNT = 347;
	public static final int INCORRECT_ITEM_PRICE = 348;
	public static final int PRIVATE_STORE_ALREADY_CLOSED = 349;
	public static final int ITEM_OUT_OF_STOCK = 350;
	public static final int INCORRECT_ITEM_COUNT = 351;
	public static final int INCORRECT_ITEM = 352;
	public static final int CANNOT_PURCHASE = 353;
	public static final int CANCEL_ENCHANT = 354;
	public static final int INAPPROPRIATE_ENCHANT_CONDITIONS = 355;
	public static final int REJECT_RESURRECTION = 356;
	public static final int ALREADY_SPOILED = 357;
	public static final int S1_HOUR_S_UNTIL_CASTLE_SIEGE_CONCLUSION = 358;
	public static final int S1_MINUTE_S_UNTIL_CASTLE_SIEGE_CONCLUSION = 359;
	public static final int CASTLE_SIEGE_S1_SECOND_S_LEFT = 360;
	public static final int OVER_HIT = 361;
	public static final int ACQUIRED_S1_BONUS_EXPERIENCE_THROUGH_OVER_HIT = 362;
	public static final int CHAT_AVAILABLE_TIME_S1_MINUTE = 363;
	public static final int ENTER_USERS_NAME_TO_SEARCH = 364;
	public static final int ARE_YOU_SURE = 365;
	public static final int SELECT_HAIR_COLOR = 366;
	public static final int CANNOT_REMOVE_CLAN_CHARACTER = 367;
	public static final int EQUIPPED__S1_S2 = 368;
	public static final int YOU_HAVE_OBTAINED__S1S2 = 369;
	// 56 public static final int FAILED_TO_PICK_UP_S1 = 370;
	public static final int ACQUIRED__S1_S2 = 371;
	// 59 public static final int FAILED_TO_EARN_S1 = 372;
	public static final int DESTROY__S1_S2_DO_YOU_WISH_TO_CONTINUE = 373;
	public static final int CRYSTALLIZE__S1_S2_DO_YOU_WISH_TO_CONTINUE = 374;
	public static final int DROPPED__S1_S2 = 375;
	public static final int S1_HAS_OBTAINED__S2S3 = 376;
	public static final int _S1_S2_DISAPPEARED = 377;
	public static final int S1_PURCHASED_S2 = 378;
	public static final int S1_PURCHASED__S2_S3 = 379;
	public static final int S1_PURCHASED_S3_S2_S = 380;
	public static final int CANNOT_CONNECT_TO_PETITION_SERVER = 381;
	public static final int CURRENTLY_THERE_ARE_NO_USERS_THAT_HAVE_CHECKED_OUT_A_GM_ID = 382;
	public static final int REQUEST_CONFIRMED_TO_END_CONSULTATION_AT_PETITION_SERVER = 383;
	public static final int THE_CLIENT_IS_NOT_LOGGED_ONTO_THE_GAME_SERVER = 384;
	public static final int REQUEST_CONFIRMED_TO_BEGIN_CONSULTATION_AT_PETITION_SERVER = 385;
	public static final int PETITION_REQUESTS_MUST_BE_OVER_FIVE_CHARACTERS = 386;
	public static final int ENDING_PETITION_CONSULTATION = 387;
	public static final int NOT_UNDER_PETITION_CONSULTATION = 388;
	public static final int PETITION_APPLICATION_ACCEPTED_RECEIPT_NO_IS_S1 = 389;
	public static final int ALREADY_APPLIED_FOR_PETITION = 390;
	public static final int RECEIPT_NO_S1_PETITION_CANCELLED = 391;
	public static final int UNDER_PETITION_ADVICE = 392;
	public static final int FAILED_TO_CANCEL_PETITION_PLEASE_TRY_AGAIN_LATER = 393;
	public static final int PETITION_CONSULTATION_WITH_S1_UNDER_WAY = 394;
	public static final int ENDING_PETITION_CONSULTATION_WITH_S1 = 395;
	public static final int PLEASE_LOGIN_AFTER_CHANGING_YOUR_TEMPORARY_PASSWORD = 396;
	public static final int NOT_A_PAID_ACCOUNT = 397;
	public static final int YOU_HAVE_NO_MORE_TIME_LEFT_ON_YOUR_ACCOUNT = 398;
	public static final int SYSTEM_ERROR = 399;
	public static final int DISCARD_S1_DO_YOU_WISH_TO_CONTINUE = 400;
	public static final int TOO_MANY_QUESTS_IN_PROGRESS = 401;
	public static final int YOU_MAY_NOT_GET_ON_BOARD_WITHOUT_A_PASS = 402;
	public static final int YOU_HAVE_EXCEEDED_YOUR_POCKET_MONEY_LIMIT = 403;
	public static final int CREATE_ITEM_LEVEL_IS_TOO_LOW_TO_REGISTER_THIS_RECIPE = 404;
	public static final int THE_TOTAL_PRICE_OF_THE_PRODUCT_IS_TOO_HIGH = 405;
	public static final int PETITION_APPLICATION_ACCEPTED = 406;
	public static final int PETITION_UNDER_PROCESS = 407;
	public static final int SET_PERIOD = 408;
	public static final int SET_TIME_S1_S2_S3 = 409;
	public static final int REGISTRATION_PERIOD = 410;
	public static final int REGISTRATION_TIME_S1_S2_S3 = 411;
	public static final int BATTLE_BEGINS_IN_S1_S2_S4 = 412;
	public static final int BATTLE_ENDS_IN_S1_S2_S5 = 413;
	public static final int STANDBY = 414;
	public static final int UNDER_SIEGE = 415;
	public static final int CANNOT_BE_EXCHANGED = 416;
	public static final int S1__HAS_BEEN_DISARMED = 417;
	public static final int THERE_IS_A_SIGNIFICANT_DIFFERENCE_BETWEEN_THE_ITEMS_PRICE_AND_ITS_STANDARD_PRICE_PLEASE_CHECK_AGAIN = 418;
	public static final int S1_MINUTE_S_OF_DESIGNATED_USAGE_TIME_LEFT = 419;
	public static final int TIME_EXPIRED = 420;
	public static final int ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT = 421;
	public static final int YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT = 422;
	public static final int THE_SCROLL_OF_ENCHANT_HAS_BEEN_CANCELED = 423;
	public static final int DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL = 424;
	public static final int YOUR_CREATE_ITEM_LEVEL_IS_TOO_LOW = 425;
	public static final int YOUR_ACCOUNT_HAS_BEEN_REPORTED_FOR_INTENTIONALLY_NOT_PAYING_THE_CYBER_CAFE_FEES = 426;
	public static final int PLEASE_CONTACT_US = 427;
	public static final int XXXXX = 429;

	// missed 430-447 (not needed)

	public static final int SYSTEM_ERROR_PLEASE_LOG_IN_AGAIN_LATER = 448;
	public static final int PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT = 449;
	public static final int CONFIRM_YOUR_ACCOUNT_INFORMATION_AND_LOG_IN_AGAIN_LATER = 450;
	public static final int THE_PASSWORD_YOU_HAVE_ENTERED_IS_INCORRECT = 451;
	public static final int PLEASE_CONFIRM_YOUR_ACCOUNT_INFORMATION_AND_TRY_LOGGING_IN_AGAIN = 452;
	public static final int YOUR_ACCOUNT_INFORMATION_IS_INCORRECT = 453;
	public static final int FOR_MORE_DETAILS_PLEASE_CONTACT_OUR_CUSTOMER_SERVICE_CENTER_AT_HTTP__SUPPORTPLAYNCCOM = 454;
	public static final int THE_ACCOUNT_IS_ALREADY_IN_USE_ACCESS_DENIED = 455;
	public static final int LINEAGE_II_GAME_SERVICES_MAY_BE_USED_BY_INDIVIDUALS_15_YEARS_OF_AGE_OR_OLDER_EXCEPT_FOR_PVP_SERVERS = 456;
	public static final int SERVER_UNDER_MAINTENANCE_PLEASE_TRY_AGAIN_LATER = 457;
	public static final int YOUR_USAGE_TERM_HAS_EXPIRED = 458;
	public static final int PLEASE_VISIT_THE_OFFICIAL_LINEAGE_II_WEBSITE_AT_HTTP__WWWLINEAGE2COM = 459;
	public static final int TO_REACTIVATE_YOUR_ACCOUNT = 460;
	public static final int ACCESS_FAILED = 461;
	// 184 public static final int PLEASE_TRY_AGAIN_LATER = 462;
	public static final int FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY = 464;
	public static final int YOU_ARE_NOT_CURRENTLY_ALLIED_WITH_ANY_CLANS = 465;
	public static final int YOU_HAVE_EXCEEDED_THE_LIMIT = 466;
	public static final int YOU_MAY_NOT_ACCEPT_ANY_CLAN_WITHIN_A_DAY_AFTER_EXPELLING_ANOTHER_CLAN = 467;
	public static final int A_CLAN_THAT_HAS_WITHDRAWN_OR_BEEN_EXPELLED_CANNOT_ENTER_INTO_AN_ALLIANCE_WITHIN_ONE_DAY_OF_WITHDRAWAL_OR_EXPULSION = 468;
	public static final int YOU_MAY_NOT_ALLY_WITH_A_CLAN_YOU_ARE_AT_BATTLE_WITH = 469;
	public static final int ONLY_THE_CLAN_LEADER_MAY_APPLY_FOR_WITHDRAWAL_FROM_THE_ALLIANCE = 470;
	public static final int ALLIANCE_LEADERS_CANNOT_WITHDRAW = 471;
	public static final int YOU_CANNOT_EXPEL_YOURSELF_FROM_THE_CLAN = 472;
	public static final int DIFFERENT_ALLIANCE = 473;
	public static final int THE_FOLLOWING_CLAN_DOES_NOT_EXIST = 474;
	public static final int INCORRECT_IMAGE_SIZE_PLEASE_ADJUST_TO_8X12 = 476;
	public static final int NO_RESPONSE_INVITATION_TO_JOIN_AN_ALLIANCE_HAS_BEEN_CANCELLED = 477;
	public static final int NO_RESPONSE_YOUR_ENTRANCE_TO_THE_ALLIANCE_HAS_BEEN_CANCELLED = 478;
	public static final int S1_HAS_JOINED_AS_A_FRIEND = 479;
	public static final int PLEASE_CHECK_YOUR_FRIENDS_LIST = 480;
	public static final int S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST = 481;
	public static final int FRIEND_LIST_IS_NOT_READY_YET_PLEASE_TRY_AGAIN_LATER = 483;
	public static final int ALREADY_REGISTERED_ON_THE_FRIENDS_LIST = 484;
	public static final int NO_NEW_FRIEND_INVITATIONS_FROM_OTHER_USERS = 485;
	public static final int THE_FOLLOWING_USER_IS_NOT_IN_YOUR_FRIENDS_LIST = 486;
	public static final int _FRIENDS_LIST_ = 487; // ======<FRIENDS_LIST>======
	public static final int S1_CURRENTLY_ONLINE = 488;
	public static final int S1_CURRENTLY_OFFLINE = 489;
	public static final int __EQUALS__ = 490; // ========================
	public static final int _ALLIANCE_INFORMATION_ = 491; // =======<ALLIANCE_INFORMATION>=======
	public static final int ALLIANCE_NAME_S1 = 492;
	public static final int CONNECTION_S1_TOTAL_S2 = 493;
	public static final int ALLIANCE_LEADER_S2_OF_S1 = 494;
	public static final int AFFILIATED_CLANS_TOTAL_S1_CLAN_S = 495;
	public static final int _CLAN_INFORMATION_ = 496; // =====<CLAN_INFORMATION>=====
	public static final int CLAN_NAME_S1 = 497;
	public static final int CLAN_LEADER_S1 = 498;
	public static final int CLAN_LEVEL_S1 = 499;
	public static final int __DASHES__ = 500; // ------------------------
	public static final int YOU_ALREADY_BELONG_TO_ANOTHER_ALLIANCE = 502;
	public static final int S1_FRIEND_HAS_LOGGED_IN = 503;
	public static final int ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES = 504;
	public static final int YOU_CANNOT_CREATE_A_NEW_ALLIANCE_WITHIN_10_DAYS_AFTER_DISSOLUTION = 505;
	public static final int INCORRECT_ALLIANCE_NAME = 506;
	public static final int INCORRECT_LENGTH_FOR_AN_ALLIANCE_NAME = 507;
	public static final int THIS_ALLIANCE_NAME_ALREADY_EXISTS = 508;
	public static final int CANNOT_ACCEPT_CLAN_ALLY_IS_REGISTERED_AS_AN_ENEMY_DURING_SIEGE_BATTLE = 509;
	public static final int YOU_HAVE_INVITED_SOMEONE_TO_YOUR_ALLIANCE = 510;
	public static final int SELECT_USER_TO_INVITE = 511;
	public static final int DO_YOU_REALLY_WISH_TO_WITHDRAW_FROM_THE_ALLIANCE = 512;
	public static final int ENTER_THE_NAME_OF_THE_CLAN_YOU_WISH_TO_EXPEL = 513;
	public static final int DO_YOU_REALLY_WISH_TO_DISSOLVE_THE_ALLIANCE = 514;
	public static final int ENTER_FILE_NAME_FOR_THE_ALLIANCE_CREST = 515;
	public static final int S1_HAS_INVITED_YOU_AS_A_FRIEND = 516;
	public static final int YOU_HAVE_ACCEPTED_THE_ALLIANCE = 517;
	public static final int YOU_HAVE_FAILED_TO_INVITE_A_CLAN_INTO_THE_ALLIANCE = 518;
	public static final int YOU_HAVE_WITHDRAWN_FROM_THE_ALLIANCE = 519;
	public static final int YOU_HAVE_FAILED_TO_WITHDRAW_FROM_THE_ALLIANCE = 520;
	public static final int YOU_HAVE_SUCCEEDED_IN_EXPELLING_A_CLAN = 521;
	public static final int YOU_HAVE_FAILED_TO_EXPEL_A_CLAN = 522;
	public static final int THE_ALLIANCE_HAS_BEEN_DISSOLVED = 523;
	public static final int YOU_HAVE_FAILED_TO_DISSOLVE_THE_ALLIANCE = 524;
	public static final int YOU_HAVE_SUCCEEDED_IN_INVITING_A_FRIEND = 525;
	public static final int YOU_HAVE_FAILED_TO_INVITE_A_FRIEND = 526;
	public static final int S2_THE_LEADER_OF_S1_HAS_REQUESTED_AN_ALLIANCE = 527;
	public static final int FILE_NOT_FOUND = 528;
	public static final int YOU_MAY_ONLY_REGISTER_8X12_BMP_FILES_WITH_256_COLORS = 529;
	public static final int SPIRITSHOT_DOES_NOT_MATCH_WEAPON_GRADE = 530;
	public static final int NOT_ENOUGH_SPIRITSHOTS = 531;
	public static final int CANNOT_USE_SPIRITSHOTS = 532;
	public static final int POWER_OF_MANA_ENABLED = 533;
	public static final int POWER_OF_MANA_DISABLED = 534;
	public static final int NAME_PET = 535;
	public static final int HOW_MUCH_ADENA_DO_YOU_WISH_TO_TRANSFER_TO_YOUR_INVENTORY = 536;
	public static final int HOW_MUCH_WILL_YOU_TRANSFER = 537;
	public static final int SP_HAS_DECREASED_BY_S1 = 538;
	public static final int EXPERIENCE_HAS_DECREASED_BY_S1 = 539;
	public static final int CLAN_LEADERS_CANNOT_BE_DELETED_DISSOLVE_THE_CLAN_AND_TRY_AGAIN = 540;
	public static final int YOU_CANNOT_DELETE_A_CLAN_MEMBER_WITHDRAW_FROM_THE_CLAN_AND_TRY_AGAIN = 541;
	public static final int NPC_SERVER_NOT_OPERATING_PETS_CANNOT_BE_SUMMONED = 542;
	public static final int YOU_ALREADY_HAVE_A_PET = 543;
	public static final int ITEM_NOT_AVAILABLE_FOR_PETS = 544;
	public static final int DUE_TO_THE_VOLUME_LIMIT_OF_THE_PETS_INVENTORY_NO_MORE_ITEMS_CAN_BE_PLACED_THERE = 545;
	public static final int EXCEEDED_PET_INVENTORYS_WEIGHT_LIMIT = 546;
	public static final int SUMMON_A_PET = 547;
	public static final int YOUR_PETS_NAME_CAN_BE_UP_TO_8_CHARACTERS = 548;
	public static final int TO_CREATE_AN_ALLIANCE_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER = 549;
	public static final int YOU_CANNOT_CREATE_AN_ALLIANCE_DURING_THE_TERM_OF_DISSOLUTION_POSTPONEMENT = 550;
	public static final int YOU_CANNOT_RAISE_YOUR_CLAN_LEVEL_DURING_THE_TERM_OF_DISPERSION_POSTPONEMENT = 551;
	public static final int DURING_THE_GRACE_PERIOD_FOR_DISSOLVING_A_CLAN_REGISTRATION_OR_DELETION_OF_A_CLANS_CREST_IS_NOT_ALLOWED = 552;
	public static final int THE_OPPOSING_CLAN_HAS_APPLIED_FOR_DISPERSION = 553;
	public static final int YOU_CANNOT_DISPERSE_THE_CLANS_IN_YOUR_ALLIANCE = 554;
	public static final int YOU_CANNOT_MOVE_YOUR_ITEM_WEIGHT_IS_TOO_GREAT = 555;
	public static final int YOU_CANNOT_MOVE_IN_THIS_STATE = 556;
	public static final int THE_PET_HAS_BEEN_SUMMONED_AND_CANNOT_BE_DELETED = 557;
	public static final int THE_PET_HAS_BEEN_SUMMONED_AND_CANNOT_BE_LET_GO = 558;
	public static final int PURCHASED_S2_FROM_S1 = 559;
	public static final int PURCHASED_S2_S3_FROM_S1 = 560;
	public static final int PURCHASED_S3_S2_S_FROM_S1_ = 561;
	public static final int CANNOT_CRYSTALLIZE_CRYSTALLIZATION_SKILL_LEVEL_TOO_LOW = 562;
	public static final int FAILED_TO_DISABLE_ATTACK_TARGET = 563;
	public static final int FAILED_TO_CHANGE_ATTACK_TARGET = 564;
	public static final int NOT_ENOUGH_LUCK = 565;
	public static final int CONFUSION_FAILED = 566;
	public static final int FEAR_FAILED = 567;
	public static final int CUBIC_SUMMONING_FAILED = 568;
	public static final int CAUTION_THE_ITEM_PRICE_GREATLY_DIFFERS_FROM_THE_SHOPS_STANDARD_PRICE_DO_YOU_WISH_TO_CONTINUE = 569;
	public static final int HOW_MANY__S1__S_DO_YOU_WISH_TO_PURCHASE = 570;
	public static final int HOW_MANY__S1__S_DO_YOU_WANT_TO_PURCHASE = 571;
	public static final int DO_YOU_WISH_TO_JOIN_S1S_PARTY_ITEM_DISTRIBUTION_FINDERS_KEEPERS = 572;
	public static final int DO_YOU_WISH_TO_JOIN_S1S_PARTY_ITEM_DISTRIBUTION_RANDOM = 573;
	public static final int PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME = 574;
	public static final int HOW_MUCH_ADENA_DO_YOU_WISH_TO_TRANSFER_TO_YOUR_PET = 575;
	public static final int HOW_MUCH_DO_YOU_WISH_TO_TRANSFER = 576;
	public static final int YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS = 577;
	public static final int YOU_CANNOT_SUMMON_DURING_COMBAT = 578;
	public static final int A_PET_CANNOT_BE_SENT_BACK_DURING_BATTLE = 579;
	public static final int YOU_MAY_NOT_USE_MULTIPLE_PETS_OR_SERVITORS_AT_THE_SAME_TIME = 580;
	public static final int THERE_IS_A_SPACE_IN_THE_NAME = 581;
	public static final int INAPPROPRIATE_CHARACTER_NAME = 582;
	public static final int NAME_INCLUDES_FORBIDDEN_WORDS = 583;
	public static final int ALREADY_IN_USE_BY_ANOTHER_PET = 584;
	public static final int PLEASE_DECIDE_ON_THE_PRICE = 585;
	public static final int PET_ITEMS_CANNOT_BE_REGISTERED_AS_SHORTCUTS = 586;
	public static final int IRREGULAR_SYSTEM_SPEED = 587;
	public static final int PET_INVENTORY_IS_FULL = 588;
	public static final int A_DEAD_PET_CANNOT_BE_SENT_BACK = 589;
	public static final int CANNOT_GIVE_ITEMS_TO_A_DEAD_PET = 590;
	public static final int AN_INVALID_CHARACTER_IS_INCLUDED_IN_THE_PETS_NAME = 591;
	public static final int DO_YOU_WISH_TO_DISMISS_YOUR_PET_DISMISSING_YOUR_PET_WILL_CAUSE_THE_PET_NECKLACE_TO_DISAPPEAR = 592;
	public static final int YOUR_PET_HAS_LEFT_DUE_TO_UNBEARABLE_HUNGER = 593;
	public static final int YOU_CANNOT_RESTORE_HUNGRY_PETS = 594;
	public static final int YOUR_PET_IS_VERY_HUNGRY = 595;
	public static final int YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY = 596;
	public static final int YOUR_PET_IS_VERY_HUNGRY_PLEASE_BE_CAREFUL = 597;
	public static final int YOU_CANNOT_CHAT_WHILE_YOU_ARE_INVISIBLE = 598;
	public static final int THE_GM_HAS_AN_IMPORTANT_NOTICE_CHAT_IS_TEMPORARILY_ABORTED = 599;
	public static final int YOU_CANNOT_EQUIP_A_PET_ITEM = 600;
	public static final int THERE_ARE_S1_PETITIONS_PENDING = 601;
	public static final int THE_PETITION_SYSTEM_IS_CURRENTLY_UNAVAILABLE_PLEASE_TRY_AGAIN_LATER = 602;
	public static final int THAT_ITEM_CANNOT_BE_DISCARDED_OR_EXCHANGED = 603;
	public static final int YOU_MAY_NOT_CALL_FORTH_A_PET_OR_SUMMONED_CREATURE_FROM_THIS_LOCATION = 604;
	public static final int YOU_MAY_REGISTER_UP_TO_64_PEOPLE_ON_YOUR_LIST = 605;
	public static final int YOU_CANNOT_BE_REGISTERED_BECAUSE_THE_OTHER_PERSON_HAS_ALREADY_REGISTERED_64_PEOPLE_ON_HIS_HER_LIST = 606;
	public static final int YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN__COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1 = 607;
	public static final int S1_HAS_OBTAINED_3_S2_S_BY_USING_SWEEPER = 608;
	public static final int S1_HAS_OBTAINED_S2_BY_USING_SWEEPER = 609;
	public static final int YOUR_SKILL_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_HP = 610;
	public static final int YOU_HAVE_SUCCEEDED_IN_CONFUSING_THE_ENEMY = 611;
	public static final int THE_SPOIL_CONDITION_HAS_BEEN_ACTIVATED = 612;
	public static final int _IGNORE_LIST_ = 613; // ======<IGNORE_LIST>======
	public static final int YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST = 615;
	// dublicate of 306
	// public static final int YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER = 616;
	public static final int S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST = 617;
	public static final int S1_HAS_BEEN_REMOVED_FROM_YOUR_IGNORE_LIST = 618;
	public static final int S1__HAS_PLACED_YOU_ON_HIS_HER_IGNORE_LIST = 619;
	public static final int THIS_SERVER_IS_RESERVED_FOR_PLAYERS_IN_KOREA__TO_USE_LINEAGE_II_GAME_SERVICES_PLEASE_CONNECT_TO_THE_SERVER_IN_YOUR_REGION = 621;
	public static final int YOU_MAY_NOT_MAKE_A_DECLARATION_OF_WAR_DURING_AN_ALLIANCE_BATTLE = 622;
	public static final int YOUR_OPPONENT_HAS_EXCEEDED_THE_NUMBER_OF_SIMULTANEOUS_ALLIANCE_BATTLES_ALLOWED = 623;
	public static final int S1_CLAN_LEADER_IS_NOT_CURRENTLY_CONNECTED_TO_THE_GAME_SERVER = 624;
	public static final int YOUR_REQUEST_FOR_ALLIANCE_BATTLE_TRUCE_HAS_BEEN_DENIED = 625;
	public static final int CLAN_BATTLE_HAS_BEEN_REFUSED_BECAUSE_YOU_DID_NOT_RESPOND_TO_S1_CLANS_WAR_PROCLAMATION = 627;
	public static final int YOU_HAVE_ALREADY_BEEN_AT_WAR_WITH_THE_S1_CLAN_5_DAYS_MUST_PASS_BEFORE_YOU_CAN_DECLARE_WAR_AGAIN = 628;
	public static final int WAR_WITH_THE_S1_CLAN_IS_OVER = 631;
	public static final int YOUR_ALLIANCE_LEADER_HAS_BEEN_SLAIN_YOU_HAVE_BEEN_DEFEATED_BY_THE_S1_CLAN = 634;
	public static final int THE_TIME_LIMIT_FOR_THE_CLAN_WAR_HAS_BEEN_EXCEEDED_WAR_WITH_THE_S1_CLAN_IS_OVER = 635;
	public static final int A_CLAN_ALLY_HAS_REGISTERED_ITSELF_TO_THE_OPPONENT = 637;
	public static final int YOU_HAVE_ALREADY_REQUESTED_A_SIEGE_BATTLE = 638;
	public static final int YOUR_APPLICATION_HAS_BEEN_DENIED_BECAUSE_YOU_HAVE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE = 639;
	public static final int YOU_HAVE_FAILED_TO_REFUSE_CASTLE_DEFENSE_AID = 640;
	public static final int YOU_HAVE_FAILED_TO_APPROVE_CASTLE_DEFENSE_AID = 641;
	public static final int YOU_ARE_ALREADY_REGISTERED_TO_THE_ATTACKER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST = 642;
	public static final int YOU_HAVE_ALREADY_REGISTERED_TO_THE_DEFENDER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST = 643;
	public static final int YOU_ARE_NOT_YET_REGISTERED_FOR_THE_CASTLE_SIEGE = 644;
	public static final int ONLY_CLANS_WITH_LEVEL_4_AND_HIGHER_MAY_REGISTER_FOR_A_CASTLE_SIEGE = 645;
	public static final int YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_CASTLE_DEFENDER_LIST = 646;
	public static final int YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_SIEGE_TIME = 647;
	public static final int NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_ATTACKER_SIDE = 648;
	public static final int NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_DEFENDER_SIDE = 649;
	public static final int YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION = 650;
	public static final int PLACE_S1_IN_THE_CURRENT_LOCATION_AND_DIRECTION_DO_YOU_WISH_TO_CONTINUE = 651;
	public static final int THE_TARGET_OF_THE_SUMMONED_MONSTER_IS_WRONG = 652;
	public static final int YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_POSITION_MERCENARIES = 653;
	public static final int YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_CANCEL_MERCENARY_POSITIONING = 654;
	public static final int MERCENARIES_CANNOT_BE_POSITIONED_HERE = 655;
	public static final int THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE = 656;
	public static final int POSITIONING_CANNOT_BE_DONE_HERE_BECAUSE_THE_DISTANCE_BETWEEN_MERCENARIES_IS_TOO_SHORT = 657;
	public static final int THIS_IS_NOT_A_MERCENARY_OF_A_CASTLE_THAT_YOU_OWN_AND_SO_YOU_CANNOT_CANCEL_ITS_POSITIONING = 658;
	public static final int THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATIONS_CANNOT_BE_ACCEPTED_OR_REJECTED = 659;
	public static final int THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATION_AND_CANCELLATION_CANNOT_BE_DONE = 660;
	public static final int IT_IS_A_CHARACTER_THAT_CANNOT_BE_SPOILED = 661;
	public static final int THE_OTHER_PLAYER_IS_REJECTING_FRIEND_INVITATIONS = 662;
	public static final int THE_SIEGE_TIME_HAS_BEEN_DECLARED_FOR_S_IT_IS_NOT_POSSIBLE_TO_CHANGE_THE_TIME_AFTER_A_SIEGE_TIME_HAS_BEEN_DECLARED_DO_YOU_WANT_TO_CONTINUE = 663;
	public static final int PLEASE_CHOOSE_A_PERSON_TO_RECEIVE = 664;
	public static final int S2_OF_S1_ALLIANCE_IS_APPLYING_FOR_ALLIANCE_WAR_DO_YOU_WANT_TO_ACCEPT_THE_CHALLENGE = 665;
	public static final int A_REQUEST_FOR_CEASEFIRE_HAS_BEEN_RECEIVED_FROM_S1_ALLIANCE_DO_YOU_AGREE = 666;
	public static final int YOU_ARE_REGISTERING_ON_THE_ATTACKING_SIDE_OF_THE_S1_SIEGE_DO_YOU_WANT_TO_CONTINUE = 667;
	public static final int YOU_ARE_REGISTERING_ON_THE_DEFENDING_SIDE_OF_THE_S1_SIEGE_DO_YOU_WANT_TO_CONTINUE = 668;
	public static final int YOU_ARE_CANCELING_YOUR_APPLICATION_TO_PARTICIPATE_IN_THE_S1_SIEGE_BATTLE_DO_YOU_WANT_TO_CONTINUE = 669;
	public static final int YOU_ARE_REFUSING_THE_REGISTRATION_OF_S1_CLAN_ON_THE_DEFENDING_SIDE_DO_YOU_WANT_TO_CONTINUE = 670;
	public static final int YOU_ARE_AGREEING_TO_THE_REGISTRATION_OF_S1_CLAN_ON_THE_DEFENDING_SIDE_DO_YOU_WANT_TO_CONTINUE = 671;
	public static final int S1_ADENA_DISAPPEARED = 672;
	public static final int ONLY_A_CLAN_LEADER_WHOSE_CLAN_IS_OF_LEVEL_2_OR_HIGHER_IS_ALLOWED_TO_PARTICIPATE_IN_A_CLAN_HALL_AUCTION = 673;
	public static final int IT_HAS_NOT_YET_BEEN_SEVEN_DAYS_SINCE_CANCELING_AN_AUCTION = 674;
	public static final int THERE_ARE_NO_CLAN_HALLS_UP_FOR_AUCTION = 675;
	public static final int SINCE_YOU_HAVE_ALREADY_SUBMITTED_A_BID_YOU_ARE_NOT_ALLOWED_TO_PARTICIPATE_IN_ANOTHER_AUCTION_AT_THIS_TIME = 676;
	public static final int YOUR_BID_PRICE_MUST_BE_HIGHER_THAN_THE_MINIMUM_PRICE_THAT_CAN_BE_BID = 677;
	public static final int YOU_HAVE_SUBMITTED_A_BID_IN_THE_AUCTION_OF_S1 = 678;
	public static final int YOU_HAVE_CANCELED_YOUR_BID = 679;
	public static final int YOU_CANNOT_PARTICIPATE_IN_AN_AUCTION = 680;
	public static final int THE_CLAN_DOES_NOT_OWN_A_CLAN_HALL = 681;
	public static final int YOU_ARE_MOVING_TO_ANOTHER_VILLAGE_DO_YOU_WANT_TO_CONTINUE = 682;
	public static final int THERE_ARE_NO_PRIORITY_RIGHTS_ON_A_SWEEPER = 683;
	public static final int YOU_CANNOT_POSITION_MERCENARIES_DURING_A_SIEGE = 684;
	public static final int YOU_CANNOT_APPLY_FOR_CLAN_WAR_WITH_A_CLAN_THAT_BELONGS_TO_THE_SAME_ALLIANCE = 685;
	public static final int YOU_HAVE_RECEIVED_S1_DAMAGE_FROM_THE_FIRE_OF_MAGIC = 686;
	public static final int YOU_CANNOT_MOVE_IN_A_FROZEN_STATE_PLEASE_WAIT_A_MOMENT = 687;
	public static final int THE_CLAN_THAT_OWNS_THE_CASTLE_IS_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE = 688;
	public static final int A_CLAN_THAT_OWNS_A_CASTLE_CANNOT_PARTICIPATE_IN_ANOTHER_SIEGE = 689;
	public static final int YOU_CANNOT_REGISTER_ON_THE_ATTACKING_SIDE_BECAUSE_YOU_ARE_PART_OF_AN_ALLIANCE_WITH_THE_CLAN_THAT_OWNS_THE_CASTLE = 690;
	public static final int S1_CLAN_IS_ALREADY_A_MEMBER_OF_S2_ALLIANCE = 691;
	public static final int THE_OTHER_PARTY_IS_FROZEN_PLEASE_WAIT_A_MOMENT = 692;
	public static final int THE_PACKAGE_THAT_ARRIVED_IS_IN_ANOTHER_WAREHOUSE = 693;
	public static final int NO_PACKAGES_HAVE_ARRIVED = 694;
	public static final int YOU_CANNOT_SET_THE_NAME_OF_THE_PET = 695;
	public static final int YOUR_ACCOUNT_IS_RESTRICTED_FOR_NOT_PAYING_YOUR_PC_ROOM_USAGE_FEES = 696;
	public static final int THE_ITEM_ENCHANT_VALUE_IS_STRANGE = 697;
	public static final int THE_PRICE_IS_DIFFERENT_THAN_THE_SAME_ITEM_ON_THE_SALES_LIST = 698;
	public static final int CURRENTLY_NOT_PURCHASING = 699;
	public static final int THE_PURCHASE_IS_COMPLETE = 700;
	public static final int YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS = 701;
	public static final int THERE_ARE_NOT_ANY_GMS_THAT_ARE_PROVIDING_CUSTOMER_SERVICE_CURRENTLY = 702;
	public static final int _GM_LIST_ = 703; // ======<GM_LIST>======
	public static final int GM_S1 = 704;
	public static final int YOU_CANNOT_EXCLUDE_YOURSELF = 705;
	public static final int YOU_CAN_ONLY_REGISTER_UP_TO_64_NAMES_ON_YOUR_EXCLUDE_LIST = 706;
	public static final int YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE = 707;
	public static final int YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CASTLE_WAREHOUSE = 708;
	public static final int YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE = 709;
	public static final int ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE = 710;
	public static final int THE_SIEGE_OF_S1_HAS_STARTED = 711;
	public static final int THE_SIEGE_OF_S1_HAS_FINISHED = 712;
	public static final int S1_S2_S3_S4S5 = 713;
	public static final int A_TRAP_DEVICE_HAS_TRIPPED = 714;
	public static final int THE_TRAP_DEVICE_HAS_STOPPED = 715;
	public static final int IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE = 716;
	public static final int THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE = 717;
	public static final int THE_CASTLE_GATES_CANNOT_BE_OPENED_AND_CLOSED_DURING_A_SIEGE = 718;
	public static final int YOU_FAILED_AT_ITEM_MIXING = 719;
	public static final int THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE = 720;
	public static final int YOU_CANNOT_CREATE_AN_ALLIANCE_WHILE_PARTICIPATING_IN_A_SIEGE = 721;
	public static final int YOU_CANNOT_DISSOLVE_AN_ALLIANCE_WHILE_AN_AFFILIATED_CLAN_IS_PARTICIPATING_IN_A_SIEGE_BATTLE = 722;
	public static final int THE_OPPOSING_CLAN_IS_PARTICIPATING_IN_A_SIEGE_BATTLE = 723;
	public static final int YOU_CANNOT_LEAVE_WHILE_PARTICIPATING_IN_A_SIEGE_BATTLE = 724;
	public static final int YOU_CANNOT_BANISH_A_CLAN_FROM_AN_ALLIANCE_WHILE_THE_CLAN_IS_PARTICIPATING_IN_A_SIEGE = 725;
	public static final int THE_FROZEN_CONDITION_HAS_STARTED_PLEASE_WAIT_A_MOMENT = 726;
	public static final int THE_FROZEN_CONDITION_WAS_REMOVED = 727;
	public static final int YOU_CANNOT_APPLY_FOR_DISSOLUTION_AGAIN_WITHIN_SEVEN_DAYS_AFTER_A_PREVIOUS_APPLICATION_FOR_DISSOLUTION = 728;
	public static final int THAT_ITEM_CANNOT_BE_DISCARDED = 729;
	public static final int YOU_HAVE_SUBMITTED_S1_PETITIONS_YOU_MAY_SUBMIT_S2_MORE_PETITIONS_TODAY = 730;
	public static final int A_PETITION_HAS_BEEN_RECEIVED_BY_THE_GM_ON_BEHALF_OF_S1_IT_IS_PETITION_S2 = 731;
	public static final int S1_HAS_RECEIVED_A_REQUEST_FOR_A_CONSULTATION_WITH_THE_GM = 732;
	public static final int WE_HAVE_RECEIVED_S1_PETITIONS_FROM_YOU_TODAY_AND_THAT_IS_THE_MAXIMUM_THAT_YOU_CAN_SUBMIT_IN_ONE_DAY_YOU_CANNOT_SUBMIT_ANY_MORE_PETITIONS = 733;
	public static final int YOU_FAILED_AT_SUBMITTING_A_PETITION_ON_BEHALF_OF_SOMEONE_ELSE_S1_ALREADY_SUBMITTED_A_PETITION = 734;
	public static final int YOU_FAILED_AT_SUBMITTING_A_PETITION_ON_BEHALF_OF_S1_THE_ERROR_IS_S2 = 735;
	public static final int THE_PETITION_WAS_CANCELED_YOU_MAY_SUBMIT_S1_MORE_PETITIONS_TODAY = 736;
	public static final int YOU_FAILED_AT_SUBMITTING_A_PETITION_ON_BEHALF_OF_S1 = 737;
	public static final int YOU_HAVE_NOT_SUBMITTED_A_PETITION = 738;
	public static final int YOU_FAILED_AT_CANCELING_A_PETITION_ON_BEHALF_OF_S1_THE_ERROR_CODE_IS_S2 = 739;
	public static final int S1_PARTICIPATED_IN_A_PETITION_CHAT_AT_THE_REQUEST_OF_THE_GM = 740;
	public static final int YOU_FAILED_AT_ADDING_S1_TO_THE_PETITION_CHAT_A_PETITION_HAS_ALREADY_BEEN_SUBMITTED = 741;
	public static final int YOU_FAILED_AT_ADDING_S1_TO_THE_PETITION_CHAT_THE_ERROR_CODE_IS_S2 = 742;
	public static final int S1_LEFT_THE_PETITION_CHAT = 743;
	public static final int YOU_FAILED_AT_REMOVING_S1_FROM_THE_PETITION_CHAT_THE_ERROR_CODE_IS_S2 = 744;
	public static final int YOU_ARE_CURRENTLY_NOT_IN_A_PETITION_CHAT = 745;
	public static final int IT_IS_NOT_CURRENTLY_A_PETITION = 746;
	public static final int IF_YOU_NEED_HELP_PLEASE_USE_11_INQUIRY_ON_THE_OFFICIAL_WEB_SITE = 747;
	public static final int THE_DISTANCE_IS_TOO_FAR_AND_SO_THE_CASTING_HAS_BEEN_STOPPED = 748;
	public static final int THE_EFFECT_OF_S1_HAS_BEEN_REMOVED = 749;
	public static final int THERE_ARE_NO_OTHER_SKILLS_TO_LEARN = 750;
	public static final int AS_THERE_IS_A_CONFLICT_IN_THE_SIEGE_RELATIONSHIP_WITH_A_CLAN_IN_THE_ALLIANCE_YOU_CANNOT_INVITE_THAT_CLAN_TO_THE_ALLIANCE = 751;
	public static final int THAT_NAME_CANNOT_BE_USED = 752;
	public static final int YOU_CANNOT_POSITION_MERCENARIES_HERE = 753;
	public static final int THERE_ARE_S1_HOURS_AND_S2_MINUTES_LEFT_IN_THIS_WEEKS_USAGE_TIME = 754;
	public static final int THERE_ARE_S1_MINUTES_LEFT_IN_THIS_WEEKS_USAGE_TIME = 755;
	public static final int THIS_WEEKS_USAGE_TIME_HAS_FINISHED = 756;
	public static final int THERE_ARE_S1_HOURS_AND_S2_MINUTES_LEFT_IN_THE_FIXED_USE_TIME = 757;
	public static final int THERE_ARE_S1_MINUTES_LEFT_IN_THIS_WEEKS_PLAY_TIME = 758;
	public static final int S1_CANNOT_JOIN_THE_CLAN_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_HE_SHE_LEFT_ANOTHER_CLAN = 760;
	public static final int S1_CLAN_CANNOT_JOIN_THE_ALLIANCE_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_IT_LEFT_ANOTHER_ALLIANCE = 761;
	public static final int S1_ROLLED_S2_AND_S3S_EYE_CAME_OUT = 762;
	public static final int YOU_FAILED_AT_SENDING_THE_PACKAGE_BECAUSE_YOU_ARE_TOO_FAR_FROM_THE_WAREHOUSE = 763;
	public static final int YOU_HAVE_BEEN_PLAYING_FOR_AN_EXTENDED_PERIOD_OF_TIME_PLEASE_CONSIDER_TAKING_A_BREAK = 764;
	public static final int GAMEGUARD_IS_ALREADY_RUNNING_PLEASE_TRY_RUNNING_IT_AGAIN_AFTER_REBOOTING = 765;
	public static final int THERE_IS_A_GAMEGUARD_INITIALIZATION_ERROR_PLEASE_TRY_RUNNING_IT_AGAIN_AFTER_REBOOTING = 766;
	public static final int THE_GAMEGUARD_FILE_IS_DAMAGED__PLEASE_REINSTALL_GAMEGUARD = 767;
	public static final int A_WINDOWS_SYSTEM_FILE_IS_DAMAGED_PLEASE_REINSTALL_INTERNET_EXPLORER = 768;
	public static final int A_HACKING_TOOL_HAS_BEEN_DISCOVERED_PLEASE_TRY_PLAYING_AGAIN_AFTER_CLOSING_UNNECESSARY_PROGRAMS = 769;
	public static final int THE_GAMEGUARD_UPDATE_WAS_CANCELED_PLEASE_CHECK_YOUR_NETWORK_CONNECTION_STATUS_OR_FIREWALL = 770;
	public static final int THE_GAMEGUARD_UPDATE_WAS_CANCELED_PLEASE_TRY_RUNNING_IT_AGAIN_AFTER_DOING_A_VIRUS_SCAN_OR_CHANGING_THE_SETTINGS_IN_YOUR_PC_MANAGEMENT_PROGRAM = 771;
	public static final int THERE_WAS_A_PROBLEM_WHEN_RUNNING_GAMEGUARD = 772;
	public static final int THE_GAME_OR_GAMEGUARD_FILES_ARE_DAMAGED = 773;
	public static final int SINCE_THIS_IS_A_PEACE_ZONE_PLAY_TIME_DOES_NOT_GET_EXPENDED_HERE = 774;
	public static final int FROM_HERE_ON_PLAY_TIME_WILL_BE_EXPENDED = 775;
	public static final int THE_CLAN_HALL_WHICH_WAS_PUT_UP_FOR_AUCTION_HAS_BEEN_AWARDED_TO_S1_CLAN = 776;
	public static final int THE_CLAN_HALL_WHICH_HAD_BEEN_PUT_UP_FOR_AUCTION_WAS_NOT_SOLD_AND_THEREFORE_HAS_BEEN_RELISTED = 777;
	public static final int YOU_MAY_NOT_LOG_OUT_FROM_THIS_LOCATION = 778;
	public static final int YOU_MAY_NOT_RESTART_IN_THIS_LOCATION = 779;
	public static final int OBSERVATION_IS_ONLY_POSSIBLE_DURING_A_SIEGE = 780;
	public static final int OBSERVERS_CANNOT_PARTICIPATE = 781;
	public static final int YOU_MAY_NOT_OBSERVE_A_SUMMONED_CREATURE = 782;
	public static final int LOTTERY_TICKET_SALES_HAVE_BEEN_TEMPORARILY_SUSPENDED = 783;
	public static final int TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE = 784;
	public static final int THE_RESULTS_OF_LOTTERY_NUMBER_S1_HAVE_NOT_YET_BEEN_PUBLISHED = 785;
	public static final int INCORRECT_SYNTAX = 786;
	public static final int THE_TRYOUTS_ARE_FINISHED = 787;
	public static final int THE_FINALS_ARE_FINISHED = 788;
	public static final int THE_TRYOUTS_HAVE_BEGUN = 789;
	public static final int THE_FINALS_HAVE_BEGUN = 790;
	public static final int THE_FINAL_MATCH_IS_ABOUT_TO_BEGIN_LINE_UP = 791;
	public static final int THE_SIEGE_OF_THE_CLAN_HALL_IS_FINISHED = 792;
	public static final int THE_SIEGE_OF_THE_CLAN_HALL_HAS_BEGUN = 793;
	public static final int YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT = 794;
	public static final int ONLY_CLAN_LEADERS_ARE_AUTHORIZED_TO_SET_RIGHTS = 795;
	public static final int YOUR_REMAINING_OBSERVATION_TIME_IS_S1_MINUTES = 796;
	public static final int YOU_MAY_CREATE_UP_TO_48_MACROS = 797;
	public static final int ITEM_REGISTRATION_IS_IRREVERSIBLE_DO_YOU_WISH_TO_CONTINUE = 798;
	public static final int THE_OBSERVATION_TIME_HAS_EXPIRED = 799;
	public static final int YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER = 800;
	public static final int REGISTRATION_FOR_THE_CLAN_HALL_SIEGE_IS_CLOSED = 801;
	public static final int PETITIONS_ARE_NOT_BEING_ACCEPTED_AT_THIS_TIME_YOU_MAY_SUBMIT_YOUR_PETITION_AFTER_S1_AM_PM = 802;
	public static final int ENTER_THE_SPECIFICS_OF_YOUR_PETITION = 803;
	public static final int SELECT_A_TYPE = 804;
	public static final int IF_YOU_ARE_TRAPPED_TRY_TYPING__UNSTUCK = 806;
	public static final int THIS_TERRAIN_IS_UNNAVIGABLE_PREPARE_FOR_TRANSPORT_TO_THE_NEAREST_VILLAGE = 807;
	public static final int YOU_ARE_STUCK_YOU_MAY_SUBMIT_A_PETITION_BY_TYPING__GM = 808;
	public static final int YOU_ARE_STUCK_YOU_WILL_BE_TRANSPORTED_TO_THE_NEAREST_VILLAGE_IN_FIVE_MINUTES = 809;
	public static final int INVALID_MACRO_REFER_TO_THE_HELP_FILE_FOR_INSTRUCTIONS = 810;
	public static final int YOU_WILL_BE_MOVED_TO_S1_DO_YOU_WISH_TO_CONTINUE = 811;
	public static final int THE_SECRET_TRAP_HAS_INFLICTED_S1_DAMAGE_ON_YOU = 812;
	public static final int YOU_HAVE_BEEN_POISONED_BY_A_SECRET_TRAP = 813;
	public static final int YOUR_SPEED_HAS_BEEN_DECREASED_BY_A_SECRET_TRAP = 814;
	public static final int THE_TRYOUTS_ARE_ABOUT_TO_BEGIN = 815;
	public static final int TICKETS_ARE_NOW_AVAILABLE_FOR_THE_S1TH_MONSTER_RACE = 816;
	public static final int WE_ARE_NOW_SELLING_TICKETS_FOR_THE_S1TH_MONSTER_RACE = 817;
	public static final int TICKET_SALES_FOR_THE_MONSTER_RACE_WILL_CEASE_IN_S1_MINUTE_S = 818;
	public static final int TICKETS_SALES_ARE_CLOSED_FOR_THE_S1TH_MONSTER_RACE_ODDS_ARE_POSTED = 819;
	public static final int THE_S2TH_MONSTER_RACE_WILL_BEGIN_IN_S1_MINUTES = 820;
	public static final int THE_S1TH_MONSTER_RACE_WILL_BEGIN_IN_30_SECONDS = 821;
	public static final int THE_S1TH_MONSTER_RACE_IS_ABOUT_TO_BEGIN_COUNTDOWN_IN_FIVE_SECONDS = 822;
	public static final int THE_RACE_WILL_BEGIN_IN_S1_SECONDS = 823;
	public static final int THEYRE_OFF = 824;
	public static final int MONSTER_RACE_S1_IS_FINISHED = 825;
	public static final int FIRST_PRIZE_GOES_TO_THE_PLAYER_IN_LANE_S1_SECOND_PRIZE_GOES_TO_THE_PLAYER_IN_LANE_S2 = 826;
	public static final int YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM = 827;
	public static final int ARE_YOU_SURE_YOU_WISH_TO_DELETE_THE_S1_MACRO = 828;
	public static final int S1_HAS_ROLLED_S2 = 834;
	public static final int YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIMETRY_AGAIN_LATER = 835;
	public static final int THE_INVENTORY_IS_FULL_NO_FURTHER_QUEST_ITEMS_MAY_BE_DEPOSITED_AT_THIS_TIME = 836;
	public static final int MACRO_DESCRIPTIONS_MAY_CONTAIN_UP_TO_32_CHARACTERS = 837;
	public static final int ENTER_THE_NAME_OF_THE_MACRO = 838;
	public static final int THAT_NAME_IS_ALREADY_ASSIGNED_TO_ANOTHER_MACRO = 839;
	public static final int THAT_RECIPE_IS_ALREADY_REGISTERED = 840;
	public static final int NO_FURTHER_RECIPES_MAY_BE_REGISTERED = 841;
	public static final int YOU_ARE_NOT_AUTHORIZED_TO_REGISTER_A_RECIPE = 842;
	public static final int THE_SIEGE_OF_S1_IS_FINISHED = 843;
	public static final int THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN = 844;
	public static final int THE_DEADLINE_TO_REGISTER_FOR_THE_SIEGE_OF_S1_HAS_PASSED = 845;
	public static final int THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST = 846;
	public static final int A_CLAN_THAT_OWNS_A_CLAN_HALL_MAY_NOT_PARTICIPATE_IN_A_CLAN_HALL_SIEGE = 847;
	public static final int S1_HAS_BEEN_DELETED = 848;
	public static final int S1_CANNOT_BE_FOUND = 849;
	public static final int S1_HAS_BEEN_ADDED = 851;
	public static final int THE_RECIPE_IS_INCORRECT = 852;
	public static final int YOU_MAY_NOT_ALTER_YOUR_RECIPE_BOOK_WHILE_ENGAGED_IN_MANUFACTURING = 853;
	public static final int YOU_LACK_S2_OF_S1 = 854;
	public static final int S1_CLAN_HAS_DEFEATED_S2 = 855;
	public static final int THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW = 856;
	public static final int S1_CLAN_HAS_WON_IN_THE_PRELIMINARY_MATCH_OF_S2 = 857;
	public static final int THE_PRELIMINARY_MATCH_OF_S1_HAS_ENDED_IN_A_DRAW = 858;
	public static final int PLEASE_REGISTER_A_RECIPE = 859;
	public static final int YOU_MAY_NOT_BUILD_YOUR_HEADQUARTERS_IN_CLOSE_PROXIMITY_TO_ANOTHER_HEADQUARTERS = 860;
	public static final int YOU_HAVE_EXCEEDED_THE_MAXIMUM_NUMBER_OF_MEMOS = 861;
	public static final int ODDS_ARE_NOT_POSTED_UNTIL_TICKET_SALES_HAVE_CLOSED = 862;
	public static final int YOU_FEEL_THE_ENERGY_OF_FIRE = 863;
	public static final int YOU_FEEL_THE_ENERGY_OF_WATER = 864;
	public static final int YOU_FEEL_THE_ENERGY_OF_WIND = 865;
	public static final int YOU_MAY_NO_LONGER_GATHER_ENERGY = 866;
	public static final int THE_ENERGY_IS_DEPLETED = 867;
	public static final int THE_ENERGY_OF_FIRE_HAS_BEEN_DELIVERED = 868;
	public static final int THE_ENERGY_OF_WATER_HAS_BEEN_DELIVERED = 869;
	public static final int THE_ENERGY_OF_WIND_HAS_BEEN_DELIVERED = 870;
	public static final int THE_SEED_HAS_BEEN_SOWN = 871;
	public static final int THIS_SEED_MAY_NOT_BE_SOWN_HERE = 872;
	public static final int THAT_CHARACTER_DOES_NOT_EXIST = 873;
	public static final int THE_CAPACITY_OF_THE_WAREHOUSE_HAS_BEEN_EXCEEDED = 874;
	public static final int TRANSPORT_OF_CARGO_HAS_BEEN_CANCELED = 875;
	public static final int CARGO_WAS_NOT_DELIVERED = 876;
	public static final int THE_SYMBOL_HAS_BEEN_ADDED = 877;
	public static final int THE_SYMBOL_HAS_BEEN_DELETED = 878;
	public static final int THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE = 879;
	public static final int THE_TRANSACTION_IS_COMPLETE = 880;
	public static final int THERE_IS_A_DISCREPANCY_ON_THE_INVOICE = 881;
	public static final int SEED_QUANTITY_IS_INCORRECT = 882;
	public static final int SEED_INFORMATION_IS_INCORRECT = 883;
	public static final int THE_MANOR_INFORMATION_HAS_BEEN_UPDATED = 884;
	public static final int THE_NUMBER_OF_CROPS_IS_INCORRECT = 885;
	public static final int THE_CROPS_ARE_PRICED_INCORRECTLY = 886;
	public static final int THE_TYPE_IS_INCORRECT = 887;
	public static final int NO_CROPS_CAN_BE_PURCHASED_AT_THIS_TIME = 888;
	public static final int THE_SEED_WAS_SUCCESSFULLY_SOWN = 889;
	public static final int THE_SEED_WAS_NOT_SOWN = 890;
	public static final int YOU_ARE_NOT_AUTHORIZED_TO_HARVEST = 891;
	public static final int THE_HARVEST_HAS_FAILED = 892;
	public static final int THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN = 893;
	public static final int UP_TO_S1_RECIPES_CAN_BE_REGISTERED = 894;
	public static final int NO_RECIPES_HAVE_BEEN_REGISTERED = 895;
	public static final int QUEST_RECIPES_CAN_NOT_BE_REGISTERED = 896;
	public static final int THE_FEE_TO_CREATE_THE_ITEM_IS_INCORRECT = 897;
	public static final int THE_SYMBOL_CANNOT_BE_DRAWN = 899;
	public static final int NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL = 900;
	public static final int THE_SYMBOL_INFORMATION_CANNOT_BE_FOUND = 901;
	public static final int THE_NUMBER_OF_ITEMS_IS_INCORRECT = 902;
	public static final int YOU_MAY_NOT_SUBMIT_A_PETITION_WHILE_FROZEN_BE_PATIENT = 903;
	public static final int ITEMS_CANNOT_BE_DISCARDED_WHILE_IN_PRIVATE_STORE_STATUS = 904;
	public static final int THE_CURRENT_SCORE_FOR_THE_HUMAN_RACE_IS_S1 = 905;
	public static final int THE_CURRENT_SCORE_FOR_THE_ELVEN_RACE_IS_S1 = 906;
	public static final int THE_CURRENT_SCORE_FOR_THE_DARK_ELVEN_RACE_IS_S1 = 907;
	public static final int THE_CURRENT_SCORE_FOR_THE_ORC_RACE_IS_S1 = 908;
	public static final int THE_CURRENT_SCORE_FOR_THE_DWARVEN_RACE_IS_S1 = 909;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_TALKING_ISLAND_VILLAGE = 910;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_GLUDIN_VILLAGE = 911;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_GLUDIO_CASTLE_TOWN = 912;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_THE_NEUTRAL_ZONE = 913;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_ELVEN_VILLAGE = 914;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_DARK_ELVEN_VILLAGE = 915;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_DION_CASTLE_TOWN = 916;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_FLORAN_VILLAGE = 917;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_GIRAN_CASTLE_TOWN = 918;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_GIRAN_HARBOR = 919;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_ORC_VILLAGE = 920;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_DWARVEN_VILLAGE = 921;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_THE_TOWN_OF_OREN = 922;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_HUNTERS_VILLAGE = 923;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_ADEN_CASTLE_TOWN = 924;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_THE_COLISEUM = 925;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_HEINE = 926;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_THE_TOWN_OF_SCHUTTGART = 1714;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_PRIMEVAL_ISLE = 1924;
	public static final int THE_CURRENT_TIME_IS_S1S2_IN_THE_DAY = 927;
	public static final int THE_CURRENT_TIME_IS_S1S2_IN_THE_NIGHT = 928;
	public static final int NO_COMPENSATION_WAS_GIVEN_FOR_THE_FARM_PRODUCTS = 929;
	public static final int LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD = 930;
	public static final int THE_WINNING_LOTTERY_TICKET_NUMBER_HAS_NOT_YET_BEEN_ANNOUNCED = 931;
	public static final int YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING = 932;
	public static final int THE_SEED_PRICING_GREATLY_DIFFERS_FROM_STANDARD_SEED_PRICES = 933;
	public static final int IT_IS_A_DELETED_RECIPE = 934;
	public static final int THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION = 935;
	public static final int CURRENTLY_PREPARING_FOR_PRIVATE_WORKSHOP = 937;
	public static final int THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE = 938;
	public static final int YOU_CANNOT_EXCHANGE_WHILE_BLOCKING_EVERYTHING = 939;
	public static final int S1_IS_BLOCKING_EVERYTHING = 940;
	public static final int RESTART_AT_TALKING_ISLAND_VILLAGE = 941;
	public static final int RESTART_AT_GLUDIN_VILLAGE = 942;
	public static final int RESTART_AT_GLUDIN_CASTLE_TOWN = 943;
	public static final int RESTART_AT_THE_NEUTRAL_ZONE = 944;
	public static final int RESTART_AT_ELVEN_VILLAGE = 945;
	public static final int RESTART_AT_DARK_ELVEN_VILLAGE = 946;
	public static final int RESTART_AT_DION_CASTLE_TOWN = 947;
	public static final int RESTART_AT_FLORAN_VILLAGE = 948;
	public static final int RESTART_AT_GIRAN_CASTLE_TOWN = 949;
	public static final int RESTART_AT_GIRAN_HARBOR = 950;
	public static final int RESTART_AT_ORC_VILLAGE = 951;
	public static final int RESTART_AT_DWARVEN_VILLAGE = 952;
	public static final int RESTART_AT_THE_TOWN_OF_OREN = 953;
	public static final int RESTART_AT_HUNTERS_VILLAGE = 954;
	public static final int RESTART_AT_ADEN_CASTLE_TOWN = 955;
	public static final int RESTART_AT_THE_COLISEUM = 956;
	public static final int RESTART_AT_HEINE = 957;
	public static final int ITEMS_CANNOT_BE_DISCARDED_OR_DESTROYED_WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP = 958;
	public static final int S1_S2_MANUFACTURING_SUCCESS = 959;
	public static final int S1_MANUFACTURING_FAILURE = 960;
	public static final int YOU_ARE_NOW_BLOCKING_EVERYTHING = 961;
	public static final int YOU_ARE_NO_LONGER_BLOCKING_EVERYTHING = 962;
	public static final int PLEASE_DETERMINE_THE_MANUFACTURING_PRICE = 963;
	public static final int CHATTING_IS_PROHIBITED_FOR_ABOUT_ONE_MINUTE = 964;
	public static final int THE_CHATTING_PROHIBITION_HAS_BEEN_REMOVED = 965;
	public static final int CHATTING_IS_CURRENTLY_PROHIBITED_IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_BECOME_EVEN_LONGER = 966;
	public static final int DO_YOU_ACCEPT_THE_PARTY_INVITATION_FROM_S1_ITEM_DISTRIBUTION_RANDOM_INCLUDING_SPOIL = 967;
	public static final int DO_YOU_ACCEPT_THE_PARTY_INVITATION_FROM_S1_ITEM_DISTRIBUTION_BY_TURN = 968;
	public static final int DO_YOU_ACCEPT_THE_PARTY_INVITATION_FROM_S1_ITEM_DISTRIBUTION_BY_TURN_INCLUDING_SPOIL = 969;
	public static final int S2S_MP_HAS_BEEN_DRAINED_BY_S1 = 970;
	public static final int PETITIONS_CANNOT_EXCEED_255_CHARACTERS = 971;
	public static final int PETS_CANNOT_USE_THIS_ITEM = 972;
	public static final int PLEASE_INPUT_NO_MORE_THAN_THE_NUMBER_YOU_HAVE = 973;
	public static final int THE_SOUL_CRYSTAL_SUCCEEDED_IN_ABSORBING_A_SOUL = 974;
	public static final int THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_A_SOUL = 975;
	public static final int SOUL_CRYSTAL_BROKE = 976;
	public static final int THE_SOUL_CRYSTALS_CAUSED_RESONATION_AND_FAILED_AT_ABSORBING_A_SOUL = 977;
	public static final int THE_SOUL_CRYSTAL_IS_REFUSING_TO_ABSORB_A_SOUL = 978;
	public static final int CANNOT_PROCESS_A_MONSTER_RACE_TICKET = 1003;
	public static final int YOU_HAVE_REGISTERED_FOR_A_CLAN_HALL_AUCTION = 1004;
	public static final int THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE = 1005;
	public static final int YOU_HAVE_BID_IN_A_CLAN_HALL_AUCTION = 1006;
	public static final int THE_PRELIMINARY_MATCH_REGISTRATION_OF_S1_HAS_FINISHED = 1007;
	public static final int A_HUNGRY_STRIDER_CANNOT_BE_MOUNTED_OR_DISMOUNTED = 1008;
	public static final int A_STRIDER_CANNOT_BE_RIDDEN_WHEN_DEAD = 1009;
	public static final int A_DEAD_STRIDER_CANNOT_BE_RIDDEN = 1010;
	public static final int A_STRIDER_IN_BATTLE_CANNOT_BE_RIDDEN = 1011;
	public static final int A_STRIDER_CANNOT_BE_RIDDEN_WHILE_IN_BATTLE = 1012;
	public static final int A_STRIDER_CAN_BE_RIDDEN_ONLY_WHEN_STANDING = 1013;
	public static final int THE_PET_ACQUIRED_EXPERIENCE_POINTS_OF_S1 = 1014;
	public static final int THE_PET_GAVE_DAMAGE_OF_S1 = 1015;
	public static final int THE_PET_RECEIVED_DAMAGE_OF_S2_CAUSED_BY_S1 = 1016;
	public static final int PETS_CRITICAL_HIT = 1017;
	public static final int THE_PET_USES_S1 = 1018;
	public static final int THE_PET_GAVE_S1 = 1020;
	public static final int THE_PET_GAVE_S2_S1_S = 1021;
	public static final int THE_PET_GAVE__S1_S2 = 1022;
	public static final int THE_PET_GAVE_S1_ADENA = 1023;
	public static final int THE_PET_PUT_ON_S1 = 1024;
	public static final int THE_PET_TOOK_OFF_S1 = 1025;
	public static final int THE_SUMMONED_MONSTER_GAVE_DAMAGE_OF_S1 = 1026;
	public static final int THE_SUMMONED_MONSTER_RECEIVED_DAMAGE_OF_S2_CAUSED_BY_S1 = 1027;
	public static final int SUMMONED_MONSTERS_CRITICAL_HIT = 1028;
	public static final int A_SUMMONED_MONSTER_USES_S1 = 1029;
	public static final int _PARTY_INFORMATION_ = 1030;
	public static final int LOOTING_METHOD_FINDERS_KEEPERS = 1031;
	public static final int LOOTING_METHOD_RANDOM = 1032;
	public static final int LOOTING_METHOD_RANDOM_INCLUDING_SPOIL = 1033;
	public static final int LOOTING_METHOD_BY_TURN = 1034;
	public static final int LOOTING_METHOD_BY_TURN_INCLUDING_SPOIL = 1035;
	public static final int YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED = 1036;
	public static final int S1_MANUFACTURED_S2 = 1037;
	public static final int S1_MANUFACTURED_S3_S2_S = 1038;
	public static final int ITEMS_LEFT_AT_THE_CLAN_HALL_WAREHOUSE_CAN_ONLY_BE_RETRIEVED_BY_THE_CLAN_LEADER_DO_YOU_WANT_TO_CONTINUE = 1039;
	public static final int PACKAGES_SENT_CAN_ONLY_BE_RETRIEVED_AT_THIS_WAREHOUSE_DO_YOU_WANT_TO_CONTINUE = 1040;
	public static final int THE_NEXT_SEED_PURCHASE_PRICE_IS_S1_ADENA = 1041;
	public static final int THE_NEXT_FARM_GOODS_PURCHASE_PRICE_IS_S1_ADENA = 1042;
	public static final int AT_THE_CURRENT_TIME_THE__UNSTUCK_COMMAND_CANNOT_BE_USED_PLEASE_SEND_IN_A_PETITION = 1043;
	public static final int MONSTER_RACE_PAYOUT_INFORMATION_IS_NOT_AVAILABLE_WHILE_TICKETS_ARE_BEING_SOLD = 1044;
	public static final int NOT_CURRENTLY_PREPARING_FOR_A_MONSTER_RACE = 1045;
	public static final int MONSTER_RACE_TICKETS_ARE_NO_LONGER_AVAILABLE = 1046;
	public static final int WE_DID_NOT_SUCCEED_IN_PRODUCING_S1_ITEM = 1047;
	public static final int WHISPERING_IS_NOT_POSSIBLE_IN_STATE_OF_OVERALL_BLOCKING = 1048;
	public static final int IT_IS_NOT_POSSIBLE_TO_MAKE_INVITATIONS_FOR_ORGANIZING_PARTIES_IN_STATE_OF_OVERALL_BLOCKING = 1049;
	public static final int NO_CB_IN_MY_CLAN = 1050;
	public static final int PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW = 1051;
	public static final int THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED = 1052;
	public static final int IT_IS_IMPOSSIBLE_TO_BE_RESSURECTED_IN_BATTLEFIELDS_WHERE_SIEGE_WARS_ARE_IN_PROCESS = 1053;
	public static final int YOU_HAVE_ENTERED_A_LAND_WITH_MYSTERIOUS_POWERS = 1054;
	public static final int YOU_HAVE_LEFT_THE_LAND_WHICH_HAS_MYSTERIOUS_POWERS = 1055;
	public static final int YOU_HAVE_EXCEEDED_THE_CASTLES_STORAGE_LIMIT_OF_ADENA = 1056;
	public static final int THIS_COMMAND_CAN_ONLY_BE_USED_IN_THE_RELAX_SERVER = 1057;
	public static final int THE_SALES_AMOUNT_OF_SEEDS_IS_S1_ADENA = 1058;
	public static final int THE_REMAINING_PURCHASING_AMOUNT_IS_S1_ADENA = 1059;
	public static final int THE_REMAINDER_AFTER_SELLING_THE_SEEDS_IS_S1 = 1060;
	public static final int THE_RECIPE_CANNOT_BE_REGISTERED__YOU_DO_NOT_HAVE_THE_ABILITY_TO_CREATE_ITEMS = 1061;
	public static final int WRITING_SOMETHING_NEW_IS_POSSIBLE_AFTER_LEVEL_10 = 1062;
	public static final int PETITION_SERVICE_IS_NOT_AVAILABEL_FOR_S1_TO_S2_IN_CASE_OF_BEING_TRAPPED_IN_TERRITORY_WHERE_YOU_ARE_UNABLE_TO_MOVE_PLEASE_USE_THE__UNSTUCK_COMMAND = 1063;
	public static final int EQUIPMENT_OF__S1_S2_HAS_BEEN_REMOVED = 1064;
	public static final int WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM = 1065;
	public static final int S1_HPS_HAVE_BEEN_RESTORED = 1066;
	public static final int XS2_HP_HAS_BEEN_RESTORED_BY_S1 = 1067;
	public static final int S1_MPS_HAVE_BEEN_RESTORED = 1068;
	public static final int XS2_MP_HAS_BEEN_RESTORED_BY_S1 = 1069;
	public static final int XYOU_DO_NOT_HAVE_XREADX_PERMISSION = 1070;
	public static final int XYOU_DO_NOT_HAVE_XWRITEX_PERMISSION = 1071;
	public static final int YOU_HAVE_OBTAINED_A_TICKET_FOR_THE_MONSTER_RACE_S1__SINGLE = 1072;
	public static final int YOU_HAVE_OBTAINED_A_TICKET_FOR_THE_MONSTER_RACE_S1__DOUBLE = 1073;
	public static final int YOU_DO_NOT_MEET_THE_AGE_REQUIREMENT_TO_PURCHASE_A_MONSTER_RACE_TICKET = 1074;
	public static final int THE_SECOND_BID_AMOUNT_MUST_BE_HIGHER_THAN_THE_ORIGINAL = 1075;
	public static final int THE_GAME_CANNOT_BE_TERMINATED = 1076;
	public static final int A_GAMEGUARD_EXECUTION_ERROR_HAS_OCCURRED_PLEASE_SEND_THE_ERL_FILE_S_LOCATED_IN_THE_GAMEGUARD_FOLDER_TO_GAME = 1077;
	public static final int WHEN_A_USERS_KEYBOARD_INPUT_EXCEEDS_A_CERTAIN_CUMULATIVE_SCORE_A_CHAT_BAN_WILL_BE_APPLIED_THIS_IS_DONE_TO_DISCOURAGE_SPAMMING_PLEASE_AVOID_POSTING_THE_SAME_MESSAGE_MULTIPLE_TIMES_DURING_A_SHORT_PERIOD = 1078;
	public static final int THE_TARGET_IS_CURRENTLY_BANNED_FROM_CHATTING = 1079;
	public static final int DO_YOU_WISH_TO_USE_THE_FACELIFTING_POTION_X_TYPE_A_IT_IS_PERMANENT = 1080;
	public static final int DO_YOU_WISH_TO_USE_THE_DYE_POTION_X_TYPE_A_IT_IS_PERMANENT = 1081;
	public static final int DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_X_TYPE_A_IT_IS_PERMANENT = 1082;
	public static final int THE_FACELIFTING_POTION__TYPE_A_IS_BEING_USED = 1083;
	public static final int THE_DYE_POTION__TYPE_A_IS_BEING_USED = 1084;
	public static final int THE_HAIR_STYLE_CHANGE_POTION__TYPE_A_IS_BEING_USED = 1085;
	public static final int YOUR_FACIAL_APPEARANCE_HAS_BEEN_CHANGED = 1086;
	public static final int YOUR_HAIR_COLOR_HAS_BEEN_CHANGED = 1087;
	public static final int YOUR_HAIR_STYLE_HAS_BEEN_CHANGED = 1088;
	public static final int S1_HAS_OBTAINED_A_FIRST_ANNIVERSARY_COMMEMORATIVE_ITEM = 1089;
	public static final int DO_YOU_WISH_TO_USE_THE_FACELIFTING_POTION_X_TYPE_B_IT_IS_PERMANENT = 1090;
	public static final int XDO_YOU_WISH_TO_USE_THE_FACELIFTING_POTION_X_TYPE_C_IT_IS_PERMANENT = 1091;
	public static final int XDO_YOU_WISH_TO_USE_THE_DYE_POTION_X_TYPE_B_IT_IS_PERMANENT = 1092;
	public static final int XDO_YOU_WISH_TO_USE_THE_DYE_POTION_X_TYPE_C_IT_IS_PERMANENT = 1093;
	public static final int XDO_YOU_WISH_TO_USE_THE_DYE_POTION_X_TYPE_D_IT_IS_PERMANENT = 1094;
	public static final int XDO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_X_TYPE_B_IT_IS_PERMANENT = 1095;
	public static final int XDO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_X_TYPE_C_IT_IS_PERMANENT = 1096;
	public static final int XDO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_X_TYPE_D_IT_IS_PERMANENT = 1097;
	public static final int XDO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_X_TYPE_E_IT_IS_PERMANENT = 1098;
	public static final int XDO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_X_TYPE_F_IT_IS_PERMANENT = 1099;
	public static final int XDO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_X_TYPE_G_IT_IS_PERMANENT = 1100;
	public static final int THE_FACELIFTING_POTION__TYPE_B_IS_BEING_USED = 1101;
	public static final int THE_FACELIFTING_POTION__TYPE_C_IS_BEING_USED = 1102;
	public static final int THE_DYE_POTION__TYPE_B_IS_BEING_USED = 1103;
	public static final int THE_DYE_POTION__TYPE_C_IS_BEING_USED = 1104;
	public static final int THE_DYE_POTION__TYPE_D_IS_BEING_USED = 1105;
	public static final int THE_HAIR_STYLE_CHANGE_POTION__TYPE_B_IS_BEING_USED = 1106;
	public static final int THE_HAIR_STYLE_CHANGE_POTION__TYPE_C_IS_BEING_USED = 1107;
	public static final int THE_HAIR_STYLE_CHANGE_POTION__TYPE_D_IS_BEING_USED = 1108;
	public static final int THE_HAIR_STYLE_CHANGE_POTION__TYPE_E_IS_BEING_USED = 1109;
	public static final int THE_HAIR_STYLE_CHANGE_POTION__TYPE_F_IS_BEING_USED = 1110;
	public static final int THE_HAIR_STYLE_CHANGE_POTION__TYPE_G_IS_BEING_USED = 1111;
	public static final int THE_PRIZE_AMOUNT_FOR_THE_WINNER_OF_LOTTERY__S1__IS_S2_ADENA_WE_HAVE_S3_FIRST_PRIZE_WINNERS = 1112;
	public static final int THE_PRIZE_AMOUNT_FOR_LUCKY_LOTTERY__S1__IS_S2_ADENA_THERE_WAS_NO_FIRST_PRIZE_WINNER_IN_THIS_DRAWING_THEREFORE_THE_JACKPOT_WILL_BE_ADDED_TO_THE_NEXT_DRAWING = 1113;
	public static final int YOUR_CLAN_MAY_NOT_REGISTER_TO_PARTICIPATE_IN_A_SIEGE_WHILE_UNDER_A_GRACE_PERIOD_OF_THE_CLANS_DISSOLUTION = 1114;
	public static final int INDIVIDUALS_MAY_NOT_SURRENDER_DURING_COMBAT = 1115;
	public static final int ONE_CANNOT_LEAVE_ONES_CLAN_DURING_COMBAT = 1116;
	public static final int A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT = 1117;
	public static final int PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORYS_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY = 1118;
	public static final int QUEST_WAS_AUTOMATICALLY_CANCELED_WHEN_YOU_ATTEMPTED_TO_SETTLE_THE_ACCOUNTS_OF_YOUR_QUEST_WHILE_YOUR_INVENTORY_EXCEEDED_80_PERCENT_OF_CAPACITY = 1119;
	public static final int YOU_ARE_STILL_IN_THE_CLAN = 1120;
	public static final int YOU_DO_NOT_HAVE_THE_RIGHT_TO_VOTE = 1121;
	public static final int THERE_IS_NO_CANDIDATE = 1122;
	public static final int WEIGHT_AND_VOLUME_LIMIT_HAS_BEEN_EXCEEDED_THAT_SKILL_IS_CURRENTLY_UNAVAILABLE = 1123;
	public static final int A_RECIPE_BOOK_MAY_NOT_BE_USED_WHILE_USING_A_SKILL = 1124;
	public static final int AN_ITEM_MAY_NOT_BE_CREATED_WHILE_ENGAGED_IN_TRADING = 1125;
	public static final int YOU_MAY_NOT_ENTER_A_NEGATIVE_NUMBER = 1126;
	public static final int THE_REWARD_MUST_BE_LESS_THAN_10_TIMES_THE_STANDARD_PRICE = 1127;
	public static final int A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL = 1128;
	public static final int THIS_IS_NOT_ALLOWED_WHILE_USING_A_FERRY = 1129;
	public static final int YOU_HAVE_GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_THE_SERVITOR = 1130;
	public static final int IT_IS_NOW_MIDNIGHT_AND_THE_EFFECT_OF_S1_CAN_BE_FELT = 1131;
	public static final int IT_IS_DAWN_AND_THE_EFFECT_OF_S1_WILL_NOW_DISAPPEAR = 1132;
	public static final int SINCE_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT = 1133;
	public static final int SINCE_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR = 1134;
	public static final int WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP = 1135;
	public static final int SINCE_THERE_WAS_AN_ACCOUNT_THAT_USED_THIS_IP_AND_ATTEMPTED_TO_LOG_IN_ILLEGALLY_THIS_ACCOUNT_IS_NOT_ALLOWED_TO_CONNECT_TO_THE_GAME_SERVER_FOR_S1_MINUTES_PLEASE_USE_ANOTHER_GAME_SERVER = 1136;
	public static final int S1_HARVESTED_S3_S2_S = 1137;
	public static final int S1_HARVESTED_S2_S = 1138;
	public static final int THE_WEIGHT_AND_VOLUME_LIMIT_OF_INVENTORY_MUST_NOT_BE_EXCEEDED = 1139;
	public static final int WOULD_YOU_LIKE_TO_OPEN_THE_GATE = 1140;
	public static final int WOULD_YOU_LIKE_TO_CLOSE_THE_GATE = 1141;
	public static final int SINCE_S1_ALREADY_EXISTS_NEARBY_YOU_CANNOT_SUMMON_IT_AGAIN = 1142;
	public static final int SINCE_YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_MAINTAIN_THE_SERVITORS_STAY_THE_SERVITOR_WILL_DISAPPEAR = 1143;
	public static final int CURRENTLY_YOU_DONT_HAVE_ANYBODY_TO_CHAT_WITH_IN_THE_GAME = 1144;
	public static final int S2_HAS_BEEN_CREATED_FOR_S1_AFTER_THE_PAYMENT_OF_S3_ADENA_IS_RECEIVED = 1145;
	public static final int S1_CREATED_S2_AFTER_RECEIVING_S3_ADENA = 1146;
	public static final int S2_S3_HAVE_BEEN_CREATED_FOR_S1_AT_THE_PRICE_OF_S4_ADENA = 1147;
	public static final int S1_CREATED_S2_S3_AT_THE_PRICE_OF_S4_ADENA = 1148;
	public static final int THE_ATTEMPT_TO_CREATE_S2_FOR_S1_AT_THE_PRICE_OF_S3_ADENA_HAS_FAILED = 1149;
	public static final int S1_HAS_FAILED_TO_CREATE_S2_AT_THE_PRICE_OF_S3_ADENA = 1150;
	public static final int S2_IS_SOLD_TO_S1_AT_THE_PRICE_OF_S3_ADENA = 1151;
	public static final int S2_S3_HAVE_BEEN_SOLD_TO_S1_FOR_S4_ADENA = 1152;
	public static final int S2_HAS_BEEN_PURCHASED_FROM_S1_AT_THE_PRICE_OF_S3_ADENA = 1153;
	public static final int S3_S2_HAS_BEEN_PURCHASED_FROM_S1_FOR_S4_ADENA = 1154;
	public static final int _S2S3_HAS_BEEN_SOLD_TO_S1_AT_THE_PRICE_OF_S4_ADENA = 1155;
	public static final int _S2S3_HAS_BEEN_PURCHASED_FROM_S1_AT_THE_PRICE_OF_S4_ADENA = 1156;
	public static final int TRYING_ON_STATE_LASTS_FOR_ONLY_5_SECONDS_WHEN_A_CHARACTERS_STATE_CHANGES_IT_CAN_BE_CANCELLED = 1157;
	public static final int YOU_CANNOT_GET_DOWN_FROM_A_PLACE_THAT_IS_TOO_HIGH = 1158;
	public static final int THIS_IS_A_QUEST_EVENT_PERIOD = 1176;
	public static final int THIS_IS_THE_SEAL_VALIDATION_PERIOD = 1177;
	public static final int THIS_SEAL_PERMITS_THE_GROUP_THAT_HOLDS_IT_TO_EXCLUSIVELY_ENTER_THE_DUNGEON_OPENED_BY_THE_SEAL_OF_AVARICE_DURING_THE_SEAL_VALIDATION_PERIOD__IT_ALSO_PERMITS_TRADING_WITH_THE_MERCHANT_OF_MAMMON_WHO_APPEARS_IN_SPECIAL_DUNGEONS_AND_PERMITS_MEETINGS_WITH_ANAKIM_OR_LILITH_IN_THE_DISCIPLES_NECROPOLIS = 1178;
	public static final int THIS_SEAL_PERMITS_THE_GROUP_THAT_HOLDS_IT_TO_ENTER_THE_DUNGEON_OPENED_BY_THE_SEAL_OF_GNOSIS_USE_THE_TELEPORTATION_SERVICE_OFFERED_BY_THE_PRIEST_IN_THE_VILLAGE_AND_DO_BUSINESS_WITH_THE_MERCHANT_OF_MAMMON_THE_ORATOR_OF_REVELATIONS_APPEARS_AND_CASTS_GOOD_MAGIC_ON_THE_WINNERS_AND_THE_PREACHER_OF_DOOM_APPEARS_AND_CASTS_BAD_MAGIC_ON_THE_LOSERS = 1179;
	public static final int DURING_THE_SEAL_VALIDATION_PERIOD_THE_COSTS_OF_CASTLE_DEFENSE_MERCENARIES_AND_RENOVATIONS_BASIC_P_DEF_OF_CASTLE_GATES_AND_CASTLE_WALLS_AND_MAXIMUM_TAX_RATES_WILL_ALL_CHANGE_TO_FAVOR_THE_GROUP_OF_FIGHTERS_THAT_POSSESSES_THIS_SEAL = 1180;
	public static final int DO_YOU_REALLY_WISH_TO_CHANGE_THE_TITLE = 1181;
	public static final int DO_YOU_REALLY_WISH_TO_DELETE_THE_CLAN_CREST = 1182;
	public static final int THIS_IS_THE_INITIAL_PERIOD = 1183;
	public static final int THIS_IS_A_PERIOD_OF_CALCULATIING_STATISTICS_IN_THE_SERVER = 1184;
	public static final int DAYS_LEFT_UNTIL_DELETION = 1185;
	public static final int IF_YOU_HAVE_LOST_YOUR_ACCOUNT_INFORMATION_PLEASE_VISIT_THE_OFFICIAL_LINEAGE_II_SUPPORT_WEBSITE_AT_HTTP__SUPPORTPLAYNCCOM = 1187;
	public static final int THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_IS_IN_EFFECT_IT_WILL_BE_DISSOLVED_WHEN_THE_CASTLE_LORD_IS_REPLACED = 1189;
	public static final int THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_HAS_BEEN_DISSOLVED = 1190;
	public static final int A_MERCENARY_CAN_BE_ASSIGNED_TO_A_POSITION_FROM_THE_BEGINNING_OF_THE_SEAL_VALIDATION_PERIOD_UNTIL_THE_TIME_WHEN_A_SIEGE_STARTS = 1194;
	public static final int THIS_MERCENARY_CANNOT_BE_ASSIGNED_TO_A_POSITION_BY_USING_THE_SEAL_OF_STRIFE = 1195;
	public static final int YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY = 1196;
	public static final int SUMMONING_A_SERVITOR_COSTS_S2_S1 = 1197;
	public static final int THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED = 1198;
	public static final int _CLAN_WAR_TARGET_ = 1199; // =======<CLAN_WAR_TARGET>=======
	public static final int S1_S2_ALLIANCE = 1200;
	public static final int PLEASE_SELECT_THE_QUEST_YOU_WISH_TO_QUIT = 1201;
	public static final int S1_NO_ALLIANCE_EXISTS = 1202;
	public static final int THERE_IS_NO_CLAN_WAR_IN_PROGRESS = 1203;
	public static final int THE_SCREENSHOT_HAS_BEEN_SAVED_S1_S2XS3 = 1204;
	public static final int MAILBOX_IS_FULL100_MESSAGE_MAXIMUM = 1205;
	public static final int MEMO_BOX_IS_FULL_100_MEMO_MAXIMUM = 1206;
	public static final int PLEASE_MAKE_AN_ENTRY_IN_THE_FIELD = 1207;
	public static final int S1_DIED_AND_DROPPED_S3_S2 = 1208;
	public static final int CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL = 1209;
	public static final int SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_BEGUN_VISIT_A_PRIEST_OF_DAWN_OR_DUSK_TO_PARTICIPATE_IN_THE_EVENT = 1210;
	public static final int SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_ENDED_THE_NEXT_QUEST_EVENT_WILL_START_IN_ONE_WEEK = 1211;
	public static final int SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_AVARICE = 1212;
	public static final int SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_GNOSIS = 1213;
	public static final int SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_STRIFE = 1214;
	public static final int SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_AVARICE = 1215;
	public static final int SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_GNOSIS = 1216;
	public static final int SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_STRIFE = 1217;
	public static final int SEVEN_SIGNS_THE_SEAL_VALIDATION_PERIOD_HAS_BEGUN = 1218;
	public static final int SEVEN_SIGNS_THE_SEAL_VALIDATION_PERIOD_HAS_ENDED = 1219;
	public static final int ARE_YOU_SURE_YOU_WISH_TO_SUMMON_IT = 1220;
	public static final int DO_YOU_REALLY_WISH_TO_RETURN_IT = 1221;
	public static final int CURRENT_LOCATION_S1_S2_S3_GM_CONSULTATION_SERVICE = 1222;
	public static final int WE_DEPART_FOR_TALKING_ISLAND_IN_FIVE_MINUTES = 1223;
	public static final int WE_DEPART_FOR_TALKING_ISLAND_IN_ONE_MINUTE = 1224;
	public static final int ALL_ABOARD_FOR_TALKING_ISLAND = 1225;
	public static final int WE_ARE_NOW_LEAVING_FOR_TALKING_ISLAND = 1226;
	public static final int YOU_HAVE_S1_UNREAD_MESSAGES = 1227;
	public static final int S1_HAS_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL_TO_S1_ = 1228;
	public static final int NO_MORE_MESSAGES_MAY_BE_SENT_AT_THIS_TIME_EACH_ACCOUNT_IS_ALLOWED_10_MESSAGES_PER_DAY = 1229;
	public static final int YOU_ARE_LIMITED_TO_FIVE_RECIPIENTS_AT_A_TIME = 1230;
	public static final int YOUVE_SENT_MAIL = 1231;
	public static final int THE_MESSAGE_WAS_NOT_SENT = 1232;
	public static final int YOUVE_GOT_MAIL = 1233;
	public static final int THE_MAIL_HAS_BEEN_STORED_IN_YOUR_TEMPORARY_MAILBOX = 1234;
	public static final int DO_YOU_WISH_TO_DELETE_ALL_YOUR_FRIENDS = 1235;
	public static final int PLEASE_ENTER_SECURITY_CARD_NUMBER = 1236;
	public static final int PLEASE_ENTER_THE_CARD_NUMBER_FOR_NUMBER_S1 = 1237;
	public static final int YOUR_TEMPORARY_MAILBOX_IS_FULL_NO_MORE_MAIL_CAN_BE_STORED_10_MESSAGE_LIMIT = 1238;
	public static final int LOADING_OF_THE_KEYBOARD_SECURITY_MODULE_HAS_FAILED_PLEASE_EXIT_THE_GAME_AND_RELOAD = 1239;
	public static final int SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_WON = 1240;
	public static final int SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_WON = 1241;
	public static final int USERS_WHO_HAVE_NOT_VERIFIED_THEIR_AGE_CANNOT_LOG_IN_BETWEEN_1000_PM_AND_600_AM = 1242;
	public static final int THE_SECURITY_CARD_NUMBER_IS_INVALID = 1243;
	public static final int USERS_WHO_HAVE_NOT_VERIFIED_THEIR_AGE_CANNOT_LOG_IN_BETWEEN_1000_PM_AND_600_AM_LOGGING_OFF = 1244;
	public static final int YOU_WILL_BE_LOGGED_OUT_IN_S1_MINUTES = 1245;
	public static final int S1_DIED_AND_HAS_DROPPED_S2_ADENA = 1246;
	public static final int THE_CORPSE_IS_TOO_OLD_THE_SKILL_CANNOT_BE_USED = 1247;
	public static final int YOU_ARE_OUT_OF_FEED_MOUNT_STATUS_CANCELED = 1248;
	public static final int YOU_MAY_ONLY_RIDE_A_WYVERN_WHILE_YOURE_RIDING_A_STRIDER = 1249;
	public static final int DO_YOU_REALLY_WANT_TO_SURRENDER_IF_YOU_SURRENDER_DURING_AN_ALLIANCE_WAR_YOUR_EXP_WILL_DROP_AS_MUCH_AS_WHEN_YOUR_CHARACTER_DIES_ONCE = 1250;
	public static final int ARE_YOU_SURE_YOU_WANT_TO_DISMISS_THE_ALLIANCE_IF_YOU_USE_THE__ALLYDISMISS_COMMAND_YOU_WILL_NOT_BE_ABLE_TO_ACCEPT_ANOTHER_CLAN_TO_YOUR_ALLIANCE_FOR_ONE_DAY = 1251;
	public static final int ARE_YOU_SURE_YOU_WANT_TO_SURRENDER_EXP_PENALTY_WILL_BE_THE_SAME_AS_DEATH = 1252;
	public static final int ARE_YOU_SURE_YOU_WANT_TO_SURRENDER_EXP_PENALTY_WILL_BE_THE_SAME_AS_DEATH_AND_YOU_WILL_NOT_BE_ALLOWED_TO_PARTICIPATE_IN_CLAN_WAR = 1253;
	public static final int THANK_YOU_FOR_SUBMITTING_FEEDBACK = 1254;
	public static final int GM_CONSULTATION_HAS_BEGUN = 1255;
	public static final int PLEASE_WRITE_THE_NAME_AFTER_THE_COMMAND = 1256;
	public static final int THE_SPECIAL_SKILL_OF_A_SERVITOR_OR_PET_CANNOT_BE_REGISTERED_AS_A_MACRO = 1257;
	public static final int S1_HAS_BEEN_CRYSTALLIZED = 1258;
	public static final int _ALLIANCE_TARGET_ = 1259; // =======<ALLIANCE_TARGET>=======
	public static final int SEVEN_SIGNS_PREPARATIONS_HAVE_BEGUN_FOR_THE_NEXT_QUEST_EVENT = 1260;
	public static final int SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_BEGUN_SPEAK_WITH_A_PRIEST_OF_DAWN_OR_DUSK_PRIESTESS_IF_YOU_WISH_TO_PARTICIPATE_IN_THE_EVENT = 1261;
	public static final int SEVEN_SIGNS_QUEST_EVENT_HAS_ENDED_RESULTS_ARE_BEING_TALLIED = 1262;
	public static final int SEVEN_SIGNS_THIS_IS_THE_SEAL_VALIDATION_PERIOD_A_NEW_QUEST_EVENT_PERIOD_BEGINS_NEXT_MONDAY = 1263;
	public static final int THIS_SOUL_STONE_CANNOT_CURRENTLY_ABSORB_SOULS_ABSORPTION_HAS_FAILED = 1264;
	public static final int YOU_CANT_ABSORB_SOULS_WITHOUT_A_SOUL_STONE = 1265;
	public static final int THE_EXCHANGE_HAS_ENDED = 1266;
	public static final int YOUR_CONTRIBUTION_SCORE_IS_INCREASED_BY_S1 = 1267;
	public static final int DO_YOU_WISH_TO_ADD_S1_CLASS_AS_YOUR_SUB_CLASS = 1268;
	public static final int THE_NEW_SUB_CLASS_HAS_BEEN_ADDED = 1269;
	public static final int THE_TRANSFER_OF_SUB_CLASS_HAS_BEEN_COMPLETED = 1270;
	public static final int DO_YOU_WISH_TO_PARTICIPATE_UNTIL_THE_NEXT_SEAL_VALIDATION_PERIOD_YOU_ARE_A_MEMBER_OF_THE_LORDS_OF_DAWN = 1271;
	public static final int DO_YOU_WISH_TO_PARTICIPATE_UNTIL_THE_NEXT_SEAL_VALIDATION_PERIOD_YOU_ARE_A_MEMBER_OF_THE_REVOLUTIONARIES_OF_DUSK = 1272;
	public static final int YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_LORDS_OF_DAWN = 1273;
	public static final int YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_REVOLUTIONARIES_OF_DUSK = 1274;
	public static final int YOUVE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_AVARICE_DURING_THIS_QUEST_EVENT_PERIOD = 1275;
	public static final int YOUVE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_GNOSIS_DURING_THIS_QUEST_EVENT_PERIOD = 1276;
	public static final int YOUVE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_STRIFE_DURING_THIS_QUEST_EVENT_PERIOD = 1277;
	public static final int THE_NPC_SERVER_IS_NOT_OPERATING = 1278;
	public static final int CONTRIBUTION_LEVEL_HAS_EXCEEDED_THE_LIMIT_YOU_MAY_NOT_CONTINUE = 1279;
	public static final int MAGIC_CRITICAL_HIT = 1280;
	public static final int YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS = 1281;
	public static final int YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1 = 1282;
	public static final int THE_MINIMUM_FRAME_OPTION_HAS_BEEN_ACTIVATED = 1283;
	public static final int THE_MINIMUM_FRAME_OPTION_HAS_BEEN_DEACTIVATED = 1284;
	public static final int NO_INVENTORY_EXISTS_YOU_CANNOT_PURCHASE_AN_ITEM = 1285;
	public static final int UNTIL_NEXT_MONDAY_AT_120_AM = 1286;
	public static final int UNTIL_TODAY_AT_120_AM = 1287;
	public static final int IF_TRENDS_CONTINUE_S1_WILL_WIN_AND_THE_SEAL_WILL_BELONG_TO = 1288;
	public static final int SINCE_THE_SEAL_WAS_OWNED_DURING_THE_PREVIOUS_PERIOD_AND_10_PERCENT_OR_MORE_PEOPLE_HAVE_VOTED = 1289;
	public static final int ALTHOUGH_THE_SEAL_WAS_NOT_OWNED_SINCE_35_PERCENT_OR_MORE_PEOPLE_HAVE_VOTED = 1290;
	public static final int ALTHOUGH_THE_SEAL_WAS_OWNED_DURING_THE_PREVIOUS_PERIOD_BECAUSE_LESS_THAN_10_PERCENT_OF_PEOPLE_HAVE_VOTED = 1291;
	public static final int SINCE_THE_SEAL_WAS_NOT_OWNED_DURING_THE_PREVIOUS_PERIOD_AND_SINCE_LESS_THAN_35_PERCENT_OF_PEOPLE_HAVE_VOTED = 1292;
	public static final int IF_CURRENT_TRENDS_CONTINUE_IT_WILL_END_IN_A_TIE = 1293;
	public static final int SINCE_THE_COMPETITION_HAS_ENDED_IN_A_TIE_THE_SEAL_WILL_NOT_BE_AWARDED = 1294;
	public static final int SUB_CLASSES_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SKILL_IS_IN_USE = 1295;
	public static final int A_PRIVATE_STORE_MAY_NOT_BE_OPENED_IN_THIS_AREA = 1296;
	public static final int A_PRIVATE_WORKSHOP_MAY_NOT_BE_OPENED_IN_THIS_AREA = 1297;
	public static final int EXITING_THE_MONSTER_RACE_TRACK = 1298;
	public static final int S1S_CASTING_HAS_BEEN_INTERRUPTED = 1299;
	public static final int TRYING_ON_MODE_CANCELED = 1300;
	public static final int CAN_BE_USED_ONLY_BY_THE_LORDS_OF_DAWN = 1301;
	public static final int CAN_BE_USED_ONLY_BY_THE_REVOLUTIONARIES_OF_DUSK = 1302;
	public static final int USED_ONLY_DURING_A_QUEST_EVENT_PERIOD = 1303;
	public static final int DUE_TO_THE_INFLUENCE_OF_THE_SEAL_OF_STRIFE_ALL_DEFENSIVE_REGISTRATION_HAS_BEEN_CANCELED_EXCEPT_BY_ALLIANCES_OF_CASTLE_OWNING_CLANS = 1304;
	public static final int YOU_MAY_GIVE_SOMEONE_ELSE_A_SEAL_STONE_FOR_SAFEKEEPING_ONLY_DURING_A_QUEST_EVENT_PERIOD = 1305;
	public static final int TRYING_ON_MODE_HAS_ENDED = 1306;
	public static final int ACCOUNTS_MAY_ONLY_BE_SETTLED_DURING_THE_SEAL_VALIDATION_PERIOD = 1307;
	public static final int CONGRATULATIONS_YOU_HAVE_TRANSFERRED_TO_A_NEW_CLASS = 1308;
	public static final int THIS_OPTION_REQUIRES_THAT_THE_LATEST_VERSION_OF_MSN_MESSENGER_CLIENT_BE_INSTALLED_ON_YOUR_COMPUTER = 1309;
	public static final int FOR_FULL_FUNCTIONALITY_THE_LATEST_VERSION_OF_MSN_MESSENGER_CLIENT_MUST_BE_INSTALLED_ON_THE_USERS_COMPUTER = 1310;
	public static final int PREVIOUS_VERSIONS_OF_MSN_MESSENGER_ONLY_PROVIDE_THE_BASIC_FEATURES_TO_CHAT_IN_THE_GAME_ADD_DELETE_CONTACTS_AND_OTHER_OPTIONS_ARENT_AVAILABLE = 1311;
	public static final int THE_LATEST_VERSION_OF_MSN_MESSENGER_MAY_BE_OBTAINED_FROM_THE_MSN_WEB_SITE_ = 1312;
	public static final int S1_TO_BETTER_SERVE_OUR_CUSTOMERS_ALL_CHAT_HISTORIES_ARE_STORED_AND_MAINTAINED_BY_NCSOFT_IF_YOU_DO_NOT_AGREE_TO_HAVE_YOUR_CHAT_RECORDS_STORED_CLOSE_THE_CHAT_WINDOW_NOW_FOR_MORE_INFORMATION_REGARDING_THIS_ISSUE_PLEASE_VISIT_OUR_HOME_PAGE_AT_WWWNCSOFTCOM = 1313;
	public static final int PLEASE_ENTER_THE_PASSPORT_ID_OF_THE_PERSON_YOU_WISH_TO_ADD_TO_YOUR_CONTACT_LIST = 1314;
	public static final int DELETING_A_CONTACT_WILL_REMOVE_THAT_CONTACT_FROM_MSN_MESSENGER_AS_WELL_THE_CONTACT_CAN_STILL_CHECK_YOUR_ONLINE_STATUS_AND_WILL_NOT_BE_BLOCKED_FROM_SENDING_YOU_A_MESSAGE = 1315;
	public static final int THE_CONTACT_WILL_BE_DELETED_AND_BLOCKED_FROM_YOUR_CONTACT_LIST = 1316;
	public static final int WOULD_YOU_LIKE_TO_DELETE_THIS_CONTACT = 1317;
	public static final int PLEASE_SELECT_THE_CONTACT_YOU_WANT_TO_BLOCK_OR_UNBLOCK = 1318;
	public static final int PLEASE_SELECT_THE_NAME_OF_THE_CONTACT_YOU_WISH_TO_CHANGE_TO_ANOTHER_GROUP = 1319;
	public static final int AFTER_SELECTING_THE_GROUP_YOU_WISH_TO_MOVE_YOUR_CONTACT_TO_PRESS_THE_OK_BUTTON = 1320;
	public static final int ENTER_THE_NAME_OF_THE_GROUP_YOU_WISH_TO_ADD = 1321;
	public static final int SELECT_THE_GROUP_AND_ENTER_THE_NEW_NAME = 1322;
	public static final int SELECT_THE_GROUP_YOU_WISH_TO_DELETE_AND_CLICK_THE_OK_BUTTON = 1323;
	public static final int SIGNING_IN = 1324;
	public static final int YOUVE_LOGGED_INTO_ANOTHER_COMPUTER_AND_BEEN_LOGGED_OUT_OF_THE_NET_MESSENGER_SERVICE_ON_THIS_COMPUTER = 1325;
	public static final int THE_FOLLOWING_MESSAGE_COULD_NOT_BE_DELIVERED = 1327;
	public static final int MEMBERS_OF_THE_REVOLUTIONARIES_OF_DUSK_WILL_NOT_BE_RESURRECTED = 1328;
	public static final int YOU_ARE_CURRENTLY_BANNED_FROM_ACTIVITIES_RELATED_TO_THE_PRIVATE_STORE_AND_PRIVATE_WORKSHOP = 1329;
	public static final int NO_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_MAY_BE_OPENED_FOR_S1_MINUTES = 1330;
	public static final int ACTIVITIES_RELATED_TO_THE_PRIVATE_STORE_AND_PRIVATE_WORKSHOP_ARE_NOW_PERMITTED = 1331;
	public static final int ITEMS_MAY_NOT_BE_USED_AFTER_YOUR_CHARACTER_OR_PET_DIES = 1332;
	public static final int REPLAY_FILE_ISNT_ACCESSIBLE_VERIFY_THAT_REPLAYINI_FILE_EXISTS = 1333;
	public static final int THE_NEW_CAMERA_DATA_HAS_BEEN_STORED = 1334;
	public static final int THE_ATTEMPT_TO_STORE_THE_NEW_CAMERA_DATA_HAS_FAILED = 1335;
	public static final int THE_REPLAY_FILE_HAS_BEEN_CORRUPTED_PLEASE_CHECK_THE_S1S2_FILE = 1336;
	public static final int REPLAY_MODE_WILL_BE_TERMINATED_DO_YOU_WISH_TO_CONTINUE = 1337;
	public static final int YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_TRANSFERRED_AT_ONE_TIME = 1338;
	public static final int ONCE_A_MACRO_IS_ASSIGNED_TO_A_SHORTCUT_IT_CANNOT_BE_RUN_AS_A_MACRO_AGAIN = 1339;
	public static final int THIS_SERVER_CANNOT_BE_ACCESSED_BY_THE_COUPON_YOU_ARE_USING = 1340;
	public static final int THE_NAME_OR_E_MAIL_ADDRESS_YOU_ENTERED_IS_INCORRECT = 1341;
	public static final int YOU_ARE_ALREADY_LOGGED_IN = 1342;
	public static final int THE_PASSWORD_OR_E_MAIL_ADDRESS_YOU_ENTERED_IS_INCORRECT__YOUR_ATTEMPT_TO_LOG_INTO_NET_MESSENGER_SERVICE_HAS_FAILED = 1343;
	public static final int THE_SERVICE_YOU_REQUESTED_COULD_NOT_BE_LOCATED_AND_THEREFORE_YOUR_ATTEMPT_TO_LOG_INTO_THE_NET_MESSENGER_SERVICE_HAS_FAILED_PLEASE_VERIFY_THAT_YOU_ARE_CURRENTLY_CONNECTED_TO_THE_INTERNET = 1344;
	public static final int AFTER_SELECTING_A_CONTACT_NAME_CLICK_ON_THE_OK_BUTTON = 1345;
	public static final int YOU_ARE_CURRENTLY_ENTERING_A_CHAT_MESSAGE = 1346;
	public static final int THE_LINEAGE_II_MESSENGER_COULD_NOT_CARRY_OUT_THE_TASK_YOU_REQUESTED = 1347;
	public static final int S1_HAS_ENTERED_THE_CHAT_ROOM = 1348;
	public static final int S1_HAS_LEFT_THE_CHAT_ROOM = 1349;
	public static final int AFTER_SELECTING_THE_CONTACT_YOU_WANT_TO_DELETE_CLICK_THE_DELETE_BUTTON = 1351;
	public static final int YOU_HAVE_BEEN_ADDED_TO_THE_CONTACT_LIST_OF_S1_S2 = 1352;
	public static final int YOU_CAN_SET_THE_OPTION_TO_SHOW_YOUR_STATUS_AS_ALWAYS_BEING_OFF_LINE_TO_ALL_OF_YOUR_CONTACTS = 1353;
	public static final int YOU_ARE_NOT_ALLOWED_TO_CHAT_WITH_YOUR_CONTACT_WHILE_YOU_ARE_BLOCKED_FROM_CHATTING = 1354;
	public static final int THE_CONTACT_YOU_CHOSE_TO_CHAT_WITH_IS_CURRENTLY_BLOCKED_FROM_CHATTING = 1355;
	public static final int THE_CONTACT_YOU_CHOSE_TO_CHAT_WITH_IS_NOT_CURRENTLY_LOGGED_IN = 1356;
	public static final int YOU_HAVE_BEEN_BLOCKED_FROM_THE_CONTACT_YOU_SELECTED = 1357;
	public static final int YOU_ARE_BEING_LOGGED_OUT = 1358;
	public static final int YOU_HAVE_RECEIVED_A_MESSAGE_FROM_S1 = 1360;
	public static final int DUE_TO_A_SYSTEM_ERROR_YOU_HAVE_BEEN_LOGGED_OUT_OF_THE_NET_MESSENGER_SERVICE = 1361;
	public static final int PLEASE_SELECT_THE_CONTACT_YOU_WISH_TO_DELETE__IF_YOU_WOULD_LIKE_TO_DELETE_A_GROUP_CLICK_THE_BUTTON_NEXT_TO_MY_STATUS_AND_THEN_USE_THE_OPTIONS_MENU = 1362;
	public static final int YOUR_REQUEST_TO_PARTICIPATE_IN_THE_ALLIANCE_WAR_HAS_BEEN_DENIED = 1363;
	public static final int THE_REQUEST_FOR_AN_ALLIANCE_WAR_HAS_BEEN_REJECTED = 1364;
	public static final int S2_OF_S1_CLAN_HAS_SURRENDERED_AS_AN_INDIVIDUAL = 1365;
	public static final int YOU_CAN_DELETE_A_GROUP_ONLY_WHEN_YOU_DO_NOT_HAVE_ANY_CONTACT_IN_THAT_GROUP__IN_ORDER_TO_DELETE_A_GROUP_FIRST_TRANSFER_YOUR_CONTACT_S_IN_THAT_GROUP_TO_ANOTHER_GROUP = 1366;
	public static final int ONLY_MEMBERS_OF_THE_GROUP_ARE_ALLOWED_TO_ADD_RECORDS = 1367;
	public static final int THOSE_ITEMS_MAY_NOT_BE_TRIED_ON_SIMULTANEOUSLY = 1368;
	public static final int YOUVE_EXCEEDED_THE_MAXIMUM = 1369;
	public static final int YOU_CANNOT_SEND_MAIL_TO_A_GM_SUCH_AS_S1 = 1370;
	public static final int IT_HAS_BEEN_DETERMINED_THAT_YOURE_NOT_ENGAGED_IN_NORMAL_GAMEPLAY_AND_A_RESTRICTION_HAS_BEEN_IMPOSED_UPON_YOU_YOU_MAY_NOT_MOVE_FOR_S1_MINUTES = 1371;
	public static final int YOUR_PUNISHMENT_WILL_CONTINUE_FOR_S1_MINUTES = 1372;
	public static final int S1_HAS_PICKED_UP_S2_THAT_WAS_DROPPED_BY_A_RAID_BOSS = 1373;
	public static final int S1_HAS_PICKED_UP_S3_S2_S_THAT_WAS_DROPPED_BY_A_RAID_BOSS = 1374;
	public static final int S1_HAS_PICKED_UP__S2_ADENA_THAT_WAS_DROPPED_BY_A_RAID_BOSS = 1375;
	public static final int S1_HAS_PICKED_UP_S2_THAT_WAS_DROPPED_BY_ANOTHER_CHARACTER = 1376;
	public static final int S1_HAS_PICKED_UP_S3_S2_S_THAT_WAS_DROPPED_BY_ANOTHER_CHARACTER = 1377;
	public static final int S1_HAS_PICKED_UP__S3S2_THAT_WAS_DROPPED_BY_ANOTHER_CHARACTER = 1378;
	public static final int S1_HAS_OBTAINED_S2_ADENA = 1379;
	public static final int YOU_CANT_SUMMON_A_S1_WHILE_ON_THE_BATTLEGROUND = 1380;
	public static final int THE_PARTY_LEADER_HAS_OBTAINED_S2_OF_S1 = 1381;
	public static final int ARE_YOU_SURE_YOU_WANT_TO_CHOOSE_THIS_WEAPON_TO_FULFILL_THE_QUEST_YOU_MUST_BRING_THE_CHOSEN_WEAPON = 1382;
	public static final int ARE_YOU_SURE_YOU_WANT_TO_EXCHANGE = 1383;
	public static final int S1_HAS_BECOME_A_PARTY_LEADER = 1384;
	public static final int YOU_ARE_NOT_ALLOWED_TO_DISMOUNT_AT_THIS_LOCATION = 1385;
	public static final int HOLD_STATE_HAS_BEEN_LIFTED = 1386;
	public static final int PLEASE_SELECT_THE_ITEM_YOU_WOULD_LIKE_TO_TRY_ON = 1387;
	public static final int A_PARTY_ROOM_HAS_BEEN_CREATED = 1388;
	public static final int THE_PARTY_ROOMS_INFORMATION_HAS_BEEN_REVISED = 1389;
	public static final int YOU_ARE_NOT_ALLOWED_TO_ENTER_THE_PARTY_ROOM = 1390;
	public static final int YOU_HAVE_EXITED_FROM_THE_PARTY_ROOM = 1391;
	public static final int S1_HAS_LEFT_THE_PARTY_ROOM = 1392;
	public static final int YOU_HAVE_BEEN_OUSTED_FROM_THE_PARTY_ROOM = 1393;
	public static final int S1_HAS_BEEN_OUSTED_FROM_THE_PARTY_ROOM = 1394;
	public static final int THE_PARTY_ROOM_HAS_BEEN_DISBANDED = 1395;
	public static final int THE_LIST_OF_PARTY_ROOMS_CAN_BE_VIEWED_BY_A_PERSON_WHO_HAS_NOT_JOINED_A_PARTY_OR_WHO_IS_A_PARTY_LEADER = 1396;
	public static final int THE_LEADER_OF_THE_PARTY_ROOM_HAS_CHANGED = 1397;
	public static final int WE_ARE_RECRUITING_PARTY_MEMBERS = 1398;
	public static final int ONLY_A_PARTY_LEADER_CAN_TRANSFER_ONES_RIGHTS_TO_ANOTHER_PLAYER = 1399;
	public static final int PLEASE_SELECT_THE_PERSON_YOU_WISH_TO_MAKE_THE_PARTY_LEADER = 1400;
	public static final int YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF = 1401;
	public static final int YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER = 1402;
	public static final int YOU_HAVE_FAILED_TO_TRANSFER_THE_PARTY_LEADER_RIGHTS = 1403;
	public static final int THE_OWNER_OF_THE_PRIVATE_MANUFACTURING_STORE_HAS_CHANGED_THE_PRICE_FOR_CREATING_THIS_ITEM__PLEASE_CHECK_THE_NEW_PRICE_BEFORE_TRYING_AGAIN = 1404;
	public static final int S1_CPS_WILL_BE_RESTORED = 1405;
	public static final int S1_WILL_RESTORE_S2S_CP = 1406;
	public static final int YOU_ARE_USING_A_COMPUTER_THAT_DOES_NOT_ALLOW_YOU_TO_LOG_IN_WITH_TWO_ACCOUNTS_AT_THE_SAME_TIME = 1407;
	public static final int YOUR_PREPAID_REMAINING_USAGE_TIME_IS_S1_HOURS_AND_S2_MINUTES__YOU_HAVE_S3_PAID_RESERVATIONS_LEFT = 1408;
	public static final int YOUR_PREPAID_USAGE_TIME_HAS_EXPIRED_YOUR_NEW_PREPAID_RESERVATION_WILL_BE_USED_THE_REMAINING_USAGE_TIME_IS_S1_HOURS_AND_S2_MINUTES = 1409;
	public static final int YOUR_PREPAID_USAGE_TIME_HAS_EXPIRED_YOU_DO_NOT_HAVE_ANY_MORE_PREPAID_RESERVATIONS_LEFT = 1410;
	public static final int THE_NUMBER_OF_YOUR_PREPAID_RESERVATIONS_HAS_CHANGED = 1411;
	public static final int YOUR_PREPAID_USAGE_TIME_HAS_S1_MINUTES_LEFT = 1412;
	public static final int SINCE_YOU_DO_NOT_MEET_THE_REQUIREMENTS_YOU_ARE_NOT_ALLOWED_TO_ENTER_THE_PARTY_ROOM = 1413;
	public static final int THE_WIDTH_AND_LENGTH_SHOULD_BE_100_OR_MORE_GRIDS_AND_LESS_THAN_5000_GRIDS_RESPECTIVELY = 1414;
	public static final int THE_COMMAND_FILE_IS_NOT_SET = 1415;
	public static final int THE_PARTY_REPRESENTATIVE_OF_TEAM_1_HAS_NOT_BEEN_SELECTED = 1416;
	public static final int THE_PARTY_REPRESENTATIVE_OF_TEAM_2_HAS_NOT_BEEN_SELECTED = 1417;
	public static final int THE_NAME_OF_TEAM_1_HAS_NOT_YET_BEEN_CHOSEN = 1418;
	public static final int THE_NAME_OF_TEAM_2_HAS_NOT_YET_BEEN_CHOSEN = 1419;
	public static final int THE_NAME_OF_TEAM_1_AND_THE_NAME_OF_TEAM_2_ARE_IDENTICAL = 1420;
	public static final int THE_RACE_SETUP_FILE_HAS_NOT_BEEN_DESIGNATED = 1421;
	public static final int RACE_SETUP_FILE_ERROR__BUFFCNT_IS_NOT_SPECIFIED = 1422;
	public static final int RACE_SETUP_FILE_ERROR__BUFFIDS1_IS_NOT_SPECIFIED = 1423;
	public static final int RACE_SETUP_FILE_ERROR__BUFFLVS1_IS_NOT_SPECIFIED = 1424;
	public static final int RACE_SETUP_FILE_ERROR__DEFAULTALLOW_IS_NOT_SPECIFIED = 1425;
	public static final int RACE_SETUP_FILE_ERROR__EXPSKILLCNT_IS_NOT_SPECIFIED = 1426;
	public static final int RACE_SETUP_FILE_ERROR__EXPSKILLIDS1_IS_NOT_SPECIFIED = 1427;
	public static final int RACE_SETUP_FILE_ERROR__EXPITEMCNT_IS_NOT_SPECIFIED = 1428;
	public static final int RACE_SETUP_FILE_ERROR__EXPITEMIDS1_IS_NOT_SPECIFIED = 1429;
	public static final int RACE_SETUP_FILE_ERROR__TELEPORTDELAY_IS_NOT_SPECIFIED = 1430;
	public static final int THE_RACE_WILL_BE_STOPPED_TEMPORARILY = 1431;
	public static final int YOUR_OPPONENT_IS_CURRENTLY_IN_A_PETRIFIED_STATE = 1432;
	public static final int THE_USE_OF_S1_WILL_NOW_BE_AUTOMATED = 1433;
	public static final int THE_AUTOMATIC_USE_OF_S1_WILL_NOW_BE_CANCELLED = 1434;
	public static final int DUE_TO_INSUFFICIENT_S1_THE_AUTOMATIC_USE_FUNCTION_HAS_BEEN_CANCELLED = 1435;
	public static final int DUE_TO_INSUFFICIENT_S1_THE_AUTOMATIC_USE_FUNCTION_CANNOT_BE_ACTIVATED = 1436;
	public static final int PLAYERS_ARE_NO_LONGER_ALLOWED_TO_PLACE_DICE_DICE_CANNOT_BE_PURCHASED_FROM_A_VILLAGE_STORE_ANY_MORE_HOWEVER_YOU_CAN_STILL_SELL_THEM_TO_A_STORE_IN_A_VILLAGE = 1437;

	public static final int THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT = 1438;
	public static final int ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT = 1439;
	public static final int SUCCEEDED_IN_ENCHANTING_SKILL_S1 = 1440;
	public static final int FAILED_IN_ENCHANTING_SKILL_S1 = 1441;
	public static final int SP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT = 1443;
	public static final int EXP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT = 1444;
	public static final int YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_UNTRAIN_THE_ENCHANT_SKILL = 2068;

	public static final int Untrain_of_enchant_skill_was_successful_Current_level_of_enchant_skill_S1_has_been_decreased_by_1 = 2069;
	public static final int Untrain_of_enchant_skill_was_successful_Current_level_of_enchant_skill_S1_became_0_and_enchant_skill_will_be_initialized = 2070;
	public static final int You_do_not_have_all_of_the_items_needed_to_enchant_skill_route_change = 2071;
	public static final int Enchant_skill_route_change_was_successful_Lv_of_enchant_skill_S1_has_been_decreased_by_S2 = 2072;
	public static final int Enchant_skill_route_change_was_successful_Lv_of_enchant_skill_S1_will_remain = 2073;
	public static final int Skill_enchant_failed_Current_level_of_enchant_skill_S1_will_remain_unchanged = 2074;

	public static final int REMAINING_TIME_S1_SECOND = 1442;
	public static final int YOUR_PREVIOUS_SUB_CLASS_WILL_BE_DELETED_AND_YOUR_NEW_SUB_CLASS_WILL_START_AT_LEVEL_40__DO_YOU_WISH_TO_PROCEED = 1445;
	public static final int THE_FERRY_FROM_S1_TO_S2_HAS_BEEN_DELAYED = 1446;
	public static final int OTHER_SKILLS_ARE_NOT_AVAILABLE_WHILE_FISHING = 1447;
	public static final int ONLY_FISHING_SKILLS_ARE_AVAILABLE = 1448;
	public static final int SUCCEEDED_IN_GETTING_A_BITE = 1449;
	public static final int TIME_IS_UP_SO_THAT_FISH_GOT_AWAY = 1450;
	public static final int THE_FISH_GOT_AWAY = 1451;
	public static final int BAITS_HAVE_BEEN_LOST_BECAUSE_THE_FISH_GOT_AWAY = 1452;
	public static final int FISHING_POLES_ARE_NOT_INSTALLED = 1453;
	public static final int BAITS_ARE_NOT_PUT_ON_A_HOOK = 1454;
	public static final int YOU_CANT_FISH_IN_WATER = 1455;
	public static final int YOU_CANT_FISH_WHILE_YOU_ARE_ON_BOARD = 1456;
	public static final int YOU_CANT_FISH_HERE = 1457;
	public static final int CANCELS_FISHING = 1458;
	public static final int NOT_ENOUGH_BAIT = 1459;
	public static final int ENDS_FISHING = 1460;
	public static final int STARTS_FISHING = 1461;
	public static final int PUMPING_SKILL_IS_AVAILABLE_ONLY_WHILE_FISHING = 1462;
	public static final int REELING_SKILL_IS_AVAILABLE_ONLY_WHILE_FISHING = 1463;
	public static final int FISH_HAS_RESISTED = 1464;
	public static final int PUMPING_IS_SUCCESSFUL_DAMAGE_S1 = 1465;
	public static final int PUMPING_FAILED_DAMAGE_S1 = 1466;
	public static final int REELING_IS_SUCCESSFUL_DAMAGE_S1 = 1467;
	public static final int REELING_FAILED_DAMAGE_S1 = 1468;
	public static final int SUCCEEDED_IN_FISHING = 1469;
	public static final int YOU_CANNOT_DO_THAT_WHILE_FISHING = 1470;
	public static final int YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING = 1471;
	public static final int YOU_CANT_MAKE_AN_ATTACK_WITH_A_FISHING_POLE = 1472;
	public static final int S1_IS_NOT_SUFFICIENT = 1473;
	public static final int S1_IS_NOT_AVAILABLE = 1474;
	public static final int PET_HAS_DROPPED_S1 = 1475;
	public static final int PET_HAS_DROPPED__S1S2 = 1476;
	public static final int PET_HAS_DROPPED_S2_OF_S1 = 1477;
	public static final int YOU_CAN_REGISTER_ONLY_256_COLOR_BMP_FILES_WITH_A_SIZE_OF_64X64 = 1478;
	public static final int THIS_FISHING_SHOT_IS_NOT_FIT_FOR_THE_FISHING_POLE_CRYSTAL = 1479;
	public static final int DO_YOU_WANT_TO_CANCEL_YOUR_APPLICATION_FOR_JOINING_THE_GRAND_OLYMPIAD = 1480;
	public static final int YOU_HAVE_BEEN_SELECTED_FOR_NO_CLASS_GAME_DO_YOU_WANT_TO_JOIN = 1481;
	public static final int YOU_HAVE_BEEN_SELECTED_FOR_CLASSIFIED_GAME_DO_YOU_WANT_TO_JOIN = 1482;
	public static final int DO_YOU_WANT_TO_BECOME_A_HERO_NOW = 1483;
	public static final int DO_YOU_WANT_TO_USE_THE_HEROES_WEAPON_THAT_YOU_CHOSE = 1484;
	public static final int TRADED_S2_OF_CROP_S1 = 1490;
	public static final int FAILED_IN_TRADING_S2_OF_CROP_S1 = 1491;
	public static final int YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S = 1492;
	public static final int THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME = 1493;
	public static final int THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME = 1494;
	public static final int THE_GAME_WILL_START_IN_S1_SECOND_S = 1495;
	public static final int STARTS_THE_GAME = 1496;
	public static final int S1_HAS_WON_THE_GAME = 1497;
	public static final int THE_GAME_ENDED_IN_A_TIE = 1498;
	public static final int YOU_WILL_GO_BACK_TO_THE_VILLAGE_IN_S1_SECOND_S = 1499;
	public static final int YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER = 1500;
	public static final int ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD = 1501;
	public static final int YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT = 1502;
	public static final int YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES = 1503;
	public static final int YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES = 1504;
	public static final int YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME = 1505;
	public static final int YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME = 1506;
	public static final int THIS_ITEM_CANT_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT = 1507;
	public static final int THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT = 1508;
	public static final int THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT = 1509;
	public static final int S1_IS_MAKING_AN_ATTEMPT_AT_RESURRECTION = 1510;
	public static final int WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER = 1511;
	public static final int WHILE_A_PETS_MASTER_IS_ATTEMPTING_TO_RESURRECT_THE_PET_CANNOT_BE_RESURRECTED_AT_THE_SAME_TIME = 1512;
	public static final int BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED = 1513;
	public static final int SINCE_THE_PET_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_ITS_MASTER_HAS_BEEN_CANCELLED = 1514;
	public static final int SINCE_THE_MASTER_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_THE_PET_HAS_BEEN_CANCELLED = 1515;
	public static final int THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING = 1516;
	public static final int FAILED_IN_BLESSED_ENCHANT_THE_ENCHANT_VALUE_OF_THE_ITEM_BECAME_0 = 1517;
	public static final int YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM = 1518;
	public static final int PET_HAS_DIED_IF_HE_IS_NOT_RESURRECTED_IN_20_MINUTES_THE_PETS_CORPSE_WILL_DISAPPEAR_AND_HIS_ITEMS_WILL_DISAPPEAR_AS_WELL = 1519;
	public static final int SERVITOR_PASSED_AWAY = 1520;
	public static final int SERVITOR_DISAPPEASR_BECAUSE_THE_SUMMONING_TIME_IS_OVER = 1521;
	public static final int THE_CORPSE_DISAPPEARED_BECAUSE_MUCH_TIME_PASSED_AFTER_PET_DIED = 1522;
	public static final int BECAUSE_PET_OR_SERVITOR_MAY_BE_DROWNED_WHILE_THE_BOAT_MOVES_PLEASE_RELEASE_THE_SUMMON_BEFORE_DEPARTURE = 1523;
	public static final int PET_OF_S1_GAINED_S2 = 1524;
	public static final int PET_OF_S1_GAINED_S3_OF_S2 = 1525;
	public static final int PET_OF_S1_GAINED__S2S3 = 1526;
	public static final int PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY = 1527;
	public static final int A_FORCIBLE_PETITION_FROM_GM_HAS_BEEN_RECEIVED = 1528;
	public static final int S1_HAS_INVITED_YOU_TO_THE_COMMAND_CHANNEL_DO_YOU_WANT_TO_JOIN = 1529;
	public static final int SELECT_A_TARGET_OR_ENTER_THE_NAME = 1530;
	public static final int ENTER_THE_NAME_OF_CLAN_AGAINST_WHICH_YOU_WANT_TO_MAKE_AN_ATTACK = 1531;
	public static final int ENTER_THE_NAME_OF_CLAN_AGAINST_WHICH_YOU_WANT_TO_STOP_THE_WAR = 1532;
	public static final int ATTENTION_S1_PICKED_UP_S2 = 1533;
	public static final int ATTENTION_S1_PICKED_UP__S2_S3 = 1534;
	public static final int ATTENTION_S1_PET_PICKED_UP_S2 = 1535;
	public static final int ATTENTION_S1_PET_PICKED_UP__S2_S3 = 1536;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_RUNE_VILLAGE = 1537;
	public static final int CURRENT_LOCATION__S1_S2_S3_NEAR_GODDARD_CASTLE_TOWN = 1538;
	public static final int CARGO_HAS_ARRIVED_AT_TALKING_ISLAND_VILLAGE = 1539;
	public static final int CARGO_HAS_ARRIVED_AT_DARK_ELVEN_VILLAGE = 1540;
	public static final int CARGO_HAS_ARRIVED_AT_ELVEN_VILLAGE = 1541;
	public static final int CARGO_HAS_ARRIVED_AT_ORC_VILLAGE = 1542;
	public static final int CARGO_HAS_ARRIVED_AT_DWARVEN_VILLAGE = 1543;
	public static final int CARGO_HAS_ARRIVED_AT_ADEN_CASTLE_TOWN = 1544;
	public static final int CARGO_HAS_ARRIVED_AT_OREN_CASTLE_TOWN = 1545;
	public static final int CARGO_HAS_ARRIVED_AT_HUNTERS_VILLAGE = 1546;
	public static final int CARGO_HAS_ARRIVED_AT_DION_CASTLE_TOWN = 1547;
	public static final int CARGO_HAS_ARRIVED_AT_FLORAN_VILLAGE = 1548;
	public static final int CARGO_HAS_ARRIVED_AT_GLUDIN_VILLAGE = 1549;
	public static final int CARGO_HAS_ARRIVED_AT_GLUDIO_CASTLE_TOWN = 1550;
	public static final int CARGO_HAS_ARRIVED_AT_GIRAN_CASTLE_TOWN = 1551;
	public static final int CARGO_HAS_ARRIVED_AT_HEINE = 1552;
	public static final int CARGO_HAS_ARRIVED_AT_RUNE_VILLAGE = 1553;
	public static final int CARGO_HAS_ARRIVED_AT_GODDARD_CASTLE_TOWN = 1554;
	public static final int DO_YOU_WANT_TO_CANCEL_CHARACTER_DELETION = 1555;
	public static final int NOTICE_HAS_BEEN_SAVED = 1556;
	public static final int SEED_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2 = 1557;
	public static final int THE_QUANTITY_OF_SEED_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2 = 1558;
	public static final int CROP_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2 = 1559;
	public static final int THE_QUANTITY_OF_CROP_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2_ = 1560;
	public static final int S1_CLAN_HAS_DECLARED_CLAN_WAR = 1561;
	public static final int CLAN_WAR_HAS_BEEN_DECLARED_AGAINST_S1_CLAN_IF_YOU_ARE_KILLED_DURING_THE_CLAN_WAR_BY_MEMBERS_OF_THE_OPPOSING_CLAN_THE_EXPERIENCE_PENALTY_WILL_BE_REDUCED_TO_1_4_OF_NORMAL = 1562;
	public static final int S1_CLAN_CANT_MAKE_A_DECLARATION_OF_CLAN_WAR_SINCE_IT_HASNT_REACHED_THE_CLAN_LEVEL_OR_DOESNT_HAVE_ENOUGH_CLAN_MEMBERS = 1563;
	public static final int A_CLAN_WAR_CAN_BE_DECLARED_ONLY_IF_THE_CLAN_IS_LEVEL_THREE_OR_ABOVE_AND_THE_NUMBER_OF_CLAN_MEMBERS_IS_FIFTEEN_OR_GREATER = 1564;
	public static final int THE_DECLARATION_OF_WAR_CANT_BE_MADE_BECAUSE_THE_CLAN_DOES_NOT_EXIST_OR_ACT_FOR_A_LONG_PERIOD = 1565;
	public static final int S1_CLAN_HAS_STOPPED_THE_WAR = 1566;
	public static final int THE_WAR_AGAINST_S1_CLAN_HAS_BEEN_STOPPED = 1567;
	public static final int THE_TARGET_FOR_DECLARATION_IS_WRONG = 1568;
	public static final int A_DECLARATION_OF_CLAN_WAR_AGAINST_AN_ALLIED_CLAN_CANT_BE_MADE = 1569;
	public static final int A_DECLARATION_OF_WAR_AGAINST_MORE_THAN_30_CLANS_CANT_BE_MADE_AT_THE_SAME_TIME = 1570;
	public static final int _ATTACK_LIST_ = 1571; // =======<ATTACK_LIST>=======
	public static final int _UNDER_ATTACK_LIST_ = 1572; // ======<UNDER_ATTACK_LIST>======
	public static final int THERE_IS_NO_ATTACK_CLAN = 1573;
	public static final int THERE_IS_NO_UNDER_ATTACK_CLAN = 1574;
	public static final int COMMAND_CHANNEL_ONLY_BY_LEVEL_5_CLAN_LEADER_PARTY_LEADER = 1575;
	public static final int PET_USES_THE_POWER_OF_SPIRIT = 1576;
	public static final int SERVITOR_USES_THE_POWER_OF_SPIRIT = 1577;
	public static final int ITEMS_ARE_NOT_AVAILABLE_FOR_A_PRIVATE_STORE_OR_PRIVATE_MANUFACTURE = 1578;
	public static final int S1_PET_GAINED_S2_ADENA = 1579;
	public static final int THE_COMMAND_CHANNEL_HAS_BEEN_FORMED = 1580;
	public static final int THE_COMMAND_CHANNEL_HAS_BEEN_DISBANDED = 1581;
	public static final int YOU_HAVE_PARTICIPATED_IN_THE_COMMAND_CHANNEL = 1582;
	public static final int YOU_WERE_DISMISSED_FROM_THE_COMMAND_CHANNEL = 1583;
	public static final int S1_PARTY_HAS_BEEN_DISMISSED_FROM_THE_COMMAND_CHANNEL = 1584;
	public static final int THE_COMMAND_CHANNEL_HAS_BEEN_DEACTIVATED = 1585;
	public static final int YOU_HAVE_QUIT_THE_COMMAND_CHANNEL = 1586;
	public static final int S1_PARTY_HAS_LEFT_THE_COMMAND_CHANNEL = 1587;
	public static final int THE_COMMAND_CHANNEL_IS_ACTIVATED_ONLY_IF_AT_LEAST_FIVE_PARTIES_PARTICIPATE_IN = 1588;
	public static final int COMMAND_CHANNEL_AUTHORITY_HAS_BEEN_TRANSFERRED_TO_S1 = 1589;
	public static final int _COMMAND_CHANNEL_INFO_TOTAL_PARTIES_S1_ = 1590; // ===<COMMAND_CHANNEL_INFO(TOTAL_PARTIES_S1)>===
	public static final int NO_USER_HAS_BEEN_INVITED_TO_THE_COMMAND_CHANNEL = 1591;
	public static final int YOU_CANT_OPEN_COMMAND_CHANNELS_ANY_MORE = 1592;
	public static final int YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL = 1593;
	public static final int S1_PARTY_IS_ALREADY_A_MEMBER_OF_THE_COMMAND_CHANNEL = 1594;
	public static final int S1_HAS_SUCCEEDED = 1595;
	public static final int HIT_BY_S1 = 1596;
	public static final int S1_HAS_FAILED = 1597;
	public static final int WHEN_PET_OR_SERVITOR_IS_DEAD_SOULSHOTS_OR_SPIRITSHOTS_FOR_PET_OR_SERVITOR_ARE_NOT_AVAILABLE = 1598;
	public static final int WATCHING_IS_IMPOSSIBLE_DURING_COMBAT = 1599;
	public static final int TOMORROWS_ITEMS_WILL_ALL_BE_SET_TO_0__DO_YOU_WISH_TO_CONTINUE = 1600;
	public static final int TOMORROWS_ITEMS_WILL_ALL_BE_SET_TO_THE_SAME_VALUE_AS_TODAYS_ITEMS__DO_YOU_WISH_TO_CONTINUE = 1601;
	public static final int ONLY_A_PARTY_LEADER_CAN_ACCESS_THE_COMMAND_CHANNEL = 1602;
	public static final int ONLY_CHANNEL_OPENER_CAN_GIVE_ALL_COMMAND = 1603;
	public static final int WHILE_DRESSED_IN_FORMAL_WEAR_YOU_CANT_USE_ITEMS_THAT_REQUIRE_ALL_SKILLS_AND_CASTING_OPERATIONS = 1604;
	public static final int _HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR = 1605;
	public static final int YOU_HAVE_COMPLETED_THE_QUEST_FOR_3RD_OCCUPATION_CHANGE_AND_MOVED_TO_ANOTHER_CLASS_CONGRATULATIONS = 1606;
	public static final int S1_ADENA_HAS_BEEN_PAID_FOR_PURCHASING_FEES = 1607;
	public static final int YOU_CANT_BUY_ANOTHER_CASTLE_SINCE_ADENA_IS_NOT_SUFFICIENT = 1608;
	public static final int THE_DECLARATION_OF_WAR_HAS_BEEN_ALREADY_MADE_TO_THE_CLAN = 1609;
	public static final int FOOL_YOU_CANNOT_DECLARE_WAR_AGAINST_YOUR_OWN_CLAN = 1610;
	public static final int PARTY_LEADER_S1 = 1611;
	public static final int _WAR_LIST_ = 1612; // =====<WAR_LIST>=====
	public static final int THERE_IS_NO_CLAN_LISTED_ON_WAR_LIST = 1613;
	public static final int YOU_ARE_PARTICIPATING_IN_THE_CHANNEL_WHICH_HAS_BEEN_ALREADY_OPENED = 1614;
	public static final int THE_NUMBER_OF_REMAINING_PARTIES_IS_S1_UNTIL_A_CHANNEL_IS_ACTIVATED = 1615;
	public static final int THE_COMMAND_CHANNEL_HAS_BEEN_ACTIVATED = 1616;
	public static final int YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL = 1617;
	public static final int YOU_CANNOT_FISH_WHILE_USING_A_RECIPE_BOOK_PRIVATE_MANUFACTURE_OR_PRIVATE_STORE = 1638;
	public static final int OLYMPIAD_PERIOD_S1_HAS_STARTED = 1639;
	public static final int OLYMPIAD_PERIOD_S1_HAS_ENDED = 1640;
	public static final int THE_OLYMPIAD_GAME_HAS_STARTED = 1641;
	public static final int THE_OLYMPIAD_GAME_HAS_ENDED = 1642;
	public static final int CURRENT_LOCATION_S1_S2_S3_DIMENSION_GAP = 1643;
	public static final int DUE_TO_A_LARGE_NUMBER_OF_USERS_CURRENTLY_ACCESSING_OUR_SERVER_YOUR_LOGIN_ATTEMPT_HAS_FAILED_PLEASE_WAIT_A_LITTLE_WHILE_AND_ATTEMPT_TO_LOG_IN_AGAIN = 1650;
	public static final int THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS = 1651;
	public static final int THE_VIDEO_RECORDING_OF_THE_REPLAY_WILL_NOW_BEGIN = 1652;
	public static final int THE_REPLAY_FILE_HAS_BEEN_STORED_SUCCESSFULLY_S1 = 1653;
	public static final int THE_ATTEMPT_TO_RECORD_THE_REPLAY_FILE_HAS_FAILED = 1654;
	public static final int YOU_HAVE_CAUGHT_A_MONSTER = 1655;
	public static final int YOU_HAVE_SUCCESSFULLY_TRADED_THE_ITEM_WITH_THE_NPC = 1656;
	public static final int S1_HAS_GAINED_S2_OLYMPIAD_POINTS = 1657;
	public static final int S1_HAS_LOST_S2_OLYMPIAD_POINTS = 1658;
	public static final int CURRENT_LOCATION_S1_S2_S3_CEMETERY_OF_THE_EMPIRE = 1659;
	public static final int THE_CHANNEL_WAS_OPENED_BY_S1 = 1660;
	public static final int S1_HAS_OBTAINED_S3_S2S = 1661;
	public static final int IF_YOU_FISH_IN_ONE_SPOT_FOR_A_LONG_TIME_THE_SUCCESS_RATE_OF_A_FISH_TAKING_THE_BAIT_BECOMES_LOWER__PLEASE_MOVE_TO_ANOTHER_PLACE_AND_CONTINUE_YOUR_FISHING_THERE = 1662;
	public static final int THE_CLANS_EMBLEM_WAS_SUCCESSFULLY_REGISTERED__ONLY_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_A_CASTLE_CAN_GET_THEIR_EMBLEM_DISPLAYED_ON_CLAN_RELATED_ITEMS = 1663;
	public static final int BECAUSE_THE_FISH_IS_RESISTING_THE_FLOAT_IS_BOBBING_UP_AND_DOWN_A_LOT = 1664;
	public static final int SINCE_THE_FISH_IS_EXHAUSTED_THE_FLOAT_IS_MOVING_ONLY_SLIGHTLY = 1665;
	public static final int YOU_HAVE_OBTAINED__S1_S2 = 1666;
	public static final int INSTANT_KILL = 1667;
	public static final int YOU_HAVE_BEEN_STRUCK_BY_THE_INSTANT_KILL_SKILL = 1668;
	public static final int THE_ATTEMPT_FOR_ITEM_CONVERSION_HAS_FAILED = 1669;
	public static final int DUE_TO_YOUR_REELING_ANDOR_PUMPING_SKILL_BEING_THREE_OR_MORE_LEVELS_HIGHER_THAN_YOUR_FISHING_SKILL_A_50_DAMAGE_PENALTY_WILL_BE_APPLIED = 1670;
	public static final int YOUR_REELING_WAS_SUCCESSFUL_MASTERY_PENALTYS1_ = 1671;
	public static final int YOUR_PUMPING_WAS_SUCCESSFUL_MASTERY_PENALTYS1_ = 1672;
	public static final int THE_CURRENT_FOR_THIS_OLYMPIAD_IS_S1_WINS_S2_DEFEATS_S3_YOU_HAVE_EARNED_S4_OLYMPIAD_POINTS = 1673;
	public static final int THIS_COMMAND_CAN_ONLY_BE_USED_BY_A_NOBLESSE = 1674;
	public static final int A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM = 1675;
	public static final int SINCE_A_SERVITOR_OR_A_PET_DOES_NOT_EXIST_AUTOMATIC_USE_IS_NOT_APPLICABLE = 1676;
	public static final int A_CEASE_FIRE_DURING_A_CLAN_WAR_CAN_NOT_BE_CALLED_WHILE_MEMBERS_OF_YOUR_CLAN_ARE_ENGAGED_IN_BATTLE = 1677;
	public static final int YOU_HAVE_NOT_DECLARED_A_CLAN_WAR_TO_S1_CLAN = 1678;
	public static final int ONLY_THE_CREATOR_OF_A_CHANNEL_CAN_ISSUE_A_GLOBAL_COMMAND = 1679;
	public static final int S1_HAS_DECLINED_THE_CHANNEL_INVITATION = 1680;
	public static final int SINCE_S1_DID_NOT_RESPOND_YOUR_CHANNEL_INVITATION_HAS_FAILED = 1681;
	public static final int ONLY_THE_CREATOR_OF_A_CHANNEL_CAN_USE_THE_CHANNEL_DISMISS_COMMAND = 1682;
	public static final int ONLY_A_PARTY_LEADER_CAN_CHOOSE_THE_OPTION_TO_LEAVE_A_CHANNEL = 1683;
	public static final int WHILE_A_CLAN_IS_BEING_DISSOLVED_IT_IS_IMPOSSIBLE_TO_DECLARE_A_CLAN_WAR_AGAINST_IT = 1684;
	public static final int IF_YOUR_PK_COUNT_IS_1_OR_MORE_YOU_ARE_NOT_ALLOWED_TO_WEAR_THIS_ITEM = 1685;
	public static final int THE_CASTLE_WALL_HAS_SUSTAINED_DAMAGE = 1686;
	public static final int THIS_AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_ATOP_OF_A_WYVERN_YOU_WILL_BE_DISMOUNTED_FROM_YOUR_WYVERN_IF_YOU_DO_NOT_LEAVE = 1687;
	public static final int YOU_CANNOT_PRACTICE_ENCHANTING_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_MANUFACTURING_WORKSHOP = 1688;
	public static final int YOU_ARE_ALREADY_ON_THE_WAITING_LIST_TO_PARTICIPATE_IN_THE_GAME_FOR_YOUR_CLASS = 1689;
	public static final int YOU_ARE_ALREADY_ON_THE_WAITING_LIST_FOR_ALL_CLASSES_WAITING_TO_PARTICIPATE_IN_THE_GAME = 1690;
	public static final int SINCE_80_PERCENT_OR_MORE_OF_YOUR_INVENTORY_SLOTS_ARE_FULL_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD = 1691;
	public static final int SINCE_YOU_HAVE_CHANGED_YOUR_CLASS_INTO_A_SUB_JOB_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD = 1692;
	public static final int WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME = 1693;
	public static final int ONLY_A_CLAN_LEADER_THAT_IS_A_NOBLESSE_CAN_VIEW_THE_SIEGE_WAR_STATUS_WINDOW_DURING_A_SIEGE_WAR = 1694;
	public static final int IT_CAN_BE_USED_ONLY_WHILE_A_SIEGE_WAR_IS_TAKING_PLACE = 1695;
	public static final int IF_THE_ACCUMULATED_ONLINE_ACCESS_TIME_IS_S1_OR_MORE_A_PENALTY_WILL_BE_IMPOSED__PLEASE_TERMINATE_YOUR_SESSION_AND_TAKE_A_BREAK = 1696;
	public static final int SINCE_YOUR_CUMULATIVE_ACCESS_TIME_HAS_EXCEEDED_S1_YOUR_EXP_AND_ITEM_DROP_RATE_WERE_REDUCED_BY_HALF_PLEASE_TERMINATE_YOUR_SESSION_AND_TAKE_A_BREAK = 1697;
	public static final int SINCE_YOUR_CUMULATIVE_ACCESS_TIME_HAS_EXCEEDED_S1_YOU_NO_LONGER_HAVE_EXP_OR_ITEM_DROP_PRIVILEGE__PLEASE_TERMINATE_YOUR_SESSION_AND_TAKE_A_BREAK = 1698;
	public static final int YOU_CANNOT_DISMISS_A_PARTY_MEMBER_BY_FORCE = 1699;
	public static final int YOU_DONT_HAVE_ENOUGH_SPIRITSHOTS_NEEDED_FOR_A_PET_SERVITOR = 1700;
	public static final int YOU_DONT_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_PET_SERVITOR = 1701;
	public static final int THE_USER_WHO_CONDUCTED_A_SEARCH_A_MOMENT_AGO_HAS_BEEN_CONFIRMED_TO_BE_A_BOT_USER = 1702;
	public static final int THE_USER_WHO_CONDUCTED_A_SEARCH_A_MOMENT_AGO_HAS_BEEN_CONFIRMED_TO_BE_A_NONBOT_USER = 1703;
	public static final int PLEASE_CLOSE_THE_SETUP_WINDOW_FOR_A_PRIVATE_MANUFACTURING_STORE_OR_THE_SETUP_WINDOW_FOR_A_PRIVATE_STORE_AND_TRY_AGAIN = 1704;
	public static final int THE_MATCH_MAY_BE_DELAYED_DUE_TO_NOT_ENOUGH_COMBATANTS = 1713;
	public static final int YOU_HAVE_EARNED_S1_RAID_POINTS = 1725;
	public static final int YOU_CANNOT_JOIN_A_COMMAND_CHANNEL_WHILE_TELEPORTING = 1729;
	public static final int TO_ESTABLISH_A_CLAN_ACADEMY_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER = 1730;
	public static final int ONLY_THE_CLAN_LEADER_CAN_CREATE_A_CLAN_ACADEMY = 1731;
	public static final int TO_CREATE_A_CLAN_ACADEMY_A_BLOOD_MARK_IS_NEEDED = 1732;
	public static final int YOU_DO_NOT_HAVE_ENOUGH_ADENA_TO_CREATE_A_CLAN_ACADEMY = 1733;
	public static final int TO_JOIN_A_CLAN_ACADEMY_CHARACTERS_MUST_BE_LEVEL_40_OR_BELOW_NOT_BELONG_ANOTHER_CLAN_AND_NOT_YET_COMPLETED_THEIR_2ND_CLASS_TRANSFER = 1734;
	public static final int S1_DOES_NOT_MEET_THE_REQUIREMENTS_TO_JOIN_A_CLAN_ACADEMY = 1735;
	public static final int THE_CLAN_ACADEMY_HAS_REACHED_ITS_MAXIMUM_ENROLLMENT = 1736;
	public static final int YOUR_CLAN_HAS_NOT_ESTABLISHED_A_CLAN_ACADEMY_BUT_IS_ELIGIBLE_TO_DO_SO = 1737;
	public static final int YOUR_CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY = 1738;
	public static final int CONGRATULATIONS_THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED = 1741;
	public static final int A_MESSAGE_INVITING_S1_TO_JOIN_THE_CLAN_ACADEMY_IS_BEING_SENT = 1742;
	public static final int TO_OPEN_A_CLAN_ACADEMY_THE_LEADER_OF_A_LEVEL_5_CLAN_OR_ABOVE_MUST_PAY_XX_PROOFS_OF_BLOOD_OR_A_CERTAIN_AMOUNT_OF_ADENA = 1743;
	public static final int THERE_WAS_NO_RESPONSE_TO_YOUR_INVITATION_TO_JOIN_THE_CLAN_ACADEMY_SO_THE_INVITATION_HAS_BEEN_RESCINDED = 1744;
	public static final int THE_RECIPIENT_OF_YOUR_INVITATION_TO_JOIN_THE_CLAN_ACADEMY_HAS_DECLINED = 1745;
	public static final int YOU_HAVE_ALREADY_JOINED_A_CLAN_ACADEMY = 1746;
	public static final int CLAN_ACADEMY_MEMBER_S1_HAS_SUCCESSFULLY_COMPLETED_THE_2ND_CLASS_TRANSFER_AND_OBTAINED_S2_CLAN_REPUTATION_POINTS = 1748;
	public static final int CONGRATULATIONS_YOU_WILL_NOW_GRADUATE_FROM_THE_CLAN_ACADEMY_AND_LEAVE_YOUR_CURRENT_CLAN_AS_A_GRADUATE_OF_THE_ACADEMY_YOU_CAN_IMMEDIATELY_JOIN_A_CLAN_AS_A_REGULAR_MEMBER_WITHOUT_BEING_SUBJECT_TO_ANY_PENALTIES = 1749;
	public static final int THE_GRAND_MASTER_HAS_GIVEN_YOU_A_COMMEMORATIVE_ITEM = 1751;
	public static final int SINCE_THE_CLAN_HAS_RECEIVED_A_GRADUATE_OF_THE_CLAN_ACADEMY_IT_HAS_EARNED_S1_POINTS_TOWARD_ITS_REPUTATION_SCORE = 1752;
	public static final int S2_HAS_BEEN_DESIGNATED_AS_THE_APPRENTICE_OF_CLAN_MEMBER_S1 = 1755;
	public static final int S1_YOUR_CLAN_ACADEMYS_APPRENTICE_HAS_LOGGED_IN = 1756;
	public static final int S1_YOUR_CLAN_ACADEMYS_APPRENTICE_HAS_LOGGED_OUT = 1757;
	public static final int S1_YOUR_CLAN_ACADEMYS_SPONSOR_HAS_LOGGED_IN = 1758;
	public static final int S1_YOUR_CLAN_ACADEMYS_SPONSOR_HAS_LOGGED_OUT = 1759;
	public static final int CLAN_MEMBER_S1S_TITLE_HAS_BEEN_CHANGED_TO_S2 = 1760;
	public static final int CLAN_MEMBER_S1S_PRIVILEGE_LEVEL_HAS_BEEN_CHANGED_TO_S2 = 1761;
	public static final int S2_CLAN_MEMBER_S1S_APPRENTICE_HAS_BEEN_REMOVED = 1763;
	public static final int THIS_ITEM_CAN_ONLY_BE_WORN_BY_A_MEMBER_OF_THE_CLAN_ACADEMY = 1764;
	public static final int AS_A_GRADUATE_OF_THE_CLAN_ACADEMY_YOU_CAN_NO_LONGER_WEAR_THIS_ITEM = 1765;
	public static final int AN_APPLICATION_TO_JOIN_THE_CLAN_HAS_BEEN_SENT_TO_S1_IN_S2 = 1766;
	public static final int AN_APPLICATION_TO_JOIN_THE_CLAN_ACADEMY_HAS_BEEN_SENT_TO_S1 = 1767;
	public static final int THE_CLAN_REPUTATION_SCORE_HAS_DROPPED_BELOW_0_THE_CLAN_MAY_FACE_CERTAIN_PENALTIES_AS_A_RESULT = 1770;
	public static final int NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS = 1771;
	public static final int SINCE_YOUR_CLAN_WAS_DEFEATED_IN_A_SIEGE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE_AND_GIVEN_TO_THE_OPPOSING_CLAN = 1772;
	public static final int SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE = 1773;
	public static final int YOUR_CLAN_NEWLY_ACQUIRED_CONTESTED_CLAN_HALL_HAS_ADDED_S1_POINTS_TO_YOUR_CLAN_REPUTATION_SCORE = 1774;
	public static final int CLAN_MEMBER_S1_WAS_AN_ACTIVE_MEMBER_OF_THE_HIGHEST_RANKED_PARTY_IN_THE_FESTIVAL_OF_DARKNESS_S2_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE = 1775;
	public static final int CLAN_MEMBER_S1_WAS_NAMED_A_HERO_2S_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE = 1776;
	public static final int YOU_HAVE_SUCCESSFULLY_COMPLETED_A_CLAN_QUEST_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE = 1777;
	public static final int AN_OPPOSING_CLAN_HAS_CAPTURED_YOUR_CLAN_CONTESTED_CLAN_HALL_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE = 1778;
	public static final int AFTER_LOSING_THE_CONTESTED_CLAN_HALL_300_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE = 1779;
	public static final int YOUR_CLAN_HAS_CAPTURED_YOUR_OPPONENT_CONTESTED_CLAN_HALL_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENT_CLAN_REPUTATION_SCORE = 1780;
	public static final int YOUR_CLAN_HAS_ADDED_1S_POINTS_TO_ITS_CLAN_REPUTATION_SCORE = 1781;
	public static final int YOUR_CLAN_MEMBER_S1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENT_CLAN_REPUTATION_SCORE = 1782;
	public static final int FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE = 1783;
	public static final int YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENTS = 1784;
	public static final int THE_CLAN_YOU_BELONG_TO_HAS_BEEN_INITIALIZED_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE = 1785;
	public static final int YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE = 1786;
	public static final int S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_THE_CLAN_REPUTATION_SCORE = 1787;
	public static final int THE_CLAN_SKILL_S1_HAS_BEEN_ADDED = 1788;
	public static final int SINCE_THE_CLAN_REPUTATION_SCORE_HAS_DROPPED_TO_0_OR_LOWER_YOUR_CLAN_SKILLS_WILL_BE_DE_ACTIVATED = 1789;
	public static final int THE_CONDITIONS_NECESSARY_TO_INCREASE_THE_CLAN_LEVEL_HAVE_NOT_BEEN_MET = 1790;
	public static final int THE_CONDITIONS_NECESSARY_TO_CREATE_A_MILITARY_UNIT_HAVE_NOT_BEEN_MET = 1791;
	public static final int S1_HAS_BEEN_SELECTED_AS_THE_CAPTAIN_OF_S2 = 1793;
	public static final int THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED = 1794;
	public static final int THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED = 1795;
	public static final int FOR_KOREA_ONLY = 1796;
	public static final int CURRENTLY_UNDER_INVESTIGATION_PLEASE_WAIT = 1799;
	public static final int THE_USER_NAME_S1_HAS_A_HISTORY_OF_USING_THIRD_PARTY_PROGRAMS = 1800;
	public static final int THE_ATTEMPT_TO_SELL_HAS_FAILED = 1801;
	public static final int THE_ATTEMPT_TO_TRADE_HAS_FAILED = 1802;
	public static final int YOU_CANNOT_REGISTER_FOR_A_MATCH = 1803;
	public static final int THIS_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_7_DAYS = 1804;
	public static final int THIS_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_30_DAYS_1 = 1805;
	public static final int THIS_ACCOUNT_HAS_BEEN_PERMANENTLY_BANNED_1 = 1806;
	public static final int THIS_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_30_DAYS_2 = 1807;
	public static final int THIS_ACCOUNT_HAS_BEEN_PERMANENTLY_BANNED_2 = 1808;
	public static final int ACCOUNT_OWNER_MUST_BE_VERIFIED_IN_ORDER_TO_USE_THIS_ACCOUNT_AGAIN = 1809;
	public static final int THERE_IS_S1_HOUR_AND_S2_MINUTE_LEFT_OF_THE_FIXED_USAGE_TIME = 1813;
	public static final int S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1 = 1814;
	public static final int S2_WAS_DROPPED_IN_THE_S1_REGION = 1815;
	public static final int THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION = 1816;
	public static final int S2_OWNER_HAS_LOGGED_INTO_THE_S1_REGION = 1817;
	public static final int S1_HAS_DISAPPEARED_CW = 1818;
	public static final int THE_REGISTRATION_PERIOD_FOR_A_CLAN_HALL_WAR_HAS_ENDED = 1823;
	public static final int YOU_HAVE_BEEN_REGISTERED_FOR_A_CLAN_HALL_WAR = 1824;
	public static final int YOU_HAVE_FAILED_IN_YOUR_ATTEMPT_TO_REGISTER_FOR_THE_CLAN_HALL_WAR = 1825;
	public static final int IN_S1_MINUTES_THE_GAME_WILL_BEGIN_ALL_PLAYERS_MUST_HURRY_AND_MOVE_TO_THE_LEFT_SIDE_OF_THE_CLAN_HALLS_ARENA = 1826;
	public static final int IN_S1_MINUTES_THE_GAME_WILL_BEGIN_ALL_PLAYERS_PLEASE_ENTER_THE_ARENA_NOW = 1827;
	public static final int IN_S1_SECONDS_THE_GAME_WILL_BEGIN = 1828;
	public static final int ONLY_A_ROOM_LEADER_MAY_INVITE_OTHERS_TO_A_PARTY_ROOM = 1832;
	public static final int THE_PARTY_ROOM_IS_FULL = 1834;
	public static final int S1_IS_FULL_AND_CANNOT_ACCEPT_ADDITIONAL_CLAN_MEMBERS_AT_THIS_TIME = 1835;
	public static final int THIS_CLAN_HALL_WAR_HAS_BEEN_CANCELLED = 1841;
	public static final int S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT = 1842;
	public static final int S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED = 1843;
	public static final int S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED = 1844;
	public static final int HERO_WEAPONS_CANNOT_BE_DESTROYED = 1845;
	public static final int THE_CAPTAIN_OF_THE_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED = 1850;
	public static final int THE_CAPTAIN_OF_THE_ROYAL_GUARD_CANNOT_BE_APPOINTED = 1851;
	public static final int THE_ATTEMPT_TO_ACQUIRE_THE_SKILL_HAS_FAILED_BECAUSE_OF_AN_INSUFFICIENT_CLAN_REPUTATION_SCORE = 1852;
	public static final int QUANTITY_ITEMS_OF_THE_SAME_TYPE_CANNOT_BE_EXCHANGED_AT_THE_SAME_TIME = 1853;
	public static final int THE_ITEM_WAS_CONVERTED_SUCCESSFULLY = 1854;
	public static final int ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME_PLEASE_ENTER_A_DIFFERENT_NAME = 1855;
	public static final int SINCE_YOUR_OPPONENT_IS_NOW_THE_OWNER_OF_S1_THE_OLYMPIAD_HAS_BEEN_CANCELLED = 1856;
	public static final int SINCE_YOU_NOW_OWN_S1_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD = 1857;
	public static final int YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD_WHILE_DEAD = 1858;
	public static final int YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_MOVED_AT_ONE_TIME = 1859;
	public static final int THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW = 1860;
	public static final int THE_CLANS_CREST_HAS_BEEN_DELETED = 1861;
	public static final int THE_CLAN_SKILL_WILL_BE_ACTIVATED_BECAUSE_THE_CLANS_REPUTATION_SCORE_HAS_REACHED_TO_0_OR_HIGHER = 1862;
	public static final int S1_PURCHASED_A_CLAN_ITEM_REDUCING_THE_CLAN_REPUTATION_BY_S2_POINTS = 1863;
	public static final int THE_PET_SERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS = 1864;
	public static final int THE_PET_SERVITOR_IS_CURRENTLY_IN_A_STATE_OF_DISTRESS = 1865;
	public static final int MP_WAS_REDUCED_BY_S1 = 1866;
	public static final int YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1 = 1867;
	public static final int YOU_CANNOT_EXCHANGE_AN_ITEM_WHILE_IT_IS_BEING_USED = 1868;
	public static final int S1_HAS_GRANTED_THE_CHANNELS_MASTER_PARTY_THE_PRIVILEGE_OF_ITEM_LOOTING = 1869;
	public static final int A_COMMAND_CHANNEL_WITH_THE_ITEM_LOOTING_PRIVILEGE_ALREADY_EXISTS = 1870;
	public static final int DO_YOU_WANT_TO_DISMISS_S1_FROM_THE_CLAN = 1871;
	public static final int YOU_HAVE_S1_HOURS_AND_S2_MINUTES_LEFT = 1872;
	public static final int THERE_ARE_S1_HOURS_AND_S2_MINUTES_LEFT_IN_THE_FIXED_USE_TIME_FOR_THIS_PC_CARD = 1873;
	public static final int THERE_ARE_S1_MINUTES_LEFT_FOR_THIS_INDIVIDUAL_USER = 1874;
	public static final int THERE_ARE_S1_MINUTES_LEFT_IN_THE_FIXED_USE_TIME_FOR_THIS_PC_CARD = 1875;
	public static final int DO_YOU_WANT_TO_LEAVE_S1_CLAN = 1876;
	public static final int THE_GAME_WILL_END_IN_S1_MINUTES = 1877;
	public static final int THE_GAME_WILL_END_IN_S1_SECONDS = 1878;
	public static final int IN_S1_MINUTES_YOU_WILL_BE_TELEPORTED_OUTSIDE_OF_THE_GAME_ARENA = 1879;
	public static final int IN_S1_SECONDS_YOU_WILL_BE_TELEPORTED_OUTSIDE_OF_THE_GAME_ARENA = 1880;
	public static final int THE_PRELIMINARY_MATCH_WILL_BEGIN_IN_S1_SECONDS = 1881;
	public static final int CHARACTERS_CANNOT_BE_CREATED_FROM_THIS_SERVER = 1882;
	public static final int THERE_ARE_NO_OFFERINGS_I_OWN_OR_I_MADE_A_BID_FOR = 1883;
	public static final int ENTER_THE_PC_ROOM_COUPON_SERIAL_NUMBER = 1884;
	public static final int THIS_SERIAL_NUMBER_CANNOT_BE_ENTERED_PLEASE_TRY_AGAIN_IN_S1_MINUTES = 1885;
	public static final int THIS_SERIAL_NUMBER_HAS_ALREADY_BEEN_USED = 1886;
	public static final int INVALID_SERIAL_NUMBER_YOUR_ATTEMPT_TO_ENTER_THE_NUMBER_HAS_FAILED_S1_TIMES_YOU_WILL_BE_ALLOWED_TO_MAKE_S2_MORE_ATTEMPTS = 1887;
	public static final int INVALID_SERIAL_NUMBER_YOUR_ATTEMPT_TO_ENTER_THE_NUMBER_HAS_FAILED_5_TIMES_PLEASE_TRY_AGAIN_IN_4_HOURS = 1888;
	public static final int CONGRATULATIONS_YOU_HAVE_RECEIVED_S1 = 1889;
	public static final int SINCE_YOU_HAVE_ALREADY_USED_THIS_COUPON_YOU_MAY_NOT_USE_THIS_SERIAL_NUMBER = 1890;
	public static final int YOU_MAY_NOT_USE_ITEMS_IN_A_PRIVATE_STORE_OR_PRIVATE_WORK_SHOP = 1891;
	public static final int THE_REPLAY_FILE_FOR_THE_PREVIOUS_VERSION_CANNOT_BE_PLAYED = 1892;
	public static final int THIS_FILE_CANNOT_BE_REPLAYED = 1893;
	public static final int A_SUB_CLASS_CANNOT_BE_CREATED_OR_CHANGED_WHILE_YOU_ARE_OVER_YOUR_WEIGHT_LIMIT = 1894;
	public static final int S1_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING = 1895;
	public static final int S1_HAS_ALREADY_BEEN_SUMMONED = 1896;
	public static final int S1_IS_REQUIRED_FOR_SUMMONING = 1897;
	public static final int S1_IS_CURRENTLY_TRADING_OR_OPERATING_A_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED = 1898;
	public static final int YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING = 1899;
	public static final int S1_HAS_ENTERED_THE_PARTY_ROOM = 1900;
	public static final int S1_HAS_INVITED_YOU_TO_ENTER_THE_PARTY_ROOM = 1901;
	public static final int INCOMPATIBLE_ITEM_GRADE_THIS_ITEM_CANNOT_BE_USED = 1902;
	public static final int REQUESTED_NCOTP = 1903;
	public static final int A_SUB_CLASS_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SERVITOR_OR_PET_IS_SUMMONED = 1904;
	public static final int S2_OF_S1_WILL_BE_REPLACED_WITH_S4_OF_S3 = 1905;
	public static final int SELECT_THE_COMBAT_UNIT_YOU_WISH_TO_TRANSFER_TO = 1906;
	public static final int SELECT_THE_THE_CHARACTER_WHO_WILL_REPLACE_THE_CURRENT_CHARACTER = 1907;
	public static final int S1_IS_IN_A_STATE_WHICH_PREVENTS_SUMMONING = 1908;
	public static final int LIST_OF_CLAN_ACADEMY_GRADUATES_DURING_THE_PAST_WEEK = 1909;
	public static final int GRADUATES = 1910;
	public static final int YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD = 1911;
	public static final int ONLY_THOSE_REQUESTING_NCOTP_SHOULD_MAKE_AN_ENTRY_INTO_THIS_FIELD = 1912;
	public static final int THE_REMAINING_RECYCLE_TIME_FOR_S1_IS_S2_MINUTES = 1913;
	public static final int THE_REMAINING_RECYCLE_TIME_FOR_S1_IS_S2_SECONDS = 1914;
	public static final int THE_GAME_WILL_END_IN_S1_SECONDS_2 = 1915;
	public static final int THE_LEVEL_S1_DEATH_PENALTY_WILL_BE_ASSESSED = 1916;
	public static final int THE_DEATH_PENALTY_HAS_BEEN_LIFTED = 1917;
	public static final int THE_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL = 1918;
	public static final int THE_GRAND_OLYMPIAD_REGISTRATION_PERIOD_HAS_ENDED = 1919;
	public static final int YOUR_ACCOUNT_IS_CURRENTLY_INACTIVE_BECAUSE_YOU_HAVE_NOT_LOGGED_INTO_THE_GAME_FOR_SOME_TIME_YOU = 1920;
	public static final int S2_HOURS_AND_S3_MINUTES_HAVE_PASSED_SINCE_S1_HAS_KILLED = 1921;
	public static final int BECAUSE_S1_FAILED_TO_KILL_FOR_ONE_FULL_DAY_IT_HAS_EXPIRED = 1922;
	public static final int COURT_MAGICIAN__THE_PORTAL_HAS_BEEN_CREATED = 1923;

	// Recommedations
	public static final int YOU_CANNOT_RECOMMEND_YOURSELF = 829;
	public static final int YOU_HAVE_BEEN_RECOMMENDED = 831;
	public static final int YOU_HAVE_RECOMMENDED = 830;
	public static final int THAT_CHARACTER_IS_RECOMMENDED = 832;
	public static final int NO_MORE_RECOMMENDATIONS_TO_HAVE = 833;
	public static final int ONLY_LEVEL_SUP_10_CAN_RECOMMEND = 898;
	public static final int YOU_NO_LONGER_RECIVE_A_RECOMMENDATION = 1188;

	// Duelling
	public static final int THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL = 1926;
	public static final int S1_HAS_BEEN_CHALLENGED_TO_A_DUEL = 1927;
	public static final int S1S_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL = 1928;
	public static final int S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS = 1929;
	public static final int YOU_HAVE_ACCEPTED_S1S_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS = 1930;
	public static final int S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL = 1931;
	public static final int YOU_HAVE_ACCEPTED_S1S_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS = 1933;
	public static final int S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS = 1934;
	public static final int S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_PARTY_DUEL = 1935;
	public static final int THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL = 1936;
	public static final int SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY = 1937;
	public static final int S1_HAS_CHALLENGED_YOU_TO_A_DUEL = 1938;
	public static final int S1S_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL = 1939;
	public static final int YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME = 1940;
	public static final int THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL = 1942;
	public static final int IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE = 1944;
	public static final int THE_DUEL_WILL_BEGIN_IN_S1_SECONDS = 1945;
	public static final int LET_THE_DUEL_BEGIN = 1949;
	public static final int S1_HAS_WON_THE_DUEL = 1950;
	public static final int S1S_PARTY_HAS_WON_THE_DUEL = 1951;
	public static final int THE_DUEL_HAS_ENDED_IN_A_TIE = 1952;
	public static final int SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON = 1955;
	public static final int SINCE_S1S_PARTY_WITHDREW_FROM_THE_DUEL_S1S_PARTY_HAS_WON = 1956;
	public static final int S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE = 2017;
	public static final int S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING = 2018;
	public static final int S1_CANNOT_DUEL_BECAUSE_S1S_HP_OR_MP_IS_BELOW_50_PERCENT = 2019;
	public static final int S1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA = 2020;
	public static final int S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE = 2021;
	public static final int S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL = 2022;
	public static final int S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE = 2023;
	public static final int S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD = 2024;
	public static final int S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_A_CLAN_HALL_WAR = 2025;
	public static final int S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_A_SIEGE_WAR = 2026;
	public static final int S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER = 2027;
	public static final int S1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_S1_IS_TOO_FAR_AWAY = 2028;
	public static final int THE_ATTACK_HAS_BEEN_BLOCKED = 1996;
	public static final int S1S_IS_PERFORMING_A_COUNTERATTACK = 1997;
	public static final int YOU_COUNTERED_S1S_ATTACK = 1998;
	public static final int S1_DODGES_THE_ATTACK = 1999;
	public static final int YOU_HAVE_AVOIDED_S1_S_ATTACK = 2000;
	public static final int TRAP_FAILED = 2002;
	public static final int YOU_OBTAINED_AN_ORDINARY_MATERIAL = 2003;
	public static final int YOU_OBTAINED_A_RARE_MATERIAL = 2004;
	public static final int YOU_OBTAINED_A_UNIQUE_MATERIAL = 2005;
	public static final int YOU_OBTAINED_THE_ONLY_MATERIAL_OF_THIS_KIND = 2006;
	public static final int PLEASE_ENTER_THE_RECIPIENT_S_NAME = 2007;
	public static final int PLEASE_ENTER_THE_TEXT = 2008;
	public static final int YOU_CANNOT_EXCEED_1500_CHARACTERS = 2009;
	public static final int S1_S2 = 2010;
	public static final int THE_AUGMENTED_ITEM_CANNOT_BE_DISCARDED = 2011;
	public static final int S1_HAS_BEEN_ACTIVATED = 2012;
	public static final int YOUR_SEED_OR_REMAINING_PURCHASE_AMOUNT_IS_INADEQUATE = 2013;
	public static final int YOU_CANNOT_PROCEED_BECAUSE_THE_MANOR_CANNOT_ACCEPT_ANY_MORE_CROPS__ALL_CROPS_HAVE_BEEN_RETURNED = 2014;
	public static final int A_SKILL_IS_READY_TO_BE_USED_AGAIN = 2015;
	public static final int A_SKILL_IS_READY_TO_BE_USED_AGAIN_BUT_ITS_RE_USE_COUNTER_TIME_HAS_INCREASED = 2016;
	public static final int YOU_CANNOT_PARTICIPATE_IN_OLYMPIAD_DURING_TELEPORT = 2029;
	public static final int YOU_ARE_CURRENTLY_LOGGING_IN = 2030;
	public static final int PLEASE_WAIT_A_MOMENT = 2031;
	public static final int IT_IS_NOT_THE_RIGHT_TIME_FOR_PURCHASING_THE_ITEM = 2032;
	public static final int A_SUB_CLASS_CANNOT_BE_CREATED_OR_CHANGED_BECAUSE_YOU_HAVE_EXCEEDED_YOUR_INVENTORY_LIMIT = 2033;
	public static final int THERE_ARE_S1_HOURSS_AND_S2_MINUTES_REMAINING_UNTIL_THE_TIME_WHEN_THE_ITEM_CAN_BE_PURCHASED = 2034;
	public static final int THERE_ARE_S1_MINUTES_REMAINING_UNTIL_THE_TIME_WHEN_THE_ITEM_CAN_BE_PURCHASED = 2035;
	public static final int UNABLE_TO_INVITE_BECAUSE_THE_PARTY_IS_LOCKED = 2036;
	public static final int UNABLE_TO_CREATE_CHARACTER_YOU_ARE_UNABLE_TO_CREATE_A_NEW_CHARACTER_ON_THE_SELECTED_SERVER_A = 2037;
	public static final int FERRY_ARRIVED_AT_TALKING = 979;
	public static final int FERRY_LEAVE_FOR_GLUDIN_AFTER_10_MINUTES = 980;
	public static final int FERRY_LEAVE_FOR_GLUDIN_IN_5_MINUTES = 981;
	public static final int FERRY_LEAVE_FOR_GLUDIN_IN_1_MINUTE = 982;
	public static final int MAKE_HASTE_GET_ON_BOAT = 983;
	public static final int FERRY_LEAVE_SOON_FOR_GLUDIN = 984;
	public static final int FERRY_LEAVING_FOR_GLUDIN = 985;
	public static final int FERRY_ARRIVED_AT_GLUDIN = 986;
	public static final int FERRY_LEAVE_FOR_TALKING_AFTER_10_MINUTES = 987;
	public static final int FERRY_LEAVE_FOR_TALKING_IN_5_MINUTES = 988;
	public static final int FERRY_LEAVE_FOR_TALKING_IN_1_MINUTE = 989;
	public static final int FERRY_LEAVE_SOON_FOR_TALKING = 990;
	public static final int FERRY_LEAVING_FOR_TALKING = 991;
	public static final int FERRY_ARRIVED_AT_GIRAN = 992;
	public static final int FERRY_LEAVE_FOR_GIRAN_AFTER_10_MINUTES = 993;
	public static final int FERRY_LEAVE_FOR_GIRAN_IN_5_MINUTES = 994;
	public static final int FERRY_LEAVE_FOR_GIRAN_IN_1_MINUTE = 995;
	public static final int FERRY_LEAVE_SOON_FOR_GIRAN = 996;
	public static final int FERRY_LEAVING_FOR_GIRAN = 997;
	public static final int INNADRIL_BOAT_ANCHOR_10_MINUTES = 998;
	public static final int INNADRIL_BOAT_LEAVE_IN_5_MINUTES = 999;
	public static final int INNADRIL_BOAT_LEAVE_IN_1_MINUTE = 1000;
	public static final int INNADRIL_BOAT_LEAVE_SOON = 1001;
	public static final int INNADRIL_BOAT_LEAVING = 1002;
	public static final int FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_10_MINUTES = 1159;
	public static final int FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_5_MINUTES = 1160;
	public static final int FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_1_MINUTE = 1161;
	public static final int FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_15_MINUTES = 1162;
	public static final int FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_10_MINUTES = 1163;
	public static final int FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_5_MINUTES = 1164;
	public static final int FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_1_MINUTE = 1165;
	public static final int FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_20_MINUTES = 1166;
	public static final int FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_15_MINUTES = 1167;
	public static final int FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_10_MINUTES = 1168;
	public static final int FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_5_MINUTES = 1169;
	public static final int FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_1_MINUTE = 1170;
	public static final int INNADRIL_BOAT_ARRIVE_20_MINUTES = 1171;
	public static final int INNADRIL_BOAT_ARRIVE_15_MINUTES = 1172;
	public static final int INNADRIL_BOAT_ARRIVE_10_MINUTES = 1173;
	public static final int INNADRIL_BOAT_ARRIVE_5_MINUTES = 1174;
	public static final int INNADRIL_BOAT_ARRIVE_1_MINUTE = 1175;
	public static final int FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_10_MINUTES = 1191;
	public static final int FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_5_MINUTES = 1192;
	public static final int FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_1_MINUTE = 1193;
	public static final int FERRY_TALKING_GLUDIN_DELAYED = 1485;
	public static final int FERRY_GLUDIN_TALKING_DELAYED = 1486;
	public static final int FERRY_GIRAN_TALKING_DELAYED = 1487;
	public static final int FERRY_TALKING_GIRAN_DELAYED = 1488;
	public static final int INNADRIL_BOAT_DELAYED = 1489;
	public static final int FERRY_RUNE_GLUDIN_DELAYED = 1618;
	public static final int FERRY_GLUDIN_RUNE_DELAYED = 1619;
	public static final int ARRIVED_AT_RUNE = 1620;
	public static final int DEPARTURE_FOR_GLUDIN_5_MINUTES = 1621;
	public static final int DEPARTURE_FOR_GLUDIN_1_MINUTE = 1622;
	public static final int DEPARTURE_FOR_GLUDIN_SHORTLY = 1623;
	public static final int DEPARTURE_FOR_GLUDIN_NOW = 1624;
	public static final int DEPARTURE_FOR_RUNE_10_MINUTES = 1625;
	public static final int DEPARTURE_FOR_RUNE_5_MINUTES = 1626;
	public static final int DEPARTURE_FOR_RUNE_1_MINUTE = 1627;
	public static final int DEPARTURE_FOR_GLUDIN_SHORTLY2 = 1628;
	public static final int DEPARTURE_FOR_RUNE_NOW = 1629;
	public static final int FERRY_FROM_RUNE_AT_GLUDIN_15_MINUTES = 1630;
	public static final int FERRY_FROM_RUNE_AT_GLUDIN_10_MINUTES = 1631;
	public static final int FERRY_FROM_RUNE_AT_GLUDIN_5_MINUTES = 1632;
	public static final int FERRY_FROM_RUNE_AT_GLUDIN_1_MINUTE = 1633;
	public static final int FERRY_FROM_GLUDIN_AT_RUNE_15_MINUTES = 1634;
	public static final int FERRY_FROM_GLUDIN_AT_RUNE_10_MINUTES = 1635;
	public static final int FERRY_FROM_GLUDIN_AT_RUNE_5_MINUTES = 1636;
	public static final int FERRY_FROM_GLUDIN_AT_RUNE_1_MINUTE = 1637;
	public static final int FERRY_ARRIVED_AT_PRIMEVAL = 1988;
	public static final int FERRY_LEAVING_FOR_RUNE_3_MINUTES = 1989;
	public static final int FERRY_LEAVING_PRIMEVAL_FOR_RUNE_NOW = 1990;
	public static final int FERRY_LEAVING_FOR_PRIMEVAL_3_MINUTES = 1991;
	public static final int FERRY_LEAVING_RUNE_FOR_PRIMEVAL_NOW = 1992;
	public static final int FERRY_FROM_PRIMEVAL_TO_RUNE_DELAYED = 1993;
	public static final int FERRY_FROM_RUNE_TO_PRIMEVAL_DELAYED = 1994;

	// Augmentation
	public static final int SELECT_THE_ITEM_TO_BE_AUGMENTED = 1957;
	public static final int SELECT_THE_CATALYST_FOR_AUGMENTATION = 1958;
	public static final int REQUIRES_S1_S2 = 1959;
	public static final int THIS_IS_NOT_A_SUITABLE_ITEM = 1960;
	public static final int GEMSTONE_QUANTITY_IS_INCORRECT = 1961;
	public static final int THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED = 1962;
	public static final int SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION = 1963;
	public static final int AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM = 1964;
	public static final int AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1 = 1965;
	public static final int ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN = 1970;

	public static final int YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION = 1972;
	public static final int YOU_CANNOT_AUGMENT_ITEMS_WHILE_FROZEN = 1973;
	public static final int YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD = 1974;
	public static final int YOU_CANNOT_AUGMENT_ITEMS_WHILE_TRADE = 1975;
	public static final int YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED = 1976;
	public static final int YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING = 1977;
	public static final int YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN = 1978;

	public static final int S1 = 1983;

	public static final int PRESS_THE_AUGMENT_BUTTON_TO_BEGIN = 1984;
	public static final int AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS = 2001;

	// Shadow items
	public static final int S1S_REMAINING_MANA_IS_NOW_10 = 1979;
	public static final int S1S_REMAINING_MANA_IS_NOW_5 = 1980;
	public static final int S1S_REMAINING_MANA_IS_NOW_1 = 1981;
	public static final int S1S_REMAINING_MANA_IS_NOW_0 = 1982;

	// Абсорбация душ
	public static final int THERE_IS_NOT_ENOUGHT_SOUL = 2195;

	// Pc Bang Points
	public static final int PC_BANG_POINTS_ACQUISITION_PERIOD_PONTS_ACQUISITION_PERIOD_LEFT_S1_HOUR = 1705;
	public static final int PC_BANG_POINTS_USE_PERIOD_POINTS_USE_PERIOD_LEFT_S1_HOUR = 1706;
	public static final int YOU_ACQUIRED_S1_PC_BANG_POINT = 1707;
	public static final int DOUBLE_POINTS_YOU_AQUIRED_S1_PC_BANG_POINT = 1708;
	public static final int YOU_ARE_USING_S1_POINT = 1709;
	public static final int YOU_ARE_SHORT_OF_ACCUMULATED_POINTS = 1710;
	public static final int PC_BANG_POINTS_USE_PERIOD_HAS_EXPIRED = 1711;
	public static final int THE_PC_BANG_POINTS_ACCUMULATION_PERIOD_HAS_EXPIRED = 1712;

	public static final int C1_HAS_RECEIVED_S3_DAMAGE_FROM_C2 = 2262;
	public static final int S1_HAS_BEEN_SUCCESSFULLY_ENCHANTED2 = 7489;
	public static final int _S1_S2_HAS_BEEN_SUCCESSFULLY_ENCHANTED2 = 7490;

	public static final int C1_HAS_DONE_S3_POINTS_OF_DAMAGE_TO_C2 = 2261;

	public SystemMessage(int messageId)
	{
		_messageId = messageId;
	}

	public SystemMessage(String msg)
	{
		this(S1);
		addString(msg);
	}

	public SystemMessage(CustomMessage message)
	{
		this(message.toString());
	}

	public int getId()
	{
		return _messageId;
	}

	public SystemMessage addString(String text)
	{
		if(text == null)
		{
			System.out.println("NPE Situation!!!");
			Thread.dumpStack();
			text = "";
		}
		args.add(new Arg(TYPE_TEXT, text));
		return this;
	}

	public SystemMessage addNumber(Integer number)
	{
		args.add(new Arg(TYPE_NUMBER, number));
		return this;
	}

	public SystemMessage addNumber(Long number)
	{
		addNumber(new Integer((int) Math.min(number, Integer.MAX_VALUE)));
		return this;
	}

	public SystemMessage addNumber(Short number)
	{
		addNumber(new Integer(number));
		return this;
	}

	public SystemMessage addNumber(Byte number)
	{
		addNumber(new Integer(number));
		return this;
	}

	public SystemMessage addNpcName(int id)
	{
		args.add(new Arg(TYPE_NPC_NAME, new Integer(1000000 + id)));
		return this;
	}

	public SystemMessage addItemName(Integer id)
	{
		args.add(new Arg(TYPE_ITEM_NAME, id));
		return this;
	}

	public SystemMessage addItemName(Short id)
	{
		addItemName(new Integer(id));
		return this;
	}

	public SystemMessage addZoneName(int x, int y, int z)
	{
		args.add(new Arg(TYPE_ZONE_NAME, new int[] { x, y, z }));
		return this;
	}

	public SystemMessage addVisibleDamage(int targetId, int attackerId, int damage)
	{
		args.add(new Arg(TYPE_HP_CHANGE, new int[] { targetId, attackerId, damage }));
		return this;
	}

	public SystemMessage addSkillName(int id, int level)
	{
		args.add(new Arg(TYPE_SKILL_NAME, new int[] { id, level }));
		return this;
	}

	@Override
	protected final void writeImpl()
	{
		writeH(_messageId);
		writeC(args.size());
		for(Arg e : args)
		{
			writeC(e.type);

			switch(e.type)
			{
				case TYPE_TEXT:
				{
					writeS((String) e.obj);
					break;
				}
				case TYPE_NUMBER:
				case TYPE_NPC_NAME:
				case TYPE_ITEM_NAME:
				{
					writeD(((Number) e.obj).intValue());
					break;
				}
				case TYPE_SKILL_NAME:
				{
					int[] skill = (int[]) e.obj;
					int skillId = skill[0];
					int skillLevel = skill[1];
					if(skillLevel < 100)
					{
						writeD(skillId); // id
						writeH(skillLevel); // level
						writeH(0);
					}
					else
					{
						int baseLevel = SkillTree.getBaseLevels().get(skillId);
						int step = skillLevel % 100;
						int subLevel = (1 + step / 40) * 1000 + step % 40;

						writeD(skillId); // id скилла или object id вещи
						writeH(baseLevel);
						writeH(subLevel);
					}
					break;
				}
				case TYPE_ZONE_NAME:
				{
					int[] coord = (int[]) e.obj;
					writeD(coord[0]);
					writeD(coord[1]);
					writeD(coord[2]);
					break;
				}
				case TYPE_HP_CHANGE:
				{
					int[] object = (int[]) e.obj;
					writeD(object[0]);
					writeD(object[1]);
					writeD(object[2]);
					break;
				}
			}
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_messageId);
		writeD(args.size());
		for(Arg e : args)
		{
			writeD(e.type);

			switch(e.type)
			{
				case TYPE_TEXT:
				{
					writeS((String) e.obj);
					break;
				}
				case TYPE_NUMBER:
				case TYPE_NPC_NAME:
				case TYPE_ITEM_NAME:
				{
					writeD(((Number) e.obj).intValue());
					break;
				}
				case TYPE_SKILL_NAME:
				{
					int[] skill = (int[]) e.obj;
					writeD(skill[0]); // id
					writeD(skill[1]); // level
					break;
				}
				case TYPE_ZONE_NAME:
				{
					int[] coord = (int[]) e.obj;
					writeD(coord[0]);
					writeD(coord[1]);
					writeD(coord[2]);
					break;
				}
			}
		}
	}

	public SystemMessage addName(L2Character cha)
	{
		if(cha == null)
			return addString("null");
		if(cha.getNpcId() <= 0)
			return addString(cha.getName());
		return addNpcName(cha.getNpcId());
	}

	private class Arg
	{
		public final int type;
		public final Object obj;

		private Arg(int _type, Object _obj)
		{
			type = _type;
			obj = _obj;
		}
	}

	public static SystemMessage obtainItems(int item_id, long count, int enchantLevel)
	{
		if(item_id == 57)
			return new SystemMessage(SystemMessage.EARNED_S1_ADENA).addNumber(count);
		if(count > 1)
			return new SystemMessage(SystemMessage.EARNED_S2_S1_s).addItemName(item_id).addNumber(count);
		if(enchantLevel > 0)
			return new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED__S1_S2).addNumber(enchantLevel).addItemName(item_id);
		return new SystemMessage(SystemMessage.EARNED_S1).addItemName(item_id);
	}

	public static SystemMessage obtainItems(L2ItemInstance item)
	{
		return obtainItems(item.getItemId(), item.getCount(), item.isEquipable() ? item.getEnchantLevel() : 0);
	}

	public static SystemMessage obtainItemsBy(int item_id, long count, int enchantLevel, String targetName)
	{
		if(count > 1)
			return new SystemMessage(SystemMessage.S1_HAS_OBTAINED_S3_S2).addString(targetName).addItemName(item_id).addNumber(count);
		if(enchantLevel > 0)
			return new SystemMessage(SystemMessage.S1_HAS_OBTAINED__S2S3).addString(targetName).addNumber(enchantLevel).addItemName(item_id);
		return new SystemMessage(SystemMessage.S1_HAS_OBTAINED_S2).addString(targetName).addItemName(item_id);
	}

	public static SystemMessage obtainItemsBy(L2ItemInstance item, String targetName)
	{
		return obtainItemsBy(item.getItemId(), item.getCount(), item.isEquipable() ? item.getEnchantLevel() : 0, targetName);
	}

	public static SystemMessage removeItems(int item_id, long count, int enchantLevel)
	{
		if(item_id == 57)
			return new SystemMessage(SystemMessage.S1_ADENA_DISAPPEARED).addNumber(count);
		if(count > 1)
			return new SystemMessage(SystemMessage.S2_S1_HAS_DISAPPEARED).addItemName(item_id).addNumber(count);
		if(enchantLevel > 0)
			return new SystemMessage(SystemMessage._S1_S2_DISAPPEARED).addNumber(enchantLevel).addItemName(item_id);
		return new SystemMessage(SystemMessage.S1_HAS_DISAPPEARED).addItemName(item_id);
	}

	public static SystemMessage removeItems(int item_id, long count)
	{
		return removeItems(item_id, count, 0);
	}

	public static SystemMessage removeItems(L2ItemInstance item)
	{
		return removeItems(item.getItemId(), item.getCount(), item.getEnchantLevel());
	}
}