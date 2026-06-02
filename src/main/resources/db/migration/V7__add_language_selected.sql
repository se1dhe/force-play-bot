alter table users
    add column if not exists language_selected boolean not null default false;

