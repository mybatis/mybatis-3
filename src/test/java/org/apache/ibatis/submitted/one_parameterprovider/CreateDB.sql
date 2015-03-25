--
--    Copyright 2014-2014 the original author or authors.
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

create table accountant (
  id int,
  company int,
  firstName varchar(100),
  lastName varchar(100),
  start_dt timestamp,
  end_dt timestamp
)

create table account (
  id int,
  name varchar(100),
  company_id int
);

INSERT INTO accountant (id, company, firstName, lastName, start_dt, end_dt)
VALUES (1, 1, 'John', 'Smith', TIMESTAMP('2010-01-01 00:00:00'), TIMESTAMP('2012-06-01 18:00:00'));

INSERT INTO accountant (id, company, firstName, lastName, start_dt, end_dt)
VALUES (2, 1, 'John', 'Doe', TIMESTAMP('2012-06-01 18:00:00'), TIMESTAMP('2016-01-01 00:00:00'));

INSERT INTO account (id, name, company_id)
VALUES (1, 'Jane''s account', 1);
