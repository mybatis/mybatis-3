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

-- parent
drop table if exists parent;
create table parent (id int, nr_person_id int);

insert into parent (id, nr_person_id)
values (1, 1);

-- child
drop table if exists child;
create table child (id int, nr_parent_id int, nr_person_id int);

insert into child (id, nr_parent_id, nr_person_id)
values (1, 1, 1);

insert into child (id, nr_parent_id, nr_person_id)
values (2, 1, 1);

-- person
drop table if exists person;
create table person (id int, nr_department int);

insert into person (id,  nr_department) 
values (1, 1);

-- productattribute
drop table if exists productattribute;
create table productattribute (nr_id int);

insert into productattribute(nr_id) 
values (1);

-- department
drop table if exists department;
create table department (nr_id int,nr_attribute int,person int);

insert into department(nr_id,nr_attribute,person) 
values (1,1,1);

