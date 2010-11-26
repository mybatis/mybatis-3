drop table if exists person;
create table person (person_id int, person_name varchar(32));

drop table if exists pet;
create table pet (pet_id int, owner_id int, pet_name varchar(32));

insert into person (person_id, person_name) values (1, 'John');
insert into person (person_id, person_name) values (2, 'Rebecca');

insert into pet (pet_id, owner_id, pet_name) values (1, 1, 'Kotetsu');
insert into pet (pet_id, owner_id, pet_name) values (2, 1, 'Chien');
insert into pet (pet_id, owner_id, pet_name) values (3, 2, 'Ren');
