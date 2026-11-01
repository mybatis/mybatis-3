title: MyBatis 3 | 新手入門
author: Clinton Begin

<h1 class="d-none">Avoid blank site</h1>

## 新手入門

### 安裝

要使用 MyBatis，您只需將 [mybatis-x.x.x.jar](https://github.com/mybatis/mybatis-3/releases) 檔案加入類別路徑（classpath）中。

如果您使用 Maven，只需在 pom.xml 中加入下列相依性：

```xml
<dependency>
  <groupId>org.mybatis</groupId>
  <artifactId>mybatis</artifactId>
  <version>x.x.x</version>
</dependency>
```

### 從 XML 建構 SqlSessionFactory

每個 MyBatis 應用程式都是以 SqlSessionFactory 的實例為核心。SqlSessionFactory 實例可以透過 SqlSessionFactoryBuilder 取得。SqlSessionFactoryBuilder 可以從 XML 設定檔或自訂準備的 Configuration 類別實例建構出 SqlSessionFactory 實例。

從 XML 檔案建構 SqlSessionFactory 實例非常簡單。建議您使用類別路徑資源（classpath resource）進行此設定，但您也可以使用任何 InputStream 實例，包括從常值檔案路徑或 `file://` URL 建立的實例。MyBatis 包含一個名為 Resources 的工具類別，其中包含許多方法，讓從類別路徑與其他位置載入資源變得更加容易。

```java
String resource = "org/mybatis/example/mybatis-config.xml";
InputStream inputStream = Resources.getResourceAsStream(resource);
SqlSessionFactory sqlSessionFactory =
  new SqlSessionFactoryBuilder().build(inputStream);
```

XML 設定檔包含 MyBatis 系統核心的設定，包括取得資料庫 Connection 實例的 DataSource（資料來源），以及決定交易作用域與控制方式的 TransactionManager（交易管理員）。XML 設定檔的完整詳細資訊可在此文件後續章節找到，但以下是一個簡單的範例：

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

雖然 XML 設定檔還有許多內容，但上述範例指出了最關鍵的部分。請注意 XML 標頭，它是驗證 XML 文件所必需的。environment 元素的內容包含交易管理與連線池的環境設定。mappers 元素包含映射器清單——即包含 SQL 程式碼與映射定義的 XML 檔案及/或帶有註解的 Java 介面類別。

### 不使用 XML 建構 SqlSessionFactory

如果您偏好直接從 Java 程式碼建構設定，而非使用 XML，或者想要建立自己的設定建構器，MyBatis 提供了完整的 Configuration 類別，它提供與 XML 檔案完全相同的所有設定選項。

```java
DataSource dataSource = BlogDataSourceFactory.getBlogDataSource();
TransactionFactory transactionFactory =
  new JdbcTransactionFactory();
Environment environment =
  new Environment("development", transactionFactory, dataSource);
Configuration configuration = new Configuration(environment);
configuration.addMapper(BlogMapper.class);
SqlSessionFactory sqlSessionFactory =
  new SqlSessionFactoryBuilder().build(configuration);
```

請注意，在此情況下，設定中加入了一個映射器類別（Mapper class）。映射器類別是包含 SQL 映射註解的 Java 類別，可避免對 XML 映射的需求。然而，由於 Java 註解的某些限制以及某些 MyBatis 映射的複雜性，最進階的映射（例如：巢狀外部聯結映射 Nested Join Mapping）仍需要 XML 映射。因此，如果存在同儕 XML 檔案，MyBatis 會自動尋找並載入它（在此範例中，將根據 BlogMapper.class 的類別路徑與名稱載入 BlogMapper.xml）。稍後會有更多相關說明。

### 從 SqlSessionFactory 取得 SqlSession

現在您已經擁有 SqlSessionFactory，顧名思義，您可以取得 SqlSession 的實例。SqlSession 包含對資料庫執行 SQL 命令所需的絕對所有方法。您可以直接對 SqlSession 實例執行已映射的 SQL 陳述式。例如：

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  Blog blog = session.selectOne(
    "org.mybatis.example.BlogMapper.selectBlog", 101);
}
```

雖然這種做法可行，而且對先前版本 MyBatis 的使用者來說很熟悉，但現在有一種更簡潔的方法。透過使用能正確描述特定陳述式之參數與回傳值的介面（例如 BlogMapper.class），您現在可以執行更簡潔且具備型別安全的程式碼，而不會有容易出錯的字串常值與型別轉換。

例如：

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  BlogMapper mapper = session.getMapper(BlogMapper.class);
  Blog blog = mapper.selectBlog(101);
}
```

現在讓我們來探討這裡究竟執行了什麼。

### 探討已映射的 SQL 陳述式

此時您可能會想知道 SqlSession 或 Mapper 類別到底執行了什麼。已映射的 SQL 陳述式（Mapped SQL Statement）是一個廣泛的主題，且該主題很可能會佔據本文件的大部分內容。但為了讓您了解實際執行的內容，這裡提供幾個範例。

在上述任一範例中，陳述式都可以透過 XML 或註解來定義。讓我們先來看看 XML。透過使用多年來讓 MyBatis 聞名的 XML 映射語言，可以實現 MyBatis 提供的完整功能集。如果您以前使用過 MyBatis，這個概念對您來說會很熟悉，但 XML 映射文件有許多改進，稍後會更清楚說明。以下是一個基於 XML 的映射陳述式範例，它能滿足上述 SqlSession 呼叫：

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

雖然對於這個簡單範例來說，這看起來像是很多額外負擔，但實際上它非常輕量。您可以在單一 mapper XML 檔案中定義任意數量的已映射陳述式，因此您可以從 XML 標頭與 doctype 宣告中獲得很高的效益。檔案的其餘部分非常一目了然。它在命名空間「org.mybatis.example.BlogMapper」中為已映射陳述式定義了一個名稱「selectBlog」，這允許您透過指定完整限定名稱「org.mybatis.example.BlogMapper.selectBlog」來呼叫它，如同我們在下列範例中所做的那樣：

```java
Blog blog = session.selectOne(
  "org.mybatis.example.BlogMapper.selectBlog", 101);
```

請注意這與在完整限定的 Java 類別上呼叫方法是多麼相似，這是有原因的。此名稱可以直接映射至與命名空間同名的 Mapper 類別，並且具有與映射的 select 陳述式之名稱、參數和回傳型別相符的方法。這允許您非常簡單地對 Mapper 介面呼叫方法，如您在上方所見，但以下在範例中再次展示：

```java
BlogMapper mapper = session.getMapper(BlogMapper.class);
Blog blog = mapper.selectBlog(101);
```

第二種做法具有許多優點。首先，它不依賴字串常值，因此安全得多。其次，如果您的 IDE 具有程式碼自動補全功能，您可以在瀏覽已映射的 SQL 陳述式時利用該功能。

------

<span class="label important">NOTE</span> **關於命名空間的說明。**

在先前版本的 MyBatis 中，**命名空間（Namespaces）**是選用的，這既令人困惑又沒有幫助。現在命名空間是強制的，其目的不僅僅是使用較長的完整限定名稱來隔離陳述式。

命名空間實現了如您在此處看到的介面繫結，即使您今天認為自己用不到它們，您也應該遵循此處提出的這些做法，以防改變主意。使用一次命名空間，並將其放在適當的 Java 套件命名空間中，從長遠來看將使您的程式碼更整潔，並提高 MyBatis 的可用性。

**名稱解析：**為了減少輸入量，MyBatis 對所有具名設定元素（包括陳述式、結果映射、快取等）使用以下名稱解析規則。

- 完整限定名稱（例如「com.mypackage.MyMapper.selectAllThings」）會被直接尋找，若找到則直接使用。
- 短名稱（例如「selectAllThings」）可用於引用任何無歧義的條目。但是，如果有兩個或多個條目具有相同名稱（例如「com.foo.selectAllThings」和「com.bar.selectAllThings」），則您會收到一個錯誤，報告該短名稱具有歧義，因此必須使用完整限定名稱。

------

像 BlogMapper 這樣的 Mapper 類別還有一個訣竅。它們的映射陳述式根本不需要使用 XML 進行映射。相反地，它們可以使用 Java 註解。例如，上述的 XML 可以被消除並替換為：

```java
package org.mybatis.example;
public interface BlogMapper {
  @Select("SELECT * FROM blog WHERE id = #{id}")
  Blog selectBlog(int id);
}
```

對於簡單的陳述式，註解要簡潔得多；然而，對於更複雜的陳述式，Java 註解既有限制又顯得雜亂。因此，如果您必須處理複雜的陳述式，最好使用 XML 映射陳述式。

這將由您和您的專案團隊決定哪種方式適合您，以及以一致的方式定義映射陳述式對您來說有多重要。話雖如此，您永遠不會被局限於單一做法。您可以非常輕鬆地將以註解為基礎的映射陳述式遷移至 XML，反之亦然。

### 作用域（Scope）與生命週期

理解我們到目前為止討論過的各個類別之作用域與生命週期非常重要。不正確地使用它們可能會導致嚴重的並行問題（Concurrency problems）。

------

<span class="label important">NOTE</span> **物件生命週期與相依性注入框架**

相依性注入（Dependency Injection）框架可以建立執行緒安全、具交易性的 SqlSession 與 Mapper，並將它們直接注入到您的 Bean 中，這樣您就可以完全不用擔心它們的生命週期。您可能想參考 MyBatis-Spring 或 MyBatis-Guice 子專案，以進一步了解如何將 MyBatis 與 DI 框架結合使用。

------

#### SqlSessionFactoryBuilder

這個類別可以被實例化、使用後丟棄。一旦建立了 SqlSessionFactory，就沒有必要保留它。因此，SqlSessionFactoryBuilder 實例的最佳作用域是方法作用域（即區域方法變數）。您可以重複使用 SqlSessionFactoryBuilder 來建構多個 SqlSessionFactory 實例，但最好還是不要保留它，以確保所有 XML 解析資源都能被釋放給更重要的事情。

#### SqlSessionFactory

一旦建立，SqlSessionFactory 應該在您的應用程式執行期間一直存在。幾乎沒有任何理由將其廢棄或重新建立。在應用程式執行期間不重複建構 SqlSessionFactory 是一項最佳實務。重複建構應被視為一種「程式碼異味（Bad smell）」。因此，SqlSessionFactory 的最佳作用域是應用程式作用域（Application scope）。這可以透過多種方式實現。最簡單的方法是使用單例模式（Singleton pattern）或靜態單例模式。

#### SqlSession

每個執行緒都應該擁有自己的 SqlSession 實例。SqlSession 實例不可共用，且不是執行緒安全的。因此最佳作用域是請求（Request）或方法（Method）作用域。絕對不要將 SqlSession 實例的參照保留在類別的靜態欄位甚至是實例欄位中。絕對不要將 SqlSession 的參照保留在任何形式的受控作用域中，例如 Servlet 框架的 HttpSession。如果您正在使用任何 Web 框架，請將 SqlSession 視為遵循與 HTTP 請求類似的作用域。換句話說，在收到 HTTP 請求時，您可以開啟一個 SqlSession，然後在回傳回應時將其關閉。關閉 session 非常重要。您應該始終確保在 finally 區塊內將其關閉。以下是確保關閉 SqlSession 的標準模式：

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  // 執行工作
}
```

在您的程式碼中始終如一地使用此模式，將確保所有資料庫資源都能正確關閉。

#### Mapper 實例

Mapper 是您建立用來繫結至已映射陳述式的介面。Mapper 介面的實例是從 SqlSession 取得的。因此，就技術上而言，任何 Mapper 實例的最寬作用域與請求它們的 SqlSession 相同。然而，Mapper 實例的最佳作用域是方法作用域。也就是說，它們應該在被使用的方法內請求，然後丟棄。它們不需要明確關閉。雖然在整個請求期間保留它們並不是問題（類似於 SqlSession），但您可能會發現，在此層級管理過多資源會很快失控。保持簡單，將 Mapper 保留在方法作用域內。以下範例展示了此最佳做法：

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  BlogMapper mapper = session.getMapper(BlogMapper.class);
  // 執行工作
}
```
