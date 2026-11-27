# Review 1 rubric mapping

This mapping credits only behavior visibly implemented in this source tree. Framework behavior is not counted in place of direct servlet, JSP, or JDBC code.

## 1. Problem Understanding & Solution Design — 8 marks

| Evidence | Demonstration |
|---|---|
| `README.md` | Problem, user roles, architecture, operational steps, and technology choices. |
| `database/schema.sql` | Normalized users, categories, books, book issues, and fines with keys, checks, and indexes. |
| `src/main/java/com/library/model/` | Book, category, account, loan, and fine concepts represented as domain types. |
| `src/main/java/com/library/service/impl/IssueServiceImpl.java` | Encodes due period, borrowing limit, and late-fine rate. |
| `src/main/java/com/library/dao/impl/IssueDAOImpl.java` | Checkout/return atomicity and stock consistency. |
| `src/main/java/com/library/servlet/` and `src/main/webapp/WEB-INF/views/` | Student and librarian workflows and role-appropriate screens. |

Coverage: core catalog, account, borrowing, return, fine, and librarian-application approval workflows are implemented. Notification delivery and richer report filters are optional future enhancements and are not claimed as implemented.

## 2. Core Java Concepts — 10 marks

| Concept | Exact evidence |
|---|---|
| Encapsulation | Private fields and accessors in `model/User.java`, `Book.java`, `BookIssue.java`, `Fine.java`, and `Category.java`. |
| Inheritance | `Student extends User`; `Librarian extends User`. |
| Runtime polymorphism / overriding | `User.getDashboardType()` and `getDisplayLabel()` are abstract/overridden; `UserDAOImpl.map()` chooses a concrete type based on the stored role; `LoginServlet` calls `getDashboardType()` through the `User` reference and `DashboardServlet` uses that session value to choose the view. |
| Abstraction | `UserDAO`, `BookDAO`, `IssueDAO`, `FineDAO`, and matching service interfaces. |
| Interfaces | Service and DAO boundaries in `service/*.java` and `dao/*.java`, implemented under `impl/`. |
| Exceptions | `AuthenticationException`, `BookNotAvailableException`, `IssueNotFoundException`, `UserNotFoundException`, `DatabaseException`, and `ValidationException` are used by service/DAO/servlet flows. |
| Collections / generics | Typed `List<Book>`, `List<User>`, `List<BookIssue>`, and `Map<String,Long>` in DAOs/services/servlets. |
| Constructors | Domain and service constructors, including initialized no-argument model constructors and parameterized model constructors. |
| Method overloading | `ValidationUtil.required(value,label)` and `required(value,label,maxLength)`. |
| Enums | `User.Role`, `User.Status`, and `BookIssue.IssueStatus`. |
| Type safety | No raw collections in the DAO/service interfaces. |

Coverage: object-oriented concepts are used in live application paths rather than isolated examples.

## 3. Database Integration (JDBC) — 8 marks

| Requirement | Exact evidence |
|---|---|
| Centralized JDBC pool | `src/main/java/com/library/util/DBConnection.java` configures a single HikariCP pool; DAO connections are obtained there and returned with try-with-resources. |
| PreparedStatement / ResultSet / SQLException | `dao/impl/UserDAOImpl.java`, `BookDAOImpl.java`, `CategoryDAOImpl.java`, `IssueDAOImpl.java`, `FineDAOImpl.java`, and `DashboardDAOImpl.java`. |
| DAO pattern | DAO interfaces under `dao/` and direct JDBC implementations under `dao/impl/`. |
| Search / CRUD | `BookDAOImpl` provides parameterized create, update, archive, find, list, and search operations; `UserDAOImpl` and `CategoryDAOImpl` handle their records. |
| JDBC transactions | `IssueDAOImpl.issue()` and `returnBook()` disable auto-commit, lock/update rows, and commit business plus audit rows together; failures roll back both. `BookDAOImpl`, `FineDAOImpl`, and `UserDAOImpl` also commit their audit event with each successful mutation. |
| Relational design | Flyway migrations under `src/main/resources/db/migration` create current business tables and `audit_logs`, with foreign keys and indexes. `database/schema.sql` mirrors V1 for comparison. |
| Live statistics | `DashboardDAOImpl.statistics()` calculates dashboard totals with SQL aggregates. |
| Repeatable database setup | Flyway V1–V4, `flyway_schema_history`, and disposable MySQL Testcontainers fixtures. |

Coverage: JDBC is explicit; no ORM is in the runtime dependency graph. A live database is required for end-to-end DAO checks.

## 4. Servlets & Web Integration — 7 marks

| Requirement | Exact evidence |
|---|---|
| Actual `HttpServlet` controllers | `servlet/LoginServlet.java`, `LogoutServlet.java`, `RegisterServlet.java`, `DashboardServlet.java`, `BookListServlet.java`, `SearchBookServlet.java`, `AddBookServlet.java`, `EditBookServlet.java`, `DeleteBookServlet.java`, `IssueBookServlet.java`, `ReturnBookServlet.java`, `MyBooksServlet.java`, `HistoryServlet.java`, `UserListServlet.java`, `FineServlet.java`, `CategoryServlet.java`, `ProfileServlet.java`, `PasswordServlet.java`, `LibrarianIssueListServlet.java`, and `AdminApplicationsServlet.java`. |
| Servlet mappings | `@WebServlet` declarations in those servlet classes; `WEB-INF/web.xml` configures the application welcome route, session timeout, and error pages. |
| JSP/JSTL/EL | Public `index.jsp`, `login.jsp`, `register.jsp` and protected `WEB-INF/views/{student,librarian,admin,shared,error}` pages. Dynamic lists use JSTL; values use EL; no scriptlets. |
| Authentication | `filter/AuthenticationFilter.java` protects authenticated routes; `LoginServlet` establishes session identity; `LogoutServlet` invalidates it. |
| Authorization | `filter/AuthorizationFilter.java` enforces librarian/admin URL roles; state-changing servlet actions also validate role where member routes are shared. |
| Session and response security | `HttpSession` stores user/role, login rotates the session ID, `LibraryContextListener` selects Secure/HttpOnly/SameSite cookies per environment, `CsrfFilter` checks authenticated POST forms, and `OperationalFilter` sets CSP, nosniff, referrer, and production HSTS headers. |
| Request/view separation | Servlets prepare request attributes; JSPs render them; service layer owns workflow policy. |

Coverage: traditional JSP/Servlet integration is direct and verifiable in source.


## 5. Operational and production safeguards

| Requirement | Exact evidence |
|---|---|
| Versioned schema change | `src/main/resources/db/migration/V1__initial_schema.sql` through `V4__rename_audit_log_to_audit_logs.sql`; `LibraryContextListener` migrates before registering services. |
| Existing schema/data adoption | Flyway baseline config uses version 1; Testcontainers coverage verifies preserved rows and safe failure. |
| Audit consistency | `AuditLog.insert(Connection, ...)` participates in each mutating DAO transaction; MySQL tests inject audit table failures and assert business rollback. |
| Database privilege split | `database/docker-bootstrap.sh`, `database/local_users.sql`, and `database/production_users.sql` grant DML to the app user and schema changes to the migration user. Integration tests inspect grants and issue a book as the restricted app. |
| Production HTTPS / MySQL TLS | `deployment/oracle/compose.yaml` sets APP_ENV=production and Caddy; MySQL requires secure transport and the Tomcat entrypoint imports its CA for Connector/J VERIFY_CA. |
| HTTP response controls | `filter/OperationalFilter.java` emits CSP with frame-ancestors, X-Content-Type-Options, Referrer-Policy, and production HTTPS HSTS. |
| Monitoring | Logback request timing and Hikari pool counts in `OperationalFilter`, `LibraryContextListener`, and `LibraryTaskExecutor`. |
| Repeatable database/performance tests | `MySqlCirculationIntegrationTest`, `performance/*.jmx`, and `.github/workflows/build-war.yml`. |

