-- Run as a DBA after creating library_management. Replace both password placeholders
-- through your secret manager before execution. Production accounts require TLS.
CREATE USER IF NOT EXISTS 'library_app'@'%' IDENTIFIED BY 'REPLACE_WITH_APP_SECRET' REQUIRE SSL;
GRANT SELECT, INSERT, UPDATE, DELETE ON library_management.* TO 'library_app'@'%';

CREATE USER IF NOT EXISTS 'library_migrator'@'%' IDENTIFIED BY 'REPLACE_WITH_MIGRATOR_SECRET' REQUIRE SSL;
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES ON library_management.* TO 'library_migrator'@'%';

FLUSH PRIVILEGES;

