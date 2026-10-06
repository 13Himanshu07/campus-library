USE library_management;

-- Idempotent test fixture for LibraryTaskExecutor, FineCalculationTask and circulation.
-- Uses dedicated names/emails/ISBNs; does not update existing rows.

INSERT INTO categories (name,description)
SELECT 'MT Test - Computing','Tagged test category for multithreading verification'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='MT Test - Computing');
INSERT INTO categories (name,description)
SELECT 'MT Test - Literature','Tagged test category for circulation verification'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='MT Test - Literature');
INSERT INTO categories (name,description)
SELECT 'MT Test - Science','Tagged test category for catalog verification'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='MT Test - Science');

-- This hash matches the repository sample account password: LibraryDemo9!
INSERT INTO users (name,email,password_hash,phone,membership_id,role,status)
SELECT 'Test Student 1','mt-test-student-1@library.local','$2a$10$SkewBWZgz3WteBLOZOu/GOIcifztzq9eN04n5nWkOgRyOZH3zhzZ.','000-TEST-001','MT-TEST-STUDENT-001','STUDENT','ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='mt-test-student-1@library.local');
INSERT INTO users (name,email,password_hash,phone,membership_id,role,status)
SELECT 'Test Student 2','mt-test-student-2@library.local','$2a$10$SkewBWZgz3WteBLOZOu/GOIcifztzq9eN04n5nWkOgRyOZH3zhzZ.','000-TEST-002','MT-TEST-STUDENT-002','STUDENT','ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='mt-test-student-2@library.local');

-- 9 books: all-available, one-copy, multi-copy, and seeded-on-loan inventory states.
INSERT INTO books (title,author,isbn,category_id,description,publisher,publication_year,total_copies,available_copies)
SELECT '[MT TEST] Java Concurrency in Practice','Brian Goetz','9780321349606',c.id,'Seeded overdue-loan case for daily background fine calculation.','Addison-Wesley',2006,3,2
FROM categories c WHERE c.name='MT Test - Computing'
AND NOT EXISTS (SELECT 1 FROM books WHERE isbn='9780321349606');
INSERT INTO books (title,author,isbn,category_id,description,publisher,publication_year,total_copies,available_copies)
SELECT '[MT TEST] Effective Java','Joshua Bloch','9780321356680',c.id,'Seeded active non-overdue loan case.','Addison-Wesley',2008,2,1
FROM categories c WHERE c.name='MT Test - Computing'
AND NOT EXISTS (SELECT 1 FROM books WHERE isbn='9780321356680');
INSERT INTO books (title,author,isbn,category_id,description,publisher,publication_year,total_copies,available_copies)
SELECT '[MT TEST] The Mythical Man-Month','Frederick P. Brooks Jr.','9780201835953',c.id,'Available test catalog copy.','Addison-Wesley',1995,4,4
FROM categories c WHERE c.name='MT Test - Computing'
AND NOT EXISTS (SELECT 1 FROM books WHERE isbn='9780201835953');
INSERT INTO books (title,author,isbn,category_id,description,publisher,publication_year,total_copies,available_copies)
SELECT '[MT TEST] The Pragmatic Programmer','David Thomas','9780135957059',c.id,'Available test catalog copy.','Addison-Wesley',2019,3,3
FROM categories c WHERE c.name='MT Test - Computing'
AND NOT EXISTS (SELECT 1 FROM books WHERE isbn='9780135957059');
INSERT INTO books (title,author,isbn,category_id,description,publisher,publication_year,total_copies,available_copies)
SELECT '[MT TEST] Designing Data-Intensive Applications','Martin Kleppmann','9781449373320',c.id,'Available test catalog copy.','O’Reilly Media',2017,2,2
FROM categories c WHERE c.name='MT Test - Computing'
AND NOT EXISTS (SELECT 1 FROM books WHERE isbn='9781449373320');
INSERT INTO books (title,author,isbn,category_id,description,publisher,publication_year,total_copies,available_copies)
SELECT '[MT TEST] The Hobbit','J. R. R. Tolkien','9780547928227',c.id,'Available test catalog copy.','Mariner Books',1937,5,5
FROM categories c WHERE c.name='MT Test - Literature'
AND NOT EXISTS (SELECT 1 FROM books WHERE isbn='9780547928227');
INSERT INTO books (title,author,isbn,category_id,description,publisher,publication_year,total_copies,available_copies)
SELECT '[MT TEST] The Left Hand of Darkness','Ursula K. Le Guin','9780441478125',c.id,'Available test catalog copy.','Ace Books',1969,2,2
FROM categories c WHERE c.name='MT Test - Literature'
AND NOT EXISTS (SELECT 1 FROM books WHERE isbn='9780441478125');
INSERT INTO books (title,author,isbn,category_id,description,publisher,publication_year,total_copies,available_copies)
SELECT '[MT TEST] A Brief History of Time','Stephen Hawking','9780553380163',c.id,'Available test catalog copy.','Bantam',1988,1,1
FROM categories c WHERE c.name='MT Test - Science'
AND NOT EXISTS (SELECT 1 FROM books WHERE isbn='9780553380163');
INSERT INTO books (title,author,isbn,category_id,description,publisher,publication_year,total_copies,available_copies)
SELECT '[MT TEST] Concurrency Race Copy','Test Fixture','9780000000001',c.id,'One available copy for two-user issue race demonstration.','Test Fixture',2026,1,1
FROM categories c WHERE c.name='MT Test - Computing'
AND NOT EXISTS (SELECT 1 FROM books WHERE isbn='9780000000001');

-- Active overdue loan, dynamically dated so it remains overdue whenever this fixture is first loaded.
-- Inventory is seeded with one copy checked out (3 total, 2 available).
INSERT INTO book_issues (book_id,user_id,issue_date,due_date,return_date,status)
SELECT b.id,u.id,CURRENT_DATE-INTERVAL 24 DAY,CURRENT_DATE-INTERVAL 10 DAY,NULL,'ISSUED'
FROM books b JOIN users u ON u.email='mt-test-student-1@library.local'
WHERE b.isbn='9780321349606'
AND NOT EXISTS (SELECT 1 FROM book_issues i WHERE i.book_id=b.id AND i.user_id=u.id);

-- Active loan which is not overdue. The scheduled overdue finder must leave it without a fine.
-- Inventory is seeded with one copy checked out (2 total, 1 available).
INSERT INTO book_issues (book_id,user_id,issue_date,due_date,return_date,status)
SELECT b.id,u.id,CURRENT_DATE-INTERVAL 2 DAY,CURRENT_DATE+INTERVAL 12 DAY,NULL,'ISSUED'
FROM books b JOIN users u ON u.email='mt-test-student-2@library.local'
WHERE b.isbn='9780321356680'
AND NOT EXISTS (SELECT 1 FROM book_issues i WHERE i.book_id=b.id AND i.user_id=u.id);

-- No fines are inserted here. FineCalculationTask should create the overdue fine.

-- Verification: should show the overdue test loan, its dates and any background-created fine.
SELECT i.id AS issue_id,u.email,b.isbn,b.title,i.issue_date,i.due_date,i.return_date,i.status,
       DATEDIFF(CURRENT_DATE,i.due_date) AS overdue_days, f.amount AS fine_amount,f.paid
FROM book_issues i JOIN users u ON u.id=i.user_id JOIN books b ON b.id=i.book_id
LEFT JOIN fines f ON f.issue_id=i.id
WHERE u.email='mt-test-student-1@library.local' AND b.isbn='9780321349606';

-- Verification: expected_fine assumes LIBRARY_FINE_PER_DAY=5.00 (the application default).
SELECT i.id AS issue_id,i.due_date,f.amount AS actual_fine,
       ROUND(DATEDIFF(CURRENT_DATE,i.due_date)*5.00,2) AS expected_fine,f.paid
FROM book_issues i JOIN users u ON u.id=i.user_id JOIN books b ON b.id=i.book_id
JOIN fines f ON f.issue_id=i.id
WHERE u.email='mt-test-student-1@library.local' AND b.isbn='9780321349606';

-- Verification: test inventory and one-copy race book.
SELECT isbn,title,total_copies,available_copies
FROM books WHERE isbn IN ('9780321349606','9780321356680','9780201835953','9780135957059',
                          '9781449373320','9780547928227','9780441478125','9780553380163','9780000000001')
ORDER BY isbn;

-- CLEANUP (optional): removes only records carrying the unique test emails/ISBNs/category names.
-- Run only after testing. Any test copies issued through the application must first be returned,
-- because the book_issues foreign key prevents removing a book that still has loan rows.
/*
DELETE f FROM fines f JOIN book_issues i ON i.id=f.issue_id
JOIN users u ON u.id=i.user_id
WHERE u.email IN ('mt-test-student-1@library.local','mt-test-student-2@library.local');

DELETE i FROM book_issues i
JOIN users u ON u.id=i.user_id
JOIN books b ON b.id=i.book_id
WHERE u.email IN ('mt-test-student-1@library.local','mt-test-student-2@library.local')
  AND b.isbn IN ('9780321349606','9780321356680','9780201835953','9780135957059',
                 '9781449373320','9780547928227','9780441478125','9780553380163','9780000000001');

DELETE FROM books WHERE isbn IN ('9780321349606','9780321356680','9780201835953','9780135957059',
                                 '9781449373320','9780547928227','9780441478125','9780553380163','9780000000001');
DELETE FROM users WHERE email IN ('mt-test-student-1@library.local','mt-test-student-2@library.local');
DELETE FROM categories WHERE name IN ('MT Test - Computing','MT Test - Literature','MT Test - Science')
  AND NOT EXISTS (SELECT 1 FROM books WHERE books.category_id=categories.id);
*/
