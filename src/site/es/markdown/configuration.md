title: MyBatis 3 | Configuración
author: Clinton Begin, Eduardo Macarron

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

# Configuración

El fichero de configuración XML contiene parámetros y configuraciones que tienen un efecto crucial en cómo se comporta MyBatis. A alto nivel contiene:

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

## properties

Contiene propiedades externalizables y sustituibles que se pueden configurar en un típico properties de Java o bien puede definirse su contenido directamente mediante subelementos property. Por ejemplo:

```xml
<properties resource="org/mybatis/example/config.properties">
  <property name="username" value="dev_user"/>
  <property name="password" value="F2Fa3!33TYyg"/>
</properties>
```

Las propiedades pueden usarse a lo largo del fichero de configuración para sustituir valores que deben configurarse dinámicamente. Por ejemplo:

```xml
<dataSource type="POOLED">
  <property name="driver" value="${driver}"/>
  <property name="url" value="${url}"/>
  <property name="username" value="${username}"/>
  <property name="password" value="${password}"/>
</dataSource>
```

El usuario y password de este ejemplo se reemplazarán por los valores de los elementos de tipo property. El driver y la url se reemplazarán por los valores contenidos en el fichero config.properties. Esto aumenta mucho las posibilidades de configuración.

Las propiedades también pueden pasarse como parámetro al método SqlSessionFactoryBuilder.build(). Por ejemplo:

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, props);

// ... or ...

SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, environment, props);
```

Si una propiedad existe en más de un lugar, MyBatis la carga en este orden:

- Primero se leen las propiedades especificadas en el elemento XML properties,
- Posteriormente se cargan las propiedades de recursos de tipo classpath o url del elementos properties, si hubiera una propiedad repetida sería sobrescrita,
- Y finalmente se leen las propiedades pasadas como parámetro, que en caso de duplicidad sobrescriben las propiedades que se hayan cargado del elemento properties o de recursos/url.

Por tanto las properties más prioritarias son las pasadas como parámetro, seguidas de los atributos tipo classpath/url y finalmente las propiedades especificadas en el elemento properties..

Since the MyBatis 3.4.2, your can specify a default value into placeholder as follow:

```xml
<dataSource type="POOLED">
  <!-- ... -->
  <property name="username" value="${username:ut_user}"/> <!-- If 'username' property not present, username become 'ut_user' -->
</dataSource>
```

This feature is disabled by default. If you specify a default value into placeholder, you should be enable this feature by adding a special property as follow:

```xml
<properties resource="org/mybatis/example/config.properties">
  <!-- ... -->
  <property name="org.apache.ibatis.parsing.PropertyParser.enable-default-value" value="true"/> <!-- Enable this feature -->
</properties>
```

<span class="label important">NOTE</span> Also If you are used already the `":"` as property key(e.g. `db:username`) or you are used already the ternary operator of OGNL expression(e.g. `${tableName != null ? tableName : 'global_constants'}`) on your sql definition, you should be change the character that separate key and default value by adding a special property as follow:

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

## settings

Son muy importantes para definir cómo se comporta MyBatis en ejecución. La siguiente tabla describe las configuraciones (settings), sus significados y sus valores por defecto.

| Configuración | Descripción | Valores admitidos | Valor por defecto |
| --- | --- | --- | --- |
| cacheEnabled | Habilita o inhabilita globalmente todas las cachés definidas en el mapper | true \| false | true |
| lazyLoadingEnabled | Habilita o inhabilita globalmente la carga diferida (lazy loading). Cuando está activo, todas las relaciones se cargan en modo diferido. Este valor se puede sobrescribir en cada relación usando el atributo `fetchType`. | true \| false | false |
| aggressiveLazyLoading | When enabled, any method call will load all the lazy properties of the object. Otherwise, each property is loaded on demand (see also `lazyLoadTriggerMethods`). | true \| false | false (true in ≤3.4.1) |
| <del>multipleResultSetsEnabled</del> | Deprecated. This option has no effect. | true \| false | true |
| useColumnLabel | Utiliza la etiqueta de columna (label) en lugar del nombre de columna. Algunos drivers se comportan distinto en lo que a esto respecta. Consulta la documentación del driver o prueba ambos modos para descubrir cómo funciona tu driver | true \| false | true |
| useGeneratedKeys | Habilita el uso del soporte JDBC para claves autogeneradas. Se requiere un driver compatible. Este parámetro fuerza el uso de las claves autogeneradas si está habilitado. Algunos drivers indican que no son compatibles aunque funcionan correctamente (ej. Derby) | true \| false | false |
| autoMappingBehavior | Especifica cómo deben mapearse de forma automática las columnas a los campos/propiedades. NONE desactiva el mapeo automático. PARTIAL sólo mapea automáticamente los resultados que no contienen result maps anidados en su interior. FULL mapea resultados de cualquier complejidad (contengan anidados o no). | NONE, PARTIAL, FULL | PARTIAL |
| autoMappingUnknownColumnBehavior | Specify the behavior when detects an unknown column (or unknown property type) of automatic mapping target. <ul><li>`NONE`: Do nothing</li> <li>`WARNING`: Output warning log (The log level of `'org.apache.ibatis.session.AutoMappingUnknownColumnBehavior'` must be set to `WARN`)</li> <li>`FAILING`: Fail mapping (Throw `SqlSessionException`)</li></ul> Note that there could be false-positives when \`autoMappingBehavior\` is set to \`FULL\`. | NONE, WARNING, FAILING | NONE |
| defaultExecutorType | Configura el ejecutor (executor) por defecto. SIMPLE no hace nada especial. REUSE reúsa prepared statements. BATCH reúsa statements y ejecuta actualizaciones en batch. | SIMPLE REUSE BATCH | SIMPLE |
| defaultStatementTimeout | Establece el número de segundos que debe esperar el driver la respuesta de la base de datos. | Cualquier entero no negativo | Sin valor (null) |
| defaultFetchSize | Sets the driver a hint as to control fetching size for return results. This parameter value can be override by a query setting. | Cualquier entero no negativo | Sin valor (null) |
| defaultResultSetType | Specifies a scroll strategy when omit it per statement settings. (Since: 3.5.2) | FORWARD\_ONLY \| SCROLL\_SENSITIVE \| SCROLL\_INSENSITIVE \| DEFAULT(same behavior with \'Not Set\') | Not Set (null) |
| safeRowBoundsEnabled | Controla la comprobación de seguridad para usar RowBounds con mapeos de resultados anidados. Establecer false permite omitir esta comprobación. | true \| false | false |
| safeResultHandlerEnabled | Cuando es true, impide usar un ResultHandler personalizado con mapeos de resultados anidados, salvo si resultOrdered es true. Establecer false permite omitir esta comprobación. | true \| false | true |
| mapUnderscoreToCamelCase | Mapea automáticamente los nombres clásicos de columnas de base de datos A\_COLUMN a nombres clásicos de propiedades Java aColumn. | true \| false | false |
| localCacheScope | MyBatis usa una caché local para resolver referencias circulares y acelerar consultas anidadas repetidas. Por defecto (SESSION), los resultados de las consultas ordinarias se almacenan durante toda la sesión. Las consultas con un ResultHandler personalizado no leen la caché local, y las consultas con Cursor no la utilizan. Si localCacheScope=STATEMENT, la caché local solo se usará durante la ejecución de una sentencia y no se compartirán datos entre distintas llamadas a SqlSession. | SESSION \| STATEMENT | SESSION |
| jdbcTypeForNull | Especifica el tipo JDBC para valores nulos cuando no se ha especificado un tipo concreto para el parámetro. Algunos drivers requieren que se indique el tipo JDBC de la columna pero otros permite valores genéricos como NULL, VARCHAR or OTHER. | JdbcType enumeration. Los más comunes son: NULL, VARCHAR and OTHER | OTHER |
| lazyLoadTriggerMethods | Permite especificar qué métodos de Object disparan la carga diferida | Lista de métodos separados por comas | equals,clone,hashCode,toString |
| defaultScriptingLanguage | Permite especificar que lenguaje se usará en el SQL dinámico. | Un type alias o una nombre de clase completamente cualificado | org.apache.ibatis.scripting.xmltags.XMLLanguageDriver |
| defaultEnumTypeHandler | Specifies the `TypeHandler` used by default for Enum. (Since: 3.4.5) | A type alias or fully qualified class name. | org.apache.ibatis.type.EnumTypeHandler |
| callSettersOnNulls | Permite especificar si se invocarán a los setters y a los métodos put de los mapas si el valor obtenido es null. Ten en cuenta que, si se activa, las propiedades que puedan recibir valores nulos no pueden ser de tipos primitivos. | true \| false | false |
| returnInstanceForEmptyRow | MyBatis, by default, returns `null` when all the columns of a returned row are NULL. When this setting is enabled, MyBatis returns an empty instance instead. Note that it is also applied to nested results (i.e. collection and association). Since: 3.4.2 | true \| false | false |
| logPrefix | Permite especificar el prefijo que MyBatis añadirá a los nombres de logger. | Cualquier cadena | No informado |
| logImpl | Permite especificar qué implementación de logging utilizar. Si no se especifica, la implementación se detectará automáticamente. | SLF4J \| LOG4J(deprecated since 3.5.9) \| LOG4J2 \| JDK\_LOGGING \| COMMONS\_LOGGING \| STDOUT\_LOGGING \| NO\_LOGGING | No informado |
| proxyFactory | Permite especificar qué herramienta de generación de proxys se usará para crear los objetos con capacidad de carga lazy. | CGLIB (deprecated since 3.5.10) \| JAVASSIST | JAVASSIST (MyBatis 3.3 or above) |
| vfsImpl | Specifies VFS implementations | Fully qualified class names of custom VFS implementation separated by commas. | Not set |
| useActualParamName | Allow referencing statement parameters by their actual names declared in the method signature. To use this feature, your project must be compiled in Java 8 with `-parameters` option. (Since: 3.4.1) | true \| false | true |
| configurationFactory | Specifies the class that provides an instance of `Configuration`. The returned Configuration instance is used to load lazy properties of deserialized objects. This class must have a method with a signature `static Configuration getConfiguration()`. (Since: 3.2.3) | A type alias or fully qualified class name. | Not set |
| shrinkWhitespacesInSql | Removes extra whitespace characters from the SQL. Note that this also affects literal strings in SQL. (Since 3.5.5) | true \| false | false |
| defaultSqlProviderType | Specifies an sql provider class that holds provider method (Since 3.5.6). This class apply to the `type`(or `value`) attribute on sql provider annotation(e.g. `@SelectProvider`), when these attribute was omitted. | A type alias or fully qualified class name | Not set |
| nullableOnForEach | Specifies the default value of \'nullable\' attribute on \'foreach\' tag. (Since 3.5.9) | true \| false | false |
| argNameBasedConstructorAutoMapping | When applying constructor auto-mapping, argument name is used to search the column to map instead of relying on the column order. (Since 3.5.10) | true \| false | false |
| ignoreParameterMetaData | Skips retrieving parameter meta data when resolving type handler. (Since 3.6.1) | true \| false | false |

A continuación se muestra un ejemplo del elemento settings al completo:

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
  <setting name="logImpl" value="JDK_LOGGING"/>
  <setting name="proxyFactory" value="JAVASSIST"/>
  <setting name="vfsImpl" value="org.mybatis.example.YourselfVfsImpl"/>
  <setting name="useActualParamName" value="true"/>
  <setting name="configurationFactory" value="org.mybatis.example.ConfigurationFactory"/>
</settings>
```

## typeAliases

Un type alias es simplemente un alias (un nombre más corto) para un tipo Java. Solo es importante para la configuración XML y existe para reducir la cantidad de texto al teclear nombres de clase cualificados (fully qualified). Por ejemplo:

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

Con esta configuración, puede usarse `Blog` en lugar de `domain.blog.Blog`

Tambien puedes indicar un paquete para que MyBatis busque beans de tipo alias. Por ejemplo:

```xml
<typeAliases>
  <package name="domain.blog"/>
</typeAliases>
```

Cada bean encontrado en `domain.blog`, en caso de que no contenga ninguna anotación, se registrará como alias usando su nombre no cualificado en minúsculas. Es decir, `domain.blog.Author` se registrará como will be registered as `author`. Si se encuentra la anotación `@Alias` se usará su valor como alias. Mira el ejemplo a continuación:

```java
@Alias("author")
public class Author {
    ...
}
```

Hay muchos type aliases pre construidos. No son sensibles a mayúsculas/minúsculas. Observa los nombres especiales de los tipos primitivos dadas las colisiones de nombres.

| Alias | Tipo mapeado |
| --- | --- |
| \_byte | byte |
| \_char (since 3.5.10) | char |
| \_character (since 3.5.10) | char |
| \_long | long |
| \_short | short |
| \_int | int |
| \_integer | int |
| \_double | double |
| \_float | float |
| \_boolean | boolean |
| string | String |
| byte | Byte |
| char (since 3.5.10) | Character |
| character (since 3.5.10) | Character |
| long | Long |
| short | Short |
| int | Integer |
| integer | Integer |
| double | Double |
| float | Float |
| boolean | Boolean |
| date | Date |
| decimal | BigDecimal |
| bigdecimal | BigDecimal |
| biginteger | BigInteger |
| object | Object |
| date\[\] | Date\[\] |
| decimal\[\] | BigDecimal\[\] |
| bigdecimal\[\] | BigDecimal\[\] |
| biginteger\[\] | BigInteger\[\] |
| object\[\] | Object\[\] |
| map | Map |
| hashmap | HashMap |
| list | List |
| arraylist | ArrayList |
| collection | Collection |
| iterator | Iterator |

## typeHandlers

Cuando MyBatis establece el valor de un parámetro de un PreparedStatement u obtiene un valor de un ResultSet, se utiliza un TypeHandler para convertir el valor al tipo Java apropiado. La siguiente tabla recoge los TypeHandlers predefinidos.

<span class="label important">NOTE</span> Since version 3.4.5, The MyBatis has been supported JSR-310(Date and Time API) by default.

| Type Handler | Tipos Java | Tipos JDBC |
| --- | --- | --- |
| `BooleanTypeHandler` | `java.lang.Boolean`, `boolean` | Cualquiera compatible con `BOOLEAN` |
| `ByteTypeHandler` | `java.lang.Byte`, `byte` | Cualquiera compatible con `NUMERIC` o `TINYINT` |
| `ShortTypeHandler` | `java.lang.Short`, `short` | Cualquiera compatible con `NUMERIC` o `SMALLINT` |
| `IntegerTypeHandler` | `java.lang.Integer`, `int` | Cualquiera compatible con `NUMERIC` o `INTEGER` |
| `LongTypeHandler` | `java.lang.Long`, `long` | Cualquiera compatible con `NUMERIC` o `BIGINT` |
| `FloatTypeHandler` | `java.lang.Float`, `float` | Cualquiera compatible con `NUMERIC` o `FLOAT` |
| `DoubleTypeHandler` | `java.lang.Double`, `double` | Cualquiera compatible con `NUMERIC` o `DOUBLE` |
| `BigDecimalTypeHandler` | `java.math.BigDecimal` | Cualquiera compatible con `NUMERIC` o `DECIMAL` |
| `StringTypeHandler` | `java.lang.String` | `CHAR`, `VARCHAR`, `LONGVARCHAR` |
| `ClobReaderTypeHandler` | `java.io.Reader` | - |
| `ClobTypeHandler` | `java.lang.String` | `CLOB` |
| `NStringTypeHandler` | `java.lang.String` | `NVARCHAR`, `NCHAR`, `LONGNVARCHAR` |
| `NClobTypeHandler` | `java.lang.String` | `NCLOB` |
| `BlobInputStreamTypeHandler` | `java.io.InputStream` | - |
| `ByteArrayTypeHandler` | `byte[]` | Cualquiera compatible con byte stream |
| `BlobTypeHandler` | `byte[]` | `BLOB`, `LONGVARBINARY` |
| `DateTypeHandler` | `java.util.Date` | `TIMESTAMP` |
| `DateOnlyTypeHandler` | `java.util.Date` | `DATE` |
| `TimeOnlyTypeHandler` | `java.util.Date` | `TIME` |
| `SqlTimestampTypeHandler` | `java.sql.Timestamp` | `TIMESTAMP` |
| `SqlDateTypeHandler` | `java.sql.Date` | `DATE` |
| `SqlTimeTypeHandler` | `java.sql.Time` | `TIME` |
| `ObjectTypeHandler` | Any | `OTHER`, or unspecified |
| `EnumTypeHandler` | Enumeration Type | `VARCHAR` Cualquiera compatible con string porque se guarda el código (no el índice). |
| `EnumOrdinalTypeHandler` | Enumeration Type | Cualquiera compatible con `NUMERIC` o `DOUBLE` por que se guarda la posición (no el código). |
| `SqlxmlTypeHandler` | `java.lang.String` | `SQLXML` |
| `InstantTypeHandler` | `java.time.Instant` | `TIMESTAMP` |
| `LocalDateTimeTypeHandler` | `java.time.LocalDateTime` | `TIMESTAMP` |
| `LocalDateTypeHandler` | `java.time.LocalDate` | `DATE` |
| `LocalTimeTypeHandler` | `java.time.LocalTime` | `TIME` |
| `OffsetDateTimeTypeHandler` | `java.time.OffsetDateTime` | `TIMESTAMP` |
| `OffsetTimeTypeHandler` | `java.time.OffsetTime` | `TIME` |
| `ZonedDateTimeTypeHandler` | `java.time.ZonedDateTime` | `TIMESTAMP` |
| `YearTypeHandler` | `java.time.Year` | `INTEGER` |
| `MonthTypeHandler` | `java.time.Month` | `INTEGER` |
| `YearMonthTypeHandler` | `java.time.YearMonth` | `VARCHAR` or `LONGVARCHAR` |
| `JapaneseDateTypeHandler` | `java.time.chrono.JapaneseDate` | `DATE` |

Es posible sobrescribir los TypeHandlers o crear TypeHandlers personalizados para tratar tipos no soportados o no estándares. Para ello, debes implementar la interfaz `org.apache.ibatis.type.TypeHandler` o extender la clase de ayuda `org.apache.ibatis.type.BaseTypeHandler` y opcionalmente mapear el TypeHandler a un tipo JDBC. Por ejemplo:

```java
// ExampleTypeHandler.java
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ExampleTypeHandler extends BaseTypeHandler<String> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, parameter);
  }

  @Override
  public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return rs.getString(columnName);
  }

  @Override
  public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return rs.getString(columnIndex);
  }

  @Override
  public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
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

Al usar este TypeHandler se sobrescribe el TypeHandler existente para los tipos String y los parámetros y resultados VARCHAR. Cuando `ignoreParameterMetaData` es `false` (el valor por defecto), MyBatis puede consultar los metadatos de parámetros JDBC para elegir un TypeHandler según el tipo JDBC si no se especifica en el mapeo del parámetro. Si el driver no proporciona esos metadatos, especifica `jdbcType="VARCHAR"` en el mapeo del parámetro. Si un mapeo de resultados no especifica `typeHandler` ni `jdbcType`, MyBatis puede usar los metadatos del conjunto de resultados para elegir el TypeHandler, como se explica más adelante.

MyBatis conoce el tipo Java que quieres gestionar introspecionando tipo genérico del TypeHandler, pero puedes modificar este comportamiento de dos maneras.

- Añadir un atributo `javaType` al elemento typeHandler (por ejemplo: `javaType="String"`)
- Añadir una anotación `@MappedTypes` a tu clase TypeHandler especificando la lista de tipos java a la que asociarlo. Esta anotación será ignorada si se ha especificado también un atributo `javaType`.

El tipo JDBC asociado se puede especificar de dos maneras:

- Añadiendo los atributos `javaType` y `jdbcType` al mismo elemento typeHandler (por ejemplo: `javaType="String" jdbcType="VARCHAR"`). Si se omite `javaType`, el atributo XML `jdbcType` se ignora y se usa `@MappedJdbcTypes` si está presente.
- Añadiendo una anotación `@MappedJdbcTypes` a tu clase TypeHandler especificando la lista de tipos JDBC a la que asociarlo. Esta anotación se ignora si se han especificado tanto `javaType` como `jdbcType` en el mismo elemento.

En un mapeo de `ResultMap`, un `typeHandler` explícito selecciona directamente el manejador. Si se especifica `jdbcType` sin `typeHandler`, MyBatis resuelve el manejador a partir del tipo Java y del tipo JDBC indicado al analizar el mapeo. Si no se especifica ninguno, el manejador se selecciona al leer los resultados, usando el tipo Java y el tipo JDBC real de la columna obtenido de los metadatos del conjunto de resultados. Por tanto, un manejador anotado con `@MappedJdbcTypes` puede usarse para columnas de resultados compatibles sin `includeNullJdbcType=true`. Esta opción también registra el manejador para `jdbcType=null`; no es un requisito para usarlo en un `ResultMap`.

Y finalmente puedes hacer que MyBatis busque tus TypeHandlers:

```xml
<!-- mybatis-config.xml -->
<typeHandlers>
  <package name="org.mybatis.example"/>
</typeHandlers>
```

Observa que cuando usas la función de búsqueda los tipos JDBC sólo se pueden especificar usando anotaciones.

Puedes crear un TypeHandler genérico que sea capaz de manejar más de un tipo de clase. Para ello añade un constructor que recibe una clase como parámetro y MyBatis le pasará la clase actual cuando construya el TypeHandler.

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

`EnumTypeHandler` y `EnumOrdinalTypeHandler` son TypeHandlers genéricos. Conoceremos más sobre ellos en la próxima sección.

## Handling Enums

Si quires mapear un `Enum`, debes usar bien un `EnumTypeHandler` o un `EnumOrdinalTypeHandler`.

Por ejemplo, digamos que quieres guardar el modo de reondeo que debe usarse con un número determinado que debe redondearse. Por defecto MyBatis usa un `EnumTypeHandler` para comvertir los valores del `Enum` a sus nombres.

<b>Observa que el `EnumTypeHandler` es un handler especial en el sentido de que no maneja una clase específica, como los demás handlers sino cualquier clase que extiende de `Enum`</b>

Sin embargo, puede que no queramos guardar nombres. Nuestro DBA puede insistir en que usemos un entero en su lugar. Muy sencillo: añade un `EnumOrdinalTypeHandler` a las sección de `typeHandlers` de tu fichero de configuración y ahora todos los `RoundingMode` se mapearán a un entero usando su valor ordinal.

```xml
<!-- mybatis-config.xml -->
<typeHandlers>
  <typeHandler handler="org.apache.ibatis.type.EnumOrdinalTypeHandler" javaType="java.math.RoundingMode"/>
</typeHandlers>
```

Pero ¿y si quieres mapear el mismo `Enum` a un string en un sitio pero a un entero en otro?

El mapeo automático siempre usará `EnumOrdinalTypeHandler`, así que si queremos usar el clásico `EnumTypeHandler`, debemos indicarlo establiencidolo esplícitamente su uso en los statements.

Los mappers no se tratarán hasta la sección siguiente asi que si esta es tu primera lectura de la documentación quizá prefieras saltarte esta sección por ahora y volver más tarde).

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
    <result column="roundingMode" property="roundingMode" typeHandler="org.apache.ibatis.type.EnumTypeHandler"/>
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

Observa que esto nos fuerza a usar un `resultMap` en lugar de un `resultType` en nuestros statements tipo select.

## objectFactory

Cada vez que MyBatis crea una nueva instancia de un objeto de resultado usa una instancia de ObjectFactory para hacerlo. El ObjectFactory por defecto no hace mucho más que instanciar la clase destino usando su constructor por defecto, o el constructor que se ha parametrizado en su caso. Es posible sobrescribir el comportamiento por defecto creando tu propio ObjectFactory. Por ejemplo:

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

La interfaz ObjectFactory es muy sencilla. Contiene dos métodos de creación, uno para el constructor por defecto y otro para el constructor parametrizado. El método isCollection identifica los tipos de colección. Adicionalmente, el método setProperties sirve para configurar el ObjectFactory. Las propiedades definidas en el cuerpo del elemento objectFactory se pasan al método setProperties después de que el ObjectFactory haya sido inicializado.

## plugins

MyBatis permite interceptar las llamadas en ciertos puntos de la ejecución de un mapped statement. Estos son ejemplos de métodos de las interfaces admitidas que los plugins pueden interceptar:

- Executor (update, query, queryCursor, flushStatements, commit, rollback, getTransaction, close, isClosed)
- ParameterHandler (getParameterObject, setParameters)
- ResultSetHandler (handleResultSets, handleCursorResultSets, handleOutputParameters)
- StatementHandler (prepare, parameterize, batch, update, query, queryCursor)

Los detalles de estos métodos se pueden conocer observando sus firmas y el código fuente de los mismos que está disponible en el sitio de MyBatis. Es recomendable que comprendas el funcionamiento del método que estas sobrescribiendo siempre que vayas a hacer algo más complejo que monitorizar llamadas. Ten en cuenta que si modificas el comportamiento de alguno de estos métodos existe la posibilidad de que rompas el funcionamiento de MyBatis. Estas clases son de bajo nivel y por tanto debes usar los plugins con cuidado.

Utilizar un plugin es muy sencillo para la potencia que ofrecen. Simplemente implementa el interfaz Interceptor y asegúrate de especificar las signaturas que quieres interceptar.

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
    // implement pre processing if need
    Object returnObject = invocation.proceed();
    // implement post processing if need
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

El plugin anterior interceptará cualquier llamada al método “update” en la instancia de Executor, que es un objeto interno que se encarga de la ejecución a bajo nivel de los mapped statements.

<span class="label important">NOTA</span> <strong>Acerca de sobrescribir la clase Configuration</strong>

Además de modificar el comportamiento de MyBatis mediante los plugins, también es posible sobrescribir la clase Configuración por completo. Extiende la clase, sobrescribe sus métodos y pásala como parámetro en la llamada al método SqlSessionFactoryBuilder.build(myConfig). Nuevamente, ten cuenta que esto puede afectar seriamente al funcionamiento de MyBatis así que úsalo con cuidado.

## environments

En MyBatis pueden configurarse varios entornos. De esta forma puedes usar tus SQL Maps en distintas bases de datos por muchos motivos. Por ejemplo puede que tengas una configuración distinta para tus entornos de desarrollo, pruebas y producción. O quizá tengas varias bases de datos en producción que comparten el esquema y quieres usar los mismos SQL maps sobre todas ellas. Como ves, hay muchos casos.

<strong>Debes recordar un asunto importante. Cuando configures varios entornos, solo será posible usar UNO por cada instancia de SqlSessionFactory.</strong>

Por lo tanto, si quieres conectar a dos bases de datos, deberás crear dos instancias de SqlSessionFactory, una para cada cual. Para el caso de tres bases de datos necesitarás tres instancias y así sucesivamente. Es fácil de recordar:

- <strong>Una instancia de SqlSessionFactory por base de datos</strong>

Para indicar qué entorno debe utilizarse, debes informar el parámetro opcional correspondiente en la llamada al SqlSessionFactoryBuilder. Existen dos métodos que aceptan el entorno:

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, environment);
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, environment,properties);
```

Si se omite el entorno se usará el entorno por defecto:

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader,properties);
```

El elemento environments contiene la configuración del entorno:

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

Observa que las secciones importantes son:

- El ID del entorno por defecto (ej. default=”development”).
- El ID de de cada entorno definido (ej. id=”development”).
- La configuración del TransactionManager (ej. type=”JDBC”)
- La configuración del DataSource (ej. type=”POOLED”)

El ID del entorno por defecto y de los entornos existentes son auto-explicativos. Puedes nombrarlos como más te guste, tan sólo asegúrate de que el valor por defecto coincide con un entorno existente.

<strong>transactionManager</strong>

MyBatis incluye dos tipos de TransactionManager (ej. type=”\[JDBC|MANAGED\]”):

- JDBC – Este TransactionManager simplemente hace uso del las capacidades de commit y rollback de JDBC. Utiliza la conexión obtenida del DataSource para gestionar la transacción. By default, it enables auto-commit when closing the connection for compatibility with some drivers. However, for some drivers, enabling auto-commit is not only unnecessary, but also is an expensive operation. So, since version 3.5.10, you can skip this step by setting the \"skipSetAutoCommitOnClose\" property to true. For example:
  ```xml
  <transactionManager type="JDBC">
    <property name="skipSetAutoCommitOnClose" value="true"/>
  </transactionManager>
  ```
- MANAGED – Este TransactionManager no hace nada. No hace commit ni rollback sobre la conexión. En su lugar, permite que el contenedor gestione el ciclo de vida completo de la transacción (ej. Spring o un servidor de aplicaciones JEE). Por defecto cierra la conexión. Sin embargo, algunos contenedores no esperan que la conexión se cierre y por tanto, si necesitas cambiar este comportamiento, informa la propiedad closeConnection a false. Por ejemplo:
  ```xml
  <transactionManager type="MANAGED">
    <property name="closeConnection" value="false"/>
  </transactionManager>
  ```

<span class="label important">NOTA</span> Si estás pensando en usar MyBatis con Spring no necesitas configurar ningún TransactionManager porque el módulo de Spring configurará el suyo propio sobrescribiendo cualquier otra configuración previa.

Ninguno de estos TransactionManagers necesita ninguna propiedad. Sin embargo ambos son Type Aliases, es decir, en lugar de usarlos puedes informar el nombre totalmente cualificado o el Type Alias de tu propia implementación del interfaz TransactionFactory:

```java
public interface TransactionFactory {
  default void setProperties(Properties props) { // Since 3.5.2, change to default method
    // NOP
  }
  Transaction newTransaction(Connection conn);
  Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);
}
```

Todas las propiedades que configures en el XML se pasarán al método setProperties() tras la instanciación de la clase. Tu implementación debe crear una implementación de Transaction, que a su vez es también un interfaz muy sencillo:

```java
public interface Transaction {
  Connection getConnection() throws SQLException;
  void commit() throws SQLException;
  void rollback() throws SQLException;
  void close() throws SQLException;
  Integer getTimeout() throws SQLException;
}
```

Con estos dos interfaces puedes personalizar por completo la forma en la que MyBatis gestiona las transacciones.

<strong>dataSource</strong>

El elemento dataSource sirve para configurar la forma de obtener conexiones JDBC mediante la interfaz DataSource JDBC estándar.

- La mayoría de las aplicaciones que usen MyBatis configurarán el dataSource como se muestra en el ejemplo. Sin embargo, esta configuración no es obligatoria. Ten en cuenta, aun así, que el dataSource es necesario para utilizar Lazy Loading.

Hay tres tipos de dataSources pre-construidos (ej. type=”????”):

<strong>UNPOOLED</strong> – Esta implementación de DataSource abre y cierra una conexión JDBC cada vez que se solcita una conexión. Aunque es un poco lento, es una buena elección para aplicaciones que no necesitan la velocidad de tener conexiones abiertas de forma inmediata. Las bases de datos tienen un rendimiento distinto en cuanto al rendimiento que aportan con este tipo de DataSource, para algunas de ellas no es muy importante tener un pool y por tanto esta configuración es apropiada. El DataSource UNPOOLED tiene las siguientes opciones de configuración:

- `driver` – El nombre completamente cualificado de la clase java del driver JDBC (NO de la clase DataSource en el caso de que tu driver incluya una).
- `url` – La URL de la instancia de base de datos.
- `username` – El usuario de conexión.
- `password` – La password de conexión.
- `autoCommit` – Configura el modo de confirmación automática de las nuevas conexiones. Si se omite, se usa el valor por defecto del driver.
- `defaultTransactionIsolationLevel` – El nivel de aislamiento por defecto con el que se crearán las conexiones.
- `defaultNetworkTimeout` – The default network timeout value in milliseconds to wait for the database operation to complete. See the API documentation of `java.sql.Connection#setNetworkTimeout()` for details.

Opcionalmente, puedes también pasar propiedades al driver de la base de datos. Para ello prefija las propiedades con “driver.”, por ejemplo:

- `driver.encoding=UTF8`

Esto pasaría la propiedad “encoding” con el valor “UTF8” al driver de base datos mediante el método DriverManager.getConnection(url, driverProperties).

<strong>POOLED</strong> – Esta implementación de DataSource hace usa un pool de conexiones para evitar el tiempo necesario en realizar la conexión y autenticación cada vez que se solicita una nueva instancia de conexión. Este es un enfoque habitual en aplicaciones Web concurrentes para obtener el mejor tiempo de respuesta posible.

Además de las propiedades de (UNPOOLED) hay otras muchas propiedades que se pueden usar para configurar el DataSource POOLED:

- `poolMaximumActiveConnections` – Número máximo de conexiónes activas que pueden existir de forma simultánea. Por defecto: 10
- `poolMaximumIdleConnections` – Número máximo de conexiones libres que pueden existir de forma simultánea.
- `poolMaximumCheckoutTime` – Tiempo máximo que puede permanecer una conexión fuera del pool antes de que sea forzosamente devuelta. Por defecto: 20000ms (20 segundos)
- `poolTimeToWait` – Este es un parámetro de bajo nivel que permite escribir un log y reintentar la adquisición de una conexión en caso de que no se haya conseguido la conexión transcurrido un tiempo razonable (esto evita que se produzcan fallos constantes y silenciosos si el pool está mal configurado). Por defecto: 20000ms (20 segundos)
- `poolMaximumLocalBadConnectionTolerance` – This is a low level setting about tolerance of bad connections got for any thread. If a thread got a bad connection, it may still have another chance to re-attempt to get another connection which is valid. But the retrying times should not more than the sum of `poolMaximumIdleConnections` and `poolMaximumLocalBadConnectionTolerance`. Default: 3 (Since: 3.4.5)
- `poolPingQuery` – La query de ping (sondeo) que se envía a la base de datos para verificar que la conexión funciona correctamente y que está lista para aceptar nuevas peticiones de conexión. El valor por defecto es \"NO PING QUERY SET\", que hará que la mayoría de los drivers de base de datos devuelvan un error con un mensaje de error decente.
- `poolPingEnabled` – Habilita o inhabilita la query de ping. Si está habilitada deberías informar también la propiedad poolPingQuery con una sentencia SQL (preferentemente una rápida). Por defecto: false.
- `poolPingConnectionsNotUsedFor` – Configura la frecuencia con la que se ejecutará la sentencia poolPingQuery. Normalmente se iguala al timeout de la conexión de base de datos para evitar pings innecesarios. Por defecto: 0 (todas las conexiones se testean continuamente – solo si se ha habilitado poolPingEnabled).

<strong>JNDI</strong> – Esta implementación de DataSource está pensada para ser usada en contenedores como Spring o los servidores de aplicaciones JEE en los que es posible configurar un DataSource de forma externa y alojarlo en el contexto JNDI. Esta configuración de DataSource requiere solo dos propiedades:

- `initial_context` – Propiedad que se usa para realizar el lookup en el InitialContext (initialContext.lookup(initial\_context)). Esta propiedad es opcional, si no se informa, se buscará directamente la propiedad data\_source.
- `data_source` – Es el contexto donde se debe buscar el DataSource. El DataSource se buscará en el contexto resultado de buscar data\_source en el InitialContext o si no se ha informado la propiedad se buscará directamente sobre InitialContext.

Al igual que en las otras configuraciones de DataSource. Es posible enviar propiedades directamente al InitialContext prefijando las propiedades con “env.”, por ejemplo:

- `env.encoding=UTF8`

Enviará la propiedad “encoding” y el valor “UTF-8” al constructor del InitialContext durante su instanciación.

You can plug any 3rd party DataSource by implementing the interface `org.apache.ibatis.datasource.DataSourceFactory`:

```java
public interface DataSourceFactory {
  void setProperties(Properties props);
  DataSource getDataSource();
}
```

The `org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory` puede extenderse para crear nuevos adaptadores. Por ejemplo, este es el código necesario para integrar C3P0:

```java
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3P0DataSourceFactory extends UnpooledDataSourceFactory {

  public C3P0DataSourceFactory() {
    this.dataSource = new ComboPooledDataSource();
  }
}
```

Para configurarlo, añade una propiedad por cada método al que quieres que llame MyBatis. A continuación se muestra una configuración de ejemplo para conectar con una base de datos PostgresSQL:

```xml
<dataSource type="org.myproject.C3P0DataSourceFactory">
  <property name="driver" value="org.postgresql.Driver"/>
  <property name="url" value="jdbc:postgresql:mydb"/>
  <property name="username" value="postgres"/>
  <property name="password" value="root"/>
</dataSource>
```

## databaseIdProvider

MyBatis puede ejecutar sentencias distintas en función del fabricante (vendor) de tu base de datos. El soporte de múltiples bases de datos se basa en el atributo de `databaseId` de los mapped statements. MyBatis cargará todos los statements que no tengan atributo `databaseId` attribute o aquellos cuyo `databaseId` coincida con el valor en curso. Si se encuentra un statement con y sin atributo `databaseId` el último se descartará. Para activar el soporte de multi vendor añade un `databaseIdProvider` al fichero mybatis-config.xml file de la siguiente forma:

```xml
<databaseIdProvider type="DB_VENDOR" />
```

La implementación DB\_VENDOR del databaseIdProvider establece como databaseId el String devuelto por `DatabaseMetaData#getDatabaseProductName()`. Como normalmente este string es demasiado largo, y además, distintas versiones del mismo producto devuelven valores similares, puedes traducirlo a un valor más corto añadiendo propiedades de la siguiente forma:

```xml
<databaseIdProvider type="DB_VENDOR">
  <property name="SQL Server" value="sqlserver"/>
  <property name="DB2" value="db2"/>
  <property name="Oracle" value="oracle" />
</databaseIdProvider>
```

Cuando se añaden propiedades, el databaseIdProvider DB\_VENDOR devuelve el primer valor que corresponde a la primera clave encontrada en el nombre devuelto por `DatabaseMetaData#getDatabaseProductName()` o null si no se encuentra ninguna. En este caso, si `getDatabaseProductName()` devuelve \"Oracle (DataDirect)\" el databaseId se informará con \"oracle\".

Puedes construir tu propio DatabaseIdProvider implementando la interfaz `org.apache.ibatis.mapping.DatabaseIdProvider` y registrandolo en el fichero mybatis-config.xml:

```java
public interface DatabaseIdProvider {
  default void setProperties(Properties p) { // Since 3.5.2, change to default method
    // NOP
  }
  String getDatabaseId(DataSource dataSource) throws SQLException;
}
```

## mappers

Ahora que se ha configurado el comportamiento de MyBatis con todos los elementos de configuración comentados estamos listos para definir los SQL mapped statements (sentencias SQL mapeadas). Primeramente necesitaremos indicarle a MyBatis dónde encontrarlos. Java no ofrece muchas posibilidades de auto-descubrimiento así que la mejor forma es simplemente decirle a MyBatis donde encontrar los ficheros de mapeo. Puedes utilizar referencias tipo classpath, o tipo path o referencias url completamente cualificadas (incluyendo file&#58;///) . Por ejemplo:

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

Esta configuración sólo indica a MyBatis cuáles son los ficheros de mapeo. El resto de la configuración se encuentra dentro de estos ficheros, y eso es de lo que hablaremos en el siguiente apartado.
