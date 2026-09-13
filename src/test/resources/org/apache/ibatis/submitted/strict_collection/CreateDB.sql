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

CREATE TABLE groups (
  id INTEGER PRIMARY KEY,
  name VARCHAR(50)
);

CREATE TABLE users (
  id INTEGER PRIMARY KEY,
  name VARCHAR(50),
  group_id INTEGER
);

INSERT INTO groups (id, name) VALUES (1, 'Group 1');
INSERT INTO groups (id, name) VALUES (2, 'Group 2');

INSERT INTO users (id, name, group_id) VALUES (1, 'User 1', 1);
INSERT INTO users (id, name, group_id) VALUES (2, 'User 2', 1);
INSERT INTO users (id, name, group_id) VALUES (3, 'User 3', 2);
