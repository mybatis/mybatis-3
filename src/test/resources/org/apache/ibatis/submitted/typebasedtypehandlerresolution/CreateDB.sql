--
--    Copyright 2009-2025 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       https://www.apache.org/licenses/LICENSE-2.0
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
  strvalue varchar(20),
  intvalue int,
  datevalue int,
  datevalue2 date,
  strings varchar(20),
  integers varchar(20)
);

insert into users (id, strvalue, intvalue, datevalue, datevalue2, strings, integers)
  values(1, 'garden', 31, 20200511, '2020-05-09', 'a,b,c', '1,3,5');
insert into users (id, strvalue, intvalue, datevalue, datevalue2, strings, integers)
  values(2, 'vault', 32, 20200512, '2020-05-10', 'd,e,f', '7,9');


create table product (
  id int identity,
  name varchar(20)
);
