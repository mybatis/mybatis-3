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

DROP TABLE PERSON;

-- Creating Tables

create table person (
  id int not null, 
  first_name char(50) not null,
  last_name char(50) not null,
  primary key (id)
);


-- Creating Test Data

insert into person values(1, 'Jeff', 'Jones');
insert into person values(2, 'Matt', 'Jones');
insert into person values(3, 'Amy', 'Jones');
insert into person values(4, 'Scott', 'Jones');
insert into person values(5, 'Wilma', 'Jones');
insert into person values(6, 'Fred', 'Jones');
insert into person values(7, 'Jeff', 'Smith');
insert into person values(8, 'Matt', 'Smith');
insert into person values(9, 'Amy', 'Smith');
