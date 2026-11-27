-- Run as a DBA after creating library_management. Replace both password placeholders
-- through your secret manager before execution. Production accounts require TLS.
CREATE USER IF NOT EXISTS 'library_app'@'%' IDENTIFIED BY 'REPLACE_WITH_APP_SECRET' REQUIRE SSL;
ALTER USER 'library_app'@'%' IDENTIFIED BY 'REPLACE_WITH_APP_SECRET' REQUIRE SSL;
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'library_app'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON library_management.* TO 'library_app'@'%';

CREATE USER IF NOT EXISTS 'library_migrator'@'%' IDENTIFIED BY 'REPLACE_WITH_MIGRATOR_SECRET' REQUIRE SSL;
ALTER USER 'library_migrator'@'%' IDENTIFIED BY 'REPLACE_WITH_MIGRATOR_SECRET' REQUIRE SSL;
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'library_migrator'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES ON library_management.* TO 'library_migrator'@'%';

FLUSH PRIVILEGES;
