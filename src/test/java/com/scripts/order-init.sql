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

DROP TABLE ORDERS;

-- Creating Tables

CREATE TABLE ORDERS (
    ORD_ID               INT NOT NULL,
    ORD_ACC_ID           INT NOT NULL,
    ORD_DATE             TIMESTAMP NOT NULL,
    ORD_CARD_TYPE        VARCHAR(255),
    ORD_CARD_NUMBER      VARCHAR(255),
    ORD_CARD_EXPIRY      VARCHAR(255),
    ORD_STREET           VARCHAR(255),
    ORD_CITY             VARCHAR(255),
    ORD_PROVINCE         VARCHAR(255),
    ORD_POSTAL_CODE      VARCHAR(255),
    ORD_FAVOURITE_LINE   INT,
    PRIMARY KEY (ORD_ID)
);

-- Creating Test Data

INSERT INTO ORDERS VALUES (1, 1, '2003-02-15 08:15:00', 'VISA', '999999999999', '05/03', '11 This Street', 'Victoria', 'BC', 'C4B 4F4',2);
INSERT INTO ORDERS VALUES (2, 4, '2003-02-15 08:15:00', 'MC', '888888888888', '06/03', '222 That Street', 'Edmonton', 'AB', 'X4K 5Y4',1);
INSERT INTO ORDERS VALUES (3, 3, '2003-02-15 08:15:00', 'AMEX', '777777777777', '07/03', '333 Other Street', 'Regina', 'SK', 'Z4U 6Y4',2);
INSERT INTO ORDERS VALUES (4, 2, '2003-02-15 08:15:00', 'MC', '666666666666', '08/03', '444 His Street', 'Toronto', 'ON', 'K4U 3S4',1);
INSERT INTO ORDERS VALUES (5, 5, '2003-02-15 08:15:00', 'VISA', '555555555555', '09/03', '555 Her Street', 'Calgary', 'AB', 'J4J 7S4',2);
INSERT INTO ORDERS VALUES (6, 5, '2003-02-15 08:15:00', 'VISA', '999999999999', '10/03', '6 Their Street', 'Victoria', 'BC', 'T4H 9G4',1);
INSERT INTO ORDERS VALUES (7, 4, '2003-02-15 08:15:00', 'MC', '888888888888', '11/03', '77 Lucky Street', 'Edmonton', 'AB', 'R4A 0Z4',2);
INSERT INTO ORDERS VALUES (8, 3, '2003-02-15 08:15:00', 'AMEX', '777777777777', '12/03', '888 Our Street', 'Regina', 'SK', 'S4S 7G4',1);
INSERT INTO ORDERS VALUES (9, 2, '2003-02-15 08:15:00', 'MC', '666666666666', '01/04', '999 Your Street', 'Toronto', 'ON', 'G4D 9F4',2);
INSERT INTO ORDERS VALUES (10, 1, '2003-02-15 08:15:00', 'VISA', '555555555555', '02/04', '99 Some Street', 'Calgary', 'AB', 'W4G 7A4',1);
