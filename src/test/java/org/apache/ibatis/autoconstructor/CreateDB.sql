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

DROP TABLE subject
IF EXISTS;

CREATE TABLE subject (
  id     INT NOT NULL,
  name   VARCHAR(20),
  age    INT NOT NULL,
  height INT,
  weight INT
);

INSERT INTO subject (id, name, age, height, weight) VALUES (1, 'a', 10, 100, 45);
INSERT INTO subject (id, name, age, height, weight) VALUES (2, 'b', 10, NULL, 45);
INSERT INTO subject (id, name, age, height, weight) VALUES (2, 'b', 10, NULL, NULL);

