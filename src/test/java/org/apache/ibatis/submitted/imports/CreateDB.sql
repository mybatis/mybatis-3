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

drop table user if exists;

create table users (
  id int,
  name varchar(20),
  age int
);

insert into users (id, name, age) values(1, 'User1', 33);
insert into users (id, name, age) values(2, 'User2', 34);
insert into users (id, name, age) values(3, 'User3', 35);
insert into users (id, name, age) values(4, 'User4', 36);
insert into users (id, name, age) values(5, 'User5', 37);


drop table company if exists;

create table company (
  id int,
  name varchar(20),
  asset int
);

insert into users (id, name, asset) values(1, 'company1', 33);
insert into users (id, name, asset) values(2, 'company2', 34);
insert into users (id, name, asset) values(3, 'company3', 35);
insert into users (id, name, asset) values(4, 'company4', 36);
insert into users (id, name, asset) values(5, 'company5', 37);

