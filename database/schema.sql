CREATE DATABASE IF NOT EXISTS library_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_management;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  phone VARCHAR(40),
  membership_id VARCHAR(40) NOT NULL UNIQUE,
  role ENUM('STUDENT','LIBRARIAN','ADMIN') NOT NULL DEFAULT 'STUDENT',
  status ENUM('ACTIVE','PENDING','DISABLED') NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(500)
);
CREATE TABLE IF NOT EXISTS books (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  author VARCHAR(180) NOT NULL,
  isbn VARCHAR(32) NOT NULL UNIQUE,
  category_id BIGINT,
  description VARCHAR(3000),
  publisher VARCHAR(180),
  publication_year INT,
  total_copies INT NOT NULL DEFAULT 0,
  available_copies INT NOT NULL DEFAULT 0,
  archived BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
  CONSTRAINT chk_book_copies CHECK (total_copies >= 0 AND available_copies >= 0 AND available_copies <= total_copies),
  INDEX idx_books_title (title), INDEX idx_books_author (author)
);
CREATE TABLE IF NOT EXISTS book_issues (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  book_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  issue_date DATE NOT NULL,
  due_date DATE NOT NULL,
  return_date DATE,
  status ENUM('ISSUED','RETURNED','OVERDUE') NOT NULL DEFAULT 'ISSUED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_issues_book FOREIGN KEY (book_id) REFERENCES books(id),
  CONSTRAINT fk_issues_user FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_issues_user_status (user_id,status), INDEX idx_issues_due_status (due_date,status)
);
CREATE TABLE IF NOT EXISTS fines (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  issue_id BIGINT NOT NULL UNIQUE,
  amount DECIMAL(10,2) NOT NULL,
  paid BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  paid_at TIMESTAMP NULL,
  CONSTRAINT fk_fines_issue FOREIGN KEY (issue_id) REFERENCES book_issues(id),
  INDEX idx_fines_paid (paid)
);
