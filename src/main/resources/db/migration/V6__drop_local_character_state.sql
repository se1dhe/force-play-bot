alter table accounts
    drop column if exists allowed_hwid_primary,
    drop column if exists allowed_hwid_secondary;

alter table characters
    drop column if exists last_hwid,
    drop column if exists lock_hwid_primary,
    drop column if exists lock_hwid_secondary,
    drop column if exists trade_key_installed,
    drop column if exists trade_key_enabled;
