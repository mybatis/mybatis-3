title: MyBatis 3 | 日誌
author: Clinton Begin

<h1 class="d-none">Avoid blank site</h1>

## 日誌

MyBatis 透過使用內部日誌工廠（Log Factory）來提供日誌資訊。內部日誌工廠會將日誌資訊委派給下列日誌實作之一：

- SLF4J
- Apache Commons Logging
- Log4j 2
- Log4j（自 3.5.9 起已廢棄）
- JDK logging

所選擇的日誌解決方案取決於 MyBatis 內部日誌工廠的執行時期內部自我檢查（Runtime introspection）。MyBatis 日誌工廠將使用它找到的第一個日誌實作（依照上述順序搜尋實作）。如果 MyBatis 找不到上述任何實作，則將停用日誌記錄。

許多環境將 Commons Logging 作為應用程式伺服器類別路徑的一部分提供（常見範例包括 Tomcat 和 WebSphere）。請務必注意，在此類環境中，MyBatis 將使用 Commons Logging 作為日誌實作。在像 WebSphere 這樣的環境中，這將意味著您的 Log4J 設定將被忽略，因為 WebSphere 提供了自己的 Commons Logging 專屬實作。這可能令人非常沮喪，因為看起來 MyBatis 忽略了您的 Log4J 設定（事實上，MyBatis 忽略您的 Log4J 設定是因為 MyBatis 在此類環境中會使用 Commons Logging）。如果您的應用程式執行的環境在類別路徑中包含 Commons Logging，但您更希望使用其他日誌實作之一，您可以透過在 mybatis-config.xml 檔案中加入以下設定來選擇不同的日誌實作：

```xml
<configuration>
  <settings>
    ...
    <setting name="logImpl" value="LOG4J"/>
    ...
  </settings>
</configuration>
```

有效值包括 SLF4J、LOG4J、LOG4J2、JDK_LOGGING、COMMONS_LOGGING、STDOUT_LOGGING、NO_LOGGING，或者是實作了 `org.apache.ibatis.logging.Log` 且接受一個 String 作為建構子參數的完整限定類別名稱。

您也可以透過呼叫下列方法之一來選擇實作：

```java
org.apache.ibatis.logging.LogFactory.useSlf4jLogging();
org.apache.ibatis.logging.LogFactory.useLog4JLogging();
org.apache.ibatis.logging.LogFactory.useLog4J2Logging();
org.apache.ibatis.logging.LogFactory.useJdkLogging();
org.apache.ibatis.logging.LogFactory.useCommonsLogging();
org.apache.ibatis.logging.LogFactory.useStdOutLogging();
```

如果您選擇呼叫這些方法之一，應在呼叫任何其他 MyBatis 方法之前進行。此外，只有在該實作於執行時期類別路徑中可用時，這些方法才會切換到所請求的日誌實作。例如，如果您嘗試選擇 Log4J2 日誌，而 Log4J2 在執行時期不可用，則 MyBatis 將忽略使用 Log4J2 的請求，並使用其正常的演算法來尋找日誌實作。

SLF4J、Apache Commons Logging、Apache Log4J 和 JDK Logging API 的具體細節超出了本文件的範圍。但是，下面的範例設定應該可以幫助您入門。如果您想了解有關這些框架的更多資訊，可以從下列位置取得更多資訊：

- [SLF4J](https://www.slf4j.org/)
- [Apache Commons Logging](https://commons.apache.org/proper/commons-logging/)
- [Apache Log4j 2.x](https://logging.apache.org/log4j/2.x/)
- [JDK Logging API](https://docs.oracle.com/javase/8/docs/technotes/guides/logging/overview.html)

### 日誌設定

若要查看 MyBatis 日誌陳述式，您可以在套件、Mapper 完整限定類別名稱、命名空間或完整限定陳述式名稱上啟用日誌記錄。

同樣地，具體做法取決於所使用的日誌實作。我們將展示如何使用 SLF4J(Logback) 來完成。設定日誌服務只需要包含一或多個額外的設定檔（例如 `logback.xml`），有時還需要一個新的 JAR 檔。以下範例設定將以 SLF4J(Logback) 作為提供者來設定完整的日誌服務。包含 2 個步驟。

#### 步驟 1：新增 SLF4J 與 Logback JAR 檔

因為我們使用的是 SLF4J(Logback)，所以需要確保其 JAR 檔對我們的應用程式可用。要使用 SLF4J(Logback)，您需要將 JAR 檔加入至應用程式類別路徑中。

對於 Web 或企業級應用程式，您可以將 `logback-classic.jar`、`logback-core.jar` 和 `slf4j-api.jar` 加入至 `WEB-INF/lib` 目錄中；對於獨立應用程式，您只需將其加入至 JVM `-classpath` 啟動參數中。

如果您使用 Maven，可以透過在 `pom.xml` 中加入下列設定來下載 JAR 檔：

```xml
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-classic</artifactId>
  <version>1.x.x</version>
</dependency>
```

#### 步驟 2：設定 Logback

設定 Logback 很簡單。假設您要為此 Mapper 啟用日誌：

```java
package org.mybatis.example;
public interface BlogMapper {
  @Select("SELECT * FROM blog WHERE id = #{id}")
  Blog selectBlog(int id);
}
```

如下所示建立一個名為 `logback.xml` 的檔案並將其放在您的類別路徑中：

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

上述檔案將使 SLF4J(Logback) 回報 `org.mybatis.example.BlogMapper` 的詳細日誌，而對應用程式的其餘類別僅記錄錯誤。

如果您想以更精細的層級調整日誌記錄，可以針對特定陳述式開啟日誌記錄，而不是針對整個 Mapper 檔案。下列設定將僅為 `selectBlog` 陳述式啟用日誌記錄：

```xml
<logger name="org.mybatis.example.BlogMapper.selectBlog">
  <level value="trace"/>
</logger>
```

相反地，您可能想要為一組 Mapper 啟用日誌記錄。在那種情況下，您應該將 Mapper 所在的根套件新增為 logger：

```xml
<logger name="org.mybatis.example">
  <level value="trace"/>
</logger>
```

某些查詢可能會回傳龐大的結果集。在這種情況下，您可能希望看到 SQL 陳述式，但不希望看到結果。為此，SQL 陳述式記錄在 DEBUG 層級（在 JDK logging 中為 FINE），而結果記錄在 TRACE 層級（在 JDK logging 中為 FINER）。因此，如果您只想查看陳述式而不查看結果，請將層級設定為 DEBUG。

```xml
<logger name="org.mybatis.example">
  <level value="debug"/>
</logger>
```

但是，如果您使用的不是 Mapper 介面，而是像這樣的 Mapper XML 檔案呢？

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

在這種情況下，您可以透過為命名空間新增 logger 來為整個 XML 檔案啟用日誌記錄，如下所示：

```xml
<logger name="org.mybatis.example.BlogMapper">
  <level value="trace"/>
</logger>
```

或者針對特定陳述式：

```xml
<logger name="org.mybatis.example.BlogMapper.selectBlog">
  <level value="trace"/>
</logger>
```

是的，正如您可能已經注意到的，為 Mapper 介面或為 XML Mapper 檔案設定日誌記錄沒有任何差別。

<span class="label important">NOTE</span> 如果您使用的是 SLF4J 或 Log4j 2，MyBatis 將使用標記 `MYBATIS` 來呼叫它。

`logback.xml` 檔案中的其餘設定用於設定 appender，這超出了本文件的範圍。但是，您可以在 [Logback](https://logback.qos.ch/) 網站上找到更多資訊。或者，您可以直接進行實驗，看看不同的設定選項會產生什麼效果。

#### 設定範例：Log4j 2

```xml
<!-- pom.xml -->
<dependency>
  <groupId>org.apache.logging.log4j</groupId>
  <artifactId>log4j-core</artifactId>
  <version>2.x.x</version>
</dependency>
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- log4j2.xml -->
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

#### 設定範例：Log4j

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

#### 設定範例：JDK logging

```properties
# logging.properties
handlers=java.util.logging.ConsoleHandler
.level=SEVERE

org.mybatis.example.BlogMapper.level=FINER

java.util.logging.ConsoleHandler.level=ALL
java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format=%1$tT.%1$tL %4$s %3$s - %5$s%6$s%n
```
