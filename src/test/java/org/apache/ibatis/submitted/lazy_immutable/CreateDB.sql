CREATE TABLE immutables (
    id          INTEGER PRIMARY KEY,
    description VARCHAR (30) NOT NULL
);

INSERT INTO immutables (id, description) VALUES (1, 'Description of immutable');
INSERT INTO immutables (id, description) VALUES (2, 'Another immutable');