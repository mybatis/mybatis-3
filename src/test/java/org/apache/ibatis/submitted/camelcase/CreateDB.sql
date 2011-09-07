drop table names if exists;

create table names (
  ID int,
  FIRST_NAME varchar(20),
  LAST_NAME varchar(20)
);

insert into names (ID, FIRST_NAME, LAST_NAME) values(1, 'Fred', 'Flintstone');
insert into names (ID, FIRST_NAME, LAST_NAME) values(2, 'Wilma', 'Flintstone');
insert into names (ID, FIRST_NAME, LAST_NAME) values(3, 'Pebbles', 'Flintstone');
insert into names (ID, FIRST_NAME, LAST_NAME) values(4, 'Barney', 'Rubble');
insert into names (ID, FIRST_NAME, LAST_NAME) values(5, 'Betty', 'Rubble');
insert into names (ID, FIRST_NAME, LAST_NAME) values(6, 'Bamm Bamm', 'Rubble');
