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

drop table string_string if exists;

create table string_string (
    id identity,
    key varchar(255),
    value varchar(255)
);

drop table int_bool if exists;

create table int_bool (
    id identity,
    key integer,
    value boolean
);

drop table nested_bean if exists;

create table nested_bean (
    id identity,
    keya integer,
    keyb boolean,
    valuea integer,
    valueb boolean
);

drop table key_cols if exists;

create table key_cols (
    id identity,
    col_a integer,
    col_b integer
);

insert into key_cols (id, col_a, col_b) values (1, 11, 222);
insert into key_cols (id, col_a, col_b) values (2, 22, 222);
insert into key_cols (id, col_a, col_b) values (3, 22, 333);
