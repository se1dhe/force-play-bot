update tarot_seasons
set name = 'Хроники Эльморадена',
    description = 'Карты судьбы Lineage 2: рейдовые боссы, древние печати, оружие героев и трофеи охотников.',
    active = true
where id = 1;

update tarot_decks
set name = 'Колода Семи Печатей',
    description = 'Колода, собранная из реликвий Эльморадена: от Adena и кристаллов до оружия героев и эпик-бижутерии.',
    active = true
where id = 1;

with arcana_values(name, image, description, rarity, pos) as (
    values
        ('Путник Глудио', '/tarot/icons/etc_adena_i00.png', 'Первый выход из города, легкая сумка Adena и дорога к первой охоте.', 'COMMON', 1),
        ('Кузнец Маммона', '/tarot/icons/etc_oriharukon_i00.png', 'Ремесло, заточка и обмен, где каждая руда может стать силой.', 'COMMON', 2),
        ('Оракул Евы', '/tarot/icons/etc_bless_of_eva_i00.png', 'Тихое благословение воды, которое хранит героя в долгом походе.', 'COMMON', 3),
        ('Леди Адена', '/tarot/icons/etc_ancient_adena_i00.png', 'Богатство торговых рядов, древняя Adena и удачная сделка.', 'COMMON', 4),
        ('Лорд Замка', '/tarot/icons/etc_ancient_crown_i02.png', 'Власть, осада и право держать знамя над стенами замка.', 'COMMON', 5),
        ('Жрец Семи Печатей', '/tarot/icons/etc_sp_scroll1_i00.png', 'Древний обет, печати и сила, открытая только терпеливым.', 'COMMON', 6),
        ('Клятва Клана', '/tarot/icons/accessory_ring_of_queen_ant_i00.png', 'Союз, верность и выбор, который меняет судьбу всего клана.', 'COMMON', 7),
        ('Осадная Колесница', '/tarot/icons/weapon_tallum_blade_i00.png', 'Рывок сквозь ворота, боевой приказ и победа в правильный момент.', 'RARE', 8),
        ('Титан Арены', '/tarot/icons/weapon_heavens_divider_i00.png', 'Грубая сила, выдержка и удар, после которого спор окончен.', 'RARE', 9),
        ('Отшельник Башни Дерзости', '/tarot/icons/etc_sp_scroll2_i00.png', 'Одинокий подъем по этажам, где знание дороже быстрой славы.', 'COMMON', 10),
        ('Колесо Респауна', '/tarot/icons/etc_treasure_box_i00.png', 'Время босса, спор за спот и шанс, который приходит по расписанию.', 'RARE', 11),
        ('Закон Олимпиады', '/tarot/icons/etc_crystal_silver_i00.png', 'Честная дуэль, счет очков и цена каждого решения.', 'COMMON', 12),
        ('Застывший Бафф', '/tarot/icons/etc_crystal_blue_i00.png', 'Пауза перед рывком: иногда победа начинается с ожидания.', 'COMMON', 13),
        ('Падение Закена', '/tarot/icons/weapon_dark_screamer_i00.png', 'Конец старой фазы и новый заход, где слабость превращается в добычу.', 'RARE', 14),
        ('Баланс Евы', '/tarot/icons/etc_crystal_red_i00.png', 'Точный ритм боя, расходников и отступления, когда лишний шаг стоит жизни.', 'COMMON', 15),
        ('Искушение Антараса', '/tarot/icons/accessory_earring_of_antaras_i00.png', 'Сила дракона манит, но за каждую легендарную вещь платят кровью.', 'RARE', 16),
        ('Башня Баюма', '/tarot/icons/accessory_ring_of_baium_i00.png', 'Разлом амбиций: вершина открывается тем, кто пережил падение.', 'RARE', 17),
        ('Звезда Гирана', '/tarot/icons/etc_crystal_gold_i00.png', 'Свет рынка, редкий кристалл и цель, ради которой собирают пати.', 'RARE', 18),
        ('Луна Некрополя', '/tarot/icons/etc_sp_scroll3_i00.png', 'Тень катакомб, скрытая комната и награда за внимательность.', 'RARE', 19),
        ('Солнце Героя', '/tarot/icons/weapon_forgotten_blade_i00.png', 'Сияние геройского оружия и миг, когда весь сервер знает имя победителя.', 'RARE', 20),
        ('Суд Валакаса', '/tarot/icons/accessory_necklace_of_valakas_i00.png', 'Огненный приговор рейда: либо клан выдержит, либо пепел заберет всех.', 'RARE', 21),
        ('Корона Эльморадена', '/tarot/icons/etc_ancient_crown_i02.png', 'Полный круг хроник: замки, боссы, герои и добыча сходятся в одной карте.', 'ROYAL', 22)
)
update tarot_arcana a
set name = v.name,
    image = v.image,
    description = v.description,
    rarity = v.rarity
from (
    select id, row_number() over (order by id) as pos
    from tarot_arcana
    where season_id = 1
) ordered
join arcana_values v on v.pos = ordered.pos
where a.id = ordered.id;

with reward_values(name, description, icon, rarity, weight, enabled, pos) as (
    values
        ('Adena странника', 'Базовая добыча с охоты: немного Adena всегда двигает героя дальше.', '/tarot/icons/etc_adena_i00.png', 'COMMON', 80, true, 1),
        ('Ancient Adena Семи Печатей', 'Монеты катакомб и некрополей для тех, кто помнит старые хроники.', '/tarot/icons/etc_ancient_adena_i00.png', 'COMMON', 60, true, 2),
        ('Silver Crystal', 'Полезный кристалл для ремесла, обмена и подготовки экипировки.', '/tarot/icons/etc_crystal_silver_i00.png', 'COMMON', 54, true, 3),
        ('Red Crystal', 'Горячий ресурс для героя, который уже слышит зов следующего грейда.', '/tarot/icons/etc_crystal_red_i00.png', 'COMMON', 45, true, 4),
        ('Oriharukon Ore', 'Руда кузнецов, из которой начинается дорога к серьезному оружию.', '/tarot/icons/etc_oriharukon_ore_i00.png', 'COMMON', 42, true, 5),
        ('Scroll: Enchant Weapon A', 'Заточка для смелых: маленький шанс, большой азарт.', '/tarot/icons/etc_scroll_of_enchant_weapon_i04.png', 'RARE', 18, true, 6),
        ('Blessed Scroll: Enchant Weapon A', 'Благословенная попытка усилить оружие без лишней драмы.', '/tarot/icons/etc_blessed_scrl_of_ench_wp_a_i04.png', 'RARE', 11, true, 7),
        ('Tallum Blade', 'Клинок, который пахнет кузней, осадами и старой школой Lineage 2.', '/tarot/icons/weapon_tallum_blade_i00.png', 'RARE', 9, true, 8),
        ('Soul Bow', 'Лук для охотника, который предпочитает решать бой с дистанции.', '/tarot/icons/weapon_soul_bow_i00.png', 'RARE', 7, true, 9),
        ('Queen Ant Ring', 'Эпик-бижутерия низких хроник: маленькая икона, огромная память сервера.', '/tarot/icons/accessory_ring_of_queen_ant_i00.png', 'RARE', 5, true, 10),
        ('Antaras Earring', 'Королевский трофей дракона земли, добытый только сильным кланом.', '/tarot/icons/accessory_earring_of_antaras_i00.png', 'ROYAL', 1, true, 11),
        ('Valakas Necklace', 'Огненная реликвия Валакаса и знак настоящей серверной легенды.', '/tarot/icons/accessory_necklace_of_valakas_i00.png', 'ROYAL', 1, true, 12),
        ('Baium Ring', 'Кольцо с вершины Tower of Insolence, где амбиции проверяются рейдом.', '/tarot/icons/accessory_ring_of_baium_i00.png', 'ROYAL', 1, true, 13)
),
ordered_rewards as (
    select id, row_number() over (order by id) as pos
    from tarot_rewards
    where season_id = 1
)
update tarot_rewards r
set name = v.name,
    description = v.description,
    icon = v.icon,
    rarity = v.rarity,
    weight = v.weight,
    enabled = v.enabled
from ordered_rewards o
join reward_values v on v.pos = o.pos
where r.id = o.id;

with reward_values(name, description, icon, rarity, weight, enabled, pos) as (
    values
        ('Adena странника', 'Базовая добыча с охоты: немного Adena всегда двигает героя дальше.', '/tarot/icons/etc_adena_i00.png', 'COMMON', 80, true, 1),
        ('Ancient Adena Семи Печатей', 'Монеты катакомб и некрополей для тех, кто помнит старые хроники.', '/tarot/icons/etc_ancient_adena_i00.png', 'COMMON', 60, true, 2),
        ('Silver Crystal', 'Полезный кристалл для ремесла, обмена и подготовки экипировки.', '/tarot/icons/etc_crystal_silver_i00.png', 'COMMON', 54, true, 3),
        ('Red Crystal', 'Горячий ресурс для героя, который уже слышит зов следующего грейда.', '/tarot/icons/etc_crystal_red_i00.png', 'COMMON', 45, true, 4),
        ('Oriharukon Ore', 'Руда кузнецов, из которой начинается дорога к серьезному оружию.', '/tarot/icons/etc_oriharukon_ore_i00.png', 'COMMON', 42, true, 5),
        ('Scroll: Enchant Weapon A', 'Заточка для смелых: маленький шанс, большой азарт.', '/tarot/icons/etc_scroll_of_enchant_weapon_i04.png', 'RARE', 18, true, 6),
        ('Blessed Scroll: Enchant Weapon A', 'Благословенная попытка усилить оружие без лишней драмы.', '/tarot/icons/etc_blessed_scrl_of_ench_wp_a_i04.png', 'RARE', 11, true, 7),
        ('Tallum Blade', 'Клинок, который пахнет кузней, осадами и старой школой Lineage 2.', '/tarot/icons/weapon_tallum_blade_i00.png', 'RARE', 9, true, 8),
        ('Soul Bow', 'Лук для охотника, который предпочитает решать бой с дистанции.', '/tarot/icons/weapon_soul_bow_i00.png', 'RARE', 7, true, 9),
        ('Queen Ant Ring', 'Эпик-бижутерия низких хроник: маленькая икона, огромная память сервера.', '/tarot/icons/accessory_ring_of_queen_ant_i00.png', 'RARE', 5, true, 10),
        ('Antaras Earring', 'Королевский трофей дракона земли, добытый только сильным кланом.', '/tarot/icons/accessory_earring_of_antaras_i00.png', 'ROYAL', 1, true, 11),
        ('Valakas Necklace', 'Огненная реликвия Валакаса и знак настоящей серверной легенды.', '/tarot/icons/accessory_necklace_of_valakas_i00.png', 'ROYAL', 1, true, 12),
        ('Baium Ring', 'Кольцо с вершины Tower of Insolence, где амбиции проверяются рейдом.', '/tarot/icons/accessory_ring_of_baium_i00.png', 'ROYAL', 1, true, 13)
)
insert into tarot_rewards (name, description, icon, rarity, weight, enabled, season_id)
select v.name, v.description, v.icon, v.rarity, v.weight, v.enabled, 1
from reward_values v
where not exists (
    select 1
    from tarot_rewards r
    where r.season_id = 1 and r.name = v.name
);

update tarot_settings
set value = '/tarot/assets/astaria_background.jpg'
where key = 'background';

update tarot_purchase_packages
set enabled = true
where code in ('draw_1', 'draw_5', 'draw_10');
