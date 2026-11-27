# Campus Library on Oracle Cloud (production Compose)

The Compose stack runs Tomcat 10.1, MySQL 8, and Caddy. Only Caddy publishes ports 80/443; Tomcat and MySQL stay on the private Compose network. APP_ENV is explicitly production. Caddy provisions public HTTPS; the servlet filter redirects HTTP to `PUBLIC_HTTPS_HOST`, sets HSTS for HTTPS requests, and issues Secure/HttpOnly/SameSite=Lax session cookies.

## Prepare and provision

- Use an Ubuntu VM with Docker Engine and the Compose plugin.
- Allow TCP 22 from administrator IPs and TCP 80/443. Keep 3306 and 8080 private.
- Point the public DNS A record at the VM.
- Build the WAR with JDK 17 and Maven.

From the repository root:

```bash
mvn clean package
cp deployment/oracle/.env.example deployment/oracle/.env
openssl rand -hex 32
```

Replace `PUBLIC_DOMAIN` and generate three independent hexadecimal secrets for `MYSQL_ROOT_PASSWORD`, `DB_PASSWORD`, and `DB_MIGRATION_PASSWORD`. Generate each value with its own `openssl rand -hex 32` invocation. The Compose environment requires all three passwords; the bootstrap script accepts hexadecimal passwords so SQL interpolation is constrained and predictable. Keep `.env` private and never check it in.

Start the production stack:

```bash
docker compose --env-file deployment/oracle/.env -f deployment/oracle/compose.yaml up -d --build
```

On a fresh MySQL volume, the image enables `require_secure_transport`, creates the restricted `library_app` and `library_migrator` accounts, and exports the generated MySQL CA into the private `mysql_tls` volume. The Tomcat entrypoint imports that CA into a temporary PKCS12 truststore before starting. The app URL uses Connector/J `sslMode=VERIFY_CA`; production startup rejects root credentials and unverified/disabled MySQL TLS. Flyway creates a fresh schema and starts the application only after migration succeeds. No demo records are loaded automatically.

The MySQL bootstrap creates only app DML grants and migrator schema grants, including DROP because Flyway V4 renames the audit table. Review `database/docker-bootstrap.sh`, `database/production_users.sql`, and check actual grants with `SHOW GRANTS FOR 'library_app'@'%';` and `SHOW GRANTS FOR 'library_migrator'@'%';`.

## Existing MySQL volumes

Initialization scripts run only for a new MySQL data directory. Before deploying this Compose change onto an existing volume, back up `mysql_data`, apply the reviewed grants from `database/production_users.sql` with the existing root account and configured production secrets, export the current `/var/lib/mysql/ca.pem` into the `mysql_tls` volume, then recreate the API container. The migration baseline preserves existing rows; compare schema to V1 first. Do not reset or remove the volume.

## Operations

The app log and Hikari pool metrics are available through:

```bash
docker compose --env-file deployment/oracle/.env -f deployment/oracle/compose.yaml logs -f api proxy database
```

Caddy obtains and renews the public certificate. Back up `mysql_data` and the private `.env`/secret material according to your operations policy. For managed MySQL, use its CA and `sslMode=VERIFY_IDENTITY` (including JDBC truststore settings); do not disable certificate verification.

`/health/live` reports process liveness. `/health/ready` runs `SELECT 1` through the application pool and returns only `ready` or `not_ready`; it never includes connection details. Caddy overwrites `X-Forwarded-Proto` with the incoming scheme before proxying. `TRUST_PROXY_TLS=true` is enabled only in this private proxy topology. HTTP redirects to HTTPS and HSTS is returned only on secure production requests.

The configuration requires `PUBLIC_DOMAIN`, `MYSQL_ROOT_PASSWORD`, `DB_PASSWORD`, and `DB_MIGRATION_PASSWORD`. Optional circulation settings are `LIBRARY_BORROW_DAYS`, `LIBRARY_BORROW_LIMIT`, and `LIBRARY_FINE_PER_DAY`. The pool can be tuned in `.env` with `DB_POOL_MAX_SIZE`, `DB_POOL_MIN_IDLE`, `DB_POOL_CONNECTION_TIMEOUT_MS`, `DB_POOL_IDLE_TIMEOUT_MS`, `DB_POOL_MAX_LIFETIME_MS`, `DB_POOL_INIT_FAIL_TIMEOUT_MS`, and `DB_POOL_NAME`. Application JDBC and Flyway use the same CA-verified TLS URL but distinct accounts.

## Local development

Use `APP_ENV=development`, local-only account grants in `database/local_users.sql`, and `sslMode=DISABLED` only on a trusted local network. Production cookie/TLS requirements do not change local HTTP behavior.
