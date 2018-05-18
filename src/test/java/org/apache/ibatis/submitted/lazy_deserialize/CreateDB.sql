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

CREATE TABLE table_foo (
    id      INTEGER PRIMARY KEY,
    id_bar  INTEGER NOT NULL);

CREATE TABLE table_bar (
    id      INTEGER PRIMARY KEY);

ALTER TABLE table_foo ADD CONSTRAINT bar_fk
FOREIGN KEY (id_bar) REFERENCES table_bar (id)
ON UPDATE CASCADE ON DELETE RESTRICT;

INSERT INTO table_bar (id) VALUES (10);
INSERT INTO table_foo (id, id_bar) VALUES (1, 10);
