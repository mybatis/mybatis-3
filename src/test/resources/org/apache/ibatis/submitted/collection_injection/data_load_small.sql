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

insert into house (id, name) values ( 1, 'MyBatis Headquarters' );

insert into room (id, name, house_id, size_m2, wall_type, wall_height) VALUES ( 1, 'Kitchen', 1, 25, 'Brick', 20 );
insert into room (id, name, house_id, size_m2, wall_type, wall_height) VALUES ( 2, 'Dining room', 1, 100, 'Wood', 10 );
insert into room (id, name, house_id, size_m2, wall_type, wall_height) VALUES ( 3, 'Programming room', 1, 200, 'Steel', 15 );

insert into furniture (id, description, room_id) VALUES ( 1, 'Coffee machine', 1);
insert into furniture (id, description, room_id) VALUES ( 2, 'Fridge', 1);
insert into furniture (id, description, room_id) VALUES ( 3, 'Table', 2);
insert into furniture (id, description, room_id) VALUES ( 4, 'Big screen', 3);
insert into furniture (id, description, room_id) VALUES ( 5, 'Laptop', 3);

insert into defect (id, defect, furniture_id) VALUES ( 1, 'Does not work', 1 );
insert into defect (id, defect, furniture_id) VALUES ( 2, 'Cannot run intellij', 5 );
