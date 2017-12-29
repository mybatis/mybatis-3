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

drop table users if exists;

create table users (
  id int,
  name varchar(20) ,
  dept_id int
);

insert into users (id, name,dept_id) values(1, 'User1',1);

drop table depts if exists;

create table depts (
  id int,
  name varchar(20)
);

insert into depts (id, name) values(1, 'Dept1');

