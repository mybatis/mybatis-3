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

create schema ibtest authorization dba;

create table ibtest.names (
id int,
description varchar(20)
);

insert into ibtest.names (id, description) values(1, 'Fred');
insert into ibtest.names (id, description) values(2, 'Wilma');
insert into ibtest.names (id, description) values(3, 'Pebbles');
insert into ibtest.names (id, description) values(4, 'Barney');
insert into ibtest.names (id, description) values(5, 'Betty');
insert into ibtest.names (id, description) values(6, 'Bamm Bamm');
insert into ibtest.names (id, description) values(7, 'Rock ''n Roll');

create table ibtest.numerics (
  id int,
  tinynumber tinyint,
  smallnumber smallint,
  longinteger bigint,
  biginteger bigint,
  numericnumber numeric(10,2),
  decimalnumber decimal(10,2),
  realnumber real,
  floatnumber float,
  doublenumber double
);

insert into ibtest.numerics values(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);