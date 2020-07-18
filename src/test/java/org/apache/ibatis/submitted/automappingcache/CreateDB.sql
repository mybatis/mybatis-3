drop table users if exists;
drop table inner_entity if exists ;
drop table another_entity if exists ;
create table users (
  id int,
  name varchar(20)
);

create table inner_entity
(
    id int,
    user_id int,
    complexCalculated int
);

create table another_entity
(
    id int,
    otherField varchar(20)
);

insert into users (id, name) values
(1, 'PatternOne'),
(2, 'PatternOne'),
(3, 'PatternTwo'),
(4, 'PatternTwo');

insert into inner_entity (id, user_id, complexCalculated) values
(1, 1, 111),
(2, 1, 1111),
(3, 2, 222),
(4, 2, 2222);

insert into another_entity (id, otherField) values
(1, 'EntityOne'),
(2, 'EntityTwo');