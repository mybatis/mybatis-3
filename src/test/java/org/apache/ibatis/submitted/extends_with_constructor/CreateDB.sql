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

create table student (
  id int,
  name varchar(100),
  teacher_id int
);
create table teacher (
  id int,
  name varchar(100)
);

INSERT INTO student (id, name, teacher_id)
VALUES (1, 'John Smith', 1);
INSERT INTO student (id, name, teacher_id)
VALUES (2, 'Joe Smith', 1);

INSERT INTO teacher (id, name)
VALUES (1, 'Christian Poitras');
