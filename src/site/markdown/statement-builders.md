title: MyBatis 3 | The SQL Builder Class
author: Clinton Begin

<h1 class="d-none">Avoid blank site</h1>

## The SQL Builder Class

### The Problem

One of the nastiest things a Java developer will ever have to do is embed SQL in Java code. Usually this is done because the SQL has to be dynamically generated - otherwise you could externalize it in a file or a stored proc. As you've already seen, MyBatis has a powerful answer for dynamic SQL generation in its XML mapping features. However, sometimes it becomes necessary to build a SQL statement string inside of Java code. In that case, MyBatis has one more feature to help you out, before reducing yourself to the typical mess of plus signs, quotes, newlines, formatting problems and nested conditionals to deal with extra commas or AND conjunctions. Indeed, dynamically generating SQL code in Java can be a real nightmare. For example:

```java
String sql = "SELECT P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME, "
"P.LAST_NAME,P.CREATED_ON, P.UPDATED_ON " +
"FROM PERSON P, ACCOUNT A " +
"INNER JOIN DEPARTMENT D on D.ID = P.DEPARTMENT_ID " +
"INNER JOIN COMPANY C on D.COMPANY_ID = C.ID " +
"WHERE (P.ID = A.ID AND P.FIRST_NAME like ?) " +
"OR (P.LAST_NAME like ?) " +
"GROUP BY P.ID " +
"HAVING (P.LAST_NAME like ?) " +
"OR (P.FIRST_NAME like ?) " +
"ORDER BY P.ID, P.FULL_NAME";
```

### The Solution

MyBatis 3 offers a convenient utility class to help with the problem. With the SQL class, you simply create an instance that lets you call methods against it to build a SQL statement one step at a time. The example problem above would look like this when rewritten with the SQL class:

```java
private String selectPersonSql() {
  return new SQL() {{
    SELECT("P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME");
    SELECT("P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON");
    FROM("PERSON P");
    FROM("ACCOUNT A");
    INNER_JOIN("DEPARTMENT D on D.ID = P.DEPARTMENT_ID");
    INNER_JOIN("COMPANY C on D.COMPANY_ID = C.ID");
    WHERE("P.ID = A.ID");
    WHERE("P.FIRST_NAME like ?");
    OR();
    WHERE("P.LAST_NAME like ?");
    GROUP_BY("P.ID");
    HAVING("P.LAST_NAME like ?");
    OR();
    HAVING("P.FIRST_NAME like ?");
    ORDER_BY("P.ID");
    ORDER_BY("P.FULL_NAME");
  }}.toString();
}
```

What is so special about that example? Well, if you look closely, it doesn't have to worry about accidentally duplicating "AND" keywords, or choosing between "WHERE" and "AND" or none at all. The SQL class takes care of understanding where "WHERE" needs to go, where an "AND" should be used and all of the String concatenation.

### The SQL Class

Here are some examples:

```java
// Anonymous inner class
public String deletePersonSql() {
  return new SQL() {{
    DELETE_FROM("PERSON");
    WHERE("ID = #{id}");
  }}.toString();
}

// Builder / Fluent style
public String insertPersonSql() {
  String sql = new SQL()
    .INSERT_INTO("PERSON")
    .VALUES("ID, FIRST_NAME", "#{id}, #{firstName}")
    .VALUES("LAST_NAME", "#{lastName}")
    .toString();
  return sql;
}

// With conditionals (note the final parameters, required for the anonymous inner class to access them)
public String selectPersonLike(final String id, final String firstName, final String lastName) {
  return new SQL() {{
    SELECT("P.ID, P.USERNAME, P.PASSWORD, P.FIRST_NAME, P.LAST_NAME");
    FROM("PERSON P");
    if (id != null) {
      WHERE("P.ID like #{id}");
    }
    if (firstName != null) {
      WHERE("P.FIRST_NAME like #{firstName}");
    }
    if (lastName != null) {
      WHERE("P.LAST_NAME like #{lastName}");
    }
    ORDER_BY("P.LAST_NAME");
  }}.toString();
}

public String deletePersonSql() {
  return new SQL() {{
    DELETE_FROM("PERSON");
    WHERE("ID = #{id}");
  }}.toString();
}

public String insertPersonSql() {
  return new SQL() {{
    INSERT_INTO("PERSON");
    VALUES("ID, FIRST_NAME", "#{id}, #{firstName}");
    VALUES("LAST_NAME", "#{lastName}");
  }}.toString();
}

public String updatePersonSql() {
  return new SQL() {{
    UPDATE("PERSON");
    SET("FIRST_NAME = #{firstName}");
    WHERE("ID = #{id}");
  }}.toString();
}
```

| Method                                                                                                                                                                                                                                                                  | Description                                                                                                                                                                                                                                                                                                          |
|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <ul><li>`SELECT(String)`</li><li>`SELECT(String...)`</li></ul>                                                                                                                                                                                                          | Starts or appends to a `SELECT` clause. Can be called more than once, and parameters will be appended to the `SELECT` clause. The parameters are usually a comma separated list of columns and aliases, but can be anything acceptable to the driver.                                                                |
| <ul><li>`SELECT_DISTINCT(String)`</li><li>`SELECT_DISTINCT(String...)`</li></ul>                                                                                                                                                                                        | Starts or appends to a `SELECT` clause, also adds the `DISTINCT` keyword to the generated query. Can be called more than once, and parameters will be appended to the `SELECT` clause. The parameters are usually a comma separated list of columns and aliases, but can be anything acceptable to the driver.       |
| <ul><li>`FROM(String)`</li><li>`FROM(String...)`</li></ul>                                                                                                                                                                                                              | Starts or appends to a `FROM` clause. Can be called more than once, and parameters will be appended to the `FROM` clause. Parameters are usually a table name and an alias, or anything acceptable to the driver.                                                                                                    |
| <ul><li>`JOIN(String)`</li><li>`JOIN(String...)`</li><li>`INNER_JOIN(String)`</li><li>`INNER_JOIN(String...)`</li><li>`LEFT_OUTER_JOIN(String)`</li><li>`LEFT_OUTER_JOIN(String...)`</li><li>`RIGHT_OUTER_JOIN(String)`</li><li>`RIGHT_OUTER_JOIN(String...)`</li></ul> | Adds a new `JOIN` clause of the appropriate type, depending on the method called. The parameter can include a standard join consisting of the columns and the conditions to join on.                                                                                                                                 |
| <ul><li>`WHERE(String)`</li><li>`WHERE(String...)`</li></ul>                                                                                                                                                                                                            | Appends a new `WHERE` clause condition, concatenated by`AND`. Can be called multiple times, which causes it to concatenate the new conditions each time with`AND`. Use `OR()` to split with an`OR`.                                                                                                                  |
| `OR()`                                                                                                                                                                                                                                                                  | Splits the current `WHERE` clause conditions with an`OR`. Can be called more than once, but calling more than once in a row will generate erratic`SQL`.                                                                                                                                                              |
| `AND()`                                                                                                                                                                                                                                                                 | Splits the current `WHERE` clause conditions with an`AND`. Can be called more than once, but calling more than once in a row will generate erratic`SQL`. Because `WHERE` and `HAVING` both automatically concatenate with `AND`, this is a very uncommon method to use and is only really included for completeness. |
| <ul><li>`GROUP_BY(String)`</li><li>`GROUP_BY(String...)`</li></ul>                                                                                                                                                                                                      | Appends a new `GROUP BY` clause elements, concatenated by a comma. Can be called multiple times, which causes it to concatenate the new conditions each time with a comma.                                                                                                                                           |
| <ul><li>`HAVING(String)`</li><li>`HAVING(String...)`</li></ul>                                                                                                                                                                                                          | Appends a new `HAVING` clause condition, concatenated by AND. Can be called multiple times, which causes it to concatenate the new conditions each time with`AND`. Use `OR()` to split with an`OR`.                                                                                                                  |
| <ul><li>`ORDER_BY(String)`</li><li>`ORDER_BY(String...)`</li></ul>                                                                                                                                                                                                      | Appends a new `ORDER BY` clause elements, concatenated by a comma. Can be called multiple times, which causes it to concatenate the new conditions each time with a comma.                                                                                                                                           |
| <ul><li>`LIMIT(String)`</li><li>`LIMIT(int)`</li></ul>                                                                                                                                                                                                                  | Appends a `LIMIT` clause. This method valid when use together with SELECT(), UPDATE() and DELETE(). And this method is designed to use together with OFFSET() when use SELECT(). (Available since 3.5.2)                                                                                                             |
| <ul><li>`OFFSET(String)`</li><li>`OFFSET(long)`</li></ul>                                                                                                                                                                                                               | Appends a `OFFSET` clause. This method valid when use together with SELECT(). And this method is designed to use together with LIMIT(). (Available since 3.5.2)                                                                                                                                                      |
| <ul><li>`OFFSET_ROWS(String)`</li><li>`OFFSET_ROWS(long)`</li></ul>                                                                                                                                                                                                     | Appends a `OFFSET n ROWS` clause. This method valid when use together with SELECT(). And this method is designed to use together with FETCH_FIRST_ROWS_ONLY(). (Available since 3.5.2)                                                                                                                               |
| <ul><li>`FETCH_FIRST_ROWS_ONLY(String)`</li><li>`FETCH_FIRST_ROWS_ONLY(int)`</li></ul>                                                                                                                                                                                  | Appends a `FETCH FIRST n ROWS ONLY` clause. This method valid when use together with SELECT(). And this method is designed to use together with OFFSET_ROWS(). (Available since 3.5.2)                                                                                                                               |
| `DELETE_FROM(String)`                                                                                                                                                                                                                                                   | Starts a delete statement and specifies the table to delete from. Generally this should be followed by a WHERE statement!                                                                                                                                                                                            |
| `INSERT_INTO(String)`                                                                                                                                                                                                                                                   | Starts an insert statement and specifies the table to insert into. This should be followed by one or more VALUES() or INTO_COLUMNS() and INTO_VALUES() calls.                                                                                                                                                        |
| <ul><li>`SET(String)`</li><li>`SET(String...)`</li></ul>                                                                                                                                                                                                                | Appends to the "set" list for an update statement.                                                                                                                                                                                                                                                                   |
| `UPDATE(String)`                                                                                                                                                                                                                                                        | Starts an update statement and specifies the table to update. This should be followed by one or more SET() calls, and usually a WHERE() call.                                                                                                                                                                        |
| `VALUES(String, String)`                                                                                                                                                                                                                                                | Appends to an insert statement. The first parameter is the column(s) to insert, the second parameter is the value(s).                                                                                                                                                                                                |
| `INTO_COLUMNS(String...)`                                                                                                                                                                                                                                               | Appends columns phrase to an insert statement. This should be call INTO_VALUES() with together.                                                                                                                                                                                                                      |
| `INTO_VALUES(String...)`                                                                                                                                                                                                                                                | Appends values phrase to an insert statement. This should be call INTO_COLUMNS() with together.                                                                                                                                                                                                                      |
| `ADD_ROW()`                                                                                                                                                                                                                                                             | Add new row for bulk insert. (Available since 3.5.2)                                                                                                                                                                                                                                                                 |


<span class="label important">NOTE</span> It is important to note that SQL class writes `LIMIT`, `OFFSET`, `OFFSET n ROWS` and `FETCH FIRST n ROWS ONLY` clauses into the generated statement as is. In other words, the library does not attempt to normalize those values for databases that don’t support these clauses directly. Therefore, it is very important for users to understand whether or not the target database supports these clauses. If the target database does not support these clauses, then it is likely that using this support will create SQL that has runtime errors.

Since version 3.4.2, you can use variable-length arguments as follows:

```java
public String selectPersonSql() {
  return new SQL()
    .SELECT("P.ID", "A.USERNAME", "A.PASSWORD", "P.FULL_NAME", "D.DEPARTMENT_NAME", "C.COMPANY_NAME")
    .FROM("PERSON P", "ACCOUNT A")
    .INNER_JOIN("DEPARTMENT D on D.ID = P.DEPARTMENT_ID", "COMPANY C on D.COMPANY_ID = C.ID")
    .WHERE("P.ID = A.ID", "P.FULL_NAME like #{name}")
    .ORDER_BY("P.ID", "P.FULL_NAME")
    .toString();
}

public String insertPersonSql() {
  return new SQL()
    .INSERT_INTO("PERSON")
    .INTO_COLUMNS("ID", "FULL_NAME")
    .INTO_VALUES("#{id}", "#{fullName}")
    .toString();
}

public String updatePersonSql() {
  return new SQL()
    .UPDATE("PERSON")
    .SET("FULL_NAME = #{fullName}", "DATE_OF_BIRTH = #{dateOfBirth}")
    .WHERE("ID = #{id}")
    .toString();
}
```

Since version 3.5.2, you can create insert statement for bulk insert as follow:

```java
public String insertPersonsSql() {
  // INSERT INTO PERSON (ID, FULL_NAME)
  //     VALUES (#{mainPerson.id}, #{mainPerson.fullName}) , (#{subPerson.id}, #{subPerson.fullName})
  return new SQL()
    .INSERT_INTO("PERSON")
    .INTO_COLUMNS("ID", "FULL_NAME")
    .INTO_VALUES("#{mainPerson.id}", "#{mainPerson.fullName}")
    .ADD_ROW()
    .INTO_VALUES("#{subPerson.id}", "#{subPerson.fullName}")
    .toString();
}
```

Since version 3.5.2, you can create select statement for limiting search result rows clause as follow:

```java
public String selectPersonsWithOffsetLimitSql() {
  // SELECT id, name FROM PERSON
  //     LIMIT #{limit} OFFSET #{offset}
  return new SQL()
    .SELECT("id", "name")
    .FROM("PERSON")
    .LIMIT("#{limit}")
    .OFFSET("#{offset}")
    .toString();
}

public String selectPersonsWithFetchFirstSql() {
  // SELECT id, name FROM PERSON
  //     OFFSET #{offset} ROWS FETCH FIRST #{limit} ROWS ONLY
  return new SQL()
    .SELECT("id", "name")
    .FROM("PERSON")
    .OFFSET_ROWS("#{offset}")
    .FETCH_FIRST_ROWS_ONLY("#{limit}")
    .toString();
}
```

### SqlBuilder and SelectBuilder (DEPRECATED)

Before version 3.2 we took a bit of a different approach, by utilizing a ThreadLocal variable to mask some of the language limitations that make Java DSLs a bit cumbersome. However, this approach is now deprecated, as modern frameworks have warmed people to the idea of using builder-type patterns and anonymous inner classes for such things. Therefore the SelectBuilder and SqlBuilder classes have been deprecated.

The following methods apply to only the deprecated SqlBuilder and SelectBuilder classes.

| Method                | Description                                                                                                                                                                                                                                                                                                                                                |
|-----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `BEGIN()` / `RESET()` | These methods clear the ThreadLocal state of the SelectBuilder class, and prepare it for a new statement to be built. `BEGIN()` reads best when starting a new statement. `RESET()` reads best when clearing a statement in the middle of execution for some reason (perhaps if the logic demands a completely different statement under some conditions). |
| `SQL()`               | This returns the generated `SQL()` and resets the `SelectBuilder` state (as if `BEGIN()` or `RESET()` were called). Thus, this method can only be called ONCE!                                                                                                                                                                                             |


The SelectBuilder and SqlBuilder classes are not magical, but it's important to know how they work. SelectBuilder and SqlBuilder use a combination of Static Imports and a ThreadLocal variable to enable a clean syntax that can be easily interlaced with conditionals. To use them, you statically import the methods from the classes like this (one or the other, not both):

```java
import static org.apache.ibatis.jdbc.SelectBuilder.*;
```

```java
import static org.apache.ibatis.jdbc.SqlBuilder.*;
```

This allows us to create methods like these:

```java
/* DEPRECATED */
public String selectBlogsSql() {
  BEGIN(); // Clears ThreadLocal variable
  SELECT("*");
  FROM("BLOG");
  return SQL();
}
```

```java
/* DEPRECATED */
private String selectPersonSql() {
  BEGIN(); // Clears ThreadLocal variable
  SELECT("P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME");
  SELECT("P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON");
  FROM("PERSON P");
  FROM("ACCOUNT A");
  INNER_JOIN("DEPARTMENT D on D.ID = P.DEPARTMENT_ID");
  INNER_JOIN("COMPANY C on D.COMPANY_ID = C.ID");
  WHERE("P.ID = A.ID");
  WHERE("P.FIRST_NAME like ?");
  OR();
  WHERE("P.LAST_NAME like ?");
  GROUP_BY("P.ID");
  HAVING("P.LAST_NAME like ?");
  OR();
  HAVING("P.FIRST_NAME like ?");
  ORDER_BY("P.ID");
  ORDER_BY("P.FULL_NAME");
  return SQL();
}
```
