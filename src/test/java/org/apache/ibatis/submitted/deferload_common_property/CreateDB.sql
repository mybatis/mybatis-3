create table Child (
  id int,
  name varchar(100),
  father_id int
);
create table Father (
  id int,
  name varchar(100)
);

INSERT INTO Child (id, name, father_id)
VALUES (1, 'John Smith jr', 1);
INSERT INTO Child (id, name, father_id)
VALUES (2, 'John Smith jr 2', 1);

INSERT INTO Father (id, name)
VALUES (1, 'John Smith');
