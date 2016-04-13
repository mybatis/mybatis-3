--
--    Copyright 2009-2016 the original author or authors.
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
create table person (person_id int, person_name varchar(32));

drop table if exists pet;
create table pet (pet_id int, owner_id int, pet_name varchar(32));

insert into person (person_id, person_name) values (1, 'John');
insert into person (person_id, person_name) values (2, 'Rebecca');

insert into pet (pet_id, owner_id, pet_name) values (1, 1, 'Kotetsu');
insert into pet (pet_id, owner_id, pet_name) values (2, 1, 'Chien');
insert into pet (pet_id, owner_id, pet_name) values (3, 2, 'Ren');
