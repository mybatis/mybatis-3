create table person (
  id int,
  firstName varchar(100),
  lastName varchar(100),
  parent_id int default null,
  parent_firstName varchar(100) default null,
  parent_lastName varchar(100) default null
);

INSERT INTO person (id, firstName, lastName)
VALUES (1, 'John', 'Smith');

INSERT INTO person (id, firstName, lastName, parent_id, parent_firstName, parent_lastName)
VALUES (2, 'Christian', 'Poitras', 1, 'John', 'Smith');

INSERT INTO person (id, firstName, lastName, parent_id, parent_firstName, parent_lastName)
VALUES (3, 'Clinton', 'Begin', 1, 'John', 'Smith');
