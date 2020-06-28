--
--    Copyright 2009-2020 the original author or authors.
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

-- ----------------------------
-- Table structure for  role 
-- ----------------------------
CREATE TABLE role (
id int,
name varchar(30)
);

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO role (id,name)
VALUES ('1', 'teacher');
INSERT INTO role (id,name)
VALUES ('2', 'student');
INSERT INTO role (id,name)
VALUES ('3', 'Headmaster');
INSERT INTO role (id,name)
VALUES ('4', 'Learning-commissary');

CREATE TABLE user (
id int,
username varchar(32),
friend_id int
);

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO user (id,username)
VALUES ('1', 'James Gosling');
INSERT INTO user (id,username)
VALUES ('2', 'Doug Lea');
INSERT INTO user (id,username)
VALUES ('3', 'Rod johnson');
INSERT INTO user (id,username, friend_id)
VALUES ('4', 'Juergen Hoeller', 1);

-- ----------------------------
-- Table structure for `user_role`
-- ----------------------------
CREATE TABLE user_role (
  id int,
  role_id int,
  user_id int
);

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO user_role (id,role_id,user_id)
VALUES ('1', '2', '4');
INSERT INTO user_role (id,role_id,user_id)
VALUES ('2', '3', '1');
INSERT INTO user_role (id,role_id,user_id)
VALUES ('3', '1', '2');
INSERT INTO user_role (id,role_id,user_id)
VALUES ('4', '2', '3');
INSERT INTO user_role (id,role_id,user_id)
VALUES ('5', '4', '4');
INSERT INTO user_role (id,role_id,user_id)
VALUES ('6', '1', '1');
