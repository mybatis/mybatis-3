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

CREATE SCHEMA mbtest;

CREATE TABLE mbtest.order_detail
(
  order_id integer NOT NULL,
  line_number integer NOT NULL,
  quantity integer NOT NULL,
  item_description character varying(50) NOT NULL,
  CONSTRAINT order_detail_pkey PRIMARY KEY (order_id, line_number)
)
WITH (
  OIDS=FALSE
);

ALTER TABLE mbtest.order_detail OWNER TO postgres;

CREATE TABLE mbtest.order_header
(
  order_id integer NOT NULL,
  cust_name character varying(50) NOT NULL,
  CONSTRAINT order_header_pkey PRIMARY KEY (order_id)
)
WITH (
  OIDS=FALSE
);

ALTER TABLE mbtest.order_header OWNER TO postgres;

INSERT INTO mbtest.order_header(order_id, cust_name)
  VALUES (1, 'Fred');
INSERT INTO mbtest.order_header(order_id, cust_name)
  VALUES (2, 'Barney');
INSERT INTO mbtest.order_detail(order_id, line_number, quantity, item_description)
  VALUES (1, 1, 1, 'Pen');
INSERT INTO mbtest.order_detail(order_id, line_number, quantity, item_description)
  VALUES (1, 2, 3, 'Pencil');
INSERT INTO mbtest.order_detail(order_id, line_number, quantity, item_description)
  VALUES (1, 3, 2, 'Notepad');
INSERT INTO mbtest.order_detail(order_id, line_number, quantity, item_description)
  VALUES (2, 1, 1, 'Compass');
INSERT INTO mbtest.order_detail(order_id, line_number, quantity, item_description)
  VALUES (2, 2, 1, 'Protractor');
INSERT INTO mbtest.order_detail(order_id, line_number, quantity, item_description)
  VALUES (2, 3, 2, 'Pencil');

-- @DELIMITER |

CREATE OR REPLACE FUNCTION mbtest.get_order(order_number integer)
  RETURNS refcursor AS
$BODY$
DECLARE
  mycurs refcursor;
BEGIN
  open mycurs for select a.*, b.*
  from mbtest.order_header a join mbtest.order_detail b
    on a.order_id = b.order_id
  where a.order_id = ORDER_NUMBER;
  return mycurs;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100|

-- @DELIMITER ;

ALTER FUNCTION mbtest.get_order(integer) OWNER TO postgres;

