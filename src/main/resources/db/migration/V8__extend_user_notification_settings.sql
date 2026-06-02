ALTER TABLE users
    ADD COLUMN announce_autofarm_death BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN announce_server_restart BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN announce_new_hwid_login BOOLEAN NOT NULL DEFAULT TRUE;
