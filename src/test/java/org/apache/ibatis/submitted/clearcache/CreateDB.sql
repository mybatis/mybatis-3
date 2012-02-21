drop table if exists address;
drop table if exists person;

create table person(
	id int,
	firstname varchar(20),
	lastname varchar(20)
);

create table address(
	id int,
	personId int,
	street varchar(20),
	city varchar(20),
	state varchar(2),
	zip varchar(5)
);