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

DROP TABLE blog;
DROP TABLE post;
DROP TABLE comment;

CREATE TABLE blog (
id          INT,
title       VARCHAR(255)
);

CREATE TABLE post (
id          INT,
blog_id     INT,
body        VARCHAR(255)
);

CREATE TABLE comment (
id          INT,
post_id     INT,
comment     VARCHAR(255)
);

-- Data load
INSERT INTO blog (id,title) VALUES (1,'Blog with posts');
INSERT INTO blog (id,title) VALUES (2,'Blog without posts');

INSERT INTO post (id,blog_id,body) VALUES (1,1,'I think if I never smelled another corn nut it would be too soon...');
INSERT INTO post (id,blog_id,body) VALUES (2,1,'That''s not a dog.  THAT''s a dog!');

INSERT INTO comment (id,post_id,comment) VALUES (1,1,'I disagree and think...');
INSERT INTO comment (id,post_id,comment) VALUES (2,1,'I agree and think troll is an...');
INSERT INTO comment (id,post_id,comment) VALUES (3,2,'I don not agree and still think troll is an...');