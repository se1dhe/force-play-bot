CREATE TABLE IF NOT EXISTS `character_blocklist` (
  `obj_Id` int(10) NOT NULL,
  `target_Id` int(10) NOT NULL,
  `target_Name` varchar(35) CHARACTER SET UTF8 NOT NULL,
  PRIMARY KEY  (`obj_Id`,`target_Id`,`target_Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;