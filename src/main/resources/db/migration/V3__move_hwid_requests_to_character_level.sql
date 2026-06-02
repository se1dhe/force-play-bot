alter table hwid_requests
    add column character_id bigint references characters(id);

create index if not exists idx_hwid_requests_character_id on hwid_requests(character_id);
