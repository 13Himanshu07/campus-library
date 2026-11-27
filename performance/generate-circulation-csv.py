#!/usr/bin/env python3
"""Export private JMeter circulation fixtures from the dedicated staging database."""

import argparse
import csv
import os
import subprocess
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument("output", type=Path, help="private CSV output path; do not commit")
parser.add_argument("--users", type=int, default=500, help="fixture rows to export (1..10000)")
args = parser.parse_args()
if not 1 <= args.users <= 10000:
    parser.error("--users must be between 1 and 10000")

mysql = os.environ.get("MYSQL_BIN", "mysql")
host = os.environ.get("MYSQL_HOST", "127.0.0.1")
port = os.environ.get("MYSQL_PORT", "3306")
user = os.environ.get("MYSQL_USER", "library_app_loadtest")
database = os.environ.get("MYSQL_DATABASE", "library_management_staging")
password = os.environ.get("MYSQL_PASSWORD")
if not password:
    raise SystemExit("Set MYSQL_PASSWORD in the environment; it is not read from command-line arguments.")

query = f"""
SELECT student.email, 'LibraryDemo9!', borrow_book.id, active_loan.id,
       'load-librarian@library.local', 'LibraryDemo9!'
FROM users student
JOIN books borrow_book ON borrow_book.isbn=CONCAT('LT', LPAD(1000 + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(student.email, '@', 1), '-', -1) AS UNSIGNED), 11, '0'))
JOIN books loan_book ON loan_book.isbn=CONCAT('LT', LPAD(CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(student.email, '@', 1), '-', -1) AS UNSIGNED), 11, '0'))
JOIN book_issues active_loan ON active_loan.book_id=loan_book.id AND active_loan.user_id=student.id AND active_loan.return_date IS NULL
WHERE student.email LIKE 'load-student-%@library.local'
ORDER BY student.email
LIMIT {args.users}
"""
env = os.environ.copy()
env["MYSQL_PWD"] = password
process = subprocess.run(
    [mysql, "--host", host, "--port", port, "--user", user, "--database", database,
     "--batch", "--skip-column-names", "--raw", "--execute", query],
    env=env, check=True, capture_output=True, text=True,
)
rows = [line.split("\t") for line in process.stdout.splitlines() if line]
if len(rows) != args.users:
    raise SystemExit(f"Expected {args.users} complete circulation fixtures; found {len(rows)}. Run seed-large-dataset.sql first.")
args.output.parent.mkdir(parents=True, exist_ok=True)
with args.output.open("w", newline="", encoding="utf-8") as output:
    writer = csv.writer(output)
    writer.writerow(["studentEmail", "studentPassword", "bookId", "issueId", "librarianEmail", "librarianPassword"])
    writer.writerows(rows)
try:
    args.output.chmod(0o600)
except OSError:
    pass
print(f"Wrote {len(rows)} private circulation rows to {args.output}")
