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

drop table legacy_jsr310 if exists;

create table legacy_jsr310 (
  id int,
  d date,
  ts timestamp(9),
  tstz timestamp(9) with time zone
);

insert into legacy_jsr310 (id, d, ts, tstz) values
(1, '2011-12-30', '2019-03-10 02:30:00.123456789', '2019-03-10 02:30:00.123456789+12:34');
