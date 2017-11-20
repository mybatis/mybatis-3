--
--    Copyright 2009-2017 the original author or authors.
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

DROP TABLE child_t
IF EXISTS;

DROP TABLE parent_t
IF EXISTS;

CREATE TABLE child_t (
  id     VARCHAR(10),
  name   VARCHAR(30),
  family VARCHAR(10)
);

CREATE TABLE parent_t (
  id     VARCHAR(10),
  name   VARCHAR(30),
  family VARCHAR(10)
);

CREATE VIEW child AS
  SELECT
    c.id,
    c.name,
    p.id   AS p_id,
    p.name AS p_name
  FROM child_t c LEFT JOIN parent_t p ON (c.family = p.family);

INSERT INTO child_t VALUES ('C10', 'Child Name', 'FAM1');
INSERT INTO parent_t VALUES ('P11', 'Parent Name', 'FAM1');
