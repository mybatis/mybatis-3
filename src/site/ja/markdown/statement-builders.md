title: MyBatis 3 | SQL ビルダークラス
author: Clinton Begin, Iwao AVE!

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

# SQL ビルダークラス

## 問題

Java のコードに SQL を埋め込むのは、Java 開発者にとって最も不愉快なことの一つでしょう。通常これは、SQL を動的に生成する必要がある場合に行われます（そうでなければ、別ファイルやストアドプロシージャといった形で外部化できるはずなので）。これまで見てきたように、MyBatis では XML マッピング機能を使って動的に SQL を生成することができます。しかし、時には Java のコード内で SQL ステートメントを構築しなくてはならない場合もあります。こうした場合に \'+\' 記号や引用符、改行、整形などの典型的な問題や余分なカンマや AND 演算子を扱うために条件式を入れ子にしたりしなくて済むように、MyBatis にはもう一つの方法を提供しています。

```java
// THE PROBLEM
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

## 解決策

MyBatis 3 ではこうした問題を解決する際の助けとなる便利なクラス \'SQL\' が用意されています。SQL クラスのインスタンスを作成し、順番にメソッドを呼び出していくことで SQL 文を構築することができるようになっています。上記のコードは SQL クラスを使うと次のように書き換えることができます。

```java
import org.apache.ibatis.jdbc.SQL;

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

この例の何がそんなに特別なのでしょうか。よく見るとわかりますが、この例では、誤って \"AND\" が重複してしまうことや、\"WHERE\" と \"AND\" のどちらを使うか、あるいはどちらも使わないか、といったことを考慮せずに書くことができています。SQL クラスは、どこに \"WHERE\" を挿入すれば良いか、どこで \"AND\" が必要か、といったことを判断しながら文字列を連結します。

## SQL クラス

いくつか例を挙げておきます。

```java
// 匿名内部クラス
public String deletePersonSql() {
  return new SQL() {{
    DELETE_FROM("PERSON");
    WHERE("ID = #{id}");
  }}.toString();
}

// メソッドチェーン（Builder / Fluent スタイル）
public String insertPersonSql() {
  String sql = new SQL()
    .INSERT_INTO("PERSON")
    .VALUES("ID, FIRST_NAME", "#{id}, #{firstName}")
    .VALUES("LAST_NAME", "#{lastName}")
    .toString();
  return sql;
}

// 条件分岐あり（匿名内部クラスからアクセスできるよう、引数は final として宣言されています）
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

| メソッド | 説明 |
| --- | --- |
| <ul><li>`SELECT(String)`</li> <li>`SELECT(String...)`</li></ul> | SELECT 句を開始、あるいは SELECT 句に文字列を追加します。このメソッドは複数回呼び出すことができ、その場合は引数として渡した文字列が SELECT 句に追加されます。引数は通常カンマ区切りの列名あるいはエイリアスのリストですが、ドライバが受け付ける文字列であれば何でも構いません。 |
| <ul><li>`SELECT_DISTINCT(String)`</li> <li>`SELECT_DISTINCT(String...)`</li></ul> | SELECT 句を開始、あるいは SELECT 句に文字列を追加します。また、生成されるクエリに DISTINCT キーワードを追加します。このメソッドは複数回呼び出すことができ、その場合は SELECT 句に引数として渡した文字列が追加されます。引数は通常カンマ区切りの列名あるいはエイリアスのリストですが、ドライバが受け付ける文字列であれば何でも構いません。 |
| <ul><li>`FROM(String)`</li> <li>`FROM(String...)`</li></ul> | FROM 句を開始、あるいは FROM 句に文字列を追加します。このメソッドは複数回呼び出すことができ、その場合は FROM 句に引数として渡した文字列が追加されます。引数は通常カンマ区切りのテーブル名あるいはエイリアスのリストですが、ドライバが受け付ける文字列であれば何でも構いません。 |
| <ul><li>`JOIN(String)`</li> <li>`JOIN(String...)`</li> <li>`INNER_JOIN(String)`</li> <li>`INNER_JOIN(String...)`</li> <li>`LEFT_OUTER_JOIN(String)`</li> <li>`LEFT_OUTER_JOIN(String...)`</li> <li>`RIGHT_OUTER_JOIN(String)`</li> <li>`RIGHT_OUTER_JOIN(String...)`</li> <li>`OUTER_JOIN(String)`</li> <li>`OUTER_JOIN(String...)`</li></ul> | 呼び出されたメソッドに応じて、新しい JOIN 句を追加します。引数には結合対象のテーブルと結合条件の文字列を指定します。 |
| <ul><li>`WHERE(String)`</li> <li>`WHERE(String...)`</li></ul> | 新しい WHERE 条件を AND で連結して追加します。このメソッドは複数回呼び出すことができ、その場合は引数で指定した新しい条件が AND と共に追加されます。複数の条件を OR で連結する場合は OR() メソッドを使ってください。 |
| `OR()` | 最後に使用した WHERE または HAVING の条件を OR で分割します。複数回呼ぶこともできますが、連続して呼び出すと不正な SQL が生成される場合があります。 |
| `AND()` | 最後に使用した WHERE または HAVING の条件を AND で分割します。複数回呼ぶこともできますが、連続して呼び出すと不正な SQL が生成される可能性があります。`WHERE` と `HAVING` はどちらも自動的に `AND` を挟んで引数を連結するので、このメソッドを使うことは通常ありません。 |
| <ul><li>`GROUP_BY(String)`</li> <li>`GROUP_BY(String...)`</li></ul> | カンマを挟んで `GROUP BY` 句を追加します。複数回呼ぶことができ、その度にカンマを挟んで新しい条件が追加されます。 |
| <ul><li>`HAVING(String)`</li> <li>`HAVING(String...)`</li></ul> | AND を挟んで新しい `HAVING` 句を追加します。複数回呼ぶことができ、その度に AND を挟んで新しい条件が追加されます。複数の条件を OR で連結する場合は OR() メソッドを使ってください。 |
| <ul><li>`ORDER_BY(String)`</li> <li>`ORDER_BY(String...)`</li></ul> | カンマを挟んで `ORDER BY` 句を追加します。複数回呼ぶことができ、その度にカンマを挟んで新しい条件が追加されます。 |
| <ul><li>`LIMIT(String)`</li> <li>`LIMIT(int)`</li></ul> | `LIMIT` 句を追加します。 このメソッドは SELECT(), UPDATE(), DELETE() と一緒に使うと有効になり、 SELECT()使用時は、OFFSET()と一緒に使うように設計されています。 (3.5.2以降で利用可能) |
| <ul><li>`OFFSET(String)`</li> <li>`OFFSET(long)`</li></ul> | `OFFSET` 句を追加します。 このメソッドは SELECT() と一緒に使うと有効になり、 LIMIT()と一緒に使うように設計されています。(3.5.2以降で利用可能) |
| <ul><li>`OFFSET_ROWS(String)`</li> <li>`OFFSET_ROWS(long)`</li></ul> | `OFFSET n ROWS` 句を追加します。 このメソッドは SELECT() と一緒に使うと有効になり、 FETCH\_FIRST\_ROWS\_ONLY()と一緒に使うように設計されています。 (3.5.2以降で利用可能) |
| <ul><li>`FETCH_FIRST_ROWS_ONLY(String)`</li> <li>`FETCH_FIRST_ROWS_ONLY(int)`</li></ul> | `FETCH FIRST n ROWS ONLY` 句を追加します。 このメソッドは SELECT() と一緒に使うと有効になり、 OFFSET\_ROWS()と一緒に使うように設計されています。 (3.5.2以降で利用可能) |
| `DELETE_FROM(String)` | 対象となるテーブルを指定して delete ステートメントを開始します。通常、この後に WHERE メソッドの呼び出しが続きます。 |
| `INSERT_INTO(String)` | 対象となるテーブルを指定して insert ステートメントを開始します。通常、この後に一回以上 VALUES() 又は INTO\_COLUMNS() と INTO\_VALUES() メソッドの呼び出しが続きます。 |
| <ul><li>`SET(String)`</li> <li>`SET(String...)`</li></ul> | update ステートメントの set 句に文字列を追加します。 |
| `UPDATE(String)` | 対象となるテーブルを指定して update ステートメントを開始します。通常、この後に一回以上の SET() メソッドと WHERE() メソッドの呼び出しが続きます。 |
| `VALUES(String, String)` | insert ステートメントに values 句を追加します。第一引数は列のリストで、第二引数は値のリストです。 |
| `INTO_COLUMNS(String...)` | insert ステートメントにカラムリスト追加します。 このメソッドは、INTO\_VALUES() メソッドと一緒に呼び出します。 |
| `INTO_VALUES(String...)` | insert ステートメントに value 句を追加します。 このメソッドは、INTO\_COLUMNS() メソッドと一緒に呼び出します。 |
| `ADD_ROW()` | 一括挿入用に新しい行領域を追加します。 (3.5.2以降で利用可能) |

<span class="label important">NOTE</span> `LIMIT` 、`OFFSET` 、`OFFSET n ROWS` 、`FETCH FIRST n ROWS ONLY` 句は生成されたステートメントにそのまま書き込むという点に注意することが重要です。 言い換えると、これらの句をサポートしていないデータベースに対して、そのデータベースで解釈可能な表現へ変換することはしません。 そのため、利用するデータベースがこれらの句をサポートしているか否かを事前に把握しておくことが重要になります。 もし、利用するデータベースがこれらの句をサポートしていない場合は、SQL実行時にエラーになります。

バージョン3.4.2以降では、次のように可変長引数を使うことができます。

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

バージョン3.5.2以降では、次のように一括挿入用のステートメントを作成することができます。

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

バージョン3.5.2以降では、次のように検索結果行を制限するための検索ステートメントを作成することができます。

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

バージョン 3.5.15 以降では、`applyIf(boolean, Consumer<SQL>)` と `applyIf(BooleanSupplier, Consumer<SQL>)` を使用すると、条件が真の場合にだけ SQL 句を追加できます。`applyForEach(Iterable<E>, ForEachConsumer<SQL, E>)` は各要素についてコンシューマーを呼び出し、SQL ビルダー、要素、0 から始まるインデックスを渡します。これらのメソッドはビルダーを返すため、呼び出しを連結できます。

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

## SqlBuilder および SelectBuilder （非推奨）

<a id="deprecated-builders"></a>

当初は ThreadLocal 変数を使って Java 言語の制限を補うアプローチを採用していましたが、最近はビルダーパターンや匿名内部クラスを使っているフレームワークが主流となってきていることもあり、このアプローチを使っている SelectBuilder, SqlBuilder クラスは非推奨（deprecated）扱いとなりました。

以下は、非推奨となった SqlBuilder, SelectBuilder のみを対象とした説明になります。

| Method | Description |
| --- | --- |
| `BEGIN()` / `RESET()` | インポートした SelectBuilder または SqlBuilder クラスの ThreadLocal の状態をクリアし、新たなステートメント構築に備えます。各クラスは独立した状態を保持します。新しいステートメントを開始するときは BEGIN() を使い、構築中のステートメントを破棄する場合（条件次第で全く別のステートメントが必要となるようなケース）は RESET() を使うようにすると読みやすいコードになります。 |
| `SQL()` | 生成された SQL を返すと同時に、インポートしたビルダークラスの状態をリセットします。ステートメントごとに一度呼び出し、再度呼び出す前に次のステートメントを構築してください。 |

SelectBuilder や SqlBuilder クラスは魔法ではないので、仕組みを理解しておく必要があります。SelectBuilder, SqlBuilder は静的インポート（Static Import）と ThreadLocal 変数を組み合わせることで、条件分岐と相性の良いクリーンな構文で SQL に求められるフォーマット処理を可能にしています。まずどちらかのクラスを静的にインポートしておく必要があります（両方ではありません）。

```java
import static org.apache.ibatis.jdbc.SelectBuilder.*;
```

```java
import static org.apache.ibatis.jdbc.SqlBuilder.*;
```

例えば、次のようなメソッドを書くことができます。

```java
/* DEPRECATED 非推奨 */
public String selectBlogsSql() {
  BEGIN(); // Clears ThreadLocal variable
  SELECT("*");
  FROM("BLOG");
  return SQL();
}
```

```java
/* DEPRECATED 非推奨 */
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
