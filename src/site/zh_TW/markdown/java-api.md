title: MyBatis 3 | Java API
author: Clinton Begin

<h1 class="d-none">Avoid blank site</h1>

## Java API

既然您已經知道如何設定 MyBatis 並建立映射，現在您可以享受成果了。MyBatis Java API 是您收穫努力成果的地方。正如您將看到的，相較於 JDBC，MyBatis 大幅簡化了您的程式碼，並使其保持簡潔、易於理解與維護。MyBatis 3 引入了許多重大改進，使處理 SQL Maps 變得更加出色。

<a id="directory-structure"></a>
### 目錄結構 (Directory Structure)

在我們深入探討 Java API 本身之前，了解目錄結構的最佳實踐至關重要。MyBatis 非常有彈性，您幾乎可以對檔案進行任何安排。但如同任何框架一樣，它也有推薦的偏好方式。

讓我們看看典型的應用程式目錄結構：

```
/my_application
  /bin
  /devlib
  /lib                <-- MyBatis *.jar 檔案放置於此。
  /src
    /org/myapp/
      /action
      /data           <-- MyBatis 成品放置於此，包含 Mapper 類別、XML 設定檔、XML 映射檔。
        /mybatis-config.xml
        /BlogMapper.java
        /BlogMapper.xml
      /model
      /service
      /view
    /properties       <-- XML 設定檔中包含的屬性檔放置於此。
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
      /web.xml
```

請記住，這些只是偏好設定而非硬性要求，但使用通用的目錄結構會讓團隊中的其他成員心存感激。

本節其餘的範例都將假定您遵循此目錄結構。

<a id="sqlsessions"></a>
### SqlSessions

使用 MyBatis 的主要 Java 介面是 SqlSession。透過此介面，您可以執行命令、取得映射器（Mapper）以及管理交易（Transaction）。我們稍後會詳細討論 SqlSession 本身，但首先我們必須學習如何取得 SqlSession 的實例。SqlSession 是由 SqlSessionFactory 實例所建立的。SqlSessionFactory 包含以各種不同方式建立 SqlSession 實例的方法。而 SqlSessionFactory 本身則是由 SqlSessionFactoryBuilder 所建立，後者可以從 XML、註解或手寫 Java 設定中建立 SqlSessionFactory。

<span class="label important">NOTE</span> 當在 Spring 或 Guice 等相依性注入（Dependency Injection, DI）框架中使用 MyBatis 時，SqlSession 是由 DI 框架建立並注入的，因此您不需要使用 SqlSessionFactoryBuilder 或 SqlSessionFactory，可以直接跳至 SqlSession 章節。更多資訊請參閱 MyBatis-Spring 或 MyBatis-Guice 手冊。

<a id="sqlsessionfactorybuilder"></a>
#### SqlSessionFactoryBuilder

SqlSessionFactoryBuilder 具有五個 build() 方法，每個方法都允許您從不同的來源建構 SqlSessionFactory。

```java
SqlSessionFactory build(InputStream inputStream)
SqlSessionFactory build(InputStream inputStream, String environment)
SqlSessionFactory build(InputStream inputStream, Properties properties)
SqlSessionFactory build(InputStream inputStream, String env, Properties props)
SqlSessionFactory build(Configuration config)
```

前四個方法是最常用的，因為它們接受參照 XML 文件的 InputStream 實例，更具體地說是前面討論過的 mybatis-config.xml 檔案。選填參數為 environment 與 properties。Environment 決定要載入哪個環境，包含資料來源（DataSource）與交易管理員（TransactionManager）。例如：

```xml
<environments default="development">
  <environment id="development">
    <transactionManager type="JDBC">
        ...
    <dataSource type="POOLED">
        ...
  </environment>
  <environment id="production">
    <transactionManager type="MANAGED">
        ...
    <dataSource type="JNDI">
        ...
  </environment>
</environments>
```

如果您呼叫接受 environment 參數的 build 方法，則 MyBatis 將使用該環境的設定。當然，如果您指定了無效的環境，將會收到錯誤。如果您呼叫不接受 environment 參數的 build 方法之一，則會使用預設環境（在上述範例中指定為 default="development"）。

如果您呼叫接受 properties 實例的方法，則 MyBatis 將載入這些屬性並使其在您的設定中可用。這些屬性可以使用語法 `${propName}` 取代設定中的大多數值。

回想一下，屬性也可以從 mybatis-config.xml 檔案中參照，或者直接在其中指定。因此，了解優先循序非常重要。我們之前在本文件中提到過這一點，但為了方便參照，在此再次列出：

------

如果屬性存在於上述多個位置，MyBatis 會依循以下循序載入它們：

- 首先讀取 properties 元素主體中指定的屬性，
- 其次讀取從 properties 元素的類別路徑資源（classpath resource）或 url 屬性載入的屬性，並覆寫已存在的任何重複屬性，
- 最後讀取作為方法參數傳遞的屬性，並覆寫可能已從 properties 主體和 resource/url 屬性載入的任何重複屬性。

因此，最高優先權的屬性是作為方法參數傳入的屬性，其次是 resource/url 屬性，最後是在 properties 元素主體中指定的屬性。

------

總結來說，前四個方法大致相同，但透過多載允許您選擇性地指定環境與／或屬性。以下是從 mybatis-config.xml 檔案建構 SqlSessionFactory 的範例：

```java
String resource = "org/mybatis/builder/mybatis-config.xml";
InputStream inputStream = Resources.getResourceAsStream(resource);
SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
SqlSessionFactory factory = builder.build(inputStream);
```

請注意，我們使用了 Resources 工具類別，該類別位於 org.apache.ibatis.io 套件中。顧名思義，Resources 類別可協助您從類別路徑、檔案系統甚至是 Web URL 載入資源。快速瀏覽該類別的原始碼或透過 IDE 檢查，將會發現其相當實用且顯而易見的方法集。以下是簡要清單：

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

最後一個 build 方法接受 Configuration 的實例。Configuration 類別包含您可能需要了解的有關 SqlSessionFactory 實例的所有資訊。Configuration 類別對於自省（introspecting）設定非常有用，包含尋找與操作 SQL 映射（一旦應用程式開始接受請求，則不建議進行操作）。Configuration 類別具備您已經了解的所有設定參數，只是將它們公開為 Java API。以下是一個手動建立 Configuration 實例並將其傳遞給 build() 方法以建立 SqlSessionFactory 的簡單範例：

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

現在您擁有了一個可用於建立 SqlSession 實例的 SqlSessionFactory。

<a id="sqlsessionmanager"></a>
#### SqlSessionManager

自 3.4.0+ 版本起，MyBatis 提供了 `SqlSessionManager`，它同時實作了 `SqlSessionFactory` 與 `SqlSession` 介面。與每次呼叫 `openSession()` 都會建立新 `SqlSession` 的 `DefaultSqlSessionFactory` 不同，`SqlSessionManager` 使用 `ThreadLocal` 來管理每個執行緒的工作階段。這提供了一種在多執行緒環境中管理 `SqlSession` 生命週期的便捷方式，而無需外部相依性注入框架。

<a id="creating-a-sqlsessionmanager"></a>
##### 建立 SqlSessionManager (Creating a SqlSessionManager)

您可以透過兩種方式建立 `SqlSessionManager`：

1. 使用接受 MyBatis XML 設定檔的靜態 `newInstance()` 方法之一：

```java
SqlSessionManager manager = SqlSessionManager.newInstance(inputStream);
```

或指定環境或屬性：

```java
SqlSessionManager manager = SqlSessionManager.newInstance(inputStream, environment);

// ... 或 ...

SqlSessionManager manager = SqlSessionManager.newInstance(inputStream, properties);
```

2. 包裝現有的 `SqlSessionFactory` 實例：

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(inputStream);
SqlSessionManager manager = SqlSessionManager.newInstance(factory);
```

<a id="managed-sessions"></a>
##### 受控工作階段 (Managed Sessions)

`SqlSessionManager` 的主要特性是能夠按執行緒管理工作階段。一旦啟動受控工作階段，所有透過 `SqlSessionManager` 代理對 `SqlSession` 方法的呼叫都將使用該執行緒區域（thread-local）工作階段。

啟動受控工作階段：

```java
manager.startManagedSession();
```

或指定具體選項：

```java
// 啟用自動提交 (auto-commit)
manager.startManagedSession(true);

// 指定特定執行器型別
manager.startManagedSession(ExecutorType.BATCH);

// 指定特定隔離層級
manager.startManagedSession(TransactionIsolationLevel.READ_COMMITTED);

// 指定特定連線
manager.startManagedSession(connection);
```

檢查目前執行緒是否已啟動受控工作階段：

```java
boolean isStarted = manager.isManagedSessionStarted();
```

<a id="using-the-sqlsessionmanager"></a>
##### 使用 SqlSessionManager (Using the SqlSessionManager)

當受控工作階段啟動時，呼叫 `commit()`、`rollback()`、`close()` 或任何資料存取方法（`selectOne`、`selectList`、`insert`、`update`、`delete` 等）都將對該執行緒區域工作階段進行操作。工作階段結束後，您必須確保呼叫 `close()` 以清理執行緒區域儲存區。因為它使用 ThreadLocal，在連線池化的 Web 伺服器環境中（如 Tomcat 或 Jetty），若未呼叫 `close()` 將使 SQL 工作階段洩漏至執行緒上，當該執行緒被重複用於不同的 HTTP 請求時，會導致無法預測的交易行為或連線洩漏：

```java
manager.startManagedSession();
try {
    // ... 使用工作階段 ...
    manager.commit();
} finally {
    manager.close();
}
```

若尚未啟動任何受控工作階段，透過內部代理呼叫資料存取方法（如 `selectOne`、`selectList`、`insert`、`update`、`delete`）將自動開啟一個短暫的工作階段，執行該單一方法、明確提交（commit）該暫時工作階段，並在 `finally` 區塊中立即將其關閉；而直接呼叫工作階段生命週期與交易控制方法（如 `commit()`、`rollback()`、`close()`）在未啟動受控工作階段時則會拋出 `SqlSessionException`。

<a id="difference-from-defaultsqlsessionfactory"></a>
##### 與 DefaultSqlSessionFactory 的差異 (Difference from DefaultSqlSessionFactory)

| 功能 | DefaultSqlSessionFactory | SqlSessionManager |
|---|---|---|
| 工作階段建立 | 每次 `openSession()` 呼叫皆建立新工作階段 | 可透過 `startManagedSession()` 管理執行緒區域工作階段 |
| 執行緒安全 | 需要外部管理 | 內建執行緒區域工作階段管理 |
| 自動工作階段生命週期 | 否（需手動關閉） | 是，當未啟動受控工作階段時 |
| DI 框架整合 | 需要特定 DI 整合（例如 Spring 中的 `SqlSessionTemplate`） | 可在非 DI 環境中取代 `SqlSessionFactory` 與 `SqlSession` |

`SqlSessionManager` 在不使用相依性注入框架但仍需要按執行緒適當管理工作階段的環境中特別有用。如果您使用的是 Spring 或 Guice，您應繼續使用 `MyBatis-Spring` 或 `MyBatis-Guice` 進行工作階段管理。

<a id="sqlsessionfactory"></a>
#### SqlSessionFactory

SqlSessionFactory 具有八個用於建立 SqlSession 實例的方法。一般而言，在選擇這些方法之一時，您需要做出的決策包括：

- **交易（Transaction）**：您是否希望為工作階段使用交易範圍，或者使用自動提交（在大多數資料庫與／或 JDBC 驅動程式中通常意味著沒有交易）？
- **連線（Connection）**：您希望 MyBatis 為您從已設定的 DataSource 獲取連線，還是希望提供您自己的連線？
- **執行（Execution）**：您希望 MyBatis 重複使用 PreparedStatement 與／或批次處理更新（包含 insert 與 delete）嗎？

這組多載的 openSession() 方法簽章允許您選擇這些選項的任何合適組合。

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

不帶參數的預設 openSession() 方法將建立具有以下特性的 SqlSession：

- <u>將會</u>啟動交易範圍（即非自動提交）。
- 將從使用中環境所設定的 DataSource 實例獲取 `Connection` 物件。
- 交易隔離層級將採用驅動程式或資料來源所使用的預設值。
- 不會重複使用 PreparedStatement，也不會批次執行更新。

大多數方法都相當不言自明。若要啟用自動提交，請將 `true` 傳遞給選填的 `autoCommit` 參數。若要提供您自己的連線，請將 `Connection` 實例傳遞給 `connection` 參數。請注意，沒有同時設定 `Connection` 和 `autoCommit` 的多載方法，因為 MyBatis 將使用提供的連線物件目前所採用的任何設定。MyBatis 為交易隔離層級使用了一個名為 `TransactionIsolationLevel` 的 Java 列舉包裝函式，但它們的運作方式正如預期，並具備 JDBC 支援的 5 個層級（`NONE`、`READ_UNCOMMITTED`、`READ_COMMITTED`、`REPEATABLE_READ`、`SERIALIZABLE`）。

您可能不太熟悉的一個參數是 `ExecutorType`。此列舉定義了 3 個值：

- `ExecutorType.SIMPLE`：此種型別的執行器沒有特殊行為。它為每次陳述式的執行建立一個新的 PreparedStatement。
- `ExecutorType.REUSE`：此種型別的執行器將重複使用 PreparedStatement。
- `ExecutorType.BATCH`：此執行器將批次處理所有更新陳述式，並在它們之間執行 SELECT 時視需要劃分界限，以確保行為易於理解。

<span class="label important">NOTE</span> SqlSessionFactory 上還有一個我們尚未提及的方法，那就是 *getConfiguration()*。此方法將回傳一個 Configuration 實例，您可以在執行時期使用它來自省 MyBatis 設定。

<span class="label important">NOTE</span> 如果您使用過舊版 MyBatis，您會記得工作階段、交易與批次是各自獨立的概念。現在不再如此。三者都整齊地包含在單一工作階段的範圍內。您無需單獨處理交易或批次即可獲得它們的完整好處。

<a id="sqlsession"></a>
#### SqlSession

如前所述，SqlSession 實例是 MyBatis 中最強大的類別。在這裡您可以找到執行陳述式、認可或復原交易以及取得映射器實例的所有方法。

SqlSession 類別有二十多個方法，因此讓我們將它們劃分為更容易理解的群組。

<a id="statement-execution-methods"></a>
##### 陳述式執行方法 (Statement Execution Methods)

這些方法用於執行在 SQL 映射 XML 檔中定義的 SELECT、INSERT、UPDATE 與 DELETE 陳述式。它們相當淺顯易懂，每個方法都接受陳述式 ID 與參數物件（可以是基本型別、自動裝箱型別或包裝類別、JavaBean、POJO 或 Map）。

```java
<T> T selectOne(String statement, Object parameter)
<E> List<E> selectList(String statement, Object parameter)
<T> Cursor<T> selectCursor(String statement, Object parameter)
<K,V> Map<K,V> selectMap(String statement, Object parameter, String mapKey)
int insert(String statement, Object parameter)
int update(String statement, Object parameter)
int delete(String statement, Object parameter)
```

`selectOne` 與 `selectList` 之間的唯一差異在於 `selectOne` 必須剛好回傳一個物件或 `null`（無）。若回傳超過一個物件，將會拋出例外。如果您不知道預期有多少個物件，請使用 `selectList`。如果您想檢查物件是否存在，回傳計數（0 或 1）會更好。`selectMap` 是一個特殊案例，其設計目的是根據結果物件中的某個屬性，將結果清單轉換為 `Map`。因為並非所有陳述式都需要參數，所以這些方法提供了不需要參數物件的多載版本。

`insert`、`update` 與 `delete` 方法回傳的值表示陳述式所影響的資料列數。

```java
<T> T selectOne(String statement)
<E> List<E> selectList(String statement)
<T> Cursor<T> selectCursor(String statement)
<K,V> Map<K,V> selectMap(String statement, String mapKey)
int insert(String statement)
int update(String statement)
int delete(String statement)
```

`Cursor` 提供與 List 相同的結果，只不過它使用 `Iterator` 延遲載入資料。

```java
try (Cursor<MyEntity> entities = session.selectCursor(statement, param)) {
   for (MyEntity entity : entities) {
      // 處理單一實體
   }
}
```

最後，還有五個進階版本的 `select` 方法，允許您限制回傳的資料列範圍，或提供自訂的結果處理邏輯，通常用於非常龐大的資料集。

```java
<E> List<E> selectList (String statement, Object parameter, RowBounds rowBounds)
<T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds)
<K,V> Map<K,V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowbounds)
void select (String statement, Object parameter, ResultHandler<T> handler)
void select (String statement, Object parameter, RowBounds rowBounds, ResultHandler<T> handler)
```

`RowBounds` 參數會使 MyBatis 跳過指定數量的記錄，並將回傳的結果數量限制為特定數目。`RowBounds` 類別具有同時接受 `offset` 與 `limit` 的建構函式，除此之外是不可變的。

```java
int offset = 100;
int limit = 25;
RowBounds rowBounds = new RowBounds(offset, limit);
```

不同的驅動程式在這方面能達到的效率層級不同。為了獲得最佳效能，請使用 `SCROLL_SENSITIVE` 或 `SCROLL_INSENSITIVE` 的結果集型別（換言之：非 `FORWARD_ONLY`）。

`ResultHandler` 參數允許您隨心所欲地處理每一列。您可以將其新增至 `List`、建立 `Map`、`Set`，或者捨棄每個結果而僅保留計算的彙整總額。您幾乎可以使用 `ResultHandler` 執行任何操作，而這正是 MyBatis 內部用來建立結果集清單的機制。

自 3.4.6 起，傳遞給 `CALLABLE` 陳述式的 `ResultHandler` 將套用於預存程序中所有的 `REFCURSOR` 輸出參數（如果存在的話）。

該介面非常簡單：

```java
package org.apache.ibatis.session;
public interface ResultHandler<T> {
  void handleResult(ResultContext<? extends T> context);
}
```

`ResultContext` 參數讓您可以存取結果物件本身、已建立結果物件數量的計數，以及可用於阻止 MyBatis 載入更多結果的 `stop()` 方法。

使用 `ResultHandler` 有兩個您應該注意的限制：

- 從使用 `ResultHandler` 呼叫的方法中獲取的資料將不會被快取。
- 當使用進階 `resultMap` 時，MyBatis 可能需要多個列才能建立一個物件。如果使用了 `ResultHandler`，您可能會收到一個其關聯或集合尚未填滿的物件。

<a id="batch-update-statement-flush-method"></a>
##### 批次更新陳述式清除方法 (Batch update statement Flush Method)

有一個方法可在任何時候清除（執行）儲存在 JDBC 驅動程式類別中的批次更新陳述式。當 `ExecutorType` 為 `ExecutorType.BATCH` 時可以使用此方法。

```java
List<BatchResult> flushStatements()
```

<a id="transaction-control-methods"></a>
##### 交易控制方法 (Transaction Control Methods)

有四個方法可用於控制交易的範圍。當然，如果您選擇使用自動認可，或者使用外部交易管理員，這些方法將沒有任何效果。但是，如果您使用由 `Connection` 實例管理的 JDBC 交易管理員，那麼以下四個方法將會派上用場：

```java
void commit()
void commit(boolean force)
void rollback()
void rollback(boolean force)
```

預設情況下，除非 MyBatis 偵測到資料庫因呼叫 `insert`、`update`、`delete` 或啟用了 `affectData` 的 `select` 而發生變更，否則不會實際提交。如果您在未呼叫這些方法的情況下以某種方式進行了變更，則可以將 `true` 傳入 `commit` 與 `rollback` 方法以確保它們將被認可（請注意，您仍然無法強制處於自動提交模式的工作階段或使用外部交易管理員的工作階段進行提交）。大多數時候您不必呼叫 `rollback()`，因為如果您沒有呼叫 commit，MyBatis 會替您進行復原。但是，如果您需要對可能進行多次 commit 和 rollback 的工作階段進行更細緻的控制，您可以使用 rollback 選項來實現這一點。

<span class="label important">NOTE</span> MyBatis-Spring 與 MyBatis-Guice 提供宣告式交易處理。因此，如果您將 MyBatis 與 Spring 或 Guice 一起使用，請參閱其專屬手冊。

<a id="local-cache"></a>
##### 本地快取 (Local Cache)

MyBatis 使用兩種快取：本地快取與二級快取。

每次建立新工作階段時，MyBatis 都會建立本地快取並將其附加至該工作階段。在工作階段內執行的任何查詢都將儲存在本地快取中，因此具有相同輸入參數的相同查詢的進一步執行將不會叫用資料庫。本地快取會在 `update`、`commit`、`rollback` 與 `close` 時被清除。

預設情況下，本地快取資料會用於整個工作階段期間。需要此快取來解析循環參照並加速重複的巢狀查詢，因此它永遠無法完全停用，但您可以透過設定 `localCacheScope=STATEMENT`，將本地快取設定為僅在陳述式執行期間使用。

請注意，當 `localCacheScope` 設為 `SESSION` 時，MyBatis 會回傳儲存在本地快取中之相同物件的參照。對回傳物件（清單等）的任何修改都會影響本地快取內容，進而影響在工作階段生命週期中從快取回傳的值。因此，作為最佳實踐，切勿修改 MyBatis 回傳的物件。

您可以在任何時候透過呼叫以下方法清除本地快取：

```java
void clearCache()
```

<a id="ensuring-that-sqlsession-is-closed"></a>
##### 確保關閉 SqlSession (Ensuring that SqlSession is Closed)

```java
void close()
```

您必須確保的最重要事情就是關閉所開啟的任何工作階段。確保這一點的最佳方法是使用以下工作單元（unit of work）模式：

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
    // 以下 3 行為「執行某些工作」的虛擬碼
    session.insert(...);
    session.update(...);
    session.delete(...);
    session.commit();
}
```

<span class="label important">NOTE</span> 就像 `SqlSessionFactory` 一樣，您可以透過呼叫 `getConfiguration()` 方法來取得 `SqlSession` 正在使用的 `Configuration` 實例。

```java
Configuration getConfiguration()
```

<a id="using-mappers"></a>
##### 使用映射器 (Using Mappers)

```java
<T> T getMapper(Class<T> type)
```

雖然上面的各種 `insert`、`update`、`delete` 與 `select` 方法功能強大，但它們也非常冗長、非型別安全，且對您的 IDE 或單元測試的幫助不如預期。我們之前在「新手入門」小節中已經看過使用 `Mapper` 的範例。

因此，執行映射陳述式的更常用方式是使用 `Mapper` 類別。`Mapper` 類別只是一個介面，其方法定義與 `SqlSession` 方法相對應。以下範例類別展示了一些方法簽章以及它們如何映射至 `SqlSession`：

```java
public interface AuthorMapper {
  // (Author) selectOne("selectAuthor", 5);
  Author selectAuthor(int id);

  // (List<Author>) selectList("selectAuthors")
  List<Author> selectAuthors();

  // (Map<Integer,Author>) selectMap("selectAuthorsAsMap", "id")
  @MapKey("id")
  Map<Integer, Author> selectAuthorsAsMap();

  // insert("insertAuthor", author)
  int insertAuthor(Author author);

  // update("updateAuthor", author)
  int updateAuthor(Author author);

  // delete("deleteAuthor", 5)
  int deleteAuthor(int id);
}
```

簡而言之，每個 `Mapper` 方法簽章都應與其關聯的 `SqlSession` 方法相符，但不包含 `String` 參數 ID。相反地，方法名稱必須與映射陳述式 ID 相符。

此外，回傳型別必須與單一結果的預期結果型別相符，或者對於多個結果或 `Cursor`，必須與陣列或集合相符。支援所有常見型別，包括：基本型別、`Map`、POJO 與 `JavaBean`。

<span class="label important">NOTE</span> Mapper 介面不需要實作任何介面或繼承任何類別。只要方法簽章可用於唯一識別對應的映射陳述式即可。

<span class="label important">NOTE</span> Mapper 介面可以繼承其他介面。當使用 XML 繫結至 `Mapper` 介面時，請確保您在適當的命名空間中宣告了陳述式。此外，唯一的限制是您不能在階層架構中的兩個介面中宣告相同的方法簽章（無論如何這都是個壞主意）。

您可以將多個參數傳遞給 mapper 方法。如果您這樣做，預設情況下它們將以文字 "param" 加上其在參數清單中的位置來命名，例如：`#{param1}`、`#{param2}` 等。如果您希望變更參數的名稱（僅限多個參數），您可以在參數上使用 `@Param("paramName")` 註解。

您也可以將 `RowBounds` 實例傳遞給方法以限制查詢結果。

<a id="mapper-annotations"></a>
##### 映射器註解 (Mapper Annotations)

從最初開始，MyBatis 就是一個由 XML 驅動的框架。設定基於 XML，映射陳述式也定義在 XML 中。在 MyBatis 3 中，有了新的選擇。MyBatis 3 建構在全面且強大的 Java 設定 API 之上。此設定 API 是基於 XML 之 MyBatis 設定以及基於註解之設定的基礎。註解提供了一種實作映射陳述式的方法，而無需引入 XML 的額外負擔。

<span class="label important">NOTE</span> Java 註解在表達力與靈活性方面有所限制。因此，複雜結果映射的設定不如 XML 那樣富有表現力。透過結合使用 `@NamedResultMap` 註解，以及 `@Arg`、`@One` 與 `@Many` 依名稱參照其他結果映射的能力，我們相信在 XML 中可以實現的大部分功能也可以透過註解來實現。

**註解如下：**

| 註解 | 目標 | XML 等效項 | 說明 |
|---|---|---|---|
| `@CacheNamespace` | `Class` | `<cache>` | 為給定的命名空間（即類別）設定快取。屬性：`implementation`、`eviction`、`flushInterval`、`size`、`readWrite`、`blocking`、`properties`。 |
| `@Property` | N/A | `<property>` | 指定屬性值或佔位符（可由在 `mybatis-config.xml` 中定義的設定屬性所替換）。屬性：`name`、`value`。（自 MyBatis 3.4.2+ 起可用） |
| `@CacheNamespaceRef` | `Class` | `<cacheRef>` | 參照另一個命名空間的快取以供使用。請注意，在 XML 映射檔中宣告的快取被視為單獨的命名空間，即使它們共享相同的完整類別名稱（FQCN）。屬性：`value` 與 `name`。如果您使用此註解，應指定 `value` 或 `name` 屬性之一。對於 `value` 屬性，請指定表示命名空間的 Java 型別（命名空間名稱成為指定 Java 型別的 FQCN）；對於 `name` 屬性（此屬性自 3.4.2 起可用），請指定表示命名空間的名稱。 |
| `@ConstructorArgs` | `Method` | `<constructor>` | 收集一組結果以傳遞給結果物件的建構函式。屬性：`value`，它是一個 `Arg` 陣列。 |
| `@Arg` | N/A | <ul><li>`<arg>`</li><li>`<idArg>`</li></ul> | 作為 ConstructorArgs 集合一部分的單一建構函式引數。屬性：`id`、`column`、`javaType`、`jdbcType`、`typeHandler`、`select`、`resultMap`。id 屬性是一個布林值，用於識別用於比較的屬性，類似於 `<idArg>` XML 元素。自 3.5.4 起，它可以用作可重複註解。 |
| `@TypeDiscriminator` | `Method` | `<discriminator>` | 一組可用於決定要執行之結果映射的值案例。屬性：`column`、`javaType`、`jdbcType`、`typeHandler`、`cases`。cases 屬性是一個 `Case` 陣列。 |
| `@Case` | N/A | `<case>` | 單一值案例及其對應的映射。屬性：`value`、`type`、`results`。results 屬性是一個 Results 陣列，因此此 `Case` 註解類似於實際的 `ResultMap`，由下方的 `Results` 註解所指定。 |
| `@NamedResultMap` | `Type` | `<resultMap>` | 此註解用於建立不直接綁定至 select 陳述式且可供多個陳述式重複使用的結果映射。這類似於在 XML 中建立 `<resultMap>`。該註解應附加至型別（通常是 mapper 介面）。該註解可以接受 `@Arg` 陣列、與／或 `@Result` 陣列、與／或 `@TypeDiscriminator`。任何數量的 `@NamedResultMap` 註解都可以附加至型別。 |
| `@Results` | `Method` | `<resultMap>` | 結果映射清單，包含特定結果欄位如何映射至屬性或欄位的詳細資料。屬性：`value`、`id`。value 屬性是 `Result` 註解的陣列。id 屬性是結果映射的名稱。 |
| `@Result` | N/A | <ul><li>`<result>`</li><li>`<id>`</li></ul> | 欄位與屬性或欄位之間的單一結果映射。屬性：`id`、`column`、`property`、`javaType`、`jdbcType`、`typeHandler`、`one`、`many`。id 屬性是一個布林值，表示該屬性應用於比較（類似於 XML 映射中的 `<id>`）。one 屬性用於單一關聯，類似於 `<association>`；many 屬性用於集合，類似於 `<collection>`。它們之所以這樣命名是為了避免類別命名衝突。自 3.5.4 起，它可以用作可重複註解。 |
| `@One` | N/A | `<association>` | 映射至複雜型別的單一屬性值。屬性：`select`，即可以載入適當型別實例的映射陳述式（即 mapper 方法）的完整限定名稱。`fetchType`，覆寫此映射的全域設定參數 `lazyLoadingEnabled`。`resultMap`（自 3.5.5 起可用），即從查詢結果映射至單一容器物件的結果映射的完整限定名稱。`columnPrefix`（自 3.5.5 起可用），即在巢狀結果映射中用於分組查詢欄位的欄位前置詞。<span class="label important">NOTE</span> 您會注意到註解 API 不支援聯結映射。這是因為 Java 註解不允許循環參照的限制所致。 |
| `@Many` | N/A | `<collection>` | 映射至複雜型別的集合屬性。屬性：`select`，即可以載入適當型別實例集合的映射陳述式（即 mapper 方法）的完整限定名稱。`fetchType`，覆寫此映射的全域設定參數 `lazyLoadingEnabled`。`resultMap`（自 3.5.5 起可用），即從查詢結果映射至集合物件的結果映射的完整限定名稱。`columnPrefix`（自 3.5.5 起可用），即在巢狀結果映射中用於分組查詢欄位的欄位前置詞。<span class="label important">NOTE</span> 您會注意到註解 API 不支援聯結映射。這是因為 Java 註解不允許循環參照的限制所致。 |
| `@MapKey` | `Method` | | 用於回傳型別為 Map 的方法上。它用於根據結果物件的某個屬性，將結果物件清單轉換為 Map。屬性：`value`，用作 map 鍵的屬性。 |
| `@Options` | `Method` | 映射陳述式的屬性 | 此註解提供了對通常作為屬性存在於映射陳述式上的廣泛切換開關與設定選項的存取。與其使每個陳述式註解複雜化，`Options` 註解提供了一致且清晰的方式來存取這些選項。屬性：`useCache=true`、`flushCache=FlushCachePolicy.DEFAULT`、`resultSetType=DEFAULT`、`statementType=PREPARED`、`fetchSize=-1`、`timeout=-1`、`useGeneratedKeys=false`、`keyProperty=""`、`keyColumn=""`、`resultSets=""` 與 `databaseId=""`。請務必理解，在 Java 註解中無法將 `null` 指定為值。因此，一旦啟用 `Options` 註解，您的陳述式就會受限於所有的預設值。請留意預設值為何，以避免非預期的行為。`databaseId`（自 3.5.5 起可用），若已設定 `DatabaseIdProvider`，MyBatis 將使用沒有 `databaseId` 屬性或其 `databaseId` 與目前資料庫相符的 `Options`。若同時找到帶有與不帶有 `databaseId` 的設定，後者將被捨棄。<br/><br/>請注意，`keyColumn` 僅在某些特定資料庫（如 Oracle 與 PostgreSQL）中才需要。有關這些屬性中允許值的更多資訊，請參見上方關於 insert 陳述式中 `keyColumn` 與 `keyProperty` 的討論。 |
| <ul><li>`@Insert`</li><li>`@Update`</li><li>`@Delete`</li><li>`@Select`</li></ul> | `Method` | <ul><li>`<insert>`</li><li>`<update>`</li><li>`<delete>`</li><li>`<select>`</li></ul> | 這些註解中的每一個都代表要執行的實際 SQL。它們各自接受一個字串陣列（或單一字串即可）。如果傳遞的是字串陣列，它們會以單一空格連接在一起進行分隔。這有助於避免在 Java 程式碼中建構 SQL 時出現「遺漏空格」的問題。但是，如果您願意，也可以自行串接為單一字串。屬性：`value`，構成單一 SQL 陳述式的字串陣列。`databaseId`（自 3.5.5 起可用），若已設定 `DatabaseIdProvider`，MyBatis 將使用沒有 `databaseId` 屬性或其 `databaseId` 與目前資料庫相符的陳述式。若同時找到帶有與不帶有 `databaseId` 的陳述式，後者將被捨棄。 |
| <ul><li>`@InsertProvider`</li><li>`@UpdateProvider`</li><li>`@DeleteProvider`</li><li>`@SelectProvider`</li></ul> | `Method` | <ul><li>`<insert>`</li><li>`<update>`</li><li>`<delete>`</li><li>`<select>`</li></ul> | 允許建立動態 SQL。這些替代的 SQL 註解允許您指定在執行時期回傳要執行之 SQL 的類別與方法名稱（自 3.4.6 起，您可以指定 `CharSequence` 代替 `String` 作為方法回傳型別）。在執行映射陳述式時，MyBatis 將實例化該類別並執行該方法（由提供者指定）。您可以透過 `ProviderContext`（自 MyBatis 3.4.5 或更高版本起可用）將傳遞給 mapper 方法的引數、「Mapper 介面型別」、「Mapper 方法」與「資料庫 ID」作為方法引數傳遞。（在 MyBatis 3.4 或更高版本中，允許傳入多個參數）。屬性：`value`、`type`、`method` 與 `databaseId`。`value` 與 `type` 屬性為類別（`type` 屬性是 `value` 的別名，您必須指定其中之一。但當指定全域設定的 `defaultSqlProviderType` 時，這兩個屬性都可以省略）。`method` 是該類別上的方法名稱（自 3.5.1 起，您可以省略 `method` 屬性，MyBatis 將透過 `ProviderMethodResolver` 介面解析目標方法。若未解析出，MyBatis 將使用名為 `provideSql` 的保留後備方法）。`databaseId`（自 3.5.5 起可用），若已設定 `DatabaseIdProvider`，MyBatis 將使用沒有 `databaseId` 屬性或其 `databaseId` 與目前資料庫相符的提供者方法。若同時找到帶有與不帶有 `databaseId` 的提供者方法，後者將被捨棄。<span class="label important">NOTE</span> 接續本節的是關於該類別的討論，該類別有助於以更簡潔、更易讀的方式建構動態 SQL。 |
| `@Param` | `Parameter` | N/A | 若您的 mapper 方法接受多個參數，此註解可套用於 mapper 方法參數以指定其各自的名稱。否則，多個參數將根據其位置以 "param" 為前置詞進行命名（不包含任何 `RowBounds` 參數）。例如，`#{param1}`、`#{param2}` 等為預設值。使用 `@Param("person")`，參數將命名為 `#{person}`。 |
| `@SelectKey` | `Method` | `<selectKey>` | 此註解為以 `@Insert`、`@InsertProvider`、`@Update` 或 `@UpdateProvider` 註解的方法重現了 `<selectKey>` 的功能。對於其他方法它將被忽略。如果您指定了 `@SelectKey` 註解，則 MyBatis 將忽略透過 `@Options` 註解或設定屬性所設定的任何產生鍵屬性。屬性：`statement` 為要執行的 SQL 陳述式字串陣列，`keyProperty` 為將以新值更新的參數物件屬性，`before` 必須為 `true` 或 `false` 以表示 SQL 陳述式應在 insert 之前還是之後執行，`resultType` 為 `keyProperty` 的 Java 型別，`statementType` 為陳述式的型別（`STATEMENT`、`PREPARED` 或 `CALLABLE` 之一，分別映射至 `Statement`、`PreparedStatement` 與 `CallableStatement`，預設值為 `PREPARED`）。`databaseId`（自 3.5.5 起可用），若已設定 `DatabaseIdProvider`，MyBatis 將使用沒有 `databaseId` 屬性或其 `databaseId` 與目前資料庫相符的陳述式。若同時找到帶有與不帶有 `databaseId` 的陳述式，後者將被捨棄。 |
| `@ResultMap` | `Method` | N/A | 此註解用於向 `@Select` 或 `@SelectProvider` 註解提供 XML 映射器中 `<resultMap>` 元素的 id，或 `@NamedResultMap` 或 `@Results` 的 id。這允許帶有註解的 select 重複使用在 XML 中定義的 resultMap。如果在帶有註解的 select 上同時指定了此註解與 `@Results` 或 `@ConstructorArgs` 註解，則此註解將覆寫它們。 |
| `@ResultOrdered` | `Method` | select XML 的 "resultOrdered" 屬性 | 此註解用於表示結果集已知為有序的。當建構在其建構函式中包含集合的物件時需要此註解。 |
| `@ResultType` | `Method` | N/A | 此註解在使用結果處理器時使用。在這種情況下，回傳型別為 void，因此 MyBatis 必須有某種方法來決定為每列建構之物件的型別。如果有 XML 結果映射，請使用 @ResultMap 註解。如果在 XML 的 `<select>` 元素上指定了結果型別，則不需要其他註解。在其他情況下，請使用此註解。例如，如果帶有 @Select 註解的方法將使用結果處理器，則回傳型別必須為 void，且需要此註解（或 @ResultMap）。除非方法回傳型別為 void，否則將忽略此註解。 |
| `@Flush` | `Method` | N/A | 若使用此註解，可以透過在 Mapper 介面定義的方法呼叫 `SqlSession#flushStatements()`。（MyBatis 3.3 或更高版本） |

<a id="mapper-annotation-examples"></a>
##### 映射器註解範例 (Mapper Annotation Examples)

此範例展示了在 insert 之前使用 @SelectKey 註解從序列中擷取值：

```java
@Insert("insert into table3 (id, name) values(#{nameId}, #{name})")
@SelectKey(statement="call next value for TestSequence", keyProperty="nameId", before=true, resultType=int.class)
int insertTable3(Name name);
```

此範例展示了在 insert 之後使用 @SelectKey 註解擷取識別值：

```java
@Insert("insert into table2 (name) values(#{name})")
@SelectKey(statement="call identity()", keyProperty="nameId", before=false, resultType=int.class)
int insertTable2(Name name);
```

此範例展示了使用 `@Flush` 註解呼叫 `SqlSession#flushStatements()`：

```java
@Flush
List<BatchResult> flush();
```

這些範例展示了如何透過指定 @Results 註解的 id 屬性來命名 ResultMap。

```java
@Results(id = "userResult", value = {
  @Result(property = "id", column = "uid", id = true),
  @Result(property = "firstName", column = "first_name"),
  @Result(property = "lastName", column = "last_name")
  })
@Select("select * from users where id = #{id}")
User getUserById(Integer id);

@Results(id = "companyResults")
@ConstructorArgs({
  @Arg(column = "cid", javaType = Integer.class, id = true),
  @Arg(column = "name", javaType = String.class)
})
@Select("select * from company where id = #{id}")
Company getCompanyById(Integer id);
```

此範例展示了使用 SelectProvider 註解的單一參數：

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

此範例展示了使用 Sql Provider 註解的多個參數：

```java
@SelectProvider(type = UserSqlBuilder.class, method = "buildGetUsersByName")
List<User> getUsersByName(
    @Param("name") String name, @Param("orderByColumn") String orderByColumn);

class UserSqlBuilder {

  // 若未使用 @Param，您應該定義與 mapper 方法相同的引數
  public static String buildGetUsersByName(
      final String name, final String orderByColumn) {
    return new SQL(){{
      SELECT("*");
      FROM("users");
      WHERE("name like #{name} || '%'");
      ORDER_BY(orderByColumn);
    }}.toString();
  }

  // 若使用 @Param，您可以僅定義要使用的引數
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

此範例展示了使用全域設定將一個 SQL 提供者類別共享至所有 mapper 方法的用法（自 3.5.6 起可用）：

```java
Configuration configuration = new Configuration();
configuration.setDefaultSqlProviderType(TemplateFilePathProvider.class); // 指定要共享給所有 mapper 方法的 SQL 提供者類別
// ...
```

```java
// 可以省略 SQL 提供者註解上的 type/value 屬性
// 若省略它，MyBatis 將套用在 defaultSqlProviderType 上指定的類別。
public interface UserMapper {

  @SelectProvider // 等同於 @SelectProvider(TemplateFilePathProvider.class)
  User findUser(int id);

  @InsertProvider // 等同於 @InsertProvider(TemplateFilePathProvider.class)
  void createUser(User user);

  @UpdateProvider // 等同於 @UpdateProvider(TemplateFilePathProvider.class)
  void updateUser(User user);

  @DeleteProvider // 等同於 @DeleteProvider(TemplateFilePathProvider.class)
  void deleteUser(int id);
}
```

此範例展示了使用 `ProviderMethodResolver` 預設實作的用法（自 MyBatis 3.5.1 或更高版本起可用）：

```java
@SelectProvider(UserSqlProvider.class)
List<User> getUsersByName(String name);

// 在您的提供者類別上實作 ProviderMethodResolver
class UserSqlProvider implements ProviderMethodResolver {
  // 在預設實作中，它將解析方法名稱與 mapper 方法名稱相符的方法
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

此範例展示了在陳述式註解上使用 `databaseId` 屬性的用法（自 3.5.5 起可用）：

```java
@Select(value = "SELECT SYS_GUID() FROM dual", databaseId = "oracle") // 若 DatabaseIdProvider 提供 "oracle" 則使用此陳述式
@Select(value = "SELECT uuid_generate_v4()", databaseId = "postgres") // 若 DatabaseIdProvider 提供 "postgres" 則使用此陳述式
@Select("SELECT RANDOM_UUID()") // 若未設定 DatabaseIdProvider 或未比對到 databaseId 則使用此陳述式
String generateId();
```
