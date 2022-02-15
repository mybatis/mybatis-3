--
--    Copyright 2009-2022 the original author or authors.
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


-- do not run this script, this file just to show the table structure
drop table users if exists;

create table users (
  id int,
  code varchar(20),
  name varchar(20)
);

insert into users (id, code, name) values(1, 'Code1', 'User1');
insert into users (id, code, name) values(2, 'Code2', 'User2');
insert into users (id, code, name) values(3, 'Code3', 'User3');
