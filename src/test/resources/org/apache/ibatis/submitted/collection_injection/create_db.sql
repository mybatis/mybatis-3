--
--    Copyright 2009-2024 the original author or authors.
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

drop table defect if exists;
drop table furniture if exists;
drop table room if exists;
drop table house if exists;

create table house (
    id int not null primary key,
    name varchar(255)
);

create table room (
    id int not null primary key,
    name varchar(255),
    house_id int,
    size_m2 int,
    wall_type varchar(10),
    wall_height int
);

alter table room add constraint fk_room_house_id
    foreign key (house_id) references house (id);

create table furniture (
    id int not null primary key,
    description varchar(255),
    room_id int
);

alter table furniture add constraint fk_furniture_room_id
    foreign key (room_id) references room (id);

create table defect (
    id int not null primary key,
    defect varchar(255),
    furniture_id int
);

alter table defect add constraint fk_defects_furniture_id
    foreign key (furniture_id) references furniture (id);
