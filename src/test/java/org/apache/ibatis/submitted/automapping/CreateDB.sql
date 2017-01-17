--
--    Copyright 2009-2017 the original author or authors.
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

drop table users if exists;

drop table books if exists;

create table users (
  id int,
  name varchar(20),
  phone varchar(20),
  phone_number bigint
);

create table books (
  version int,
  name varchar(20)
);

create table pets (
  id int,
  owner int,
  breeder int,
  name varchar(20)
);

create table breeder (
  id int,
  name varchar(20)
);

-- '+86 12345678901' can't be converted to a number
insert into users (id, name, phone, phone_number) values(1, 'User1', '+86 12345678901', 12345678901);
insert into users (id, name, phone, phone_number) values(2, 'User2', '+86 12345678902', 12345678902);

insert into books (version, name) values(99, 'Learn Java');

insert into pets (id, owner, breeder, name) values(11, 1, null, 'Ren');
insert into pets (id, owner, breeder, name) values(12, 2, 101, 'Chien');
insert into pets (id, owner, breeder, name) values(13, 2, null, 'Kotetsu');

insert into breeder (id, name) values(101, 'John');
