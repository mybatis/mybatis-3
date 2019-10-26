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

drop table product_sku if exists;
drop table product_info if exists;
drop table product if exists;

create table product (
  id varchar(32) not null,
  code varchar(80) not null,
  name varchar(240) not null
);

create table product_sku (
  id varchar(32) not null,
  product_id varchar(32) not null,
  code varchar(80) not null,
  color varchar(40),
  size varchar(40)
);

create table product_info (
  id int  not null,
  product_id varchar(32) not null,
  other_info varchar(240)
);

insert into product(id, code, name) values('10000000000000000000000000000001', 'P001', 'Product 001');
insert into product_sku(id ,product_id, code, color, size) values('20000000000000000000000000000001', '10000000000000000000000000000001', 'S001', 'red', '80');
insert into product_sku(id ,product_id, code, color, size) values('20000000000000000000000000000002', '10000000000000000000000000000001', 'S001', 'blue', '10');
insert into product_info(id, product_id, other_info) values(1, '10000000000000000000000000000001', 'Sale 50% Off');
