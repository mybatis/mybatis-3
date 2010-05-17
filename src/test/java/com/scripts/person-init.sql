-- HSQL DATABASE

-- Dropping Tables

DROP TABLE PERSON;

-- Creating Tables

create table person (
  id int not null, 
  first_name char(50) not null,
  last_name char(50) not null,
  primary key (id)
);


-- Creating Test Data

insert into person values(1, 'Jeff', 'Jones');
insert into person values(2, 'Matt', 'Jones');
insert into person values(3, 'Amy', 'Jones');
insert into person values(4, 'Scott', 'Jones');
insert into person values(5, 'Wilma', 'Jones');
insert into person values(6, 'Fred', 'Jones');
insert into person values(7, 'Jeff', 'Smith');
insert into person values(8, 'Matt', 'Smith');
insert into person values(9, 'Amy', 'Smith');
