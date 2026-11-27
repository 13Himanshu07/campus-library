-- Local-only account template. Replace the placeholder before running; never use these grants in production.
CREATE USER IF NOT EXISTS 'library_app'@'localhost' IDENTIFIED BY 'REPLACE_WITH_LOCAL_APP_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE ON library_management.* TO 'library_app'@'localhost';
CREATE USER IF NOT EXISTS 'library_migrator'@'localhost' IDENTIFIED BY 'REPLACE_WITH_LOCAL_MIGRATOR_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES ON library_management.* TO 'library_migrator'@'localhost';
FLUSH PRIVILEGES;
