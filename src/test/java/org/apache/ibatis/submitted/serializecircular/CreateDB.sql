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
create table person (id int, nr_department int);

insert into person (id,  nr_department) 
values (1, 1);

drop table if exists productattribute;
create table productattribute (nr_id int);

insert into productattribute(nr_id) 
values (1);

drop table if exists department;
create table department (nr_id int,nr_attribute int,person int);

insert into department(nr_id,nr_attribute,person) 
values (1,1,1);

