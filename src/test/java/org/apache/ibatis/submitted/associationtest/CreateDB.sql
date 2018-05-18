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

drop table cars if exists;

create table cars (
  carid integer,
  cartype varchar(20),
  enginetype varchar(20),
  enginecylinders integer,
  brakestype varchar(20)
);

insert into cars (carid, cartype, enginetype, enginecylinders, brakestype) values(1, 'VW',   'Diesel', 4,    null);
insert into cars (carid, cartype, enginetype, enginecylinders, brakestype) values(2, 'Opel',    null,    null, 'drum');
insert into cars (carid, cartype, enginetype, enginecylinders, brakestype) values(3, 'Audi', 'Diesel', 4,    'disk');
insert into cars (carid, cartype, enginetype, enginecylinders, brakestype) values(4, 'Ford', 'Gas',    8,    'drum');
