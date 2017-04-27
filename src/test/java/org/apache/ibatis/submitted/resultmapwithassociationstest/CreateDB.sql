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

drop table person if exists;
drop table address if exists;

CREATE TABLE address
(
    id int NOT NULL,
    CONSTRAINT pk_address PRIMARY KEY (id),
)
;


CREATE TABLE person
(
    id int NOT NULL,
    id_address integer NOT NULL,
    CONSTRAINT pk_person PRIMARY KEY (id),
    CONSTRAINT fk_person_id_address FOREIGN KEY (id_address) REFERENCES address (id) ON UPDATE RESTRICT ON DELETE RESTRICT
)
;

INSERT INTO address (id) VALUES  (1);
INSERT INTO address (id) VALUES  (2);
INSERT INTO address (id) VALUES  (3);
INSERT INTO person (id, id_address) VALUES (1, 1);
INSERT INTO person (id, id_address) VALUES (2, 2);
INSERT INTO person (id, id_address) VALUES (3, 3);
