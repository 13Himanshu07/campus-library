USE library_management;
INSERT IGNORE INTO categories (id,name,description) VALUES
  (1,'Computer Science','Programming, systems and computing'),
  (2,'Literature','Fiction, poetry and literary criticism'),
  (3,'Science','Natural and applied sciences');
-- Demo password for these accounts is LibraryDemo9! (BCrypt hash).
INSERT IGNORE INTO users (name,email,password_hash,membership_id,role,status) VALUES
  ('Demo Student','student@library.local','$2a$10$SkewBWZgz3WteBLOZOu/GOIcifztzq9eN04n5nWkOgRyOZH3zhzZ.','DEMO-STUDENT','STUDENT','ACTIVE'),
  ('Demo Librarian','librarian@library.local','$2a$10$SkewBWZgz3WteBLOZOu/GOIcifztzq9eN04n5nWkOgRyOZH3zhzZ.','DEMO-LIBRARIAN','LIBRARIAN','ACTIVE'),
  ('Demo Administrator','admin@library.local','$2a$10$SkewBWZgz3WteBLOZOu/GOIcifztzq9eN04n5nWkOgRyOZH3zhzZ.','DEMO-ADMIN','ADMIN','ACTIVE');
INSERT IGNORE INTO books (title,author,isbn,category_id,description,total_copies,available_copies) VALUES
  ('Effective Java','Joshua Bloch','9780134685991',1,'A practical guide to Java programming.',4,4),
  ('Clean Code','Robert C. Martin','9780132350884',1,'A handbook of agile software craftsmanship.',3,3),
  ('Pride and Prejudice','Jane Austen','9780141439518',2,'A classic novel.',2,2);
