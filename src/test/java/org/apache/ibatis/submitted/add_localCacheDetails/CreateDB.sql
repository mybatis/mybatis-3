-- ----------------------------
-- Table structure for  role 
-- ----------------------------
CREATE TABLE role (
id int,
role_name varchar(10)
);

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO role (id,role_name)
VALUES ('1', 'teacher');
INSERT INTO role (id,role_name)
VALUES ('2', 'student');
INSERT INTO role (id,role_name)
VALUES ('3', 'Headmaster');
INSERT INTO role (id,role_name)
VALUES ('4', 'Learning-commissary');

CREATE TABLE user (
id int,
username varchar(32),
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
INSERT INTO user (id,username)
VALUES ('4', 'Juergen Hoeller');

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
