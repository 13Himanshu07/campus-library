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

The suite currently contains 15 MySQL integration test cases. It verifies two distinct Hikari pools borrowing the last copy and returning the same loan concurrently, inventory/issue/fine/audit consistency, physical connection reuse, pool singleton behavior and shutdown, borrow/return/payment/registration rollback, including a return audit-insert failure, fine amounts and DECIMAL rounding for different overdue durations, duplicate fine prevention, payment persistence, paid-fine preservation during recalculation, restricted app/migrator grants, fresh Flyway migrations, existing-schema baselining with rows preserved, and safe failure when a migration collides with an existing index. Audit insert failures are injected to confirm the associated business transaction rolls back.

Without Docker, Testcontainers reports those database tests skipped; this is not a pass for MySQL behavior. In that case, install/start Docker and rerun the integration command. The local developer environment used for this audit has no Docker/MySQL executable, so it cannot execute the container tests or verify live MySQL grants. GitHub Actions runs `docker info`, executes the MySQL test class, and validates its Surefire XML with `scripts/verify-mysql-integration.py`; CI fails if the named test suite executes fewer than 14 cases or any case is skipped, failed, or errored. CI then packages the WAR, validates Compose rendering, and starts database, Tomcat, and Caddy in production mode, checking the HTTP redirect, HTTPS readiness response, HSTS, CSP, and Flyway startup.

## Database/migration verification

- `SchemaMigrationConsistencyTest` normalizes and compares `database/schema.sql` with Flyway V1.
- Fresh MySQL migration coverage checks V1–V4 and `flyway_schema_history`.
- Existing database coverage applies V1, inserts a sentinel row, removes migration history to model an existing untracked install, then checks baseline plus V2–V4 leave that row intact.
- Failure coverage forces V3 to collide with an existing index, verifies Flyway fails, and verifies original user data remains.
- App and migration grants are inspected with `SHOW GRANTS`; test app DAO operations run using the restricted application account.

For a real deployment, back up and compare schema before startup. Flyway baselines a non-empty database at V1 and applies later migrations. It does not drop/reset data. New changes must be new versioned SQL files. Use `flyway_schema_history` to inspect applied versions.

## Security checks

Unit tests verify production rejects root app credentials, disabled TLS, and TLS modes other than exact `VERIFY_CA`/`VERIFY_IDENTITY` values, while development remains compatible with local HTTP. They check CSP `frame-ancestors`, `nosniff`, referrer policy, HSTS only for production HTTPS, pool input validation, and production session cookies Secure/HttpOnly/SameSite=Lax. `DeploymentSecurityConfigTest` checks split credentials, the complete env example, private Tomcat/MySQL ports, and the Caddy proxy route.

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

Use an isolated database named `library_management_staging`; do not point the data seed at production or a normal user database. As a staging DBA, prepare its restricted app/migration users from `performance/create-loadtest-users.sql`, start the app with that schema, then seed 50,000 books, 10,000 student accounts, 500 active loans, and dedicated borrow targets with `performance/seed-large-dataset.sql`. The SQL guard refuses another schema name. Export at least as many private unique rows as selected threads with `performance/generate-circulation-csv.py`; search and circulation plans both consume this CSV. The generated CSV is ignored by Git. Plans validate successful page content as well as status and extract CSRF tokens from authenticated pages.

JMeter has not been run during this audit, so performance results are **NOT RUN**; no latency, p95, throughput, error-rate, or pool-utilization measurements are claimed. Save the `.jtl`, JMeter HTML dashboard, dataset dimensions, thread/ramp/hold settings, and Hikari active/idle/waiting observations for each run. To reproduce the profiles, use 20 users for a 60-second smoke test, then 50, 100, and 500 users. Record average, p50/p95/p99 latency, requests/second, error percentage, Hikari active/idle/waiting counts, connection timeouts, database latency, and host CPU/memory. Never run stress profiles against production.
