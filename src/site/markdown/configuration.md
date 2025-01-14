title: MyBatis 3 | Configuration
author: Clinton Begin

<h1 class="d-none">Avoid blank site</h1>

## Configuration

The MyBatis configuration contains settings and properties that have a dramatic effect on how MyBatis behaves. The high level structure of the document is as follows:

- configuration
  - [properties](#properties)
  - [settings](#settings)
  - [typeAliases](#typeAliases)
  - [typeHandlers](#typeHandlers)
  - [objectFactory](#objectFactory)
  - [plugins](#plugins)
  - [environments](#environments)
    - environment
      - transactionManager
      - dataSource
  - [databaseIdProvider](#databaseIdProvider)
  - [mappers](#mappers)

### properties

These are externalizable, substitutable properties that can be configured in a typical Java Properties file instance, or passed in through sub-elements of the properties element. For example:

```xml
<properties resource="org/mybatis/example/config.properties">
  <property name="username" value="dev_user"/>
  <property name="password" value="F2Fa3!33TYyg"/>
</properties>
```

The properties can then be used throughout the configuration files to substitute values that need to be dynamically configured. For example:

```xml
<dataSource type="POOLED">
  <property name="driver" value="${driver}"/>
  <property name="url" value="${url}"/>
  <property name="username" value="${username}"/>
  <property name="password" value="${password}"/>
</dataSource>
```

The username and password in this example will be replaced by the values set in the properties elements. The driver and url properties would be replaced with values contained from the config.properties file. This provides a lot of options for configuration.

Properties can also be passed into the SqlSessionFactoryBuilder.build() methods. For example:

```java
SqlSessionFactory factory =
  sqlSessionFactoryBuilder.build(reader, props);

// ... or ...

SqlSessionFactory factory =
  new SqlSessionFactoryBuilder.build(reader, environment, props);
```

If a property exists in more than one of these places, MyBatis loads them in the following order:

- Properties specified in the body of the properties element are read first,
- Properties loaded from the classpath resource or url attributes of the properties element are read second, and override any duplicate properties already specified,
- Properties passed as a method parameter are read last, and override any duplicate properties that may have been loaded from the properties body and the resource/url attributes.

Thus, the highest priority properties are those passed in as a method parameter, followed by resource/url attributes and finally the properties specified in the body of the properties element.

Since the MyBatis 3.4.2, your can specify a default value into placeholder as follow:

```xml
<dataSource type="POOLED">
  <!-- ... -->
  <property name="username" value="${username:ut_user}"/> <!-- If 'username' property not present, username become 'ut_user' -->
</dataSource>
```

This feature is disabled by default. If you specify a default value into placeholder, you should enable this feature by adding a special property as follow:

```xml
<properties resource="org/mybatis/example/config.properties">
  <!-- ... -->
  <property name="org.apache.ibatis.parsing.PropertyParser.enable-default-value" value="true"/> <!-- Enable this feature -->
</properties>
```

<span class="label important">NOTE</span> This will conflict with the `":"` character in property keys (e.g. `db:username`) or the ternary operator of OGNL expressions (e.g. `${tableName != null ? tableName : 'global_constants'}`) on a SQL definition. If you use either and want default property values, you must change the default value separator by adding this special property:

```xml
<properties resource="org/mybatis/example/config.properties">
  <!-- ... -->
  <property name="org.apache.ibatis.parsing.PropertyParser.default-value-separator" value="?:"/> <!-- Change default value of separator -->
</properties>
```

```xml
<dataSource type="POOLED">
  <!-- ... -->
  <property name="username" value="${db:username?:ut_user}"/>
</dataSource>
```

### settings

These are extremely important tweaks that modify the way that MyBatis behaves at runtime. The following table describes the settings, their meanings and their default values.

| Setting                            | Description                                                                                                                                                                                                                                                                                                                                                                                                                                      | Valid Values                                                                                                                               | Default                                               |
|------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| cacheEnabled                       | Globally enables or disables any caches configured in any mapper under this configuration.                                                                                                                                                                                                                                                                                                                                                       | true &#124; false                                                                                                                          | true                                                  |
| lazyLoadingEnabled                 | Globally enables or disables lazy loading. When enabled, all relations will be lazily loaded. This value can be superseded for a specific relation by using the `fetchType` attribute on it.                                                                                                                                                                                                                                                     | true &#124; false                                                                                                                          | false                                                 |
| aggressiveLazyLoading              | When enabled, any method call will load all the lazy properties of the object. Otherwise, each property is loaded on demand (see also `lazyLoadTriggerMethods`).                                                                                                                                                                                                                                                                                 | true &#124; false                                                                                                                          | false (true in ≤3.4.1)                                |
| ~~multipleResultSetsEnabled~~    | Deprecated. This option has no effect.                                                                                                                                                                                                                                                                                                                                     | true &#124; false                                                                                                                          | true                                                  |
| useColumnLabel                     | Uses the column label instead of the column name. Different drivers behave differently in this respect. Refer to the driver documentation, or test out both modes to determine how your driver behaves.                                                                                                                                                                                                                                          | true &#124; false                                                                                                                          | true                                                  |
| useGeneratedKeys                   | Allows JDBC support for generated keys. A compatible driver is required. This setting forces generated keys to be used if set to true, as some drivers deny compatibility but still work (e.g. Derby).                                                                                                                                                                                                                                           | true &#124; false                                                                                                                          | false                                                 |
| autoMappingBehavior                | Specifies if and how MyBatis should automatically map columns to fields/properties. NONE disables auto-mapping. PARTIAL will only auto-map results with no nested result mappings defined inside. FULL will auto-map result mappings of any complexity (containing nested or otherwise).                                                                                                                                                         | NONE, PARTIAL, FULL                                                                                                                        | PARTIAL                                               |
| autoMappingUnknownColumnBehavior   | Specify the behavior when detects an unknown column (or unknown property type) of automatic mapping target.<ul><li>`NONE`: Do nothing</li><li>`WARNING`: Output warning log (The log level of `'org.apache.ibatis.session.AutoMappingUnknownColumnBehavior'` must be set to `WARN`)</li><li>`FAILING`: Fail mapping (Throw `SqlSessionException`)</li></ul>Note that there could be false-positives when `autoMappingBehavior` is set to `FULL`. | NONE, WARNING, FAILING                                                                                                                     | NONE                                                  |
| defaultExecutorType                | Configures the default executor. SIMPLE executor does nothing special. REUSE executor reuses prepared statements. BATCH executor reuses statements and batches updates.                                                                                                                                                                                                                                                                          | SIMPLE REUSE BATCH                                                                                                                         | SIMPLE                                                |
| defaultStatementTimeout            | Sets the number of seconds the driver will wait for a response from the database.                                                                                                                                                                                                                                                                                                                                                                | Any positive integer                                                                                                                       | Not Set (null)                                        |
| defaultFetchSize                   | Sets the driver a hint as to control fetching size for return results. This parameter value can be override by a query setting.                                                                                                                                                                                                                                                                                                                  | Any positive integer                                                                                                                       | Not Set (null)                                        |
| defaultResultSetType               | Specifies a scroll strategy when omit it per statement settings. (Since: 3.5.2)                                                                                                                                                                                                                                                                                                                                                                  | FORWARD_ONLY &#124; SCROLL_SENSITIVE &#124; SCROLL_INSENSITIVE &#124; DEFAULT(same behavior with 'Not Set')                                | Not Set (null)                                        |
| safeRowBoundsEnabled               | Allows using RowBounds on nested statements. If allow, set the false.                                                                                                                                                                                                                                                                                                                                                                            | true &#124; false                                                                                                                          | false                                                 |
| safeResultHandlerEnabled           | Allows using ResultHandler on nested statements. If allow, set the false.                                                                                                                                                                                                                                                                                                                                                                        | true &#124; false                                                                                                                          | true                                                  |
| mapUnderscoreToCamelCase           | Enables automatic mapping from classic database column names A_COLUMN to camel case classic Java property names aColumn.                                                                                                                                                                                                                                                                                                                         | true &#124; false                                                                                                                          | false                                                 |
| localCacheScope                    | MyBatis uses local cache to prevent circular references and speed up repeated nested queries. By default (SESSION) all queries executed during a session are cached. If localCacheScope=STATEMENT local session will be used just for statement execution, no data will be shared between two different calls to the same SqlSession.                                                                                                            | SESSION &#124; STATEMENT                                                                                                                   | SESSION                                               |
| jdbcTypeForNull                    | Specifies the JDBC type for null values when no specific JDBC type was provided for the parameter. Some drivers require specifying the column JDBC type but others work with generic values like NULL, VARCHAR or OTHER.                                                                                                                                                                                                                         | JdbcType enumeration. Most common are: NULL, VARCHAR and OTHER                                                                             | OTHER                                                 |
| lazyLoadTriggerMethods             | Specifies which Object's methods trigger a lazy load                                                                                                                                                                                                                                                                                                                                                                                             | A method name list separated by commas                                                                                                     | equals,clone,hashCode,toString                        |
| defaultScriptingLanguage           | Specifies the language used by default for dynamic SQL generation.                                                                                                                                                                                                                                                                                                                                                                               | A type alias or fully qualified class name.                                                                                                | org.apache.ibatis.scripting.xmltags.XMLLanguageDriver |
| defaultEnumTypeHandler             | Specifies the `TypeHandler` used by default for Enum. (Since: 3.4.5)                                                                                                                                                                                                                                                                                                                                                                             | A type alias or fully qualified class name.                                                                                                | org.apache.ibatis.type.EnumTypeHandler                |
| callSettersOnNulls                 | Specifies if setters or map's put method will be called when a retrieved value is null. It is useful when you rely on Map.keySet() or null value initialization. Note primitives such as (int,boolean,etc.) will not be set to null.                                                                                                                                                                                                             | true &#124; false                                                                                                                          | false                                                 |
| returnInstanceForEmptyRow          | MyBatis, by default, returns `null` when all the columns of a returned row are NULL. When this setting is enabled, MyBatis returns an empty instance instead. Note that it is also applied to nested results (i.e. collection and association). Since: 3.4.2                                                                                                                                                                                    | true &#124; false                                                                                                                          | false                                                 |
| logPrefix                          | Specifies the prefix string that MyBatis will add to the logger names.                                                                                                                                                                                                                                                                                                                                                                           | Any String                                                                                                                                 | Not set                                               |
| logImpl                            | Specifies which logging implementation MyBatis should use. If this setting is not present logging implementation will be autodiscovered.                                                                                                                                                                                                                                                                                                         | SLF4J &#124; LOG4J(deprecated since 3.5.9) &#124; LOG4J2 &#124; JDK_LOGGING &#124; COMMONS_LOGGING &#124; STDOUT_LOGGING &#124; NO_LOGGING | Not set                                               |
| proxyFactory                       | Specifies the proxy tool that MyBatis will use for creating lazy loading capable objects.                                                                                                                                                                                                                                                                                                                                                        | CGLIB (deprecated since 3.5.10) &#124; JAVASSIST                                                                                           | JAVASSIST (MyBatis 3.3 or above)                      |
| vfsImpl                            | Specifies VFS implementations                                                                                                                                                                                                                                                                                                                                                                                                                    | Fully qualified class names of custom VFS implementation separated by commas.                                                              | Not set                                               |
| useActualParamName                 | Allow referencing statement parameters by their actual names declared in the method signature. To use this feature, your project must be compiled in Java 8 with `-parameters` option. (Since: 3.4.1)                                                                                                                                                                                                                                            | true &#124; false                                                                                                                          | true                                                  |
| configurationFactory               | Specifies the class that provides an instance of `Configuration`. The returned Configuration instance is used to load lazy properties of deserialized objects. This class must have a method with a signature `static Configuration getConfiguration()`. (Since: 3.2.3)                                                                                                                                                                          | A type alias or fully qualified class name.                                                                                                | Not set                                               |
| shrinkWhitespacesInSql             | Removes extra whitespace characters from the SQL. Note that this also affects literal strings in SQL. (Since 3.5.5)                                                                                                                                                                                                                                                                                                                              | true &#124; false                                                                                                                          | false                                                 |
| defaultSqlProviderType             | Specifies an sql provider class that holds provider method (Since 3.5.6). This class apply to the `type`(or `value`) attribute on sql provider annotation(e.g. `@SelectProvider`), when these attribute was omitted.                                                                                                                                                                                                                             | A type alias or fully qualified class name                                                                                                 | Not set                                               |
| nullableOnForEach                  | Specifies the default value of 'nullable' attribute on 'foreach' tag. (Since 3.5.9)                                                                                                                                                                                                                                                                                                                                                              | true &#124; false                                                                                                                          | false                                                 |
| argNameBasedConstructorAutoMapping | When applying constructor auto-mapping, argument name is used to search the column to map instead of relying on the column order. (Since 3.5.10)                                                                                                                                                                                                                                                                                                 | true &#124; false                                                                                                                          | false                                                 |

An example of the settings element fully configured is as follows:

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

### typeAliases

A type alias is simply a shorter name for a Java type. It's only relevant to the XML configuration and simply exists to reduce redundant typing of fully qualified classnames. For example:

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

With this configuration, `Blog` can now be used anywhere that `domain.blog.Blog` could be.

You can also specify a package where MyBatis will search for beans. For example:

```xml
<typeAliases>
  <package name="domain.blog"/>
</typeAliases>
```

Each bean found in `domain.blog` , if no annotation is found, will be registered as an alias using uncapitalized non-qualified class name of the bean. That is `domain.blog.Author` will be registered as `author`. If the `@Alias` annotation is found its value will be used as an alias. See the example below:

```java
@Alias("author")
public class Author {
    ...
}
```

There are many built-in type aliases for common Java types. They are all case insensitive, note the special handling of primitives due to the overloaded names.

| Alias                     | Mapped Type  |
|---------------------------|--------------|
| _byte                     | byte         |
| _char (since 3.5.10)      | char         |
| _character (since 3.5.10) | char         |
| _long                     | long         |
| _short                    | short        |
| _int                      | int          |
| _integer                  | int          |
| _double                   | double       |
| _float                    | float        |
| _boolean                  | boolean      |
| string                    | String       |
| byte                      | Byte         |
| char (since 3.5.10)       | Character    |
| character (since 3.5.10)  | Character    |
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


### typeHandlers

Whenever MyBatis sets a parameter on a PreparedStatement or retrieves a value from a ResultSet, a TypeHandler is used to retrieve the value in a means appropriate to the Java type. The following table describes the default TypeHandlers.

<span class="label important">NOTE</span> Since version 3.4.5, MyBatis supports JSR-310 (Date and Time API) by default.

| Type Handler                 | Java Types                      | JDBC Types                                                                             |
|------------------------------|---------------------------------|----------------------------------------------------------------------------------------|
| `BooleanTypeHandler`         | `java.lang.Boolean`, `boolean`  | Any compatible `BOOLEAN`                                                               |
| `ByteTypeHandler`            | `java.lang.Byte`, `byte`        | Any compatible `NUMERIC` or `BYTE`                                                     |
| `ShortTypeHandler`           | `java.lang.Short`, `short`      | Any compatible `NUMERIC` or `SMALLINT`                                                 |
| `IntegerTypeHandler`         | `java.lang.Integer`, `int`      | Any compatible `NUMERIC` or `INTEGER`                                                  |
| `LongTypeHandler`            | `java.lang.Long`, `long`        | Any compatible `NUMERIC` or `BIGINT`                                                   |
| `FloatTypeHandler`           | `java.lang.Float`, `float`      | Any compatible `NUMERIC` or `FLOAT`                                                    |
| `DoubleTypeHandler`          | `java.lang.Double`, `double`    | Any compatible `NUMERIC` or `DOUBLE`                                                   |
| `BigDecimalTypeHandler`      | `java.math.BigDecimal`          | Any compatible `NUMERIC` or `DECIMAL`                                                  |
| `StringTypeHandler`          | `java.lang.String`              | `CHAR`, `VARCHAR`                                                                      |
| `ClobReaderTypeHandler`      | `java.io.Reader`                | -                                                                                      |
| `ClobTypeHandler`            | `java.lang.String`              | `CLOB`, `LONGVARCHAR`                                                                  |
| `NStringTypeHandler`         | `java.lang.String`              | `NVARCHAR`, `NCHAR`                                                                    |
| `NClobTypeHandler`           | `java.lang.String`              | `NCLOB`                                                                                |
| `BlobInputStreamTypeHandler` | `java.io.InputStream`           | -                                                                                      |
| `ByteArrayTypeHandler`       | `byte[]`                        | Any compatible byte stream type                                                        |
| `BlobTypeHandler`            | `byte[]`                        | `BLOB`, `LONGVARBINARY`                                                                |
| `DateTypeHandler`            | `java.util.Date`                | `TIMESTAMP`                                                                            |
| `DateOnlyTypeHandler`        | `java.util.Date`                | `DATE`                                                                                 |
| `TimeOnlyTypeHandler`        | `java.util.Date`                | `TIME`                                                                                 |
| `SqlTimestampTypeHandler`    | `java.sql.Timestamp`            | `TIMESTAMP`                                                                            |
| `SqlDateTypeHandler`         | `java.sql.Date`                 | `DATE`                                                                                 |
| `SqlTimeTypeHandler`         | `java.sql.Time`                 | `TIME`                                                                                 |
| `ObjectTypeHandler`          | Any                             | `OTHER`, or unspecified                                                                |
| `EnumTypeHandler`            | Enumeration Type                | `VARCHAR` any string compatible type, as the code is stored (not index).               |
| `EnumOrdinalTypeHandler`     | Enumeration Type                | Any compatible `NUMERIC` or `DOUBLE`, as the position is stored (not the code itself). |
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
| `YearMonthTypeHandler`       | `java.time.YearMonth`           | `VARCHAR` or `LONGVARCHAR`                                                             |
| `JapaneseDateTypeHandler`    | `java.time.chrono.JapaneseDate` | `DATE`                                                                                 |

You can override the type handlers or create your own to deal with unsupported or non-standard types. To do so, implement the interface `org.apache.ibatis.type.TypeHandler` or extend the convenience class `org.apache.ibatis.type.BaseTypeHandler` and optionally map it to a JDBC type. For example:

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

Using such a TypeHandler would override the existing type handler for Java String properties and VARCHAR parameters and results. Note that MyBatis does not introspect upon the database metadata to determine the type, so you must specify that it’s a VARCHAR field in the parameter and result mappings to hook in the correct type handler. This is due to the fact that MyBatis is unaware of the data type until the statement is executed.

MyBatis will know the Java type that you want to handle with this TypeHandler by introspecting its generic type, but you can override this behavior by two means:

- Adding a `javaType` attribute to the typeHandler element (for example: `javaType="String"`)
- Adding a `@MappedTypes` annotation to your TypeHandler class specifying the list of java types to associate it with. This annotation will be ignored if the `javaType` attribute as also been specified.

The associated JDBC type can be specified by two means:

- Adding a `jdbcType` attribute to the typeHandler element (for example: `jdbcType="VARCHAR"`).
- Adding a `@MappedJdbcTypes` annotation to your TypeHandler class specifying the list of JDBC types to associate it with. This annotation will be ignored if the `jdbcType` attribute as also been specified.

When deciding which TypeHandler to use in a `ResultMap`, the Java type is known (from the result type), but the JDBC type is unknown. MyBatis therefore uses the combination `javaType=[TheJavaType], jdbcType=null` to choose a TypeHandler. This means that using a `@MappedJdbcTypes` annotation *restricts* the scope of a TypeHandler and makes it unavailable for use in `ResultMap`s unless explicitly set. To make a TypeHandler available for use in a `ResultMap`, set `includeNullJdbcType=true` on the `@MappedJdbcTypes` annotation. Since Mybatis 3.4.0 however, if a **single** TypeHandler is registered to handle a Java type, it will be used by default in `ResultMap`s using this Java type (i.e. even without `includeNullJdbcType=true`).

And finally you can let MyBatis search for your TypeHandlers:

```xml
<!-- mybatis-config.xml -->
<typeHandlers>
  <package name="org.mybatis.example"/>
</typeHandlers>
```

Note that when using the autodiscovery feature JDBC types can only be specified with annotations.

You can create a generic TypeHandler that is able to handle more than one class. For that purpose add a constructor that receives the class as a parameter and MyBatis will pass the actual class when constructing the TypeHandler.

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

`EnumTypeHandler` and `EnumOrdinalTypeHandler` are generic TypeHandlers. We will learn about them in the following section.

### Handling Enums

If you want to map an `Enum`, you'll need to use either `EnumTypeHandler` or `EnumOrdinalTypeHandler`.

For example, let's say that we need to store the rounding mode that should be used with some number if it needs to be rounded. By default, MyBatis uses `EnumTypeHandler` to convert the `Enum` values to their names.

**Note `EnumTypeHandler` is special in the sense that unlike other handlers, it does not handle just one specific class, but any class that extends `Enum`**

However, we may not want to store names. Our DBA may insist on an integer code instead. That's just as easy: add `EnumOrdinalTypeHandler` to the `typeHandlers` in your config file, and now each `RoundingMode` will be mapped to an integer using its ordinal value.

```xml
<!-- mybatis-config.xml -->
<typeHandlers>
  <typeHandler handler="org.apache.ibatis.type.EnumOrdinalTypeHandler"
    javaType="java.math.RoundingMode"/>
</typeHandlers>
```

But what if you want to map the same `Enum` to a string in one place and to integer in another?

The auto-mapper will automatically use `EnumOrdinalTypeHandler`, so if we want to go back to using plain old ordinary `EnumTypeHandler`, we have to tell it, by explicitly setting the type handler to use for those SQL statements.

(Mapper files aren't covered until the next section, so if this is your first time reading through the documentation, you may want to skip this for now and come back to it later.)

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

Note that this forces us to use a `resultMap` instead of a `resultType` in our select statements.

### objectFactory

Each time MyBatis creates a new instance of a result object, it uses an ObjectFactory instance to do so. The default ObjectFactory does little more than instantiate the target class with a default constructor, or a parameterized constructor if parameter mappings exist. If you want to override the default behaviour of the ObjectFactory, you can create your own. For example:

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

The ObjectFactory interface is very simple. It contains two create methods, one to deal with the default constructor, and the other to deal with parameterized constructors. Finally, the setProperties method can be used to configure the ObjectFactory. Properties defined within the body of the objectFactory element will be passed to the setProperties method after initialization of your ObjectFactory instance.

### plugins

MyBatis allows you to intercept calls to at certain points within the execution of a mapped statement. By default, MyBatis allows plug-ins to intercept method calls of:

- Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
- ParameterHandler (getParameterObject, setParameters)
- ResultSetHandler (handleResultSets, handleOutputParameters)
- StatementHandler (prepare, parameterize, batch, update, query)

The details of these classes methods can be discovered by looking at the full method signature of each, and the source code which is available with each MyBatis release. You should understand the behaviour of the method you’re overriding, assuming you’re doing something more than just monitoring calls. If you attempt to modify or override the behaviour of a given method, you’re likely to break the core of MyBatis. These are low level classes and methods, so use plug-ins with caution.

Using plug-ins is pretty simple given the power they provide. Simply implement the Interceptor interface, being sure to specify the signatures you want to intercept.

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
    // implement pre-processing if needed
    Object returnObject = invocation.proceed();
    // implement post-processing if needed
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

The plug-in above will intercept all calls to the "update" method on the Executor instance, which is an internal object responsible for the low-level execution of mapped statements.

<span class="label important">NOTE</span> **Overriding the Configuration Class**

In addition to modifying core MyBatis behaviour with plugins, you can also override the `Configuration` class entirely. Simply extend it and override any methods inside, and pass it into the call to the `SqlSessionFactoryBuilder.build(myConfig)` method. Again though, this could have a severe impact on the behaviour of MyBatis, so use caution.

### environments

MyBatis can be configured with multiple environments. This helps you to apply your SQL Maps to multiple databases for any number of reasons. For example, you might have a different configuration for your Development, Test and Production environments. Or, you may have multiple production databases that share the same schema, and you’d like to use the same SQL maps for both. There are many use cases.

**One important thing to remember though: While you can configure multiple environments, you can only choose ONE per SqlSessionFactory instance.**

So if you want to connect to two databases, you need to create two instances of SqlSessionFactory, one for each. For three databases, you’d need three instances, and so on. It’s really easy to remember:

- **One SqlSessionFactory instance per database**

To specify which environment to build, you simply pass it to the SqlSessionFactoryBuilder as an optional parameter. The two signatures that accept the environment are:

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, environment);
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, environment, properties);
```

If the environment is omitted, then the default environment is loaded, as follows:

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, properties);
```

The environments element defines how the environment is configured.

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

Notice the key sections here:

- The default Environment ID (e.g. default="development").
- The Environment ID for each environment defined (e.g. id="development").
- The TransactionManager configuration (e.g. type="JDBC")
- The DataSource configuration (e.g. type="POOLED")

The default environment and the environment IDs are self explanatory. Name them whatever you like, just make sure the default matches one of them.

**transactionManager**

There are two TransactionManager types (i.e. type="[JDBC|MANAGED]") that are included with MyBatis:

- JDBC – This configuration simply makes use of the JDBC commit and rollback facilities directly. It relies on the connection retrieved from the dataSource to manage the scope of the transaction. By default, it enables auto-commit when closing the connection for compatibility with some drivers. However, for some drivers, enabling auto-commit is not only unnecessary, but also is an expensive operation. So, since version 3.5.10, you can skip this step by setting the "skipSetAutoCommitOnClose" property to true. For example:

  ```xml
  <transactionManager type="JDBC">
    <property name="skipSetAutoCommitOnClose" value="true"/>
  </transactionManager>
  ```

- MANAGED – This configuration simply does almost nothing. It never commits, or rolls back a connection. Instead, it lets the container manage the full lifecycle of the transaction (e.g. a JEE Application Server context). By default it does close the connection. However, some containers don’t expect this, and thus if you need to stop it from closing the connection, set the "closeConnection" property to false. For example:

  ```xml
  <transactionManager type="MANAGED">
    <property name="closeConnection" value="false"/>
  </transactionManager>
  ```

<span class="label important">NOTE</span> If you are planning to use MyBatis with Spring there is no need to configure any TransactionManager because the Spring module will set its own one overriding any previously set configuration.

Neither of these TransactionManager types require any properties. However, they are both Type Aliases, so in other words, instead of using them, you could put your own fully qualified class name or Type Alias that refers to your own implementation of the TransactionFactory interface.

```java
public interface TransactionFactory {
  default void setProperties(Properties props) { // Since 3.5.2, change to default method
    // NOP
  }
  Transaction newTransaction(Connection conn);
  Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);
}
```

Any properties configured in the XML will be passed to the setProperties() method after instantiation. Your implementation would also need to create a Transaction implementation, which is also a very simple interface:

```java
public interface Transaction {
  Connection getConnection() throws SQLException;
  void commit() throws SQLException;
  void rollback() throws SQLException;
  void close() throws SQLException;
  Integer getTimeout() throws SQLException;
}
```

Using these two interfaces, you can completely customize how MyBatis deals with Transactions.

**dataSource**

The dataSource element configures the source of JDBC Connection objects using the standard JDBC DataSource interface.

Most MyBatis applications will configure a dataSource as in the example. However, it’s not required. Realize though, that to facilitate Lazy Loading, this dataSource is required.

There are three built-in dataSource types (i.e. type="[UNPOOLED|POOLED|JNDI]"):

**UNPOOLED** – This implementation of DataSource simply opens and closes a connection each time it is requested. While it’s a bit slower, this is a good choice for simple applications that do not require the performance of immediately available connections. Different databases are also different in this performance area, so for some it may be less important to pool and this configuration will be ideal. The UNPOOLED DataSource has the following properties to configure:

- `driver` – This is the fully qualified Java class of the JDBC driver (NOT of the DataSource class if your driver includes one).
- `url` – This is the JDBC URL for your database instance.
- `username` – The database username to log in with.
- `password` - The database password to log in with.
- `defaultTransactionIsolationLevel` – The default transaction isolation level for connections.
- `defaultNetworkTimeout` – The default network timeout value in milliseconds to wait for the database operation to complete. See the API documentation of `java.sql.Connection#setNetworkTimeout()` for details.

Optionally, you can pass properties to the database driver as well. To do this, prefix the properties with `driver.`, for example:

- `driver.encoding=UTF8`

This will pass the property `encoding`, with the value `UTF8`, to your database driver via the `DriverManager.getConnection(url, driverProperties)` method.

**POOLED** – This implementation of DataSource pools JDBC Connection objects to avoid the initial connection and authentication time required to create a new Connection instance. This is a popular approach for concurrent web applications to achieve the fastest response.

In addition to the (UNPOOLED) properties above, there are many more properties that can be used to configure the POOLED datasource:

- `poolMaximumActiveConnections` – This is the number of active (i.e. in use) connections that can exist at any given time. Default: 10
- `poolMaximumIdleConnections` – The number of idle connections that can exist at any given time.
- `poolMaximumCheckoutTime` – This is the amount of time that a Connection can be "checked out" of the pool before it will be forcefully returned. Default: 20000ms (i.e. 20 seconds)
- `poolTimeToWait` – This is a low level setting that gives the pool a chance to print a log status and re-attempt the acquisition of a connection in the case that it’s taking unusually long (to avoid failing silently forever if the pool is misconfigured). Default: 20000ms (i.e. 20 seconds)
- `poolMaximumLocalBadConnectionTolerance` – This is a low level setting about tolerance of bad connections got for any thread. If a thread got a bad connection, it may still have another chance to re-attempt to get another connection which is valid. But the retrying times should not more than the sum of `poolMaximumIdleConnections` and `poolMaximumLocalBadConnectionTolerance`. Default: 3 (Since: 3.4.5)
- `poolPingQuery` – The Ping Query is sent to the database to validate that a connection is in good working order and is ready to accept requests. The default is "NO PING QUERY SET", which will cause most database drivers to fail with a decent error message.
- `poolPingEnabled` – This enables or disables the ping query. If enabled, you must also set the poolPingQuery property with a valid SQL statement (preferably a very fast one). Default: false.
- `poolPingConnectionsNotUsedFor` – This configures how often the poolPingQuery will be used. This can be set to match the typical timeout for a database connection, to avoid unnecessary pings. Default: 0 (i.e. all connections are pinged every time – but only if poolPingEnabled is true of course).

**JNDI** – This implementation of DataSource is intended for use with containers such as EJB or Application Servers that may configure the DataSource centrally or externally and place a reference to it in a JNDI context. This DataSource configuration only requires two properties:

- `initial_context` – This property is used for the Context lookup from the InitialContext (i.e. initialContext.lookup(initial_context)). This property is optional, and if omitted, then the data_source property will be looked up against the InitialContext directly.
- `data_source` – This is the context path where the reference to the instance of the DataSource can be found. It will be looked up against the context returned by the initial_context lookup, or against the InitialContext directly if no initial_context is supplied.

Similar to the other DataSource configurations, it’s possible to send properties directly to the InitialContext by prefixing those properties with `env.`, for example:

- `env.encoding=UTF8`

This would send the property `encoding` with the value of `UTF8` to the constructor of the InitialContext upon instantiation.

You can plug any 3rd party DataSource by implementing the interface `org.apache.ibatis.datasource.DataSourceFactory`:

```java
public interface DataSourceFactory {
  void setProperties(Properties props);
  DataSource getDataSource();
}
```

`org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory` can be used as super class to build new datasource adapters. For example this is the code needed to plug C3P0:

```java
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3P0DataSourceFactory extends UnpooledDataSourceFactory {

  public C3P0DataSourceFactory() {
    this.dataSource = new ComboPooledDataSource();
  }
}
```

To set it up, add a property for each setter method you want MyBatis to call. Follows below a sample configuration which connects to a PostgreSQL database:

```xml
<dataSource type="org.myproject.C3P0DataSourceFactory">
  <property name="driver" value="org.postgresql.Driver"/>
  <property name="url" value="jdbc:postgresql:mydb"/>
  <property name="username" value="postgres"/>
  <property name="password" value="root"/>
</dataSource>
```

### databaseIdProvider

MyBatis is able to execute different statements depending on your database vendor. The multi-db vendor support is based on the mapped statements `databaseId` attribute. MyBatis will load all statements with no `databaseId` attribute or with a `databaseId` that matches the current one. In case the same statement is found with and without the `databaseId` the latter will be discarded. To enable the multi vendor support add a `databaseIdProvider` to mybatis-config.xml file as follows:

```xml
<databaseIdProvider type="DB_VENDOR" />
```

The DB_VENDOR implementation databaseIdProvider sets as databaseId the String returned by `DatabaseMetaData#getDatabaseProductName()`. Given that usually that string is too long and that different versions of the same product may return different values, you may want to convert it to a shorter one by adding properties like follows:

```xml
<databaseIdProvider type="DB_VENDOR">
  <property name="SQL Server" value="sqlserver"/>
  <property name="DB2" value="db2"/>
  <property name="Oracle" value="oracle" />
</databaseIdProvider>
```

When properties are provided, the DB_VENDOR databaseIdProvider will search the property value corresponding to the first key found in the returned database product name or "null" if there is not a matching property. In this case, if `getDatabaseProductName()` returns "Oracle (DataDirect)" the databaseId will be set to "oracle".

You can build your own DatabaseIdProvider by implementing the interface `org.apache.ibatis.mapping.DatabaseIdProvider` and registering it in mybatis-config.xml:

```java
public interface DatabaseIdProvider {
  default void setProperties(Properties p) { // Since 3.5.2, changed to default method
    // NOP
  }
  String getDatabaseId(DataSource dataSource) throws SQLException;
}
```

### mappers

Now that the behavior of MyBatis is configured with the above configuration elements, we’re ready to define our mapped SQL statements. But first, we need to tell MyBatis where to find them. Java doesn’t really provide any good means of auto-discovery in this regard, so the best way to do it is to simply tell MyBatis where to find the mapping files. You can use classpath relative resource references, fully qualified url references (including `file:///` URLs), class names or package names. For example:

```xml
<!-- Using classpath relative resources -->
<mappers>
  <mapper resource="org/mybatis/builder/AuthorMapper.xml"/>
  <mapper resource="org/mybatis/builder/BlogMapper.xml"/>
  <mapper resource="org/mybatis/builder/PostMapper.xml"/>
</mappers>
```
```xml
<!-- Using url fully qualified paths -->
<mappers>
  <mapper url="file:///var/mappers/AuthorMapper.xml"/>
  <mapper url="file:///var/mappers/BlogMapper.xml"/>
  <mapper url="file:///var/mappers/PostMapper.xml"/>
</mappers>
```
```xml
<!-- Using mapper interface classes -->
<mappers>
  <mapper class="org.mybatis.builder.AuthorMapper"/>
  <mapper class="org.mybatis.builder.BlogMapper"/>
  <mapper class="org.mybatis.builder.PostMapper"/>
</mappers>
```
```xml
<!-- Register all interfaces in a package as mappers -->
<mappers>
  <package name="org.mybatis.builder"/>
</mappers>
```

These statement simply tell MyBatis where to go from here. The rest of the details are in each of the SQL Mapping files, and that’s exactly what the next section will discuss.
