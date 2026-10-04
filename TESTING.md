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

## Verified environment

- Java: 26.0.2
- Maven: 3.9.16
- MySQL Community Server: 8.0.46
- Apache Tomcat: 10.1.60
- Packaging: WAR
- Application URL: http://localhost:8080/campus-library/

## Verified automated results

- `mvn clean package`: BUILD SUCCESS
- `mvn test`: 4 tests passed, 0 failures, 0 errors, 0 skipped
- JSP compilation check: 0 errors
- MySQL Connector/J included in the generated WAR

## Verified manual workflows

- Student login and role-based dashboard: PASS
- Student book browsing and search: PASS
- Student book issue/request: PASS
- My Books and borrowing history: PASS
- Student fines page: PASS
- Librarian dashboard: PASS
- Librarian add/edit/remove book: PASS
- Librarian category management: PASS
- Librarian member management: PASS
- Librarian issue and return workflow: PASS
- Admin login and dashboard: PASS
- Admin applications page: PASS
- Authentication and authorization flows: PASS
- UTF-8 character and icon rendering: PASS
- Database-backed transactions: PASS

## Final verification

The application was deployed to Apache Tomcat 10.1.60 and tested through the browser after the final UI, UTF-8, and JDBC connection fixes. The tested Java Web version is committed to the `main` branch.
