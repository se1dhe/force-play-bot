DROP TABLE IF EXISTS `casino_rooms`;
CREATE TABLE `casino_rooms`
(
    `id` INT NOT NULL AUTO_INCREMENT,
    `creator_id` INT NOT NULL,
    `name` VARCHAR(35),
    `bet_item` INT NOT NULL,
    `bet_price` INT NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=MyISAM;

DROP TABLE IF EXISTS `casino_history`;
CREATE TABLE `casino_history`
(
    `obj_id_1` INT NOT NULL,
    `obj_id_2` INT NOT NULL,
    `bet_item` INT NOT NULL,
    `bet_price` INT NOT NULL,
    `date` INT UNSIGNED NOT NULL,
    `winner_id` INT NOT NULL,
    `score_1` TINYINT NOT NULL DEFAULT 0,
    `score_2` TINYINT NOT NULL DEFAULT 0
) ENGINE=MyISAM;

DROP TABLE IF EXISTS `casino_stats`;
CREATE TABLE `casino_stats` (
    `item_id` INT(11) NOT NULL,
    `value` BIGINT(20) NOT NULL DEFAULT 0,
    `count` INT NOT NULL,
    PRIMARY KEY (`item_id`)
) ENGINE=InnoDB;