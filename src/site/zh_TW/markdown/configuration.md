title: MyBatis 3 | XML 設定
author: Clinton Begin

<h1 class="d-none">Avoid blank site</h1>

## XML 設定

MyBatis 的 XML 設定包含深入影響 MyBatis 行為的設定與屬性。設定文件的整體結構如下：

- configuration（組態設定）
  - [properties](#properties)（屬性）
  - [settings](#settings)（設定）
  - [typeAliases](#typealiases)（型別別名）
  - [typeHandlers](#typehandlers)（型別處理器）
  - [objectFactory](#objectfactory)（物件工廠）
  - [plugins](#plugins)（外掛模組）
  - [environments](#environments)（環境設定）
    - environment（環境）
      - transactionManager（交易管理員）
      - dataSource（資料來源）
  - [databaseIdProvider](#databaseidprovider)（資料庫廠商識別碼）
  - [mappers](#mappers)（映射器）

### properties

這些是可外部化、可替換的屬性，可以在典型的 Java 屬性檔（Properties 實例）中進行設定，或是透過 properties 元素的子元素傳入。例如：

```xml
<properties resource="org/mybatis/example/config.properties">
  <property name="username" value="dev_user"/>
  <property name="password" value="F2Fa3!33TYyg"/>
</properties>
```

這些屬性隨後可用於整個設定檔中，用來替換需要動態設定的數值。例如：

```xml
<dataSource type="POOLED">
  <property name="driver" value="${driver}"/>
  <property name="url" value="${url}"/>
  <property name="username" value="${username}"/>
  <property name="password" value="${password}"/>
</dataSource>
```

在此範例中，username 與 password 將被 properties 元素中設定的值所替換。driver 與 url 屬性則會被 config.properties 檔案中所包含的值所替換。這為設定提供了豐富的彈性選擇。

屬性也可以傳入 `SqlSessionFactoryBuilder.build()` 方法中。例如：

```java
SqlSessionFactory factory =
  new SqlSessionFactoryBuilder().build(reader, props);

// ... 或 ...

SqlSessionFactory factory =
  new SqlSessionFactoryBuilder().build(reader, environment, props);
```

若某個屬性存在於上述多個位置，MyBatis 會依照以下順序載入它們：

- 首先讀取 properties 元素主體中指定的屬性，
- 其次讀取從 properties 元素的 classpath 資源（resource）或 url 屬性載入的屬性，這會覆蓋先前已指定的任何同名屬性，
- 最後讀取作為方法參數傳入的屬性，這會覆蓋從 properties 主體以及 resource/url 屬性載入的任何同名屬性。

因此，優先權最高的屬性是作為方法參數傳入的屬性，其次是 resource/url 屬性，最後才是在 properties 元素主體中指定的屬性。

自 MyBatis 3.4.2 起，您可以在佔位符中指定預設值，如下所示：

```xml
<dataSource type="POOLED">
  <!-- ... -->
  <property name="username" value="${username:ut_user}"/> <!-- 若 'username' 屬性不存在，username 將使用 'ut_user' -->
</dataSource>
```

此功能預設為停用。若您要在佔位符中指定預設值，應新增如下的特殊屬性以啟用此功能：

```xml
<properties resource="org/mybatis/example/config.properties">
  <!-- ... -->
  <property name="org.apache.ibatis.parsing.PropertyParser.enable-default-value" value="true"/> <!-- 啟用此功能 -->
</properties>
```

<span class="label important">注意</span> 這會與屬性索引鍵中的 `":"` 字元（例如 `db:username`）或 SQL 定義中 OGNL 運算式的三元運算子（例如 `${tableName != null ? tableName : 'global_constants'}`）產生衝突。若您使用其中任一種並希望使用預設屬性值，則必須透過新增以下特殊屬性來變更預設值分隔符號：

```xml
<properties resource="org/mybatis/example/config.properties">
  <!-- ... -->
  <property name="org.apache.ibatis.parsing.PropertyParser.default-value-separator" value="?:"/> <!-- 變更預設值分隔符號 -->
</properties>
```

```xml
<dataSource type="POOLED">
  <!-- ... -->
  <property name="username" value="${db:username?:ut_user}"/>
</dataSource>
```

### settings

這些是極為重要的調整設定，能大幅修改 MyBatis 執行階段的行為。下表說明各項設定、其意涵以及預設值。

| 設定選項 | 說明 | 有效值 | 預設值 |
|------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| cacheEnabled                       | 全域啟用或停用此設定下任何映射器中設定的所有快取。                                                                                                                                                                                                                                                                                                                                                               | true &#124; false                                                                                                                          | true                                                  |
| lazyLoadingEnabled                 | 全域啟用或停用延遲載入。啟用時，所有關聯物件皆會延遲載入。此設定可透過個別關聯上的 `fetchType` 屬性覆寫。                                                                                                                                                                                                                                                                                                           | true &#124; false                                                                                                                          | false                                                 |
| aggressiveLazyLoading              | 啟用時，呼叫任何方法都會載入該物件的所有延遲載入屬性。否則，每個屬性僅在需要時才載入（另請參閱 `lazyLoadTriggerMethods`）。                                                                                                                                                                                                                                                                                         | true &#124; false                                                                                                                          | false（≤3.4.1 為 true）                               |
| ~~multipleResultSetsEnabled~~      | 已廢棄。此選項無任何效果。                                                                                                                                                                                                                                                                                                                                                                                       | true &#124; false                                                                                                                          | true                                                  |
| useColumnLabel                     | 使用欄位標籤（column label）取代欄位名稱（column name）。不同驅動程式在此方面的行為有所差異。請參閱驅動程式文件，或測試兩種模式以確定驅動程式的行為。                                                                                                                                                                                                                                                           | true &#124; false                                                                                                                          | true                                                  |
| useGeneratedKeys                   | 允許 JDBC 支援自動產生金鑰（generated keys）。需要相容的驅動程式。若設為 true，即使部分宣告不相容但實際可行的驅動程式（例如 Derby），也會強制使用自動產生金鑰。                                                                                                                                                                                                                                                   | true &#124; false                                                                                                                          | false                                                 |
| autoMappingBehavior                | 指定 MyBatis 是否以及如何自動將欄位映射至屬性/欄位。NONE 停用自動映射。PARTIAL 僅自動映射沒有巢狀結果映射定義的結果。FULL 自動映射任何複雜度的結果映射（包含巢狀或其他情況）。                                                                                                                                                                                                                                 | NONE, PARTIAL, FULL                                                                                                                        | PARTIAL                                               |
| autoMappingUnknownColumnBehavior   | 指定當偵測到自動映射目標中未知欄位（或未知屬性型別）時的行為。<ul><li>`NONE`：不執行任何動作</li><li>`WARNING`：輸出警告日誌（`'org.apache.ibatis.session.AutoMappingUnknownColumnBehavior'` 的日誌層級必須設為 `WARN`）</li><li>`FAILING`：映射失敗（拋出 `SqlSessionException`）</li></ul>請注意，當 `autoMappingBehavior` 設為 `FULL` 時，可能會出現誤判（false-positives）。                                             | NONE, WARNING, FAILING                                                                                                                     | NONE                                                  |
| defaultExecutorType                | 設定預設執行器。SIMPLE 執行器不具特殊行為。REUSE 執行器重複使用 PreparedStatement。BATCH 執行器重複使用陳述式並執行批次更新。                                                                                                                                                                                                                                                                                   | SIMPLE &#124; REUSE &#124; BATCH                                                                                                           | SIMPLE                                                |
| defaultStatementTimeout            | 設定驅動程式等待資料庫回應的逾時秒數。                                                                                                                                                                                                                                                                                                                                                                           | 任何正整數                                                                                                                                 | 未設定 (null)                                         |
| defaultFetchSize                   | 為驅動程式提供擷取資料列數（fetch size）的提示，以控制回傳結果的大小。此參數值可由個別查詢設定覆寫。                                                                                                                                                                                                                                                                                                             | 任何正整數                                                                                                                                 | 未設定 (null)                                         |
| defaultResultSetType               | 當省略個別陳述式設定時，指定預設的結果集捲動策略。（自 3.5.2 起）                                                                                                                                                                                                                                                                                                                                                | FORWARD_ONLY &#124; SCROLL_SENSITIVE &#124; SCROLL_INSENSITIVE &#124; DEFAULT（行為與「未設定」相同）                                      | 未設定 (null)                                         |
| safeRowBoundsEnabled               | 允許在巢狀陳述式上使用 RowBounds。若允許，請設為 false。                                                                                                                                                                                                                                                                                                                                                         | true &#124; false                                                                                                                          | false                                                 |
| safeResultHandlerEnabled           | 允許在巢狀陳述式上使用 ResultHandler。若允許，請設為 false。                                                                                                                                                                                                                                                                                                                                                     | true &#124; false                                                                                                                          | true                                                  |
| mapUnderscoreToCamelCase           | 啟用從傳統資料庫欄位名稱 A_COLUMN 到傳統 Java 駝峰式屬性名稱 aColumn 的自動映射。                                                                                                                                                                                                                                                                                                                                | true &#124; false                                                                                                                          | false                                                 |
| localCacheScope                    | MyBatis 使用本地快取（local cache）來防止循環參照並加速重複的巢狀查詢。預設值（SESSION）會在同一個 Session 期間快取所有執行的查詢。若設為 localCacheScope=STATEMENT，本地 Session 僅用於陳述式執行期間，對同一個 SqlSession 的兩次不同呼叫將不會共享資料。                                                                                                                                                 | SESSION &#124; STATEMENT                                                                                                                   | SESSION                                               |
| jdbcTypeForNull                    | 當未提供參數的具體 JDBC 型別時，指定 null 值的 JDBC 型別。某些驅動程式需要指定欄位的 JDBC 型別，但其他驅動程式可使用通用值（如 NULL、VARCHAR 或 OTHER）。                                                                                                                                                                                                                                                       | JdbcType 列舉。最常見的值為：NULL、VARCHAR 與 OTHER                                                                                        | OTHER                                                 |
| lazyLoadTriggerMethods             | 指定哪些物件方法會觸發延遲載入。                                                                                                                                                                                                                                                                                                                                                                                 | 以逗號分隔的方法名稱清單                                                                                                                   | equals,clone,hashCode,toString                        |
| defaultScriptingLanguage           | 指定動態 SQL 產生所使用的預設指令碼語言。                                                                                                                                                                                                                                                                                                                                                                         | 型別別名或完整類別名稱。                                                                                                                   | org.apache.ibatis.scripting.xmltags.XMLLanguageDriver |
| defaultEnumTypeHandler             | 指定列舉型別（Enum）預設使用的 `TypeHandler`。（自 3.4.5 起）                                                                                                                                                                                                                                                                                                                                                    | 型別別名或完整類別名稱。                                                                                                                   | org.apache.ibatis.type.EnumTypeHandler                |
| callSettersOnNulls                 | 指定當擷取的值為 null 時，是否呼叫 Setter 方法或 Map 的 put 方法。當您依賴 Map.keySet() 或 null 值初始化時非常有用。請注意，基本型別（如 int、boolean 等）不會被設為 null。                                                                                                                                                                                                                                   | true &#124; false                                                                                                                          | false                                                 |
| returnInstanceForEmptyRow          | 當回傳資料列的所有欄位皆為 NULL 時，MyBatis 預設會回傳 `null`。啟用此設定後，MyBatis 會改為回傳一個空的實例。請注意，此設定亦適用於巢狀結果（即集合 collection 與關聯 association）。（自 3.4.2 起）                                                                                                                                                                                                        | true &#124; false                                                                                                                          | false                                                 |
| logPrefix                          | 指定 MyBatis 在日誌記錄器名稱前附加的前置字串。                                                                                                                                                                                                                                                                                                                                                                  | 任何字串                                                                                                                                   | 未設定                                                |
| logImpl                            | 指定 MyBatis 應使用的日誌實作。若未提供此設定，將自動探索日誌實作。                                                                                                                                                                                                                                                                                                                                              | SLF4J &#124; LOG4J（自 3.5.9 起已廢棄）&#124; LOG4J2 &#124; JDK_LOGGING &#124; COMMONS_LOGGING &#124; STDOUT_LOGGING &#124; NO_LOGGING | 未設定                                                |
| proxyFactory                       | 指定 MyBatis 用於建立具備延遲載入能力物件的 Proxy 工具。                                                                                                                                                                                                                                                                                                                                                         | CGLIB（自 3.5.10 起已廢棄）&#124; JAVASSIST                                                                                                | JAVASSIST（MyBatis 3.3 或更新版本）                   |
| vfsImpl                            | 指定 VFS 實作類別。                                                                                                                                                                                                                                                                                                                                                                                              | 以逗號分隔的自訂 VFS 實作完整類別名稱。                                                                                                    | 未設定                                                |
| useActualParamName                 | 允許透過方法簽名中宣告的實際名稱來參照陳述式參數。若要使用此功能，專案必須在 Java 8 中以 `-parameters` 選項編譯。（自 3.4.1 起）                                                                                                                                                                                                                                                                                | true &#124; false                                                                                                                          | true                                                  |
| configurationFactory               | 指定提供 `Configuration` 實例的類別。回傳的 Configuration 實例用於載入還原序列化物件的延遲載入屬性。此類別必須包含簽名為 `static Configuration getConfiguration()` 的方法。（自 3.2.3 起）                                                                                                                                                                                                                   | 型別別名或完整類別名稱。                                                                                                                   | 未設定                                                |
| shrinkWhitespacesInSql             | 從 SQL 中移除多餘的空白字元。請注意，這也會影響 SQL 中的字串常值。（自 3.5.5 起）                                                                                                                                                                                                                                                                                                                                | true &#124; false                                                                                                                          | false                                                 |
| defaultSqlProviderType             | 指定持有 Provider 方法的 SQL Provider 類別（自 3.5.6 起）。當 SQL Provider 註解（例如 `@SelectProvider`）省略 `type`（或 `value`）屬性時，將套用此類別。                                                                                                                                                                                                                                                        | 型別別名或完整類別名稱                                                                                                                     | 未設定                                                |
| nullableOnForEach                  | 指定 `foreach` 標籤上 'nullable' 屬性的預設值。（自 3.5.9 起）                                                                                                                                                                                                                                                                                                                                                   | true &#124; false                                                                                                                          | false                                                 |
| argNameBasedConstructorAutoMapping | 套用建構函式自動映射時，使用引數名稱搜尋要映射的欄位，而非依賴欄位順序。（自 3.5.10 起）                                                                                                                                                                                                                                                                                                                         | true &#124; false                                                                                                                          | false                                                 |

以下是 settings 元素完整設定的範例：

```xml
<settings>
  <setting name="cacheEnabled" value="true"/>
  <setting name="lazyLoadingEnabled" value="true"/>
  <setting name="aggressiveLazyLoading" value="true"/>
  <setting name="useColumnLabel" value="true"/>
  <setting name="useGeneratedKeys" value="false"/>
  <setting name="autoMappingBehavior" value="PARTIAL"/>
  <setting name="autoMappingUnknownColumnBehavior" value="WARNING"/>
  <setting name="defaultExecutorType" value="SIMPLE"/>
  <setting name="defaultStatementTimeout" value="25"/>
  <setting name="defaultFetchSize" value="100"/>
  <setting name="safeRowBoundsEnabled" value="false"/>
  <setting name="safeResultHandlerEnabled" value="true"/>
  <setting name="mapUnderscoreToCamelCase" value="false"/>
  <setting name="localCacheScope" value="SESSION"/>
  <setting name="jdbcTypeForNull" value="OTHER"/>
  <setting name="lazyLoadTriggerMethods" value="equals,clone,hashCode,toString"/>
  <setting name="defaultScriptingLanguage" value="org.apache.ibatis.scripting.xmltags.XMLLanguageDriver"/>
  <setting name="defaultEnumTypeHandler" value="org.apache.ibatis.type.EnumTypeHandler"/>
  <setting name="callSettersOnNulls" value="false"/>
  <setting name="returnInstanceForEmptyRow" value="false"/>
  <setting name="logPrefix" value="exampleLogPreFix_"/>
  <setting name="logImpl" value="SLF4J | LOG4J | LOG4J2 | JDK_LOGGING | COMMONS_LOGGING | STDOUT_LOGGING | NO_LOGGING"/>
  <setting name="proxyFactory" value="CGLIB | JAVASSIST"/>
  <setting name="vfsImpl" value="org.mybatis.example.YourselfVfsImpl"/>
  <setting name="useActualParamName" value="true"/>
  <setting name="configurationFactory" value="org.mybatis.example.ConfigurationFactory"/>
</settings>
```

<a id="typealiases"></a>
### typeAliases

型別別名就是 Java 型別的簡短別名。它僅與 XML 設定相關，其存在的目的只是為了減少輸入完整類別名稱的重複與繁瑣。例如：

```xml
<typeAliases>
  <typeAlias alias="Author" type="domain.blog.Author"/>
  <typeAlias alias="Blog" type="domain.blog.Blog"/>
  <typeAlias alias="Comment" type="domain.blog.Comment"/>
  <typeAlias alias="Post" type="domain.blog.Post"/>
  <typeAlias alias="Section" type="domain.blog.Section"/>
  <typeAlias alias="Tag" type="domain.blog.Tag"/>
</typeAliases>
```

有了這個設定，`Blog` 現在便可用於任何原本能使用 `domain.blog.Blog` 的地方。

您也可以指定一個套件，讓 MyBatis 在其中搜尋 Bean。例如：

```xml
<typeAliases>
  <package name="domain.blog"/>
</typeAliases>
```

在 `domain.blog` 中找到的每個 Bean，若未找到註解，將使用該 Bean 的非完整限定小寫類別名稱註冊為別名。也就是說，`domain.blog.Author` 會被註冊為 `author`。若找到 `@Alias` 註解，則其設定值將被用作別名。請參見以下範例：

```java
@Alias("author")
public class Author {
    ...
}
```

MyBatis 針對常見的 Java 型別提供了許多內建的型別別名。它們均不區分大小寫，請注意基本型別因名稱多載而有的特殊處理。

| 別名 | 映射型別 |
|---------------------------|--------------|
| _byte                     | byte         |
| _char（自 3.5.10 起）     | char         |
| _character（自 3.5.10 起）| char         |
| _long                     | long         |
| _short                    | short        |
| _int                      | int          |
| _integer                  | int          |
| _double                   | double       |
| _float                    | float        |
| _boolean                  | boolean      |
| string                    | String       |
| byte                      | Byte         |
| char（自 3.5.10 起）      | Character    |
| character（自 3.5.10 起） | Character    |
| long                      | Long         |
| short                     | Short        |
| int                       | Integer      |
| integer                   | Integer      |
| double                    | Double       |
| float                     | Float        |
| boolean                   | Boolean      |
| date                      | Date         |
| decimal                   | BigDecimal   |
| bigdecimal                | BigDecimal   |
| biginteger                | BigInteger   |
| object                    | Object       |
| date[]                    | Date[]       |
| decimal[]                 | BigDecimal[] |
| bigdecimal[]              | BigDecimal[] |
| biginteger[]              | BigInteger[] |
| object[]                  | Object[]     |
| map                       | Map          |
| hashmap                   | HashMap      |
| list                      | List         |
| arraylist                 | ArrayList    |
| collection                | Collection   |
| iterator                  | Iterator     |

<a id="typehandlers"></a>
### typeHandlers

每當 MyBatis 在 PreparedStatement 上設定參數，或是從 ResultSet 中擷取數值時，都會使用 TypeHandler 以適合該 Java 型別的方式擷取數值。下表說明預設的 TypeHandler。

<span class="label important">注意</span> 自 3.4.5 版起，MyBatis 預設支援 JSR-310（日期與時間 API）。

| 型別處理器 | Java 型別 | JDBC 型別 |
|------------------------------|---------------------------------|----------------------------------------------------------------------------------------|
| `BooleanTypeHandler`         | `java.lang.Boolean`, `boolean`  | 任何相容的 `BOOLEAN`                                                                   |
| `ByteTypeHandler`            | `java.lang.Byte`, `byte`        | 任何相容的 `NUMERIC` 或 `BYTE`                                                         |
| `ShortTypeHandler`           | `java.lang.Short`, `short`      | 任何相容的 `NUMERIC` 或 `SMALLINT`                                                     |
| `IntegerTypeHandler`         | `java.lang.Integer`, `int`      | 任何相容的 `NUMERIC` 或 `INTEGER`                                                      |
| `LongTypeHandler`            | `java.lang.Long`, `long`        | 任何相容的 `NUMERIC` 或 `BIGINT`                                                       |
| `FloatTypeHandler`           | `java.lang.Float`, `float`      | 任何相容的 `NUMERIC` 或 `FLOAT`                                                        |
| `DoubleTypeHandler`          | `java.lang.Double`, `double`    | 任何相容的 `NUMERIC` 或 `DOUBLE`                                                       |
| `BigDecimalTypeHandler`      | `java.math.BigDecimal`          | 任何相容的 `NUMERIC` 或 `DECIMAL`                                                      |
| `StringTypeHandler`          | `java.lang.String`              | `CHAR`, `VARCHAR`                                                                      |
| `ClobReaderTypeHandler`      | `java.io.Reader`                | -                                                                                      |
| `ClobTypeHandler`            | `java.lang.String`              | `CLOB`, `LONGVARCHAR`                                                                  |
| `NStringTypeHandler`         | `java.lang.String`              | `NVARCHAR`, `NCHAR`                                                                    |
| `NClobTypeHandler`           | `java.lang.String`              | `NCLOB`                                                                                |
| `BlobInputStreamTypeHandler` | `java.io.InputStream`           | -                                                                                      |
| `ByteArrayTypeHandler`       | `byte[]`                        | 任何相容的位元組串流型別                                                               |
| `BlobTypeHandler`            | `byte[]`                        | `BLOB`, `LONGVARBINARY`                                                                |
| `DateTypeHandler`            | `java.util.Date`                | `TIMESTAMP`                                                                            |
| `DateOnlyTypeHandler`        | `java.util.Date`                | `DATE`                                                                                 |
| `TimeOnlyTypeHandler`        | `java.util.Date`                | `TIME`                                                                                 |
| `SqlTimestampTypeHandler`    | `java.sql.Timestamp`            | `TIMESTAMP`                                                                            |
| `SqlDateTypeHandler`         | `java.sql.Date`                 | `DATE`                                                                                 |
| `SqlTimeTypeHandler`         | `java.sql.Time`                 | `TIME`                                                                                 |
| `ObjectTypeHandler`          | 任何型別                        | `OTHER` 或未指定                                                                       |
| `EnumTypeHandler`            | 列舉型別                        | `VARCHAR` 任何字串相容型別，因為儲存的是代碼（而非索引）。                            |
| `EnumOrdinalTypeHandler`     | 列舉型別                        | 任何相容的 `NUMERIC` 或 `DOUBLE`，因為儲存的是序數位置（而非代碼本身）。               |
| `SqlxmlTypeHandler`          | `java.lang.String`              | `SQLXML`                                                                               |
| `InstantTypeHandler`         | `java.time.Instant`             | `TIMESTAMP`                                                                            |
| `LocalDateTimeTypeHandler`   | `java.time.LocalDateTime`       | `TIMESTAMP`                                                                            |
| `LocalDateTypeHandler`       | `java.time.LocalDate`           | `DATE`                                                                                 |
| `LocalTimeTypeHandler`       | `java.time.LocalTime`           | `TIME`                                                                                 |
| `OffsetDateTimeTypeHandler`  | `java.time.OffsetDateTime`      | `TIMESTAMP`                                                                            |
| `OffsetTimeTypeHandler`      | `java.time.OffsetTime`          | `TIME`                                                                                 |
| `ZonedDateTimeTypeHandler`   | `java.time.ZonedDateTime`       | `TIMESTAMP`                                                                            |
| `YearTypeHandler`            | `java.time.Year`                | `INTEGER`                                                                              |
| `MonthTypeHandler`           | `java.time.Month`               | `INTEGER`                                                                              |
| `YearMonthTypeHandler`       | `java.time.YearMonth`           | `VARCHAR` 或 `LONGVARCHAR`                                                             |
| `JapaneseDateTypeHandler`    | `java.time.chrono.JapaneseDate` | `DATE`                                                                                 |

您可以覆寫型別處理器或建立自己的處理器，以處理不支援或非標準的型別。若要執行此動作，請實作介面 `org.apache.ibatis.type.TypeHandler`，或繼承便利類別 `org.apache.ibatis.type.BaseTypeHandler`，並選擇性地將其映射至 JDBC 型別。例如：

```java
// ExampleTypeHandler.java
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ExampleTypeHandler extends BaseTypeHandler<String> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i,
    String parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, parameter);
  }

  @Override
  public String getNullableResult(ResultSet rs, String columnName)
    throws SQLException {
    return rs.getString(columnName);
  }

  @Override
  public String getNullableResult(ResultSet rs, int columnIndex)
    throws SQLException {
    return rs.getString(columnIndex);
  }

  @Override
  public String getNullableResult(CallableStatement cs, int columnIndex)
    throws SQLException {
    return cs.getString(columnIndex);
  }
}
```
```xml
<!-- mybatis-config.xml -->
<typeHandlers>
  <typeHandler handler="org.mybatis.example.ExampleTypeHandler"/>
</typeHandlers>
```

使用這樣的 TypeHandler 將會覆寫 Java String 屬性以及 VARCHAR 參數與結果的現有型別處理器。請注意，MyBatis 不會反思檢查資料庫中繼資料來判斷型別，因此您必須在參數與結果映射中明確指定該欄位為 VARCHAR，以便掛接正確的型別處理器。這是因為 MyBatis 在陳述式實際執行之前並不知曉資料型別。

MyBatis 會藉由反思檢查其泛型型別來得知您希望此 TypeHandler 處理的 Java 型別，但您可以透過兩種方式覆寫此行為：

- 在 typeHandler 元素中新增 `javaType` 屬性（例如：`javaType="String"`）
- 在您的 TypeHandler 類別中新增 `@MappedTypes` 註解，指定要與之關聯的 Java 型別清單。若已指定 `javaType` 屬性，此註解將會被忽略。

相關聯的 JDBC 型別可透過兩種方式指定：

- 在 typeHandler 元素中新增 `jdbcType` 屬性（例如：`jdbcType="VARCHAR"`）。
- 在您的 TypeHandler 類別中新增 `@MappedJdbcTypes` 註解，指定要與之關聯的 JDBC 型別清單。若已指定 `jdbcType` 屬性，此註解將會被忽略。

在決定於 `ResultMap` 中使用哪個 TypeHandler 時，Java 型別是已知的（來自結果型別），但 JDBC 型別是未知的。因此，MyBatis 會使用組合 `javaType=[TheJavaType], jdbcType=null` 來選擇 TypeHandler。這表示使用 `@MappedJdbcTypes` 註解會*限制* TypeHandler 的作用範圍，並使其在 `ResultMap` 中無法使用，除非明確指定。若要讓 TypeHandler 可在 `ResultMap` 中使用，請在 `@MappedJdbcTypes` 註解上設定 `includeNullJdbcType=true`。然而自 MyBatis 3.4.0 起，若僅註冊了**單一** TypeHandler 來處理某個 Java 型別，則在使用此 Java 型別的 `ResultMap` 中將預設使用該處理器（即即使未設定 `includeNullJdbcType=true` 亦同）。

最後，您可以讓 MyBatis 自動搜尋您的 TypeHandler：

```xml
<!-- mybatis-config.xml -->
<typeHandlers>
  <package name="org.mybatis.example"/>
</typeHandlers>
```

請注意，使用自動探索功能時，JDBC 型別只能透過註解來指定。

您可以建立能夠處理多個類別的泛型 TypeHandler。為此，請新增一個接收 Class 作為參數的建構函式，MyBatis 在建構該 TypeHandler 時便會傳入實際的類別。

```java
//GenericTypeHandler.java
public class GenericTypeHandler<E extends MyObject> extends BaseTypeHandler<E> {

  private Class<E> type;

  public GenericTypeHandler(Class<E> type) {
    if (type == null) throw new IllegalArgumentException("Type argument cannot be null");
    this.type = type;
  }
  ...
```

`EnumTypeHandler` 與 `EnumOrdinalTypeHandler` 即為泛型 TypeHandler。我們將在下一節深入探討它們。

### 處理列舉 (Handling Enums)

若您要映射 `Enum`，則需要使用 `EnumTypeHandler` 或 `EnumOrdinalTypeHandler`。

例如，假設我們需要儲存某個數值在四捨五入時所應使用的捨入模式。預設情況下，MyBatis 使用 `EnumTypeHandler` 將 `Enum` 數值轉換為其名稱。

**請注意，`EnumTypeHandler` 的特殊之處在於，與其他處理器不同，它不僅能處理單一特定類別，還能處理任何繼承自 `Enum` 的類別。**

然而，我們可能不想儲存名稱。我們的 DBA 可能堅持使用整數代碼。這同樣很簡單：在設定檔的 `typeHandlers` 中加入 `EnumOrdinalTypeHandler`，現在每個 `RoundingMode` 都會使用其序數值映射為整數。

```xml
<!-- mybatis-config.xml -->
<typeHandlers>
  <typeHandler handler="org.apache.ibatis.type.EnumOrdinalTypeHandler"
    javaType="java.math.RoundingMode"/>
</typeHandlers>
```

但如果您希望將同一個 `Enum` 在某處映射為字串，而在另一處映射為整數呢？

自動映射器會自動使用 `EnumOrdinalTypeHandler`，因此若我們希望換回使用普通傳統的 `EnumTypeHandler`，就必須透過為這些 SQL 陳述式明確設定要使用的型別處理器來告知它。

（映射檔在下一節才會涵蓋，因此若這是您第一次閱讀本文件，您可能希望暫時略過此處，稍後再回來查看。）

```xml
<!DOCTYPE mapper
    PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "https://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="org.apache.ibatis.submitted.rounding.Mapper">
    <resultMap type="org.apache.ibatis.submitted.rounding.User" id="usermap">
        <id column="id" property="id"/>
        <result column="name" property="name"/>
        <result column="funkyNumber" property="funkyNumber"/>
        <result column="roundingMode" property="roundingMode"/>
    </resultMap>

    <select id="getUser" resultMap="usermap">
        select * from users
    </select>
    <insert id="insert">
        insert into users (id, name, funkyNumber, roundingMode) values (
            #{id}, #{name}, #{funkyNumber}, #{roundingMode}
        )
    </insert>

    <resultMap type="org.apache.ibatis.submitted.rounding.User" id="usermap2">
        <id column="id" property="id"/>
        <result column="name" property="name"/>
        <result column="funkyNumber" property="funkyNumber"/>
        <result column="roundingMode" property="roundingMode"
         typeHandler="org.apache.ibatis.type.EnumTypeHandler"/>
    </resultMap>
    <select id="getUser2" resultMap="usermap2">
        select * from users2
    </select>
    <insert id="insert2">
        insert into users2 (id, name, funkyNumber, roundingMode) values (
            #{id}, #{name}, #{funkyNumber}, #{roundingMode, typeHandler=org.apache.ibatis.type.EnumTypeHandler}
        )
    </insert>

</mapper>
```

請注意，這迫使我們在 select 陳述式中使用 `resultMap` 而非 `resultType`。

<a id="objectfactory"></a>
### objectFactory

每次 MyBatis 建立結果物件的新實例時，都會使用 ObjectFactory 實例來執行。預設的 ObjectFactory 僅使用預設建構函式，或在存在參數映射時使用具參數的建構函式來實例化目標類別。若您希望覆寫 ObjectFactory 的預設行為，您可以自行建立。例如：

```java
// ExampleObjectFactory.java
public class ExampleObjectFactory extends DefaultObjectFactory {
  @Override
  public <T> T create(Class<T> type) {
    return super.create(type);
  }

  @Override
  public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    return super.create(type, constructorArgTypes, constructorArgs);
  }

  @Override
  public void setProperties(Properties properties) {
    super.setProperties(properties);
  }

  @Override
  public <T> boolean isCollection(Class<T> type) {
    return Collection.class.isAssignableFrom(type);
  }
}
```
```xml
<!-- mybatis-config.xml -->
<objectFactory type="org.mybatis.example.ExampleObjectFactory">
  <property name="someProperty" value="100"/>
</objectFactory>
```

ObjectFactory 介面非常單純。它包含兩個 create 方法，一個用於處理預設建構函式，另一個用於處理具參數的建構函式。最後，`setProperties` 方法可用於設定 ObjectFactory。在 ObjectFactory 實例初始化之後，定義於 objectFactory 元素主體內的屬性將會傳遞至 `setProperties` 方法。

### plugins

MyBatis 允許您在已映射陳述式執行的特定切入點攔截呼叫。預設情況下，MyBatis 允許外掛模組攔截以下類別的方法呼叫：

- Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
- ParameterHandler (getParameterObject, setParameters)
- ResultSetHandler (handleResultSets, handleOutputParameters)
- StatementHandler (prepare, parameterize, batch, update, query)

透過檢視各方法的完整簽名以及隨每個 MyBatis 版本提供的原始碼，可以了解這些類別方法的詳細資訊。假設您要做的不僅僅是監控呼叫，您應當深入了解所覆寫方法的行為。若您嘗試修改或覆寫特定方法的行為，很有可能會破壞 MyBatis 的核心運作。這些是低階類別與方法，因此請謹慎使用外掛模組。

鑑於外掛模組所提供的強大功能，使用外掛模組其實相當簡單。只需實作 Interceptor 介面，並務必指定您想要攔截的方法簽名。

```java
// ExamplePlugin.java
@Intercepts({@Signature(
  type= Executor.class,
  method = "update",
  args = {MappedStatement.class,Object.class})})
public class ExamplePlugin implements Interceptor {
  private Properties properties = new Properties();

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    // 若有需要，在此實作前置處理
    Object returnObject = invocation.proceed();
    // 若有需要，在此實作後置處理
    return returnObject;
  }

  @Override
  public void setProperties(Properties properties) {
    this.properties = properties;
  }
}
```
```xml
<!-- mybatis-config.xml -->
<plugins>
  <plugin interceptor="org.mybatis.example.ExamplePlugin">
    <property name="someProperty" value="100"/>
  </plugin>
</plugins>
```

上述外掛模組將攔截對 Executor 實例上 "update" 方法的所有呼叫，Executor 是一個負責已映射陳述式低階執行的內部物件。

<span class="label important">注意</span> **覆寫 Configuration 類別**

除了使用外掛模組修改 MyBatis 核心行為之外，您還可以完全覆寫 `Configuration` 類別。只需繼承它並覆寫其中的任何方法，然後將其傳入對 `SqlSessionFactoryBuilder.build(myConfig)` 方法的呼叫即可。然而，這同樣可能對 MyBatis 的行為產生嚴重的影響，因此請謹慎使用。

### environments

MyBatis 可以設定多個環境。這有助於您出於各種考量將 SQL 映射套用至多個資料庫。例如，您可能對開發、測試與正式環境擁有不同的設定。或者，您可能擁有多個共用相同結構綱要（Schema）的正式資料庫，並希望對兩者使用相同的 SQL 映射。這類使用情境非常廣泛。

**然而請記住一件重要的事情：雖然您可以設定多個環境，但每個 SqlSessionFactory 實例只能選擇「一個」環境。**

因此，若您想連接至兩個資料庫，就需要建立兩個 SqlSessionFactory 實例，各對應一個資料庫。對於三個資料庫，您需要三個實例，依此類推。這非常容易記住：

- **每個資料庫對應一個 SqlSessionFactory 實例**

若要指定要建置的環境，只需將其作為選擇性參數傳入 SqlSessionFactoryBuilder。接受環境參數的兩個簽名為：

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, environment);
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, environment, properties);
```

若省略環境參數，則會載入預設環境，如下所示：

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, properties);
```

environments 元素定義了環境的設定方式。

```xml
<environments default="development">
  <environment id="development">
    <transactionManager type="JDBC">
      <property name="..." value="..."/>
    </transactionManager>
    <dataSource type="POOLED">
      <property name="driver" value="${driver}"/>
      <property name="url" value="${url}"/>
      <property name="username" value="${username}"/>
      <property name="password" value="${password}"/>
    </dataSource>
  </environment>
</environments>
```

請注意此處的關鍵部分：

- 預設環境 ID（例如：default="development"）。
- 針對每個定義之環境的環境 ID（例如：id="development"）。
- TransactionManager 設定（例如：type="JDBC"）。
- DataSource 設定（例如：type="POOLED"）。

預設環境與環境 ID 顯而易見。您可以隨意為它們命名，只需確保預設值與其中一個相符即可。

**transactionManager**

MyBatis 內建了兩種 TransactionManager 型別（即 type="[JDBC|MANAGED]"）：

- JDBC – 此設定直接使用 JDBC 的 commit 與 rollback 功能。它依賴從 dataSource 擷取的連線來管理交易的作用範圍。預設情況下，它在關閉連線時會啟用 auto-commit，以與某些驅動程式相容。然而，對於某些驅動程式而言，啟用 auto-commit 不僅不必要，而且是一項昂貴的操作。因此，自 3.5.10 版起，您可以透過將 "skipSetAutoCommitOnClose" 屬性設為 true 來跳過此步驟。例如：

  ```xml
  <transactionManager type="JDBC">
    <property name="skipSetAutoCommitOnClose" value="true"/>
  </transactionManager>
  ```

- MANAGED – 此設定幾乎不執行任何動作。它從不提交（commit）或復原（rollback）連線。相反地，它讓容器來管理交易的完整生命週期（例如 JEE 應用程式伺服器環境）。預設情況下它確實會關閉連線。然而，某些容器並不期望這種行為，因此若您需要阻止它關閉連線，請將 "closeConnection" 屬性設為 false。例如：

  ```xml
  <transactionManager type="MANAGED">
    <property name="closeConnection" value="false"/>
  </transactionManager>
  ```

<span class="label important">注意</span> 若您計畫搭配 Spring 使用 MyBatis，則不需要設定任何 TransactionManager，因為 Spring 模組會設定自己的管理員並覆寫先前設定的任何組態。

這兩種 TransactionManager 型別都不需要任何屬性。然而，它們都是型別別名，換句話說，您可以不用它們，而是放置您自己的完整類別名稱或參照自訂 TransactionFactory 介面實作的型別別名。

```java
public interface TransactionFactory {
  default void setProperties(Properties props) { // 自 3.5.2 起改為 default 方法
    // NOP
  }
  Transaction newTransaction(Connection conn);
  Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);
}
```

在 XML 中設定的任何屬性在實例化後都將傳遞至 `setProperties()` 方法。您的實作還需要建立一個 Transaction 實作，這同樣是一個非常簡單的介面：

```java
public interface Transaction {
  Connection getConnection() throws SQLException;
  void commit() throws SQLException;
  void rollback() throws SQLException;
  void close() throws SQLException;
  Integer getTimeout() throws SQLException;
}
```

使用這兩個介面，您可以完全自訂 MyBatis 處理交易的方式。

**dataSource**

dataSource 元素使用標準的 JDBC DataSource 介面設定 JDBC Connection 物件的來源。

大多數 MyBatis 應用程式都會如範例所示設定 dataSource。然而，這並非必要。不過請注意，為了便於延遲載入，此 dataSource 是必要的。

有三種內建的 dataSource 型別（即 type="[UNPOOLED|POOLED|JNDI]"）：

**UNPOOLED** – 此 DataSource 實作在每次請求時僅開啟與關閉連線。雖然速度較慢，但對於不需要立即取得連線效能的簡單應用程式來說，這是一個不錯的選擇。不同資料庫在此效能方面也有所不同，因此對於某些資料庫而言，使用連線池可能不太重要，此時這種設定將會非常理想。UNPOOLED DataSource 具有以下可設定的屬性：

- `driver` – 這是 JDBC 驅動程式的完整 Java 類別名稱（若您的驅動程式包含 DataSource 類別，請注意不是該類別）。
- `url` – 這是您資料庫實例的 JDBC URL。
- `username` – 用於登入的資料庫使用者名稱。
- `password` - 用於登入的資料庫密碼。
- `defaultTransactionIsolationLevel` – 連線的預設交易隔離層級。
- `defaultNetworkTimeout` – 等待資料庫操作完成的預設網路逾時值（毫秒）。詳情請參閱 `java.sql.Connection#setNetworkTimeout()` 的 API 文件。

選擇性地，您也可以將屬性直接傳遞給資料庫驅動程式。為此，請為這些屬性加上前置詞 `driver.`，例如：

- `driver.encoding=UTF8`

這將透過 `DriverManager.getConnection(url, driverProperties)` 方法將值為 `UTF8` 的屬性 `encoding` 傳遞給您的資料庫驅動程式。

**POOLED** – 此 DataSource 實作將 JDBC Connection 物件建立為連線池，以避免每次建立新 Connection 實例所需的初始連線與驗證時間。這是並行 Web 應用程式實現最快回應速度的常用方法。

除了上述 (UNPOOLED) 屬性外，還有更多可用於設定 POOLED 資料來源的屬性：

- `poolMaximumActiveConnections` – 在任何給定時間可以存在的使用中（即正在使用）連線數。預設值：10
- `poolMaximumIdleConnections` – 在任何給定時間可以存在的閒置連線數。
- `poolMaximumCheckoutTime` – 連線從池中「簽出（checked out）」後，被強制歸還前的最長時間。預設值：20000ms（即 20 秒）
- `poolTimeToWait` – 這是一個低階設定，當取得連線耗費異常長的時間時，讓連線池有機會列印日誌狀態並重新嘗試取得連線（以避免在連線池設定錯誤時永遠無聲失敗）。預設值：20000ms（即 20 秒）
- `poolMaximumLocalBadConnectionTolerance` – 這是一個關於任何執行緒取得無效連線容忍度的低階設定。若執行緒取得無效連線，它可能仍有另一次機會重新嘗試取得另一個有效連線。但重試次數不應超過 `poolMaximumIdleConnections` 與 `poolMaximumLocalBadConnectionTolerance` 之和。預設值：3（自 3.4.5 起）
- `poolPingQuery` – Ping Query 會發送至資料庫以驗證連線是否處於正常運作狀態並準備好接受請求。預設值為 "NO PING QUERY SET"，這會導致大多數資料庫驅動程式失敗並顯示合適的錯誤訊息。
- `poolPingEnabled` – 啟用或停用 Ping Query。若啟用，您還必須為 poolPingQuery 屬性設定有效的 SQL 陳述式（最好是非常快速的陳述式）。預設值：false。
- `poolPingConnectionsNotUsedFor` – 設定 poolPingQuery 的使用頻率。可以將其設定為符合資料庫連線的典型逾時時間，以避免不必要的 Ping。預設值：0（即每次都會對所有連線進行 Ping，當然僅在 poolPingEnabled 為 true 時有效）。

**JNDI** – 此 DataSource 實作適用於諸如 EJB 或應用程式伺服器等容器，這些容器可能集中或在外部設定 DataSource，並在 JNDI 內容環境中放置對其的參照。此 DataSource 設定僅需要兩個屬性：

- `initial_context` – 此屬性用於從 InitialContext 中尋找 Context（即 initialContext.lookup(initial_context)）。此屬性為選擇性的，若省略，則會直接針對 InitialContext 尋找 data_source 屬性。
- `data_source` – 這是可以找到 DataSource 實例參照的內容路徑。它將針對 initial_context 尋找所回傳的內容進行尋找，若未提供 initial_context，則直接針對 InitialContext 進行尋找。

與其他 DataSource 設定類似，可以透過為屬性加上前置詞 `env.` 來直接將屬性傳送至 InitialContext，例如：

- `env.encoding=UTF8`

這將在實例化時將值為 `UTF8` 的屬性 `encoding` 傳送至 InitialContext 的建構函式。

您可以透過實作介面 `org.apache.ibatis.datasource.DataSourceFactory` 來外掛任何第三方 DataSource：

```java
public interface DataSourceFactory {
  void setProperties(Properties props);
  DataSource getDataSource();
}
```

`org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory` 可作為建置新資料來源轉接器的父類別。例如，以下是外掛 C3P0 所需的程式碼：

```java
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3P0DataSourceFactory extends UnpooledDataSourceFactory {

  public C3P0DataSourceFactory() {
    this.dataSource = new ComboPooledDataSource();
  }
}
```

若要進行設定，請針對您希望 MyBatis 呼叫的每個 Setter 方法新增一個屬性。以下是連接至 PostgreSQL 資料庫的範例設定：

```xml
<dataSource type="org.myproject.C3P0DataSourceFactory">
  <property name="driver" value="org.postgresql.Driver"/>
  <property name="url" value="jdbc:postgresql:mydb"/>
  <property name="username" value="postgres"/>
  <property name="password" value="root"/>
</dataSource>
```

<a id="databaseidprovider"></a>
### databaseIdProvider

MyBatis 能夠根據您的資料庫廠商執行不同的陳述式。多資料庫廠商支援是基於已映射陳述式的 `databaseId` 屬性。MyBatis 將載入所有沒有 `databaseId` 屬性的陳述式，或者 `databaseId` 與目前資料庫相符的陳述式。若找到具有相同名稱但分別包含與不包含 `databaseId` 的陳述式，後者將被捨棄。若要啟用多廠商支援，請在 mybatis-config.xml 檔案中新增 `databaseIdProvider`，如下所示：

```xml
<databaseIdProvider type="DB_VENDOR" />
```

DB_VENDOR 實作的 databaseIdProvider 會將 `DatabaseMetaData#getDatabaseProductName()` 所回傳的字串設為 databaseId。由於該字串通常過長，且同一產品的不同版本可能會回傳不同的值，因此您可能希望透過新增如下屬性將其轉換為較短的字串：

```xml
<databaseIdProvider type="DB_VENDOR">
  <property name="SQL Server" value="sqlserver"/>
  <property name="DB2" value="db2"/>
  <property name="Oracle" value="oracle" />
</databaseIdProvider>
```

當提供屬性時，DB_VENDOR databaseIdProvider 將回傳與回傳之資料庫產品名稱中找到的第一個索引鍵相對應的屬性值；若沒有相符的屬性，則回傳 "null"。在此範例中，若 `getDatabaseProductName()` 回傳 "Oracle (DataDirect)"，則 databaseId 將被設定為 "oracle"。

您可以透過實作介面 `org.apache.ibatis.mapping.DatabaseIdProvider` 並在 mybatis-config.xml 中註冊，來建置您自己的 DatabaseIdProvider：

```java
public interface DatabaseIdProvider {
  default void setProperties(Properties p) { // 自 3.5.2 起改為 default 方法
    // NOP
  }
  String getDatabaseId(DataSource dataSource) throws SQLException;
}
```

### mappers

現在 MyBatis 的行為已經透過上述設定元素完成設定，我們已準備好定義已映射的 SQL 陳述式。但首先，我們需要告訴 MyBatis 在哪裡可以找到它們。Java 在這方面並沒有提供任何良好的自動探索途徑，因此最好的做法就是直接告訴 MyBatis 在哪裡尋找映射檔。您可以使用相對於 classpath 的資源參照、完全限定的 URL 參照（包括 `file:///` URL）、類別名稱或套件名稱。例如：

```xml
<!-- 使用相對於 classpath 的資源 -->
<mappers>
  <mapper resource="org/mybatis/builder/AuthorMapper.xml"/>
  <mapper resource="org/mybatis/builder/BlogMapper.xml"/>
  <mapper resource="org/mybatis/builder/PostMapper.xml"/>
</mappers>
```
```xml
<!-- 使用完全限定的 url 路徑 -->
<mappers>
  <mapper url="file:///var/mappers/AuthorMapper.xml"/>
  <mapper url="file:///var/mappers/BlogMapper.xml"/>
  <mapper url="file:///var/mappers/PostMapper.xml"/>
</mappers>
```
```xml
<!-- 使用映射器介面類別 -->
<mappers>
  <mapper class="org.mybatis.builder.AuthorMapper"/>
  <mapper class="org.mybatis.builder.BlogMapper"/>
  <mapper class="org.mybatis.builder.PostMapper"/>
</mappers>
```
```xml
<!-- 將套件中的所有介面註冊為映射器 -->
<mappers>
  <package name="org.mybatis.builder"/>
</mappers>
```

這些陳述式只是告訴 MyBatis 接下來該往何處尋找。其餘的詳細資訊都在各個 SQL 映射檔中，而這正是下一節將要深入討論的內容。
