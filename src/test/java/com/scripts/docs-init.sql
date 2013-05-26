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

DROP TABLE DOCUMENTS;
DROP TABLE PERSON_DOCUMENTS;
DROP TABLE DOCUMENT_ATTRIBUTES;

CREATE TABLE DOCUMENTS  (
   DOCUMENT_ID           INTEGER                          NOT NULL,
   DOCUMENT_TITLE        VARCHAR(32),
   DOCUMENT_TYPE         VARCHAR(32),
   DOCUMENT_PAGENUMBER   INTEGER,
   DOCUMENT_CITY         VARCHAR(32),
   PRIMARY KEY (DOCUMENT_ID)
);

INSERT INTO DOCUMENTS VALUES (1, 'The World of Null-A', 'BOOK', 55, null);
INSERT INTO DOCUMENTS VALUES (2, 'Le Progres de Lyon', 'NEWSPAPER', null , 'Lyon');
INSERT INTO DOCUMENTS VALUES (3, 'Lord of the Rings', 'BOOK', 3587, null);
INSERT INTO DOCUMENTS VALUES (4, 'Le Canard enchaine', 'NEWSPAPER', null , 'Paris');
INSERT INTO DOCUMENTS VALUES (5, 'Le Monde', 'BROADSHEET', null , 'Paris');
INSERT INTO DOCUMENTS VALUES (6, 'Foundation', 'MONOGRAPH', 557, null);

CREATE TABLE PERSON_DOCUMENTS (
  PERSON_ID INTEGER NOT NULL,
  PERSON_NAME VARCHAR(50),
  DOCUMENT_ID INTEGER NOT NULL,
  PRIMARY KEY (PERSON_ID)
);

insert into person_documents values (1, 'Jeff', 2);
insert into person_documents values (2, 'Matt', 3);
insert into person_documents values (3, 'Amy', 6);

create table Document_Attributes (
  document_id int not null,
  attribute varchar(50) not null
);

insert into Document_Attributes(document_id, attribute) values (1, 'English');
insert into Document_Attributes(document_id, attribute) values (1, 'Sci-Fi');
insert into Document_Attributes(document_id, attribute) values (2, 'French');
insert into Document_Attributes(document_id, attribute) values (3, 'English');
insert into Document_Attributes(document_id, attribute) values (3, 'Fantasy');
