title: MyBatis 3 | Java API
author: Clinton Begin, Iwao AVE!

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

<!--
Previously outside <body> in the XDoc source; retained without rendering.
The <code>databaseId</code>(Available since 3.5.5), in case there is a configured <code>DatabaseIdProvider</code>,
  the MyBatis use the <code>Options</code> with no <code>databaseId</code> attribute or with a <code>databaseId</code>
  that matches the current one. If found with and without the <code>databaseId</code> the latter will be discarded.<br></br><br></br>
-->

# Java API

<a id="javaApi"></a>

前章までで、設定方法と SQL マップの書き方は学びました。MyBatis Java API は、今までの努力が実を結ぶところです。この章を読めば、JDBC と比べると、MyBatis によってコードが大幅に単純化され、クリーンで理解しやすくメンテナンスも容易な状態を保つことができるということが分かると思います。

## ディレクトリ構造

<a id="directoryStructure"></a>

Java API の説明に入る前に、ディレクトリ構造に関するベストプラクティスを理解しておくことが重要です。MyBatis は柔軟で、基本的にファイルは好きな場所に配置することができますが、多くのフレームワークと同様に推奨される構成があります。

典型的なアプリケーションのディレクトリ構造を見てみましょう。

<pre>/my_application
  /bin
  /devlib
  <strong>/lib                <code>&lt;-- MyBatis *.jar ファイルはここに配置します。</code></strong>
  /src
    /org/myapp/
      /action
      <strong>/data           <code>&lt;-- MyBatis 関連のファイル（Mapper クラス、XML 設定ファイル、XML マッピングファイル）はここに配置します。</code></strong>
        /mybatis-config.xml
        /BlogMapper.java
        /BlogMapper.xml
      /model
      /service
      /view
    <strong>/properties       <code>&lt;-- 設定ファイルで読み込む properties ファイルはここに配置します。</code></strong>
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

これは推奨されるディレクトリ構造であって必須ではありませんが、一般的なディレクトリ構造を使っておけば他の開発者からは感謝されるはずです。

尚、この章のサンプルでは、上記のディレクトリ構造を前提にしています。

## SqlSessions

<a id="sqlSessions"></a>

MyBatis の最も良く使う Java インターフェイスは SqlSession です。コマンドの実行、Mapper の取得、トランザクション管理はこのインターフェイスを通して行うことができます。SqlSession については、後で詳しく説明しますが、その前に SqlSession のインスタンスを取得する方法について学ばなくてはなりません。SqlSession は SqlSessionFactory のインスタンスによって作成されます。SqlSessionFactory には、様々な方法で SqlSession を作成するメソッドが含まれています。SqlSessionFactory 自身は SqlSessionFactoryBuilder によって作られますが、作成される SqlSessionFactory は XML、アノテーション、ハードコードされた Java コンフィグレーションのいずれかの方法で設定することができます。

<span class="label important">NOTE</span> MyBatis を Spring や Guice といった依存性注入（DI = Dependency Injection）フレームワークと組み合わせて使う場合、SqlSession は DI フレームワークによって作成・注入されます。SqlSessionFactoryBuilder や SqlSessionFactory を使う必要はありませんので、SqlSession の章まで飛ばして構いません。DIフレームワークとの組み合わせについては MyBatis-Spring および MyBatis-Guice のマニュアルを参照してください。

#### SqlSessionFactoryBuilder

SqlSessionFactoryBuilder には９つの build() オーバーロードがあります。InputStream を受け取るものが４つ、Reader を受け取るものが４つ、Configuration を受け取るものが１つです。

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

最初の８つのメソッドは InputStream または Reader から XML ファイル（例えば mybatis-config.xml）を読み込みます。オプションの引数は environment と properties です。environment は、データソースやトランザクションマネージャーも含めて、どの environment を読み込むかを決定します。例：

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

引数として environment を取る build メソッドを呼び出した場合、MyBatis はその environment の設定を使ってビルドを実行します。未定義の environment を指定した場合はエラーが発生します。引数に environment を取らない build メソッドを呼び出した場合はデフォルトの environment が使用されます（上記の例では default=\"development\" と指定されています）。

引数に properties のインスタンスを取る build メソッドを実行した場合、これらのプロパティは設定内でアクセスできるように読み込まれます。設定内では ${propName} のように記述することでプロパティを参照することができます。

このドキュメントの前の方で説明しましたが、プロパティは mybatis-config.xml ファイルからも参照される可能性があるので、優先順位について理解しておくことが重要です。再掲しておきます。

---

プロパティが複数の箇所で指定されていた場合、MyBatis は次の順序で読み込みます。

- まず最初に properties 要素のボディで指定されたプロパティが読み込まれます。
- 次に、クラスパスリソースや properties 要素の url 属性で指定された Properties が読み込まれます。先に設定されていた値があれば上書きされます。
- そして最後にメソッドの引数として渡された値が読み込まれます。ここでも properties 要素のボディで指定された値や resource/url 属性で指定されたファイルから同じキーを持つ値が読み込まれていた場合には上書きされます。

従って、メソッド引数として渡されたプロパティが最も優先度が高く、次に resource/url 属性、最も優先度が低いのは properties 要素のボディで指定された値ということになります。

---

まとめると、最初の８つの build メソッドは入力形式と、environment または properties を指定するかどうかが異なります。mybatis-config.xml ファイルから SqlSessionFactory をビルドする例を挙げておきます。

<pre class="prettyprint"><code>String <strong>resource</strong> = "org/mybatis/builder/mybatis-config.xml";
InputStream <strong>inputStream</strong> = Resources.getResourceAsStream(resource);
SqlSessionFactoryBuilder <strong>builder</strong> = new SqlSessionFactoryBuilder();
SqlSessionFactory <strong>factory</strong> = builder.build(inputStream);</code></pre>

ここでは org.apache.ibatis.io パッケージに含まれている Resources ユーティリティクラスを利用しています。このクラスは名前からも分かるように、クラスパスやファイルシステムあるいはウェブ上の URL からリソースを読み込むためのメソッドを提供します。分かりやすい実装なので、ソースを読めばどのようなメソッドが用意されているか分かると思いますが、シグネチャだけリストアップしておきます。

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

もう一つの build メソッドは Configuration のインスタンスを引数に取ります。Configuration クラスには、SqlSessionFactory のインスタンスについて必要とされる設定が全てが含まれています。Configuration クラスを使うと、設定を調べたり、SQL マップの検索、変更を行うこともできます（アプリケーションがリクエストを受け付けた後で SQL マップを操作するのは避けるべきですが）。Configuration クラスは Java API からのみ利用可能で、これまでに出てきた全ての設定にアクセスすることができます。コード内で生成した Configuration のインスタンスを build() メソッドに渡して SqlSessionFactory を作成する簡単な例を挙げておきます。

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

SqlSessionFactory が手に入ったので、SqlSession のインスタンスを生成することができます。

<a id="sqlSessionManager"></a>

#### SqlSessionManager

MyBatis 3.4.0 以降では、`SqlSessionFactory` と `SqlSession` の両方を実装する `SqlSessionManager` を利用できます。`openSession()` を呼び出すたびに新しい `SqlSession` を作成する `DefaultSqlSessionFactory` と異なり、`SqlSessionManager` は `ThreadLocal` を使ってスレッドごとにセッションを管理します。これにより、外部の依存性注入フレームワークを使わずに、マルチスレッド環境で `SqlSession` のライフサイクルを管理できます。

##### SqlSessionManager の作成

`SqlSessionManager` は次の２つの方法で作成できます。

1. MyBatis の XML 設定ファイルを受け取る静的な `newInstance()` メソッドを使う方法：

   ```java
   SqlSessionManager manager = SqlSessionManager.newInstance(inputStream);
   ```

   environment または properties を指定することもできます。

   ```java
   SqlSessionManager manager = SqlSessionManager.newInstance(inputStream, environment);

   // ... or ...

   SqlSessionManager manager = SqlSessionManager.newInstance(inputStream, properties);
   ```
2. 既存の `SqlSessionFactory` インスタンスをラップする方法：

   ```java
   SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(inputStream);
   SqlSessionManager manager = SqlSessionManager.newInstance(factory);
   ```

##### マネージドセッション

`SqlSessionManager` の主な機能は、スレッドごとにセッションを管理することです。マネージドセッションを開始すると、`SqlSessionManager` プロキシを通じて呼び出されるすべての `SqlSession` メソッドは、そのスレッドに紐づくセッションを使用します。

マネージドセッションを開始するには、次のようにします。

```java
manager.startManagedSession();
```

オプションを指定して開始することもできます。

```java
// 自動コミットを指定
manager.startManagedSession(true);

// ExecutorType を指定
manager.startManagedSession(ExecutorType.BATCH);

// 分離レベルを指定
manager.startManagedSession(TransactionIsolationLevel.READ_COMMITTED);

// Connection を指定
manager.startManagedSession(connection);
```

現在のスレッドでマネージドセッションが開始されているか確認するには、次のようにします。

```java
boolean isStarted = manager.isManagedSessionStarted();
```

##### SqlSessionManager の使用

マネージドセッションが開始されている場合、`commit()`、`rollback()`、`close()`、およびデータアクセスメソッド（`selectOne`、`selectList`、`insert`、`update`、`delete` など）は、スレッドに紐づくセッションに対して動作します。処理が終わったら、スレッドローカルの状態を消去するために必ず `close()` を呼び出してください。Tomcat や Jetty のようにスレッドを再利用する Web サーバーで呼び出しを忘れると、別の HTTP リクエストでもそのセッションが使われ、予期しないトランザクションの動作やコネクションのリークが起こる可能性があります。

```java
manager.startManagedSession();
try {
    // セッションを使用
    manager.commit();
} finally {
    manager.close();
}
```

マネージドセッションが開始されていない場合、`SqlSessionManager` のデータアクセスメソッド（`selectOne`、`selectList`、`selectMap`、`selectCursor`、`select`、`insert`、`update`、`delete`）を呼び出すと、短命のセッションが自動的に開かれ、そのメソッドを実行した後に閉じられます。メソッドが正常に終了するとマネージャーは `commit()` を呼び出し、例外がスローされると `rollback()` を呼び出します。これらの呼び出しが実際にデータベースのトランザクションを制御するかどうかは、設定されたトランザクションマネージャーによって異なります。返された `Cursor` はセッションが閉じられると同時に閉じられるため、`selectCursor` を使う場合はマネージドセッションまたは明示的に開いたセッションを使用してください。セッションのライフサイクルに関わるメソッド（`commit()`、`rollback()`、`close()`、`clearCache()`、`flushStatements()`、`getConnection()`）はセッションを自動的には開かず、マネージドセッションが開始されていなければ `SqlSessionException` をスローします。

##### DefaultSqlSessionFactory との違い

| 機能 | DefaultSqlSessionFactory | SqlSessionManager |
| --- | --- | --- |
| セッションの作成 | `openSession()` を呼び出すたびに新しいセッションを作成 | `startManagedSession()` でスレッドごとのセッションを管理可能 |
| スレッドセーフティ | 外部での管理が必要 | スレッドローカルのセッション管理を内蔵 |
| セッションのライフサイクルの自動管理 | なし（手動で閉じる必要あり） | あり（マネージドセッションが開始されていない場合） |
| DI フレームワークとの連携 | DI 固有の仕組みが必要（例：Spring の `SqlSessionTemplate`） | DI を使わない環境では `SqlSessionFactory` と `SqlSession` の代わりに使用可能 |

`SqlSessionManager` は、依存性注入フレームワークを使わずにスレッドごとのセッション管理が必要な環境で特に役立ちます。Spring や Guice を使用している場合は、引き続き `MyBatis-Spring` または `MyBatis-Guice` を使ってセッションを管理してください。

#### SqlSessionFactory

SqlSessionFactory には、SqlSession のインスタンスを作成するためのメソッドが８つ用意されています。一般的には、以下のような条件を考慮してメソッドを選ぶことになると思います。

- <strong>Transaction</strong>: 生成したセッションに対してトランザクション管理が必要か、あるいは auto-commit モード（ほとんどのデータベース／JDBC ドライバで「トランザクション無し」を意味します）で利用するか。
- <strong>Connection</strong>: 設定した DataSource 経由で MyBatis に Connection を取得させるか、あるいはあなたが Connection を提供するか。
- <strong>Execution</strong>: PreparedStatement の再利用とバッチ更新（update, insert, delete）の両方あるいはどちらか一方を行う必要があるか。

上記の判定によって渡すべき引数が決まれば適切な openSession() メソッドを選択することができるはずです。

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

引数を取らないデフォルトの openSession() メソッドは次のような性質を持った SqlSession を生成します。

- MyBatis は auto-commit の無効化を要求します。実際のトランザクション制御は設定されたトランザクションマネージャーに従います。
- `Connection` オブジェクトは、必要になったときに現在の environment で設定されている DataSource インスタンスから取得されます。
- ドライバーまたはデータソースのデフォルトのトランザクション分離レベルが適用されます。
- PreparedStatement の再利用とバッチ更新は、設定された `defaultExecutorType` に従います。デフォルトの `SIMPLE` ではどちらも行われません。

ほとんどのメソッドは見たままです。auto-commit を有効にするには、オプションの引数 auto-commit に対して `true` を指定します。自前の接続を使う場合は、`Connection` のインスタンスを引数として渡します。auto-commit と Connection を両方引数に持つメソッドはありませんが、これは MyBatis が渡された Connection のモードをそのまま利用するからです。トランザクション分離レベルを指定する場合、MyBatis が提供する enum 型のラッパー `TransactionIsolationLevel` を引数として渡します。JDBC によって提供される５つのトランザクション分離レベル(`NONE`, `READ_UNCOMMITTED`, `READ_COMMITTED`, `REPEATABLE_READ`, `SERIALIZABLE`) が用意されています。

もう一つの引数 `ExecutorType` は列挙型で、３つの値が定義されています。

- `ExecutorType.SIMPLE`: 特別なことは行いません。ステートメントを実行するたびに新しい PreparedStatement を作成します。
- `ExecutorType.REUSE`: PreparedStatement を再利用します。
- `ExecutorType.BATCH`: 全ての更新ステートメントをバッチで実行し、途中で SELECT が実行される場合は、より分かりやすい動作となるよう必要に応じてトランザクション境界を設定します。

<span class="label important">NOTE</span> 説明しませんでしたが、SqlSessionFactory にはもう一つ <em>getConfiguration()</em> というメソッドがあります。このメソッドは、MyBatis の実行時設定についての情報を保持する Configuration のインスタンスを返します。

<span class="label important">NOTE</span> 旧バージョンの MyBatis では、セッション、トランザクション、バッチは互いに独立していました。MyBatis 3 では、これら３つは全てセッションのスコープに含まれているので、それぞれを別々に扱う必要はありません。

#### SqlSession

以前説明したように、SqlSession は MyBatis でも最も強力なクラスで、ステートメントの実行、トランザクションのコミットまたはロールバック、Mapper インスタンスの取得を行うメソッドが全て含まれています。

SqlSession には 20 を超えるメソッドが定義されていますので、いくつかのグループに分けて説明します。

##### ステートメントを実行するメソッド

これらは、SQL Mapper で定義されている SELECT, INSERT, UPDATE, DELETE の各メソッドを実行するためのメソッドです。ほとんど自明ですが、それぞれ引数としてステートメントの ID とステートメントの引数オブジェクト（プリミティブ、JavaBean、POJO、Map のいずれか）を取ります。

```xml
<T> T selectOne(String statement, Object parameter)
<E> List<E> selectList(String statement, Object parameter)
<T> Cursor<T> selectCursor(String statement, Object parameter)
<K,V> Map<K,V> selectMap(String statement, Object parameter, String mapKey)
int insert(String statement, Object parameter)
int update(String statement, Object parameter)
int delete(String statement, Object parameter)
```

selectOne と selectList の違いは、selectOne は１つのオブジェクトまたは null を返さなくてはならないということです。複数のオブジェクトが返されると例外が発生します。結果のオブジェクト数が未知の場合は selectList を使用してください。オブジェクトが存在するかどうかを確認したいのなら、カウント結果（0 or 1）を返すようにした方が良いでしょう。selectMap は、結果のリストを mapKey で指定したプロパティに基づいたマップに格納して返す特殊なメソッドです。<br />ステートメントの引数は不要な場合もあるので、parameter オブジェクトの引数を持たないオーバーロードメソッドも用意されています。

insert, update, delete の各メソッドは、ステートメントの実行によって影響を受けた行数を返します。

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

最後に、高度な処理を行うための select メソッドがあります。これらは主に非常に大きなデータセットを扱う場合に、返される行の範囲を限定したり、カスタムの ResultHandler を使って独自に結果処理を行うことができるようになっています。

```xml
<E> List<E> selectList (String statement, Object parameter, RowBounds rowBounds)
<T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds)
<K,V> Map<K,V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowbounds)
void select(String statement, ResultHandler handler)
void select(String statement, Object parameter, ResultHandler handler)
void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler)
```

RowBounds 引数を渡すことによって、指定された数のレコードをスキップし、結果として返される行の数を制限することができます。RowBounds クラスはイミュータブルで、コンストラクタ引数として offset と limit を取ります。

```java
int offset = 100;
int limit = 25;
RowBounds rowBounds = new RowBounds(offset, limit);
```

ドライバーによって得られる効果は異なります。SCROLL\_SENSITIVE または SCROLL\_INSENSITIVE （つまり FORWARD\_ONLY 以外）の結果セットタイプを使った時、最も良いパフォーマンスが得られます。

ResultHandler を渡すと、各行を自由に処理することができます。List に追加したり、Map や Set を作成することもできますし、結果を捨てて合計値のみを返すこともできます。ResultHandler を使えば好きな処理を行うことも可能で、MyBatis 自身も内部的に結果リストを構築するために ResultHandler を利用しています。

3.4.6 以降では、CALLABLE ステートメントに渡された ResultHandler は、指定されたストアド・プロシージャで宣言されている REFCURSOR 型の OUT 引数全てに対して適用されます。

ResultHandler インターフェイスは非常にシンプルです。

```java
package org.apache.ibatis.session;
public interface ResultHandler<T> {
  void handleResult(ResultContext<? extends T> context);
}
```

引数 ResultContext を介して結果オブジェクトにアクセスすることができます。ResultContext#getResultCount() メソッドは作成された結果オブジェクトの数を返します。ResultContext#stop() メソッドを呼び出すと、それ以上結果を読み込まないよう MyBatis に指示します。

ResultHandler を使用する場合に注意すべき点が２つあります。

- ResultHandler を引数に取るメソッドから返されるデータはキャッシュされません。
- 複雑な ResultMap では複数行のデータがひとつのオブジェクトにマッピングされることもあります。こうした ResultMap を ResultHandler と併用する際、association や collection のデータがマッピングされる前の状態のオブジェクトが渡される場合があります。

##### バッチ更新ステートメントをフラッシュするメソッド

バッチ更新用に JDBC ドライバ内に蓄積されたステートメントを任意のタイミングでデータベースへフラッシュ(実行)するメソッドがあります。このメソッドは、 `ExecutorType` として `ExecutorType.BATCH` を使用している場合に使用することができます。

```java
List<BatchResult> flushStatements()
```

<h5 id="transaction-control-methods">トランザクションを制御するメソッド</h5>

トランザクションのスコープを制御するメソッドは４つあります。auto-commit を使用する場合や外部のトランザクションマネージャーを使う場合、JDBC の commit または rollback は実行されませんが、ローカルキャッシュのクリアと保留中のバッチ文の実行または破棄は行われます。JDBC トランザクションマネージャーを使う場合は、これらのメソッドでデータベースのトランザクションも制御します。

```java
void commit()
void commit(boolean force)
void rollback()
void rollback(boolean force)
```

デフォルトでは、データベースの変更を伴うメソッド insert, update, delete, `affectData` を有効化した select が実行されない限り MyBatis は commit を実行しません。何らかの理由でこれらのメソッドを使わずにデータを変更した場合は確実にコミットされるように commit メソッドに引数 true を渡してください（ただし、auto-commit モードのセッションや外部のトランザクションマネージャーを使っている場合は true を渡してもコミットされません）。commit が実行されない場合、MyBatis がロールバックを実行するので、通常明示的に rollback() メソッドを呼び出す必要はありません。しかし、一つのセッションの中で複数のコミットやロールバックが必要とされるようなケースでは、rollback() メソッドを使ってより細かい制御を行うことが可能です。

<span class="label important">NOTE</span> Mybatis-Spring と MyBatis-Guice では宣言的トランザクションがサポートされています。詳細は各サブプロジェクトのドキュメントを参照してください。

##### ローカルキャッシュ

MyBatis は２種類の内部キャッシュを使用します。

新しいセッションが生成される際、同時にローカルキャッシュが作成され、セッションにアタッチされます。通常のクエリはローカルキャッシュに保存され、同一パラメーターで再度呼び出された場合はデータベースに問い合わせずに結果を返します。カスタム ResultHandler を渡すクエリはローカルキャッシュから結果を読み取りません。Cursor を使うクエリはローカルキャッシュを一切使用しません。ローカルキャッシュは update, commit, rollback, close が実行されるとクリアされるようになっています。

デフォルトでは、ローカルキャッシュはセッションが破棄されるまで保持されます。ローカルキャッシュは ResultMap の循環参照の解決やネストされたクエリの効率化にも使用されているため、完全に無効化することはできませんが、localCacheScope に STATEMENT を設定することで保持期間をステートメント単位に変更することができます。

localCacheScope に SESSION が設定されている場合、MyBatis は同一オブジェクトへの参照を返すという点に注意してください。返されたオブジェクトやリストなどに変更を加えた場合、ローカルキャッシュの内容が変更されることになるので、セッションが有効な間に同じクエリを発行すると意図しない結果が戻ることになります。同一セッション内で同じクエリを繰り返し発行するようなケースでは、結果として返されたオブジェクトを変更しないほうが無難です。

また、次のメソッドを呼び出すことにより任意のタイミングでローカルキャッシュをクリアすることも可能です。

```java
void clearCache()
```

##### 確実に SqlSession をクローズする

```java
void close()
```

最も重要なのは、オープンした session は必ずクローズする必要があるということです。そのためには次のようなパターンでコードを書くのが最も確実です。

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
    // following 3 lines pseudocode for "doing some work"
    session.insert(...);
    session.update(...);
    session.delete(...);
    session.commit();
}
```

<span class="label important">NOTE</span> SqlSessionFactory と同様、getConfiguration() メソッドを呼び出すことで使用中の Configuration のインスタンスを取得することができます。

```java
Configuration getConfiguration()
```

##### Mapper を使う

```xml
<T> T getMapper(Class<T> type)
```

SqlSession に用意されている insert, update, delete, select などのメソッドは確かに強力ですが、かなり冗長で、型に安全でないため IDE やユニットテストの機能をフルに活用することができません。既にスタートガイドの章で Mapper を使う例が出てきました。

マップドステートメントを実行する際は Mapper クラスを使った方法がより一般的です。Mapper クラスは SqlSession のメソッドに対応したメソッド定義を持つインターフェイスです。次の例は、Mapper クラスで定義されているメソッドが SqlSession のメソッドとどのように対応しているかを表しています。

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

基本的に、それぞれの Mapper メソッドのシグネチャは、対応する SqlSession のメソッドのシグネチャからステートメントの ID を指定する String 型の引数を除いたものになっています。ステートメントの ID は引数で指定するのではなくメソッド名から取得されます。

戻り値の型について補足しておくと、クエリの結果が単一オブジェクトの場合はその型と一致している必要があり、複数の場合は配列またはコレクションになります。プリミティブ、Map, POJO, JavaBean など通常の型は一通り指定可能です。

<span class="label important">NOTE</span> Mapper インターフェイスは、他のインターフェイスを実装したり、他のクラスを継承する必要はありません。定義されているメソッドのシグネチャから対応するステートメントを識別できるようになっていれば OK です。

<span class="label important">NOTE</span> Mapper インターフェイスは他のインターフェイスを継承することができます。Mapper インターフェイスを XML と組み合わせて使う場合は、ステートメントが正しいネームスペースに含まれるように注意してください。また唯一の制限として、継承関係にある複数のインターフェイスに同じシグネチャを持つメソッドを定義することはできません（そもそも良い考えではありません）。

Mapper メソッドに複数の引数を渡すこともできます。ステートメントの中では #{param1}, #{param2} のように引数の順番を変数名として使用することができます。単一の引数を含め、@Param(\"paramName\") アノテーションで引数に名前を付けることもできます。

引数として RowBounds のインスタンスを渡すとクエリ結果の行数を制限することができます。

##### Mapper アノテーション

MyBatis は当初から XML 駆動型のフレームワークでした。設定ファイルは XML でしたし、マップドステートメントも XML で定義されています。MyBatis 3 からは新しいオプションが追加されました。MyBatis 3 は包括的で強力な Java ベースの Configuration API の上に構築されています。この Configuration API が、XML による設定とアノテーションによる設定の基礎となっています。アノテーションを使うと、シンプルなマップドステートメントを無駄なく実装することができます。

<span class="label important">NOTE</span> 残念ながら、アノテーションの表現力と柔軟性には制限があります。調査や試行錯誤に多くの時間を費やしたにも関わらず、複雑なマッピングをアノテーションで実現することはできません。（例えば）C# の Attributes にはこのような制限が無いので、MyBatis.NET では XML の代わりに Attributes を活用することができます。とは言っても、Java のアノテーションにも利点が無いわけではありません。

<strong>アノテーション一覧：</strong>

| アノテーション | Target | 対応する XML 要素 | 説明 |
| --- | --- | --- | --- |
| `@CacheNamespace` | `Class` | `<cache>` | アノテーションを付加したネームスペース（＝クラス）に対するキャッシュを設定します。属性: `implementation`, `eviction`, `flushInterval`, `size`, `readWrite`, `blocking`, `properties` |
| `@Property` | N/A | `<property>` | プロパティ値またはプレースホルダ(`mybatis-config.xml`で定義した構成プロパティで置き換えすることができる)を指定します。属性: `name`, `value`。 (MyBatis 3.4.2+で利用可能) |
| `@CacheNamespaceRef` | `Class` | `<cache-ref>` | 指定したネームスペースのキャッシュを参照します。アノテーションで定義したステートメントは、同じネームスペースの XML マッパーで宣言したキャッシュを自動的には使用しません。そのキャッシュを使用するには、このアノテーションで参照します。属性: `value`, `name` このアノテーションを使用する場合は、`value`または`name`属性のどちらかを指定する必要があります。 `value`属性にはネームスペースを示すJava型(ネームスペース名は指定したJava型のFQCNになる)を、 `name`属性(この属性は3.4.2以降で利用可能)にはネームスペースを示す名前を指定します。 |
| `@ConstructorArgs` | `Method` | `<constructor>` | 結果オブジェクトのコンストラクターに渡す結果のグループを指定します。単一値 `value` として `Arg` の配列を取ります。 |
| `@Arg` | N/A | <ul><li>`<arg>`</li> <li>`<idArg>`</li></ul> | ConstructorArgs に含まれる個々のコンストラクター引数です。属性: `id`, `column`, `javaType`, `jdbcType`, `typeHandler`, `select`, `resultMap`, `name`, `columnPrefix`. id は真偽値で、指定したプロパティがオブジェクトをユニークに識別できる値であることを表します。この働きは XML 要素の `<idArg>` に相当します。 3.5.4以降では、繰り返し可能な注釈として使用することができます。 |
| `@TypeDiscriminator` | `Method` | `<discriminator>` | 使用する結果マップを決定するための Case のグループを構成します。属性: `column`, `javaType`, `jdbcType`, `typeHandler`, `cases`. case の値は `Case` の配列です。 |
| `@Case` | N/A | `<case>` | 値と対応するマッピングを含む個々の判定条件です。属性: `value`, `type`, `results`, `constructArgs`. results は `Result` の配列、constructArgs は `Arg` の配列です。 |
| `@NamedResultMap` | `Type` | `<resultMap>` | 複数のステートメントで再利用できる結果マップを型（通常は Mapper インターフェイス）に定義します。必須属性は `id` と `javaType`、任意の属性は `constructorArguments`（Arg の配列）、`propertyMappings`（Result の配列）、`typeDiscriminator` です。少なくとも１種類のマッピングを指定します。 |
| `@Results` | `Method` | `<resultMap>` | ある結果列とプロパティまたはフィールドのマッピング情報を定義する Results のリストです。属性: `value`, `id`. value 属性は `Result` アノテーションの配列です。 id 属性でマッピング情報の名前を指定することができます。 |
| `@Result` | N/A | <ul><li>`<result>`</li> <li>`<id>`</li></ul> | ある結果列とプロパティまたはフィールドのマッピング情報を定義します。属性: `id`, `column`, `property`, `javaType`, `jdbcType`, `typeHandler`, `one`, `many`. id は真偽値で、そのプロパティがオブジェクトの比較に使うよう指示します（XML マッピングにおける id 要素とほぼ同じです）。one は XML における association 要素に、many は collection 要素に相当します（クラス名のコンフリクトを避けるため異なる名称が使われています）。 3.5.4以降では、繰り返し可能な注釈として使用することができます。 |
| `@One` | N/A | `<association>` | 複雑型のプロパティのマッピング情報を定義します。属性: `select`, `fetchType`, `resultMap`(3.5.5以降で利用可能), `columnPrefix`(3.5.5以降で利用可能). select は適切な型を読み込むことができるマップドステートメント（Mapper メソッド）の完全修飾名です。 fetchType はグローバルな設定 `lazyLoadingEnabled` をオーバーライドする場合に指定します。 resultMap は結果列を単一のコンテナオブジェクト(JavaBeanなど)へマッピングするための結果マップの完全修飾名を指定します。 columnPrefix はネストした結果マップで結果列をグループ化するためのカラム名のプレフィックスを指定します。 |
| `@Many` | N/A | `<collection>` | 複雑型のプロパティのマッピング情報を定義します。属性: `select`, `fetchType`, `resultMap`(3.5.5以降で利用可能), `columnPrefix`(3.5.5以降で利用可能). select は適切な型のコレクションを読み込むことができるマップドステートメント（Mapper メソッド）の完全修飾名です。 fetchType はグローバルな設定 `lazyLoadingEnabled` をオーバーライドする場合に指定します。 resultMap は結果列をコレクションオブジェクト(JavaBeanのリストなど)へマッピングするための結果マップの完全修飾名を指定します。 columnPrefix はネストした結果マップで結果列をグループ化するためのカラム名のプレフィックスを指定します。 |
| `@MapKey` | `Method` |  | このアノテーションは Map を返すメソッドに付加します。結果オブジェクトのリストを Map として返すことができます。単一値 `value` は、結果オブジェクトのプロパティで、Map のキーとして使用されます。 |
| `@Options` | `Method` | マップドステートメントの属性 | このアノテーションを使うと、通常マップドステートメントの属性として指定される多様なスイッチや設定オプションにアクセスすることができます。`Options` アノテーションによって、各ステートメントのアノテーションを複雑化することなく、一貫したクリーンな方法で設定にアクセスできるよう工夫されています。キー: Attributes: `useCache=true`, `flushCache=FlushCachePolicy.DEFAULT`, `resultSetType=DEFAULT`, `statementType=PREPARED`, `fetchSize=-1`, `timeout=-1`, `useGeneratedKeys=false`, `keyProperty=""`, `keyColumn=""`, `resultSets=""`, `databaseId=""`. Java アノテーションを使う場合、値として `null` を指定することはできないという制限があります。これはどういうことかというと、`Options` アノテーションを付加したステートメントにはデフォルトのオプションが適用されるということです。予期しない動作を防ぐため、各オプションのデフォルト値を把握しておくようにしてください。 `databaseId`(3.5.5以降で利用可能)：`DatabaseIdProvider`の設定がある場合は、 MyBatisは`databaseId`の指定がないものまたは`DatabaseIdProvider`が提供する値と一致するものを利用します。 もし`databaseId`の指定があるものとないものを両方見つけた場合は、後者は破棄されます。<br /><br /> `keyColumn` は特定のデータベース（Oracle や PostgreSQL など）でのみ必須となります。 `keyColumn` と `keyProperty` に対して指定可能な値については、上で出てきた insert ステートメントについての説明を参照してください。 |
| <ul><li>`@Insert`</li> <li>`@Update`</li> <li>`@Delete`</li> <li>`@Select`</li></ul> | `Method` | <ul><li>`<insert>`</li> <li>`<update>`</li> <li>`<delete>`</li> <li>`<select>`</li></ul> | これらのアノテーションは、それぞれ実行対象の SQL に対応しています。各アノテーションは String の配列（単一の String でも OK）を引数に取ります。 渡された String の配列は、スペース区切りで連結されます。これによって、Java のコード内で SQL を構築するときに良くある \'スペースの付け忘れ\' によるバグを防ぐことができます。一応、\'+\' 記号で連結した文字列を引数とすることも可能です。 `value`：SQL ステートメントを構成する String の配列です。`@Select` の `affectData` は、データを変更する SELECT をトランザクション制御の対象として指定します。 `databaseId`(3.5.5以降で利用可能)：`DatabaseIdProvider`の設定がある場合は、 MyBatisは`databaseId`の指定がないものまたは`DatabaseIdProvider`が提供する値と一致するアノテーションに指定されているステートメントを利用します。 もし`databaseId`の指定があるものとないものを両方見つけた場合は、後者は破棄されます。 |
| <ul><li>`@InsertProvider`</li> <li>`@UpdateProvider`</li> <li>`@DeleteProvider`</li> <li>`@SelectProvider`</li></ul> | `Method` | <ul><li>`<insert>`</li> <li>`<update>`</li> <li>`<delete>`</li> <li>`<select>`</li></ul> | これらのアノテーションは動的 SQL を生成するためのものです。実行時に指定されたメソッドが呼び出され、メソッドから返された SQL ステートメントが実行されます (MyBatis 3.4.6以降では、メソッドの返り値として `String` ではなく `CharSequence` を指定することができます)。 マップドステートメントを実行する際、プロバイダーによって指定したクラスのインスタンスが作成され、指定されたメソッドが実行されます。 なお、メソッド引数にはMapperメソッドの引数に渡したオブジェクトに加え、`ProviderContext`(MyBatis 3.4.5以降で利用可能)を介して「Mapperインタフェースの型」「Mapperメソッド」「データベースID」を渡すことができます。(MyBatis 3.4以降では、複数の引数を渡すことができます) キー: `value`, `type`, `method`, `databaseId`。`@SelectProvider` の `affectData` は、データを変更する SELECT をトランザクション制御の対象として指定します。 `value` と `type` にはクラスオブジェクトを指定します (`type` は `value` の別名で、どちらか一方を指定する必要があります。 ただし、グローバル設定として`defaultSqlProviderType`を指定している場合は両方とも省略することができます)。 `method` にはメソッド名を指定します (MyBatis 3.5.1以降では、`method` 属性を省略することができます。その際MyBatisは、`ProviderMethodResolver` インタフェースを介して対象メソッドの解決を試み、 対象メソッドが解決できない場合は、`provideSql`という名前のメソッドを代替メソッドとして利用します)。 `databaseId`(3.5.5以降で利用可能)：`DatabaseIdProvider`の設定がある場合は、 MyBatisは`databaseId`の指定がないものまたは`DatabaseIdProvider`が提供する値と一致するアノテーションに指定されているメソッドを利用します。 もし`databaseId`の指定があるものとないものを両方見つけた場合は、後者は破棄されます。 <span class="label important">NOTE</span> 次の章で、クリーンで可読性の高いコードで動的 SQL を構築するためのクラスについて説明します。 |
| `@Param` | `Parameter` | N/A | Mapper メソッドが複数の引数を取る場合、このアノテーションを付加することで各引数を名前で参照できるようになります。アノテーションがない場合、各引数は位置を表す数字で `#{param1}`, `#{param2}` のように参照します（`RowBounds` 引数はカウントしません）。 引数に `@Param("person")` というアノテーションを付加すると、引数が１つだけの場合も `#{person}` として参照できます。 |
| `@SelectKey` | `Method` | `<selectKey>` | このアノテーションを `@Insert`, `@InsertProvider`, `@Update`, `@UpdateProvider` が付加されたメソッドに追加することで、XML の `<selectKey>` に相当する機能を実現することができます（他のメソッドに追加しても無視されます）。`@SelectKey` アノテーションが指定されている場合、`@Options` アノテーションや設定プロパティによるキーの自動生成に関する設定は無視されます。 属性: `statement` SQL ステートメントを構成する String の配列です。 `keyProperty` は自動生成されたキーの値が設定される引数オブジェクトのプロパティを指定します。`keyColumn` はキーの値を取得する列名を指定します。複数ある場合はカンマで区切ります。`before` insert または update の前にステートメントを実行する場合は true、後に実行する場合は false を指定します。`resultType` は keyProperty で指定したプロパティの Java タイプです。`statementType` はステートメントの種類で STATEMENT, PREPARED, CALLABLE のいずれかを指定します（デフォルトは PREPARED）。 `databaseId`(3.5.5以降で利用可能)：`DatabaseIdProvider`の設定がある場合は、 MyBatisは`databaseId`の指定がないものまたは`DatabaseIdProvider`が提供する値と一致するアノテーションに指定されているステートメントを利用します。 もし`databaseId`の指定があるものとないものを両方見つけた場合は、後者は破棄されます。 |
| `@ResultMap` | `Method` | N/A | このアノテーションを `@Select` または `@SelectProvider` が付加されているメソッドに追加することで、結果のマッピングに XML の Mapper ファイルで定義されている resultMap 要素を利用することができます。の id を指定します。単一値の `value` には resultMap の id を指定します。このアノテーションは `@Results` や `@ConstructorArgs` による指定よりも優先されます。 |
| `@ResultOrdered` | `Method` | `<select>` の resultOrdered 属性 | 結果が順序付けられていることを示します。コンストラクターにコレクションを持つオブジェクトを作成する場合に必要です。真偽値の `value` はデフォルトで `true` です。 |
| `@ResultType` | `Method` | N/A | ResultHandler を使うメソッドでは戻り値の型が void となるので、このアノテーションを使って各行のデータをどのクラスにマップするかを指定します。XMLの ResultMap が存在する場合は @ResultMap アノテーションで指定することができます。XML の `<select>` 要素で resultType が指定されている場合はアノテーションによる指定は不要です。それ以外の場合、例えば @Select アノテーションが付加された引数に ResultHandler を含むメソッドの場合は戻り値の型は void である必要があるので、このアノテーション（あるいは @ResultMap）を使って型を指定する必要があります。メソッドの戻り値の型が void 以外の場合、このアノテーションは無視されます。 |
| `@Flush` | `Method` | N/A | このアノテーションを使用すると、`SqlSession#flushStatements()`メソッドを Mapper インタフェースに定義したメソッド経由で呼び出すことができます。(MyBatis 3.3以上) |

##### Mapper アノテーションのサンプル

次のコードは @SelectKey アノテーションを使って insert 前にシーケンスの値を取得する例です。

<pre class="prettyprint"><code>@Insert("insert into table3 (id, name) values(#{nameId}, #{name})")
@SelectKey(statement="call next value for TestSequence", keyProperty="nameId", before=<strong>true</strong>, resultType=<strong>int.class</strong>)
<strong>int</strong> insertTable3(Name name);</code></pre>

次のコードは @SelectKey アノテーションを使って insert 後に identity の値を取得する例です。

<pre class="prettyprint"><code>@Insert("insert into table2 (name) values(#{name})")
@SelectKey(statement="call identity()", keyProperty="nameId", before=<strong>false</strong>, resultType=<strong>int.class</strong>)
<strong>int</strong> insertTable2(Name name);</code></pre>

次のコードは `@Flush` アノテーションを使って `SqlSession#flushStatements()`メソッドを呼び出す例です。

```java
@Flush
List<BatchResult> flush();
```

次のコードは @Results アノテーションの id 属性で名前を指定する例です。

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

次のコードは SQLプロバイダー用のアノテーションを使用して、パラメータをひとつ受け取る例です。

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

次のコードは SQLプロバイダー用のアノテーションを使用して、複数パラメータをひとつ受け取る例です。

```java
@SelectProvider(type = UserSqlBuilder.class, method = "buildGetUsersByName")
List<User> getUsersByName(
    @Param("name") String name, @Param("orderByColumn") String orderByColumn);

class UserSqlBuilder {

  // @Paramを使わない場合は, Mapperメソッドと同じ引数で定義する必要があります。
  public static String buildGetUsersByName(
      final String name, final String orderByColumn) {
    return new SQL(){{
      SELECT("*");
      FROM("users");
      WHERE("name like #{name} || '%'");
      ORDER_BY(orderByColumn);
    }}.toString();
  }

  // @Paramを使う場合は, 必要な引数のみ定義することができます。
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

次のコードは、グローバル設定を利用して全てのマッパーメソッドで同じSQLプロバイダクラスを利用する例です。(3.5.6以降で利用可能)

```java
Configuration configuration = new Configuration();
configuration.setDefaultSqlProviderType(TemplateFilePathProvider.class); // 全てのマッパーメソッドで利用するSQLプロバイダクラスを指定する
// ...
```

```java
// 各メソッドのSQLプロバイダアノテーションの type または value 属性は省略することができ、
// 省略した場合はMyBaitsはグローバル設定の defaultSqlProviderType に指定されているクラスを適用する
public interface UserMapper {

  @SelectProvider // @SelectProvider(TemplateFilePathProvider.class) と同義
  User findUser(int id);

  @InsertProvider // @InsertProvider(TemplateFilePathProvider.class) と同義
  void createUser(User user);

  @UpdateProvider // @UpdateProvider(TemplateFilePathProvider.class) と同義
  void updateUser(User user);

  @DeleteProvider // @DeleteProvider(TemplateFilePathProvider.class) と同義
  void deleteUser(int id);
}
```

次のコードは、`ProviderMethodResolver`(MyBatis 3.5.1以降で利用可能)のデフォルト実装の利用例です。

```java
@SelectProvider(UserSqlProvider.class)
List<User> getUsersByName(String name);

// SQLプロバイダクラスにProviderMethodResolverを実装する
class UserSqlProvider implements ProviderMethodResolver {

  // デフォルト実装では、マッパーメソッドと同名のメソッドが対象メソッドとして扱われます。
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

次のコードは、`databaseId`属性(3.5.5以降で利用可能)の利用例です。

```java
@Select(value = "SELECT SYS_GUID() FROM dual", databaseId = "oracle") // DatabaseIdProviderが提供する値が "oracle" の時にこのステートメントを利用する
@Select(value = "SELECT uuid_generate_v4()", databaseId = "postgres") // DatabaseIdProviderが提供する値が "postgres" の時にこのステートメントを利用する
@Select("SELECT RANDOM_UUID()") // DatabaseIdProviderの設定がないまたはDatabaseIdProviderが提供する値に一致しない時にこのステートメントを利用する
String generateId();
```
