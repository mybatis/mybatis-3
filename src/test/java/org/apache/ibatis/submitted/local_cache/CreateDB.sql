--
--    Copyright 2009-2019 the original author or authors.
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

drop table order_lines if exists;
drop table orders if exists;

create table orders (
  id int,
  date_time timestamp
);

create table order_lines (
  id int,
  name varchar(256),
  unit_price int,
  quantity int,
  order_id int
);

insert into orders (id, date_time) values (1, current_timestamp);
insert into order_lines (id, name, unit_price, quantity, order_id) values (1, 'Book', 500, 2, 1);
insert into order_lines (id, name, unit_price, quantity, order_id) values (2, 'Drink', 100, 10, 1);

insert into orders (id, date_time) values (2, current_timestamp);
insert into order_lines (id, name, unit_price, quantity, order_id) values (3, 'Food', 5000, 2, 2);
insert into order_lines (id, name, unit_price, quantity, order_id) values (4, 'Blu-Lay Disc', 10000, 1, 2);
