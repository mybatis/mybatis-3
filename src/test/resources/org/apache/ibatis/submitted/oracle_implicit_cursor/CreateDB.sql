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

-- @DELIMITER |
begin
  execute immediate 'drop table author';
exception
  when others then
    if sqlcode != -942 then
      raise;
    end if;
end;
begin
  execute immediate 'drop table book';
exception
  when others then
    if sqlcode != -942 then
      raise;
    end if;
end;
|
-- @DELIMITER ;

create table author (id int, name varchar(10));

insert into author (id, name) values (1, 'John');
insert into author (id, name) values (2, 'Jane');


create table book (id int, author_id int, name varchar(10));

insert into book (id, author_id, name) values (1, 1, 'C#');
insert into book (id, author_id, name) values (2, 1, 'Python');
insert into book (id, author_id, name) values (3, 2, 'SQL');
insert into book (id, author_id, name) values (4, 2, 'Java');
insert into book (id, author_id, name) values (5, 1, 'Ruby');
