CREATE TABLE IF NOT EXISTS `global_variables` (
	`name` VARCHAR(86) CHARACTER SET UTF8 NOT NULL DEFAULT '0',
	`value` VARCHAR(4000) CHARACTER SET UTF8 NOT NULL DEFAULT '0',
	`expire_time` bigint(20) NOT NULL DEFAULT '0',
	UNIQUE KEY `prim` (`name`),
	KEY `name` (`name`),
	KEY `value` (`value`),
	KEY `expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;