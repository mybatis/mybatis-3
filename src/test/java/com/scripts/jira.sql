DROP TABLE ARTICLE_INDEX;

CREATE TABLE ARTICLE_INDEX (
   CATEGORY_ID           INTEGER                          NOT NULL,
   CATEGORY_TITLE        VARCHAR(32),
   TOPIC_TITLE           VARCHAR(32),
   TOPIC_DESCRIPTION     VARCHAR(80),
   PRIMARY KEY (CATEGORY_ID)
);

INSERT INTO ARTICLE_INDEX VALUES (1, 'Health', 'Heart', 'Exercises for your heart');
INSERT INTO ARTICLE_INDEX VALUES (2, 'Health', 'Health', 'General health for people');
INSERT INTO ARTICLE_INDEX VALUES (3, 'Health', 'Love', 'How love affects your health1');
INSERT INTO ARTICLE_INDEX VALUES (4, 'Love', 'Heart', 'Language of the heart');
INSERT INTO ARTICLE_INDEX VALUES (5, 'Love', 'Health', 'How love affects your health2');
INSERT INTO ARTICLE_INDEX VALUES (6, 'Combat', 'Health', 'Staying alive in combat');
INSERT INTO ARTICLE_INDEX VALUES (7, 'Love', 'Love', 'Love is all around');
INSERT INTO ARTICLE_INDEX VALUES (8, 'Health', 'Heart', 'More Exercises for your heart');
