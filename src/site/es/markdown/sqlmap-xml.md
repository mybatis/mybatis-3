title: MyBatis 3 | Ficheros XML de mapeo
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

# Ficheros XML de mapeo

La potencia de MyBatis reside en los Mapped Statements. Aquí es donde está la magia. Para lo potentes que son, los ficheros XML de mapeo son relativamente simples. Sin duda, si los comparas con el código JDBC equivalente comprobarás que ahorras el 95% del código.

Los ficheros XML de mapeos SQL solo tienen unos pocos elementos de alto nivel (en el orden en el que deberían definirse):

- `cache` – Configuración de la caché para un namespace.
- `cache-ref` – Referencia a la caché de otro namespace.
- `resultMap` – El elemento más complejo y potente que describe como cargar tus objetos a partir de los ResultSets.
- <del>`parameterMap` – Deprecada! Antigua forma de mapear parámetros. Se recomienda el uso de parámetros en línea. Este elemento puede ser eliminado en futuras versiones. No se ha documentado en este manual.</del>
- `sql` – Un trozo de SQL reusable que puede utilizarse en otras sentencias.
- `insert` – Una sentencia INSERT.
- `update` – A Una sentencia UPDATE.
- `delete` – Una sentencia DELETE.
- `select` – Una sentencia SELECT.

Las siguientes secciones describen estos elementos en detalle, comenzando con los propios elementos.

## select

El select statement es uno de los elementos que más utilizarás en MyBatis. No es demasiado útil almacenar datos en la base de datos si no puedes leerlos, de hecho las aplicaciones suelen leer bastantes más datos de los que modifican. Por cada insert, update o delete posiblemente haya varias selects. Este es uno de los principios básicos de MyBatis y la razón por la que se ha puesto tanto esfuerzo en las consultas y el mapeo de resultados. El select statement es bastante simple para los casos simples. Por ejemplo:

```xml
<select id="selectPerson" parameterType="int" resultType="hashmap">
  SELECT * FROM PERSON WHERE ID = #{id}
</select>
```

Esta sentencia se llama “selectPerson”, recibe un parámetro de tipo in (o Integer), y devuelve una HashMap usando los nombres de columna como clave y los valores del registro como valores.

Observa la notación utilizada para los parámetros:

```java
#{id}
```

Esto le indica a MyBatis que cree un parámetro de PreparedStatement. Con JDBC, ese parámetro iría identificado con una “?” en la select que se le pasa al PreparedStatement, algo así:

```java
// Código JDBC similar, NO MyBatis…
String selectPerson = "SELECT * FROM PERSON WHERE ID=?";
PreparedStatement ps = conn.prepareStatement(selectPerson);
ps.setInt(1,id);
```

JDBC requiere mucho más código para extraer los resultados y mapearlos a una instancia de un objetos, que es precisamente lo que MyBatis evita que tengas que hacer. Aun queda mucho por conocer sobre los parámetros y el mapeo de resultados. Todos sus detalles merecen su propio capítulo, y serán tratados más adelante.

El select statement tiene más atributos que te permiten configurar como debe comportarse cada select statement.

```xml
<select
  id="selectPerson"
  parameterType="int"
  parameterMap="deprecated"
  resultType="hashmap"
  flushCache="false"
  useCache="true"
  timeout="10"
  fetchSize="256"
  statementType="PREPARED"
  resultSetType="FORWARD_ONLY">
```

| Atributo | Descripción |
| --- | --- |
| `id` | Un identificador único dentro del namespace que se utiliza para identificar el statement. |
| `parameterType` | El nombre completamente cualificado de la clase o el alias del parámetro que se pasará al statement. Este atributo es opcional porque MyBatis puede calcular el TypeHandler a utilizar a partir del parametro actual usado en la invocación al statement. Por defecto: no informado. |
| <del>parameterMap</del> | <del>Este es un atributo obsoleto que permite referenciar a un elemento parameterMap externo. Se recomienda utilizar mapeos en línea (in-line) y el atributo parameterType.</del> |
| `resultType` | El nombre completamente cualificado o el alias del tipo de retorno de este statement. Ten en cuenta que en el caso de las colecciones el parámetro debe ser el tipo contenido en la colección, no el propio tipo de la colección. Puedes utilizar resultType o resultMap, pero no ambos. |
| `resultMap` | Una referencia una un resultMap externo. Los resultMaps son la característica más potente de MyBatis, con un conocimiento detallado de los mismos, se pueden resolver muchos casos complejos de mapeos. Puedes utilizar resultMap O resultType, pero no ambos. |
| `flushCache` | Informar esta propiedad a true hará que la cache local y la de segundo nivel se vacíen cada vez que se llame a este statement. Por defecto es false para select statements. |
| `useCache` | Informar esta propiedad a true hará que los resultados de la ejecución de este statement se cacheen en la caché de segundo nivel. Por defecto es true para las select statements. |
| `timeout` | Establece el número de segundos que el driver esperará a que la base de datos le devuelva una respuesta antes de lanzar una excepción. Por defecto: no informado (depende del driver de base de datos). |
| `fetchSize` | Este es un atributo que “sugiere” al driver que devuelva los resultados en bloques de filas en el número indicado por el parámetro. Por defecto: no informado (depende del driver de base de datos). |
| `statementType` | Puede valer STATEMENT, PREPARED o CALLABLE. Hace que MyBatis use Statement, PreparedStatement o CallableStatement respectivamente. Por defecto: PREPARED. |
| `resultSetType` | Puede valer FORWARD\_ONLY\|SCROLL\_SENSITIVE\|SCROLL\_INSENSITIVE\|DEFAULT. Si no se indica, se utiliza el valor configurado de `defaultResultSetType`; DEFAULT utiliza el valor predeterminado del controlador JDBC. |
| `databaseId` | Si hay un DatabaseIdProvider configurado. MyBatis cargará todos los statements sin el atributo `databaseId` o aquellos con un `databaseId` que coincide con el actual. Si hay dos versiones del mismo statement, una con un `databaseId` coincidente y otra sin él, se descartará la versión sin `databaseId`. |
| `resultOrdered` | De aplicación exclusiva para select anidadas. Si es true, se asume que los resultados anidados están agrupados de forma que cuando se lee un nuevo resultado principal nuevo, no habrá más referencias a resultados principales anteriores. De esta forma los resultados anidados se rellenarán de una manera mucho ás eficiente en términos de memoria. Defecto: `false`. |
| `resultSets` | This is only applicable for multiple result sets. It lists the result sets that will be returned by the statement and gives a name to each one. Names are separated by commas. |
| `affectData` | Set this to true when writing a INSERT, UPDATE or DELETE statement that returns data so that the transaction is controlled properly. Also see [Transaction Control Method](./java-api.html#transaction-control-methods). Default: `false` (since 3.5.12) |
[Atributos de Select]

## insert, update and delete

<a id="insert_update_and_delete"></a>

Los insert, update y delete statements son muy similares en su implementación:

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

| Atributo | Descripción |
| --- | --- |
| `id` | Un identificador único dentro del namespace que se utiliza para identificar el statement. |
| `parameterType` | El nombre completamente cualificado de la clase o el alias del parámetro que se pasará al statement. Este atributo es opcional porque MyBatis puede calcular el TypeHandler a utilizar a partir del parametro actual usado en la invocación al statement. Por defecto: no informado. |
| <del>`parameterMap`</del> | <del>Método deprecado de referirse a un parameterMap externo. Usa mapeos inline y el atributo parameterType.</del> |
| `flushCache` | Informar esta propiedad a true hará que la caché se vacíe cada vez que se llame a este statement. Por defecto es false para select statements. |
| `timeout` | Establece el número máximo de segundos que el driver esperará a que la base de datos le devuelva una respuesta antes de lanzar una excepción. Por defecto: no informado (depende del driver de base de datos). |
| `statementType` | Puede valer STATEMENT, PREPARED o CALLABLE. Hace que MyBatis use Statement, PreparedStatement o CallableStatement respectivamente. Por defecto: PREPARED. |
| `useGeneratedKeys` | (solo en insert y update) Indica a MyBatis que utilice el método getGeneratedKeys de JDBC para recuperar las claves autogeneras automáticamente por la base de datos. (ej. campos autoincrementales en SGBD como MySQL o SQL Server). Por defecto: false |
| `keyProperty` | (solo en insert y update) Indica la propiedad a la que MyBatis debe asignar la clave autogenerada devuelva por getGeneratedKeys o por un elemento hijo de tipo selectKey. Por defecto: no informado. Puede contener una lista de nombres seperados por comas en el caso de que se esperen varios valores autogenerados. |
| `keyColumn` | (solo en insert y update) Indica el nombre de la columna en tabla con clave generada. Solo se requiere en algunas bases de datos (como PostgreSQL) donde la columna clave no es la primera de la tabla. Puede contener una lista de nombres seperados por comas en el caso de que se esperen varios valores autogenerados. |
| `databaseId` | Si hay un DatabaseIdProvider configurado. MyBatis cargará todos los statements sin el atributo `databaseId` o aquellos con un `databaseId` que coincide con el actual. Si hay dos versiones del mismo statement, una con un `databaseId` coincidente y otra sin él, se descartará la versión sin `databaseId`. |
[Insert, Update and Delete Attributes]

A continuación se muestran unos ejemplos de insert, update y delete.

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

Tal y como se ha indicado, insert es algo más complejo dado que dispone de algunos atributos extra para gestionar la generación de claves de varias formas distintas.

Primeramente, si tu base de datos soporta la auto-generación de claves (ej. MySQL y SQL Server), entonces puedes simplemente informar el atributo useGeneratedKeys=”true” e informar también en keyProperty el nombre del la propiedad donde guardar el valor y ya has terminado. Por ejemplo, si la columna id de la tabla Author del ejemplo siguiente fuera autogenerada el insert statement se escribiría de la siguiente forma:

```xml
<insert id="insertAuthor" useGeneratedKeys="true"
    keyProperty="id">
  insert into Author (username,password,email,bio)
  values (#{username},#{password},#{email},#{bio})
</insert>
```

If your database also supports multi-row insert, you can pass a list or an array of `Author`s and retrieve the auto-generated keys.

```xml
<insert id="insertAuthor" useGeneratedKeys="true"
    keyProperty="id">
  insert into Author (username, password, email, bio) values
  <foreach item="item" collection="list" separator=",">
    (#{item.username}, #{item.password}, #{item.email}, #{item.bio})
  </foreach>
</insert>
```

MyBatis puede tratar las claves autogeneradas de otra forma para el caso de las bases de datos que no soportan columnas autogeneradas, o porque su driver JDBC no haya incluido aun dicho soporte.

A continuación se muestra un ejemplo muy simple que genera un id aleatorio (algo que posiblemente nunca harás pero que demuestra la flexibilidad de MyBatis y cómo MyBatis ignora la forma en la que se consigue la clave):

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

En el ejemplo anterior, el selectKey statement se ejecuta primero, la propiedad id de Author se informará y posteriormente se invocará al insert statement. Esto proporciona un comportamiento similar a la generación de claves en base de datos sin complicar el código Java.

El elemento selectKey tiene el siguiente aspecto:

```xml
<selectKey
  keyProperty="id"
  resultType="int"
  order="BEFORE"
  statementType="PREPARED">
```

| Attribute | Description |
| --- | --- |
| `keyProperty` | La propiedad destino con la que debe informarse el resultado del selectKey statement. Puede contener una lista de nombres seperados por comas en el caso de que se esperen varios valores autogenerados. |
| `keyColumn` | Los nombres de columnas en el ResultSet que corresponden con las propiedades. Puede contener una lista de nombres seperados por comas en el caso de que se esperen varios valores autogenerados. |
| `resultType` | El tipo de retorno. Es necesario especificarlo porque MyBatis no infiere el tipo de resultado de `selectKey`. MyBatis permite usar cualquier tipo simple, incluyendo Strings. |
| `order` | Puede contener BEFORE o AFTER. Si se informa a BEFORE, entonces la obtención de la clave se realizará primero, se informará el campo indicado en keyProperty y se ejecutará la insert. Si se informa a AFTER se ejecuta primero la insert y después la selectKey – Esto es habitual en bases de datos como Oracle que soportan llamadas embebidas a secuencias dentro de una sentencia insert. |
| `statementType` | Al igual que antes, MyBatis soporta sentencias de tipo STATEMENT, PREPARED and CALLABLE que corresponden Statement, PreparedStatement y CallableStatement respectivamente. |
[selectKey Attributes]

As an irregular case, some databases allow INSERT, UPDATE or DELETE statement to return result set (e.g. `RETURNING` clause of PostgreSQL and MariaDB or `OUTPUT` clause of MS SQL Server). This type of statement must be written as `<select>` to map the returned data.

```xml
<select id="insertAndGetAuthor" resultType="domain.blog.Author"
      affectData="true" flushCache="true">
  insert into Author (username, password, email, bio)
  values (#{username}, #{password}, #{email}, #{bio})
  returning id, username, password, email, bio
</select>
```

## sql

Este elemento se utiliza para definir un fragmento reusable de código SQL que puede ser incluido en otras sentencias. It can be statically (during load phase) parametrized. Different property values can vary in include instances. Por ejemplo:

```xml
<sql id="userColumns"> ${alias}.id,${alias}.username,${alias}.password </sql>
```

Este fragmento de SQL puede ser incluido en otra sentencia, por ejemplo:

```xml
<select id="selectUsers" resultType="map">
  select
    <include refid="userColumns"><property name="alias" value="t1"/></include>,
    <include refid="userColumns"><property name="alias" value="t2"/></include>
  from some_table t1
    cross join some_table t2
</select>
```

Property value can be also used in include refid attribute or property values inside include clause, for example:

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

## Parameters

En todos los statements anteriores se han mostrado ejemplos de parámetros simples. Los parámetros son elementos muy potentes en MyBatis. En los casos simples, probablemente el 90% de los casos, no hay mucho que decir sobre ellos, por ejemplo:

```xml
<select id="selectUsers" parameterType="int" resultType="User">
  select id, username, password
  from users
  where id = #{id}
</select>
```

El ejemplo anterior muestra un mapeo muy simple de parámetro con nombre. El atributo parameterType se ha informado a “int”, por lo tanto el nombre del parámetro puede ser cualquiera. Los tipos primitivos y los tipos de datos simples como Integer o String no tienen propiedades relevantes y por tanto el parámetro será reemplazado por su valor. Sin embargo, si pasas un objeto complejo, entonces el comportamiento es distinto. Por ejemplo:

```xml
<insert id="insertUser" parameterType="User">
  insert into users (id, username, password)
  values (#{id}, #{username}, #{password})
</insert>
```

Si se pasa un objeto de tipo User como parámetro en este statement, se buscarán en él las propiedades id, username y password y sus valores se pasarán como parámetros de un PreparedStatement.

Esta es una Buena forma de pasar parámetros a statements. Pero los parameter maps (mapas de parámetros) tienen otras muchas características.features of parameter maps.

Primeramente, es posible especificar un tipo de dato concreto.

```java
#{property,javaType=int,jdbcType=NUMERIC}
```

Como en otros casos, el tipo de Java (javaType) puede casi siempre obtenerse del objeto recibido como parámetro, salvo si el objeto es un HashMap. En ese caso debe indicarse el javaType para asegurar que se usa el TypeHandler correcto.

<span class="label important">NOTA</span> El tipo JDBC es obligatorio para todas las columnas que admiten null cuando se pasa un null como valor. Puedes investigar este tema por tu cuenta leyendo los JavaDocs del método PreparedStatement.setNull().

Si quieres customizar aun más el tratamiento de tipos de datos, puedes indicar un TypeHandler específico (o un alias), por ejemplo:

```java
#{age,javaType=int,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
```

Comienza a parecer demasiado verboso, pero lo cierto es que rara vez necesitaras nada de esto.

Para los tipos numéricos hay un atributo numericScale que permite especificar cuantas posiciones decimales son relevantes.

```java
#{height,javaType=double,jdbcType=NUMERIC,numericScale=2}
```

Finalmente, el atributo mode te permite especificar parámetros IN, OUT o INOUT. Si un parámetro es OUT o INOUT, el valor actual de las propiedades del objeto pasado como parámetro será modificado. Si el mode=OUT (o INOUT) y el jdbcType=CURSOR (ej. Oracle REFCURSOR), debes especificar un resultMap para mapear el ResultSet al tipo del parámetro. Ten en cuenta que el atributo javaType es opcional en este caso, dado que se establecerá automáticamente al valor ResultSet en caso de no haberse especificado si el jdbcType es CURSOR.

```java
#{department, mode=OUT, jdbcType=CURSOR, javaType=ResultSet, resultMap=departmentResultMap}
```

MyBatis también soporta tipos de datos avanzados como los structs, pero en este caso debes indicar en el statement el jdbcTypeName en la declaración del parámetro de tipo OUT.

```java
#{middleInitial, mode=OUT, jdbcType=STRUCT, jdbcTypeName=MY_TYPE}
```

A pesar de estas potentes opciones, la mayoría de las veces simplemente debes especificar el nombre de la propiedad y MyBatis adivinará lo demás. A lo sumo, deberás especificar los jdbcTypes para las columnas que admiten nulos.

```java
#{firstName}
#{middleInitial,jdbcType=VARCHAR}
#{lastName}
```

#### Sustitución de Strings

Por defecto, usar la sintaxis #{} hace que MyBatis genere propiedades de PreparedStatement y que asigne los valores a parámetros de PreparedStatement de forma segura (ej. ?). Aunque esto es más seguro, más rápido y casi siempre la opción adecuada, en algunos casos sólo quieres inyectar un trozo de texto sin modificaciones dentro de la sentencia SQL. Por ejemplo, para el caso de ORDER BY, podrías utilizar algo así:

```java
ORDER BY ${columnName}
```

En este caso MyBatis no alterará el contenido del texto.

String Substitution can be very useful when the metadata(i.e. table name or column name) in the sql statement is dynamic, for example, if you want to `select` from a table by any one of its columns, instead of writing code like:

```java
@Select("select * from user where id = #{id}")
User findById(@Param("id") long id);

@Select("select * from user where name = #{name}")
User findByName(@Param("name") String name);

@Select("select * from user where email = #{email}")
User findByEmail(@Param("email") String email);

// and more "findByXxx" method
```

you can just write:

```java
@Select("select * from user where ${column} = #{value}")
User findByColumn(@Param("column") String column, @Param("value") Object value);
```

in which the `${column}` will be substituted directly and the `#{value}` will be \"prepared\". Thus you can just do the same work by:

```java
User userOfId1 = userMapper.findByColumn("id", 1L);
User userOfNameKid = userMapper.findByColumn("name", "kid");
User userOfEmail = userMapper.findByColumn("email", "noone@nowhere.com");
```

This idea can be applied to substitute the table name as well.

<span class="label important">NOTA</span> No es seguro recoger un texto introducido por el usuario e inyectarlo en una sentencia SQL. Esto permite ataques de inyección de SQL y por tanto debes impedir que estos campos se informen con la entrada del usuario, o realizar tus propias comprobaciones o escapes.

## Result Maps

El elemento resultMap es el elemento más importante y potente de MyBatis. Te permite eliminar el 90% del código que requiere el JDBC para obtener datos de ResultSets, y en algunos casos incluso te permite hacer cosas que no están siquiera soportadas en JDBC. En realidad, escribir un código equivalente para realizar algo similar a un mapeo para un statement complejo podría requerir cientos de líneas de código. El diseño de los ResultMaps es tal, que los statements simples no requieren un ResultMap explícito, y los statements más complejos requieren sólo la información imprescindible para describir relaciones.

Ya has visto algunos ejemplos de un statement sencillo que no requiere un ResultMap explícito. Por ejemplo:

```xml
<select id="selectUsers" resultType="map">
  select id, username, hashedPassword
  from some_table
  where id = #{id}
</select>
```

Este statement simplemente obtiene como resultado una HashMap que contiene como claves todas las columnas, tal y como se ha especificado en el atributo resultType. Aunque es muy útil en muchos casos, una HashMap no contribuye a un buen modelo de dominio. Es más probable que tu aplicación use JavaBeans o POJOs (Plain Old Java Objects) para el modelo de dominio. MyBatis soporta ambos. Dado el siguiente JavaBean:

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

Basándose en la especificación de JavaBeans, la clase anterior tiene 3 propiedades: ip, username y hashedPassword. Todas ellas coinciden exactamente con los nombres de columna en la sentencia select.

Este JavaBean puede mapearse desde un ResultSet de forma casi tan sencilla como la HashMap.

```xml
<select id="selectUsers" resultType="com.someapp.model.User">
  select id, username, hashedPassword
  from some_table
  where id = #{id}
</select>
```

Y recuerda que los TypeAliases son tus amigos. Úsalos y de esa forma no tendrás que escribir constantemente el nombre totalmente cualificado (fully qualified). Por ejemplo:

```xml
<!-- In Config XML file -->
<typeAlias type="com.someapp.model.User" alias="User"/>

<!-- In SQL Mapping XML file -->
<select id="selectUsers" resultType="User">
  select id, username, hashedPassword
  from some_table
  where id = #{id}
</select>
```

En estos casos MyBatis crea automáticamente un RestulMap entre bastidores para mapear las columnas a las propiedades del JavaBean en base a sus nombres. Si los nombres de las columnas no coinciden exactamente, puedes emplear alias en los nombres de columnas de la sentencia SQL (una característica estándar del SQL) para hacer que coincidan. Por ejemplo:

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

Lo mejor de los ResultMaps es que ya has aprendido mucho sobre ellos ¡y ni siquiera los has visto! Los casos sencillos no requieren nada más que lo que ya has visto. Solo como ejemplo, veamos qué aspecto tendría el último ejemplo utilizando un ResultMap externo, lo cual es otra forma de solucionar las divergencias entre los nombres de columnas y de propiedades.

```xml
<resultMap id="userResultMap" type="User">
  <id property="id" column="user_id" />
  <result property="username" column="user_name"/>
  <result property="hashedPassword" column="hashed_password"/>
</resultMap>
```

Y el statement que las referencia utiliza para ello el atributo resultMap (fíjate que hemos eliminado el atributo resultType). Por ejemplo:

```xml
<select id="selectUsers" resultMap="userResultMap">
  select user_id, user_name, hashed_password
  from some_table
  where id = #{id}
</select>
```

Ojalá todo fuera tan sencillo.

#### Mapeo de resultados avanzado

MyBatis fue creado con una idea en mente: las bases de datos no siempre son como a ti te gustaría que fueran. Nos encantaría que todas las bases de datos estuvieran en 3ª forma normal o BCNF, pero no lo están. Sería genial que una base de datos encajara perfectamente con todas las aplicaciones que la usan pero no es así. Los ResultMaps son la respuesta de MyBatis a este problema.

Por ejemplo, ¿cómo mapearías este statement?

```xml
<!-- Very Complex Statement -->
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

Posiblemente te gustaría mapearlo a un modelo de objetos formado por un Blog que ha sido escrito por un Autor, y tiene varios Posts, cada uno de ellos puede tener cero o varios comentarios y tags. A continuación puede observarse un ResultMap complejo (asumimos que Author, Blog, Post, Comments y Tags son typeAliases). Échale un vistazo, pero no te preocupes, iremos paso a paso. Aunque parece enorme, es en realidad, bastante sencillo.

```xml
<!-- Very Complex Result Map -->
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

El elemento resultMap tiene varios sub-elementos y una estructura que merece la pena comentar. A continuación se muestra una vista conceptual del elemento resultMap.

#### resultMap

- `constructor` - usado para inyectar resultados en el constructor de la clase durante la instanciación
  - `idArg` - argumento ID; marcar el argumento ID mejora el rendimiento
  - `arg` - un resultado normal inyectado en el constructor
- `id` – result ID; marcar los results con ID mejora el rendimiento
- `result` – un resultado normal inyectado en un campo o una propiedad de un JavaBean
- `association` – una asociación con un objeto complejo; muchos resultados acabarán siendo de este tipo
  - result mapping anidado – las asociaciones son resultMaps en sí mismas o pueden apuntar a otro resultMap
- `collection` – una colección de tipos complejos
  - result mapping anidado – las colecciones son resultMaps en sí mismas o pueden apuntar a otro resultMap
- `discriminator` – utiliza un valor del resultado para determinar qué resultMap utilizar
  - `case` – un resultMap basado en un valor concreto
    - result mapping anidado – un case es un resultMap en sí mismo y por tanto puede contener a su vez elementos propios de un resultMap o bien apuntar a un resultMap externo.

| Atributo | Descripción |
| --- | --- |
| `id` | Un identificador único dentro del namespace que se utiliza para identificar el result map. |
| `type` | El nombre completamente cualificado de la clase o el alias del parámetro que se pasará al statement. |
| `autoMapping` | Si el atributo está presente MyBatis habilita o inhabilita el automapping para este result map. El atributo sobreescribe el parametro global autoMappingBehavior. Valor por defecto: no informado. |
[Atributos de ResultMap]

<span class="label important">Buena práctia</span> Construye los ResultMaps de forma incremental. Las pruebas unitarias son de gran ayuda en ellos. Si intentas construir un ResultMap gigantesco como el que se ha visto anteriormente, es muy probable que lo hagas mal y será difícil trabajar con él. Comienza con una versión sencilla y evolucionarla paso a paso. Y haz pruebas unitarias! La parte negativa de utilizar frameworks es que a veces son una caja negra (sean opensource o no). Lo mejor que puedes hacer para asegurar que estás consiguiendo el comportamiento que pretendes es escribir pruebas unitarias. También son de utilidad para enviar bugs.

Las próximas secciones harán un recorrido por cada uno de los elementos en detalle.

#### id, result

```xml
<id property="id" column="post_id"/>
<result property="subject" column="post_subject"/>
```

Estos son los ResultMaps más sencillos. Ambos id, y result mapean una columna con una propiedad o campo de un tipo de dato simple (String, int, double, Date, etc.).

La única diferencia entre ambos es que id marca que dicho resultado es un identificador y dicha propiedad se utilizará en las comparaciones entre instancias de objetos. Esto mejora el rendimiento global y especialmente el rendimiento de la cache y los mapeos anidados (ej. mapeo de joins).

Cada uno tiene los siguientes atributos:

| Atributo | Descripción |
| --- | --- |
| `property` | El campo o propiedad al que se va a mapear al valor resultado. Si existe una propiedad tipo JavaBean para el nombre dado, se utilizará. En caso contrario MyBatis buscará un campo con el mismo nombre. En ambos casos puedes utilizar navegación compleja usando la notación habitual con puntos. Por ejemplo, puedes mapear a algo sencillo como: “username”, o a algo más complejo como: “address.street.number”. |
| `column` | El nombre de la columna de la base de datos, o el alias de columna. Es el mismo string que se pasaría al método resultSet.getString(columnName). |
| `javaType` | Un nombre de clase Java totalmente cualificado, o un typeAlias (más adelante se indican los typeAlias predefinidos). Normalmente MyBatis puede adivinar el tipo de datos de una propiedad de un JavaBean. Sin embargo si usas una HashMap deberás especificar el javaType para obtener el comportamiento deseado. |
| `jdbcType` | El tipo JDBC de la columna de resultado. Indícalo cuando MyBatis lo necesite, junto con el tipo Java, para elegir un TypeHandler para este mapeo. |
| `typeHandler` | Ya hemos hablado sobre typeHandlers anteriormente. Usando esta propiedad se puede sobre escribir el typeHandler por defecto. El valor admite un nombre totalmente cualificado o un alias. |
[Atributos de id y result]

#### Tipos JDBC soportados

Para referencia futura, MyBatis soporta los siguientes tipos JDBC por medio de la enumeración JdbcType.

<table>
          <tr>
            <td><code>BIT</code></td>
            <td><code>FLOAT</code></td>
            <td><code>CHAR</code></td>
            <td><code>TIMESTAMP</code></td>
            <td><code>OTHER</code></td>
            <td><code>UNDEFINED</code></td>
          </tr>
          <tr>
            <td><code>TINYINT</code></td>
            <td><code>REAL</code></td>
            <td><code>VARCHAR</code></td>
            <td><code>BINARY</code></td>
            <td><code>BLOB</code></td>
            <td><code>NVARCHAR</code></td>
          </tr>
          <tr>
            <td><code>SMALLINT</code></td>
            <td><code>DOUBLE</code></td>
            <td><code>LONGVARCHAR</code></td>
            <td><code>VARBINARY</code></td>
            <td><code>CLOB</code></td>
            <td><code>NCHAR</code></td>
          </tr>
          <tr>
            <td><code>INTEGER</code></td>
            <td><code>NUMERIC</code></td>
            <td><code>DATE</code></td>
            <td><code>LONGVARBINARY</code></td>
            <td><code>BOOLEAN</code></td>
            <td><code>NCLOB</code></td>
          </tr>
          <tr>
            <td><code>BIGINT</code></td>
            <td><code>DECIMAL</code></td>
            <td><code>TIME</code></td>
            <td><code>NULL</code></td>
            <td><code>CURSOR</code></td>
            <td><code>ARRAY</code></td>
          </tr>
          <tr>
            <td><code>STRUCT</code></td>
            <td><code>JAVA_OBJECT</code></td>
            <td><code>DISTINCT</code></td>
            <td><code>REF</code></td>
            <td><code>DATALINK</code></td>
            <td><code>ROWID</code></td>
          </tr>
          <tr>
            <td><code>LONGNVARCHAR</code></td>
            <td><code>SQLXML</code></td>
            <td><code>DATETIMEOFFSET</code></td>
            <td><code>TIME_WITH_TIMEZONE</code></td>
            <td><code>TIMESTAMP_WITH_TIMEZONE</code></td>
            <td></td>
          </tr>
        </table>

#### constructor

Aunque las propiedades funcionan bien en clases tipo Data Transfer Object (DTO), y posiblemente en la mayor parte de tu modelo de dominio, hay algunos casos en los que puedes querer clases inmutables. En ocasiones, las tablas que contienen información que nunca o raramente cambia son apropiadas para las clases inmutables. La inyección en el constructor te permite informar valores durante la instanciación de la clase, sin necesidad de exponer métodos públicos. MyBatis tambien soporta propiedades privadas para conseguir esto mismo pero habrá quien prefiera utilizar la inyección de Constructor. El elemento constructor permite hacer esto.

Dado el siguiente constructor:

```java
public class User {
   //...
   public User(Integer id, String username, int age) {
     //...
  }
//...
}
```

In order to inject the results into the constructor, MyBatis needs to identify the constructor for somehow. In the following example, MyBatis searches a constructor declared with three parameters: `java.lang.Integer`, `java.lang.String` and `int` in this order.

```xml
<constructor>
   <idArg column="id" javaType="int"/>
   <arg column="username" javaType="String"/>
   <arg column="age" javaType="_int"/>
</constructor>
```

When you are dealing with a constructor with many parameters, maintaining the order of arg elements is error-prone.<br /> Since 3.4.3, by specifying the name of each parameter, you can write arg elements in any order. To reference constructor parameters by their names, you can either add `@Param` annotation to them or compile the project with \'-parameters\' compiler option and enable `useActualParamName` (this option is enabled by default). The following example is valid for the same constructor even though the order of the second and the third parameters does not match with the declared order.

```xml
<constructor>
   <idArg column="id" javaType="int" name="id" />
   <arg column="age" javaType="_int" name="age" />
   <arg column="username" javaType="String" name="username" />
</constructor>
```

`javaType` can be omitted if there is a writable property with the same name and type.

El resto de atributos son los mismos que los de los elementos id y result.

| Atributo | Descripción |
| --- | --- |
| `column` | El nombre de la columna de la base de datos, o el alias de columna. Es el mismo string que se pasaría al método resultSet.getString(columnName). |
| `javaType` | Un nombre de clase Java totalmente cualificado, o un typeAlias (más adelante se indican los typeAlias predefinidos). Normalmente MyBatis puede adivinar el tipo de datos de una propiedad de un JavaBean. Sin embargo si usas una HashMap deberás especificar el javaType para obtener el comportamiento deseado. |
| `jdbcType` | El tipo JDBC de la columna de resultado. Indícalo cuando MyBatis lo necesite, junto con el tipo Java, para elegir un TypeHandler para este mapeo. |
| `typeHandler` | Ya hemos hablado sobre typeHandlers anteriormente. Usando esta propiedad se puede sobre escribir el typeHandler por defecto. El valor admite un nombre totalmente cualificado o un alias. |
| `select` | El id de otro mapped statement que cargará el tipo complejo asociado a este argumento. Los valores obtenidos de las columnas especificadas en el atributo column se pasarán como parámetros al select statement referenciado. Ver el elemento association para más información. |
| `resultMap` | El id de un resultmap que puede mapear los resultados anidados de este argumento al grafo de objetos (object graph) apropiado. Es una alternativa a llamar a otro select statement. Permite hacer join de varias tablas en un solo ResultSet. Un ResultSet de este tipo puede contener bloques repetidos de datos que deben ser descompuestos y mapeados apropiadamente a un árbol de objetos (object graph). MyBatis te permite encadenar RestultMaps para tratar resultados anidados. Ver el elemento association para más información. |
| `name` | The name of the constructor parameter. Specifying name allows you to write arg elements in any order. See the above explanation. Since 3.4.3. |

##### Nested Results for association or collection

While the following sections describe how to use `association` and `collection` for both Nested selects and Nested results, Since 3.6.0 we can now inject both using `constructor` mapping.

Considering the following:

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

We can map `UserRole` as a nested result, MyBatis will wait until the row has been fully ‘completed’ before creating the object, this means that by the time the `User` gets created, `userRoles` will be complete and cannot be modified anymore.

```xml
<resultMap id="userResultMap" type="User">
  <constructor>
    <idArg column="id" javaType="int" />
    <arg column="username" javaType="String" />
    <arg javaType="List" resultMap="userRoleResultMap" columnPrefix="role_"/>
  </constructor>
</resultMap>
```

To achieve fully immutable objects in this example, we can also use constructor injection for `UserRole`

```xml
<resultMap id="userRoleResultMap" type="UserRole">
  <constructor>
    <idArg column="id" javaType="int" />
    <arg column="role" javaType="String" />
  </constructor>
</resultMap>
```

MyBatis needs to be explicitly told that the results have been ordered in such a way, that when a new main row is retrieved from the result set, no previous row results will be retrieved again. This can be set on the statement with the `resultOrdered` attribute:

```xml
<select id="getAllUsers" resultMap="userResultMap" resultOrdered="true">
    select
      u.id,
      u.username,
      r.id as role_id,
      r.role as role_role
    from user u
      left join user_role ur on u.id = ur.user_id
      left join role r on r.id = ur.role_id
    order by u.id, r.id
</select>
```

Note that `order by` is specified to order the results correctly. We can imagine the output to look something like:

| row\_nr | u.id | u.username | r.id | r.role |
| --- | --- | --- | --- | --- |
| 1 | 1 | John | 1 | Admins |
| 2 | 1 | John | 2 | Users |
| 3 | 2 | Jack | null | null |
| 4 | 3 | Peter | 2 | Users |
| 5 | 3 | Peter | 3 | Maintainers |
| 6 | 3 | Peter | 4 | Approvers |

After this query is run, we would have the following results:

```java
User{username=John, roles=[Admins, Users]}
User{username=Jack, roles=[]}
User{username=Peter, roles=[Users, Maintainers, Approvers]}
```

If the 5th row here would have somehow appeared below the first row (via some `ORDER BY`), MyBatis would not be able to fully construct the `John` user correctly using constructor collection mapping.

It is important to note that mixed mappings have limited support, i.e. property mappings combined with nested constructor mappings are likely to fail. When using this functionality, it is preferable for the entire mapping hierarchy to use immutable constructor mappings.

#### association

```xml
<association property="author" javaType="Author">
  <id property="id" column="author_id"/>
  <result property="username" column="author_username"/>
</association>
```

El elemento association trata las relaciones de tipo “tiene-un”. Por ejemplo, en nuestro ejemplo, un Blog tiene un Autor. Un mapeo association funciona casi como cualquier otro result. Debes especificar la propiedad destino, el javaType de la propiedad (que normalmente MyBatis puede adivinar), el jdbcType si fuera necesario y un typeHandler si quieres sobre escribir el tratamiento de los valores de retorno.

Donde la association es distinta es en que debes indicar a MyBatis como cargar la asociación. MyBatis puede hacerlo de dos formas distintas:

- Nested Select: Ejecutando otra select que devuelve el tipo complejo deseado.
- Nested Results: Usando un ResultMap anidado que trata con los datos repetidos de resultsets provenientes de joins.

Primeramente, examinemos la propiedades del elemento. Como veras, es distinto de un ResultMap normal solo por los atributos select y resultMap.

| Attribute | Description |
| --- | --- |
| `property` | El campo o propiedad a la que se debe mapear la columna. Si existe una propiedad JavaBean igual al nombre dado, se usará. En caso contrario, MyBatis buscará un campo con el nombre indicado. En ambos casos puedes usar navegación compleja usando la notación habitual con puntos. Por ejemplo puedes mapear algo simple como: “username”, o algo más complejo como: “address.street.number”. |
| `javaType` | Un nombre de clase Java totalmente cualificado, o un typeAlias (más adelante se indican los typeAlias predefinidos). Normalmente MyBatis puede adivinar el tipo de datos de una propiedad de un JavaBean. Sin embargo si usas una HashMap deberás especificar el javaType para obtener el comportamiento deseado. |
| `jdbcType` | El tipo JDBC de la columna de resultado. Indícalo cuando MyBatis lo necesite, junto con el tipo Java, para elegir un TypeHandler para este mapeo. |
| `typeHandler` | Ya hemos hablado sobre typeHandlers anteriormente. Usando esta propiedad se puede sobre escribir el typeHandler por defecto. El valor admite un nombre totalmente cualificado o un alias. |

#### Select anidada en Association

| Atributo | Descripción |
| --- | --- |
| `column` | El nombre de la columna de la base de datos, o el alias de columna que contiene el valor que será pasado como parámetro de entrada al statement anidado. Es el mismo string que se pasaría al método resultSet.getString(columnName). Nota: para tratar con claves compuestas, puedes especificar varios nombres usando esta sintaxis column=”{prop1=col1,prop2=col2}”. Esto hará que se informen las propiedades prop1 y prop2 del objeto parámetro del select statement destino |
| `select` | El id de otro mapped statement que cargará el tipo complejo asociado a esta propiedad. Los valores obtenidos de las columnas especificadas en el atributo column se pasarán como parámetros al select statement referenciado. A continuación se muestra un ejemplo detallado. Nota: para tratar con claves compuestas, puedes especificar varios nombres usando esta sintaxis column=”{prop1=col1,prop2=col2}”. Esto hará que se informen las propiedades prop1 y prop2 del objeto parámetro del select statement destino |
| `fetchType` | Opcional. Los valores válidos son `lazy` y `eager`. Si está presente sobrescribe el parámetro global de configuración `lazyLoadingEnabled` para este mapping. |

Por ejemplo:

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

Tenemos dos statements: uno para cargar el Blog, el otro para cargar el Autor, y el RestulMap de Blog describe que la sentencia “selectAuthor” debe utilizarse para cargar su propiedad author.

Todas las demás propiedades se cargarán automáticamente asumiendo que los nombres de propiedad y de columna coinciden.

Aunque este enfoque es simple, puede no tener un buen rendimiento con gran cantidad de datos. Este problema es conocido como “El problema de las N+1 Selects”. En resumidas cuentas, el problema de N+1 selects está causado por esto:

- Ejecutas una sentencia SQL para obtener una lista de registros (el “+1”).
- Para cada registro obtenido ejecutas una select para obtener sus detalles (el “N”).

Este problema puede provocar la ejecución de cientos o miles de sentencias SQL. Lo cual no es demasiado recomendable.

MyBatis puede cargar esas consultas de forma diferida (lazy load), por lo tanto se evita el coste de lanzar todas esas consultas a la vez. Sin embargo, si cargas la lista e inmediatamente iteras por ella para acceder a los datos anidados, acabarás cargando todos los registros y por lo tanto el rendimiento puede llegar a ser muy malo.

Así que, hay otra forma de hacerlo.

#### ResultMaps anidadas en Association

| Attribute | Description |
| --- | --- |
| `resultMap` | El id de un resultmap que puede mapear los resultados anidados de esta asociación al grafo de objetos apropiado. Es una alternativa a llamar a otro select statement. Permite hacer join de varias tablas en un solo ResultSet. Un ResultSet de este tipo puede contener bloques repetidos de datos que deben ser descompuestos y mapeados apropiadamente a un árbol de objetos (object graph). MyBatis te permite encadenar RestultMaps para tratar resultados anidados. A continuación se muestra un ejemplo detallado. |
| `columnPrefix` | Cuando se hace una join de varias tablas, es posible que tengas que usar alias de columna para evitar nombres duplicados en el ResultSet. El atributo columnPrefix te permite mapear dichas columnas a un Result Map externo. Más adelante se muestra un ejemplo de esta función. |
| `notNullColumn` | Por defecto MyBatis sólo crea objetos hijos si al menos una de las columnas mapeadas a las propiedades de dicho objeto es no nula. Con este atributo se puede modificar este comportamiento especificando qué columnas deben tener un valor de forma que MyBatis sólo creará un objeto hijo si alguna de estas columnas no es nula. Pueden indicarse una lista de columnas usando la coma como separador. |
| `autoMapping` | If present, MyBatis will enable or disable automapping when mapping the result to this property. This attribute overrides the global autoMappingBehavior. Note that it has no effect on an external resultMap, so it is pointless to use it with `select` or `resultMap` attribute. Default value: unset. |

Previamente has visto un ejemplo muy complejo de asociaciones anidadas. Lo que se muestra a continuación es un ejemplo más simple que muestra cómo funciona esta característica. En lugar de ejecutar un statement separado, vamos a hacer una JOIN de las tablas Blog y Author de la siguiente forma:

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

Fíjate en la join, y el especial cuidado que se ha dedicado a que todos los resultados tengan un alias que les de un nombre único y claro. Esto hace el mapeo mucho más sencillo. Ahora podemos mapear los resultados:

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="blog_id" />
  <result property="title" column="blog_title"/>
  <association property="author" javaType="Author" resultMap="authorResult"/>
</resultMap>

<resultMap id="authorResult" type="Author">
  <id property="id" column="author_id"/>
  <result property="username" column="author_username"/>
  <result property="password" column="author_password"/>
  <result property="email" column="author_email"/>
  <result property="bio" column="author_bio"/>
</resultMap>
```

En el ejemplo anterior puedes ver que la asociación “author” de Blog delega la carga de las instancias de Author en el ResultMap “authorResult”.

<span class="important">Muy importante</span>: El elemento id tiene un papel muy importante en el mapeo de resultados anidados. Debes especificar siempre una o más propiedades que se puedan usar para identificar unívocamente los resultados. Lo cierto es que MyBatis también va a funcionar si no lo haces pero a costa de una importante penalización en rendimiento. Elige el número mínimo de propiedades que pueda identificar unívocamente un resultado. La clave primaria es una elección obvia (incluso si es compuesta).

En el ejemplo anterior se usa un resultMap externo para mapear la asociación. Esto hace que el resultMap del Autor sea reusable. Sin embargo, si no hay necesidad de reusarla o simplemente prefieres colocar todos los mapeos en un solo ResultMap, puedes anidarlo en la propia asociación. A continuación se muestra un ejemplo de este enfoque:

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

Pero ¿y si el blog tiene un co-autor? La select sería algo así:

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

Recuerda que el resultMap de Author está definido de la siguiente forma.

```xml
<resultMap id="authorResult" type="Author">
  <id property="id" column="author_id"/>
  <result property="username" column="author_username"/>
  <result property="password" column="author_password"/>
  <result property="email" column="author_email"/>
  <result property="bio" column="author_bio"/>
</resultMap>
```

Dado que los nombres de columnas de los resultados difieren de los nombres especidficados en el resultMap, necesitas especificar un atributo `columnPrefix` para poder reusar el result map de Author para los co-autores.

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

#### Nested Cursor for Association

Some databases can return `java.sql.ResultSet` as a column value.<br /> Here is the statement and result map.

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
  select b.id, b.title, cursor(
    select a.id, a.username from author a where b.author_id = a.id
  ) author from blog b
</select>
```

Compared to the examples in the previous section, the key difference is the `jdbcType` attribute in the `<association>` element.<br /> Its value `CURSOR` indicates that the value of the column `author` is nested cursor.

#### ResultSets múltiples en Association

| Atributo | Descripción |
| --- | --- |
| `column` | Cuando se usan result sets multiples este atributo especifica las columnas (separadas por comas) que se correlarán con las indicadas en `foreignColumn` para identificar al padre y e hijo de una relación. |
| `foreignColumn` | Identifica los nombres de las columnas que contienen las claves foráneas que se emparejarán con las columnas especificadas en el atributo `column` del tipo padre. |
| `resultSet` | Identifica el nombre del result set de donde se cargarán los datos. |

Desce la versión 3.2.3 MyBatis proporciona otra forma más de resolver el problema del N+1.

Algunas bases de datos permiten que un procedimiento almacenado devuelva más de un resultset o ejecutar más de una sentencia a la vez y obtener de vuelta un resultset por cada. Esto se puede usar para acceder una sola vez a la base de datos y obtener datos relacionados sin usar una join.

En el ejemplo, el procedimiento almacenado deolverá dos result sets. El primero contendrá Blogs y el segundo Authors.

```sql
SELECT * FROM BLOG WHERE ID = #{id}

SELECT * FROM AUTHOR WHERE ID = #{id}
```

Se debe proporcionar un nombre a cada resultset informando el atributo `resultSets` del mapped statement con una lista de nombres separados por comas.

```xml
<select id="selectBlog" resultSets="blogs,authors" resultMap="blogResult" statementType="CALLABLE">
  {call getBlogsAndAuthors(#{id,jdbcType=INTEGER,mode=IN})}
</select>
```

Ahora podemos especificar que los datos para rellenar la asociación \"author\" vienen en el result set \"authors\":

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

Has visto como se utiliza la asociación “Tiene Un”. Pero ¿qué hay que el “Tiene Muchos”? Ese es el contenido de la siguiente sección.

#### collection

```xml
<collection property="posts" ofType="domain.blog.Post">
  <id property="id" column="post_id"/>
  <result property="subject" column="post_subject"/>
  <result property="body" column="post_body"/>
</collection>
```

El elemento collection funciona de forma casi idéntica al association. En realidad, es tan similar, que documentar todas las similitudes sería redundante. Así que enfoquémonos en las diferencias.

Para continuar con nuestro ejemplo anterior, un Blog solo tiene un Autor. Pero un Blog tiene muchos Posts. En la clase Blog esto se representaría con algo como:

```java
private List<Post> posts;
```

Para mapear un conjunto de resultados anidados a una Lista como esta, debemos usar el elemento collection. Al igual que el elemento association, podemos usar una select anidada, o bien resultados anidados cargados desde una join.

#### Select anidada en Collection

Primeramente, echemos un vistazo al uso de una select anidada para cargar los Posts de un Blog.

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

Hay unas cuantas diferencias que habrás visto de forma inmediata, pero la mayor parte tiene el mismo aspecto que el elemento association que vimos anteriormente. Primeramente, verás que estamos usando el elemento collection. Verás también que hay un nuevo atributo “ofType”. Este atributo es necesario para distinguir el tipo de la propiedad del JavaBean (o del campo) y el tipo contenido por la colección. Por tanto podrías leer el siguiente mapeo de esta forma:

```xml
<collection property="posts" javaType="ArrayList" column="id" ofType="Post" select="selectPostsForBlog"/>
```

<span class="important">Leído como</span>: \"Una colección de posts en un ArrayList de tipos Post.\"

El javaType es casi siempre innecesario, porque MyBatis lo adivinará en la mayoría de los casos. Así que podrías acortarlo de esta forma:

```xml
<collection property="posts" column="id" ofType="Post" select="selectPostsForBlog"/>
```

#### ResultMaps anidados en Collection

A estas alturas, posiblemente ya imaginarás cómo funcionan los ResultMaps anidados en una colección porque funcionan exactamente igual que en una asociación, salvo por que se añade igualmente el atributo “ofType”.

Primero, echemos un vistazo al SQL:

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

Nuevamente hemos hecho una JOIN de las tablas Blog y Post, y hemos tenido cuidado de asegurarnos que las columnas obtenidas tienen un alias adecuado. Ahora, mapear un Blog y colección de Post es tan simple como:

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

Nuevamente, recuerda la importancia del elemento id, o lee la sección de asociación si no lo has hecho aun.

Además, si prefieres el formato más largo que aporta más reusabilidad a tus ResultMaps, puedes utilizar esta forma alternativa de mapeo:

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

#### Nested Cursor for Collection

It might be obvious, but nested cursor can return multiple rows.<br /> Just like `<association>`, you just need to specify `jdbcType="CURSOR"` in the `<collection>` element.

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
  select b.id, b.title, cursor(
    select p.id, p.subject, p.body from post p where p.blog_id = b.id
  ) posts from blog b
</select>
```

#### ResultSets múltiples en Collection

De la misma forma que hicimos en la association, podemos llamar a un procedimiento almacenado que devuelva dos resultsets, uno con Blogs y otro con Posts:

```sql
SELECT * FROM BLOG WHERE ID = #{id}

SELECT * FROM POST WHERE BLOG_ID = #{id}
```

Se debe proporcionar un nombre a cada resultset informando el atributo `resultSets` del mapped statement con una lista de nombres separados por comas.

```xml
<select id="selectBlog" resultSets="blogs,posts" resultMap="blogResult" statementType="CALLABLE">
  {call getBlogsAndPosts(#{id,jdbcType=INTEGER,mode=IN})}
</select>
```

Especificamos que la collection \"posts\" se rellenará con datos contenidos en el resultset llamado \"posts\":

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

<span class="label important">NOTA</span> No hay límite en profundidad, amplitud o combinaciones de las asociaciones y colecciones que mapees. Debes tener en cuenta el rendimiento cuando crees los mapeos. Las pruebas unitarias y de rendimiento de tu aplicación son de gran utilidad para conocer cuales el mejor enfoque para tu aplicación. La parte positiva es que MyBatis te permite cambiar de opinión más tarde, con muy poco (o ningún) cambio en tu código.

El mapeo de asociaciones y colecciones es un tema denso. La documentación solo puede llevarte hasta aquí. Con un poco de práctica, todo se irá aclarando rápidamente.

#### discriminator

```xml
<discriminator javaType="int" column="draft">
  <case value="1" resultType="DraftPost"/>
</discriminator>
```

En ocasiones una base de datos puede devolver resultados de muchos y distintos (y esperamos que relacionados) tipos de datos. El elemento discriminator fue diseñado para tratar esta situación, y otras como la jerarquías de herencia de clases. El discriminador es bastante fácil de comprender, dado que funciona muy parecido la sentencia switch de Java.

Una definición de discriminator especifica los atributos column y javaType. Column indica de dónde debe MyBatis obtener el valor con el que comparar. El javaType es necesario para asegurar que se utiliza el tipo de comparación adecuada (aunque la comparación de Strings posiblemente funcione casi en todos los casos). Por ejemplo:

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

En este ejemplo, MyBatis obtendrá cada registro del ResultSet y comparará su valor vehicle\_type. Si coincide con alguno de los casos del discriminador entonces usará el ResultMap especificado en cada caso. Esto se hace de forma exclusiva, es decir, el resto del ResultMap se ignora (a no ser que se extienda, de lo que hablaremos en un Segundo). Si no coincide ninguno de los casos MyBatis utilizará el resultmap definido fuera del bloque discriminator. Por tanto si carResult ha sido declarado de la siguiente forma:

```xml
<resultMap id="carResult" type="Car">
  <result property="doorCount" column="door_count" />
</resultMap>
```

Entonces solo la propiedad doorCount se cargará. Esto se hace así para permitir grupos de discriminadores completamente independientes, incluso que no tengan ninguna relación con el ResultMap padre. En este caso sabemos que hay relación entre coches y vehículos, dado que un coche es-un vehículo. Por tanto queremos que el resto de propiedades se carguen también, así que con un simple cambio en el ResultMap habremos terminado.

```xml
<resultMap id="carResult" type="Car" extends="vehicleResult">
  <result property="doorCount" column="door_count" />
</resultMap>
```

Ahora, se cargarán todas las propiedades tanto de vehicleResult como de carResult.

Nuevamente, hay quien puede pensar que la definición externa es tediosa. Por tanto hay una sintaxis alternativa para los que prefieran un estilo más conciso. Por ejemplo:

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

<span class="label important">NOTA</span> Recuerda que todos estos son ResultMaps, y que si no indicas ningún result en ellos, MyBatis mapeará automáticamente las columnas a las propiedades por ti. Así que en muchos casos estos ejemplos son más verbosos de lo que realmente debieran ser. Dicho esto, la mayoría de las bases de datos son bastante complejas y muchas veces no podemos depender de ello para todos los casos.

## Auto-mapeo

Como ya has visto en las secciones previas, en los casos simples MyBatis puede auto-mapear los resultados por ti y en el resto de los casos es posible que tengas que crear un result map. Pero, como verás en esta sección también puedes combinar ambas estrategias. Veamos en detalle cómo funciona el auto-mapeo.

Al auto-mapear resultados MyBatis obtiene el nombre de columna y busca una propiedad con el mismo nombre sin tener en cuenta las mayúsculas. Es decir, si se encuentra una columna <i>ID</i> y una propiedad <i>id</i>, MyBatis informará la propiedad <i>id</i> con el valor de la columna <i>ID</i>.

Normalmente las columnas de base de datos se nombran usando mayúsculas y separando las palabras con un subrayado, mientras que las propiedades java se nombran habitualmente siguiendo la notación tipo camelcase. Para habilitar el auto-mapeo entre ellas informa el parámetro de configuración `mapUnderscoreToCamelCase` a true.

El auto-mapeo funciona incluso cuando hay un result map específico. Cuando esto sucede, para cada result map, todas las columnas que están presentes en el ResultSet y que no tienen un mapeo manual se auto-mapearán. Posteriormente se procesarán los mapeos manuales. En el siguiente ejemplo las columnas <i>id</i> y <i>userName</i> se auto-mapearán y la columna <i>hashed\_password</i> se mapeará manualmente.

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
  <result property="hashedPassword" column="hashed_password"/>
</resultMap>
```

Hay tres niveles de auto-mapeo:

- `NONE` - desactiva el auto-mapeo. Solo las propiedades mapeadas manaulmente se informarán.
- `PARTIAL` - auto-mapea todos los resultados que no tienen un mapeo anidado definido en su interior (joins).
- `FULL` - lo auto-mapea todo.

El valor por defecto es `PARTIAL`, y hay una razón para ello. Cuandos se utiliza `FULL` el auto-mapeo se realiza cuando se están procesando resultados de joins y las joins obtienen datos de distintas entidades en la misma fila por lo tanto podrían producirse mapeos automáticos indeseados. Para comprender el riesgo observa el siguiente ejemplo:

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

Con este result map ambos <i>Blog</i> y <i>Author</i> se auto-mapearán. Pero fíjate que <i>Author</i> tiene un <i>id</i> y que hay una columna con nombre <i>id</i> en el ResultSet por lo que el id de Author se rellenará con el id de Blog, y eso no era lo que esperabas. Por tanto usa la opción `FULL` con cuidado.

Independientemente del nivel de auto-mapeo configurado puedes activar o desactivar el auto-mapeo para un ResultMap especifico añadiendole el atributo`autoMapping`:

```xml
<resultMap id="userResultMap" type="User" autoMapping="false">
  <result property="hashedPassword" column="hashed_password"/>
</resultMap>
```

## cache

MyBatis incluye una funcionalidad de caché transaccional de segundo nivel muy potente que es ampliamente configurable y personalizable. Se han realizado muchos cambios en la caché de MyBatis 3 para hacer la a la vez más potente y más sencilla de configurar.

Por defecto la única caché activa es la caché local de sesión que se utiliza únicamente durante la duración de una sesión. Para habilitar la caché de segundo nivel global simplemente necesitas añadir una línea en tu fichero de mapping:

```xml
<cache/>
```

Eso es todo literalmente. El efecto de esta sencilla línea es el siguiente:

- Si `cacheEnabled` es true, los resultados de las sentencias select del fichero de mapeo pueden almacenarse en la caché de segundo nivel cuando `useCache` es true y no se utiliza un ResultHandler personalizado. Las consultas con Cursor no utilizan esta caché.
- Todas las sentencias insert, update y delete del mapped statement vaciarán la caché.
- La caché usará un algoritmo de reemplazo tipo Least Recently Used (LRU).
- La caché no se vaciará por tiempo (ej. no Flush Interval).
- La caché guardará 1024 referencias a listas u objetos (según lo que devuelva el statement).
- La caché puede tratarse como una cache de tipo lectura/escritura, lo cual significa que los objetos obtenidos no se comparten y pueden modificarse con seguridad por el llamante sin interferir en otras potenciales modificaciones realizadas por otros llamantes o hilos.

<span class="label important">NOTE</span> The cache will only apply to statements declared in the mapping file where the cache tag is located. If you are using the Java API in conjunction with the XML mapping files, then statements declared in the companion interface will not be cached by default. You will need to refer to the cache region using the @CacheNamespaceRef annotation.

Todas estas propiedades son modificables mediante atributos del elemento cache. Por ejemplo:

```xml
<cache
  eviction="FIFO"
  flushInterval="60000"
  size="512"
  readOnly="true"/>
```

Esta configuración más avanzada de caché crea una cache de tipo FIFO que se vacía cada 60 segundos, guarda hasta 512 referencias a objetos o listas, y los objetos devueltos son considerados de solo lectura, esto es, que modificarlos puede crear problemas en llamantes de otros hilos.

Las políticas de reemplazo son las siguientes:

- `LRU` – Least Recently Used: Borra los objetos que llevan más tiempo sin ser usados.
- `FIFO` – First In First Out: Borra los objetos en el mismo orden en el que entraron en la caché.
- `SOFT` – Soft Reference: Borra los objetos en base a las referencias Soft del Garbage Collector.
- `WEAK` – Weak Reference: Es más agresivo y borra objetos basándose en el estado del Garbage Collector y las referencias débiles.

LRU es la política por defecto.

El atributo flushInterval acepta un entero positivo y debería representar un lapso de tiempo razonable en milisegundos. No está activo por defecto, por tanto no hay intervalo de vaciado y la caché solo se vacía mediante llamadas a otros statements.

El atributo size acepta un entero positivo, ten en cuenta el tamaño de los objetos que quieres cachear y la cantidad de memoria de la que dispone. Por defecto es 1024.

El atributo readOnly puede informarse con true o false. Una caché de solo lectura devuelve la misma instancia de objeto a todos los llamantes. Por lo tanto estos objetos no deben modificarse. Por otro lado esto proporciona una mejora en el rendimiento. Una caché de tipo lectura-escritura devuelve una copia (vía serialización) del objeto cacheado. Esto es más lento, pero más seguro, y por ello el valor por defecto es false.

<span class="label important">NOTA</span> La caché de segundo nivel es transaccional. Esto significa que solo es actualizada cuando una sesión acaba con commit o cuando acaba con rollback pero no se ha ejecutado ninguna sentencia insert/delete/update con el parámetro flushCache=true.

#### Como usar una caché personalizada

Además de poder personalizar la caché de las formas indicadas, puedes sustituir el sistema de caché por completo y proporcionar tu propia caché, o crear un adaptador para cachés de terceros.

```xml
<cache type="com.domain.something.MyCustomCache"/>
```

Este ejemplo muestra cómo usar una caché personalizada. La clase especificada en el atributo type debe implementar el interfaz org.apache.ibatis.cache.Cache y proporcionar un constructor que recibe como parámetro un String id. Este es uno de los interfaces más complejos de MyBatis pero su funcionalidad es simple.

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

Para configurar tu caché añade simplemente propiedades tipo JavaBean a tu implementación, y pasa las propiedades usando el elemento cache, por ejemplo el siguiente ejemplo llamará a un método “setCacheFile(String file)” en tu implementación de caché:

```xml
<cache type="com.domain.something.MyCustomCache">
  <property name="cacheFile" value="/tmp/my-custom-cache.tmp"/>
</cache>
```

Puedes utilizar propiedades JavaBean de cualquier tipo simple y MyBatis hará la conversión. And you can specify a placeholder(e.g. `${cache.file}`) to replace value defined at [configuration properties](configuration.html#properties).

Since 3.4.2, the MyBatis has been supported to call an initialization method after it\'s set all properties. If you want to use this feature, please implements the `org.apache.ibatis.builder.InitializingObject` interface on your custom cache class.

```java
public interface InitializingObject {
  void initialize() throws Exception;
}
```

<span class="label important">NOTE</span> Los parametros de configuración de la cache (eviction, read write..etc.) explicados anteriormente no aplican cuando se usa una caché personalizada.

Es importante recordar que la configuración de la caches y la instancia de caché está asociadas al namespace del fichero SQL Map. Y por tanto, a todas las sentencias del mismo namespace dado que la cache está asociada a él. Los statements pueden modificar cómo interactúan con la caché, o excluirse a sí mismos completamente utilizando dos atributos simples. Por defecto los statements están configurados así:

```xml
<select ... flushCache="false" useCache="true"/>
<insert ... flushCache="true"/>
<update ... flushCache="true"/>
<delete ... flushCache="true"/>
```

Dado que estos son los valores por defecto, no deberías nunca configurar un statement de esa forma. En cambio, utiliza los atributos flushCache y useCache si quieres modificar el valor por defecto. Por ejemplo en algunos casos quieres excluir los resultados de un caché particular de la caché, o quizá quieras que un statement de tipo select vacíe la caché. O de forma similar, puede que quieras que algunas update statements no la vacíen.

#### cache-ref

Recuerda que en la sección anterior se indicó que la caché de un namespace sería utilizada por statements del mismo namespace. Es posible que en alguna ocasión quieras compartir la misma configuración de caché e instancia entre statements de distintos namespaces. En estos casos puedes hacer referencia a otra caché usando el elemento cache-ref.

```xml
<cache-ref namespace="com.someone.application.data.SomeMapper"/>
```
