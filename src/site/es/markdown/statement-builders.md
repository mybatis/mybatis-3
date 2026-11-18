title: MyBatis 3 | Statement Builders
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

# The SQL Builder Class

## El Problema

Una de las cosas más tediosas que un programador Java puede llegar a tener que hacer es incluir código SQL en código Java. Normalmente esto se hace cuando es necesario generar dinámicamente el SQL – de otra forma podrías externalizar el código en un fichero o un procedimiento almacenado. Como ya has visto, MyBatis tiene una respuesta potente a la generación dinámica de SQL mediante las capacidades del mapeo XML. Sin embargo, en ocasiones se hace necesario construir una sentencia SQL dentro del código Java. En este caso, MyBatis tiene una funcionalidad más para ayudarte en ello, antes de que comiences con el típico lío de signos de suma, comillas, líneas nuevas, problemas de formato y condicionales anidados para tratar con las comas extra y las conjunciones AND… Realmente, generar código dinámico en java, puede ser una verdadera pesadilla. Por ejemplo:

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

## La Solución

MyBatis 3 introduce un concepto un tanto distinto para tratar con el problema. Con la clase SQL, puedes crear una sentencia SQL en un sólo paso invocando a sus métodos. El ejemplo anterior tendría este aspecto si se rescribe con la clase SQL:

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

¿Qué hay de especial en este ejemplo? Bien, si lo miras detenidamente, verás que no hay que preocuparse de duplicar “AND”s, o elegir entre “WHERE” o “AND”, o ninguno de ambos! La clase SQL se ocupa de colocar el \"WHERE\" donde debe de ir, si debe usarse \"AND\" o no y de realizar todas las concatenaciones de Strings.

## La clase SQL

Aqui van algunos ejemplos:

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

public String updatePersonSql() {
  return new SQL() {{
    UPDATE("PERSON");
    SET("FIRST_NAME = #{firstName}");
    WHERE("ID = #{id}");
  }}.toString();
}
```

| Metodo | Descripción |
| --- | --- |
| <ul><li>`SELECT(String)`</li> <li>`SELECT(String...)`</li></ul> | Comienza o añade a una sentencia SELECT. Se puede invocar más de una vez y los parámetros se irán añadiendo a la sentencia SELECT. Los parámetros son normalmente una lista de columnas o alias separados por comas, pero puede ser cualquier cosa que acepte el driver de base de datos. |
| <ul><li>`SELECT_DISTINCT(String)`</li> <li>`SELECT_DISTINCT(String...)`</li></ul> | Comienza o añade a una sentencia SELECT, también añade la palabra clave “DISTINCT” a la sentencia generada. Se puede invocar más de una vez y los parámetros se irán añadiendo a la sentencia SELECT. Los parámetros son normalmente una lista de columnas o alias separados por comas, pero puede ser cualquier cosa que acepte el driver de base de datos. |
| <ul><li>`FROM(String)`</li> <li>`FROM(String...)`</li></ul> | Comienza o añade a una cláusula FROM. Se puede invocar más de una vez y los parámetros se irán añadiendo a la clausula FROM. Los parámetros son normalmente un nombre de tabla o alias o cualquier cosa que acepte el driver de base de datos. |
| <ul><li>`JOIN(String)`</li> <li>`JOIN(String...)`</li> <li>`INNER_JOIN(String)`</li> <li>`INNER_JOIN(String...)`</li> <li>`LEFT_OUTER_JOIN(String)`</li> <li>`LEFT_OUTER_JOIN(String...)`</li> <li>`RIGHT_OUTER_JOIN(String)`</li> <li>`RIGHT_OUTER_JOIN(String...)`</li> <li>`OUTER_JOIN(String)`</li> <li>`OUTER_JOIN(String...)`</li></ul> | Añade una nueva clausula JOIN del tipo apropiado, dependiendo al método que se haya llamado. El parámetro puede incluir un join estándar que consiste en las columnas y las condiciones sobre las que hacer la join. |
| <ul><li>`WHERE(String)`</li> <li>`WHERE(String...)`</li></ul> | Añade una nueva condición a la clausula WHERE concatenada con un AND. Puede llamarse más de una vez, lo cual hará que se añadan más condiciones todas ellas concatenadas con un AND. O usa OR() para partirlas con un OR(). |
| `OR()` | Parte las condiciones de la cláusula WHERE o HAVING utilizada más recientemente con un OR. Puede llamarse más de una vez, pero llamarlo varias veces seguidas puede producir sentencias incorrectas. |
| `AND()` | Parte las condiciones de la cláusula WHERE o HAVING utilizada más recientemente con un AND. Puede llamarse más de una vez, pero llamarlo varias veces seguidas puede producir sentencias incorrectas. Dado que WHERE y HAVING concatenan automáticamente con AND, rara vez es necesario invocar este método. |
| <ul><li>`GROUP_BY(String)`</li> <li>`GROUP_BY(String...)`</li></ul> | Añade una nueva clausula GROUP BY grupo, concatenada con una coma. Se le puede llamar más de una vez, lo cual hará que se concatenen nuevas condiciones separadas también por coma. |
| <ul><li>`HAVING(String)`</li> <li>`HAVING(String...)`</li></ul> | Añade una nueva clausula HAVING, concatenada con un AND. Se le puede llamar más de una vez, lo cual hará que se concatenen nuevas condiciones separadas también por AND. Usa OR() para dividirlas por OR. |
| <ul><li>`ORDER_BY(String)`</li> <li>`ORDER_BY(String...)`</li></ul> | Añade un Nuevo elemento a la clausula ORDER BY concatenado por coma. Se le puede llamar más de una vez, lo cual hará que se concatenen nuevas condiciones separadas también por coma. |
| <ul><li>`LIMIT(String)`</li> <li>`LIMIT(int)`</li></ul> | Appends a `LIMIT` clause. This method valid when use together with SELECT(), UPDATE() and DELETE(). And this method is designed to use together with OFFSET() when use SELECT(). (Available since 3.5.2) |
| <ul><li>`OFFSET(String)`</li> <li>`OFFSET(long)`</li></ul> | Appends a `OFFSET` clause. This method valid when use together with SELECT(). And this method is designed to use together with LIMIT(). (Available since 3.5.2) |
| <ul><li>`OFFSET_ROWS(String)`</li> <li>`OFFSET_ROWS(long)`</li></ul> | Appends a `OFFSET n ROWS` clause. This method valid when use together with SELECT(). And this method is designed to use together with FETCH\_FIRST\_ROWS\_ONLY(). (Available since 3.5.2) |
| <ul><li>`FETCH_FIRST_ROWS_ONLY(String)`</li> <li>`FETCH_FIRST_ROWS_ONLY(int)`</li></ul> | Appends a `FETCH FIRST n ROWS ONLY` clause. This method valid when use together with SELECT(). And this method is designed to use together with OFFSET\_ROWS(). (Available since 3.5.2) |
| `DELETE_FROM(String)` | Comienza una sentencia delete y especifica la tabla donde borrar. Generalmente suele ir seguida de una clausula WHERE! |
| `INSERT_INTO(String)` | Comienza una sentencia insert y especifica al tabla en la que insertar. Suele ir seguida de una o más llamadas a VALUES() o INTO\_COLUMNS() y INTO\_VALUES(). |
| <ul><li>`SET(String)`</li> <li>`SET(String...)`</li></ul> | Añade a la lista “set” de una update. |
| `UPDATE(String)` | Comienza una sentencia update y especifica la tabla que modificar Suele ir seguida de una o más llamadas a SET() y normalmente de una llamada a WHERE(). |
| `VALUES(String, String)` | Añade a una sentencia insert. El primer parámetro es el nombre de columna y el Segundo el valor(es). |
| `INTO_COLUMNS(String...)` | Appends columns phrase to an insert statement. This should be call INTO\_VALUES() with together. |
| `INTO_VALUES(String...)` | Appends values phrase to an insert statement. This should be call INTO\_COLUMNS() with together. |
| `ADD_ROW()` | Add new row for bulk insert. (Available since 3.5.2) |

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

Desde la versión 3.5.15, `applyIf(boolean, Consumer<SQL>)` y `applyIf(BooleanSupplier, Consumer<SQL>)` añaden cláusulas SQL solo cuando se cumple la condición. `applyForEach(Iterable<E>, ForEachConsumer<SQL, E>)` invoca el consumidor por cada elemento, pasándole el constructor SQL, el elemento y su índice, que comienza en cero. Estos métodos devuelven el constructor para encadenar llamadas.

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

## SqlBuilder y SelectBuilder (DEPRECADAS)

En versiones anteriores a la 3.2 optamos por una solución distinta, usando una variable ThreadLocal para resolver algunas limitaciones de las que hacen los DSLs Java algo incomodos. Sin embargo, esta solución está ahora desprecada porque los frameworks actuales están mas orientados a usar patrones builder-type y clases anónimas interas para este tipo de cosas. Por lo tanto las clases SelectBuilder y SqlBuilder están ahora deprecadas.

Los siguientes métodos aplican solo a las clases deprecadas SqlBuilder y SelectBuilder.

| Método | Descripción |
| --- | --- |
| `BEGIN()` / `RESET()` | Estos métodos reinician el estado ThreadLocal de la clase importada, SelectBuilder o SqlBuilder; cada clase mantiene su propio estado. BEGIN() se lee mejor al comenzar una sentencia. RESET() se lee mejor cuando se descarta una sentencia durante su construcción (por ejemplo, si la lógica requiere una sentencia distinta). |
| `SQL()` | Devuelve el SQL generado y reinicia el estado de la clase importada. Llama a SQL() una vez por sentencia; construye otra sentencia antes de volver a llamarlo. |

La clase SelectBuilder no es mágica, pero es importante que conozcas cómo funcionan. SelectBuilder y SqlBuilder usan una combinación de imports estáticos y una variable ThreadLocal para permitir una sintaxis más limpia más fácilmente usabe con condicionales. Para usarlas debes importar estáticamente métodos de las siguientes clases (uno u otro, no ambos):

```java
import static org.apache.ibatis.jdbc.SelectBuilder.*;
```

```java
import static org.apache.ibatis.jdbc.SqlBuilder.*;
```

De esta forma podrás crear métodos como estos:

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
