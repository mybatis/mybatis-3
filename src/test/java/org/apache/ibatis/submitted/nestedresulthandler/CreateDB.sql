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

drop table persons if exists;
drop table items if exists;

create table persons (
  id int,
  name varchar(20)
);

create table items (
  id int,
  owner int,
  name varchar(20)
);
 
insert into persons (id, name) values (1, 'grandma');
insert into persons (id, name) values (2, 'sister');
insert into persons (id, name) values (3, 'brother');

insert into items (id, owner, name) values (1, 1, 'book');
insert into items (id, owner, name) values (2, 1, 'tv');
insert into items (id, owner, name) values (3, 2, 'shoes');
insert into items (id, owner, name) values (4, 3, 'car');
insert into items (id, owner, name) values (5, 2, 'phone');
