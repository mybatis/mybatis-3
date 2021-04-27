--
--    Copyright 2009-2020 the original author or authors.
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
DROP TABLE users if exists;

create table users (
  id int,
  first_name varchar(100),
  last_name varchar(100),
  role varchar(20),
  status int
);

INSERT INTO users (id, first_name, last_name, role, status) VALUES (1, 'John', 'Twain', 'WRITER', 0);
INSERT INTO users (id, first_name, last_name, role, status) VALUES (2, 'Jacob', 'Smith', 'WRITER', 1);
INSERT INTO users (id, first_name, last_name, role, status) VALUES (3, 'Thomas', 'Clinton', 'REVIEWER', 2);
INSERT INTO users (id, first_name, last_name, role, status) VALUES (4, 'Bill', 'Snow', 'REVIEWER', 0);
INSERT INTO users (id, first_name, last_name, role, status) VALUES (5, 'Mark', 'Carlson', 'ADMIN', 1);
INSERT INTO users (id, first_name, last_name, role, status) VALUES (6, 'John', 'Peterson', 'ADMIN', 2);

