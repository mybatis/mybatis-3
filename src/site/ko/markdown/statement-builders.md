title: 마이바티스 3 | SQL Builder 클래스
author: Clinton Begin, 이동국(한국어 번역)

<!--

       Copyright 2009-2026 the original author or authors.

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

          https://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.

-->

# SQL Builder 클래스

## 문제점

자바코드에서 SQL을 작성하는 작업은 자바 개발자를 가장 힘들게 하는 것중 하나이다. 대개는 동적 SQL을 작성해야 하지만 종종 파일이나 저장프로시저에 작성해야 할수도 있다. 이미 본것처럼 마이바티스는 XML매핑 기능으로 동적 SQL을 처리하는 강력한 기능을 제공한다. 하지만 종종 자바 코드를 사용해서 SQL구문을 만들어야 할수도 있다. 마이바티스는 이런 경우를 위한 기능도 제공한다. 더하기 기호나 따옴표, 개행처리, 포매팅 그리고 콤마나 문자열을 합치는 등의 다양한 조건을 처리하지 않아도 되도록 해준다. 정말 자바코드에서 SQL을 동적으로 만드는 것은 악몽과 같다. 예를들어보자.

```java
String sql = "SELECT P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME, " +
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

## 해결방법

마이바티스 3은 이런 문제를 해결하기 위해 편리한 클래스를 제공한다. SQL클래스를 사용해서 인스턴스를 만들고 SQL구문을 만드는 메소드를 호출하자. 위 문제가 되는 예제를 SQL클래스를 사용해서 재작성하면 다음과 같다.

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

이 예제가 특별한게 뭘까? 자 조금더 자세히 보자. \"AND\" 키워드가 중복으로 나오는 것이나 \"WHERE\"과 \"AND\"를 선택하는데 걱정할 필요가 없다. SQL클래스는 \"WHERE\"가 필요한 위치를 이해하고 처리한다. 그리고 \"AND\"를 사용하고 문자열을 합쳐야 하는 부분에 대해서도 알고 처리한다.

## SQL 클래스

여기에 몇가지 예제가 있다.

```java
// 익명 내부 클래스
public String deletePersonSql() {
  return new SQL() {{
    DELETE_FROM("PERSON");
    WHERE("ID = #{id}");
  }}.toString();
}

// 빌더 / 깔끔한 형태
public String insertPersonSql() {
  String sql = new SQL()
    .INSERT_INTO("PERSON")
    .VALUES("ID, FIRST_NAME", "#{id}, #{firstName}")
    .VALUES("LAST_NAME", "#{lastName}")
    .toString();
  return sql;
}

// 분기처리를 포함한 형태 (final 파라미터, 파라미터에 접근하는 익명 내부 클래스가 필요함)
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

public String updatePersonSql() {
  return new SQL() {{
    UPDATE("PERSON");
    SET("FIRST_NAME = #{firstName}");
    WHERE("ID = #{id}");
  }}.toString();
}
```

| 메소드 | 설명 |
| --- | --- |
| <ul><li>`SELECT(String)`</li> <li>`SELECT(String...)`</li></ul> | `SELECT`절로 시작하거나 덧붙이기. 한번 이상 호출할 수 있고 파라미터는 `SELECT`절에 덧붙일것이다. 파라미터는 칼럼과 별칭의 목록을 콤마를 구분자로 나열하지만 드라이버에 따라 처리가 안될수도 있다. |
| <ul><li>`SELECT_DISTINCT(String)`</li> <li>`SELECT_DISTINCT(String...)`</li></ul> | `SELECT`절로 시작하거나 덧붙이기. 생성된 쿼리에 `DISTINCT`를 추가한다. 한번 이상 호출할 수 있고 파라미터는 `SELECT`절에 덧붙일것이다. 파라미터는 칼럼과 별칭의 목록을 콤마를 구분자로 나열하지만 드라이버에 따라 처리가 안될수도 있다. |
| <ul><li>`FROM(String)`</li> <li>`FROM(String...)`</li></ul> | `FROM`절로 시작하거나 덧붙이기. 한번 이상 호출할 수 있고 파라미터는 `FROM`절에 덧붙일것이다. 파라미터는 테이블명과 별칭이지만 드라이버에 따라 처리가 안될수도 있다. |
| <ul><li>`JOIN(String)`</li> <li>`JOIN(String...)`</li> <li>`INNER_JOIN(String)`</li> <li>`INNER_JOIN(String...)`</li> <li>`LEFT_OUTER_JOIN(String)`</li> <li>`LEFT_OUTER_JOIN(String...)`</li> <li>`RIGHT_OUTER_JOIN(String)`</li> <li>`RIGHT_OUTER_JOIN(String...)`</li> <li>`OUTER_JOIN(String)`</li> <li>`OUTER_JOIN(String...)`</li></ul> | 새로운 적절한 타입의 `JOIN`절을 추가. 타입은 호출하는 메소드에 따라 다르다. 파라미터는 조인을 구성하는 칼럼과 조건이다. |
| <ul><li>`WHERE(String)`</li> <li>`WHERE(String...)`</li></ul> | 새로운 `WHERE`절과 `AND`로 합친 조건을 덧붙인다. 여러번 호출할 수 있고 `AND`를 사용할때마다 새로운 조건을 합친다. `OR`로 분리하려면 `OR()`를 사용하자. |
| `OR()` | `OR`를 사용해서 마지막으로 사용한 `WHERE` 또는 `HAVING` 조건절을 분리한다. 여러번 호출할 수 있지만 연속해서 여러번 호출할 경우 잘못된 `SQL`을 만들수도 있다. |
| `AND()` | `AND`를 사용해서 마지막으로 사용한 `WHERE` 또는 `HAVING` 조건절을 분리한다. 여러번 호출할 수 있지만 연속해서 여러번 호출할 경우 잘못된 `SQL`을 만들수도 있다. `WHERE`와 `HAVING` 두가지는 `AND`를 자동으로 붙이기 때문에 흔하게 사용하지 않고 필요할때만 사용한다. |
| <ul><li>`GROUP_BY(String)`</li> <li>`GROUP_BY(String...)`</li></ul> | 새로운 `GROUP BY`절을 콤마를 더해서 덧붙인다. 여러번 호출할 수 있고 개별 조건을 콤마를 붙여서 합칠 수 있다. |
| <ul><li>`HAVING(String)`</li> <li>`HAVING(String...)`</li></ul> | 새로운 `HAVING`절을 AND를 더해서 덧붙인다. 여러번 호출할 수 있고 개별 조건을 `AND`를 붙여서 합칠 수 있다. `OR`로 분리하기 위해서는 `OR`를 사용하자. |
| <ul><li>`ORDER_BY(String)`</li> <li>`ORDER_BY(String...)`</li></ul> | 새로운 `ORDER BY`절을 콤마를 더해서 덧붙인다. 여러번 호출할 수 있고 개별조건을 콤마를 붙여서 합칠수 있다. |
| <ul><li>`LIMIT(String)`</li> <li>`LIMIT(int)`</li></ul> | Appends a `LIMIT` clause. This method valid when use together with SELECT(), UPDATE() and DELETE(). And this method is designed to use together with OFFSET() when use SELECT(). (Available since 3.5.2) |
| <ul><li>`OFFSET(String)`</li> <li>`OFFSET(long)`</li></ul> | Appends a `OFFSET` clause. This method valid when use together with SELECT(). And this method is designed to use together with LIMIT(). (Available since 3.5.2) |
| <ul><li>`OFFSET_ROWS(String)`</li> <li>`OFFSET_ROWS(long)`</li></ul> | Appends a `OFFSET n ROWS` clause. This method valid when use together with SELECT(). And this method is designed to use together with FETCH_FIRST_ROWS_ONLY(). (Available since 3.5.2) |
| <ul><li>`FETCH_FIRST_ROWS_ONLY(String)`</li> <li>`FETCH_FIRST_ROWS_ONLY(int)`</li></ul> | Appends a `FETCH FIRST n ROWS ONLY` clause. This method valid when use together with SELECT(). And this method is designed to use together with OFFSET_ROWS(). (Available since 3.5.2) |
| `DELETE_FROM(String)` | delete구문을 시작하고 삭제할 테이블을 명시한다. 대개는 뒤에 WHERE 구문을 붙여서 삭제할 대상 조건을 명시한다. |
| `INSERT_INTO(String)` | insert구문을 시작하고 입력할 테이블을 명시한다. VALUES() or INTO_COLUMNS() and INTO_VALUES() 메소드 호출은 여러번 해서 입력한 칼럼과 값을 명시한다. |
| <ul><li>`SET(String)`</li> <li>`SET(String...)`</li></ul> | update구문에서 \"set\" 대상 목록을 덧붙인다. |
| `UPDATE(String)` | update구문을 시작하고 수정할 테이블을 명시한다. 수정할 칼럼과 값을 명시하기 위해 SET()메소드 호출을 여러번 할수 있고 대개 수정할 대상 데이터를 한정하기 위해 WHERE()메소드도 호출한다. |
| `VALUES(String, String)` | insert구문에 덧붙인다. 첫번째 파라미터는 입력할 칼럼이고 두번째 파라미터는 입력할 값이다. |
| `INTO_COLUMNS(String...)` | insert 구문에 columns 절을 추가한다. 반드시 INTO_VALUES()와 함께 호출되어야 한다. |
| `INTO_VALUES(String...)` | insert 구문에 values 절을 추가한다. 반드시 INTO_COLUMNS()와 함께 호출되어야 한다. |
| `ADD_ROW()` | Add new row for bulk insert. (Available since 3.5.2) |

<span class="label important">NOTE</span> It is important to note that SQL class writes `LIMIT`, `OFFSET`, `OFFSET n ROWS` and `FETCH FIRST n ROWS ONLY` clauses into the generated statement as is. In other words, the library does not attempt to normalize those values for databases that don’t support these clauses directly. Therefore, it is very important for users to understand whether or not the target database supports these clauses. If the target database does not support these clauses, then it is likely that using this support will create SQL that has runtime errors.

3.4.2 버전부터, variable-length 매개변수를 아래와 같이 사용할 수 있습니다.

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

3.5.15 버전부터 `applyIf(boolean, Consumer<SQL>)`와 `applyIf(BooleanSupplier, Consumer<SQL>)`로 조건이 참일 때만 SQL 절을 추가할 수 있습니다. `applyForEach(Iterable<E>, ForEachConsumer<SQL, E>)`는 각 요소마다 콜백을 호출하며 SQL 빌더, 요소, 0부터 시작하는 인덱스를 전달합니다. 이 메서드들은 빌더를 반환하므로 호출을 이어서 작성할 수 있습니다.

```java
public String selectPersonsSql(boolean activeOnly) {
  return new SQL()
    .SELECT("ID", "NAME")
    .FROM("PERSON")
    .applyIf(activeOnly, sql -> sql.WHERE("ACTIVE = 1"))
    .toString();
}

// Mapper interface
@InsertProvider(type = PersonSqlProvider.class, method = "insertPersonsSql")
void insertPersons(@Param("persons") List<Person> persons);

// PersonSqlProvider
public String insertPersonsSql(@Param("persons") List<Person> persons) {
  return new SQL()
    .INSERT_INTO("PERSON")
    .INTO_COLUMNS("ID", "NAME")
    .applyForEach(persons, (sql, person, index) ->
      sql.INTO_VALUES("#{persons[" + index + "].id}", "#{persons[" + index + "].name}").ADD_ROW())
    .toString();
}
```

## SqlBuilder 와 SelectBuilder (향후 제거예정)

3.2버전 이전에는 다른 방법을 사용했다. 자바 DSL을 다루기 힘든 언어의 제한점을 가리기 위해 ThreadLocal변수를 사용했다. 하지만 이 방법은 이제 사용하지 않길 바란다. 최근에는 빌더 타입의 패턴과 익명 내부 클래스를 사용하는 추세이다. 그러므로 SelectBuilder 와 SqlBuilder는 향후 제거할 예정이다.

다음에 나열된 메소드는 SqlBuilder 와 SelectBuilder클래스에서만 사용할 수 있다.

| 메소드 | 설명 |
| --- | --- |
| `BEGIN()` / `RESET()` | 이 메서드들은 가져온 SelectBuilder 또는 SqlBuilder 클래스의 ThreadLocal 상태를 초기화하고 새로운 구문을 만들 준비를 한다. 두 클래스는 각각 별도의 상태를 유지한다. `BEGIN()`은 새로운 구문을 시작할때 사용하는게 좋다. `RESET()`은 처리중인 구문의 상태를 몇가지 사유로 지워야 할때 사용하는게 좋다. (아마도 몇가지 사유로 완전히 다른 구문을 실행해야 할때 사용할것이다.) |
| `SQL()` | 생성된 SQL을 반환하고 가져온 빌더 클래스의 상태를 초기화한다. 구문마다 한 번 호출하며, 다시 호출하기 전에 다음 구문을 작성해야 한다. |

SelectBuilder와 SqlBuilder클래스가 신기하지는 않지만 이 클래스가 동작하는 방식을 아는게 중요하다. SelectBuilder와 SqlBuilder는 문법을 명확히 처리하기 위해 Static Imports와 ThreadLocal변수의 조합을 사용한다. 이 두가지 클래스를 사용하기 위해 다음처럼 정적인 방법으로 import할 필요가 있다.

```java
import static org.apache.ibatis.jdbc.SelectBuilder.*;
```

```java
import static org.apache.ibatis.jdbc.SqlBuilder.*;
```

다음처럼 메소드를 생성하도록 해준다.

```java
/* 향후 제거예정 */
public String selectBlogsSql() {
  BEGIN(); // ThreadLocal 변수 제거
  SELECT("*");
  FROM("BLOG");
  return SQL();
}
```

```java
/* 향후 제거예정 */
private String selectPersonSql() {
  BEGIN(); // ThreadLocal 변수 제거
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
