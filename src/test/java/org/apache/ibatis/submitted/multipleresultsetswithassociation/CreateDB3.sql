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

    INSERT INTO order_header(order_id, cust_name)
        VALUES (1, 'Fred');
    INSERT INTO order_header(order_id, cust_name)
        VALUES (2, 'Barney');
    INSERT INTO order_header(order_id, cust_name)
        VALUES (3, 'Homer');        

    INSERT INTO order_detail(order_id, line_number, quantity, item_description)
        VALUES (1, 1, 1, 'Pen');
    INSERT INTO order_detail(order_id, line_number, quantity, item_description)
        VALUES (1, 2, 3, 'Pencil');
    INSERT INTO order_detail(order_id, line_number, quantity, item_description)
        VALUES (1, 3, 2, 'Notepad');
    INSERT INTO order_detail(order_id, line_number, quantity, item_description)
        VALUES (2, 1, 1, 'Compass');
    INSERT INTO order_detail(order_id, line_number, quantity, item_description)
        VALUES (2, 2, 1, 'Protractor');
    INSERT INTO order_detail(order_id, line_number, quantity, item_description)
        VALUES (2, 3, 2, 'Pencil');
