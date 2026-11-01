title: MyBatis 3 | SQL Builder 類別
author: Clinton Begin

<h1 class="d-none">Avoid blank site</h1>

## SQL Builder 類別

### 問題所在

Java 開發者最痛苦的事情之一就是在 Java 程式碼中嵌入 SQL。通常這樣做是因為 SQL 必須動態產生——否則您可以將其外置在檔案或預存程序中。正如您所見，MyBatis 的 XML 映射功能為動態 SQL 產生提供了強大的解決方案。然而，有時在 Java 程式碼內部建構 SQL 陳述式字串變得十分必要。在這種情況下，MyBatis 還提供了另一項功能來為您提供協助，避免陷入典型的加號、引號、換行符號、格式化問題以及為了處理額外的逗號或 AND 連接詞而產生的巢狀條件判斷等混亂之中。事實上，在 Java 中動態產生 SQL 程式碼確實可能是一場惡夢。例如：

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

### 解決方案

MyBatis 3 提供了一個便利的工具類別來協助解決此問題。使用 SQL 類別，您只需建立一個實例，並呼叫其上的方法來一步一步建構 SQL 陳述式。當使用 SQL 類別重寫時，上述範例問題將如下所示：

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

這個範例有什麼特別之處呢？仔細觀察可以發現，它不必擔心意外重複「AND」關鍵字，或者在「WHERE」與「AND」之間做出選擇，甚至完全不需要它們。SQL 類別會負責理解「WHERE」該放在哪裡、哪裡該使用「AND」以及所有的字串串接。

### SQL 類別

以下是一些範例：

```java
// 匿名內部類別
public String deletePersonSql() {
  return new SQL() {{
    DELETE_FROM("PERSON");
    WHERE("ID = #{id}");
  }}.toString();
}

// Builder / Fluent 風格
public String insertPersonSql() {
  String sql = new SQL()
    .INSERT_INTO("PERSON")
    .VALUES("ID, FIRST_NAME", "#{id}, #{firstName}")
    .VALUES("LAST_NAME", "#{lastName}")
    .toString();
  return sql;
}

// 包含條件判斷（注意 final 參數，匿名內部類別需要它才能存取）
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

| 方法 | 說明 |
|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <ul><li>`SELECT(String)`</li><li>`SELECT(String...)`</li></ul> | 開始或附加至 `SELECT` 子句。可以呼叫多次，參數將會附加至 `SELECT` 子句。參數通常是以逗號分隔的欄位與別名清單，但可以是驅動程式所接受的任何內容。 |
| <ul><li>`SELECT_DISTINCT(String)`</li><li>`SELECT_DISTINCT(String...)`</li></ul> | 開始或附加至 `SELECT` 子句，並在產生的查詢中加入 `DISTINCT` 關鍵字。可以呼叫多次，參數將會附加至 `SELECT` 子句。參數通常是以逗號分隔的欄位與別名清單，但可以是驅動程式所接受的任何內容。 |
| <ul><li>`FROM(String)`</li><li>`FROM(String...)`</li></ul> | 開始或附加至 `FROM` 子句。可以呼叫多次，參數將會附加至 `FROM` 子句。參數通常是資料表名稱與別名，或是驅動程式所接受的任何內容。 |
| <ul><li>`JOIN(String)`</li><li>`JOIN(String...)`</li><li>`INNER_JOIN(String)`</li><li>`INNER_JOIN(String...)`</li><li>`LEFT_OUTER_JOIN(String)`</li><li>`LEFT_OUTER_JOIN(String...)`</li><li>`RIGHT_OUTER_JOIN(String)`</li><li>`RIGHT_OUTER_JOIN(String...)`</li><li>`OUTER_JOIN(String)`</li><li>`OUTER_JOIN(String...)`</li></ul> | 根據呼叫的方法，加入適當型別的新 `JOIN` 子句。參數可以包含由要聯結的欄位與條件所組成的標準 join。 |
| <ul><li>`WHERE(String)`</li><li>`WHERE(String...)`</li></ul> | 附加新的 `WHERE` 子句條件，以 `AND` 串接。可以呼叫多次，每次都會使用 `AND` 串接新條件。使用 `OR()` 以 `OR` 分隔。 |
| `OR()` | 使用 `OR` 分隔目前的 `WHERE` 子句條件。可以呼叫多次，但連續多次呼叫會產生不穩定的 `SQL`。 |
| `AND()` | 使用 `AND` 分隔目前的 `WHERE` 子句條件。可以呼叫多次，但連續多次呼叫會產生不穩定的 `SQL`。由於 `WHERE` 與 `HAVING` 都會自動以 `AND` 串接，因此這是一個非常不常用的方法，僅為了完整性而包含在內。 |
| <ul><li>`GROUP_BY(String)`</li><li>`GROUP_BY(String...)`</li></ul> | 附加新的 `GROUP BY` 子句元素，以逗號串接。可以呼叫多次，每次都會使用逗號串接新條件。 |
| <ul><li>`HAVING(String)`</li><li>`HAVING(String...)`</li></ul> | 附加新的 `HAVING` 子句條件，以 `AND` 串接。可以呼叫多次，每次都會使用 `AND` 串接新條件。使用 `OR()` 以 `OR` 分隔。 |
| <ul><li>`ORDER_BY(String)`</li><li>`ORDER_BY(String...)`</li></ul> | 附加新的 `ORDER BY` 子句元素，以逗號串接。可以呼叫多次，每次都會使用逗號串接新條件。 |
| <ul><li>`LIMIT(String)`</li><li>`LIMIT(int)`</li></ul> | 附加 `LIMIT` 子句。此方法在與 `SELECT()`、`UPDATE()` 及 `DELETE()` 一起使用時有效。在與 `SELECT()` 一起使用時，設計為與 `OFFSET()` 搭配使用。（自 3.5.2 起提供） |
| <ul><li>`OFFSET(String)`</li><li>`OFFSET(long)`</li></ul> | 附加 `OFFSET` 子句。此方法在與 `SELECT()` 一起使用時有效。設計為與 `LIMIT()` 搭配使用。（自 3.5.2 起提供） |
| <ul><li>`OFFSET_ROWS(String)`</li><li>`OFFSET_ROWS(long)`</li></ul> | 附加 `OFFSET n ROWS` 子句。此方法在與 `SELECT()` 一起使用時有效。設計為與 `FETCH_FIRST_ROWS_ONLY()` 搭配使用。（自 3.5.2 起提供） |
| <ul><li>`FETCH_FIRST_ROWS_ONLY(String)`</li><li>`FETCH_FIRST_ROWS_ONLY(int)`</li></ul> | 附加 `FETCH FIRST n ROWS ONLY` 子句。此方法在與 `SELECT()` 一起使用時有效。設計為與 `OFFSET_ROWS()` 搭配使用。（自 3.5.2 起提供） |
| `DELETE_FROM(String)` | 開始一個 delete 陳述式並指定要從中刪除的資料表。通常後面應該跟隨一個 WHERE 陳述式！ |
| `INSERT_INTO(String)` | 開始一個 insert 陳述式並指定要插入的資料表。後面應該跟隨一或多個 `VALUES()` 或 `INTO_COLUMNS()` 與 `INTO_VALUES()` 呼叫。 |
| <ul><li>`SET(String)`</li><li>`SET(String...)`</li></ul> | 附加至 update 陳述式的「set」清單。 |
| `UPDATE(String)` | 開始一個 update 陳述式並指定要更新的資料表。後面應該跟隨一或多個 `SET()` 呼叫，且通常會有一個 `WHERE()` 呼叫。 |
| `VALUES(String, String)` | 附加至 insert 陳述式。第一個參數是要插入的欄位，第二個參數是值。 |
| `INTO_COLUMNS(String...)` | 將欄位片語附加至 insert 陳述式。這應該與 `INTO_VALUES()` 一起呼叫。 |
| `INTO_VALUES(String...)` | 將值片語附加至 insert 陳述式。這應該與 `INTO_COLUMNS()` 一起呼叫。 |
| `ADD_ROW()` | 為批次插入新增資料列。（自 3.5.2 起提供） |


<span class="label important">NOTE</span> 請特別注意，SQL 類別會將 `LIMIT`、`OFFSET`、`OFFSET n ROWS` 與 `FETCH FIRST n ROWS ONLY` 子句原封不動地寫入產生的陳述式中。換句話說，對於未直接支援這些子句的資料庫，程式庫不會嘗試正規化這些值。因此，使用者必須非常清楚了解目標資料庫是否支援這些子句。如果目標資料庫不支援這些子句，則使用此功能很可能會產生出現執行階段錯誤的 SQL。

自版本 3.4.2 起，您可以如下使用可變長度引數（Varargs）：

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

自版本 3.5.2 起，您可以如下建立用於批次插入的 insert 陳述式：

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

自版本 3.5.2 起，您可以如下建立帶有列數限制子句的 select 陳述式：

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

### SqlBuilder 與 SelectBuilder（已廢棄）

在 3.2 版本之前，我們採用了稍有不同的做法，利用 ThreadLocal 變數來掩蓋一些使 Java DSL 顯得繁瑣的語言限制。然而，這種做法現在已被廢棄，因為現代框架已經讓人們習慣為此類事情使用 builder 模式與匿名內部類別。因此，SelectBuilder 與 SqlBuilder 類別已被廢棄。

下列方法僅適用於已廢棄的 SqlBuilder 與 SelectBuilder 類別。

| 方法 | 說明 |
|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `BEGIN()` / `RESET()` | 這些方法會清除 `SelectBuilder` 類別的 `ThreadLocal` 狀態，並為建構新的陳述式做好準備。在開始新的陳述式時，`BEGIN()` 的可讀性最佳。當因為某些原因在執行過程中清除陳述式時（例如在某些條件下邏輯需要完全不同的陳述式），`RESET()` 的可讀性最佳。 |
| `SQL()` | 這會回傳產生的 SQL 並重設 `SelectBuilder` 狀態（如同呼叫了 `BEGIN()` 或 `RESET()`）。因此，此方法只能被呼叫一次！ |


SelectBuilder 與 SqlBuilder 類別並不神奇，但了解它們的工作原理很重要。SelectBuilder 與 SqlBuilder 結合使用靜態匯入（Static Imports）與 ThreadLocal 變數，以實現可輕鬆與條件判斷交錯的簡潔語法。要使用它們，您可以從類別中靜態匯入方法，如下所示（二選一，不可同時使用）：

```java
import static org.apache.ibatis.jdbc.SelectBuilder.*;
```

```java
import static org.apache.ibatis.jdbc.SqlBuilder.*;
```

這允許我們建立如下的方法：

```java
/* 已廢棄 (DEPRECATED) */
public String selectBlogsSql() {
  BEGIN(); // 清除 ThreadLocal 變數
  SELECT("*");
  FROM("BLOG");
  return SQL();
}
```

```java
/* 已廢棄 (DEPRECATED) */
private String selectPersonSql() {
  BEGIN(); // 清除 ThreadLocal 變數
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
