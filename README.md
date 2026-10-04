# Campus Library Management System

## Project Overview

Campus Library is a Java 17 web application for managing a college library catalog, member accounts, book circulation, and overdue fines. It is built as a traditional Java Web application for evaluation of Java, JDBC, Servlet, and JSP concepts.

## Problem Statement

Manual library records make it difficult to find books, track copies and due dates, manage member access, and calculate overdue charges consistently. The system centralizes these workflows and records them in a relational database.

## Proposed Solution

The application uses MVC with JSP views, Jakarta Servlet controllers, service interfaces for business rules, DAO interfaces with direct JDBC implementations, and MySQL persistence. It packages as a WAR for Apache Tomcat; it is not a standalone executable.

```text
Browser → JSP / HTML / CSS / JavaScript → Jakarta Servlets → Service layer → DAO layer → JDBC → MySQL
```

## Features

- Student registration, login, profile updates, password changes, borrowing history, and fine viewing.
- Optional librarian applications with administrator approval.
- Search the catalog by title, author, ISBN, or category.
- Librarian book and category management, member account management, returns, and fine payment recording.
- Five-book borrowing limit, duplicate-loan prevention, 14-day due dates, and configurable late fines (default ₹5/day).
- Dashboard totals for catalog, available copies, active loans, overdue books, students, pending librarian applications, and unpaid fines.
- BCrypt password hashes, role-based access, session rotation, CSRF protection on authenticated POST forms, and responsive pages.

## User Roles

| Role | Main capabilities |
|---|---|
| Student | Browse/search, borrow eligible books, view active loans/history/fines, manage profile and password. |
| Librarian | Manage books/categories/members, record returns, review circulation and fines. |
| Administrator | Access librarian workflows and approve or reject librarian applications. |

## Technology Stack

- Java 17; Jakarta Servlet 6; JSP 3.1; JSTL 3 and Expression Language.
- JDBC with MySQL Connector/J; MySQL 8 schema.
- Maven WAR packaging; Apache Tomcat 10.1+.
- HTML, CSS, and JavaScript for presentation and browser interactions.
- BCrypt for password hashing.

## System Architecture

- **Model:** Encapsulated domain types for users, books, categories, issues, and fines.
- **Servlet/controller:** Handles HTTP requests, calls services, and forwards request data to JSP views.
- **Service:** Validates input and applies account, catalog, circulation, and fine rules.
- **DAO:** Defines persistence contracts; implementations use SQL and JDBC directly.
- **Database:** Stores related records with primary keys, foreign keys, unique constraints, checks, and indexes.

## Project Structure

```text
database/                         schema, sample data, reset script
src/main/java/com/library/model   domain models
src/main/java/com/library/dao     DAO interfaces and JDBC implementations
src/main/java/com/library/service service interfaces and implementations
src/main/java/com/library/servlet HttpServlet controllers
src/main/java/com/library/filter  authentication, authorization, CSRF filters
src/main/java/com/library/exception custom application exceptions
src/main/java/com/library/util    JDBC connection, password, validation utilities
src/main/webapp                   public JSPs, protected views, CSS, JavaScript, web.xml
src/test/java                     Java behavior and JSP compilation checks
pom.xml                            Maven WAR build
RUBRIC_MAPPING.md                  source-file mapping to the college rubric
TESTING.md                         automated and manual verification checklist
```

## Database Design

`database/schema.sql` creates five related tables:

| Table | Purpose |
|---|---|
| `users` | Student, librarian, and administrator accounts and status. |
| `categories` | Book categories. |
| `books` | Catalog details, category relationship, copy counts, and archive state. |
| `book_issues` | Borrower, book, issue/due/return dates, and circulation status. |
| `fines` | Fine amount and payment state for a returned issue. |

The schema declares primary and foreign keys, unique email/membership/ISBN/category values, copy-count validation, and indexes for catalog and loan lookups. Review the SQL before applying it to an existing database. `database/reset_database.sql` is destructive: it drops and recreates `library_management`.

## JDBC Implementation

`src/main/java/com/library/util/DBConnection.java` opens connections through `DriverManager`. DAO implementations use `Connection`, `PreparedStatement`, `ResultSet`, `SQLException`, and try-with-resources. In `IssueDAOImpl`, issue and return operations disable auto-commit, lock relevant rows, update loan/inventory/fine records, commit on success, and roll back on failure.

## Servlet Implementation

The controllers extend `HttpServlet` through `BaseServlet` and use `@WebServlet` mappings. They call service interfaces and forward data to JSPs; business policy is kept in services and persistence in DAOs. `WEB-INF/web.xml` configures the welcome page, session timeout, and error pages.

## JSP Frontend

`index.jsp`, `login.jsp`, and `register.jsp` are public entry pages. Authenticated pages are under `WEB-INF/views/` for student, librarian, administrator, shared, and error views. JSPs use JSTL and EL rather than Java scriptlets. Shared CSS is in `src/main/webapp/assets/css/`; confirmation behavior is in `src/main/webapp/assets/js/app.js`.

## Authentication and Authorization

Successful login stores the user ID, display name, role, and dashboard view in `HttpSession`, then rotates the session ID. `AuthenticationFilter` requires a session for protected routes; `AuthorizationFilter` restricts librarian and administrator routes. `CsrfFilter` validates session-bound tokens on authenticated POST requests. Logout invalidates the session. Passwords are stored as BCrypt hashes.

## Requirements

- JDK 17
- Maven 3.9+
- MySQL 8
- Apache Tomcat 10.1+

## Database Setup

Create a MySQL account with privileges for the application schema, then import the schema and (optionally) sample data:

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/sample_data.sql
```

## Configuration

Set environment variables in the process that starts Tomcat. Do not commit real credentials.

```text
DB_URL=jdbc:mysql://localhost:3306/library_management?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=library_app
DB_PASSWORD=your-local-password
LIBRARY_BORROW_DAYS=14
LIBRARY_BORROW_LIMIT=5
LIBRARY_FINE_PER_DAY=5.00
```

The first three variables configure JDBC. Circulation variables are optional and default to the values shown. For a hosted MySQL service, use its supplied host, database name, TLS options, username, and password.

## Running the Project

Package the WAR:

```bash
mvn clean package
```

Copy `target/campus-library.war` to Tomcat's `webapps/` directory and start Tomcat. The context path is `/campus-library`; rename it to `ROOT.war` to deploy at `/`. With the default local port, open `http://localhost:8080/campus-library/`.

## Maven Build

`mvn clean package` compiles Java sources, runs automated tests (including JSP compilation), and creates `target/campus-library.war`. Generated `target/`, `.class`, and `.war` files are ignored by Git.

## Tomcat Deployment

Use Tomcat 10.1 or newer, configure database/circulation environment variables, and deploy the WAR. The repository includes an Oracle Cloud Compose example in `deployment/oracle/`; it is optional. Deployment-specific instructions are in that directory's README.

## Demo Credentials

After loading `database/sample_data.sql`, use these local-only accounts:

| Role | Email | Password |
|---|---|---|
| Student | `student@library.local` | `LibraryDemo9!` |
| Librarian | `librarian@library.local` | `LibraryDemo9!` |
| Administrator | `admin@library.local` | `LibraryDemo9!` |

Change or remove these demonstration accounts before making a deployment public.

## Core Java Concepts

- **Encapsulation:** private state and controlled accessors in model classes.
- **Inheritance and polymorphism:** `Student` and `Librarian` extend abstract `User` and override role-specific behavior.
- **Abstraction and interfaces:** DAO and service interfaces separate contracts from implementations.
- **Collections and generics:** typed lists and maps carry domain data and dashboard statistics.
- **Enums:** account roles/statuses and issue statuses model constrained states.
- **Exceptions:** custom authentication, validation, availability, lookup, and database exceptions handle application failures.
- **Constructors and overloading:** model/service construction and validation overloads are used in live application paths.

See `RUBRIC_MAPPING.md` for exact source evidence against each mark category.

## Screenshots

No screenshots are currently included. Capture the student, librarian, and administrator views from a running Tomcat/MySQL deployment before adding them here.

## Future Enhancements

- Automated JDBC integration tests against a disposable MySQL database.
- Notification delivery and richer report filters.
- Add verified screenshots and live deployment instructions after testing the target hosting environment.

## Testing

Run `mvn clean package` for automated checks. Follow `TESTING.md` for database-backed and browser workflows; those manual checks require a configured MySQL database and running Tomcat instance.
