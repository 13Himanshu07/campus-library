# Campus Library Management System

A Java 17 web application for a college library. Students can register, search the catalog, request available books, review due dates and history, and see fines. Librarians manage books, categories, student accounts, returns, and fines.

## Architecture

The application follows MVC with a Servlet controller layer, service interfaces for business rules, JDBC DAOs for persistence, and JSP/JSTL views:

```text
Browser → JSP / HTML / CSS / JavaScript → Jakarta Servlets → Service interfaces → JDBC DAO → MySQL
```

The deployable artifact is `target/campus-library.war`, intended for Apache Tomcat 10.1 or newer. The application does not require a framework runtime.

## Technologies

- Java 17
- Jakarta Servlet 6, JSP 3.1, JSTL 3
- JDBC with MySQL Connector/J and `PreparedStatement`
- MySQL 8
- Maven WAR packaging
- Apache Tomcat 10.1+
- HTML, CSS, JavaScript, BCrypt password hashing

## Features

- Student registration and sign-in; optional librarian applications with administrator approval; BCrypt password hashes; role-based access; HttpSession login and logout.
- Book catalog search by title, author, ISBN, or category; category organization and copy availability.
- Librarian book add/edit/archive, member account management, physical return recording, and fine payment recording.
- Student borrowing with a five-book limit, duplicate-loan prevention, 14-day due dates, and ₹5/day late fines.
- Student current loans, due dates, borrowing history, profile, and fines.
- Database-backed dashboard totals for catalog, available copies, loans, overdue books, students, and unpaid fines.
- Prepared statements throughout; explicit JDBC transactions and row locks for issue/return flows; CSRF tokens on authenticated forms.
- Responsive dashboard UI, shared JSP includes, validation and error views.

## Project layout

```text
database/                         MySQL schema, demo data, reset script
src/main/java/com/library/model   Encapsulated domain objects and role types
src/main/java/com/library/dao     DAO interfaces and JDBC implementations
src/main/java/com/library/service Service interfaces and business rules
src/main/java/com/library/servlet HttpServlet controllers
src/main/java/com/library/filter  Authentication, authorization, and CSRF filters
src/main/java/com/library/util    JDBC connection, password, and validation utilities
src/main/webapp                   Public JSPs, protected JSP/JSTL views, CSS, web.xml
RUBRIC_MAPPING.md                 File-by-file mapping to the Review 1 rubric
TESTING.md                        Automated and manual verification checklist
```

## Database setup

1. Install and start MySQL 8. Create a database user with permission to create/read/write the `library_management` schema, or use a local development account.
2. Import the schema and optional demo data:

   ```bash
   mysql -u root -p < database/schema.sql
   mysql -u root -p < database/sample_data.sql
   ```

3. Configure environment variables before starting Tomcat:

   ```text
   DB_URL=jdbc:mysql://localhost:3306/library_management?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
   DB_USERNAME=library_app
   DB_PASSWORD=your-local-password
   ```

`DB_URL` defaults to a local `library_management` database. Set a private `DB_PASSWORD`; credentials are not stored in Java source or JSPs. The schema has `users`, `categories`, `books`, `book_issues`, and `fines`, with foreign keys, uniqueness rules, copy-count checks, and lookup indexes.

Optional circulation settings are `LIBRARY_BORROW_DAYS` (default `14`), `LIBRARY_BORROW_LIMIT` (default `5`), and `LIBRARY_FINE_PER_DAY` (default `5.00`).

## JDBC and transaction handling

`DBConnection` obtains a `java.sql.Connection` with `DriverManager`. DAO implementations use `PreparedStatement`, `ResultSet`, `SQLException`, and try-with-resources. `IssueDAOImpl` wraps checkout and return in explicit `setAutoCommit(false)`, `commit`, and rollback handling. Checkout locks the selected book row (`FOR UPDATE`) before decrementing inventory; return updates the issue, inventory, and fine record in one transaction.

## Servlet, JSP, and security flow

`@WebServlet` classes receive browser requests, call a service interface, set request attributes, and forward to JSPs under `WEB-INF/views`. JSPs use JSTL and Expression Language for rendering; they contain no Java scriptlets. `web.xml` declares the welcome page, session timeout, and error pages.

After login, the servlet rotates the session ID and stores the user, role, and dashboard destination in `HttpSession`. `AuthenticationFilter` protects authenticated routes; `AuthorizationFilter` restricts librarian/admin paths; `CsrfFilter` checks session-bound tokens on authenticated POST requests. Logout invalidates the session. Passwords are BCrypt hashes.

## Build and run

Requirements: JDK 17, Maven 3.9+, MySQL 8, and Tomcat 10.1+.

```bash
mvn clean package
```

Copy `target/campus-library.war` to Tomcat's `webapps/` directory and start Tomcat. The app is deployed as `/campus-library`; copy it as `ROOT.war` to use `/`. Configure the three database environment variables in Tomcat's service environment before starting it. Open `http://localhost:8080/campus-library/`.

For local development, set those environment variables and use Tomcat's deployment directory or your IDE's Tomcat 10.1 integration. The WAR is not a standalone executable.

### Demo accounts

After importing `database/sample_data.sql`:

| Role | Email | Password |
|---|---|---|
| Student | `student@library.local` | `LibraryDemo9!` |
| Librarian | `librarian@library.local` | `LibraryDemo9!` |
| Administrator | `admin@library.local` | `LibraryDemo9!` |

Demo credentials are for local evaluation only. Change or remove them before exposing a deployment publicly. The sample SQL inserts only demonstration accounts and catalog rows.

## Deploy to Tomcat

For the included Oracle Cloud Compose example, see [`deployment/oracle/README.md`](deployment/oracle/README.md). The container runs Tomcat and connects to a separate MySQL service; Caddy can provide HTTPS. For a managed Tomcat host, upload the WAR and configure the database environment variables in its service settings.

## Tests and rubric

Run `mvn test` for the included unit tests. Use [`TESTING.md`](TESTING.md) for the database-backed manual workflow checklist. [`RUBRIC_MAPPING.md`](RUBRIC_MAPPING.md) identifies the files that visibly demonstrate each rubric requirement.
