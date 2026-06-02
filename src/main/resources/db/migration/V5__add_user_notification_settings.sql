alter table users
    add column announce_boss_spawn boolean not null default false,
    add column announce_event_start boolean not null default false;
