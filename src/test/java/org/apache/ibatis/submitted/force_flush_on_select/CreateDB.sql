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

 DROP TABLE person;

 CREATE TABLE person (
  id int,
  firstName varchar(100),
  lastName varchar(100)
);

INSERT INTO person (id, firstName, lastName)
VALUES (1, 'John', 'Smith');

INSERT INTO person (id, firstName, lastName)
VALUES (2, 'Christian', 'Poitras');

INSERT INTO person (id, firstName, lastName)
VALUES (3, 'Clinton', 'Begin');
