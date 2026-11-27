#!/bin/bash
set -euo pipefail
: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD is required}"
: "${MYSQL_DATABASE:?MYSQL_DATABASE is required}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"
: "${DB_MIGRATION_PASSWORD:?DB_MIGRATION_PASSWORD is required}"
# Restrict interpolation to generated hex passwords so SQL literals stay safe.
case "${DB_PASSWORD}${DB_MIGRATION_PASSWORD}" in *[!a-fA-F0-9]*|'') echo 'Database passwords must be non-empty hex strings.' >&2; exit 1;; esac
mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" <<SQL
CREATE USER IF NOT EXISTS 'library_app'@'%' IDENTIFIED BY '${DB_PASSWORD}' REQUIRE SSL;
ALTER USER 'library_app'@'%' IDENTIFIED BY '${DB_PASSWORD}' REQUIRE SSL;
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'library_app'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON ${MYSQL_DATABASE}.* TO 'library_app'@'%';
CREATE USER IF NOT EXISTS 'library_migrator'@'%' IDENTIFIED BY '${DB_MIGRATION_PASSWORD}' REQUIRE SSL;
ALTER USER 'library_migrator'@'%' IDENTIFIED BY '${DB_MIGRATION_PASSWORD}' REQUIRE SSL;
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'library_migrator'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES ON ${MYSQL_DATABASE}.* TO 'library_migrator'@'%';
SQL
mkdir -p /run/mysql-tls
cp /var/lib/mysql/ca.pem /run/mysql-tls/ca.pem
chmod 0644 /run/mysql-tls/ca.pem

