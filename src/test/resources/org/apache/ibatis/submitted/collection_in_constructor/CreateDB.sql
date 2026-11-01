--
--    Copyright 2009-2024 the original author or authors.
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

drop table store if exists;
drop table aisle if exists;
drop table clerk if exists;

create table store (
  id int,
  name varchar(20)
);

create table aisle (
  id int,
  name varchar(20),
  store_id int
);

create table clerk (
  id int,
  name varchar(20),
  is_manager boolean,
  store_id int
);

insert into store (id, name) values(1, 'Store 1');
insert into store (id, name) values(2, 'Store 2');
insert into store (id, name) values(3, 'Store 3');

insert into aisle (id, name, store_id) values(101, 'Aisle 101', 1);
insert into aisle (id, name, store_id) values(102, 'Aisle 102', 1);
insert into aisle (id, name, store_id) values(103, 'Aisle 103', 1);
insert into aisle (id, name, store_id) values(104, 'Aisle 104', 3);
insert into aisle (id, name, store_id) values(105, 'Aisle 105', 3);

insert into clerk (id, name, is_manager, store_id) values (1001, 'Clerk 1001', 0, 1);
insert into clerk (id, name, is_manager, store_id) values (1002, 'Clerk 1002', 1, 1);
insert into clerk (id, name, is_manager, store_id) values (1003, 'Clerk 1003', 0, 1);
insert into clerk (id, name, is_manager, store_id) values (1004, 'Clerk 1004', 0, 1);
insert into clerk (id, name, is_manager, store_id) values (1005, 'Clerk 1005', 1, 1);
