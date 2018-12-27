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

CREATE MEMORY TABLE POST(POST_ID INTEGER NOT NULL PRIMARY KEY,CONTENT VARCHAR(255) NOT NULL,AUTHOR_ID INTEGER NOT NULL);
CREATE MEMORY TABLE AUTHOR(AUTHOR_ID INTEGER NOT NULL PRIMARY KEY,NAME VARCHAR(255));
INSERT INTO POST VALUES(1,'content 1',1);
INSERT INTO POST VALUES(2,'content 2',1);
INSERT INTO POST VALUES(3,'content 3',1);
INSERT INTO AUTHOR VALUES(1,'James Brown');
ALTER TABLE POST ADD CONSTRAINT AUTHOR_CONSTRAINT FOREIGN KEY(AUTHOR_ID) REFERENCES AUTHOR(AUTHOR_ID);
