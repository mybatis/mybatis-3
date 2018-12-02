--
--    Copyright 2009-2018 the original author or authors.
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

drop table users if exists
go

drop procedure getusers if exists
go

create table users (
  id int,
  name varchar(20)
)
go

create procedure getusers()
modifies sql data
dynamic result sets 1
BEGIN ATOMIC
  declare cur cursor for select * from users order by id;
  open cur;
END
go

insert into users (id, name) values(1, 'User1')
go

insert into users (id, name) values(2, 'User1')
go

insert into users (id, name) values(3, 'User1')
go

insert into users (id, name) values(4, 'User1')
go

insert into users (id, name) values(5, 'User1')
go



