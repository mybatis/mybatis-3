--
--    Copyright 2009-2012 the original author or authors.
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

create table Child (
  id int,
  name varchar(100),
  father_id int,
  grand_father_id int
);
create table Father (
  id int,
  name varchar(100),
  grand_father_id int
);
create table GrandFather (
  id int,
  name varchar(100)
);

INSERT INTO Child (id, name, father_id, grand_father_id)
VALUES (1, 'John Smith jr', 1, 1);

INSERT INTO Father (id, name, grand_father_id)
VALUES (1, 'John Smith', 1);

INSERT INTO GrandFather (id, name)
VALUES (1, 'John Smith sen');
