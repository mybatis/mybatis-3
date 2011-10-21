create table student (
  id int,
  name varchar(100),
  teacher_id int
);
create table teacher (
  id int,
  name varchar(100)
);

INSERT INTO student (id, name, teacher_id)
VALUES (1, 'John Smith', 1);
INSERT INTO student (id, name, teacher_id)
VALUES (2, 'Joe Smith', 1);

INSERT INTO teacher (id, name)
VALUES (1, 'Christian Poitras');
