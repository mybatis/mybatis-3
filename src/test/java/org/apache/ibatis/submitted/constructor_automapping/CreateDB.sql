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

drop table articles if exists;
drop table authors if exists;

create table articles (
  id int,
  title varchar(20),
  author_id int
);

create table authors (
  id int,
  name varchar(20)
);

insert into articles (id, title, author_id) values
(1, 'Article1', 100),
(2, 'Article2', 200);

insert into authors (id, name) values
(100, 'Author1'),
(200, 'Author2');
