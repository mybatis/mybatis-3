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

drop table ParentChild if exists;

create table ParentChild (
  parent_id int,
  child_id int
);

insert into ParentChild (parent_id, child_id) values(1, 1);
insert into ParentChild (parent_id, child_id) values(1, 2);
insert into ParentChild (parent_id, child_id) values(2, 3);
