title: MyBatis 3 | Logging
author: Clinton Begin

<h1 class="d-none">Avoid blank site</h1>

## Logging

MyBatis provides logging information through the use of an internal log factory. The internal log factory will delegate logging information to one of the following log implementations:

- SLF4J
- Apache Commons Logging
- Log4j 2
- Log4j (deprecated since 3.5.9)
- JDK logging

The logging solution chosen is based on runtime introspection by the internal MyBatis log factory. The MyBatis log factory will use the first logging implementation it finds (implementations are searched in the above order). If MyBatis finds none of the above implementations, then logging will be disabled.

Many environments ship Commons Logging as a part of the application server classpath (good examples include Tomcat and WebSphere). It is important to know that in such environments, MyBatis will use Commons Logging as the logging implementation. In an environment like WebSphere this will mean that your Log4J configuration will be ignored because WebSphere supplies its own proprietary implementation of Commons Logging. This can be very frustrating because it will appear that MyBatis is ignoring your Log4J configuration (in fact, MyBatis is ignoring your Log4J configuration because MyBatis will use Commons Logging in such environments). If your application is running in an environment where Commons Logging is included in the classpath but you would rather use one of the other logging implementations you can select a different logging implementation by adding a setting in mybatis-config.xml file as follows:

```xml
<configuration>
  <settings>
    ...
    <setting name="logImpl" value="LOG4J"/>
    ...
  </settings>
</configuration>
```

Valid values are SLF4J, LOG4J, LOG4J2, JDK_LOGGING, COMMONS_LOGGING, STDOUT_LOGGING, NO_LOGGING or a full qualified class name that implements `org.apache.ibatis.logging.Log` and gets an string as a constructor parameter.

You can also select the implementation by calling one of the following methods:

```java
org.apache.ibatis.logging.LogFactory.useSlf4jLogging();
org.apache.ibatis.logging.LogFactory.useLog4JLogging();
org.apache.ibatis.logging.LogFactory.useLog4J2Logging();
org.apache.ibatis.logging.LogFactory.useJdkLogging();
org.apache.ibatis.logging.LogFactory.useCommonsLogging();
org.apache.ibatis.logging.LogFactory.useStdOutLogging();
```

If you choose to call one of these methods, you should do so before calling any other MyBatis method. Also, these methods will only switch to the requested log implementation if that implementation is available on the runtime classpath. For example, if you try to select Log4J2 logging and Log4J2 is not available at runtime, then MyBatis will ignore the request to use Log4J2 and will use it's normal algorithm for discovering logging implementations.

The specifics of SLF4J, Apache Commons Logging, Apache Log4J and the JDK Logging API are beyond the scope of this document. However the example configuration below should get you started. If you would like to know more about these frameworks, you can get more information from the following locations:

- [SLF4J](https://www.slf4j.org/)
- [Apache Commons Logging](https://commons.apache.org/proper/commons-logging/)
- [Apache Log4j 2.x](https://logging.apache.org/log4j/2.x/)
- [JDK Logging API](https://docs.oracle.com/javase/8/docs/technotes/guides/logging/overview.html)

### Logging Configuration

To see MyBatis logging statements you may enable logging on a package, a mapper fully qualified class name, a namespace o a fully qualified statement name.

Again, how you do this is dependent on the logging implementation in use. We'll show how to do it with SLF4J(Logback). Configuring the logging services is simply a matter of including one or more extra configuration files (e.g. `logback.xml`) and sometimes a new JAR file. The following example configuration will configure full logging services using SLF4J(Logback) as a provider. There are 2 steps.

#### Step 1: Add the SLF4J + Logback JAR files

Because we are using SLF4J(Logback), we will need to ensure its JAR file is available to our application. To use SLF4J(Logback), you need to add the JAR file to your application classpath.

For web or enterprise applications you can add the `logback-classic.jar` ,`logback-core.jar` and `slf4j-api.jar` to your `WEB-INF/lib` directory, or for a standalone application you can simply add it to the JVM `-classpath` startup parameter.

If you use the maven, you can download jar files by adding following settings on your `pom.xml`.

```xml
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-classic</artifactId>
  <version>1.x.x</version>
</dependency>
```

#### Step 2: Configure Logback

Configuring Logback is simple. Suppose you want to enable the log for this mapper:

```java
package org.mybatis.example;
public interface BlogMapper {
  @Select("SELECT * FROM blog WHERE id = #{id}")
  Blog selectBlog(int id);
}
```

Create a file called `logback.xml` as shown below and place it in your classpath:

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

The above file will cause SLF4J(Logback) to report detailed logging for `org.mybatis.example.BlogMapper` and just errors for the rest of the classes of your application.

If you want to tune the logging at a finer level you can turn logging on for specific statements instead of the whole mapper file. The following line will enable logging just for the `selectBlog` statement:

```xml
<logger name="org.mybatis.example.BlogMapper.selectBlog">
  <level value="trace"/>
</logger>
```

By the contrary you may want to enable logging for a group of mappers. In that case you should add as a logger the root package where your mappers reside:

```xml
<logger name="org.mybatis.example">
  <level value="trace"/>
</logger>
```

There are queries that can return huge result sets. In that cases you may want to see the SQL statement but not the results. For that purpose SQL statements are logged at the DEBUG level (FINE in JDK logging) and results at the TRACE level (FINER in JDK logging), so in case you want to see the statement but not the result, set the level to DEBUG.

```xml
<logger name="org.mybatis.example">
  <level value="debug"/>
</logger>
```

But what about if you are not using mapper interfaces but mapper XML files like this one?

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

In that case you can enable logging for the whole XML file by adding a logger for the namespace as shown below:

```xml
<logger name="org.mybatis.example.BlogMapper">
  <level value="trace"/>
</logger>
```

Or for an specific statement:

```xml
<logger name="org.mybatis.example.BlogMapper.selectBlog">
  <level value="trace"/>
</logger>
```

Yes, as you may have noticed, there is no difference in configuring logging for mapper interfaces or for XML mapper files.

<span class="label important">NOTE</span> If you are using SLF4J or Log4j 2 MyBatis will call it using the marker `MYBATIS`.

The remaining configuration in the `logback.xml` file is used to configure the appenders, which is beyond the scope of this document. However, you can find more information at the [Logback](https://logback.qos.ch/) website. Or, you could simply experiment with it to see what effects the different configuration options have.

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
