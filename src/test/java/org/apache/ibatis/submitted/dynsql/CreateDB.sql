create schema ibtest authorization dba;

create table ibtest.names (
id int,
description varchar(20)
);

insert into ibtest.names (id, description) values(1, 'Fred');
insert into ibtest.names (id, description) values(2, 'Wilma');
insert into ibtest.names (id, description) values(3, 'Pebbles');
insert into ibtest.names (id, description) values(4, 'Barney');
insert into ibtest.names (id, description) values(5, 'Betty');
insert into ibtest.names (id, description) values(6, 'Bamm Bamm');
