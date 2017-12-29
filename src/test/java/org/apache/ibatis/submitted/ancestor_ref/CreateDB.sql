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
drop table friend if exists;

create table users (
  id int,
  name varchar(20)
);

create table friend (
  user_id int,
  friend_id int
);

insert into users (id, name) values
(1, 'User1'), (2, 'User2'), (3, 'User3');

insert into friend (user_id, friend_id) values
(1, 2), (2, 2), (2, 3);

drop table blog if exists;
drop table author if exists;

create table blog (
  id int,
  title varchar(16),
  author_id int,
  co_author_id int
);

create table author (
  id int,
  name varchar(16),
  reputation int
);

insert into blog (id, title, author_id, co_author_id) values
(1, 'Blog1', 1, 2), (2, 'Blog2', 2, 3);

insert into author (id, name, reputation) values
(1, 'Author1', 1), (2, 'Author2', 2), (3, 'Author3', 3);
