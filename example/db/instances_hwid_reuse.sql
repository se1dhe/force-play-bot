CREATE TABLE IF NOT EXISTS `instances_hwid_reuse` (
    `id` INT NOT NULL DEFAULT '0',
    `hwid` VARCHAR(64) CHARACTER SET UTF8 DEFAULT '',
    `reuse` BIGINT(20) NOT NULL DEFAULT '0',
    UNIQUE KEY `prim` (`id`,`hwid`),
    KEY `hwid` (`hwid`)
) ENGINE=MyISAM;