--
--    Copyright 2009-2019 the original author or authors.
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

drop table vehicle if exists;
drop table owner if exists;

create table vehicle (
  id int,
  maker varchar(20),
  vehicle_type int,
  door_count int,
  carrying_capacity float
);

insert into vehicle (id, maker, vehicle_type, door_count, carrying_capacity) values
(1, 'Maker1', 1, 5, null),
(2, 'Maker2', 2, null, 1.5);

create table owner (
    id int,
    name varchar(20),
    vehicle_type varchar(20),
    vehicle_id int
);

insert into owner (id, name, vehicle_type, vehicle_id) values
(1, 'Owner1', 'truck', 2),
(2, 'Owner2', 'car', 1);
