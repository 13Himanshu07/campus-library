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
| Connection / DriverManager | `src/main/java/com/library/util/DBConnection.java`. |
| PreparedStatement / ResultSet / SQLException | `dao/impl/UserDAOImpl.java`, `BookDAOImpl.java`, `CategoryDAOImpl.java`, `IssueDAOImpl.java`, `FineDAOImpl.java`, and `DashboardDAOImpl.java`. |
| DAO pattern | DAO interfaces under `dao/` and direct JDBC implementations under `dao/impl/`. |
| Search / CRUD | `BookDAOImpl` provides parameterized create, update, archive, find, list, and search operations; `UserDAOImpl` and `CategoryDAOImpl` handle their records. |
| JDBC transactions | `IssueDAOImpl.issue()` and `returnBook()` disable auto-commit, lock/update rows, commit on success, and roll back on failure. |
| Relational design | `database/schema.sql`: five tables, foreign keys, unique constraints, a copy-count check, and indexes. |
| Live statistics | `DashboardDAOImpl.statistics()` calculates dashboard totals with SQL aggregates. |
| Repeatable database setup | `database/schema.sql`, `database/sample_data.sql`, and `database/reset_database.sql`. |

Coverage: JDBC is explicit; no ORM is in the runtime dependency graph. A live database is required for end-to-end DAO checks.

## 4. Servlets & Web Integration — 7 marks

| Requirement | Exact evidence |
|---|---|
| Actual `HttpServlet` controllers | `servlet/LoginServlet.java`, `LogoutServlet.java`, `RegisterServlet.java`, `DashboardServlet.java`, `BookListServlet.java`, `SearchBookServlet.java`, `AddBookServlet.java`, `EditBookServlet.java`, `DeleteBookServlet.java`, `IssueBookServlet.java`, `ReturnBookServlet.java`, `MyBooksServlet.java`, `HistoryServlet.java`, `UserListServlet.java`, `FineServlet.java`, `CategoryServlet.java`, `ProfileServlet.java`, `PasswordServlet.java`, `LibrarianIssueListServlet.java`, and `AdminApplicationsServlet.java`. |
| Servlet mappings | `@WebServlet` declarations in those servlet classes; `WEB-INF/web.xml` configures the application welcome route, session timeout, and error pages. |
| JSP/JSTL/EL | Public `index.jsp`, `login.jsp`, `register.jsp` and protected `WEB-INF/views/{student,librarian,admin,shared,error}` pages. Dynamic lists use JSTL; values use EL; no scriptlets. |
| Authentication | `filter/AuthenticationFilter.java` protects authenticated routes; `LoginServlet` establishes session identity; `LogoutServlet` invalidates it. |
| Authorization | `filter/AuthorizationFilter.java` enforces librarian/admin URL roles; state-changing servlet actions also validate role where member routes are shared. |
| Session security | `HttpSession` stores user/role, login rotates the session ID, `web.xml` sets inactivity timeout, `CsrfFilter` checks authenticated form submissions. |
| Request/view separation | Servlets prepare request attributes; JSPs render them; service layer owns workflow policy. |

Coverage: traditional JSP/Servlet integration is direct and verifiable in source.
