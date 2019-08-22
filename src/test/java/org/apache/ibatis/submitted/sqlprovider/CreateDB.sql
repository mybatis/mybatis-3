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

drop table users if exists;
drop table memos if exists;

create table users (
  id int,
  name varchar(20),
  logical_delete boolean default false
);

create table memos (
   id int,
   memo varchar(1024),
);

insert into users (id, name) values(1, 'User1');
insert into users (id, name) values(2, 'User2');
insert into users (id, name) values(3, 'User3');
insert into users (id, name, logical_delete) values(4, 'User4', true);

insert into memos (id, memo) values(1, 'memo1');
