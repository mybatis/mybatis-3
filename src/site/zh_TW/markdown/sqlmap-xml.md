title: MyBatis 3 | Mapper XML 映射檔
author: Clinton Begin

<h1 class="d-none">Avoid blank site</h1>

## Mapper XML 映射檔

MyBatis 的真正威力在於其映射陳述式（Mapped Statements）。這是奇蹟發生的地方。儘管 Mapper XML 映射檔功能強大，但它們相對簡單。當然，如果您將它們與同等的 JDBC 程式碼進行比較，您會立刻發現它節省了 95% 的程式碼。MyBatis 的設計宗旨是專注於 SQL 本身，並盡最大努力不對您造成干擾。

Mapper XML 映射檔只有少數幾個頂級元素（按照它們應當定義的循序）：

- `cache` – 設定給定命名空間（namespace）的快取。
- `cache-ref` – 參照來自其他命名空間的快取設定。
- `resultMap` – 最複雜且強大的元素，用來描述如何從資料庫結果集（Result Set）中將資料載入至您的物件中。
- ~~`parameterMap` – 已廢棄！舊式的參數映射方式。現在建議使用行內參數（inline parameters），此元素未來可能會被移除。本文件不再贅述。~~
- `sql` – 可被其他陳述式重複參照使用的 SQL 程式碼片段。
- `insert` – 映射的 INSERT 陳述式。
- `update` – 映射的 UPDATE 陳述式。
- `delete` – 映射的 DELETE 陳述式。
- `select` – 映射的 SELECT 陳述式。

接下來的小節將依序詳細介紹這些元素，首先從陳述式本身開始。

<a id="select"></a>
### select

select 陳述式是您在 MyBatis 中最常用的元素之一。將資料存入資料庫中，直到將其查詢出來之前價值都不大，因此大多數應用程式查詢資料的頻率遠高於修改資料。對於每一次 insert、update 或 delete，通常都會有許多次 select。這是 MyBatis 的核心原則之一，也是在查詢與結果映射上投入如此多關注與努力的原因。對於簡單的情況，select 元素非常直觀。例如：

```xml
<select id="selectPerson" parameterType="int" resultType="hashmap">
  SELECT * FROM PERSON WHERE ID = #{id}
</select>
```

這個陳述式名為 `selectPerson`，接受一個 `int`（或 `Integer`）型別的參數，並回傳一個以欄位名稱為鍵（key）、列資料為值（value）的 `HashMap`。

請注意參數的標記法：

```
#{id}
```

這會指示 MyBatis 建立一個 `PreparedStatement` 參數。在 JDBC 中，這樣的參數會在傳遞給新 `PreparedStatement` 的 SQL 中以 "?" 標識，類似這樣：

```java
// 類似的 JDBC 程式碼，非 MyBatis…
String selectPerson = "SELECT * FROM PERSON WHERE ID=?";
PreparedStatement ps = conn.prepareStatement(selectPerson);
ps.setInt(1,id);
```

當然，單靠 JDBC 還需要大量程式碼才能擷取結果並將其映射至物件實例，而這正是 MyBatis 替您省去的工作。關於參數與結果映射還有很多需要了解的細節，這些細節值得在後續章節中單獨說明。

select 元素具備更多屬性，允許您微調每個陳述式的行為細節。

```xml
<select
  id="selectPerson"
  parameterType="int"
  parameterMap="deprecated"
  resultType="hashmap"
  resultMap="personResultMap"
  flushCache="false"
  useCache="true"
  timeout="10"
  fetchSize="256"
  statementType="PREPARED"
  resultSetType="FORWARD_ONLY">
```

| 屬性 | 說明 |
|---|---|
| `id` | 此命名空間中的唯一識別碼，可用於參照此陳述式。 |
| `parameterType` | 將傳遞至此陳述式之參數的完整類別名稱或別名。此屬性為選填，因為 MyBatis 可以根據傳遞給陳述式的實際參數自動推算要使用的 TypeHandler。預設為 `unset`。 |
| <strike>`parameterMap`</strike> | <strike>這是參照外部 `parameterMap` 的已廢棄方式。請改用行內參數映射與 `parameterType` 屬性。</strike> |
| `resultType` | 預期從此陳述式回傳的完整類別名稱或別名。請注意，如果回傳的是集合，這應該是集合所包含的型別，而不是集合本身的型別。請使用 `resultType` 或 `resultMap` 其中之一，不可兩者同時使用。 |
| `resultMap` | 對外部 `resultMap` 的具名參照。Result Map 是 MyBatis 最強大的功能；只要對其有深入了解，許多複雜的映射案例都能迎刃而解。請使用 `resultMap` 或 `resultType` 其中之一，不可兩者同時使用。 |
| `flushCache` | 將其設為 `true` 會導致每次呼叫此陳述式時同時清除本地快取與二級快取。預設值：select 陳述式為 `false`。 |
| `useCache` | 將其設為 `true` 會導致此陳述式的結果被快取至二級快取中。預設值：select 陳述式為 `true`。 |
| `timeout` | 設定驅動程式在拋出例外之前，等待資料庫回應請求的最大秒數。預設為 `unset`（取決於驅動程式）。 |
| `fetchSize` | 這是給驅動程式的提示，嘗試讓驅動程式以每次回傳等同於此設定數量的列批次載入資料。預設為 `unset`（取決於驅動程式）。 |
| `statementType` | 可為 `STATEMENT`、`PREPARED` 或 `CALLABLE` 其中之一。這會使 MyBatis 分別使用 `Statement`、`PreparedStatement` 或 `CallableStatement`。預設值：`PREPARED`。 |
| `resultSetType` | 可為 `FORWARD_ONLY`&#124;`SCROLL_SENSITIVE`&#124;`SCROLL_INSENSITIVE`&#124;`DEFAULT`（同 unset）其中之一。預設為 `unset`（取決於驅動程式）。 |
| `databaseId` | 若已設定 databaseIdProvider，MyBatis 將載入所有沒有 `databaseId` 屬性或其 `databaseId` 與目前資料庫相符的陳述式。若同時找到帶有與不帶有 `databaseId` 的相同陳述式，後者將被捨棄。 |
| `resultOrdered` | 僅適用於巢狀結果 select 陳述式：若設為 `true`，則假定巢狀結果已妥善分組或排序，當回傳新的主結果列時，絕不會再出現對前一結果列的參照。這能讓巢狀結果以更節省記憶體的方式填入。預設值：`false`。 |
| `resultSets` | 僅適用於多個結果集的情況。它列出此陳述式將回傳的結果集清單，並為每個結果集指定名稱。名稱之間以逗號分隔。 |
| `affectData` | 當撰寫會回傳資料的 INSERT、UPDATE 或 DELETE 陳述式時，請將此值設為 `true`，以確保交易能被正確控制。另請參閱[交易控制方法](./java-api.html#transaction-control-methods)。預設值：`false`（自 3.5.12 起）。 |
[Select Attributes]

<a id="insert-update-and-delete"></a>
### insert, update and delete

資料修改陳述式 insert、update 與 delete 在實作上非常相似：

```xml
<insert
  id="insertAuthor"
  parameterType="domain.blog.Author"
  flushCache="true"
  statementType="PREPARED"
  keyProperty=""
  keyColumn=""
  useGeneratedKeys=""
  timeout="20">

<update
  id="updateAuthor"
  parameterType="domain.blog.Author"
  flushCache="true"
  statementType="PREPARED"
  timeout="20">

<delete
  id="deleteAuthor"
  parameterType="domain.blog.Author"
  flushCache="true"
  statementType="PREPARED"
  timeout="20">
```

| 屬性 | 說明 |
|---|---|
| `id` | 此命名空間中的唯一識別碼，可用於參照此陳述式。 |
| `parameterType` | 將傳遞至此陳述式之參數的完整類別名稱或別名。此屬性為選填，因為 MyBatis 可以根據傳遞給陳述式的實際參數自動推算要使用的 TypeHandler。預設為 `unset`。 |
| ~~`parameterMap`~~ | ~~這是參照外部 parameterMap 的已廢棄方式。請改用行內參數映射與 parameterType 屬性。~~ |
| `flushCache` | 將其設為 `true` 會導致每次呼叫此陳述式時同時清除二級快取與本地快取。預設值：insert、update 與 delete 陳述式為 `true`。 |
| `timeout` | 設定驅動程式在拋出例外之前，等待資料庫回應請求的最大秒數。預設為 `unset`（取決於驅動程式）。 |
| `statementType` | 可為 `STATEMENT`、`PREPARED` 或 `CALLABLE` 其中之一。這會使 MyBatis 分別使用 `Statement`、`PreparedStatement` 或 `CallableStatement`。預設值：`PREPARED`。 |
| `useGeneratedKeys` | （僅限 insert 與 update）指示 MyBatis 使用 JDBC 的 `getGeneratedKeys` 方法來擷取資料庫內部產生的索引鍵（例如在 MySQL 或 SQL Server 等關聯式資料庫管理系統中的自動遞增欄位）。預設值：`false`。 |
| `keyProperty` | （僅限 insert 與 update）指定一個屬性，MyBatis 將透過 `getGeneratedKeys` 或 insert 陳述式的 `selectKey` 子元素所回傳的索引鍵值設定至該屬性中。預設為 `unset`。若預期會有多個產生的欄位，可為以逗號分隔的屬性名稱清單。 |
| `keyColumn` | （僅限 insert 與 update）設定資料表中具有產生鍵的欄位名稱。這僅在某些特定資料庫（如 PostgreSQL）中且鍵欄位並非資料表的第一個欄位時才需要。若預期會有多個產生的欄位，可為以逗號分隔的欄位名稱清單。 |
| `databaseId` | 若已設定 databaseIdProvider，MyBatis 將載入所有沒有 `databaseId` 屬性或其 `databaseId` 與目前資料庫相符的陳述式。若同時找到帶有與不帶有 `databaseId` 的相同陳述式，後者將被捨棄。 |
[Insert, Update and Delete Attributes]

以下是 insert、update 與 delete 陳述式的一些範例。

```xml
<insert id="insertAuthor">
  insert into Author (id,username,password,email,bio)
  values (#{id},#{username},#{password},#{email},#{bio})
</insert>

<update id="updateAuthor">
  update Author set
    username = #{username},
    password = #{password},
    email = #{email},
    bio = #{bio}
  where id = #{id}
</update>

<delete id="deleteAuthor">
  delete from Author where id = #{id}
</delete>
```

如前所述，insert 元素稍微豐富一些，因為它包含一些額外的屬性與子元素，允許它以多種方式處理主鍵的生成。

首先，如果您的資料庫支援自動產生索引鍵欄位（例如 MySQL 與 SQL Server），那麼您只需設定 `useGeneratedKeys="true"` 並將 `keyProperty` 指定為目標屬性即可。例如，如果上述 `Author` 資料表的 id 採用了自動遞增欄位型別，則陳述式應修改如下：

```xml
<insert id="insertAuthor" useGeneratedKeys="true"
    keyProperty="id">
  insert into Author (username,password,email,bio)
  values (#{username},#{password},#{email},#{bio})
</insert>
```

若您的資料庫同時支援多列插入（multi-row insert），您可以傳入 `Author` 清單或陣列，並擷取自動產生的索引鍵。

```xml
<insert id="insertAuthor" useGeneratedKeys="true"
    keyProperty="id">
  insert into Author (username, password, email, bio) values
  <foreach item="item" collection="list" separator=",">
    (#{item.username}, #{item.password}, #{item.email}, #{item.bio})
  </foreach>
</insert>
```

對於不支援自動產生欄位型別，或者 JDBC 驅動程式尚不支援自動產生索引鍵的資料庫，MyBatis 提供了另一種處理主鍵生成的方式。

這裡有一個產生隨機 ID 的簡單（且看似不可思議的）範例（這在實務中可能永遠不會做，但它展示了靈活性以及 MyBatis 的強大適應力）：

```xml
<insert id="insertAuthor">
  <selectKey keyProperty="id" resultType="int" order="BEFORE">
    select CAST(RANDOM()*1000000 as INTEGER) a from SYSIBM.SYSDUMMY1
  </selectKey>
  insert into Author
    (id, username, password, email,bio, favourite_section)
  values
    (#{id}, #{username}, #{password}, #{email}, #{bio}, #{favouriteSection,jdbcType=VARCHAR})
</insert>
```

在上面的範例中，`selectKey` 陳述式將優先執行，並設定 `Author` 的 id 屬性，隨後才會呼叫 insert 陳述式。這為您提供了與資料庫自動產生索引鍵類似的行為，而不會使您的 Java 程式碼變得繁雜。

`selectKey` 元素的設定描述如下：

```xml
<selectKey
  keyProperty="id"
  resultType="int"
  order="BEFORE"
  statementType="PREPARED">
```

| 屬性 | 說明 |
|---|---|
| `keyProperty` | `selectKey` 陳述式的結果應設定至的目標屬性。若預期會有多個產生的欄位，可為以逗號分隔的屬性名稱清單。 |
| `keyColumn` | 回傳結果集中與屬性相符的欄位名稱。若預期會有多個產生的欄位，可為以逗號分隔的欄位名稱清單。 |
| `resultType` | 結果的型別。MyBatis 通常可以推算出來，但明確指定也無妨。MyBatis 允許將任何簡單型別作為索引鍵，包含字串（String）。若您預期有多個產生的欄位，可以使用包含預期屬性的物件或 Map。 |
| `order` | 可設為 `BEFORE` 或 `AFTER`。若設為 `BEFORE`，它將先選取索引鍵，設定 `keyProperty`，然後執行 insert 陳述式。若設為 `AFTER`，它會先執行 insert 陳述式，然後再執行 `selectKey` 陳述式——這在像 Oracle 這樣通常在 insert 陳述式內部嵌入序列呼叫的資料庫中非常普遍。 |
| `statementType` | 與前面相同，MyBatis 支援 `STATEMENT`、`PREPARED` 與 `CALLABLE` 陳述式型別，分別映射至 `Statement`、`PreparedStatement` 與 `CallableStatement`。 |
[selectKey Attributes]

作為特殊情況，某些資料庫允許 INSERT、UPDATE 或 DELETE 陳述式回傳結果集（例如 PostgreSQL 與 MariaDB 的 `RETURNING` 子句，或 MS SQL Server 的 `OUTPUT` 子句）。此類陳述式必須編寫為 `<select>` 以映射回傳的資料。

```xml
<select id="insertAndGetAuthor" resultType="domain.blog.Author"
      affectData="true" flushCache="true">
  insert into Author (username, password, email, bio)
  values (#{username}, #{password}, #{email}, #{bio})
  returning id, username, password, email, bio
</select>
```

<a id="sql"></a>
### sql

此元素可用於定義可重複使用的 SQL 程式碼片段，並將其包含在其他陳述式中。它可以在靜態載入階段進行參數化。不同的 include 實例可以傳入不同的屬性值。例如：

```xml
<sql id="userColumns"> ${alias}.id,${alias}.username,${alias}.password </sql>
```

然後可以在另一個陳述式中包含此 SQL 片段，例如：

```xml
<select id="selectUsers" resultType="map">
  select
    <include refid="userColumns"><property name="alias" value="t1"/></include>,
    <include refid="userColumns"><property name="alias" value="t2"/></include>
  from some_table t1
    cross join some_table t2
</select>
```

屬性值也可以用於 include 的 refid 屬性，或 include 子句內部的屬性值中，例如：

```xml
<sql id="sometable">
  ${prefix}Table
</sql>

<sql id="someinclude">
  from
    <include refid="${include_target}"/>
</sql>

<select id="select" resultType="map">
  select
    field1, field2, field3
  <include refid="someinclude">
    <property name="prefix" value="Some"/>
    <property name="include_target" value="sometable"/>
  </include>
</select>
```

<a id="parameters"></a>
### 參數 (Parameters)

在前面的所有陳述式中，您都看到了簡單參數的範例。參數是 MyBatis 中非常強大的元素。對於簡單的情況（大概佔 90% 的使用情境），它們並不複雜，例如：

```xml
<select id="selectUsers" resultType="User">
  select id, username, password
  from users
  where id = #{id}
</select>
```

上述範例展示了一個非常簡單的具名參數映射。`parameterType` 設為 `int`，因此參數名稱可以任意命名。像 `Integer` 和 `String` 這樣的基本型別或簡單資料型別沒有相關屬性，因此會直接替換整個參數的值。但是，如果您傳入一個複雜物件，行為就會略有不同。例如：

```xml
<insert id="insertUser" parameterType="User">
  insert into users (id, username, password)
  values (#{id}, #{username}, #{password})
</insert>
```

如果將 User 型別的參數物件傳遞給該陳述式，則會尋找 id、username 與 password 屬性，並將它們的值傳遞給 `PreparedStatement` 參數。

這對於將參數傳遞給陳述式來說既美觀又簡單。但參數映射還有很多其他功能。

首先，與 MyBatis 的其他部分一樣，參數可以指定更具體的資料型別。

```
#{property,javaType=int,jdbcType=NUMERIC}
```

與 MyBatis 的其餘部分相同，`javaType` 幾乎總是可以從參數物件中推斷出來，除非該物件是 `HashMap`。在這種情況下，應當明確指定 `javaType` 以確保使用正確的 `TypeHandler`。

<span class="label important">NOTE</span> 若傳遞的值為 `null`，JDBC 要求所有允許為 Null（nullable）的欄位都必須指定 JDBC 型別。您可以透過閱讀 `PreparedStatement.setNull()` 方法的 JavaDoc 來自行驗證這一點。

為了進一步自訂型別處理，您還可以指定特定的 `TypeHandler` 類別（或別名），例如：

```
#{age,javaType=int,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
```

看起來設定開始變得冗長了，但事實上您很少需要設定這些選項。

對於數值型別，還有一個 `numericScale` 用於確定小數點後的位數。

```
#{height,javaType=double,jdbcType=NUMERIC,numericScale=2}
```

最後，`mode` 屬性允許您指定 `IN`、`OUT` 或 `INOUT` 參數。如果參數是 `OUT` 或 `INOUT`，則參數物件屬性的實際值將會被修改，正如您在呼叫輸出參數時所預期的那樣。若 `mode=OUT`（或 `INOUT`）且 `jdbcType=CURSOR`（即 Oracle REFCURSOR），您必須指定一個 `resultMap` 以將 `ResultSet` 映射至參數的型別。請注意，此處的 `javaType` 屬性是選填的，若在 `jdbcType` 為 `CURSOR` 的情況下留空，它將自動設定為 `ResultSet`。

```
#{department, mode=OUT, jdbcType=CURSOR, javaType=ResultSet, resultMap=departmentResultMap}
```

MyBatis 也支援諸如結構體（structs）等更進階的資料型別，但在註冊輸出參數時，您必須告知陳述式該型別名稱。例如（同樣地，在實務中請勿像這樣換行）：

```
#{middleInitial, mode=OUT, jdbcType=STRUCT, jdbcTypeName=MY_TYPE, resultMap=departmentResultMap}
```

儘管有這些強大的選項，但在大多數情況下，您只需指定屬性名稱，MyBatis 就會自動處理其餘細節。最多，您只需為可為 Null 的欄位指定 `jdbcType`。

```
#{firstName}
#{middleInitial,jdbcType=VARCHAR}
#{lastName}
```

<a id="string-substitution"></a>
#### 字串替換 (String Substitution)

預設情況下，使用 `#{}` 語法會使 MyBatis 產生 `PreparedStatement` 屬性，並針對 `PreparedStatement` 參數（例如 ?）安全地設定值。雖然這樣更安全、更快速且幾乎總是首選方式，但有時您只是想直接將未修改的字串注入至 SQL 陳述式中。例如，對於 ORDER BY，您可能會使用類似這樣的寫法：

```sql
ORDER BY ${columnName}
```

在這裡，MyBatis 不會修改或跳脫（escape）該字串。

當 SQL 陳述式中的中繼資料（即資料表名稱或欄位名稱）是動態的時候，字串替換非常有用。例如，如果您想依據資料表的任何欄位進行 `select`，與其撰寫如下程式碼：

```java
@Select("select * from user where id = #{id}")
User findById(@Param("id") long id);

@Select("select * from user where name = #{name}")
User findByName(@Param("name") String name);

@Select("select * from user where email = #{email}")
User findByEmail(@Param("email") String email);

// 以及更多 "findByXxx" 方法
```

您可以直接撰寫：

```java
@Select("select * from user where ${column} = #{value}")
User findByColumn(@Param("column") String column, @Param("value") String value);
```

其中 `${column}` 將被直接替換，而 `#{value}` 將被預編譯處理。因此，您可以透過以下方式完成相同的工作：

```java
User userOfId1 = userMapper.findByColumn("id", 1L);
User userOfNameKid = userMapper.findByColumn("name", "kid");
User userOfEmail = userMapper.findByColumn("email", "noone@nowhere.com");
```

此構想也可以應用於替換資料表名稱。

<span class="label important">NOTE</span> 接受來自使用者的輸入並以此種方式未經修改直接提供給陳述式是不安全的。這會導致潛在的 SQL 注入攻擊（SQL Injection），因此您應當禁止使用者在這些欄位中輸入內容，或者始終執行您自己的跳脫與校驗檢查。

<a id="result-maps"></a>
### 結果映射 (Result Maps)

`resultMap` 元素是 MyBatis 中最重要且最強大的元素。它讓您可以省去 JDBC 從 `ResultSet` 中擷取資料所需的 90% 程式碼，並且在某些情況下能做到 JDBC 甚至不支援的事情。事實上，若要為複雜陳述式的聯結映射（join mapping）編寫等效的 JDBC 程式碼，可能需要數千行。`ResultMap` 的設計理念是：對於簡單陳述式完全不需要明確的結果映射，而對於較複雜的陳述式，只需描述必要的關聯關係即可。

您已經看過沒有明確 `resultMap` 的簡單映射陳述式範例。例如：

```xml
<select id="selectUsers" resultType="map">
  select id, username, hashedPassword
  from some_table
  where id = #{id}
</select>
```

這樣的陳述式只是將所有欄位自動映射到 `HashMap` 的鍵，如 `resultType` 屬性所指定。雖然在許多情況下很有用，但 `HashMap` 並不是一個很好的領域模型（domain model）。您的應用程式更有可能使用 JavaBean 或 POJO（Plain Old Java Objects，傳統簡單 Java 物件）作為領域模型。MyBatis 同時支援這兩者。請看以下 JavaBean：

```java
package com.someapp.model;
public class User {
  private int id;
  private String username;
  private String hashedPassword;

  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }
  public String getHashedPassword() {
    return hashedPassword;
  }
  public void setHashedPassword(String hashedPassword) {
    this.hashedPassword = hashedPassword;
  }
}
```

根據 JavaBeans 規範，上述類別具有 3 個屬性：id、username 與 hashedPassword。這些與 select 陳述式中的欄位名稱完全相符。

這樣的 JavaBean 可以像 `HashMap` 一樣輕鬆地映射至 `ResultSet`。

```xml
<select id="selectUsers" resultType="com.someapp.model.User">
  select id, username, hashedPassword
  from some_table
  where id = #{id}
</select>
```

請記住，型別別名（TypeAliases）是您的好夥伴。善用它們，您就不必一直輸入類別的完整限定路徑。例如：

```xml
<!-- 在 XML 設定檔中 -->
<typeAlias type="com.someapp.model.User" alias="User"/>

<!-- 在 SQL 映射 XML 檔中 -->
<select id="selectUsers" resultType="User">
  select id, username, hashedPassword
  from some_table
  where id = #{id}
</select>
```

在這些情況下，MyBatis 在幕後自動建立了一個 `ResultMap`，根據名稱將欄位自動映射至 JavaBean 屬性。若欄位名稱無法完全吻合，您可以在 select 子句中使用欄位別名（標準的 SQL 功能）來使標籤匹配。例如：

```xml
<select id="selectUsers" resultType="User">
  select
    user_id             as "id",
    user_name           as "userName",
    hashed_password     as "hashedPassword"
  from some_table
  where id = #{id}
</select>
```

`ResultMap` 最棒的一點是，您雖然已經了解了許多概念，但甚至還沒見過它真正的定義！這些簡單的情境不需要超過您目前所見的內容。僅作為範例，讓我們看看上一個範例改寫為外部 `resultMap` 時會是什麼樣子，因為這是解決欄位名稱不符的另一種方式。

```xml
<resultMap id="userResultMap" type="User">
  <id property="id" column="user_id" />
  <result property="username" column="user_name"/>
  <result property="password" column="hashed_password"/>
</resultMap>
```

而參照它的陳述式則使用 `resultMap` 屬性來達成（請注意我們移除了 `resultType` 屬性）。例如：

```xml
<select id="selectUsers" resultMap="userResultMap">
  select user_id, user_name, hashed_password
  from some_table
  where id = #{id}
</select>
```

要是世界總能這麼簡單就好了。

<a id="advanced-result-maps"></a>
#### 進階結果映射 (Advanced Result Maps)

MyBatis 誕生時秉持著一個理念：資料庫並不總是如您所想或所需的結構。雖然我們希望每個資料庫都符合完美的第三正規化或 BCNF，但事實並非如此。如果能有一個資料庫完美契合使用它的所有應用程式，那固然很好，但這往往不切實際。Result Map 就是 MyBatis 為這個問題提供的解答。

例如，我們該如何映射下面這個陳述式？

```xml
<!-- 非常複雜的陳述式 -->
<select id="selectBlogDetails" resultMap="detailedBlogResultMap">
  select
       B.id as blog_id,
       B.title as blog_title,
       B.author_id as blog_author_id,
       A.id as author_id,
       A.username as author_username,
       A.password as author_password,
       A.email as author_email,
       A.bio as author_bio,
       A.favourite_section as author_favourite_section,
       P.id as post_id,
       P.blog_id as post_blog_id,
       P.author_id as post_author_id,
       P.created_on as post_created_on,
       P.section as post_section,
       P.subject as post_subject,
       P.draft as draft,
       P.body as post_body,
       C.id as comment_id,
       C.post_id as comment_post_id,
       C.name as comment_name,
       C.comment as comment_text,
       T.id as tag_id,
       T.name as tag_name
  from Blog B
       left outer join Author A on B.author_id = A.id
       left outer join Post P on B.id = P.blog_id
       left outer join Comment C on P.id = C.post_id
       left outer join Post_Tag PT on PT.post_id = P.id
       left outer join Tag T on PT.tag_id = T.id
  where B.id = #{id}
</select>
```

您可能希望將其映射至一個精心設計的物件模型，其中包含由 Author 所撰寫的 Blog，Blog 擁有許多 Post，而每篇 Post 又可能有零個或多個 Comment 與 Tag。以下是一個複雜 ResultMap 的完整範例（假設 Author、Blog、Post、Comments 與 Tags 都是型別別名）。請瀏覽一下，但不用擔心，我們接下來會逐步探討每個環節。雖然它初看可能令人望而生畏，但其實非常直觀。

```xml
<!-- 非常複雜的 Result Map -->
<resultMap id="detailedBlogResultMap" type="Blog">
  <constructor>
    <idArg column="blog_id" javaType="int"/>
  </constructor>
  <result property="title" column="blog_title"/>
  <association property="author" javaType="Author">
    <id property="id" column="author_id"/>
    <result property="username" column="author_username"/>
    <result property="password" column="author_password"/>
    <result property="email" column="author_email"/>
    <result property="bio" column="author_bio"/>
    <result property="favouriteSection" column="author_favourite_section"/>
  </association>
  <collection property="posts" ofType="Post">
    <id property="id" column="post_id"/>
    <result property="subject" column="post_subject"/>
    <association property="author" javaType="Author"/>
    <collection property="comments" ofType="Comment">
      <id property="id" column="comment_id"/>
    </collection>
    <collection property="tags" ofType="Tag" >
      <id property="id" column="tag_id"/>
    </collection>
    <discriminator javaType="int" column="draft">
      <case value="1" resultType="DraftPost"/>
    </discriminator>
  </collection>
</resultMap>
```

`resultMap` 元素包含許多子元素與值得深入探討的結構。以下是 `resultMap` 元素的概念圖景。

<a id="resultmap"></a>
#### resultMap

- `constructor` - 用於在類別實例化時將結果注入至建構函式中
  - `idArg` - ID 引數；將結果標記為 ID 將有助於提升整體效能
  - `arg` - 注入至建構函式的一般結果
- `id` – ID 結果；將結果標記為 ID 將有助於提升整體效能
- `result` – 注入至欄位或 JavaBean 屬性的一般結果
- `association` – 複雜型別關聯；許多結果將匯整至此型別
  - 巢狀結果映射 – 關聯本身就是 `resultMap`，或者可以參照一個 resultMap
- `collection` – 複雜型別的集合
  - 巢狀結果映射 – 集合本身就是 `resultMap`，或者可以參照一個 resultMap
- `discriminator` – 使用結果值來決定要使用哪個 `resultMap`
  - `case` – 基於特定值的結果映射案例
    - 巢狀結果映射 – case 本身也是一個結果映射，因此可以包含許多相同的元素，或者參照外部 resultMap。

| 屬性 | 說明 |
|---|---|
| `id` | 此命名空間中的唯一識別碼，可用於參照此結果映射。 |
| `type` | 完整限定的 Java 類別名稱或型別別名（內建型別別名清單請參見前面的表格）。 |
| `autoMapping` | 若設定此屬性，MyBatis 將啟用或停用此 ResultMap 的自動映射。此屬性會覆寫全域的 autoMappingBehavior。預設值：unset。 |
[ResultMap Attributes]

<span class="label important">Best Practice</span> 請始終漸進式地建構 ResultMap。單元測試在此時非常有幫助。如果您嘗試一次性建構像上面那樣龐大的 `resultMap`，很可能會出錯且難以除錯。請從簡單開始，一步一步演進，並務必撰寫單元測試！使用框架的缺點在於它們有時像個黑盒子（無論是否為開源）。確保獲得預期行為的最佳方法就是撰寫單元測試。這在提交問題回報時也很有幫助。

接下來的小節將更詳細地檢視每個元素。

<a id="id--result"></a>
#### id & result

```xml
<id property="id" column="post_id"/>
<result property="subject" column="post_subject"/>
```

這些是最基本的結果映射。*id* 與 *result* 都會將單一欄位值映射至簡單資料型別（String, int, double, Date 等）的單一屬性或欄位。

兩者之間的唯一差異在於 *id* 會將結果標記為在比較物件實例時使用的識別屬性。這有助於提升整體效能，尤其是快取與巢狀結果映射（即聯結映射）的效能。

兩者皆具有許多屬性：

| 屬性 | 說明 |
|---|---|
| `property` | 將欄位結果映射至的欄位（field）或屬性（property）。若給定名稱存在對應的 JavaBeans 屬性，則會優先使用。否則，MyBatis 將尋找給定名稱的欄位。在這兩種情況下，您都可以使用一般的點標記法進行複雜的屬性導覽。例如，您可以映射至簡單的屬性如：`username`，或較複雜的屬性如：`address.street.number`。 |
| `column` | 資料庫中的欄位名稱，或別名欄位標籤。這與通常傳遞給 `resultSet.getString(columnName)` 的字串相同。 |
| `javaType` | 完整限定的 Java 類別名稱或型別別名（內建型別別名清單請參見前面的表格）。若映射至 JavaBean，MyBatis 通常可以推斷型別。但是，如果您映射至 HashMap，則應明確指定 javaType 以確保預期行為。 |
| `jdbcType` | 下表所列支援型別中的 JDBC 型別。在 insert、update 或 delete 時，只有允許為 Null 的欄位才需要 JDBC 型別。這是 JDBC 的要求，而非 MyBatis 的要求。因此，即使您直接撰寫 JDBC 程式碼，也需要指定此型別——但僅限於允許 Null 的值。 |
| `typeHandler` | 我們之前在文件中討論過預設型別處理器。使用此屬性，您可以在每個映射的基礎上覆寫預設型別處理器。其值可以是 TypeHandler 實作的完整限定類別名稱，也可以是型別別名。 |
[Id and Result Attributes]

<a id="supported-jdbc-types"></a>
#### 支援的 JDBC 型別 (Supported JDBC Types)

供日後參考，MyBatis 透過內建的 JdbcType 列舉支援以下 JDBC 型別。

| ---------- | --------- | ------------- | --------------- | --------- | ----------- |
| `BIT`      | `FLOAT`   | `CHAR`        | `TIMESTAMP`     | `OTHER`   | `UNDEFINED` |
| `TINYINT`  | `REAL`    | `VARCHAR`     | `BINARY`        | `BLOB`    | `NVARCHAR`  |
| `SMALLINT` | `DOUBLE`  | `LONGVARCHAR` | `VARBINARY`     | `CLOB`    | `NCHAR`     |
| `INTEGER`  | `NUMERIC` | `DATE`        | `LONGVARBINARY` | `BOOLEAN` | `NCLOB`     |
| `BIGINT`   | `DECIMAL` | `TIME`        | `NULL`          | `CURSOR`  | `ARRAY`     |

<a id="constructor"></a>
#### constructor

雖然屬性（properties）適用於大多數資料傳輸物件（DTO）類別，並可能涵蓋大部分的領域模型，但在某些情況下，您可能希望使用不可變類別（immutable classes）。通常包含鮮少或從不變更的參照或查閱資料的表格非常適合不可變類別。建構函式注入允許您在類別實例化時設定值，而無需公開 public 方法。MyBatis 也支援 private 屬性與 private JavaBean 屬性來達成此目的，但有些人更偏好建構函式注入。*constructor* 元素實現了這項功能。

請看以下建構函式：

```java
public class User {
   //...
   public User(Integer id, String username, int age) {
     //...
  }
//...
}
```

為了將結果注入至建構函式中，MyBatis 需要以某種方式識別該建構函式。在以下範例中，MyBatis 會依序尋找宣告了三個參數的建構函式：`java.lang.Integer`、`java.lang.String` 與 `int`。

```xml
<constructor>
   <idArg column="id" javaType="int"/>
   <arg column="username" javaType="String"/>
   <arg column="age" javaType="_int"/>
</constructor>
```

當您處理具有許多參數的建構函式時，維護 arg 元素的循序很容易出錯。
自 3.4.3 起，透過指定每個參數的名稱，您可以按任意循序撰寫 arg 元素。若要透過名稱參照建構函式參數，您可以為其新增 `@Param` 註解，或者使用 '-parameters' 編譯器選項編譯專案並啟用 `useActualParamName`（此選項預設已啟用）。即使第二個與第三個參數的循序與宣告的循序不符，以下範例對於同一個建構函式仍然有效。

```xml
<constructor>
   <idArg column="id" javaType="int" name="id" />
   <arg column="age" javaType="_int" name="age" />
   <arg column="username" javaType="String" name="username" />
</constructor>
```

如果存在具有相同名稱與型別的可寫入屬性，則可以省略 `javaType`。

其餘的屬性與規則與一般的 id 和 result 元素相同。

| 屬性 | 說明 |
|---|---|
| `column` | 資料庫中的欄位名稱，或別名欄位標籤。這與通常傳遞給 `resultSet.getString(columnName)` 的字串相同。 |
| `javaType` | 完整限定的 Java 類別名稱或型別別名（內建型別別名清單請參見前面的表格）。若映射至 JavaBean，MyBatis 通常可以推斷型別。但是，如果您映射至 HashMap，則應明確指定 javaType 以確保預期行為。 |
| `jdbcType` | 下表所列支援型別中的 JDBC 型別。在 insert、update 或 delete 時，只有允許為 Null 的欄位才需要 JDBC 型別。這是 JDBC 的要求，而非 MyBatis 的要求。因此，即使您直接撰寫 JDBC 程式碼，也需要指定此型別——但僅限於允許 Null 的值。 |
| `typeHandler` | 我們之前在文件中討論過預設型別處理器。使用此屬性，您可以在每個映射的基礎上覆寫預設型別處理器。其值可以是 TypeHandler 實作的完整限定類別名稱，也可以是型別別名。 |
| `select` | 另一個映射陳述式的 ID，用來載入此屬性映射所需的複雜型別。從 column 屬性指定之欄位擷取的值將作為參數傳遞給目標 select 陳述式。詳情請參見 association 元素。 |
| `resultMap` | 這是 ResultMap 的 ID，可以將此引數的巢狀結果映射至適當的物件圖（object graph）。這是不呼叫另一個 select 陳述式的替代方案。它允許您將多個資料表聯結在一起成為單一 `ResultSet`。這樣的 `ResultSet` 將包含重複的資料群組，需要對其進行解構並正確映射至巢狀物件圖。為了促成這點，MyBatis 允許您將多個 result map 串聯（chain）在一起，以處理巢狀結果。詳情請參見後續的 association 元素。 |
| `name` | 建構函式參數的名稱。指定 name 允許您按任意循序撰寫 arg 元素。請參閱上述說明。自 3.4.3 起。 |

<a id="nested-results-for-association-or-collection"></a>
##### association 或 collection 的巢狀結果 (Nested Results for association or collection)

雖然以下各小節將說明如何將 `association` 與 `collection` 用於巢狀查詢（Nested selects）與巢狀結果（Nested results），但自 3.6.0 起，我們現在可以使用 `constructor` 映射來注入這兩者。

請看以下範例：

```java
public class User {
   //...
   public User(Integer id, String username, List<UserRole> userRoles) {
     //...
   }
}

public class UserRole {
    // ...
    public UserRole(Integer id, String role) {
      // ...
    }
}
```

我們可以將 `UserRole` 映射為巢狀結果，MyBatis 將等待該列完全「完成」之後再建立物件，這意味著在 `User` 建立時，`userRoles` 將已完整且無法再被修改。

```xml
<resultMap id="userResultMap" type="User">
  <constructor>
    <idArg column="id" javaType="int" />
    <arg column="username" javaType="String" />
    <arg javaType="List" resultMap="userRoleResultMap" columnPrefix="role_"/>
  </constructor>
</resultMap>
```

為了在此範例中達成完全不可變的物件，我們也可以對 `UserRole` 使用建構函式注入：

```xml
<resultMap id="userRoleResultMap" type="UserRole">
  <constructor>
    <idArg column="id" javaType="int" />
    <arg column="role" javaType="String" />
  </constructor>
</resultMap>
```

必須明確告知 MyBatis 結果已經過適當排序，這樣當從結果集中檢索到新的主列時，將不再出現任何先前的列結果。這可以在陳述式中使用 `resultOrdered` 屬性進行設定：

```xml
<select id="getAllUsers" resultMap="userResultMap" resultOrdered="true">
    select
      u.id,
      u.username,
      r.id as role_id,
      r.role as role_role
    from user u
      left join user_role ur on u.id = ur.user_id
      inner join role r on r.id = ur.role_id
    order by u.id, r.id
</select>
```

請注意，指定 `order by` 可以正確排序結果。我們可以想像輸出結果類似於：

| row_nr | u.id | u.username | r.id | r.role |
|---|---|---|---|---|
| 1 | 1 | John | 1 | Admins |
| 2 | 1 | John | 2 | Users |
| 3 | 2 | Jack | null | null |
| 4 | 3 | Peter | 2 | Users |
| 5 | 3 | Peter | 3 | Maintainers |
| 6 | 3 | Peter | 4 | Approvers |

執行此查詢後，我們將獲得以下結果：

```
User{username=John, roles=[Admins, Users]}
User{username=Jack, roles=[]}
User{username=Peter, roles=[Users, Maintainers, Approvers]}
```

如果此處的第 5 列不知何故出現在第 1 列下方（透過某種 `ORDER BY`），MyBatis 將無法使用建構函式集合映射來正確完整建構 `John` 使用者。

值得注意的是，混合映射的支援有限，即屬性映射與巢狀建構函式映射混合使用很可能會失敗。
當使用此功能時，建議整個映射階層都使用不可變的建構函式映射。

<a id="association"></a>
#### association

```xml
<association property="author" javaType="Author">
  <id property="id" column="author_id"/>
  <result property="username" column="author_username"/>
</association>
```

association 元素處理「擁有一個」（has-one）形式的關係。例如在我們的範例中，一個 Blog 只有一位 Author。關聯映射的大部分運作方式與其他結果映射相同。您指定目標屬性、該屬性的 `javaType`（MyBatis 大多數時候可以自行推斷）、必要時的 `jdbcType`，以及若您想覆寫結果值擷取邏輯時的 `typeHandler`。

association 的不同之處在於您需要告知 MyBatis 如何載入該關聯。MyBatis 可以透過兩種不同的方式來完成：

- 巢狀查詢（Nested Select）：透過執行另一個回傳所需複雜型別的映射 SQL 陳述式。
- 巢狀結果（Nested Results）：透過使用巢狀結果映射來處理聯結結果中的重複子集。

首先，讓我們檢視該元素的屬性。正如您將看到的，它與一般的結果映射僅在 select 與 resultMap 屬性上有所不同。

| 屬性 | 說明 |
|---|---|
| `property` | 將欄位結果映射至的欄位或屬性。若給定名稱存在對應的 JavaBeans 屬性，則會優先使用。否則，MyBatis 將尋找給定名稱的欄位。在這兩種情況下，您都可以使用一般的點標記法進行複雜的屬性導覽。例如，您可以映射至簡單的屬性如：`username`，或較複雜的屬性如：`address.street.number`。 |
| `javaType` | 完整限定的 Java 類別名稱或型別別名（內建型別別名清單請參見前面的表格）。若映射至 JavaBean，MyBatis 通常可以推斷型別。但是，如果您映射至 `HashMap`，則應明確指定 javaType 以確保預期行為。 |
| `jdbcType` | 下表所列支援型別中的 JDBC 型別。在 insert、update 或 delete 時，只有允許為 Null 的欄位才需要 JDBC 型別。這是 JDBC 的要求，而非 MyBatis 的要求。因此，即使您直接撰寫 JDBC 程式碼，也需要指定此型別——但僅限於允許 Null 的值。 |
| `typeHandler` | 我們之前在文件中討論過預設型別處理器。使用此屬性，您可以在每個映射的基礎上覆寫預設型別處理器。其值可以是 TypeHandler 實作的完整限定類別名稱，也可以是型別別名。 |

<a id="nested-select-for-association"></a>
#### 關聯的巢狀查詢 (Nested Select for Association)

| 屬性 | 說明 |
|---|---|
| `column` | 資料庫中的欄位名稱，或別名欄位標籤，其中保存的值將作為輸入參數傳遞給巢狀陳述式。這與通常傳遞給 `resultSet.getString(columnName)` 的字串相同。注意：若要處理複合索引鍵，您可以使用語法 `column="{prop1=col1,prop2=col2}"` 指定多個欄位名稱傳遞給巢狀 select 陳述式。這會使 `prop1` 與 `prop2` 在目標巢狀 select 陳述式的參數物件中被設定。 |
| `select` | 另一個映射陳述式的 ID，用來載入此屬性映射所需的複雜型別。從 column 屬性指定之欄位擷取的值將作為參數傳遞給目標 select 陳述式。此表後方提供了詳細範例。注意：若要處理複合索引鍵，您可以使用語法 `column="{prop1=col1,prop2=col2}"` 指定多個欄位名稱傳遞給巢狀 select 陳述式。這會使 `prop1` 與 `prop2` 在目標巢狀 select 陳述式的參數物件中被設定。 |
| `fetchType` | 選填。有效值為 `lazy` 與 `eager`。若設定此屬性，它會覆寫此映射的全域設定參數 `lazyLoadingEnabled`。 |

例如：

```xml
<resultMap id="blogResult" type="Blog">
  <association property="author" column="author_id" javaType="Author" select="selectAuthor"/>
</resultMap>

<select id="selectBlog" resultMap="blogResult">
  SELECT * FROM BLOG WHERE ID = #{id}
</select>

<select id="selectAuthor" resultType="Author">
  SELECT * FROM AUTHOR WHERE ID = #{id}
</select>
```

就是這樣。我們有兩個 select 陳述式：一個用於載入 Blog，另一個用於載入 Author，而 Blog 的 resultMap 描述了應使用 `selectAuthor` 陳述式來載入其 author 屬性。

假設欄位與屬性名稱相符，所有其他屬性都將自動載入。

雖然這種方法很簡單，但對於大型資料集或清單而言效能不佳。這個問題被稱為「N+1 查詢問題」（N+1 Selects Problem）。簡而言之，N+1 查詢問題的產生原因如下：

- 您執行了單一 SQL 陳述式來檢索記錄清單（即 "+1"）。
- 對於回傳的每筆記錄，您都執行一個 select 陳述式來載入各自的詳細資料（即 "N"）。

此問題可能導致執行數百或數千個 SQL 陳述式。這通常不是所希望的。

好處是 MyBatis 可以延遲載入（lazy load）此類查詢，因此您可以避免一次性承受所有這些陳述式的開銷。但是，如果您載入了這樣的清單並立即對其進行反覆運算以存取巢狀資料，您將觸發所有的延遲載入，效能可能會非常糟糕。

因此，還有另一種方式。

<a id="nested-results-for-association"></a>
#### 關聯的巢狀結果 (Nested Results for Association)

| 屬性 | 說明 |
|---|---|
| `resultMap` | 這是 ResultMap 的 ID，可以將此關聯的巢狀結果映射至適當的物件圖。這是不呼叫另一個 select 陳述式的替代方案。它允許您將多個資料表聯結在一起成為單一 ResultSet。這樣的 ResultSet 將包含重複的資料群組，需要對其進行解構並正確映射至巢狀物件圖。為了促成這點，MyBatis 允許您將多個 result map 串聯在一起，以處理巢狀結果。後面的範例將更容易理解。 |
| `columnPrefix` | 聯結多個資料表時，您必須使用欄位別名以避免 ResultSet 中出現重複的欄位名稱。指定 columnPrefix 允許您將此類欄位映射至外部 resultMap。請參閱本節後續說明的範例。 |
| `notNullColumn` | 預設情況下，僅當映射至子物件屬性的欄位中至少有一個不為 null 時，才會建立子物件。透過此屬性，您可以透過指定哪些欄位必須具有值來變更此行為，如此一來，只有當這些欄位中的任何一個不為 null 時，MyBatis 才會建立子物件。可以使用逗號作為分隔符號來指定多個欄位名稱。預設值：unset。 |
| `autoMapping` | 若設定此屬性，MyBatis 在將結果映射至此屬性時將啟用或停用自動映射。此屬性會覆寫全域的 autoMappingBehavior。請注意，它對外部 resultMap 沒有影響，因此將其與 `select` 或 `resultMap` 屬性一起使用是沒有意義的。預設值：unset。 |

您之前已經看過上面非常複雜的巢狀關聯範例。以下是一個簡單得多的範例來說明其運作方式。我們不會執行個別的陳述式，而是將 Blog 與 Author 資料表聯結在一起，如下所示：

```xml
<select id="selectBlog" resultMap="blogResult">
  select
    B.id            as blog_id,
    B.title         as blog_title,
    B.author_id     as blog_author_id,
    A.id            as author_id,
    A.username      as author_username,
    A.password      as author_password,
    A.email         as author_email,
    A.bio           as author_bio
  from Blog B left outer join Author A on B.author_id = A.id
  where B.id = #{id}
</select>
```

請注意聯結（join），以及為了確保所有結果都具有唯一且清晰的別名所做的努力。這使得映射變得更加容易。現在我們可以映射結果了：

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="blog_id" />
  <result property="title" column="blog_title"/>
  <association property="author" resultMap="authorResult" />
</resultMap>

<resultMap id="authorResult" type="Author">
  <id property="id" column="author_id"/>
  <result property="username" column="author_username"/>
  <result property="password" column="author_password"/>
  <result property="email" column="author_email"/>
  <result property="bio" column="author_bio"/>
</resultMap>
```

在上面的範例中，您可以看到 Blog 的 "author" 關聯委派給 "authorResult" resultMap 來載入 Author 實例。

<span class="label important">Very Important</span> id 元素在巢狀結果映射中扮演著非常關鍵的角色。您應該始終指定一個或多個可用於唯一識別結果的屬性。事實上，即使您忽略它，MyBatis 仍然可以運作，但會付出嚴重的效能代價。請儘量選擇最少且能唯一識別結果的屬性。主鍵（Primary Key）顯然是最佳選擇（即使是複合主鍵）。

現在，上面的範例使用了外部 resultMap 元素來映射關聯。這使得 Author resultMap 可以重複使用。但是，如果您不需要重複使用它，或者您只是偏好將結果映射集中到單一描述性 resultMap 中，您可以巢狀定義關聯結果映射。以下是使用此方法的相同範例：

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="blog_id" />
  <result property="title" column="blog_title"/>
  <association property="author" javaType="Author">
    <id property="id" column="author_id"/>
    <result property="username" column="author_username"/>
    <result property="password" column="author_password"/>
    <result property="email" column="author_email"/>
    <result property="bio" column="author_bio"/>
  </association>
</resultMap>
```

如果部落格有共同作者（co-author）該怎麼辦？select 陳述式看起來會像這樣：

```xml
<select id="selectBlog" resultMap="blogResult">
  select
    B.id            as blog_id,
    B.title         as blog_title,
    A.id            as author_id,
    A.username      as author_username,
    A.password      as author_password,
    A.email         as author_email,
    A.bio           as author_bio,
    CA.id           as co_author_id,
    CA.username     as co_author_username,
    CA.password     as co_author_password,
    CA.email        as co_author_email,
    CA.bio          as co_author_bio
  from Blog B
  left outer join Author A on B.author_id = A.id
  left outer join Author CA on B.co_author_id = CA.id
  where B.id = #{id}
</select>
```

回想一下 Author 的 resultMap 定義如下：

```xml
<resultMap id="authorResult" type="Author">
  <id property="id" column="author_id"/>
  <result property="username" column="author_username"/>
  <result property="password" column="author_password"/>
  <result property="email" column="author_email"/>
  <result property="bio" column="author_bio"/>
</resultMap>
```

因為結果中的欄位名稱與 resultMap 中定義的欄位不同，所以您需要指定 `columnPrefix` 來重複使用 resultMap 以映射共同作者的結果。

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="blog_id" />
  <result property="title" column="blog_title"/>
  <association property="author"
    resultMap="authorResult" />
  <association property="coAuthor"
    resultMap="authorResult"
    columnPrefix="co_" />
</resultMap>
```

<a id="nested-cursor-for-association"></a>
#### 關聯的巢狀游標 (Nested Cursor for Association)

某些資料庫可以將 `java.sql.ResultSet` 作為欄位值回傳。
以下是陳述式與結果映射範例：

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="id" />
  <result property="title" column="title" />
  <association property="author" column="author" jdbcType="CURSOR">
    <id property="id" column="id" />
    <result property="username" column="username" />
  </association>
</resultMap>

<select id="selectBlog" resultMap="blogResult">
  select b.id, b.name, cursor(
    select a.id, a.username from author a where b.author_id = a.id
  ) author from blog b
</select>
```

與前一節中的範例相比，關鍵差異在於 `<association>` 元素中的 `jdbcType` 屬性。
其值 `CURSOR` 表示欄位 `author` 的值是巢狀游標。

<a id="multiple-resultsets-for-association"></a>
#### 關聯的多個結果集 (Multiple ResultSets for Association)

| 屬性 | 說明 |
|---|---|
| `column` | 當使用多個結果集時，此屬性指定將與 `foreignColumn` 相關聯的欄位（以逗號分隔），以識別關係的父代與子代。 |
| `foreignColumn` | 識別包含外部索引鍵（Foreign Key）的欄位名稱，該外部索引鍵的值將與父代型別的 `column` 屬性中指定之欄位值進行比對。 |
| `resultSet` | 識別將從中載入此複雜型別的結果集名稱。 |

從 3.2.3 版開始，MyBatis 提供了另一種解決 N+1 問題的方法。

某些資料庫允許預存程序（stored procedures）回傳多個結果集，或者一次執行多個陳述式並為每個陳述式回傳一個結果集。這可用於僅存取一次資料庫即可回傳相關資料，而無需使用聯結。

在此範例中，預存程序執行以下查詢並回傳兩個結果集。第一個包含 Blogs，第二個包含 Authors。

```sql
SELECT * FROM BLOG WHERE ID = #{id}

SELECT * FROM AUTHOR WHERE ID = #{id}
```

必須在映射陳述式中新增 `resultSets` 屬性，並提供以逗號分隔的名稱清單，為每個結果集指定名稱。

```xml
<select id="selectBlog" resultSets="blogs,authors" resultMap="blogResult" statementType="CALLABLE">
  {call getBlogsAndAuthors(#{id,jdbcType=INTEGER,mode=IN})}
</select>
```

現在我們可以指定填入 "author" 關聯的資料來自名為 "authors" 的結果集：

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="id" />
  <result property="title" column="title"/>
  <association property="author" javaType="Author" resultSet="authors" column="author_id" foreignColumn="id">
    <id property="id" column="id"/>
    <result property="username" column="username"/>
    <result property="password" column="password"/>
    <result property="email" column="email"/>
    <result property="bio" column="bio"/>
  </association>
</resultMap>
```

您在上面已經看到了如何處理「擁有一個」形式的關聯。但是「擁有多個」（has many）呢？這就是下一節的主題。

<a id="collection"></a>
#### collection

```xml
<collection property="posts" ofType="domain.blog.Post">
  <id property="id" column="post_id"/>
  <result property="subject" column="post_subject"/>
  <result property="body" column="post_body"/>
</collection>
```

collection 元素的運作方式幾乎與 association 完全相同。事實上，兩者極其相似，記錄相同之處會顯得冗贅。因此讓我們聚焦於差異點。

繼續上面的範例，一個 Blog 只有一位 Author。但是一個 Blog 擁有許多 Post。在 Blog 類別上，這將由類似以下程式碼表示：

```java
private List<Post> posts;
```

為了將一組巢狀結果映射至這樣的 List，我們使用 collection 元素。就像 association 元素一樣，我們可以使用巢狀查詢，或是來自聯結的巢狀結果。

<a id="nested-select-for-collection"></a>
#### 集合的巢狀查詢 (Nested Select for Collection)

首先，讓我們看看使用巢狀查詢來載入 Blog 的 Post。

```xml
<resultMap id="blogResult" type="Blog">
  <collection property="posts" javaType="ArrayList" column="id" ofType="Post" select="selectPostsForBlog"/>
</resultMap>

<select id="selectBlog" resultMap="blogResult">
  SELECT * FROM BLOG WHERE ID = #{id}
</select>

<select id="selectPostsForBlog" resultType="Post">
  SELECT * FROM POST WHERE BLOG_ID = #{id}
</select>
```

有幾件事您會立即注意到，但在大多數情況下，它看起來與我們在上面學到的 association 元素非常相似。首先，您會注意到我們正在使用 collection 元素。接著您會發現有一個新的 "ofType" 屬性。需要此屬性來區分 JavaBean（或欄位）屬性型別與集合所包含的型別。因此，您可以像這樣解讀以下映射：

```xml
<collection property="posts" javaType="ArrayList" column="id" ofType="Post" select="selectPostsForBlog"/>
```

解讀為：「Post 型別之 ArrayList 中的 posts 集合。」

`javaType` 屬性通常是不必要的，因為 MyBatis 在大多數情況下會為您推斷出這一點。因此，您通常可以將其簡化為：

```xml
<collection property="posts" column="id" ofType="Post" select="selectPostsForBlog"/>
```

<a id="nested-results-for-collection"></a>
#### 集合的巢狀結果 (Nested Results for Collection)

到目前為止，您可能已經猜到了集合的巢狀結果將如何運作，因為它與 association 完全相同，只是多加了 `ofType` 屬性。

首先，讓我們看一下 SQL：

```xml
<select id="selectBlog" resultMap="blogResult">
  select
  B.id as blog_id,
  B.title as blog_title,
  B.author_id as blog_author_id,
  P.id as post_id,
  P.subject as post_subject,
  P.body as post_body
  from Blog B
  left outer join Post P on B.id = P.blog_id
  where B.id = #{id}
</select>
```

同樣地，我們聯結了 Blog 與 Post 資料表，並仔細處理了結果欄位標籤以實現簡單映射。現在將 Blog 與其 Post 集合映射就像以下程式碼一樣簡單：

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="blog_id" />
  <result property="title" column="blog_title"/>
  <collection property="posts" ofType="Post">
    <id property="id" column="post_id"/>
    <result property="subject" column="post_subject"/>
    <result property="body" column="post_body"/>
  </collection>
</resultMap>
```

同樣地，請記住此處 id 元素的重要性，若您尚未閱讀，請參閱上面的 association 章節。

此外，若您偏好提高結果映射可重複使用性的長格式，您可以使用以下替代映射：

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="blog_id" />
  <result property="title" column="blog_title"/>
  <collection property="posts" ofType="Post" resultMap="blogPostResult" columnPrefix="post_"/>
</resultMap>

<resultMap id="blogPostResult" type="Post">
  <id property="id" column="id"/>
  <result property="subject" column="subject"/>
  <result property="body" column="body"/>
</resultMap>
```

<a id="nested-cursor-for-collection"></a>
#### 集合的巢狀游標 (Nested Cursor for Collection)

顯而易見地，巢狀游標可以回傳多列資料。
就像 `<association>` 一樣，您只需要在 `<collection>` 元素中指定 `jdbcType="CURSOR"`。

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="id" />
  <result property="title" column="title" />
  <collection property="posts" column="posts" jdbcType="CURSOR">
    <id property="id" column="id" />
    <result property="subject" column="subject" />
    <result property="body" column="body" />
  </collection>
</resultMap>

<select id="selectBlog" resultMap="blogResult">
  select b.id, b.name, cursor(
    select p.id, p.subject, p.body from post p where p.blog_id = b.id
  ) posts from blog b
</select>
```

<a id="multiple-resultsets-for-collection"></a>
#### 集合的多個結果集 (Multiple ResultSets for Collection)

就像我們對 association 所做的那樣，我們可以呼叫一個預存程序，該程序執行兩個查詢並回傳兩個結果集，一個包含 Blogs，另一個包含 Posts：

```sql
SELECT * FROM BLOG WHERE ID = #{id}

SELECT * FROM POST WHERE BLOG_ID = #{id}
```

必須在映射陳述式中新增 `resultSets` 屬性，並提供以逗號分隔的名稱清單，為每個結果集指定名稱。

```xml
<select id="selectBlog" resultSets="blogs,posts" resultMap="blogResult">
  {call getBlogsAndPosts(#{id,jdbcType=INTEGER,mode=IN})}
</select>
```

我們指定 "posts" 集合將從名為 "posts" 的結果集中填入資料：

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="id" />
  <result property="title" column="title"/>
  <collection property="posts" ofType="Post" resultSet="posts" column="id" foreignColumn="blog_id">
    <id property="id" column="id"/>
    <result property="subject" column="subject"/>
    <result property="body" column="body"/>
  </collection>
</resultMap>
```

<span class="label important">NOTE</span> 您映射的關聯與集合在深度、廣度或組合上沒有限制。在映射它們時，應當考量效能。對應用程式進行單元測試與效能測試，對於發現適合應用程式的最佳方法大有助益。令人欣慰的是，MyBatis 允許您稍後改變心意，而對程式碼幾乎沒有（甚至完全沒有）影響。

進階的 association 與 collection 映射是一個深奧的主題。文件只能引領您入門。透過一些實踐，一切很快就會變得清晰明瞭。

<a id="discriminator"></a>
#### 鑑別器 (discriminator)

```xml
<discriminator javaType="int" column="draft">
  <case value="1" resultType="DraftPost"/>
</discriminator>
```

有時單一資料庫查詢可能會回傳許多不同（但希望多少相關）資料型別的結果集。discriminator 元素就是專門為處理這種情況以及包括類別繼承階層在內的其他情境而設計的。discriminator 非常容易理解，因為它的行為很像 Java 中的 switch 陳述式。

鑑別器定義指定了 column 與 javaType 屬性。column 是 MyBatis 將尋找要比較之值的位置。需要 javaType 是為了確保執行適當型別的相等性測試（儘管 String 可能適用於幾乎所有情況）。例如：

```xml
<resultMap id="vehicleResult" type="Vehicle">
  <id property="id" column="id" />
  <result property="vin" column="vin"/>
  <result property="year" column="year"/>
  <result property="make" column="make"/>
  <result property="model" column="model"/>
  <result property="color" column="color"/>
  <discriminator javaType="int" column="vehicle_type">
    <case value="1" resultMap="carResult"/>
    <case value="2" resultMap="truckResult"/>
    <case value="3" resultMap="vanResult"/>
    <case value="4" resultMap="suvResult"/>
  </discriminator>
</resultMap>
```

在此範例中，MyBatis 將從結果集中檢索每筆記錄並比較其 vehicle_type 值。如果它與任何鑑別器 case 相符，則將使用該 case 指定的 `resultMap`。這是排他性處理的，換句話說，resultMap 的其餘部分將被忽略（除非它被擴充繼承，我們稍後會討論）。如果沒有任何 case 匹配，MyBatis 只會使用鑑別器區塊外部定義的 resultMap。因此，如果 carResult 宣告如下：

```xml
<resultMap id="carResult" type="Car">
  <result property="doorCount" column="door_count" />
</resultMap>
```

那麼將「僅」載入 doorCount 屬性。這樣做是為了允許完全獨立的鑑別器 case 群組，甚至是與父 resultMap 沒有關係的群組。在此情況下，我們當然知道汽車與車輛之間存在關係，因為 Car 是一種（is-a）Vehicle。因此，我們希望其餘屬性也能被載入。對 resultMap 進行一處簡單修改即可達成：

```xml
<resultMap id="carResult" type="Car" extends="vehicleResult">
  <result property="doorCount" column="door_count" />
</resultMap>
```

現在，來自 vehicleResult 與 carResult 的所有屬性都將被載入。

不過同樣地，有些人可能會覺得這種外部定義 map 的方式有些繁瑣。因此，對於喜歡更簡潔映射風格的人來說，還有另一種替代語法。例如：

```xml
<resultMap id="vehicleResult" type="Vehicle">
  <id property="id" column="id" />
  <result property="vin" column="vin"/>
  <result property="year" column="year"/>
  <result property="make" column="make"/>
  <result property="model" column="model"/>
  <result property="color" column="color"/>
  <discriminator javaType="int" column="vehicle_type">
    <case value="1" resultType="Car">
      <result property="doorCount" column="door_count" />
    </case>
    <case value="2" resultType="Truck">
      <result property="boxSize" column="box_size" />
      <result property="extendedCab" column="extended_cab" />
    </case>
    <case value="3" resultType="Van">
      <result property="powerSlidingDoor" column="power_sliding_door" />
    </case>
    <case value="4" resultType="SUV">
      <result property="allWheelDrive" column="all_wheel_drive" />
    </case>
  </discriminator>
</resultMap>
```

<span class="label important">NOTE</span> 請記住，這些全都是 Result Map，如果您根本沒有指定任何 result，那麼 MyBatis 將自動為您匹配欄位與屬性。因此大多數這些範例都比實際需要的更為詳細。話雖如此，大多數資料庫都相當複雜，我們不太可能在所有情況下都依賴自動匹配。

<a id="auto-mapping"></a>
### 自動映射 (Auto-mapping)

正如您在前面的章節中所見，在簡單情況下 MyBatis 可以為您自動映射結果，而在其他情況下您需要建構 result map。但正如您將在本節中看到的，您也可以混合使用這兩種策略。讓我們深入了解自動映射的運作方式。

當自動映射結果時，MyBatis 將取得欄位名稱並尋找具有相同名稱的屬性（忽略大小寫）。這意味著如果找到名為 *ID* 的欄位與名為 *id* 的屬性，MyBatis 將把 *id* 屬性設定為 *ID* 欄位的值。

通常資料庫欄位使用大寫字母和單詞之間的底線命名，而 Java 屬性通常遵循駝峰式命名慣例（camelCase）。若要啟用它們之間的自動映射，請將設定 `mapUnderscoreToCamelCase` 設為 true。

即使存在特定的 result map，自動映射也能運作。發生這種情況時，對於每個 result map，ResultSet 中存在且未進行手動映射的所有欄位都將被自動映射，然後處理手動映射。在以下範例中，*id* 與 *userName* 欄位將被自動映射，而 *hashed_password* 欄位將被手動映射。

```xml
<select id="selectUsers" resultMap="userResultMap">
  select
    user_id             as "id",
    user_name           as "userName",
    hashed_password
  from some_table
  where id = #{id}
</select>
```

```xml
<resultMap id="userResultMap" type="User">
  <result property="password" column="hashed_password"/>
</resultMap>
```

自動映射有三個層級：

- `NONE` - 停用自動映射。僅設定手動映射的屬性。
- `PARTIAL` - 自動映射結果，但在內部定義了巢狀結果映射（聯結）的屬性除外。
- `FULL` - 自動映射所有內容。

預設值為 `PARTIAL`，這樣設計是有充分理由的。當使用 `FULL` 時，在處理聯結結果時將執行自動映射，而聯結會在同一列中檢索多個不同實體的資料，因此這可能會導致非預期的映射。為了理解該風險，請看以下範例：

```xml
<select id="selectBlog" resultMap="blogResult">
  select
    B.id,
    B.title,
    A.username
  from Blog B left outer join Author A on B.author_id = A.id
  where B.id = #{id}
</select>
```

```xml
<resultMap id="blogResult" type="Blog">
  <association property="author" resultMap="authorResult"/>
</resultMap>

<resultMap id="authorResult" type="Author">
  <result property="username" column="author_username"/>
</resultMap>
```

使用此 result map，*Blog* 與 *Author* 都將被自動映射。但請注意，*Author* 具有一個 *id* 屬性，且 ResultSet 中有一個名為 *id* 的欄位，因此 Author 的 id 將被填入 Blog 的 id，而這並不是您所預期的。因此請謹慎使用 `FULL` 選項。

無論設定的自動映射層級為何，您都可以透過向特定 ResultMap 新增屬性 `autoMapping` 來啟用或停用該 ResultMap 的自動映射：

```xml
<resultMap id="userResultMap" type="User" autoMapping="false">
  <result property="password" column="hashed_password"/>
</resultMap>
```

<a id="cache"></a>
### 快取 (cache)

MyBatis 包含強大的交易性查詢快取功能，具有極高的可設定性與可自訂性。MyBatis 3 快取實作進行了許多變更，使其更為強大且更容易設定。

預設情況下，僅啟用本地工作階段快取（local session caching），專門用於在工作階段（session）持續期間快取資料。若要啟用全域二級快取（second level cache），您只需在 SQL 映射檔中新增一行：

```xml
<cache/>
```

字面上就這麼簡單。這一行簡單陳述式的效果如下：

- 映射陳述式檔案中所有 select 陳述式的結果都將被快取。
- 映射陳述式檔案中所有的 insert、update 與 delete 陳述式都會清除快取。
- 快取將使用最近最少使用（LRU, Least Recently Used）演算法進行收回（eviction）。
- 快取不會依據任何時間排程進行清除（即沒有 Flush Interval）。
- 快取將儲存 1024 個對清單或物件的參照（無論查詢方法回傳什麼）。
- 快取將被視為讀／寫快取，這意味著檢索到的物件不被共享，呼叫者可以安全地修改它們，而不會干擾其他呼叫者或執行緒的潛在修改。

<span class="label important">NOTE</span> 快取僅適用於 cache 標籤所在的映射檔中宣告的陳述式。如果您將 Java API 與 XML 映射檔結合使用，則預設不會快取在相應介面中宣告的陳述式。您需要使用 @CacheNamespaceRef 註解參照該快取區域。

所有這些屬性都可以透過 cache 元素的屬性進行修改。例如：

```xml
<cache
  eviction="FIFO"
  flushInterval="60000"
  size="512"
  readOnly="true"/>
```

這個更進階的設定建立了一個 FIFO 快取，每 60 秒清除一次，最多可儲存 512 個對結果物件或清單的參照，並且回傳的物件被視為唯讀（read-only），因此修改它們可能會導致不同執行緒中的呼叫者之間發生衝突。

可用的收回策略（eviction policies）如下：

- `LRU` – 最近最少使用（Least Recently Used）：移除最長時間未被使用的物件。
- `FIFO` – 先進先出（First In First Out）：按照物件進入快取的循序移除物件。
- `SOFT` – 軟參照（Soft Reference）：基於垃圾回收器狀態與軟參照規則移除物件。
- `WEAK` – 弱參照（Weak Reference）：基於垃圾回收器狀態與弱參照規則更積極地移除物件。

預設值為 LRU。

`flushInterval` 可以設定為任何正整數，應代表以毫秒（ms）為單位的合理時間長度。預設為未設定，因此不使用清除時間間隔，快取僅透過對陳述式的呼叫進行清除。

`size` 可以設定為任何正整數，請記住您快取的物件大小以及環境的可用記憶體資源。預設值為 1024。

`readOnly` 屬性可以設為 true 或 false。唯讀快取將向所有呼叫者回傳快取物件的相同實例。因此不應修改此類物件。這提供了顯著的效能優勢。讀寫快取將回傳快取物件的複本（透過序列化）。這較慢但更安全，因此預設為 false。

<span class="label important">NOTE</span> 二級快取是具交易性的（transactional）。這意味著當 SqlSession 以 commit 結束，或者當它以 rollback 結束但沒有執行 flushCache=true 的 insert/delete/update 時，快取才會被更新。

<a id="using-a-custom-cache"></a>
#### 使用自訂快取 (Using a Custom Cache)

除了以這些方式自訂快取之外，您還可以透過實作自己的快取或建立與其他第三方快取解決方案的配接器（adapter）來完全覆寫快取行為。

```xml
<cache type="com.domain.something.MyCustomCache"/>
```

此範例展示了如何使用自訂快取實作。在 type 屬性中指定的類別必須實作 org.apache.ibatis.cache.Cache 介面，並提供一個接收 String id 作為引數的建構函式。這個介面是 MyBatis 框架中較複雜的介面之一，但就其功能而言卻很簡單。

```java
public interface Cache {
  String getId();
  int getSize();
  void putObject(Object key, Object value);
  Object getObject(Object key);
  Object removeObject(Object key);
  void clear();
}
```

若要設定您的快取，只需向您的 Cache 實作新增 public JavaBeans 屬性，並透過 cache 元素傳遞屬性即可。例如，以下設定將呼叫您的 Cache 實作上名為 `setCacheFile(String file)` 的方法：

```xml
<cache type="com.domain.something.MyCustomCache">
  <property name="cacheFile" value="/tmp/my-custom-cache.tmp"/>
</cache>
```

您可以使用所有簡單型別的 JavaBeans 屬性，MyBatis 會進行轉換。並且您可以指定一個佔位符（例如 `${cache.file}`）來替換在[設定屬性](configuration.html#properties)中定義的值。

自 3.4.2 起，MyBatis 已支援在設定完所有屬性後呼叫初始化方法。如果您想使用此功能，請在您的自訂快取類別上實作 `org.apache.ibatis.builder.InitializingObject` 介面。

```java
public interface InitializingObject {
  void initialize() throws Exception;
}
```

<span class="label important">NOTE</span> 使用自訂快取時，上一節中的快取設定（如收回策略、讀寫等）將不適用。

請務必記住，快取設定與快取實例綁定至 SQL 映射檔的命名空間（namespace）。因此，與快取位於同一命名空間中的所有陳述式都受其約束。陳述式可以透過在每個陳述式的基礎上使用兩個簡單屬性來修改它們與快取互動的方式，或者將自己完全排除在外。預設情況下，陳述式的設定如下：

```xml
<select ... flushCache="false" useCache="true"/>
<insert ... flushCache="true"/>
<update ... flushCache="true"/>
<delete ... flushCache="true"/>
```

既然那是預設值，您顯然永遠不應該明確地以這種方式設定陳述式。相反地，只有在您想變更預設行為時，才設定 flushCache 與 useCache 屬性。例如，在某些情況下，您可能希望將特定 select 陳述式的結果排除在快取之外，或者您可能希望 select 陳述式清除快取。類似地，您可能有一些 update 陳述式在執行時不需要清除快取。

<a id="cache-ref"></a>
#### cache-ref

回想上一節，只有特定命名空間的快取才會被相同命名空間內的陳述式使用或清除。有時您可能希望在命名空間之間共享相同的快取設定與實例。在這種情況下，您可以使用 cache-ref 元素來參照另一個快取。

```xml
<cache-ref namespace="com.someone.application.data.SomeMapper"/>
```
