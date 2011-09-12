create table person (
  id int,
  firstName varchar(100),
  lastName varchar(100),
  order_type varchar(100)
);

INSERT INTO person (id, firstName, lastName, order_type)
VALUES (1, 'John', 'Smith', 'fast');

INSERT INTO person (id, firstName, lastName, order_type)
VALUES (2, 'Christian', 'Poitras', 'fast');

INSERT INTO person (id, firstName, lastName, order_type)
VALUES (3, 'Clinton', 'Begin', 'slow');
