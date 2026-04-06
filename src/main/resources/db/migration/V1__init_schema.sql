create table users (
    id bigserial primary key,
    telegram_id bigint not null unique,
    language varchar(10) not null,
    created_at timestamp with time zone not null
);

create table accounts (
    id bigserial primary key,
    external_account_id varchar(100) not null,
    server_name varchar(100) not null,
    hwid varchar(255),
    user_id bigint not null references users(id),
    created_at timestamp with time zone not null
);

create unique index uk_accounts_external_server on accounts (external_account_id, server_name);

create table characters (
    id bigserial primary key,
    name varchar(100) not null,
    account_id bigint not null references accounts(id) on delete cascade
);

create unique index uk_character_name_account on characters (name, account_id);

create table hwid_requests (
    id bigserial primary key,
    account_id bigint not null references accounts(id),
    new_hwid varchar(255) not null,
    status varchar(20) not null,
    expires_at timestamp with time zone not null,
    created_at timestamp with time zone not null
);

create table promo_codes (
    id bigserial primary key,
    code varchar(100) not null unique,
    server_name varchar(100) not null,
    used_by bigint references users(id),
    used_at timestamp with time zone,
    created_at timestamp with time zone not null
);

create table referrals (
    id bigserial primary key,
    user_id bigint not null references users(id),
    referred_user_id bigint not null references users(id),
    created_at timestamp with time zone not null,
    constraint uk_referral_pair unique (user_id, referred_user_id)
);
