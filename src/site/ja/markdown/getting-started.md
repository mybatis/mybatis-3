title: MyBatis 3 | スタートガイド
author: Clinton Begin, Iwao AVE!

# スタートガイド

## Installation

MyBatis を使うためには、ダウンロードした [mybatis-x.x.x.jar](https://github.com/mybatis/mybatis-3/releases) をクラスパスに追加する必要があります。

Maven を利用している場合は pom.xml に下記の依存性を追加するだけで OK です。

```xml
<dependency>
  <groupId>org.mybatis</groupId>
  <artifactId>mybatis</artifactId>
  <version>x.x.x</version>
</dependency>
```

## XML 形式の設定ファイルを使って SqlSessionFactory を生成する

MyBatis アプリケーションは、SqlSessionFactory のインスタンスを中心に構成されています。<br />
SqlSessionFactory のインスタンスは、SqlSessionFactoryBuilder を使って取得することができます。 SqlSessionFactoryBuilder が SqlSessionFactory を生成する際の設定は、XML 形式の設定ファイルを読み込むか、独自に用意した Configuration クラスのインスタンスを渡すことで行います。

XML 形式の設定ファイルを使って SqlSessionFactory を生成するのはとても簡単です。<br />
この設定ファイルはクラスパスに置くことが推奨されますが、ファイルパスや `file://` 形式の URL 文字列から生成した InputStream を使ってクラスパス以外の場所に配置されたファイルを読み込むこともできます。 MyBatis 付属の Resources というユーティリティクラスには、クラスパスや、それ以外の場所からリソースを読み込むためのメソッドが多数用意されています。

```java
String resource = "org/mybatis/example/mybatis-config.xml";
InputStream inputStream = Resources.getResourceAsStream(resource);
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
```

XML 形式の設定ファイルには、MyBatis システムの基本設定が含まれます。 例えば、データベースから Connection のインスタンスを取得する DataSource や、トランザクションの制御方法を決定する TransactionManager などです。<br />
XML 形式の設定ファイルの詳細については後ほど改めて説明しますが、ここでは簡単なサンプルを挙げておきます。

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

XML 形式の設定ファイルについて説明すべきことは他にもたくさんありますが、最も重要な設定は上記のサンプルに含まれています。<br />
まず、XML ドキュメントのバリデーションを行うために必要となる XML ヘッダがあります。<br />
environment 要素には、トランザクション管理やコネクションプーリングといった環境依存の設定が含まれています。<br />
mappers 要素には Mapper のリストが含まれています。Mapper とは、SQL 文とマッピングの定義を含む XML ファイルです。

## XML を使わずに SqlSessionFactory を生成する

XML を使わず Java のコードで設定を行いたい方のために、Configuration クラスが提供されています。<br />
XML 形式の設定ファイルで指定できるオプションは、このクラスでも全て設定可能です。

```java
DataSource dataSource = BlogDataSourceFactory.getBlogDataSource();
TransactionFactory transactionFactory = new JdbcTransactionFactory();
Environment environment = new Environment("development", transactionFactory, dataSource);
Configuration configuration = new Configuration(environment);
configuration.addMapper(BlogMapper.class);
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
```

今回は、Configuration のインスタンスに Mapper クラスを追加していることに気づいたでしょうか。<br />
Mapper クラスには、XML を使わずに SQL マッピングを定義するためのアノテーションが含まれています。 ただし、Java アノテーションの制約と MyBatis のマッピング機能の複雑さのため、高度なマッピングを定義する際に XML 形式の Mapper が必要となる場合があります（ネストされた結合クエリの結果をマッピングする場合など）。<br />
このため、MyBatis は対になる XML ファイルを探して自動的に読み込むようになっています（この例では、BlogMapper.class のクラスパスと名前を元に BlogMapper.xml という XML ファイルが読み込まれます）。 この動作については、後ほど詳しく説明します。

## SqlSessionFactory から SqlSession を取得する

名前を見れば分かると思いますが、生成した SqlSessionFactory から SqlSession のインスタンスを取得することができます。<br />
SqlSession には、一連の SQL コマンドを実行するためのメソッドが全て用意されています。 SqlSession のインスタンスに対して、マップされた SQL 文を直接指定して実行することができます。<br />
例えば下記のようなコードになります。

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  Blog blog = (Blog) session.selectOne("org.mybatis.example.BlogMapper.selectBlog", 101);
}
```

この方法は期待通りに動作しますし、旧バージョンの MyBatis に慣れている方には分かりやすいと思いますが、 現在のバージョンではもっと美しい方法があります。 実行する SQL 文にマッチするように引数と戻り値がきちんと定義されたインターフェイスを使えば、ミスしがちな文字列やキャストなしで、より美しく、型に安全なコードを使って SQL を実行することができます。

例：

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  BlogMapper mapper = session.getMapper(BlogMapper.class);
  Blog blog = mapper.selectBlog(101);
}
```

では、実際にどのような処理が行われているのかを見て行きましょう。

## Mapped SQL Statements について

SqlSession や Mapper クラスによって何が行われているのか、そろそろ気になり出した頃ではないでしょうか。 Mapped SQL Statements は非常に大きな話題で、このドキュメントの大部分を占めることになると思いますが、 ここではいくつか例を挙げて、実際にどのような処理が行われているのかを簡単に説明してみたいと思います。

上で挙げた２つのサンプルに対応する SQL Statement は、XML、アノテーションのどちらを使っても定義することができます。<br />
はじめに XML について見て行きましょう。 何年にも渡って MyBatis の人気を築いてきた XML ベースのマッピング言語を使えば、MyBatis の全機能を把握することができます。 このドキュメントの後の方で出てきますが、旧バージョンの MyBatis を使ったことがある方なら、コンセプトは同じですが、多くの改良が加えられていることが分かると思います。 以下は、先ほどの例で SqlSession のメソッドを呼び出したときに実行されることを想定した XML ベースの Mapped Statement です。

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

例が簡単なので無駄に記述量が多いように見えるかも知れませんが、本質的にはそれほどではありません。<br />
ひとつの Mapper XML ファイルに複数の Mapped Statement を定義することができるので、XML ヘッダや doctype 宣言は一度書くだけです。<br />
残りの部分はほぼ自明ですが、「org.mybatis.example.BlogMapper」というネームスペースの中に「selectBlog」という名前の Mapped Statement を定義しています。<br />
上のサンプルと同じですが、この Mapped Statement を実行するには、以下のように完全修飾名「org.mybatis.example.BlogMapper.selectBlog」を指定して SqlSession のメソッドを呼び出します。

```java
Blog blog = session.selectOne("org.mybatis.example.BlogMapper.selectBlog", 101);
```

この例が、完全修飾名で指定した Java クラスのメソッドを呼び出すのと似ていることに気づいたでしょうか。 これには理由があります。 この完全修飾名は、Mapped Statement の名前、引数、戻り値にマッチするメソッドを持った、同名の Mapper クラスにマッピングすることができます。 上で見たような Mapper インターフェイスへの簡単な呼び出しができるのはこの仕組みのおかげです。 再掲しておきます。

```java
BlogMapper mapper = session.getMapper(BlogMapper.class);
Blog blog = mapper.selectBlog(101);
```

２番目の方法には多くの利点があります。<br />
まず、文字列リテラルに頼らずに済むので、ずっと安全です。また、Mapped SQL Statement を選ぶときに IDE のコード補完機能を使うことができます。

---

<span class="label important">NOTE</span> **ネームスペースについて**

**ネームスペース：** 過去のバージョンの MyBatis では、ネームスペースはオプションでしたが、これは混乱や誤用の原因となっていました。<br />
現在のバージョンではネームスペースは必須であり、単に長い完全修飾名を使って Statement を整理する以上に重要な役割を果たしています。

上で見たように、ネームスペースはインターフェイスバインディングを行うために使われています。 たとえ今は必要ないと思っても、将来気が変わったときのために推奨される手順通りに設定しておくべきです。 長い目で見れば、ネームスペースを指定して正しい Java パッケージに配置しておくことでクリーンなコードを書くことができ、MyBatis の使い勝手も向上するはずです。

**名前解決：** タイピング量を減らすため、MyBatis は次のようなルールに則って設定要素（Statement, Result Map, Cache など）の名前解決を行います。

- 完全修飾名（例えば「com.mypackage.MyMapper.selectAllThings」）で指定した場合は、指定された完全修飾名で要素を探し、もし見つかればその要素を参照します。
- 短縮名（例えば「selectAllThings」）で指定した場合は、短縮名部分が一致する要素を参照します。 ただし、同じ短縮名を持った要素が２つ以上存在する場合（例えば「com.foo.selectAllThings」と「com.bar.selectAllThings」）は、 指定した短縮名が曖昧なので完全修飾名で指定する必要がある、というエラーが発生します。

---

BloggerMapper のような Mapper クラスなら、実は XML を使う必要はありません。 代わりに、Java アノテーションを使って下記のように Mapped Statement を記述することができます。

```java
package org.mybatis.example;
public interface BlogMapper {
  @Select("SELECT * FROM blog WHERE id = #{id}")
  Blog selectBlog(int id);
}
```

アノテーションを使うと単純な SQL 文をスッキリ書くことができますが、複雑な SQL 文をアノテーションを使って書こうとすると可読性が落ちますし、アノテーション固有の制限もありますので、複雑な Mapped Statement を扱わなくてはならない場合には XML を使うことをお勧めします。

XML とアノテーションのどちらを使うべきか、そしてどのように一貫性を持たせるかは、あなたとあなたのプロジェクトチーム次第です。 ただ、常にどちらか一方を使わなくてはならない、という訳ではありません。 アノテーションベースの Mapped Statement を XML に書き換えることは簡単なことですし、逆もまたしかりです。

## スコープとライフサイクル

これまでに説明した様々なクラスに適したスコープや、そのライフサイクルについて理解しておくことは大変重要です。 誤って使用すると、深刻な整合性の問題の原因となります。

---

<span class="label important">NOTE</span> **オブジェクトのライフサイクルと依存性注入（Dependency Injection）フレームワーク**

Dependency Injection フレームワークを使うと、スレッドセーフでトランザクション管理された SqlSession や Mapper のインスタンスを作成し、あなたの Bean にインジェクトすることもできます。 こうすることで、SqlSession や Mapper のライフサイクルについてアプリケーションロジックと分離することができます。 MyBatis と DI フレームワークの組み合わせについては、サブプロジェクトである MyBatis-Spring または MyBatis-Guice を参照してください。

---

#### SqlSessionFactoryBuilder

このクラスは、インスタンス化し、使用し、破棄することができます。 一旦 SqlSessionFactory を生成してしまえば、このクラスを残しておく理由はありません。 したがって、このクラスのスコープとして最適なのはメソッドスコープ（つまり、ローカルメソッド変数）です。 SqlSessionFactoryBuilder を再利用して複数の SqlSessionFactory を生成することも可能ですが、XML をパースするためのリソースが他の重要なものを圧迫しないように、このクラスを保持して使いまわさない方が得策です。

#### SqlSessionFactory

生成した SqlSessionFactory は、あなたのアプリケーション実行中はそのまま残しておくべきです。 生成した SqlSessionFactory を破棄したり、再度生成する理由はないはずです。 SqlSessionFactory を再生成しない、というのは所謂ベストプラクティスで、これを行なっていたら「何かおかしいぞ」と考えるべきです。 したがって、SqlSessionFactory に最適なのはアプリケーションスコープ、ということになります。 これを実現する方法はいくつもあります。 最も簡単なのはシングルトンパターンまたはスタティックシングルトンパターンを使う方法です。

#### SqlSession

各スレッドは、独立した SqlSession のインスタンスを使うべきです。 SqlSession のインスタンスは共有されることを前提としていないため、スレッドセーフではありません。 当然、最適なスコープはメソッドスコープになります。 SqlSession のインスタンスへの参照を static なフィールドや、インスタンスフィールドにも格納してはいけません。 Servlet フレームワークの HttpSession のようなマネージドスコープに SqlSession への参照を保持するのもダメです。 もし何らかの Web フレームワークを使っているのであれば、SqlSession のスコープが HTTP リクエストと同調するようにしておくべきです。 つまり、HTTP リクエストを受け取ったら SqlSession をオープンし、レスポンスを返すときにクローズすれば良いのです。 セッションをクローズすることはとても重要です。 間違いがないよう、常に finally ブロックの中でセッションをクローズするようにした方が良いでしょう。 SqlSession を確実にクローズするための一般的なパターンは下記のようなものです。

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  // do work
}
```

常にこのパターンに従っておけば、すべてのデータベースリソースを確実にクローズすることができます。

#### Mapper インスタンス

Mapper は、Mapped Statement をバインドするためのインターフェイスです。 Mapper インターフェイスのインスタンスは SqlSession から取得されます。 したがって、Mapper インスタンスの理論上の最長のスコープは、インスタンス取得元の SqlSession と同じということになります。 ですが、Mapper インスタンスに最適なスコープはメソッドスコープです。 つまり、Mapper インスタンスを利用するメソッドの中で取得・破棄するということです。 Mapper インスタンスを明示的にクローズする必要はありません。 リクエスト処理が完了するまで残しておいても問題ありませんが、SqlSession 同様、このレベルで余分なリソースを大量に扱うと、すぐに手に負えない状況になってしまうでしょう。 単純化のため、Mapper はメソッドスコープの中で使うようにしてください。 このプラクティスを実践したのが次のサンプルになります。

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  BlogMapper mapper = session.getMapper(BlogMapper.class);
  // do work
}

```