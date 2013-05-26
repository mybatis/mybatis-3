--
--    Copyright 2009-2012 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

drop table if exists person;
create table person (
id int,
name varchar(32),
billing_address_id int,
shipping_address_id int,
room_id int
);

drop table if exists address;
create table address (
id int,
state varchar(32),
city varchar(32),
phone1_id int,
phone2_id int,
addr_type int,
caution varchar(64)
);

drop table if exists zip;
create table zip (
state varchar(32),
city varchar(32),
zip_code int
);

drop table if exists phone;
create table phone (
id int,
phone varchar(32),
area_code varchar(2)
);

drop table if exists pet;
create table pet (
id int,
owner_id int,
name varchar(32),
room_id int
);

drop table if exists state_bird;
create table state_bird (
state varchar(32),
bird varchar(32)
);

drop table if exists room;
create table room (
room_id int,
room_name varchar(32)
);

insert into room (room_id, room_name) values (31, 'Sakura');
insert into room (room_id, room_name) values (32, 'Ume');
insert into room (room_id, room_name) values (33, 'Tsubaki');

insert into pet (id, owner_id, name, room_id) values (100, 1, 'Kotetsu', 32);
insert into pet (id, owner_id, name, room_id) values (101, 1, 'Chien', null);
insert into pet (id, owner_id, name, room_id) values (102, 3, 'Dodo', 31);

insert into phone (id, phone, area_code) values (1000, '0123', '11');
insert into phone (id, phone, area_code) values (1001, '4567', '33');
insert into phone (id, phone, area_code) values (1002, '8888', '55');
insert into phone (id, phone, area_code) values (1003, '9999', '77');

insert into state_bird (state, bird) values ('IL', 'Cardinal');
insert into state_bird (state, bird) values ('CA', 'California Valley Quail');
insert into state_bird (state, bird) values ('TX', 'Mockingbird');

insert into zip (state, city, zip_code) values ('IL', 'Chicago', 81);
insert into zip (state, city, zip_code) values ('CA', 'San Francisco', 82);
insert into zip (state, city, zip_code) values ('CA', 'Los Angeles', 83);
insert into zip (state, city, zip_code) values ('TX', 'Dallas', 84);

insert into address (id, state, city, phone1_id, phone2_id, addr_type, caution) values (10, 'IL', 'Chicago', 1000, 1001, 0, null);
insert into address (id, state, city, phone1_id, phone2_id, addr_type, caution) values (11, 'CA', 'San Francisco', 1002, null, 1, 'Has a big dog.');
insert into address (id, state, city, phone1_id, phone2_id, addr_type, caution) values (12, 'CA', 'Los Angeles', null, null, 1, 'No door bell.');
insert into address (id, state, city, phone1_id, phone2_id, addr_type, caution) values (13, 'TX', 'Dallas', 1003, 1001, 0, null);

insert into person (id, name, billing_address_id, shipping_address_id, room_id) values (1, 'John', 10, 11, 33);
insert into person (id, name, billing_address_id, shipping_address_id, room_id) values (2, 'Rebecca', 12, null, null);
insert into person (id, name, billing_address_id, shipping_address_id, room_id) values (3, 'Keith', null, 13, null);
