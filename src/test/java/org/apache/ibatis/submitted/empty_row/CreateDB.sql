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

drop table parent if exists;
drop table child if exists;
drop table pet if exists;

create table parent (
id int,
col1 varchar(20),
col2 varchar(20)
);

create table child (
id int,
parent_id int,
name varchar(20)
);

create table pet (
id int,
parent_id int,
name varchar(20)
);

insert into parent (id, col1, col2) values
(1, null, null),
(2, null, null);

insert into child (id, parent_id, name) values
(1, 2, 'john'),
(2, 2, 'mary');
