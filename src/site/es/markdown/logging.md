title: MyBatis 3 | Logging
author: Clinton Begin, Eduardo Macarron

<h1 class="d-none">Avoid blank site</h1>

## Logging

MyBatis proporciona información de logging mediante el uso interno de una factoría. La factoría interna delega la información de logging en alguna de las siguientes implementaciones.

- SLF4J
- Apache Commons Logging
- Log4j 2
- Log4j (deprecated since 3.5.9)
- JDK logging

La solución de logging elegida se basa en una introspección en tiempo de ejecución de la propia factoría interna de MyBatis. La factoría usará la primera implementación de logging que encuentre (el orden de búsqueda es el de la lista de más arriba). Si no se encuentra ninguna, el logging se desactivará.

Muchos entornos vienen con Commons Logging incluido como pare del classpath del servidor (por ejemplo Tomcat y WebSphere). Es importante conocer que en esos entorno, MyBatis usará JCL como implementación de logging. En un entorno como WebSphere esto significa que tu configuración de log4j será ignorada dado que WebSphere proporciona su propia implementación de JCL. Esto puede ser muy frustrante porque parece que MyBatis está ignorando tu configuración de logging (en realidad, MyBatis está ignorando tu configuración de log4j porque está usando JCL en dicho entorno). Si tu aplicación se ejecuta en un entorno que lleva JCL incluido pero quieres usar un método distinto de logging puedes añadir un setting a tu fichero mybatis-config.xml:

```xml
<configuration>
  <settings>
    ...
    <setting name="logImpl" value="LOG4J"/>
    ...
  </settings>
</configuration>
```

Los valores válidos son: SLF4J, LOG4J, LOG4J2, JDK\_LOGGING, COMMONS\_LOGGING, STDOUT\_LOGGING, NO\_LOGGING o un nombre de clase plenamente cualificado que implemente `org.apache.ibatis.logging.Log` y reciba un string como parametro de constructor.

Tambien puedes seleccionar el método de logging llamando a uno de los siguientes métodos:

```java
org.apache.ibatis.logging.LogFactory.useSlf4jLogging();
org.apache.ibatis.logging.LogFactory.useLog4JLogging();
org.apache.ibatis.logging.LogFactory.useLog4J2Logging();
org.apache.ibatis.logging.LogFactory.useJdkLogging();
org.apache.ibatis.logging.LogFactory.useCommonsLogging();
org.apache.ibatis.logging.LogFactory.useStdOutLogging();
```

Si eliges llamar a alguno de estos métodos, deberías hacerlo antes de llamar a ningún otro método de MyBatis. Además, estos métodos solo establecerán la implementación de log indicada si dicha implementación está disponible en el classpath. Por ejemplo, si intentas seleccionar log4j2 y log4j2 no está disponible en el classpath, MyBatis ignorará la petición y usará su algoritmo normal de descubrimiento de implementaciones de logging.

Los temas específicos de JCL, Log4j y el Java Logging API quedan fuera del alcance de este documento. Sin embargo la configuración de ejemplo que se proporciona más abajo te ayudará a comenzar. Si quieres conocer más sobre estos frameworks, puedes obtener más información en las siguientes ubicaciones:

- [SLF4J](https://www.slf4j.org/)
- [Apache Commons Logging](https://commons.apache.org/proper/commons-logging/)
- [Apache Log4j 2.x](https://logging.apache.org/log4j/2.x/)
- [JDK Logging API](https://docs.oracle.com/javase/8/docs/technotes/guides/logging/overview.html)

### Configuración

Para ver el log de las sentencias debes activar el log en un paquete, el nombre plenamente cualificado de una clase, un namespace o un nombre plenamente cualificado de un mapped statement.

Nuevamente, cómo hagas esto es dependiente de la implementación de logging que se esté usando. Mostraremos cómo hacerlo con SLF4J(Logback). Configurar los servicios de logging es simplemente cuestión de añadir uno o varios ficheros de configuración (por ejemplo `logback.xml`) y a veces un nuevo JAR. El ejemplo siguiente configura todos los servicios de logging para que usen SLF4J(Logback) como proveedor. Sólo son dos pasos:

#### Paso 1: Añade el fichero SLF4J + Logback JAR

Dado que usamos SLF4J(Logback), necesitaremos asegurarnos que el fichero JAR está disponible para nuestra aplicación. Para usar SLF4J(Logback), necesitas añadir el fichero JAR al classpath de tu aplicación.

En aplicaciones Web o de empresa debes añadir tu fichero `logback-classic.jar` ,`logback-core.jar` and `slf4j-api.jar` a tu directorio `WEB-INF/lib`, y en una aplicación standalone simplemente añádela al parámetro `–classpath` de la JVM.

If you use the maven, you can download jar files by adding following settings on your `pom.xml`.

```xml
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-classic</artifactId>
  <version>1.x.x</version>
</dependency>
```

#### Paso 2: Configurar Logback

Configurar Logback es sencillo. Supongamos que quieres habilitar el log para este mapper:

```java
package org.mybatis.example;
public interface BlogMapper {
  @Select("SELECT * FROM blog WHERE id = #{id}")
  Blog selectBlog(int id);
}
```

Crea un fichero con nombre `logback.xml` como el que se muestra a continuación y colocalo en tu classpath:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration>
<configuration>

  <appender name="stdout" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%5level [%thread] - %msg%n</pattern>
    </encoder>
  </appender>

  <logger name="org.mybatis.example.BlogMapper">
    <level value="trace"/>
  </logger>
  <root level="error">
    <appender-ref ref="stdout"/>
  </root>

</configuration>
```

El fichero anterior hará que SLF4J(Logback) reporte información detallada para `org.mybatis.example.BlogMapper` e información de errores para el resto de las clases de tu aplicación.

Si quieres activar un nivel más fino de logging puedes activar el logging para statements específicos en lugar de para todo un mapper. La siguiente línea activa el logging sólo para el statement `selectBlog`:

```xml
<logger name="org.mybatis.example.BlogMapper.selectBlog">
  <level value="trace"/>
</logger>
```

Si por el contrario quieres activar el log para un grupo de mappers debes añadir un logger para el paquete raiz donde residen tus mappers:

```xml
<logger name="org.mybatis.example">
  <level value="trace"/>
</logger>
```

Hay consultas que pueden devolver una gran cantidad de datos. En esos casos puedes querer ver las sentencias SQL pero no los datos. Para conseguirlo las sentencias se loguean con nivel DEBUG (FINE en JDK) y los resultados con TRACE (FINER en JDK), por tanto si quieres ver la sentencia pero no el resultado establece el nivel a DEBUG

```xml
<logger name="org.mybatis.example">
  <level value="debug"/>
</logger>
```

Y si estás usando ficheros XML como este?

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

En tal caso puedes activar el logging de todo el fichero añadiendo un logger para el namespace como se muestra a continuación:

```xml
<logger name="org.mybatis.example.BlogMapper">
  <level value="trace"/>
</logger>
```

O para un statement específico:

```xml
<logger name="org.mybatis.example.BlogMapper.selectBlog">
  <level value="trace"/>
</logger>
```

Sí, como ya te habrás dado cuenta, no hay ninguna diferencia entre configurar el logging para un mapper o para un fichero XML.

<span class="label important">NOTA</span> Si usas SLF4J o Log4j 2 MyBatis le llamará usando `MYBATIS` como marker.

El resto de la configuración sirve para configurar los appenders, lo cual queda fuera del ámbito de este documento. Sin embargo, puedes encontrar más información en el site de [Logback](https://logback.qos.ch/). O, puedes simplemente experimentar para ver los efectos que consigues con las distintas opciones de configuración.

#### Configuration example for Log4j 2

```xml
<!-- pom.xml -->
<dependency>
  <groupId>org.apache.logging.log4j</groupId>
  <artifactId>log4j-core</artifactId>
  <version>2.x.x</version>
</dependency>
```

```xml
<!-- log4j2.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<Configuration xmlns="http://logging.apache.org/log4j/2.0/config">

  <Appenders>
    <Console name="stdout" target="SYSTEM_OUT">
      <PatternLayout pattern="%5level [%t] - %msg%n"/>
    </Console>
  </Appenders>

  <Loggers>
    <Logger name="org.mybatis.example.BlogMapper" level="trace"/>
    <Root level="error" >
      <AppenderRef ref="stdout"/>
    </Root>
  </Loggers>

</Configuration>
```

#### Configuration example for Log4j

```xml
<!-- pom.xml -->
<dependency>
  <groupId>log4j</groupId>
  <artifactId>log4j</artifactId>
  <version>1.2.17</version>
</dependency>
```

```properties
# log4j.properties
log4j.rootLogger=ERROR, stdout

log4j.logger.org.mybatis.example.BlogMapper=TRACE

log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%5p [%t] - %m%n
```

#### Configuration example for JDK logging

```properties
# logging.properties
handlers=java.util.logging.ConsoleHandler
.level=SEVERE

org.mybatis.example.BlogMapper=FINER

java.util.logging.ConsoleHandler.level=ALL
java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format=%1$tT.%1$tL %4$s %3$s - %5$s%6$s%n
```
