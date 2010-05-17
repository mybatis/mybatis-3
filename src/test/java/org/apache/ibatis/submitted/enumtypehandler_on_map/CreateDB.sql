create table person (
  id int,
  firstName varchar(100),
  lastName varchar(100),
  personType varchar(100) DEFAULT NULL
);

INSERT INTO person (id, firstName, lastName, personType)
VALUES (1, 'John', 'Smith', 'PERSON');
