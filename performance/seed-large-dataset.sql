-- Run only against the dedicated, non-production library_management_staging schema.
-- Re-running this script is safe for the LOADTEST_* users and LT-prefixed books.
USE library_management_staging;

-- MySQL 8 enforces CHECK constraints; abort before any data change on another schema.
CREATE TEMPORARY TABLE load_test_environment_guard (
    schema_name VARCHAR(64) NOT NULL CHECK (schema_name='library_management_staging')
);
INSERT INTO load_test_environment_guard(schema_name) VALUES (DATABASE());

INSERT IGNORE INTO categories(name, description)
VALUES ('Load Test', 'Reserved for isolated JMeter performance runs');
SET @load_test_category_id = (SELECT id FROM categories WHERE name='Load Test');
SET @load_test_password_hash = '$2a$10$SkewBWZgz3WteBLOZOu/GOIcifztzq9eN04n5nWkOgRyOZH3zhzZ.';

CREATE TEMPORARY TABLE load_test_digits (digit TINYINT NOT NULL PRIMARY KEY);
INSERT INTO load_test_digits(digit) VALUES (0),(1),(2),(3),(4),(5),(6),(7),(8),(9);
CREATE TEMPORARY TABLE load_test_numbers (n INT NOT NULL PRIMARY KEY);
INSERT INTO load_test_numbers(n)
SELECT 1 + d0.digit + 10*d1.digit + 100*d2.digit + 1000*d3.digit + 10000*d4.digit
FROM load_test_digits d0
CROSS JOIN load_test_digits d1
CROSS JOIN load_test_digits d2
CROSS JOIN load_test_digits d3
CROSS JOIN load_test_digits d4
WHERE 1 + d0.digit + 10*d1.digit + 100*d2.digit + 1000*d3.digit + 10000*d4.digit <= 50000;

INSERT IGNORE INTO books(title, author, isbn, category_id, description, publisher, publication_year, total_copies, available_copies)
SELECT CONCAT('Load Test Book ', LPAD(n, 5, '0')), CONCAT('Load Test Author ', MOD(n, 1000)),
       CONCAT('LT', LPAD(n, 11, '0')), @load_test_category_id,
       'Synthetic catalogue record for isolated performance testing', 'Campus Library Test Data', 2024, 2, 2
FROM load_test_numbers;

INSERT IGNORE INTO users(name, email, password_hash, membership_id, role, status)
SELECT CONCAT('Load Test Student ', LPAD(n, 5, '0')),
       CONCAT('load-student-', LPAD(n, 5, '0'), '@library.local'), @load_test_password_hash,
       CONCAT('LOAD-STUDENT-', LPAD(n, 5, '0')), 'STUDENT', 'ACTIVE'
FROM load_test_numbers WHERE n <= 10000;

INSERT IGNORE INTO users(name, email, password_hash, membership_id, role, status)
VALUES ('Load Test Librarian', 'load-librarian@library.local', @load_test_password_hash,
        'LOAD-LIBRARIAN', 'LIBRARIAN', 'ACTIVE');

-- Reserve the first 500 books as existing overdue loans for the circulation profile.
-- The next 500 are reserved as borrow targets. No non-LT records are modified.
INSERT INTO book_issues(book_id, user_id, issue_date, due_date, status)
SELECT loan_book.id, student.id, CURRENT_DATE - INTERVAL 28 DAY,
       CURRENT_DATE - INTERVAL 14 DAY, 'OVERDUE'
FROM load_test_numbers n
JOIN users student ON student.email=CONCAT('load-student-', LPAD(n.n, 5, '0'), '@library.local')
JOIN books loan_book ON loan_book.isbn=CONCAT('LT', LPAD(n.n, 11, '0'))
WHERE n.n <= 500
  AND NOT EXISTS (
      SELECT 1 FROM book_issues existing
      WHERE existing.book_id=loan_book.id AND existing.user_id=student.id AND existing.return_date IS NULL
  );

UPDATE books reserved
SET reserved.available_copies = reserved.total_copies - (
    SELECT COUNT(*) FROM book_issues active_issue
    WHERE active_issue.book_id=reserved.id AND active_issue.return_date IS NULL
)
WHERE reserved.isbn BETWEEN 'LT00000000001' AND 'LT00000000500';

UPDATE books reserved
SET reserved.available_copies = reserved.total_copies - (
    SELECT COUNT(*) FROM book_issues active_issue
    WHERE active_issue.book_id=reserved.id AND active_issue.return_date IS NULL
)
WHERE reserved.isbn BETWEEN 'LT00000001001' AND 'LT00000001500';

DROP TEMPORARY TABLE load_test_numbers;
DROP TEMPORARY TABLE load_test_digits;
DROP TEMPORARY TABLE load_test_environment_guard;
