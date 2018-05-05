--
--    Copyright 2009-2018 the original author or authors.
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

DROP TABLE extensive_subject
IF EXISTS;

CREATE TABLE subject (
  id     INT NOT NULL,
  name   VARCHAR(20),
  age    INT NOT NULL,
  height INT,
  weight INT,
  active BIT,
  dt     TIMESTAMP
);

CREATE TABLE extensive_subject (
  aByte      TINYINT,
  aShort     SMALLINT,
  aChar      CHAR,
  anInt      INT,
  aLong      BIGINT,
  aFloat     FLOAT,
  aDouble    DOUBLE,
  aBoolean   BIT,
  aString    VARCHAR(255),
  anEnum     VARCHAR(50),
  aClob      LONGVARCHAR,
  aBlob      LONGVARBINARY,
  aTimestamp TIMESTAMP
);

INSERT INTO subject VALUES
  (1, 'a', 10, 100, 45, 1, CURRENT_TIMESTAMP),
  (2, 'b', 10, NULL, 45, 1, CURRENT_TIMESTAMP),
  (2, 'c', 10, NULL, NULL, 0, CURRENT_TIMESTAMP);

INSERT INTO extensive_subject
VALUES
  (1, 1, 'a', 1, 1, 1, 1.0, 1, 'a', 'AVALUE', 'ACLOB', 'aaaaaabbbbbb', CURRENT_TIMESTAMP),
  (2, 2, 'b', 2, 2, 2, 2.0, 2, 'b', 'BVALUE', 'BCLOB', '010101010101', CURRENT_TIMESTAMP),
  (3, 3, 'c', 3, 3, 3, 3.0, 3, 'c', 'CVALUE', 'CCLOB', '777d010078da', CURRENT_TIMESTAMP);