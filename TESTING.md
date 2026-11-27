# Testing and verification

## Automated checks

From the repository root with JDK 17 and Maven:

```bash
mvn clean test
mvn package
```

`mvn clean test` compiles all code, compiles JSPs, runs application/security/schema tests, and starts the MySQL Testcontainers class when Docker is available. The MySQL class has its own disposable MySQL 8 container, isolated schemas and fixtures; it does not use a developer or production DB URL. To run only that class:

```bash
mvn -Dtest=MySqlCirculationIntegrationTest test
```

The integration tests verify two distinct Hikari pools borrowing the last copy, inventory/issue/audit consistency, duplicate returns, return/borrow rollback, fine amounts for same-day and overdue returns, fine payment persistence, paid-fine preservation during scheduled recalculation, restricted app/migrator grants, fresh Flyway migrations, existing-schema baselining with rows preserved, and safe failure when a migration collides with an existing index. Audit insert failures are injected to confirm the associated business transaction rolls back.

Without Docker, Testcontainers reports those database tests skipped; this is not a pass for MySQL behavior. In that case, install/start Docker and rerun the integration command. The local developer environment used for this audit has no Docker/MySQL executable, so it cannot execute the container tests or verify live MySQL grants. GitHub Actions runs `mvn clean test` on an Ubuntu runner with Docker and validates production Compose rendering.

## Database/migration verification

- `SchemaMigrationConsistencyTest` normalizes and compares `database/schema.sql` with Flyway V1.
- Fresh MySQL migration coverage checks V1–V4 and `flyway_schema_history`.
- Existing database coverage applies V1, inserts a sentinel row, removes migration history to model an existing untracked install, then checks baseline plus V2–V4 leave that row intact.
- Failure coverage forces V3 to collide with an existing index, verifies Flyway fails, and verifies original user data remains.
- App and migration grants are inspected with `SHOW GRANTS`; test app DAO operations run using the restricted application account.

For a real deployment, back up and compare schema before startup. Flyway baselines a non-empty database at V1 and applies later migrations. It does not drop/reset data. New changes must be new versioned SQL files. Use `flyway_schema_history` to inspect applied versions.

## Security checks

Unit tests verify production rejects root app credentials and unverified/disabled TLS, development remains compatible with local HTTP, security headers include CSP `frame-ancestors`, `nosniff`, and referrer policy, HSTS appears only for production HTTPS, and production session cookies are Secure/HttpOnly/SameSite=Lax. `DeploymentSecurityConfigTest` checks the production Compose configuration for `APP_ENV=production`, HTTPS host, verified MySQL TLS, split credentials, and absence of insecure JDBC options.

Before deploying, render Compose configuration:

```bash
docker compose --env-file deployment/oracle/.env -f deployment/oracle/compose.yaml config --quiet
```

After deployment, verify public HTTP redirects to HTTPS, the browser sees Secure/HttpOnly/SameSite=Lax on `JSESSIONID`, response CSP/HSTS headers are present over HTTPS, and Caddy renews its certificate. Verify MySQL TLS with the server status/session variables and inspect exact account grants using the instructions in `deployment/oracle/README.md`.

## Manual application smoke checks

On local development, create an empty `library_management` database and restricted local accounts using `database/local_users.sql`, configure `DB_*`, deploy the WAR to Tomcat 10.1, and let Flyway migrate it. Optionally load `database/sample_data.sql` after migration. Then verify:

- Register/login/logout, session rotation, 30-minute idle expiry, CSRF rejection, and role-based authorization.
- Catalog search by title, author, ISBN, and category.
- Borrowing available/unavailable books; return once; verify inventory, loan state, and fine amount.
- Fine payment and admin/librarian book/member workflows.
- A stopped database returns a safe user error and logs server-side failure without credentials or tokens.

## Performance plans

Apache JMeter plans live under `performance/`:

- `catalog-search-50-users.jmx`
- `catalog-search-100-users.jmx`
- `catalog-search-500-users-stress.jmx`
- `circulation-borrow-return.jmx`

Use a staging copy with at least 10,000 book rows and fixture accounts. Circulation needs a private CSV with unique test rows for the selected thread count, distinct available book IDs, active loan IDs to return, and student/librarian credentials. Do not commit the CSV or run load tests against production. Commands, dataset sizing, setup, and result collection are documented in README.

JMeter has not been run during this audit, so no latency, p95, throughput, error-rate, or pool-utilization measurements are claimed. Save the `.jtl`, JMeter HTML dashboard, dataset dimensions, thread/ramp/hold settings, and Hikari active/idle/waiting observations for each run.
