create schema ibtest authorization dba;

create table ibtest.names (
id int,
firstName varchar(20),
lastName varchar(20)
);

insert into ibtest.names (id, firstName, lastName) values(1, 'Fred', 'Flintstone');
insert into ibtest.names (id, firstName, lastName) values(2, 'Wilma', 'Flintstone');
insert into ibtest.names (id, firstName, lastName) values(3, 'Pebbles', 'Flintstone');
insert into ibtest.names (id, firstName, lastName) values(4, 'Barney', 'Rubble');
insert into ibtest.names (id, firstName, lastName) values(5, 'Betty', 'Rubble');
insert into ibtest.names (id, firstName, lastName) values(6, 'Bamm Bamm', 'Rubble');
