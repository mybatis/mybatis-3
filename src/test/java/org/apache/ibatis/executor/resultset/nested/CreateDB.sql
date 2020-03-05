DROP TABLE subject
IF EXISTS;

DROP TABLE match1
IF EXISTS;

CREATE TABLE subject (
  id     INT NOT NULL,
  name   VARCHAR(20)
);

insert into subject (id, name) values (1, 'zhangsan');
insert into subject (id, name) values (2, 'lisi');
insert into subject (id, name) values (3, 'wangwu');

CREATE TABLE match1 (
  id        INT NOT NULL,
  user_id   INT,
  score     INT
);

insert into match1 (id, user_id, score) VALUES (1, 1, 1000);
insert into match1 (id, user_id, score) VALUES (2, 1, 300);
insert into match1 (id, user_id, score) VALUES (3, 2, 500);
insert into match1 (id, user_id, score) VALUES (4, 3, 500);
