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

drop table car if exists;
drop table part if exists;

create table car (
  car_id int,
  name varchar(20)
);

create table part (
  part_id int,
  name varchar(20),
  car_id int
);

insert into car (car_id, name) values(1, 'Audi');
insert into car (car_id, name) values(2, 'Ford');
insert into car (car_id, name) values(3, 'Fiat');

insert into part (part_id, name, car_id) values(100, 'door', 1);
insert into part (part_id, name, car_id) values(101, 'windshield', 1);
insert into part (part_id, name, car_id) values(101, 'brakes', 1);
