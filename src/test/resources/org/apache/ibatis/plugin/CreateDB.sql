--
--    Copyright 2009-2024 the original author or authors.
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

drop schema public if exists; -- empty public remains

create table public.users (
  id int,
  name varchar(20)
);

insert into public.users (id, name) values (1, 'Public user 1');

drop schema myschema if exists;
create schema myschema;

create table myschema.users (
  id int,
  name varchar(20)
);

insert into myschema.users (id, name) values (1, 'Private user 1');

