title: MyBatis 3 | Java API
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

# Java API

<a id="javaApi"></a>

Ahora que ya conoces cómo configurar MyBatis y crear mapeos estás listo para lo mejor. El API Java es donde obtendrás los mejores frutos de tus esfuerzos. Como verás, comparado con JDBC, MyBatis simplifica enormemente tu código y lo mantiene limpio, de fácil comprensión y mantenimiento. MyBatis 3 incorpora muchas mejoras significativas para hacer que el trabajo con SQL Maps sea aun mejor.

## Directory Structure

<a id="directoryStructure"></a>

Antes de zambullirnos en el propio API Java , es importante comprender las mejores prácticas relativas a la estructura de directorios. MyBatis es muy flexible, y puedes hacer casi cualquier cosa con tus ficheros. Pero como en cualquier otro framework, hay una forma recomendable.

Veamos una estructura de directorios típica:

<pre>/my_application
  /bin
  /devlib
  <strong>/lib                <code>&lt;-- Los ficheros .jar de MyBatis van aqui.</code></strong>
  /src
    /org/myapp/
      /action
      <strong>/data           <code>&lt;-- Los artefactos de MyBatis van aqui, lo que incluye, mappers, configuración XML, y ficheros de mapeo XML.</code></strong>
        /mybatis-config.xml
        /BlogMapper.java
        /BlogMapper.xml
      /model
      /service
      /view
    <strong>/properties       <code>&lt;-- Las Properties incluidas en tu configuración XML van aqui.</code></strong>
  /test
    /org/myapp/
      /action
      /data
      /model
      /service
      /view
    /properties
  /web
    /WEB-INF
      /web.xml</pre>

Recuerda, esto son prefierncias no requisitos, pero habrá otros que te agradecerán que uses una estructura de directorios conún.

Los ejemplos restantes en esta sección asumen que estás utilizando esta estructura de directorios.

## SqlSessions

<a id="sqlSessions"></a>

El interfaz principal para trabajar con MyBatis es el SqlSession. A través de este interfaz puedes ejecutar comandos, obtener mappers y gestionar transacciones. Hablaremos más sobre el propio SqlSession en breve, pero primero veamos cómo obtener una instancia de SqlSession. Las SqlSessions se crean por una instancia de SqlSessionFactory. La SqlSessionFactory contiene métodos para crear instancias de SqlSessions de distintas formas. La SqlSessionFactory en si misma se crea por la SqlSessionFactoryBuilder que puede crear una SqlSessionFactory a partir de XML, anotaciones o un objeto Configuration creado por código.

<span class="label important">NOTE</span> When using MyBatis with a dependency injection framework like Spring or Guice, SqlSessions are created and injected by the DI framework so you don\'t need to use the SqlSessionFactoryBuilder or SqlSessionFactory and can go directly to the SqlSession section. Please refer to the MyBatis-Spring or MyBatis-Guice manuals for further info.

#### SqlSessionFactoryBuilder

SqlSessionFactoryBuilder tiene nueve sobrecargas de build(): cuatro reciben un InputStream, cuatro reciben un Reader y una recibe un objeto Configuration.

```java
SqlSessionFactory build(InputStream inputStream)
SqlSessionFactory build(InputStream inputStream, String environment)
SqlSessionFactory build(InputStream inputStream, Properties properties)
SqlSessionFactory build(InputStream inputStream, String env, Properties props)
SqlSessionFactory build(Reader reader)
SqlSessionFactory build(Reader reader, String environment)
SqlSessionFactory build(Reader reader, Properties properties)
SqlSessionFactory build(Reader reader, String env, Properties props)
SqlSessionFactory build(Configuration config)
```

Los primeros ocho métodos leen un documento XML, como el fichero mybatis-config.xml mencionado anteriormente, desde un InputStream o un Reader. Los parámetros opcionales son environment y properties. Environment determina qué entorno cargar, incluyendo el datasource y el gestor de transacciones. Por ejemplo:

```xml
<environments default="development">
  <environment id="development">
    <transactionManager type="JDBC">
        ...
    </transactionManager>
    <dataSource type="POOLED">
        ...
    </dataSource>
  </environment>
  <environment id="production">
    <transactionManager type="MANAGED">
        ...
    </transactionManager>
    <dataSource type="JNDI">
        ...
    </dataSource>
  </environment>
</environments>
```

Si llamas al método build que recibe el parámetro environment, entonces MyBatis usará la configuración de dicho entorno. Por supuesto, si especificas un entorno inválido, recibirás un error. Si llamas a alguno de los métodos que no reciben el parámetro environment, entonces se utilizará el entorno por defecto (que es el especificado como default=”development” en el ejemplo anterior).

Si llamas a un método que recibe una instancia de properties, MyBatis cargará dichas properties y las hará accesibles desde tu configuración. Estas propiedades pueden usarse en lugar de la gran mayoría de los valores utilizando al sintaxis: ${propName}

Recuerda que las propiedades pueden también referenciarse desde el fichero SqlMapConfig.xml, o especificarse directamente en él. Por lo tanto es importante conocer las prioridades. Lo mencionamos anteriormente en este documento, pero lo volvemos a mencionar para facilitar la referencia.

---

Si una propiedad existe en más de un lugar, MyBatis la carga en el siguiente orden:

- Las propiedades especificadas en el cuerpo del elemento properties se cargan al principio.
- Las propiedades cargadas desde los atributos resource/url del elemento properties se leen a continuación, y sobrescriben cualquier propiedad duplicada que hubiera sido cargada anteriormente.
- Las propiedades pasadas como argumento se leen al final, y sobrescriben cualquier propiedad duplicada que hubiera sido cargada anteriormente.

Por lo tanto la prioridad mayor es la de las propiedades pasadas como parámetro, seguidas por las especificadas en el atributo resource/url y finalmente las propiedades especificadas en el cuerpo del elemento properties.

---

En resumen, los primeros ocho métodos se distinguen por el tipo de entrada y por los parámetros opcionales environment y properties. Aquí hay un ejemplo de cómo se construye un SqlSessionFactory desde un fichero mybatis-config.xml.

<pre class="prettyprint"><code>String <strong>resource</strong> = "org/mybatis/builder/mybatis-config.xml";
InputStream <strong>inputStream</strong> = Resources.getResourceAsStream(resource);
SqlSessionFactoryBuilder <strong>builder</strong> = new SqlSessionFactoryBuilder();
SqlSessionFactory <strong>factory</strong> = builder.build(inputStream);</code></pre>

Observa que estamos usando la clase de utilidad Resources, que está ubicada en el paquete org.apache.ibatis.io. La clase Resources, tal y como su nombre indica, te ayuda a cargar recursos desde el classpath, el sistema de ficheros o desde una web o URL. Con un vistazo rápido al código fuente en tu IDE descubrirás un conjunto bastante obvio de métodos. Rápidamente:

```java
URL getResourceURL(String resource)
URL getResourceURL(ClassLoader loader, String resource)
InputStream getResourceAsStream(String resource)
InputStream getResourceAsStream(ClassLoader loader, String resource)
Properties getResourceAsProperties(String resource)
Properties getResourceAsProperties(ClassLoader loader, String resource)
Reader getResourceAsReader(String resource)
Reader getResourceAsReader(ClassLoader loader, String resource)
File getResourceAsFile(String resource)
File getResourceAsFile(ClassLoader loader, String resource)
InputStream getUrlAsStream(String urlString)
Reader getUrlAsReader(String urlString)
Properties getUrlAsProperties(String urlString)
Class classForName(String className)
```

El último método build() recibe una instancia de Configuration. La clase Configuration contiene todo lo que posiblemente necesites conocer de la instancia de SqlSessionFactory. La clase Configuración es útil para investigar la configuración, incluido añadir o modificar SQL maps (no es recomendable una vez la aplicación ha comenzado a aceptar peticiones). La clase Configuration tiene todas las opciones de configuración que hemos visto ya pero expuestas como una API Java. A continuación se muestra un ejemplo simple de cómo instanciar manualmente un objeto Configuration y pasarlo al método build() para crear un SqlSessionFactory.

```java
DataSource dataSource = BaseDataTest.createBlogDataSource();
TransactionFactory transactionFactory = new JdbcTransactionFactory();

Environment environment = new Environment("development", transactionFactory, dataSource);

Configuration configuration = new Configuration(environment);
configuration.setLazyLoadingEnabled(true);
configuration.getTypeAliasRegistry().registerAlias(Blog.class);
configuration.getTypeAliasRegistry().registerAlias(Post.class);
configuration.getTypeAliasRegistry().registerAlias(Author.class);
configuration.addMapper(BoundBlogMapper.class);
configuration.addMapper(BoundAuthorMapper.class);

SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
SqlSessionFactory factory = builder.build(configuration);
```

Ahora tienes un SqlSessionFactory, que puede utilizarse para crear interfaces SqlSession.

<a id="sqlSessionManager"></a>

#### SqlSessionManager

Desde la versión 3.4.0, MyBatis ofrece `SqlSessionManager`, que implementa tanto `SqlSessionFactory` como `SqlSession`. A diferencia de `DefaultSqlSessionFactory`, que crea una nueva `SqlSession` con cada llamada a `openSession()`, `SqlSessionManager` utiliza un `ThreadLocal` para gestionar las sesiones de cada hilo. Esto permite gestionar el ciclo de vida de `SqlSession` en entornos multihilo sin un framework externo de inyección de dependencias.

##### Creación de un SqlSessionManager

Se puede crear un `SqlSessionManager` de dos maneras:

1. Mediante uno de los métodos estáticos `newInstance()` que aceptan un fichero de configuración XML de MyBatis:

   ```java
   SqlSessionManager manager = SqlSessionManager.newInstance(inputStream);
   ```

   También se puede indicar un entorno o un conjunto de propiedades:

   ```java
   SqlSessionManager manager = SqlSessionManager.newInstance(inputStream, environment);

   // ... or ...

   SqlSessionManager manager = SqlSessionManager.newInstance(inputStream, properties);
   ```
2. Envolviendo una instancia existente de `SqlSessionFactory`:

   ```java
   SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(inputStream);
   SqlSessionManager manager = SqlSessionManager.newInstance(factory);
   ```

##### Sesiones gestionadas

La característica principal de `SqlSessionManager` es que gestiona las sesiones por hilo. Una vez iniciada una sesión gestionada, todas las llamadas a los métodos de `SqlSession` a través del proxy de `SqlSessionManager` utilizan la sesión asociada al hilo.

Para iniciar una sesión gestionada:

```java
manager.startManagedSession();
```

También se pueden indicar opciones concretas:

```java
// Con auto-commit
manager.startManagedSession(true);

// Con un tipo de executor específico
manager.startManagedSession(ExecutorType.BATCH);

// Con un nivel de aislamiento específico
manager.startManagedSession(TransactionIsolationLevel.READ_COMMITTED);

// Con una conexión específica
manager.startManagedSession(connection);
```

Para comprobar si se ha iniciado una sesión gestionada en el hilo actual:

```java
boolean isStarted = manager.isManagedSessionStarted();
```

##### Uso de SqlSessionManager

Cuando hay una sesión gestionada, `commit()`, `rollback()`, `close()` y los métodos de acceso a datos (`selectOne`, `selectList`, `insert`, `update`, `delete`, etc.) operan sobre la sesión asociada al hilo. Al terminar, se debe llamar a `close()` para limpiar el estado del hilo. Si no se hace en un servidor web que reutiliza hilos, como Tomcat o Jetty, la sesión puede permanecer asociada al hilo y afectar a otra petición HTTP, provocando un comportamiento transaccional imprevisible o fugas de conexiones.

```java
manager.startManagedSession();
try {
    // utilizar la sesión
    manager.commit();
} finally {
    manager.close();
}
```

Si no se ha iniciado una sesión gestionada, al llamar a un método de acceso a datos (`selectOne`, `selectList`, `selectMap`, `selectCursor`, `select`, `insert`, `update`, `delete`) de `SqlSessionManager`, se abre automáticamente una sesión de corta duración, se ejecuta ese método y se cierra la sesión. La sesión abierta automáticamente llama a `commit()` si el método termina con normalidad y a `rollback()` si lanza una excepción. Que estas llamadas confirmen o reviertan efectivamente una transacción de la base de datos depende del gestor de transacciones configurado. Los métodos del ciclo de vida de la sesión (`commit()`, `rollback()`, `close()`, `clearCache()`, `flushStatements()`, `getConnection()`) no abren una sesión automáticamente; lanzan `SqlSessionException` si no se ha iniciado una sesión gestionada.

En particular, sin una sesión gestionada, `selectCursor()` devuelve un cursor que se cierra inmediatamente al cerrarse la sesión automática. Para recorrer sus resultados, inicie una sesión gestionada antes de llamar a `selectCursor()`.

##### Diferencias con DefaultSqlSessionFactory

| Característica | DefaultSqlSessionFactory | SqlSessionManager |
| --- | --- | --- |
| Creación de sesiones | Crea una nueva sesión en cada llamada a `openSession()` | Puede gestionar sesiones por hilo mediante `startManagedSession()` |
| Seguridad entre hilos | Requiere gestión externa | Incluye gestión de sesiones mediante `ThreadLocal` |
| Ciclo de vida automático de la sesión | No (requiere cerrarla manualmente) | Sí, cuando no se ha iniciado una sesión gestionada |
| Integración con frameworks de inyección de dependencias | Requiere una integración específica (por ejemplo, `SqlSessionTemplate` en Spring) | Puede sustituir a `SqlSessionFactory` y `SqlSession` en entornos sin inyección de dependencias |

`SqlSessionManager` resulta especialmente útil cuando se necesita gestionar sesiones por hilo sin utilizar un framework de inyección de dependencias. Si se utiliza Spring o Guice, se debe seguir usando `MyBatis-Spring` o `MyBatis-Guice` para gestionar las sesiones.

#### SqlSessionFactory

SqlSessionFactory tiene ocho métodos que se usan para crear instancias de SqlSession. En general, las decisiones que deberás tomar cuando tengas qué elegir de entre alguno de estos métodos son:

- <strong>Transaction</strong>: ¿Quieres usar un ámbito transaccional para esta sesión o utilizar auto-commit (lo cual equivale a no usar transacción en la mayoría de las bases de datos y/o JDBC drivers)?
- <strong>Connection</strong>: ¿Quieres que MyBatis obtenga una conexión de un datasource o quieres proporcionar tu propia conexión?
- <strong>Execution</strong>: ¿Quieres que MyBatis reúse PreparedStatements y/o haga batch updates (incluyendo inserts y deletes)?

El conjunto de métodos sobrecargados openSession te permiten seleccionar una combinación de estas opciones que tenga sentido.

```java
SqlSession openSession()
SqlSession openSession(boolean autoCommit)
SqlSession openSession(Connection connection)
SqlSession openSession(TransactionIsolationLevel level)
SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level)
SqlSession openSession(ExecutorType execType)
SqlSession openSession(ExecutorType execType, boolean autoCommit)
SqlSession openSession(ExecutorType execType, Connection connection)
Configuration getConfiguration();
```

El método openSession() por defecto, que no recibe parámetros, crea una SqlSession con las siguientes características:

- MyBatis solicita desactivar auto-commit; el gestor de transacciones configurado determina el control efectivo de la transacción.
- La conexión se obtiene cuando se necesita de la instancia de DataSource configurada en el environment activo.
- El nivel de aislamiento transaccional será el que la base de datos tenga establecido por defecto.
- La reutilización de PreparedStatements y las actualizaciones por lotes dependen de `defaultExecutorType`. Su valor por defecto, `SIMPLE`, no realiza ninguna de las dos.

La mayoría de los métodos son auto explicativos. Para habilitar el auto-commit, pasa el valor “true” al parámetro opcional autoCommit. Para proporcionar tu propia conexión pasa una instancia de conexión al parámetro conexión. Ten en cuenta que no hay método para proporcionar tanto la conexión como el auto-commit porque MyBatis utilizará las opciones que esté usando actualmente la conexión suministrada. MyBatis usa una enumeration para indicar los niveles de aislamiento denominado TransactionIsolationLevel, pero funcionan como se espera de ellos y tiene los 5 niveles soportados por JDBC (NONE, READ\_UNCOMMITTED, READ\_COMMITTED, REPEATABLE\_READ, SERIALIZABLE).

El único parámetro que puede ser nuevo para ti es el ExecutorType. Esta enumeración define tres valores:

- `ExecutorType.SIMPLE`: Este tipo de executor no hace nada en especial. Crea un PreparedStatement para cada sentencia a ejecutar.
- `ExecutorType.REUSE`: Este tipo de executor reusará PreparedStatements.
- `ExecutorType.BATCH`: Este executor hará batch de todos las sentencias de actualización.

<span class="label important">NOTE</span> Hay un método más del SqlSessionFactory que no hemos mencionado y es getConfiguration(). Este método devuelve una instancia de Configuration que puedes usar para introspeccionar la configuración de MyBatis en tiempo de ejecución.

<span class="label important">NOTE</span> Si has usado una versión anterior de MyBatis recordarás que las sesiones, transacciones y batches eran cosas separadas. Esto ya no es así, todas ellas están contenidas en el interfaz SqlSession. No tienes que gestionar las transacciones o los batches de forma separada para obtener todo su potencial.

#### SqlSession

Como hemos comentado anteriormente, la instancia de SqlSession es la clase más potente de MyBatis. Es donde encontrarás todos los métodos para ejecutar sentencias, hacer commit o rollback de transacciones y obtener mappers.

Hay más de veinte métodos en la clase SqlSession, así que vayamos dividiéndolos en grupo fáciles de digerir.

##### Métodos de ejecución de sentencias

Estos métodos se usan para ejecutar las sentencias SELECT, INSERT, UPDATE y DELETE que se hayan definido en los ficheros xml de mapeo SQL. Son bastante auto explicativos, cada uno recibe el ID del statement y el objeto de parámetro, que puede ser una primitiva, un JavaBean, un POJO o un Map.

```xml
<T> T selectOne(String statement, Object parameter)
<E> List<E> selectList(String statement, Object parameter)
<T> Cursor<T> selectCursor(String statement, Object parameter)
<K,V> Map<K,V> selectMap(String statement, Object parameter, String mapKey)
int insert(String statement, Object parameter)
int update(String statement, Object parameter)
int delete(String statement, Object parameter)
```

La diferencia entre selectOne y selectList es que selectOne debe devolver sólo un objeto. Si hay más de uno se lanzará una excepción. Si no hay ninguno se devolverá null. Si no sabes cuantos objetos esperas recibir, usa selectList. Si quieres comprobar la existencia de un objeto sería mejor que devuelvas un count(). SelectMap es un caso especial diseñado para convertir una lista de resultados en un Map basado en las propiedades de los objetos recibidos. Como no todas las sentencias requieren un parámetro, estos métodos han sido sobrecargados de forma que se proporcionan versiones que no reciben el parámetro objeto.

El valor devuelto por los métodos insert, update and delete indica el número de filas afectadas por la sentencia.

```xml
<T> T selectOne(String statement)
<E> List<E> selectList(String statement)
<T> Cursor<T> selectCursor(String statement)
<K,V> Map<K,V> selectMap(String statement, String mapKey)
int insert(String statement)
int update(String statement)
int delete(String statement)
```

A Cursor offers the same results as a List, except it fetches data lazily using an Iterator.

```java
try (Cursor<MyEntity> entities = session.selectCursor(statement, param)) {
   for (MyEntity entity:entities) {
      // process one entity
   }
}
```

Finalmente hay seis versiones avanzadas de los métodos select que te permiten restringir el rango de filas devueltas, o proporcionar lógica de tratamiento de resultados personalizada, normalmente para grandes cantidades de datos.

```xml
<E> List<E> selectList (String statement, Object parameter, RowBounds rowBounds)
<T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds)
<K,V> Map<K,V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowbounds)
void select(String statement, ResultHandler handler)
void select(String statement, Object parameter, ResultHandler handler)
void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler)
```

El parámetro RowBounds hace que MyBatis salte los registros especificados y que limite los resultados devueltos a cierto número. La clase RowBounds tiene un constructor que recibe ambos el offset y el limit, y es inmutable.

```java
int offset = 100;
int limit = 25;
RowBounds rowBounds = new RowBounds(offset, limit);
```

El rendimiento de algunos drivers puede variar mucho en este aspecto. Para un rendimiento optimo, usa tipos de ResultSet SCROLL\_SENSITIVE o SCROLL\_INSENSITIVE (es decir, no FORWARD\_ONLY)

El parámetro ResultHandler te permite manejar cada fila como tú quieras. Puedes añadirla a una lista, crear un Map, un Set, o descartar cada resultado y guardar solo cálculos. Puedes hacer casi todo lo que quieras con un ResultHandler, de hecho, es lo que MyBatis usa internamente para construir listas de ResultSets.

Since 3.4.6, ResultHandler passed to a CALLABLE statement is used on every REFCURSOR output parameter of the stored procedure if there is any.

La interfaz es muy sencilla:

```java
package org.apache.ibatis.session;
public interface ResultHandler<T> {
  void handleResult(ResultContext<? extends T> context);
}
```

El parámetro ResultContext te da acceso al objeto resultado, al número de objetos creados y al método stop(), que permite indicar a MyBatis que detenga la carga de resultados.

Using a ResultHandler has two limitations that you should be aware of:

- Data got from an method called with a ResultHandler will not be cached.
- When using advanced resultmaps MyBatis will probably require several rows to build an object. If a ResultHandler is used you may be given an object whose associations or collections are not yet filled.

##### Batch update statement Flush Method

There is method for flushing(executing) batch update statements that stored in a JDBC driver class at any timing. This method can be used when you use the `ExecutorType.BATCH` as `ExecutorType`.

```java
List<BatchResult> flushStatements()
```

<h5 id="transaction-control-methods">Métodos de control de transacción</h5>

Hay cuatro métodos para controlar la transacción. En modo auto-commit o con un gestor de transacciones externo, no ejecutan un commit o rollback JDBC, pero sí limpian la caché local y ejecutan o descartan las sentencias pendientes por lotes. Con el gestor de transacciones JDBC, también controlan la transacción de la base de datos:

```java
void commit()
void commit(boolean force)
void rollback()
void rollback(boolean force)
```

Por defecto MyBatis no hace un commit a no ser que haya detectado que la base de datos ha sido modificada por una insert, update, delete o select con `affectData` habilitado. Si has realizado cambios sin llamar a estos métodos, entonces puedes pasar true en al método de commit() y rollback() para asegurar que se realiza el commit (ten en cuenta que aun así no puedes forzar el commit() en modo auto-commit o cuando se usa un gestor de transacciones externo). La mayoría de las veces no tendrás que llamar a rollback() dado que MyBatis lo hará por ti en caso de que no hayas llamado a commit(). Sin embargo, si necesitas un control más fino sobre la sesión, donde puede que haya varios commits, tienes esta opción para hacerlo posible.

<span class="label important">NOTA</span> MyBatis-Spring y MyBatis-Guice proporcionan gestión de transacción declarativa. Por tanto si estás usando MyBatis con Spring o Guice consulta sus manuales específicos.

##### Local Cache

MyBatis utiliza dos cachés: la caché local y la caché de segundo nivel.

Cada vez que se crea una sesión, MyBatis crea una caché local y la asocia a dicha sesión. Las consultas ordinarias ejecutadas en la sesión se almacenan en esta caché, de forma que si se repite la misma consulta con los mismos parámetros, los datos se obtienen de la caché sin acceder a la base de datos. Las consultas con un ResultHandler personalizado no leen de la caché local; las consultas con Cursor la omiten por completo. La caché local se vacía al ejecutar update, commit, rollback o close.

Por defecto, la caché local se utiliza durante toda la sesión. Esta caché es necesaria para resolver referencias circulares y acelerar consultas anidadas repetidas, así que no puede desactivarse por completo. Sin embargo, puede configurarse para que se utilice solo durante la ejecución de una sentencia mediante localCacheScope=STATEMENT.

Cuando localCacheScope está configurado como SESSION (valor por defecto), MyBatis devuelve referencias a los objetos almacenados en la caché. Cualquier modificación de un objeto o una lista influye en el contenido de la caché y en los valores que se devolverán posteriormente durante la sesión. Por lo tanto, evita modificar los objetos devueltos por MyBatis.

Puedes borrar el contenido de la caché local en el momento que desees invocando:

```java
void clearCache()
```

##### Asegurarse de que la SqlSession se cierra

```java
void close()
```

El punto más importante del que debes asegurarte es que cierras todas las sesiones que abres. La mejor forma de asegurarse es usar el patrón mostrado a continuación:

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
    // following 3 lines pseudocod for "doing some work"
    session.insert(...);
    session.update(...);
    session.delete(...);
    session.commit();
}
```

<span class="label important">NOTE</span> Al igual que con SqlSessionFactory, puedes obtener la instancia de Configuration que está usando al SqlSession llamando al método getConfiguration().

```java
Configuration getConfiguration()
```

##### Uso de Mappers

```xml
<T> T getMapper(Class<T> type)
```

Aunque los métodos insert, update, delete y select son potentes, también son muy verbosos, no hay seguridad de tipos (type safety) y no son tan apropiados para tu IDE o tus pruebas unitarias como pudieran ser. Ya hemos visto un ejemplo de uso de mappers en la sección de primeros pasos.

Por lo tanto, una forma más común de ejecutar mapped statements es utilizar clases Mapper. Un mapper es simplemente una interfaz con definiciones de métodos que se hacen encajar con métodos de SqlSession. El ejemplo siguiente demuestra algunas firmas de método y como se asignan a una SqlSession.

```java
public interface AuthorMapper {
  // (Author) selectOne("selectAuthor",5);
  Author selectAuthor(int id);
  // (List<Author>) selectList(“selectAuthors”)
  List<Author> selectAuthors();
  // (Map<Integer,Author>) selectMap("selectAuthorsAsMap", "id")
  @MapKey("id")
  Map<Integer, Author> selectAuthorsAsMap();
  // insert("insertAuthor", author)
  int insertAuthor(Author author);
  // update("updateAuthor", author)
  int updateAuthor(Author author);
  // delete("deleteAuthor",5)
  int deleteAuthor(int id);
}
```

En resumen, cada firma de método de mapper se asigna al método de la SqlSession al que está asociado pero sin parámetro ID. En su lugar el nombre del método debe ser el mismo que el ID del mapped statement.

Además, el tipo devuelto debe ser igual que el result type del mapped statement. Todos los tipos habituales se soportan, incluyendo primitivas, mapas, POJOs y JavaBeans.

<span class="label important">NOTA</span> Los mappers no necesitan implementar ninguna interfaz o extender ninguna clase. Sólo es necesario que la firma de método pueda usarse para identificar unívocamente el mapped statement correspondiente.

<span class="label important">NOTA</span> Los mappers pueden extender otras interfaces. Asegúrate que tienes tus statements en los namespaces adecuados en tu fichero XML. Además, la única limitación es que no puedes tener el mismo método, con la misma firma, en dos interfaces de la jerarquía (una mala idea en cualquier caso).

Puedes pasar más de un parámetro a un método de un mapper. Si lo haces, se usará como nombre el literal \"param\" seguido de su posición en la lista de parámetros, por ejemplo: #{param1}, #{param2} etc. También puedes usar la anotación @Param(\"paramName\") para dar nombre a un parámetro, incluso cuando sea el único.

También puedes pasar una instancia de RowBounds al método para limitar los resultados.

##### Anotaciones de mappers

Desde sus comienzos, MyBatis ha sido siempre un framework XML. La configuración se basa en XML y los mapped statements se definen en XML. Con MyBatis 3, hay más opciones. MyBatis 3 se ha desarrollado sobre una exhaustiva y potente API de configuración Java. Este API es el fundamento de la configuración basada en XML y también de la nueva configuración basada en anotaciones. Las anotaciones simplemente ofrecen una forma más sencilla de implementar los mapped statements sin introducir un montón de sobrecarga.

<span class="label important">NOTE</span> Las anotaciones Java son desafortunadamente muy limitadas en su flexibilidad y expresividad. A pesar de haber dedicado mucho tiempo a la investigación, diseño y pruebas, los mapeos más potentes de MyBatis simplemente no es posible construirlos con anotaciones. Los atributos C# (por ejemplo) no sufren de las mismas limitaciones y por tanto MyBatis.NET podrá construir una alternativa mucho más rica al XML. Dicho esto, la configuración basada en anotaciones Java también tiene sus ventajas.

<strong>Las anotaciones son las siguientes:</strong>

| Anotación | Target | XML equivalente | Descripción |
| --- | --- | --- | --- |
| `@CacheNamespace` | `Class` | `<cache>` | Configura la cache para un namespace (una clase). Atributos: implementation, eviction, flushInterval, size, readWrite, blocking and properties. |
| `@Property` | N/A | `<property>` | Specifies the property value or placeholder(can replace by configuration properties that defined at the `mybatis-config.xml`). Attributes: `name`, `value`. (Available on MyBatis 3.4.2+) |
| `@CacheNamespaceRef` | `Class` | `<cache-ref>` | Referencia la caché de un namespace. Las sentencias definidas mediante anotaciones no usan automáticamente la caché declarada en el mapper XML asociado, aunque compartan el mismo namespace. Para usar esa caché, hay que referenciarla con esta anotación. Atributos: `value` y `name`. If you use this annotation, you should be specified either `value` or `name` attribute. For the `value` attribute specify a java type indicating the namespace(the namespace name become a FQCN of specified java type), and for the `name` attribute(this attribute is available since 3.4.2) specify a name indicating the namespace. |
| `@ConstructorArgs` | `Method` | `<constructor>` | Agrupa un conjunto de resultados que serán pasados al constructor de un objeto de resultado. Atributos: value, que es un array de Args. |
| `@Arg` | N/A | <ul><li>`<arg>`</li> <li>`<idArg>`</li></ul> | Un argumento que forma parte de ConstructorArgs. Atributos: id, column, javaType, jdbcType, typeHandler, select, resultMap, name y columnPrefix. El atributo id es un valor booleano que identifica la propiedad que se usará en las comparaciones, como el elemento XML &lt;idArg&gt;. Desde 3.5.4 puede repetirse. |
| `@TypeDiscriminator` | `Method` | `<discriminator>` | Un grupo de clases que se pueden usar para determinar que mapeo de resultados realizar. Atributos: column, javaType, jdbcType, typeHandler, cases. El atributo cases es un array de Cases. |
| `@Case` | N/A | `<case>` | Un caso concreto y su mapeo correspondiente. Atributos: value, type, results y constructArgs. results es un array de anotaciones Result y constructArgs es un array de anotaciones Arg. |
| `@NamedResultMap` | `Type` | `<resultMap>` | Define en un tipo, normalmente una interfaz mapper, un mapa de resultados reutilizable por varias sentencias. id y javaType son obligatorios; constructorArguments (array de Arg), propertyMappings (array de Result) y typeDiscriminator son opcionales. Debe indicarse al menos un tipo de mapeo. |
| `@Results` | `Method` | `<resultMap>` | Una lista de Result mapping que contiene los detalles de cómo una columna particular se mapea a una propiedad o campo. Atributos: value, id. El atributo value es un array de anotaciones Result. The id attribute is the name of the result mapping. |
| `@Result` | N/A | <ul><li>`<result>`</li> <li>`<id>`</li></ul> | Un result mapping entre una columna y una propiedad o campo. Atributos: : id, column, property, javaType, jdbcType, typeHandler, one, many. El atributo id es un valor booleano que indica que la propiedad debe usarse en comparaciones (similar al &lt;id&gt; de los mapeos XML). El atributo one sirve para asociaciones de simples, similar al &lt;association&gt;, y el atributo many es para colecciones, similar al &lt;collection&gt;. Sus denominaciones son tales para evitar conflictos con nombres de clases. Since 3.5.4, it can be used as repeatable annotation. |
| `@One` | N/A | `<association>` | Un mapeo a una propiedad que contiene un tipo complejo. Atributos: select, que contiene el nombre completamente cualificado de un mapped statement (o un método de mapper) que puede cargar la instancia del tipo indicado. `fetchType`, que sobrescribe el parámetro global de configuración `lazyLoadingEnabled` para este mapeo. `resultMap`(available since 3.5.5), which is the fully qualified name of a result map that map to a single container object from select result. `columnPrefix`(available since 3.5.5), which is column prefix for grouping select columns at nested result map. |
| `@Many` | N/A | `<collection>` | Un mapeo a una propiedad que contiene una colección de tipos complejos. Atributos: select, que contiene el nombre completamente cualificado de un mapped statement (o un método de mapper) que puede cargar la instancia del tipo indicado. `fetchType`, que sobrescribe el parámetro global de configuración `lazyLoadingEnabled` para este mapeo. `resultMap`(available since 3.5.5), which is the fully qualified name of a result map that map to collection object from select result. `columnPrefix`(available since 3.5.5), which is column prefix for grouping select columns at nested result map. |
| `@MapKey` | `Method` |  | Se usa en métodos cuyo tipo de retorno es Map. Convierte una lista de resultados en un Map basado en una propiedad de esos objetos. El atributo value indica la propiedad que se usará como clave. |
| `@Options` | `Method` | Attributes of mapped statements. | Esta anotación proporciona acceso a un gran conjunto de opciones de configuración que normalmente aparecen como atributos en los mapped statements. En lugar de complicar cada anotación existente la anotación Options proporciona una forma sencilla y concisa de acceder a estas opciones. Atributos: useCache=true, flushCache=FlushCachePolicy.DEFAULT, resultSetType=DEFAULT, statementType=PREPARED, fetchSize=-1, timeout=-1, useGeneratedKeys=false, keyProperty=“”, keyColumn=“”, resultSets=“”, databaseId=\"\". Es importante comprender que las anotaciones en Java no permiten indicar un valor nulo. Por lo tanto, cuando usas la anotación Options el statement usará todos los valores por defecto. Presta atención a estos valores pro defecto para evitar comportamientos inesperados. The `databaseId`(Available since 3.5.5), in case there is a configured `DatabaseIdProvider`, the MyBatis use the `Options` with no `databaseId` attribute or with a `databaseId` that matches the current one. If found with and without the `databaseId` the latter will be discarded.<br /><br /> La keyColumn solo se requiere para algunas bases de datos (como PostgreSQL) cuando la columna no es la primera columna de la tabla. |
| <ul><li>`@Insert`</li> <li>`@Update`</li> <li>`@Delete`</li> <li>`@Select`</li></ul> | `Method` | <ul><li>`<insert>`</li> <li>`<update>`</li> <li>`<delete>`</li> <li>`<select>`</li></ul> | Cada una de estas anotaciones representa el SQL que debe ejecutarse. Cada una de ellas recibe un array de strings (o un solo string). Si se pasa un array de strings, todos ellos se concatenarán introduciendo un espacio en blanco entre ellos. Esto ayuda a evitar el problema “falta un espacio en blanco” al construir SQL en código Java. Sin embargo, también puedes concatenarlos en un solo string si lo prefieres. Atributos: value, que es el array de String para formar una única sentencia SQL. @Select también dispone de affectData para marcar un SELECT que modifica datos a efectos del control de transacciones. The `databaseId`(Available since 3.5.5), in case there is a configured `DatabaseIdProvider`, the MyBatis use a statement with no `databaseId` attribute or with a `databaseId` that matches the current one. If found with and without the `databaseId` the latter will be discarded. |
| <ul><li>`@InsertProvider`</li> <li>`@UpdateProvider`</li> <li>`@DeleteProvider`</li> <li>`@SelectProvider`</li></ul> | `Method` | <ul><li>`<insert>`</li> <li>`<update>`</li> <li>`<delete>`</li> <li>`<select>`</li></ul> | Estas anotaciones SQL alternativas te permiten especificar un nombre de clases y un método que devolverán la SQL que debe ejecutarse (Since 3.4.6, you can specify the `CharSequence` instead of `String` as a method return type). Cuando se ejecute el método MyBatis instanciará la clase y ejecutará el método especificados en el provider. You can pass objects that passed to arguments of a mapper method, \"Mapper interface type\", \"Mapper method\" and \"Database ID\" via the `ProviderContext`(available since MyBatis 3.4.5 or later) as method argument.(In MyBatis 3.4 or later, it\'s allow multiple parameters) Atributos: value, type, method y databaseId. @SelectProvider también dispone de affectData para marcar un SELECT que modifica datos a efectos del control de transacciones. El atributo value y type es el nombre completamente cualificado de una clase (The `type` attribute is alias for `value`, you must be specify either one. But both attributes can be omit when specify the `defaultSqlProviderType` as global configuration). El method es el nombre un método de dicha clase (Since 3.5.1, you can omit `method` attribute, the MyBatis will resolve a target method via the `ProviderMethodResolver` interface. If not resolve by it, the MyBatis use the reserved fallback method that named `provideSql`). The `databaseId`(Available since 3.5.5), in case there is a configured `DatabaseIdProvider`, the MyBatis will use a provider method with no `databaseId` attribute or with a `databaseId` that matches the current one. If found with and without the `databaseId` the latter will be discarded. Nota: A continuación hay una sección sobre la clase, que puede ayudar a construir SQL dinámico de una forma más clara y sencilla de leer. |
| `@Param` | `Parameter` | N/A | Esta anotación da nombre a un parámetro, incluso cuando el método recibe uno solo. Si hay varios parámetros sin anotación, también pueden referenciarse como #{param1}, #{param2}, etc.; RowBounds no se cuenta. Con @Param(\"persona\"), el parámetro puede referenciarse como #{persona}. |
| `@SelectKey` | `Method` | `<selectKey>` | Esta anotación es igual que la &lt;selectKey&gt; para métodos anotados con @Insert, @InsertProvider, @Update o @UpdateProvider. Se ignora en otros métodos. Si especificas la anotación @SelectKey, entonces MyBatis ignorará todas las propiedades de generación de claves proporcionadas por la anotación @Options, o mediante propiedades de configuración. Atributos: statement, un array de strings que contiene la SQL a ejecutar; keyProperty, la propiedad que recibirá el nuevo valor; keyColumn, los nombres de las columnas que proporcionan la clave (separados por comas si hay varios); before, que indica si la sentencia se ejecuta antes o después de la inserción o actualización; resultType, el tipo Java de la clave; y statementType, que puede ser STATEMENT, PREPARED (valor por defecto) o CALLABLE. The `databaseId`(Available since 3.5.5), in case there is a configured `DatabaseIdProvider`, the MyBatis will use a statement with no `databaseId` attribute or with a `databaseId` that matches the current one. If found with and without the `databaseId` the latter will be discarded. |
| `@ResultMap` | `Method` | N/A | Esta anotación se usa para proporcionar el id de un elemento &lt;resultMap&gt; en un mapper XML a una anotación @Select o @SelectProvider. Esto permite a las selects anotadas reusar resultmaps definidas en XML. Esta anotación sobrescribe las anotaciones @Results o @ConstructorArgs en caso de que se hayan especificado en la select anotada. |
| `@ResultOrdered` | `Method` | Atributo resultOrdered de &lt;select&gt; | Indica que el conjunto de resultados está ordenado. Es necesario al construir objetos con colecciones en sus constructores. Su atributo booleano value tiene como valor por defecto true. |
| `@ResultType` | `Method` | N/A | Esta anotación se usa cuando se utiliza un result handler. En ese caso, el tipo devuelto por el método es void y MyBatis no puede determinar el tipo del objeto que debe construir para cada fila. Si hay un result map XML entonces se utiliza la anotación @ResultMap. Si el tipo de retorno se especifica en el elemento `<select>` del XML entonces no es necesaria ninguna otra anotación. En el resto de casos, usa esta anotación. Por ejemplo en un método anotado con @Select con un result handler el valor de retorno será void y por tanto se requiere incluir esta anotación (o @ResultMap). La anotación se ignora si el tipo devuelto por el méotdo no es void. |
| `@Flush` | `Method` | N/A | If this annotation is used, it can be called the `SqlSession#flushStatements()` via method defined at a Mapper interface.(MyBatis 3.3 or above) |

##### Ejemplos de mappers anotados

Este ejemplo muestra como se usa la anotación @SelectKey para obtener un valor de una secuencia antes de de una insert:

<pre class="prettyprint"><code>@Insert("insert into table3 (id, name) values(#{nameId}, #{name})")
@SelectKey(statement="call next value for TestSequence", keyProperty="nameId", before=<strong>true</strong>, resultType=<strong>int.class</strong>)
<strong>int</strong> insertTable3(Name name);</code></pre>

Este ejemplo muestra como se usa la anotación @SelectKey para obtener el valor de una identity después de una insert:

<pre class="prettyprint"><code>@Insert("insert into table2 (name) values(#{name})")
@SelectKey(statement="call identity()", keyProperty="nameId", before=<strong>false</strong>, resultType=<strong>int.class</strong>)
<strong>int</strong> insertTable2(Name name);</code></pre>

This example shows using the `@Flush` annotation to call the `SqlSession#flushStatements()`:

```java
@Flush
List<BatchResult> flush();
```

These examples show how to name a ResultMap by specifying id attribute of @Results annotation.

<pre class="prettyprint"><code>@Results(id = "userResult", value = {
  @Result(property = "id", column = "uid", id = <strong>true</strong>),
  @Result(property = "firstName", column = "first_name"),
  @Result(property = "lastName", column = "last_name")
})
@Select("select * from users where id = #{id}")
User getUserById(Integer id);

@Results(id = "companyResults")
@ConstructorArgs({
  @Arg(column = "cid", javaType = Integer.class, id = <strong>true</strong>),
  @Arg(column = "name", javaType = String.class)
})
@Select("select * from company where id = #{id}")
Company getCompanyById(Integer id);</code></pre>

This example shows solo parameter using the Sql Provider annotation:

```java
@SelectProvider(type = UserSqlBuilder.class, method = "buildGetUsersByName")
List<User> getUsersByName(String name);

class UserSqlBuilder {
  public static String buildGetUsersByName(final String name) {
    return new SQL(){{
      SELECT("*");
      FROM("users");
      if (name != null) {
        WHERE("name like #{value} || '%'");
      }
      ORDER_BY("id");
    }}.toString();
  }
}
```

This example shows multiple parameters using the Sql Provider annotation:

```java
@SelectProvider(type = UserSqlBuilder.class, method = "buildGetUsersByName")
List<User> getUsersByName(
    @Param("name") String name, @Param("orderByColumn") String orderByColumn);

class UserSqlBuilder {

  // If not use @Param, you should be define same arguments with mapper method
  public static String buildGetUsersByName(
      final String name, final String orderByColumn) {
    return new SQL(){{
      SELECT("*");
      FROM("users");
      WHERE("name like #{name} || '%'");
      ORDER_BY(orderByColumn);
    }}.toString();
  }

  // If use @Param, you can define only arguments to be used
  public static String buildGetUsersByName(@Param("orderByColumn") final String orderByColumn) {
    return new SQL(){{
      SELECT("*");
      FROM("users");
      WHERE("name like #{name} || '%'");
      ORDER_BY(orderByColumn);
    }}.toString();
  }
}
```

This example shows usage the default implementation of `ProviderMethodResolver`(available since MyBatis 3.5.1 or later):

```java
@SelectProvider(type = UserSqlProvider.class)
List<User> getUsersByName(String name);

// Implements the ProviderMethodResolver on your provider class
class UserSqlProvider implements ProviderMethodResolver {
  // In default implementation, it will resolve a method that method name is matched with mapper method
  public static String getUsersByName(final String name) {
    return new SQL(){{
      SELECT("*");
      FROM("users");
      if (name != null) {
        WHERE("name like #{value} || '%'");
      }
      ORDER_BY("id");
    }}.toString();
  }
}
```

This example shows usage that share an sql provider class to all mapper methods using global configuration(Available since 3.5.6):

```java
Configuration configuration = new Configuration();
configuration.setDefaultSqlProviderType(TemplateFilePathProvider.class); // Specify an sql provider class for sharing on all mapper methods
// ...
```

```java
// Can omit the type/value attribute on sql provider annotation
// If omit it, the MyBatis apply the class that specified on defaultSqlProviderType.
public interface UserMapper {

  @SelectProvider // Same with @SelectProvider(TemplateFilePathProvider.class)
  User findUser(int id);

  @InsertProvider // Same with @InsertProvider(TemplateFilePathProvider.class)
  void createUser(User user);

  @UpdateProvider // Same with @UpdateProvider(TemplateFilePathProvider.class)
  void updateUser(User user);

  @DeleteProvider // Same with @DeleteProvider(TemplateFilePathProvider.class)
  void deleteUser(int id);
}
```

This example shows usage the default implementation of `ProviderMethodResolver`(available since MyBatis 3.5.1 or later):

```java
@SelectProvider(UserSqlProvider.class)
List<User> getUsersByName(String name);

// Implements the ProviderMethodResolver on your provider class
class UserSqlProvider implements ProviderMethodResolver {
  // In default implementation, it will resolve a method that method name is matched with mapper method
  public static String getUsersByName(final String name) {
    return new SQL(){{
      SELECT("*");
      FROM("users");
      if (name != null) {
        WHERE("name like #{value} || '%'");
      }
      ORDER_BY("id");
    }}.toString();
  }
}
```

This example shows usage the `databaseId` attribute on the statement annotation(Available since 3.5.5):

```java
@Select(value = "SELECT SYS_GUID() FROM dual", databaseId = "oracle") // Use this statement if DatabaseIdProvider provide "oracle"
@Select(value = "SELECT uuid_generate_v4()", databaseId = "postgres") // Use this statement if DatabaseIdProvider provide "postgres"
@Select("SELECT RANDOM_UUID()") // Use this statement if the DatabaseIdProvider not configured or not matches databaseId
String generateId();
```
