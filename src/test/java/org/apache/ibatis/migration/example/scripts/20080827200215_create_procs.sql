--// First migration.
-- Migration SQL that makes the change goes here.

CREATE PROCEDURE selectTwoSetsOfAuthors(DP1 INTEGER, DP2 INTEGER)
PARAMETER STYLE JAVA
LANGUAGE JAVA
READS SQL DATA
DYNAMIC RESULT SETS 2
EXTERNAL NAME 'databases.blog.StoredProcedures.selectTwoSetsOfTwoAuthors';

CREATE PROCEDURE insertAuthor(DP1 INTEGER, DP2 VARCHAR(255), DP3 VARCHAR(255), DP4 VARCHAR(255))
PARAMETER STYLE JAVA
LANGUAGE JAVA
EXTERNAL NAME 'databases.blog.StoredProcedures.insertAuthor';

CREATE PROCEDURE selectAuthorViaOutParams(ID INTEGER, OUT USERNAME VARCHAR(255), OUT PASSWORD VARCHAR(255), OUT EMAIL VARCHAR(255), OUT BIO VARCHAR(255))
PARAMETER STYLE JAVA
LANGUAGE JAVA
EXTERNAL NAME 'databases.blog.StoredProcedures.selectAuthorViaOutParams';

--//@UNDO
-- SQL to undo the change goes here.

DROP PROCEDURE selectTwoSetsOfAuthors;
DROP PROCEDURE insertAuthor;
DROP PROCEDURE selectAuthorViaOutParams;

