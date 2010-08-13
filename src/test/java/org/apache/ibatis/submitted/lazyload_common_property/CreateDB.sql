create table Child (
  id int,
  name varchar(100),
  father_id int,
  grand_father_id int
);
create table Father (
  id int,
  name varchar(100),
  grand_father_id int
);
create table GrandFather (
  id int,
  name varchar(100)
);

INSERT INTO Child (id, name, father_id, grand_father_id)
VALUES (1, 'John Smith jr', 1, 1);

INSERT INTO Father (id, name, grand_father_id)
VALUES (1, 'John Smith', 1);

INSERT INTO GrandFather (id, name)
VALUES (1, 'John Smith sen');
