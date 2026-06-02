CREATE TABLE IF NOT EXISTS total
	SELECT
		sum(p.price*i.count) as `sum`,
		i.item_id,
		i.owner_id,
		c.char_name,
		c.account_name,
		c.accesslevel
	FROM items i
		LEFT JOIN characters c ON (i.owner_id = c.obj_Id)
		LEFT JOIN prices p ON i.item_id = p.item_id
	GROUP BY owner_id ORDER BY `sum` DESC;

ALTER TABLE `total` ADD PRIMARY KEY ( `owner_id` );

CREATE TABLE IF NOT EXISTS too_many
	SELECT i.item_id, i.count, p.price, i.count*p.price AS `sum`, p.name, i.owner_id,  c.char_name, cs.level, c.account_name, c.accesslevel
	FROM items i
		LEFT JOIN characters c ON (i.owner_id = c.obj_Id)
		LEFT JOIN character_subclasses cs ON (cs.char_obj_id = c.obj_Id)
		LEFT JOIN prices p ON (p.item_id = i.item_id)
	WHERE ((i.count > 100000 AND i.item_id <> 57)
			OR (i.count*p.price > 10000000 and i.item_id <> 57)
			OR (i.count*p.price > 30000000 and i.item_id = 57)
			OR i.count < 0
			OR i.count*p.price < 0
		)
		AND c.accesslevel < 100
	ORDER BY `sum` DESC;
