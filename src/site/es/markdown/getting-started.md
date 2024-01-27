title: MyBatis 3 | Primeros pasos
author: Clinton Begin, Eduardo Macarron

# Primeros pasos

## Instalación

Para usar MyBatis sólo tienes que incluir el fichero [mybatis-x.x.x.jar](https://github.com/mybatis/mybatis-3/releases) en el classpath.

Si usas Maven añade esta dependencia en tu pom.xml:

```xml
<dependency>
  <groupId>org.mybatis</groupId>
  <artifactId>mybatis</artifactId>
  <version>x.x.x</version>
</dependency>
```

## Cómo crear un SqlSessionFactory a partir de XML

Una aplicación que usa MyBatis debe utilizar una instancia de SqlSessionFactory. Se puede obtener una instancia de SqlSessionFactory mediante un SqlSessionFactoryBuilder. Un SqlSessionFactoryBuilder puede construir una instancia de SqlSessionFactory a partir de un fichero de configuración XML o de una instancia personalizada de la clase Configuration.

Crear una instancia SqlSessionFactory desde un fichero xml es muy sencillo. Se recomienda usar un classpath resource, pero es posible usar cualquier InputStream, incluso creado con un path de fichero o una URL de tipo `file://`. MyBatis proporciona una clase de utilidad, llamada Resources, que contiene métodos que simplifican la carga de recursos desde el classpath u otras ubicaciones.

```java
String resource = "org/mybatis/example/mybatis-config.xml";
InputStream inputStream = Resources.getResourceAsStream(resource);
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
```

El fichero de configuración XML contiene la configuración del _core_ de MyBatis, incluyendo el DataSource para obtener instancias de conexión a la base de datos y también un TransactionManager para determinar cómo deben controlarse las transacciones. Los detalles completos de la configuración XML se describen más adelante en este documento, pero continuación se muestra un ejemplo sencillo:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "https://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
  <environments default="development">
    <environment id="development">
      <transactionManager type="JDBC"/>
      <dataSource type="POOLED">
        <property name="driver" value="${driver}"/>
        <property name="url" value="${url}"/>
        <property name="username" value="${username}"/>
        <property name="password" value="${password}"/>
      </dataSource>
    </environment>
  </environments>
  <mappers>
    <mapper resource="org/mybatis/example/BlogMapper.xml"/>
  </mappers>
</configuration>
```

Aunque hay mucha más información sobre el fichero de configuración XML, el ejemplo anterior contiene las partes más importantes. Observa que hay una cabecera XML, requerida para validar el fichero XML. El cuerpo del elemento environment contiene la configuración de la gestión de transacciones correspondiente al entorno. El elemento mappers contiene la lista de mappers – Los ficheros XML que contienen las sentencias SQL y las definiciones de mapeo.

## Cómo crear un SqlSessionFactory sin XML

Si lo prefieres puedes crear la configuración directamente desde Java, en lugar de desde XML, o crear tu propio _builder_ . MyBatis dispone una clase Configuration que proporciona las mismas opciones de configuración que el fichero XML.

```java
DataSource dataSource = BlogDataSourceFactory.getBlogDataSource();
TransactionFactory transactionFactory = new JdbcTransactionFactory();
Environment environment = new Environment("development", transactionFactory, dataSource);
Configuration configuration = new Configuration(environment);
configuration.addMapper(BlogMapper.class);
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
```

Puedes observar que en este caso la configuración está añadiendo una clase mapper. Las clases mapper son clases Java que contienen anotaciones de mapeo SQL que permiten evitar el uso de XML. Sin embargo el XML sigue siendo necesario en ocasiones, debido a ciertas limitaciones de las anotaciones Java y la complejidad que pueden alcanzar los mapeos (ej. mapeos anidados de Joins). Por esto, MyBatis siempre busca si existe un fichero XML asociado a la clase mapper (en este caso, se buscará un fichero con nombre BlogMapper.xml cuyo nombre deriva del classpath y nombre de BlogMapper.class). Hablaremos más sobre esto más adelante.

## Cómo obtener un SqlSession a partir del SqlSessionFactory

Ahora que dispones de un SqlSessionFactory, tal y como su nombre indica, puedes adquirir una instancia de SqlSession. SqlSession contiene todos los métodos necesarios para ejecutar sentencias SQL contra la base de datos. Puedes ejecutar mapped statements con la instancia de SqlSession de la siguiente forma:

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  Blog blog = (Blog) session.selectOne("org.mybatis.example.BlogMapper.selectBlog", 101);
}
```

Aunque esta forma de trabajar con la SqlSession funciona correctamente y les será familiar a aquellos que han usado las versiones anteriores de MyBatis, actualmente existe una opción más recomendada. Usar un interface (ej. BlogMapper.class) que describe tanto el parámetro de entrada como el de retorno para una sentencia. De esta forma tendrás un código más sencillo y type safe, sin castings ni literales de tipo String que son fuente frecuente de errores.

Por ejemplo:

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  BlogMapper mapper = session.getMapper(BlogMapper.class);
  Blog blog = mapper.selectBlog(101);
}
```

Vemos con detalle cómo funciona esto.

## Cómo funcionan los Mapped Statements

Te estarás preguntando qué se está ejecutando en SqlSession o en la clase Mapper. Los _Mapped Statements_ son una materia muy densa, y será el tema que domine la mayor parte de esta documentación. Pero, para que te hagas una idea de qué se está ejecutando realmente proporcionaremos un par de ejemplos.

En cualquiera de los ejemplos a continuación podrían haberse usado indistintamente XML o anotaciones. Veamos primero el XML. Todas las opciones de configuración de MyBatis pueden obtenerse mediante el lenguaje de mapeo XML que ha popularizado a MyBatis durante años. Si ya has usado MyBatis antes el concepto te será familiar, pero verás que hay numerosas mejoras en los ficheros de mapeo XML que iremos explicando más adelante. Por ejemplo este mapped statement en XML haría funcionar correctamente la llamada al SqlSession que hemos visto previamente.

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.mybatis.example.BlogMapper">
  <select id="selectBlog" resultType="Blog">
    select * from Blog where id = #{id}
  </select>
</mapper>
```

Aunque pudiera parecer que hay excesivo XML para un ejemplo tan simple, en realidad no hay tanto. Puedes definir tantos mapped statements en un solo fichero XML como quieras así que rentabilizarás las líneas XML extra que corresponden a la cabecera XML y a la declaración de doctype. El resto del fichero se explica por sí mismo. Define un nombre para el mapped statement “selectBlog”, en un namespace (espacio de nombres) “org.mybatis.example.BlogMapper”, que permite realizar una llamada especificando el nombre completo (fully qualified) “org.mybatis.example.BlogMapper.selectBlog” tal y como muestra el código a continuación:

```java
Blog blog = session.selectOne("org.mybatis.example.BlogMapper.selectBlog", 101);
```

Observa el gran parecido que hay entre esta llamada y la llamada a una clase java y hay una razón para eso. Este literal puede mapearse con una clase que tenga el mismo nombre que el namespace, con un método que coincida con el nombre del statement, y con parámetros de entrada y retorno iguales que los del statement. Esto permite que puedas hacer la misma llamada contra una interfaz Mapper tal y como se muestra a continuación:

```java
BlogMapper mapper = session.getMapper(BlogMapper.class);
Blog blog = mapper.selectBlog(101);
```

Este segunda forma de llamada tiene muchas ventajas. Primeramente, no se usan literales de tipo String lo cual es mucho más seguro dado que los errores se detectan en tiempo de compilación. Segundo, si tu IDE dispone de autocompletado de código podrás aprovecharlo.

---

<span class="label important">NOTA</span> **Una nota sobre los namespaces.**

**Los namespaces (espacios de nombres)** eran opcionales en versiones anteriores de MyBatis, lo cual creaba confusión y era de poca ayuda. Los namespaces son ahora obligatorios y tienen un propósito más allá de la clasificación de statements.

Los namespaces permiten realizar el enlace con los interfaces como se ha visto anteriormente, e incluso si crees que no los vas a usar a corto plazo, es recomendable que sigas estas prácticas de organización de código por si en un futuro decides hacer lo contrario. Usar un namespace y colocarlo en el paquete java que corresponde con el namespace hará tu código más legible y mejorará la usabilidad de MyBatis a largo plazo.

**Resolución de nombres:** Para reducir la cantidad de texto a escribir MyBatis usa las siguientes normas de resolución de nombres para todos los elementos de configuración, incluidos statements, result maps, cachés, etc.

- Primeramente se buscan directamente los nombres completamente cualificados (fully qualified names) (ej. “com.mypackage.MyMapper.selectAllThings”).
- Pueden usarse los nombres cortos (ej. “selectAllThings”) siempre que no haya ambigüedad. Sin embargo, si hubiera dos o más elementos (ej. “com.foo.selectAllThings" y "com.bar.selectAllThings”), entonces obtendrás un error indicando que el nombre es ambiguo y que debe ser “fully qualified”.

---

Hay otro aspecto importante sobre las clases Mapper como BlogMapper. Sus mapped statements pueden no estar en ningún fichero XML. En su lugar, pueden usarse anotaciones. Por ejemplo, el XML puede ser eliminado y reemplazarse por:

```java
package org.mybatis.example;
public interface BlogMapper {
  @Select("SELECT * FROM blog WHERE id = #{id}")
  Blog selectBlog(int id);
}
```

Las anotaciones son mucho más claras para sentencias sencillas, sin embargo, las anotaciones java son limitadas y más complicadas de usar para sentencias complejas. Por lo tanto, si tienes que hacer algo complejo, es mejor que uses los ficheros XML.

Es decisión tuya y de tu proyecto cuál de los dos métodos usar y cómo de importante es que los mapped statements estén definidos de forma consistente. Dicho esto, no estás limitado a usar un solo método, puedes migrar fácilmente de los mapped statements basados en anotaciones a XML y viceversa.

## Ámbito y ciclo de vida

Es muy importante entender los distintos ámbitos y ciclos de vida de las clases de las que hemos hablado hasta ahora. Usarlas de forma incorrecta puede traer serias complicaciones.

---

<span class="label important">NOTA</span> **Ciclo de vida de los objetos y frameworks de inyección de dependencias**

Los frameworks de inyección de dependencias pueden crear SqlSessions y mappers _thread safe_ (reeentrante) y transaccionales, e inyectarlos directmaente en tus beans de forma que puedes olvidarte de su ciclo de vida. Echa un vistazo a los sub-projectos MyBatis-Spring o MyBatis-Guice para conocer más detalles sobre cómo usar MyBatis con estos frameworks.

---

#### SqlSessionFactoryBuilder

Esta clase puede instanciarse, usarse y desecharse. No es necesario mantenerla una vez que ya has creado la SqlSessionFactory. Por lo tanto el mejor ámbito para el SqlSessionFactoryBuilder es el método (ej. una variable local de método). Puedes reusar el SqlSessionFactoryBuilder para construir más de una instancia de SqlSessionFactory, pero aun así es recomendable no conservar el objeto para asegurarse de que todos los recursos utilizados para el parseo de XML se han liberado correctamente y están disponibles para temas más importantes.

#### SqlSessionFactory

Una vez creado, el SqlSessionFactory debería existir durante toda la ejecución de tu aplicación. No debería haber ningún o casi ningún motivo para eliminarlo o recrearlo. Es una buena práctica el no recrear el SqlSessionFactory en tu aplicación más de una vez. Y lo contrario debería considerarse código sospechoso. Por tanto el mejor ámbito para el SqlSessionFactory es el ámbito de aplicación. Esto puede conseguirse de muchas formas. Lo más sencillo es usar el patrón Singleton o el Static Singleton.

#### SqlSession

Cada thread (hilo de ejecución) debería tener su propia instancia de SqlSession. Las instancias de SqlSession no son thread safe y no deben ser compartidas. Por tanto el ámbito adecuado es el de petición (request) o bien el método. No guardes nunca instancias de SqlSession en un campo estático o incluso en una propiedad de instancia de una clase. Nunca guardes referencias a una SqlSession en ningún tipo de ámbito gestionado como la HttpSession. Si estás usando un framework web considera que el SqlSession debería tener un ámbito similar al HttpRequest. Es decir, cuando recibas una petición http puedes abrir una SqlSession y cerrarla cuando devuelvas la respuesta. Cerrar la SqlSession es muy importante. Deberías asegurarte de que se cierra con un bloque finally. A continuación se muestra el patrón estándar para asegurarse de que las sesiones se cierran correctamente.

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  // do work
}
```

Usando este patrón en todo el código se asegura que los recursos de base de datos se liberarán correctamente.

#### Instancias de mapper

Los mappers son interfaces que creas como enlace con los mapped statements. Las instancias de mappers se obtienen de una SqlSession. Y por tanto, técnicamente el mayor ámbito de una instancia de Mapper es el mismo que el de la SqlSession de la que fueron creados. Sin embargo el ámbito más recomendable para una instancia de mapper es el ámbito de método. Es decir, deberían ser obtenidos en el método que vaya a usarlos y posteriormente descartarlos. No es necesario que sean cerrados explícitamente. Aunque no es un problema propagar estos objetos por varias clases dentro de una misma llamada, debes tener cuidado porque puede que la situación se te vaya de las manos. Hazlo fácil (keep it simple) y mantén los mappers en el ámbito de método. Este ejemplo muestra esta práctica:

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  BlogMapper mapper = session.getMapper(BlogMapper.class);
  // do work
}

```
