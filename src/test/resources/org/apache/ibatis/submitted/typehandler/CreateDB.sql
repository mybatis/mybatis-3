--
--    Copyright 2009-2025 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       https://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

drop table users if exists;
drop table product if exists;
drop table vague if exists;

create table users (
  id int,
  name varchar(20),
  city varchar(20),
  state char(20)
);

create table product (
  id int identity,
  name varchar(20),
  released_on date
);

create table vague (
  id int identity,
  vague other
);

insert into users (id, name, city, state) values(1, '   User1', '  Carmel  ', '  IN ');

insert into product (id, name, released_on) values
(1, 'iPod', '2001-11-10'),
(2, 'iPad', '2010-04-03');

insert into vague (id, vague) values (1, null);
