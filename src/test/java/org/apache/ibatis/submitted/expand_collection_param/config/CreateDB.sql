create table users (
  id int,
  first_name varchar(100),
  last_name varchar(100),
  role varchar(20),
  status int
);

INSERT INTO users (id, first_name, last_name, role, status) VALUES (1, 'John', 'Twain', 'WRITER', 0);
INSERT INTO users (id, first_name, last_name, role, status) VALUES (2, 'Jacob', 'Smith', 'WRITER', 1);
INSERT INTO users (id, first_name, last_name, role, status) VALUES (3, 'Thomas', 'Clinton', 'REVIEWER', 2);
INSERT INTO users (id, first_name, last_name, role, status) VALUES (4, 'Bill', 'Snow', 'REVIEWER', 0);
INSERT INTO users (id, first_name, last_name, role, status) VALUES (5, 'Mark', 'Carlson', 'ADMIN', 1);
INSERT INTO users (id, first_name, last_name, role, status) VALUES (6, 'John', 'Peterson', 'ADMIN', 2);

