CREATE TABLE IF NOT EXISTS `castle` (
  `id` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `name` varchar(25) NOT NULL,
  `tax_percent` int(11) NOT NULL,
  `treasury` bigint(20) unsigned NOT NULL DEFAULT '0',
  `last_siege_date` bigint(20) NOT NULL,
  `own_date` bigint(20) NOT NULL,
  `siege_date` bigint(20) NOT NULL,
  `reward_count` int(11) NOT NULL,
  PRIMARY KEY (`id`)
);

INSERT INTO `castle` VALUES
('5', 'Aden', '0', '0', '0', '0', '0', '0'),
('2', 'Dion', '0', '0', '0', '0', '0', '0'),
('3', 'Giran', '0', '0', '0', '0', '0', '0'),
('1', 'Gludio', '0', '0', '0', '0', '0', '0'),
('7', 'Goddard', '0', '0', '0', '0', '0', '0'),
('6', 'Innadril', '0', '0', '0', '0', '0', '0'),
('4', 'Oren', '0', '0', '0', '0', '0', '0'),
('8', 'Rune', '0', '0', '0', '0', '0', '0'),
('9', 'Schuttgart', '0', '0', '0', '0', '0', '0');