-- Run as a DBA only on an isolated staging MySQL 8 server.
-- Replace both password placeholders with separately generated `openssl rand -hex 32` values.
CREATE DATABASE IF NOT EXISTS library_management_staging CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'library_app_loadtest'@'%' IDENTIFIED BY 'REPLACE_WITH_64_HEX_APP_SECRET' REQUIRE SSL;
ALTER USER 'library_app_loadtest'@'%' IDENTIFIED BY 'REPLACE_WITH_64_HEX_APP_SECRET' REQUIRE SSL;
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'library_app_loadtest'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON library_management_staging.* TO 'library_app_loadtest'@'%';

CREATE USER IF NOT EXISTS 'library_migrator_loadtest'@'%' IDENTIFIED BY 'REPLACE_WITH_64_HEX_MIGRATOR_SECRET' REQUIRE SSL;
ALTER USER 'library_migrator_loadtest'@'%' IDENTIFIED BY 'REPLACE_WITH_64_HEX_MIGRATOR_SECRET' REQUIRE SSL;
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'library_migrator_loadtest'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES ON library_management_staging.* TO 'library_migrator_loadtest'@'%';

FLUSH PRIVILEGES;
