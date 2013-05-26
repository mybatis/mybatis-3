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

-- HSQL DATABASE

-- Dropping Tables

DROP TABLE LINE_ITEM;

-- Creating Tables

CREATE TABLE LINE_ITEM (
    LIN_ID             INT NOT NULL,
    LIN_ORD_ID         INT NOT NULL,
    LIN_ITM_CODE       VARCHAR(255) NOT NULL,
    LIN_QUANTITY       INT NOT NULL,
    LIN_PRICE          DECIMAL NOT NULL,
    PRIMARY KEY (LIN_ID, LIN_ORD_ID)
);


-- Creating Test Data

INSERT INTO LINE_ITEM VALUES (1, 10, 'ESM-34', 1, 45.43);
INSERT INTO LINE_ITEM VALUES (2, 10, 'QSM-98', 8, 8.40);
INSERT INTO LINE_ITEM VALUES (1, 9, 'DSM-78', 2, 45.40);
INSERT INTO LINE_ITEM VALUES (2, 9, 'TSM-12', 2, 32.12);
INSERT INTO LINE_ITEM VALUES (1, 8, 'DSM-16', 4, 41.30);
INSERT INTO LINE_ITEM VALUES (2, 8, 'GSM-65', 1, 2.20);
INSERT INTO LINE_ITEM VALUES (1, 7, 'WSM-27', 7, 52.10);
INSERT INTO LINE_ITEM VALUES (2, 7, 'ESM-23', 2, 123.34);
INSERT INTO LINE_ITEM VALUES (1, 6, 'QSM-39', 9, 12.12);
INSERT INTO LINE_ITEM VALUES (2, 6, 'ASM-45', 6, 78.77);
INSERT INTO LINE_ITEM VALUES (1, 5, 'ESM-48', 3, 43.87);
INSERT INTO LINE_ITEM VALUES (2, 5, 'WSM-98', 7, 5.40);
INSERT INTO LINE_ITEM VALUES (1, 4, 'RSM-57', 2, 78.90);
INSERT INTO LINE_ITEM VALUES (2, 4, 'XSM-78', 9, 2.34);
INSERT INTO LINE_ITEM VALUES (1, 3, 'DSM-59', 3, 5.70);
INSERT INTO LINE_ITEM VALUES (2, 3, 'DSM-53', 3, 98.78);
INSERT INTO LINE_ITEM VALUES (1, 2, 'DSM-37', 4, 7.80);
INSERT INTO LINE_ITEM VALUES (2, 2, 'FSM-12', 2, 55.78);
INSERT INTO LINE_ITEM VALUES (1, 1, 'ESM-48', 8, 87.60);
INSERT INTO LINE_ITEM VALUES (2, 1, 'ESM-23', 1, 55.40);
