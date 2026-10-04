-- DESTRUCTIVE: permanently drops all tables and records in this application's database.
DROP DATABASE IF EXISTS library_management;
SOURCE database/schema.sql;
SOURCE database/sample_data.sql;
