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

DROP TABLE ARTICLE_INDEX;

CREATE TABLE ARTICLE_INDEX (
   CATEGORY_ID           INTEGER                          NOT NULL,
   CATEGORY_TITLE        VARCHAR(32),
   TOPIC_TITLE           VARCHAR(32),
   TOPIC_DESCRIPTION     VARCHAR(80),
   PRIMARY KEY (CATEGORY_ID)
);

INSERT INTO ARTICLE_INDEX VALUES (1, 'Health', 'Heart', 'Exercises for your heart');
INSERT INTO ARTICLE_INDEX VALUES (2, 'Health', 'Health', 'General health for people');
INSERT INTO ARTICLE_INDEX VALUES (3, 'Health', 'Love', 'How love affects your health1');
INSERT INTO ARTICLE_INDEX VALUES (4, 'Love', 'Heart', 'Language of the heart');
INSERT INTO ARTICLE_INDEX VALUES (5, 'Love', 'Health', 'How love affects your health2');
INSERT INTO ARTICLE_INDEX VALUES (6, 'Combat', 'Health', 'Staying alive in combat');
INSERT INTO ARTICLE_INDEX VALUES (7, 'Love', 'Love', 'Love is all around');
INSERT INTO ARTICLE_INDEX VALUES (8, 'Health', 'Heart', 'More Exercises for your heart');
