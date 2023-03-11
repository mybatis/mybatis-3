--
--    Copyright 2009-2023 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       https://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

drop table users if exists;

create table users (
  id int,
  name varchar(20),
  friend_id int
);

create sequence IF NOT EXISTS cursor_cache_oom START WITH 1 INCREMENT BY 1;

insert into users (id, name, friend_id)
SELECT nr, 'test' + nr, mod((nr + 1), 10000) + 1
from unnest(sequence_array(1, 10000, 1)) as i(nr);
