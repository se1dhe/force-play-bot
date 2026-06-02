CREATE TABLE IF NOT EXISTS `orderlist` (
	`id` INT(4) UNSIGNED NOT NULL PRIMARY KEY auto_increment,
	`client` INT NOT NULL DEFAULT 0,
	`target` INT NOT NULL DEFAULT 0,
	`price` INT NOT NULL,
	`item` INT NOT NULL,
	`end_time` bigint NOT NULL DEFAULT 0);