--       Copyright 2009-2012 the original author or authors.
--
--       Licensed under the Apache License, Version 2.0 (the "License");
--       you may not use this file except in compliance with the License.
--       You may obtain a copy of the License at
--
--          http://www.apache.org/licenses/LICENSE-2.0
--
--       Unless required by applicable law or agreed to in writing, software
--       distributed under the License is distributed on an "AS IS" BASIS,
--       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--       See the License for the specific language governing permissions and
--       limitations under the License.

    DROP PROCEDURE GetOrderDetailsAndHeaders IF EXISTS;
    DROP TABLE order_detail IF EXISTS;
    DROP TABLE order_header IF EXISTS;

    CREATE TABLE order_detail
    (
        order_id integer NOT NULL,
        line_number integer NOT NULL,
        quantity integer NOT NULL,
        item_description varchar(50) NOT NULL,
        PRIMARY KEY (order_id, line_number)
    );

    CREATE TABLE order_header
    (
        order_id integer NOT NULL,
        cust_name varchar(50) NOT NULL,
        PRIMARY KEY (order_id)
    );
