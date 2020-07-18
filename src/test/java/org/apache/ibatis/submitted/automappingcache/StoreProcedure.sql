CREATE PROCEDURE get_multi_resultSets()
READS SQL DATA DYNAMIC RESULT SETS 3
BEGIN ATOMIC
    DECLARE result1 CURSOR WITH RETURN FOR
    SELECT id, otherField FROM another_entity FOR READ ONLY;

    DECLARE result2 CURSOR WITH RETURN FOR
        SELECT u.id,name,
            ie.id as "innerEntity.id",
            ie.user_id as "innerEntity.user.id",
            ie.complexCalculated AS "innerEntity.complexCalculated"
        FROM users u inner join inner_entity ie on u.id = ie.user_id
        WHERE name='PatternOne' FOR READ ONLY;

    DECLARE result3 CURSOR WITH RETURN FOR
        SELECT * FROM users
        WHERE name = 'PatternTwo' FOR READ ONLY;

    OPEN result1; OPEN result2; OPEN result3;
END;