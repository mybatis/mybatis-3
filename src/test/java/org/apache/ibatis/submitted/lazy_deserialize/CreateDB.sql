CREATE TABLE table_foo (
    id      INTEGER PRIMARY KEY,
    id_bar  INTEGER NOT NULL);

CREATE TABLE table_bar (
    id      INTEGER PRIMARY KEY);

ALTER TABLE table_foo ADD CONSTRAINT bar_fk
FOREIGN KEY (id_bar) REFERENCES table_bar (id)
ON UPDATE CASCADE ON DELETE RESTRICT;

INSERT INTO table_bar (id) VALUES (10);
INSERT INTO table_foo (id, id_bar) VALUES (1, 10);
