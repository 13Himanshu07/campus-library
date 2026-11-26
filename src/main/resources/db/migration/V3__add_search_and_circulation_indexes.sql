CREATE INDEX idx_books_available_title ON books (available_copies, title);
CREATE INDEX idx_issues_book_status ON book_issues (book_id, status);
