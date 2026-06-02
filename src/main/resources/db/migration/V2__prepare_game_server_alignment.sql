alter table accounts
    add column allowed_hwid_primary varchar(255),
    add column allowed_hwid_secondary varchar(255);

alter table characters
    add column external_character_id bigint,
    add column last_hwid varchar(255),
    add column lock_hwid_primary varchar(255),
    add column lock_hwid_secondary varchar(255),
    add column trade_key_installed boolean not null default false,
    add column trade_key_enabled boolean not null default false;

create unique index if not exists uk_character_external_account
    on characters (account_id, external_character_id)
    where external_character_id is not null;
