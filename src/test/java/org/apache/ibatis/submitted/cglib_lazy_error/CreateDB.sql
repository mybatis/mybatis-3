create table person (
id int,
firstName varchar(100),
lastName varchar(100),
parent int DEFAULT NULL
);

INSERT INTO person (id, firstName, lastName, parent)
VALUES (1, 'John sr.', 'Smith', null);
INSERT INTO person (id, firstName, lastName, parent)
VALUES (2, 'John', 'Smith', 1);
INSERT INTO person (id, firstName, lastName, parent)
VALUES (3, 'John jr.', 'Smith', 2);
