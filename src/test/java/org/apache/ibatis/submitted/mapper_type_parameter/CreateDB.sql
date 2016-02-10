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

drop table person if exists;
drop table country if exists;

create table person(
	id int IDENTITY,
	name varchar(20)
);

create table country(
	id bigint IDENTITY,
	name varchar(20)
);

insert into person (id, name) values (1, 'Jane'), (2, 'John'); 
insert into country (id, name) values (1, 'Japan'), (2, 'New Zealand'); 
