DROP TABLE IF EXISTS `agathions`;
CREATE TABLE `agathions` (
`item_obj_id` decimal(11) NOT NULL default 0,
`objId` decimal(11),
`owner_id` int(11) NOT NULL default 0,
PRIMARY KEY(`item_obj_id`),
INDEX(`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;