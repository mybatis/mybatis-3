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

DROP TABLE parent if exists;
DROP TABLE child if exists;
create table parent(
    id integer, 
    value varchar(20)
);

create table child(
    id integer, 
    value varchar(20)
);

create table parent_child(
    idparent integer, 
    idchild_from integer, 
    idchild_to integer
);

insert into parent (id, value) values (1, 'parent1');
insert into parent (id, value) values (2, 'parent2');

insert into child (id, value) values (1, 'child1');
insert into child (id, value) values (2, 'child2');
insert into child (id, value) values (3, 'child3');
insert into child (id, value) values (4, 'child4');

insert into parent_child (idparent, idchild_from, idchild_to) values (1, 1, 2);
insert into parent_child (idparent, idchild_from, idchild_to) values (2, 2, 3);
insert into parent_child (idparent, idchild_from, idchild_to) values (2, 1, 2);