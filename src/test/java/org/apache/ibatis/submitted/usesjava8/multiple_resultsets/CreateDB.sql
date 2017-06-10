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

