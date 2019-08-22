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

CREATE TABLE immutables (
    immutable_id          INTEGER PRIMARY KEY,
    immutable_description VARCHAR (30) NOT NULL
);

INSERT INTO immutables (immutable_id, immutable_description) VALUES (1, 'Description of immutable');
INSERT INTO immutables (immutable_id, immutable_description) VALUES (2, 'Another immutable');