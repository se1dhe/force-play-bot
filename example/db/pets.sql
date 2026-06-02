DROP TABLE IF EXISTS `pets`;
CREATE TABLE `pets` (
`item_obj_id` decimal(11) NOT NULL default 0,
`objId` decimal(11),
`name` varchar(20) CHARACTER SET utf8,
`level` decimal(11),
`curHp` decimal(18,0),
`curMp` decimal(18,0),
`exp` BIGINT,
`sp` decimal(11) ,
`fed` decimal(11),
`owner_id` int(11) NOT NULL default 0,
PRIMARY KEY(`item_obj_id`),
INDEX(`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;