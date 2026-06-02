CREATE TABLE IF NOT EXISTS `seven_signs_status` (
  `current_cycle` INT NOT NULL default '1',
  `festival_cycle` INT NOT NULL default '1',
  `active_period` INT NOT NULL default '1',
  `date` INT(11) NOT NULL default '1',
  `previous_winner` INT NOT NULL default '0',
  `dawn_stone_score` decimal(16,0) NOT NULL default '0',
  `dawn_festival_score` decimal(16,0) NOT NULL default '0',
  `dusk_stone_score` decimal(16,0) NOT NULL default '0',
  `dusk_festival_score` decimal(16,0) NOT NULL default '0',
  `avarice_owner` INT NOT NULL default '0',
  `gnosis_owner` INT NOT NULL default '0',
  `strife_owner` INT NOT NULL default '0',
  `avarice_dawn_score` INT NOT NULL default '0',
  `gnosis_dawn_score` INT NOT NULL default '0',
  `strife_dawn_score` INT NOT NULL default '0',
  `avarice_dusk_score` INT NOT NULL default '0',
  `gnosis_dusk_score` INT NOT NULL default '0',
  `strife_dusk_score` INT NOT NULL default '0',
  `accumulated_bonus0` INT NOT NULL default '0',
  `accumulated_bonus1` INT NOT NULL default '0',
  `accumulated_bonus2` INT NOT NULL default '0',
  `accumulated_bonus3` INT NOT NULL default '0',
  `accumulated_bonus4` INT NOT NULL default '0'
) ENGINE=MyISAM;
INSERT IGNORE `seven_signs_status` VALUES
(1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
