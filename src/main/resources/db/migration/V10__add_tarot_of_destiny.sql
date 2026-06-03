create table tarot_seasons (
    id bigserial primary key,
    name varchar(120) not null,
    description text,
    start_date timestamptz not null,
    end_date timestamptz,
    active boolean not null default false
);

create table tarot_decks (
    id bigserial primary key,
    name varchar(120) not null,
    description text,
    active boolean not null default false,
    season_id bigint not null references tarot_seasons(id)
);

create table tarot_arcana (
    id bigserial primary key,
    name varchar(120) not null,
    image varchar(255),
    description text,
    rarity varchar(20) not null,
    season_id bigint not null references tarot_seasons(id)
);

create table tarot_rewards (
    id bigserial primary key,
    name varchar(120) not null,
    description text,
    icon varchar(80),
    rarity varchar(20) not null,
    weight integer not null,
    enabled boolean not null default true,
    season_id bigint not null references tarot_seasons(id)
);

create table tarot_user_profiles (
    id bigserial primary key,
    user_id bigint not null unique references users(id) on delete cascade,
    free_draws_used_date date,
    free_draws_used integer not null default 0,
    purchased_draws integer not null default 0,
    pity_counter integer not null default 0,
    vip_until timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint
);

create table tarot_draws (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    season_id bigint not null references tarot_seasons(id),
    deck_id bigint not null references tarot_decks(id),
    status varchar(20) not null,
    guaranteed_royal boolean not null default false,
    selected_card_index integer,
    selected_reward_id bigint references tarot_rewards(id),
    selected_arcana_id bigint references tarot_arcana(id),
    created_at timestamptz not null,
    revealed_at timestamptz,
    claimed_at timestamptz,
    version bigint
);

create table tarot_draw_cards (
    id bigserial primary key,
    draw_id bigint not null references tarot_draws(id) on delete cascade,
    card_index integer not null,
    reward_id bigint not null references tarot_rewards(id),
    arcana_id bigint not null references tarot_arcana(id),
    royal_card boolean not null default false,
    selected boolean not null default false,
    unique (draw_id, card_index)
);

create table user_tarot_arcana (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    arcana_id bigint not null references tarot_arcana(id),
    first_opened_at timestamptz not null,
    unique (user_id, arcana_id)
);

create table tarot_feed_entries (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    reward_id bigint not null references tarot_rewards(id),
    arcana_id bigint not null references tarot_arcana(id),
    rarity varchar(20) not null,
    created_at timestamptz not null
);

create table tarot_purchases (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    package_code varchar(40) not null,
    draws integer not null,
    stars_amount integer not null,
    payload varchar(160) not null unique,
    telegram_payment_charge_id varchar(120),
    status varchar(20) not null,
    created_at timestamptz not null,
    paid_at timestamptz
);

create table tarot_settings (
    key varchar(80) primary key,
    value varchar(255) not null
);

create table tarot_purchase_packages (
    code varchar(40) primary key,
    draws integer not null,
    stars_price integer not null,
    enabled boolean not null default true
);

create index idx_tarot_decks_active on tarot_decks(season_id, active);
create index idx_tarot_rewards_pool on tarot_rewards(season_id, enabled, rarity);
create index idx_tarot_draws_user_status on tarot_draws(user_id, status, created_at desc);
create index idx_tarot_feed_created on tarot_feed_entries(created_at desc);
create index idx_tarot_purchases_status on tarot_purchases(status);

insert into tarot_seasons (id, name, description, start_date, active)
values (1, 'Сезон Астарии', 'Первый сезон Таро Судьбы в мире Force Play.', now(), true);

insert into tarot_decks (id, name, description, active, season_id)
values (1, 'Колода Судьбы', 'Базовая колода древней прорицательницы Астарии.', true, 1);

insert into tarot_arcana (name, image, description, rarity, season_id) values
('Шут', null, 'Начало пути и рискованный шаг навстречу судьбе.', 'COMMON', 1),
('Маг', null, 'Воля, концентрация и сила первого удара.', 'COMMON', 1),
('Жрица', null, 'Тайное знание, которое приходит в тишине.', 'COMMON', 1),
('Императрица', null, 'Изобилие, рост и щедрость мира.', 'COMMON', 1),
('Император', null, 'Порядок, власть и крепкая защита.', 'COMMON', 1),
('Иерофант', null, 'Древний закон и благословение наставников.', 'COMMON', 1),
('Влюбленные', null, 'Выбор, который меняет дорогу героя.', 'COMMON', 1),
('Колесница', null, 'Напор, движение и победа над хаосом.', 'RARE', 1),
('Сила', null, 'Тихая мощь и контроль над яростью.', 'RARE', 1),
('Отшельник', null, 'Поиск истины в глубине пути.', 'COMMON', 1),
('Колесо Фортуны', null, 'Поворот судьбы, который нельзя удержать.', 'RARE', 1),
('Справедливость', null, 'Равновесие, цена и честный итог.', 'COMMON', 1),
('Повешенный', null, 'Пауза перед новым пониманием.', 'COMMON', 1),
('Смерть', null, 'Конец старого и неизбежное обновление.', 'RARE', 1),
('Умеренность', null, 'Баланс, терпение и верный ритм.', 'COMMON', 1),
('Дьявол', null, 'Искушение силой и дорогая победа.', 'RARE', 1),
('Башня', null, 'Разлом, после которого открывается правда.', 'RARE', 1),
('Звезда', null, 'Надежда, свет и дальняя цель.', 'RARE', 1),
('Луна', null, 'Тени, интуиция и скрытая тропа.', 'RARE', 1),
('Солнце', null, 'Ясность, радость и благой знак.', 'RARE', 1),
('Суд', null, 'Пробуждение и зов к решающему действию.', 'RARE', 1),
('Мир', null, 'Завершение круга и большая награда.', 'ROYAL', 1);

insert into tarot_rewards (name, description, icon, rarity, weight, enabled, season_id) values
('Свиток удачи', 'Малый знак благосклонности Астарии.', '📜', 'COMMON', 80, true, 1),
('Сундук странника', 'Полезная награда для ежедневного пути.', '🎁', 'COMMON', 60, true, 1),
('Пыль древних рун', 'Материал, найденный на границе пророчества.', '✨', 'COMMON', 45, true, 1),
('Благословение охотника', 'Редкая награда для тех, кто идет дальше.', '⚔️', 'RARE', 18, true, 1),
('Серебряный знак судьбы', 'Редкий знак, который выделяет героя.', '🌙', 'RARE', 10, true, 1),
('Карта Короля Судьбы', 'Королевская награда из особого пула.', '👑', 'ROYAL', 1, true, 1),
('Королевский сундук Астарии', 'Большая награда за терпение и удачу.', '👑', 'ROYAL', 1, true, 1);

insert into tarot_settings (key, value) values
('pity_threshold', '50'),
('free_draws_regular', '1'),
('free_draws_vip', '2'),
('background', '/tarot/assets/astaria_background.jpg'),
('card_back_1', '/tarot/assets/card_back_1.jpg'),
('card_back_2', '/tarot/assets/card_back_1.jpg');

insert into tarot_purchase_packages (code, draws, stars_price, enabled) values
('draw_1', 1, 25, true),
('draw_5', 5, 100, true),
('draw_10', 10, 180, true);

select setval('tarot_seasons_id_seq', (select max(id) from tarot_seasons));
select setval('tarot_decks_id_seq', (select max(id) from tarot_decks));
