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


-- ORACLE DATABASE

-- Dropping Tables

DROP TABLE ACCOUNT2;

-- Creating Tables

CREATE TABLE ACCOUNT2 (
    ACC_ID             NUMBER(5,0) NOT NULL,
    ACC_FIRST_NAME     VARCHAR2 (255) NOT NULL,
    ACC_LAST_NAME      VARCHAR2 (255) NOT NULL,
    ACC_EMAIL          VARCHAR2 (255),
    PRIMARY KEY (ACC_ID)
);

-- Creating Test Data

INSERT INTO ACCOUNT2 VALUES(1,'Clinton', 'Begin', 'clinton.begin@ibatis.com');
INSERT INTO ACCOUNT2 VALUES(2,'Jim', 'Smith', 'jim.smith@somewhere.com');
INSERT INTO ACCOUNT2 VALUES(3,'Bob', 'Jackson', 'bob.jackson@somewhere.com');

-- Create Email Swap Procedure
CREATE or REPLACE
PROCEDURE swap_email_address
  (p_first IN OUT VARCHAR2, p_second IN OUT VARCHAR2, p_status OUT VARCHAR2)
IS
  v_id1 NUMBER (5,0);
  v_id2 NUMBER (5,0);
  v_email1 VARCHAR2 (255);
  v_email2 VARCHAR2 (255);
BEGIN

  SELECT ACC_ID, ACC_EMAIL
  INTO v_id1, v_email1
  from ACCOUNT2
  where ACC_EMAIL = p_first;

  SELECT ACC_ID, ACC_EMAIL
  INTO v_id2, v_email2
  from ACCOUNT2
  where ACC_EMAIL = p_second;

  UPDATE ACCOUNT2
  set ACC_EMAIL = v_email2
  where ACC_ID = v_id1;

  UPDATE ACCOUNT2
  set ACC_EMAIL = v_email1
  where ACC_ID = v_id2;

  SELECT ACC_EMAIL
  INTO p_first
  from ACCOUNT2
  where ACC_ID = v_id1;

  SELECT ACC_EMAIL
  INTO p_second
  from ACCOUNT2
  where ACC_ID = v_id2;

  SELECT 'success'
  INTO p_status
  from DUAL;

END;
/

-- Create No Param Procedure
CREATE or REPLACE
PROCEDURE no_param_proc
IS
  v_status VARCHAR2 (255);
BEGIN

  SELECT 'success'
  INTO v_status
  from DUAL;

END;

/


-- Call procedure

DECLARE
  p1 VARCHAR2(255) := 'jim.smith@somewhere.com';
  p2 VARCHAR2(255) := 'bob.jackson@somewhere.com';
  p3 VARCHAR2(255) := 'failure';
BEGIN
  swap_email_address (p1, p2, p3);
  no_param_proc;
  COMMIT;
  DBMS_OUTPUT.PUT_LINE (p1 || ' <-> ' || p2 || ' (' || p3 || ')');
END;
/

