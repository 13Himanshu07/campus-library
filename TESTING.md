# Testing and verification checklist

Run automated unit checks and build with:

```bash
mvn clean package
```

Start MySQL, import `database/schema.sql` and `database/sample_data.sql`, configure `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`, deploy `target/campus-library.war` to Tomcat 10.1+, and use the checklist below for database-backed checks.

## Automated tests

- [x] Student/librarian subtype behavior through a `User` reference.
- [x] BCrypt hashes and verifies passwords.
- [x] Required-field and length validation overloads.
- [ ] JDBC DAO integration against MySQL (requires configured MySQL test database).

## Manual web and JDBC checklist

- [ ] Register a student with valid details; verify BCrypt hash, role, and membership ID in `users`.
- [ ] Submit a librarian application; verify it is pending, cannot sign in while pending, and an administrator can approve or reject it.
- [ ] Sign in with valid credentials; verify role-aware dashboard and session attributes.
- [ ] Sign in with an invalid password; verify generic rejection and no authenticated session.
- [ ] Submit a librarian POST without a session; verify redirect to login.
- [ ] Sign out; verify session invalidation and protected-page redirect.
- [ ] Change a password while signed in; verify the new BCrypt hash works and the old password no longer authenticates.
- [ ] Allow a session to expire (30 minutes idle); verify protected pages require sign-in again.
- [ ] Add a book; verify category, copy totals, and available totals.
- [ ] Edit a book; verify metadata and copy-count constraints while copies are on loan.
- [ ] Archive a book with no open loans; verify it leaves the catalog.
- [ ] Attempt to archive a book with an open loan; verify rejection and unchanged inventory.
- [ ] Search by title, author, ISBN, and category; try a quote/wildcard input and verify it stays parameterized.
- [ ] Issue an available book as a student; verify issue row, due date, and one-copy decrement commit together.
- [ ] Issue an unavailable book and issue beyond the five-book limit; verify clear rejection and no partial writes.
- [ ] Attempt duplicate active issue of the same title; verify rejection.
- [ ] Return a book as a librarian; verify return date, status, stock increment, and transaction commit.
- [ ] Return a book that is past due; verify fine = late days × configured per-day rate and fine row creation.
- [ ] Mark an unpaid fine paid; verify paid state and paid timestamp.
- [ ] Verify overdue counts and dashboard totals against SQL table contents.
- [ ] Attempt to access librarian pages as a student; verify HTTP 403.
- [ ] Submit an authenticated POST without a valid CSRF token; verify HTTP 403.
- [ ] Stop MySQL and submit a database-backed request; verify a safe error page and a logged server-side exception.

Record actual results and the MySQL/Tomcat versions in this file before a live demonstration.
