title: MyBatis 3 | ロギング
author: Clinton Begin, Iwao AVE!

<h1 class="d-none">Avoid blank site</h1>

## ロギング

MyBatis は、内部の Log Factory を通してログ情報を出力します。この Log Factory は、ログ情報を次に挙げる実装のいずれかに委譲（delegate）します。

- SLF4J
- Apache Commons Logging
- Log4j 2
- Log4j (3.5.9以降非推奨)
- JDK logging

実際に使用されるのは、MyBatis 内部の Log Factory が検出した実装になります。MyBatis の Log Factory は上に挙げた順番でロギング実装を検索し、最初に見つけた実装を使用します。上記の実装が検出できなかった場合、ログは出力されません。

アプリケーションサーバーでは、出荷時のクラスパスに Commons Logging が含まれていることがよくあります（Tomcat や WebSphere は良い例でしょう）。重要なのは、このような環境では MyBatis は Commons Logging を使用するということです。これはつまり、独自の Commons Logging 実装を使う WebSphere のような環境では、あなたが追加した Log4J の設定は無視されるということを意味しています。この現象が厄介なのは、MyBatis が Log4J の設定を無視しているように見えるということです（実は、このような環境では MyBatis が Commons Loggin を使用するため、Log4J の設定が無視されているのです）。クラスパスに Commons Logging を含む環境で動作するアプリケーションでも、mybatis-config.xml に設定を追加することで別のロギング実装を使用することができます。

```xml
<configuration>
  <settings>
    ...
    <setting name="logImpl" value="LOG4J"/>
    ...
  </settings>
</configuration>
```

指定可能な値は SLF4J, LOG4J, LOG4J2, JDK\_LOGGING, COMMONS\_LOGGING, STDOUT\_LOGGING, NO\_LOGGING ですが、`org.apache.ibatis.logging.Log` インターフェイスを実装し、コンストラクター引数として String を受け取る独自に実装したクラスの完全修飾クラス名を指定することもできます。

下記のメソッドを呼び出すことでロギング実装を指定することも可能です。

```java
org.apache.ibatis.logging.LogFactory.useSlf4jLogging();
org.apache.ibatis.logging.LogFactory.useLog4JLogging();
org.apache.ibatis.logging.LogFactory.useLog4J2Logging();
org.apache.ibatis.logging.LogFactory.useJdkLogging();
org.apache.ibatis.logging.LogFactory.useCommonsLogging();
org.apache.ibatis.logging.LogFactory.useStdOutLogging();
```

これらのメソッドは、他の MyBatis のメソッドより前に呼び出す必要があります。また、要求された実装が実行時のクラスパスに含まれている場合にのみ切り替えることが可能です。例えば、Log4J2 に切り替えようとして、実行時に Log4J2 が見つからない場合、MyBatis は切り替えの要求を無視して通常のアルゴリズムでロギング実装を検索します。

SLF4J, Apache Commons Logging, Apache Log4J, JDK Logging API についての詳細はこのドキュメントの範囲外となりますが、後述の設定例は参考になると思います。これらのフレームワークについての詳しい情報は、以下の各サイトを参照してください。

- [SLF4J](https://www.slf4j.org/)
- [Apache Commons Logging](https://commons.apache.org/proper/commons-logging/)
- [Apache Log4j 2.x](https://logging.apache.org/log4j/2.x/)
- [JDK Logging API](https://docs.oracle.com/javase/8/docs/technotes/guides/logging/overview.html)

### Logging Configuration

実行されるステートメントのログを出力するためには、パッケージ、Mapper の完全修飾名、ネームスペース、あるいはステートメントの完全修飾名に対してログ出力を有効にしてください。

具体的な設定方法は使用するロギング実装によります。以下は SLF4J(Logback) での設定例です。ロギングサービスの設定は、単純にいくつかの設定ファイル（例えば `logback.xml`）と、場合によっては新しい JARを追加するだけのことです。以下は、SLF4J(Logback) をプロバイダーとして完全なロギングサービスを設定する手順です。

#### ステップ１: SLF4J + Logback の JAR ファイルを追加する。

SLF4J(Logback) を使うので、SLF4J(Logback) の JAR ファイルがアプリケーションから利用できるようにしておく必要があります。SLF4J(Logback) の JAR ファイルをダウンロードしてあなたのアプリケーションのクラスパスに追加してください。

Web あるいはエンタープライズアプリケーションの場合は、ダウンロードした `logback-classic.jar` ,`logback-core.jar`, `slf4j-api.jar` を `WEB-INF/lib` ディレクトリに追加します。スタンドアローンアプリケーションの場合は起動時の JVM 引数 `-classpath` に追加するだけです。

Mavenを利用している場合は、`pom.xml`に以下のような設定を追加することでJARファイルをダウンロードすることができます。

```xml
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-classic</artifactId>
  <version>1.x.x</version>
</dependency>
```

#### ステップ２: Logback を設定する。

Logback の設定はシンプルです。例えば次の Mapper のログを出力する場合：

```java
package org.mybatis.example;
public interface BlogMapper {
  @Select("SELECT * FROM blog WHERE id = #{id}")
  Blog selectBlog(int id);
}
```

次のテキストを含む `logback.xml` というファイルを作成し、クラスパスに配置します。

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

上記のように設定すると、SLF4J(Logback) は `org.mybatis.example.BlogMapper` について詳細なログを出力し、それ以外のクラスについてはエラーのみを出力します。

ログに出力される情報を細かく調整したいのなら、Mapper ファイル全体ではなく特定のステートメントを指定することもできます。

```xml
<logger name="org.mybatis.example.BlogMapper.selectBlog">
  <level value="trace"/>
</logger>
```

逆に、複数の Mapper に対するログを有効化したい場合もあるでしょう。その場合は、対象となる Mapper を含むパッケージを指定することができます。

```xml
<logger name="org.mybatis.example">
  <level value="trace"/>
</logger>
```

クエリが大量の結果セットを返すようなケースでSQLステートメントのみを出力したい場合に対応できるよう、SQLステートメントは DEBUG（JDK logging では FINE）レベル、結果は TRACE（JDK logging では FINER）レベルで出力されるようになっています。SQLステートメントのみを出力したい場合、ログレベルに DEBUG を設定します。

```xml
<logger name="org.mybatis.example">
  <level value="debug"/>
</logger>
```

Mapper インターフェイスを使っていない場合、例えば次のような Mapper XML ファイルを使っていたらどうすれば良いのでしょうか。

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

この場合、次のようにネームスペースを指定することでこの XML ファイル全体のログを有効化することができます。

```xml
<logger name="org.mybatis.example.BlogMapper">
  <level value="trace"/>
</logger>
```

特定のステートメントのみを対象とする場合は次のように指定します。

```xml
<logger name="org.mybatis.example.BlogMapper.selectBlog">
  <level value="trace"/>
</logger>
```

お気づきのように、Mapper インターフェイスと XML のどちらを使っている場合でも、設定方法に違いはありません。

<span class="label important">NOTE</span> SLF4J or Log4j 2 をお使いの場合、MyBatis のログは `MYBATIS` というマーカーで出力されます。

上記の `logback.xml` の残りの部分はアペンダーの設定になっていますが、このドキュメントでは説明しません。[Logback](https://logback.qos.ch/) のサイトを参照してください。あるいは、設定値を変更してみてどのような結果になるか試してみるのも良いでしょう。

#### Log4j 2の設定例

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

#### Log4jの設定例

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

#### JDK Loggingの設定例

```properties
# logging.properties
handlers=java.util.logging.ConsoleHandler
.level=SEVERE

org.mybatis.example.BlogMapper=FINER

java.util.logging.ConsoleHandler.level=ALL
java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format=%1$tT.%1$tL %4$s %3$s - %5$s%6$s%n
```
