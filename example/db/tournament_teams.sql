CREATE TABLE `tournament_teams` (
  `obj_id` int(11) NOT NULL default '0',
  `type` int(11) default NULL,
  `team_id` int(11) NOT NULL,
  `team_name` varchar(255) default NULL,
  `leader` int(11) default NULL,
  `category` int(11) default NULL,
  `wins` int(11) NOT NULL default '0',
  `losts` int(11) NOT NULL default '0',
  `status` int(11) NOT NULL default '0'
) ENGINE=MyISAM;