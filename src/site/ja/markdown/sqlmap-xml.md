title: MyBatis 3 | Mapper XML ファイル
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

# Mapper XML ファイル

Mapped Statement こそ MyBatis のパワーの源です。 ここで魔法がかけられているのです。 Mapper XML ファイルは、そのパワーの割に比較的シンプルです。 JDBC で同じ処理を書くのと比べると、９割以上のコードが省略できると思います。 MyBatis は SQL にフォーカスし、可能な限りあなたの邪魔をしないように設計されています。

Mapper XML ファイルの第一階層の要素は下記のとおりです（この順番で定義する必要があります）。

- `cache` – 指定されたネームスペースに対するキャッシュの設定です。
- `cache-ref` – 別のネームスペースで定義されているキャッシュ設定を参照します。
- `resultMap` – データベースから取得した結果セットを Java オブジェクトにマッピングするための情報を記述する、最も複雑で強力な要素です。
- <del>`parameterMap` – 非推奨！ パラメーターをマップする古い方法です。インラインパラメーターの使用が推奨されており、この要素は将来削除される予定です。ここでは解説しません。</del>
- `sql` – 他のステートメントから参照することができる、再利用可能な SQL 文字列です。
- `insert` – マップされた INSERT ステートメントです。
- `update` – マップされた UPDATE ステートメントです。
- `delete` – マップされた DELETE ステートメントです。
- `select` – マップされた SELECT ステートメントです。

次の章では、それぞれの要素について詳しく説明していきます。 まずはステートメントからです。

## select

select ステートメントは、MyBatis で最も頻繁に使われる要素のひとつです。 データを取り出すことができてはじめてデータベースにデータを追加する意味があるので、ほとんどのアプリケーションではデータを変更するよりも検索する回数の方が多くなります。 insert, update, delete のそれぞれに対して、多くの select があるはずです。 これは MyBatis の大原則の一つであり、クエリ発行と結果のマッピングに注力している理由でもあります。 シンプルなケースでは、select 要素は非常に簡単です。１つ例を挙げましょう。

```xml
<select id="selectPerson" parameterType="int" resultType="hashmap">
  SELECT * FROM PERSON WHERE ID = #{id}
</select>
```

これは selectPerson というステートメントで、int （または Integer）型の引数を取り、列名を key、値を value として保持する HashMap を返します。

パラメーターは次のように記述されています。

```java
#{id}
```

このように記述すると、MyBatis は PreparedStatement のパラメーターを作成します。 JDBC では PreparedStatement を作成する場合の \'?\' に相当します。

```java
// Similar JDBC code, NOT MyBatis…
String selectPerson = "SELECT * FROM PERSON WHERE ID=?";
PreparedStatement ps = conn.prepareStatement(selectPerson);
ps.setInt(1,id);
```

JDBC 単体で select 結果を展開してオブジェクトのインスタンスにマップするためにはもっと多くのコードが必要ですが、MyBatis を使えばそうしたコードは書かずに済みます。 パラメーターと結果のマッピングについては他にも知っておくべきことが数多くあります。 これらについては、後ほどそれぞれに独立した章を用意して説明します。

ステートメントに関して細かい設定ができるように、select 要素には多くの属性があります。

```xml
<select
  id="selectPerson"
  parameterType="int"
  parameterMap="deprecated"
  resultType="hashmap"
  flushCache="false"
  useCache="true"
  timeout="10"
  fetchSize="256"
  statementType="PREPARED"
  resultSetType="FORWARD_ONLY">
```

| 属性 | 説明 |
| --- | --- |
| id | このネームスペース内で固有な識別子。ステートメントを参照する際に使用します。 |
| parameterType | このステートメントに渡される引数の型。完全修飾クラス名またはエイリアス。TypeHandler は実際の引数に応じて自動的に導出されるため、この属性は省略可能です。デフォルト値：未設定 |
| <del>`parameterMap`</del> | <del>別の場所で定義されている `parameterMap` を参照する非推奨の方法。インラインパラメーターマッピングと `parameterType` 属性を使用してください。</del> |
| resultType | このステートメントから返されるオブジェクトの型。完全修飾クラス名またはエイリアス。ステートメントがコレクションを返す場合は、コレクションの型ではなくコレクションに含まれるオブジェクトの型を指定する必要があります。<br /> `resultType` と `resultMap` は、どちらか一方のみ指定可能です。 |
| resultMap | 別の場所で定義されている `resultMap` を参照します。 Result Map は MyBatis の中でも最も強力な機能で、深く理解すれば複雑なマッピングが必要となる様々なケースに対応することができます。<br /> `resultType` と `resultMap` は、どちらか一方のみ指定可能です。 |
| flushCache | true を指定した場合、ステートメント実行時にローカルキャッシュおよび２次キャッシュがフラッシュ（＝クリア）されます。select ステートメントの場合、デフォルト値は `false` です。 |
| useCache | true を指定した場合、ステートメントの結果が２次キャッシュに保存されます。select ステートメントの場合、デフォルト値は `true` です。 |
| timeout | ドライバーがデータベースからの応答が戻らない場合に、ドライバーが例外を投げるまでの最大待機時間（単位：秒）を設定します。デフォルトは未設定（ドライバー依存）です。 |
| fetchSize | ドライバーが結果を返す際に内部的に使用するキャッシュのサイズを指定します（ドライバーに対するヒントです）。デフォルトは未指定（ドライバー依存）です。 |
| statementType | MyBatis がクエリを実行する際に使用する Statement の種類を指定します。設定可能な値は `STATEMENT`, `PREPARED`, `CALLABLE` で、それぞれ `Statement`, `PreparedStatement`, `CallableStatement` が使用されます。デフォルトは `PREPARED` です。 |
| `resultSetType` | `FORWARD_ONLY`, `SCROLL_SENSITIVE`, `SCROLL_INSENSITIVE`, `DEFAULT` のいずれかを指定します。未指定の場合は設定済みの `defaultResultSetType` が適用され、`DEFAULT` を指定すると JDBC ドライバーのデフォルトが使用されます。 |
| `databaseId` | databaseIdProvider が設定されている場合、MyBatis は定義されているステートメントの中で `databaseId` 属性が指定されていないステートメントおよび現在の設定と一致する `databaseId` 属性を持ったステートメントをロードします。 同じステートメントで、`databaseId` 属性が指定されているものと指定されていないものが両方定義されていた場合、指定がないステートメントは無視されます。 |
| `resultOrdered` | この属性は結果がネストされた select ステートメントでのみ有効です。true が設定された場合、MyBatis はクエリの結果が正しい順番にソートされているという前提でマッピングを実行します。これによりメモリ消費を抑えることができます。 デフォルトは `false` です。 |
| `resultSets` | 複数の ResultSet を利用する場合にのみ有効です。ステートメントが返す ResultSet にそれぞれ任意の名前を付けてリストアップします。名前はカンマで区切ります。 |
| `affectData` | ResultSet を返す INSERT, UPDATE, DELETE 文を記述する場合に true をセットします。これによりトランザクション制御が正しく実行されるようになります。[トランザクションを制御するメソッド](./java-api.html#transaction-control-methods) も参照してください。 デフォルト： `false` （3.5.12 以降） |
[Select Attributes]

## insert, update and delete

<a id="insert_update_and_delete"></a>

データを変更するステートメントである insert, update, delete は、非常によく似た実装となっています。

```xml
<insert
  id="insertAuthor"
  parameterType="domain.blog.Author"
  flushCache="true"
  statementType="PREPARED"
  keyProperty=""
  keyColumn=""
  useGeneratedKeys=""
  timeout="20">

<update
  id="updateAuthor"
  parameterType="domain.blog.Author"
  flushCache="true"
  statementType="PREPARED"
  timeout="20">

<delete
  id="deleteAuthor"
  parameterType="domain.blog.Author"
  flushCache="true"
  statementType="PREPARED"
  timeout="20">
```

| 属性 | 説明 |
| --- | --- |
| id | このネームスペース内で固有な識別子。ステートメントを参照する際に使用します。 |
| parameterType | このステートメントに渡される引数の型。完全修飾クラス名またはエイリアス。TypeHandler は実際の引数に応じて自動的に導出されるため、この属性は省略可能です。デフォルト値：未設定 |
| <del>`parameterMap`</del> | <del>別の場所で定義されている parameterMap を参照する非推奨の方法。インラインパラメーターマッピングと parameterType 属性を使用してください。</del> |
| flushCache | true を指定した場合、ステートメント実行時にローカルキャッシュおよび２次キャッシュがフラッシュ（＝クリア）されます。insert, update, delete ステートメントの場合、デフォルト値は `true` です。 |
| timeout | ドライバーがデータベースからの応答が戻らない場合に、ドライバーが例外を投げるまでの最大待機時間を設定します。デフォルトは未設定（ドライバー依存）です。 |
| statementType | MyBatis がクエリを実行する際に使用する Statement の種類を指定します。設定可能な値は STATEMENT, PREPARED, CALLABLE で、それぞれ Statement, PreparedStatement, CallableStatement が使用されます。デフォルトは PREPARED です。 |
| useGeneratedKeys | （insert, update のみ）MyBatis に対して、JDBC の getGeneratedKeys メソッドを使ってデータベース側で自動生成されたキーを取得するよう指示します（例えば MySQL や SQL Server のような RDBMS における auto increment が設定された列の値が取得できます）。デフォルト値は false です。 |
| keyProperty | （insert, update のみ）getGeneratedKeys メソッドまたは selectKey 要素で指定されたクエリによって取得したキーの値は、ここで指定したプロパティにセットされます。カンマ区切りでの複数指定にも対応しています。 |
| keyColumn | （insert, update のみ）テーブル内で自動生成が設定されている列の名前を指定します。この設定は、特定のデータベース（例えば PostgreSQL）で、テーブルの先頭以外の列にキーの自動生成が設定されている場合にのみ必要となります。カンマ区切りでの複数指定にも対応しています。 |
| `databaseId` | databaseIdProvider が設定されている場合、MyBatis は定義されているステートメントの中で `databaseId` 属性が指定されていないステートメントおよび現在の設定と一致する `databaseId` 属性を持ったステートメントをロードします。 同じステートメントで、`databaseId` 属性が指定されているものと指定されていないものが両方定義されていた場合、指定がないステートメントは無視されます。 |
[Insert, Update and Delete Attributes]

以下、insert, update, delete ステートメントの例をいくつか挙げておきます。

```xml
<insert id="insertAuthor">
  insert into Author (id,username,password,email,bio)
  values (#{id},#{username},#{password},#{email},#{bio})
</insert>

<update id="updateAuthor">
  update Author set
    username = #{username},
    password = #{password},
    email = #{email},
    bio = #{bio}
  where id = #{id}
</update>

<delete id="deleteAuthor">
  delete from Author where id = #{id}
</delete>
```

前述のように、insert は自動生成されたキーを扱うため、追加の属性や子要素を持つことができるようになっています。

お使いのデータベースがキーの自動生成に対応しているなら（MySQL や SQL Server は対応しています）、単純に useGeneratedKeys に true を設定し、keyPropoerty でキーが格納されるプロパティを指定するだけです。 例えば上記の Author テーブルで id 列に自動生成が設定されていた場合、ステートメントは次のように書くことができます。

```xml
<insert id="insertAuthor" useGeneratedKeys="true"
    keyProperty="id">
  insert into Author (username,password,email,bio)
  values (#{username},#{password},#{email},#{bio})
</insert>
```

複数行の一括挿入に対応したデータベースなら、 `Author` のリストまたは配列を引数として渡して自動生成された id の値を一括取得することも可能です。

```xml
<insert id="insertAuthor" useGeneratedKeys="true"
    keyProperty="id">
  insert into Author (username, password, email, bio) values
  <foreach item="item" collection="list" separator=",">
    (#{item.username}, #{item.password}, #{item.email}, #{item.bio})
  </foreach>
</insert>
```

主にキーの自動生成に対応していないデータベースやドライバーのため、MyBatis はもうひとつ別の方法を提供しています。

全く実用的ではありませんが、MyBatis の柔軟さを示す意味も込めて、ID をランダムに生成する例を挙げておきます。

```xml
<insert id="insertAuthor">
  <selectKey keyProperty="id" resultType="int" order="BEFORE">
    select CAST(RANDOM()*1000000 as INTEGER) a from SYSIBM.SYSDUMMY1
  </selectKey>
  insert into Author
    (id, username, password, email,bio, favourite_section)
  values
    (#{id}, #{username}, #{password}, #{email}, #{bio}, #{favouriteSection,jdbcType=VARCHAR})
</insert>
```

上記の例では、最初に selectKey ステートメントが実行され、Author オブジェクトの id プロパティに生成された乱数がセットされます。 次に、insert ステートメントが実行されます。 こうすることで、Java 側でややこしいコードを書かなくても自動生成と同様の結果を得ることができるようになっています。

selectKey 要素は次の属性を持っています。

```xml
<selectKey
  keyProperty="id"
  resultType="int"
  order="BEFORE"
  statementType="PREPARED">
```

| 属性 | 説明 |
| --- | --- |
| keyProperty | selectKey ステートメントの結果がセットされるプロパティを指定します。カンマ区切りでの複数指定にも対応しています。 |
| keyColumn | プロパティに対応する結果セット中の列名を指定します。カンマ区切りでの複数指定にも対応しています。 |
| resultType | 結果の型を指定します。MyBatis は `selectKey` の結果型を自動的には推測しないため、明示的な指定が必要です。文字列も含めて、任意の単純型を指定することができます。複数の列が自動生成対象となっている場合、対応するプロパティを持つクラスあるいは Map を指定することができます。 |
| order | BEFORE または AFTER が指定可能です。BEFORE を指定した場合、最初に selectKey を実行して keyProperty に取得したキーを設定し、その後で insert を実行します。 AFTER を指定した場合、先に insert を実行してから selectKey ステートメントを実行します。 後者は、Oracle のように insert ステートメントの中に sequence の呼び出しを含むようなケースで良く使われます。 |
| statementType | select などと同じく Statement の種類を指定することができます。設定可能な値は STATEMENT, PREPARED, CALLABLE で、それぞれ Statement, PreparedStatement, CallableStatement が使用されます。 |
[selectKey Attributes]

例外として、INSERT, UPDATE, DELETE 文から ResultSet を返す SQL 文（PostgreSQL, MariaDB の `RETURNING` , MS SQL Server の `OUTPUT` など）で結果をマップするためには `<select />` を使用する必要があります。

```xml
<select id="insertAndGetAuthor" resultType="domain.blog.Author"
      affectData="true" flushCache="true">
  insert into Author (username, password, email, bio)
  values (#{username}, #{password}, #{email}, #{bio})
  returning id, username, password, email, bio
</select>
```

## sql

この要素を使うと、再利用可能な SQL コードのスニペットを定義しておいて、他のステートメントにインクルードすることができます。このときパラメータを指定することもできますが、インクルードの解決はロード時に行われるので、静的な文字列のみ指定可能です。<br /> 例：

```xml
<sql id="userColumns"> ${alias}.id,${alias}.username,${alias}.password </sql>
```

上記の SQL スニペットを他のステートメントにインクルードするには、次のように記述します。

```xml
<select id="selectUsers" resultType="map">
  select
    <include refid="userColumns"><property name="alias" value="t1"/></include>,
    <include refid="userColumns"><property name="alias" value="t2"/></include>
  from some_table t1
    cross join some_table t2
</select>
```

呼び出し側の property 要素で指定された値を、sql 要素に内包された include 要素の refid 属性や property 要素の value 属性として指定することも可能です。<br /> 例：

```xml
<sql id="sometable">
  ${prefix}Table
</sql>

<sql id="someinclude">
  from
    <include refid="${include_target}"/>
</sql>

<select id="select" resultType="map">
  select
    field1, field2, field3
  <include refid="someinclude">
    <property name="prefix" value="Some"/>
    <property name="include_target" value="sometable"/>
  </include>
</select>
```

## Parameters

これまでのステートメントの例では、すべて単純なパラメーターを利用していました。パラメーターは非常に強力な要素です。単純な用途（たぶん全用途の９割くらい）については、特筆すべきことはありません。<br /> 例：

```xml
<select id="selectUsers" parameterType="int" resultType="User">
  select id, username, password
  from users
  where id = #{id}
</select>
```

上記は、名前を指定してパラメーターを参照するごく簡単な例です。parameterType が int と指定されていますので、好きな名前を使って参照することができます。プリミティブ型や Integer, String などの単純型はプロパティを持たないので、引数の参照はパラメーターの値そのものに置き換えられます。<br /> パラメーターとして複合型を渡した場合はもう少し複雑です。次の例を見てください。

```xml
<insert id="insertUser" parameterType="User">
  insert into users (id, username, password)
  values (#{id}, #{username}, #{password})
</insert>
```

User 型のオブジェクトがパラメーターとしてステートメントに渡されると、id, username, password の各プロパティが参照され、その値が PreparedStatement の引数として渡されます。

このように、ステートメントにパラメーターを渡すだけならとても簡単です。しかし、パラメーターマップには更にいろいろな機能があります。

まず、他と同じ様に、より具体的に型を指定することができます。

```java
#{property,javaType=int,jdbcType=NUMERIC}
```

他と同様、ほとんどの場合 javaType は自動的に検出されますが、例外は HashMap です。パラメーターとして HashMap を使う場合は、正しい TypeHandler が使用されるよう javaType を指定してください。

<span class="label important">重要</span> JDBC の仕様上、値として null を設定する場合は JDBC タイプが必須となります。 詳しくは PreparedStatement.setNull() メソッドの javadoc を参照してください。

タイプハンドラーの完全修飾クラス名またはエイリアスを指定して、特定のタイプハンドラーを割り当てることもできます。

```java
#{age,javaType=int,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
```

だんだんゴチャゴチャしてきましたが、実際のところ、これらの属性を指定することは滅多にないと思います。

数値型に関しては、有効桁数を指定するための numericScale という属性もあります。

```java
#{height,javaType=double,jdbcType=NUMERIC,numericScale=2}
```

最後になりますが、mode 属性で IN, OUT, INOUT パラメーターを指定することができます。 OUT または INOUT の場合は、パラメーターオブジェクトのプロパティの値が変更されます。 mode が OUT または INOUT で jdbcType が CURSOR（Oracle の REFCURSOR）の場合は、ResultSet とパラメーターの型をマッピングする resultMap を指定する必要があります。ただし jdbcType として CURSOR を指定した場合 javaType には自動的に ResultSet が設定されますので、javaType の指定は省略可能です。

```java
#{department, mode=OUT, jdbcType=CURSOR, javaType=ResultSet, resultMap=departmentResultMap}
```

MyBatis ではより高度な struct のようなデータ型もサポートされていますが、OUT パラメーターを登録するときに型名を指定する必要があります。

```java
#{middleInitial, mode=OUT, jdbcType=STRUCT, jdbcTypeName=MY_TYPE}
```

このように多くの強力なオプションがあるわけですが、ほとんどの場合、単純にプロパティ名を指定すれば、後は MyBatis がよきに計らってくれるはずです。 あとは、null が設定可能な列に対して jdbcType を指定するくらいでしょう。

```java
#{firstName}
#{middleInitial,jdbcType=VARCHAR}
#{lastName}
```

文字列代入

デフォルトでは、#{} という表記を使うと PreparedStatement のプロパティが生成され、PreparedStatement の引数（? の部分）に対して安全な値が設定されるようになっています。 これは安全かつ高速なので、ほとんどすべてのケースで推奨される方法ですが、時には文字列をそのまま SQL 文に挿入した方が都合が良い場合があります。例えば ORDER BY 句は以下のように記述できます。

```java
ORDER BY ${columnName}
```

この場合、MyBatis は引数として渡された文字列を変更したりエスケープしたりしません。

文字列代入は、メタ情報(例：テーブル名やカラム名など)をSQLステートメントへ動的に埋め込みたい場合に非常に便利な仕組みです。 例えば、任意のカラム値に一致するレコードを取得する場合に、以下のようにカラム毎にメソッドを用意するのではなく、

```java
@Select("select * from user where id = #{id}")
User findById(@Param("id") long id);

@Select("select * from user where name = #{name}")
User findByName(@Param("name") String name);

@Select("select * from user where email = #{email}")
User findByEmail(@Param("email") String email);

// and more "findByXxx" method
```

指定したカラムの値に一致するレコードを取得するメソッドを一つ用意するだけで、同じことを実現することができます。

```java
@Select("select * from user where ${column} = #{value}")
User findByColumn(@Param("column") String column, @Param("value") Object value);
```

`${column}` は指定した文字列(カラム名)が直接代入され、`#{value}` はバインド変数として扱われるため、 以下のように使用することができます。

```java
User userOfId1 = userMapper.findByColumn("id", 1L);
User userOfNameKid = userMapper.findByColumn("name", "kid");
User userOfEmail = userMapper.findByColumn("email", "noone@nowhere.com");
```

この考え方はテーブル名にも適用することができます。

<span class="label important">重要</span> この方法を使って、ユーザーが入力した文字列を直接 SQL 文に代入するのは危険です。SQL インジェクションの原因となる可能性がありますので、ユーザーによる入力を受け付けないようにしておくか、常にプログラム側で独自にエスケープやチェックの処理を行うようにしてください。

## Result Maps

MyBatis の中で最も重要かつ強力なのが resultMap 要素です。resultMap のおかげで、JDBC を使って ResultSet からデータを取得する場合に書かなくてはならないコードの９割を省くことができ、またときには JDBC がサポートすらしていないことも可能となるのです。 実際、複雑なクエリの結果を結合してマッピングするコードを書く場合、数千行ものコードになってしまう場合もあります。 ResultMap は、簡単なステートメントなら明示的にマッピングな記述を省略でき、複雑なステートメントの場合でもオブジェクト同士の関連付けを行うための必要最小限のコードのみを記述すれば良いように設計されています。

明示的な resultMap を必要としない、簡単な Mapped Statement については既に例として出てきました。<br /> 例：

```xml
<select id="selectUsers" resultType="map">
  select id, username, hashedPassword
  from some_table
  where id = #{id}
</select>
```

この例の場合、resultType で指定された通り、すべての列が自動的に HashMap のキーとしてマップされます。 便利な時もありますが、HashMap は良いドメインモデルとは言えません。 ほとんどのアプリケーションではドメインモデルとして JavaBeans や POJO (Plain Old Java Objects) を使っていると思います。MyBatis はどちらもサポートしています。<br /> 次のような JavaBean があったとします。

```java
package com.someapp.model;
public class User {
  private int id;
  private String username;
  private String hashedPassword;

  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }
  public String getHashedPassword() {
    return hashedPassword;
  }
  public void setHashedPassword(String hashedPassword) {
    this.hashedPassword = hashedPassword;
  }
}
```

JavaBeans の仕様によれば、上記のクラスは３つのプロパティ id, username, hashedPassword を持っているということになります。 これらのプロパティは、先に挙げた select ステートメントの中で指定されている列名と一致しています。

このような条件を満たす JavaBean であれば、HashMap のときと同じように ResultSet にマップすることができます。

```xml
<select id="selectUsers" resultType="com.someapp.model.User">
  select id, username, hashedPassword
  from some_table
  where id = #{id}
</select>
```

タイプエイリアスを使えば何度も完全修飾クラス名を入力しなくて済む、ということも思い出してください。

```xml
<!-- In Config XML file -->
<typeAlias type="com.someapp.model.User" alias="User"/>

<!-- In SQL Mapping XML file -->
<select id="selectUsers" resultType="User">
  select id, username, hashedPassword
  from some_table
  where id = #{id}
</select>
```

こうしたケースでは、名前に基づいて列を JavaBean にマップするための ResultMap が MyBatis によって自動的に作成されます。 もし列名が一致しない場合、select 文で列に別名をつけることで対応可能です（列の別名は標準 SQL の機能です）。

```xml
<select id="selectUsers" resultType="User">
  select
    user_id     as "id",
    user_name     as "userName",
    hashed_password   as "hashedPassword"
  from some_table
  where id = #{id}
</select>
```

ResultMap の素晴らしいところは、まだ一つの例も挙げていないにも関わらず、あなたは既にその多くを学んでいるということです。 これらのシンプルなケースでは既に見てきた以上のことは何も必要ありません。 列名の不一致を解消するもう一つの方法でもありますが、この最後のサンプルで resultMap を明示的に定義したらどうなるか見てみましょう。

```xml
<resultMap id="userResultMap" type="User">
  <id property="id" column="user_id" />
  <result property="username" column="user_name"/>
  <result property="hashedPassword" column="hashed_password"/>
</resultMap>
```

そして、resultMap 属性を使って定義した ResultMap を参照するステートメントは以下のようになります（resultType 属性は削除しています）。

```xml
<select id="selectUsers" resultMap="userResultMap">
  select user_id, user_name, hashed_password
  from some_table
  where id = #{id}
</select>
```

全てがこんなに簡単なら良いのでしょうが、そう甘くはありません。

## 高度な結果マッピング

MyBatis は「データベースは必ずしも希望通りに定義されている訳ではない」という思想に基づいて設計されています。 すべてのデータベースが完全な第三正規形あるいは BCNF なら最高ですが、実際はそうではありません。 また、たったひとつで全てのアプリケーションに適合できるようなデータベースを作ることができたら素晴らしいですが、これも現実とは異なります。 こうした問題に対する MyBatis の答えが Result Map です。

例えば、こんなステートメントをどうやってマップすれば良いのでしょう？

```xml
<!-- Very Complex Statement -->
<select id="selectBlogDetails" resultMap="detailedBlogResultMap">
  select
    B.id as blog_id,
    B.title as blog_title,
    B.author_id as blog_author_id,
    A.id as author_id,
    A.username as author_username,
    A.password as author_password,
    A.email as author_email,
    A.bio as author_bio,
    A.favourite_section as author_favourite_section,
    P.id as post_id,
    P.blog_id as post_blog_id,
    P.author_id as post_author_id,
    P.created_on as post_created_on,
    P.section as post_section,
    P.subject as post_subject,
    P.draft as draft,
    P.body as post_body,
    C.id as comment_id,
    C.post_id as comment_post_id,
    C.name as comment_name,
    C.comment as comment_text,
    T.id as tag_id,
    T.name as tag_name
  from Blog B
    left outer join Author A on B.author_id = A.id
    left outer join Post P on B.id = P.blog_id
    left outer join Comment C on P.id = C.post_id
    left outer join Post_Tag PT on PT.post_id = P.id
    left outer join Tag T on PT.tag_id = T.id
  where B.id = #{id}
</select>
```

あなたはおそらく、Author によって書かれた、たくさんの Post を持つ Blog といったようなオブジェクトモデルにマップしたいと考えるでしょう。 次に上げるのは、複雑な ResultMap の完全な例です（Author, Blog, Post, Comments, Tags はすべてタイプエイリアスとします）。 後ほどひとつずつ説明しますが、とりあえず目を通してみてください。 最初は難解に思えるかも知れませんが、実際は非常にシンプルです。

```xml
<!-- Very Complex Result Map -->
<resultMap id="detailedBlogResultMap" type="Blog">
  <constructor>
    <idArg column="blog_id" javaType="int"/>
  </constructor>
  <result property="title" column="blog_title"/>
  <association property="author" javaType="Author">
    <id property="id" column="author_id"/>
    <result property="username" column="author_username"/>
    <result property="password" column="author_password"/>
    <result property="email" column="author_email"/>
    <result property="bio" column="author_bio"/>
    <result property="favouriteSection" column="author_favourite_section"/>
  </association>
  <collection property="posts" ofType="Post">
    <id property="id" column="post_id"/>
    <result property="subject" column="post_subject"/>
    <association property="author" javaType="Author"/>
    <collection property="comments" ofType="Comment">
      <id property="id" column="comment_id"/>
    </collection>
    <collection property="tags" ofType="Tag" >
      <id property="id" column="tag_id"/>
    </collection>
    <discriminator javaType="int" column="draft">
      <case value="1" resultType="DraftPost"/>
    </discriminator>
  </collection>
</resultMap>
```

resultMap 要素には多くの子要素や階層構造があり、それぞれについて多少説明が必要です。 以下は resultMap 要素の構成要素です。

#### resultMap

- `constructor` - クエリ結果をコンストラクタに渡してクラスのインスタンスを作成する場合に使用します。
  - `idArg` - このコンストラクタ引数が ID であることを明示します。ID 列を明示することによって全体のパフォーマンスが向上します。
  - `arg` - 通常のコンストラクタ引数です。
- `id` – このフィールドまたはプロパティが ID であることを明示します。ID を明示することによって全体のパフォーマンスが向上します。
- `result` – フィールドまたは JavaBean のプロパティにセットされる通常のデータです。
- `association` – 複雑型のアソシエーションを定義します。多くのマッピングはこのタイプに当てはまります。
  - ネストされた Result Map – アソシエーション自身も resultMap として記述します。他で定義されている resultMap を参照することもできます。
- `collection` – 複雑型のコレクションを定義します。
  - ネストされた Result Map – コレクション自身も resultMap として記述します。他で定義されている resultMap を参照することもできます。
- `discriminator` – 結果の値を使ってどの resultMap を使用するか決定します。
  - `case` – case は Result Map の一種で、値が一致した場合に使用されます。
    - ネストされた Result Map – case も Result Map なので、resultMap とほぼ同じ要素を持つことができます。また、別の場所で定義された resultMap を参照することもできます。

| 属性 | 説明 |
| --- | --- |
| `id` | このネームスペース内で固有の識別子。resultMap を参照する際に使用します。 |
| `type` | Java クラスの完全修飾クラス名またはタイプエイリアスを指定します（前に出てきた標準のタイプエイリアスのリストを参照してください）。 |
| `autoMapping` | この resulMap に結果をマッピングする際、自動マッピングを使用するかどうかを true / false で指定します。ここでの指定はグローバルな設定（autoMappingBehavior）より優先されます。デフォルト：未指定 |
[ResultMap Attributes]

<span class="label important">ベストプラクティス</span> ResultMap は段階的に実装するようにしましょう。ユニットテストも効果的です。 上で見たような巨大な resultMap を一度に実装しようとするとミスが発生しやすく、作業も困難です。 シンプルなところからスタートして、ユニットテストを書きながら徐々に拡張しましょう。 フレームワークの欠点でもありますが、ソースが公開されているかどうかに関わらず、フレームワークはある種のブラックボックスとなり得ます。期待通りに動作していることを確かめるにはユニットテストが一番です。 ユニットテストはバグを報告する場合も役に立ちます。

次の章ではそれぞれの要素について詳しく見ていきます。

#### id と result

```xml
<id property="id" column="post_id"/>
<result property="subject" column="post_subject"/>
```

<i>id</i> と <i>result</i> は結果マッピングの基本となる要素です。 どちらも、ある列の値を単純型（String, int, double, Date など）のプロパティまたはフィールドにマップするときに使用します。

両者の違いは、<i>id</i> は、その結果が識別子プロパティ（オブジェクトのインスタンスを比較する際に使われます）であることを表すという点のみです。 これによって全体的なパフォーマンスが向上しますが、特に恩恵をうけるのはキャッシュとネストされた結果マッピング（＝JOIN マッピング）の処理を行うときです。

どちらも多くの属性を持っています。

| 属性 | 説明 |
| --- | --- |
| property | 結果列の値をマップするフィールドまたはプロパティを指定します。名前が一致する JavaBean プロパティがあれば、そのプロパティが使われます。もしなければ、MyBatis は次に名前が一致するフィールドを探します。 どちらの場合も、ドット表記を使って複雑型のプロパティを指定することができます。 例えば、 \"username\" のような単純なプロパティを指定することもできますし、 \"address.street.number\" のように複雑なプロパティを指定することもできます。 |
| column | データベースで定義されている列名、あるいは列の別名を指定します。resultSet.getString(columnName) メソッドの引数として渡される文字列と同じと考えてください。 |
| javaType | Java クラスの完全修飾クラス名またはタイプエイリアスを指定します（前に出てきた標準のタイプエイリアスのリストを参照してください）。 通常、JavaBean にマッピングする場合の javaType は MyBatis によって正しく判別されます。 ただし、HashMap にマッピングする場合は正しい動作を保証するため適切な javaType を指定するようにしてください。 |
| jdbcType | 結果列の JDBC データ型を指定します。MyBatis がこのマッピングに用いる TypeHandler を Java 型と合わせて選択する際に必要な場合があります。 |
| typeHandler | デフォルトのタイプハンドラーについては既に説明しました。 このプロパティを使うと、デフォルトのタイプハンドラーをオーバーライドすることができます。 タイプハンドラーの完全修飾クラス名またはタイプエイリアスを指定します。 |
[id and result Attributes]

#### サポートされている JDBC データ型

参考として、MyBatis に含まれている JdbcType 列挙型によってサポートされている JDBC データ型を以下に挙げておきます。

<table>
          <tr>
            <td>BIT</td>
            <td>FLOAT</td>
            <td>CHAR</td>
            <td>TIMESTAMP</td>
            <td>OTHER</td>
            <td>UNDEFINED</td>
          </tr>
          <tr>
            <td>TINYINT</td>
            <td>REAL</td>
            <td>VARCHAR</td>
            <td>BINARY</td>
            <td>BLOB</td>
            <td>NVARCHAR</td>
          </tr>
          <tr>
            <td>SMALLINT</td>
            <td>DOUBLE</td>
            <td>LONGVARCHAR</td>
            <td>VARBINARY</td>
            <td>CLOB</td>
            <td>NCHAR</td>
          </tr>
          <tr>
            <td>INTEGER</td>
            <td>NUMERIC</td>
            <td>DATE</td>
            <td>LONGVARBINARY</td>
            <td>BOOLEAN</td>
            <td>NCLOB</td>
          </tr>
          <tr>
            <td>BIGINT</td>
            <td>DECIMAL</td>
            <td>TIME</td>
            <td>NULL</td>
            <td>CURSOR</td>
            <td>ARRAY</td>
          </tr>
          <tr>
            <td>STRUCT</td>
            <td>JAVA_OBJECT</td>
            <td>DISTINCT</td>
            <td>REF</td>
            <td>DATALINK</td>
            <td>ROWID</td>
          </tr>
          <tr>
            <td>LONGNVARCHAR</td>
            <td>SQLXML</td>
            <td>DATETIMEOFFSET</td>
            <td>TIME_WITH_TIMEZONE</td>
            <td>TIMESTAMP_WITH_TIMEZONE</td>
            <td></td>
          </tr>
        </table>

#### constructor

ほとんどの DTO (Data Transfer Object) やドメインモデルではプロパティを使って値を設定することができますが、時にはイミュータブルなクラスを使いたい場合もあるかも知れません。 通常更新されることのない参照用データを含むテーブルなどはイミュータブルクラスに向いています。 コンストラクタインジェクションを使うとインスタンス化の際に値を設定することができるので、カプセル化の妨げとなる public メソッドの定義が不要となります。 MyBatis は private として定義されているプロパティやフィールドもサポートしていますが、コンストラクタインジェクションを好む人もいると思います。 <i>constructor</i> 要素を使うとコンストラクタインジェクションによって値をマップすることが可能となります。

次のようなコンストラクタを考えてみます。

```java
public class User {
   //...
   public User(Integer id, String username, int age) {
  //...
  }
//...
}
```

コンストラクタ経由で値をマップするためには、指定された引数にマッチするコンストラクタを特定する必要があります。 下記の例では、MyBatis は３つの引数 `java.lang.Integer`, `java.lang.String`, `int` をこの順番で持つコンストラクタを探します。

```xml
<constructor>
   <idArg column="id" javaType="int"/>
   <arg column="username" javaType="String"/>
   <arg column="age" javaType="_int"/>
</constructor>
```

上記の指定方法では引数の型を順番通りに並べる必要がありますが、引数の多いコンストラクタを扱うのには向いていません。<br /> 3.4.3 以降では、引数名を指定することによって arg 要素を順不同で記述できるようになりました。引数を名前で指定するためには、各引数に `@Param` アノテーションを追加するか、プロジェクトを \'-parameters\' オプション付きでコンパイルし、`useActualParamName` に true （デフォルト値です）を設定します。 下記の指定では２番めと３番目の引数の順番がコンストラクタの宣言と異なりますが、引数名が指定されているので正しく動作します。

```xml
<constructor>
   <idArg column="id" javaType="int" name="id" />
   <arg column="age" javaType="_int" name="age" />
   <arg column="username" javaType="String" name="username" />
</constructor>
```

引数と同じ名前、同じ型を持つ書き込み可能なプロパティが存在する場合 `javaType` は省略可能です。

それ以外の属性とルールについては通常の id, result 要素と同じです。

| 属性 | 説明 |
| --- | --- |
| column | データベースで定義されている列名、あるいは列の別名を指定します。resultSet.getString(columnName) メソッドの引数として渡される文字列と同じと考えてください。 |
| javaType | Java クラスの完全修飾クラス名またはタイプエイリアスを指定します（前に出てきた標準のタイプエイリアスのリストを参照してください）。 通常、JavaBean にマッピングする場合の javaType は MyBatis によって正しく判別されます。 ただし、HashMap にマッピングする場合は正しい動作を保証するため適切な javaType を指定するようにしてください。 |
| jdbcType | 結果列の JDBC データ型を指定します。MyBatis がこのマッピングに用いる TypeHandler を Java 型と合わせて選択する際に必要な場合があります。 |
| typeHandler | デフォルトのタイプハンドラーについては既に説明しました。 このプロパティを使うと、デフォルトのタイプハンドラーをオーバーライドすることができます。 タイプハンドラーの完全修飾クラス名またはタイプエイリアスを指定します。 |
| select | 別の場所で定義されているステートメントの ID を指定します。 ステートメントの結果として取得したデータがこのプロパティにマップされます。 指定された select ステートメントには column 属性で指定した列の値が引数として渡されます。<br /> 詳細は association 要素の説明を参照してください。 |
| resultMap | 別の場所で定義されている ResultMap の ID を指定します。ネストされた結果をこのプロパティにマップする際、指定した ResultMap が使われます。 この方法は、別の select 文を実行する方法の代わりに使うことができ、複数のテーブルを結合して取得した ResultSet をマップすることができます。 このような ResultSet には重複や繰り返しが含まれているため、正しく分解してからネストされたオブジェクトグラフにマップしてやる必要がありますが、MyBatis ではこの処理を実現するため Result Map を連結できるようになっています。<br /> 詳細は association 要素の説明を参照してください。 |
| `name` | コンストラクタ引数の名前を指定します。引数名を指定することで arg 要素を順不同で記述できるようになります。詳細は上記の説明を参照してください。 導入されたバージョン: 3.4.3 |

##### コレクションや複雑なオブジェクトを引数に含むコンストラクタに対して、ネストされた結果をマッピングする

以降のセクションで `association` と `collection` について説明しますが、バージョン 3.6.0 以降ではコンストラクタの引数としてコレクションを指定できるようになりました。

次のクラスを例にして説明します。

```java
public class User {
   //...
   public User(Integer id, String username, List<UserRole> userRoles) {
     //...
   }
}

public class UserRole {
    // ...
    public UserRole(Integer id, String role) {
      // ...
    }
}
```

クエリがネストされた結果を返す場合、`User` のコンストラクタ引数に含まれているコレクション `List<UserRole>` をマッピングするためには、下記のような ResultMap を定義します。

MyBatis は `UserRole` が全て読み込まれてから `User` のコンストラクタを呼び出します。つまり、`User` をイミュータブルなオブジェクトとして定義することもできるということです。

```xml
<resultMap id="userResultMap" type="User">
  <constructor>
    <idArg column="id" javaType="int" />
    <arg column="username" javaType="String" />
    <arg javaType="List" resultMap="userRoleResultMap" columnPrefix="role_"/>
  </constructor>
</resultMap>
```

上記の ResultMap で参照している `UserRole` 用の ResultMap でもコンストラクタマッピングを使用すれば、`UserRole` もイミュータブルにできます。

```xml
<resultMap id="userRoleResultMap" type="UserRole">
  <constructor>
    <idArg column="id" javaType="int" />
    <arg column="role" javaType="String" />
  </constructor>
</resultMap>
```

このマッピングを実現するためには、結果セットが正しい順番でソートされていることが前提となるので、クエリで適切な `ORDER BY` 句を指定した上で、`<select>` 要素の `resultOrdered` 属性に `true` を設定してください。

```xml
<select id="getAllUsers" resultMap="userResultMap" resultOrdered="true">
    select
      u.id,
      u.username,
      r.id as role_id,
      r.role as role_role
    from user u
      left join user_role ur on u.id = ur.user_id
      left join role r on r.id = ur.role_id
    order by u.id, r.id
</select>
```

下記のように正しくソートされた結果セットが返ってくれば、期待通りにマッピングすることができます。

| row\_nr | u.id | u.username | r.id | r.role |
| --- | --- | --- | --- | --- |
| 1 | 1 | John | 1 | Admins |
| 2 | 1 | John | 2 | Users |
| 3 | 2 | Jack | null | null |
| 4 | 3 | Peter | 2 | Users |
| 5 | 3 | Peter | 3 | Maintainers |
| 6 | 3 | Peter | 4 | Approvers |

作成される `User` オブジェクトは下記のようになります。

```java
User{username=John, roles=[Admins, Users]}
User{username=Jack, roles=[]}
User{username=Peter, roles=[Users, Maintainers, Approvers]}
```

仮に、誤ったソート条件が指定されていて５行目のデータが１行目の直後に出現するようなクエリを実行してしまうと、全ての `UserRole` を読み込む前に `John` ユーザのコンストラクタが呼び出されてしまうので、期待通りの結果を得ることはできません。

コンストラクタ引数にコレクションを含むオブジェクトを使う場合は、完全にイミュータブルなオブジェクトとして定義してください。ResultMap にコンストラクタマッピングとプロパティマッピングが混在する ResultMap は正しく動作しない可能性があります。

#### association

```xml
<association property="author" column="blog_author_id" javaType="Author">
  <id property="id" column="author_id"/>
  <result property="username" column="author_username"/>
</association>
```

association 要素は \"has-one\" タイプのリレーションシップを扱います。 例えば、上で出てきたサンプルの Blog は１つの Author を持っています。 アソシエーションは、他の result とほぼ同じように動作します。 値を設定するプロパティ、値の取得元となる列、プロパティの javaType （ほとんどの場合自動で検出可能）、必要なら jdbcType、またデフォルトのタイプハンドラーをオーバーライドしたい場合は typeHandler を指定します。

association が他と異なるのは、アソシエーションを読み込む方法を MyBatis に伝える必要があるという点です。これには２つの方法があります。

- select をネストする：別のマップドステートメントを実行してその結果を読み込む方法です。
- 結果をネストする：ネストした Result Map を使って結合（Join）した結果を読み込む方法です。

まずは要素の属性を調べてみましょう。 select, resultMap 以外の属性は result 要素と同じです。

| 属性 | 説明 |
| --- | --- |
| property | 結果列の値をマップするフィールドまたはプロパティを指定します。名前が一致する JavaBean プロパティがあれば、そのプロパティが使われます。もしなければ、MyBatis は次に名前が一致するフィールドを探します。 どちらの場合も、ドット表記を使って複雑型のプロパティを指定することができます。 例えば、 \"username\" のような単純なプロパティを指定することもできますし、 \"address.street.number\" のように複雑なプロパティを指定することもできます。 |
| javaType | Java クラスの完全修飾クラス名またはタイプエイリアスを指定します（前に出てきた標準のタイプエイリアスのリストを参照してください）。 通常、JavaBean にマッピングする場合の javaType は MyBatis によって正しく判別されます。 ただし、HashMap にマッピングする場合は正しい動作を保証するため適切な javaType を指定するようにしてください。 |
| jdbcType | 結果列の JDBC データ型を指定します。MyBatis がこのマッピングに用いる TypeHandler を Java 型と合わせて選択する際に必要な場合があります。 |
| typeHandler | デフォルトのタイプハンドラーについては既に説明しました。 このプロパティを使うと、デフォルトのタイプハンドラーをオーバーライドすることができます。 タイプハンドラーの完全修飾クラス名またはタイプエイリアスを指定します。 |

#### ネストされた select を使って association を読み込む

| 属性 | 説明 |
| --- | --- |
| column | ネストされた select ステートメントに引数として渡される列名、あるいは列の別名を指定します。resultSet.getString(columnName) メソッドの引数として渡される文字列と同じと考えてください。 Note: 複合キー（composite key）を扱う場合、column=\"{prop1=col1,prop2=col2} のように記述することで複数の列名を指定することができます。この例ではネストされた select の実行時に prop1 と prop2 が引数として渡されます。 |
| select | 別の場所で定義されているステートメントの ID を指定します。 ステートメントの結果として取得したデータがこのプロパティにマップされます。 指定された select ステートメントには column 属性で指定した列の値が引数として渡されます。<br /> この表の後に詳しいサンプルがあります。 Note: 複合キー（composite key）を扱う場合、column=\"{prop1=col1,prop2=col2} のように記述することで複数の列名を指定することができます。この例ではネストされた select の実行時に prop1 と prop2 が引数として渡されます。 |
| `fetchType` | 任意の属性で、グローバルな `lazyLoadingEnabled` の設定をオーバーライドする場合に指定します。 指定可能な値は `lazy` または `eager` です。 |

例：

```xml
<resultMap id="blogResult" type="Blog">
  <association property="author" column="author_id" javaType="Author" select="selectAuthor"/>
</resultMap>

<select id="selectBlog" resultMap="blogResult">
  SELECT * FROM BLOG WHERE ID = #{id}
</select>

<select id="selectAuthor" resultType="Author">
  SELECT * FROM AUTHOR WHERE ID = #{id}
</select>
```

これだけです。 select ステートメントが二つありますが、一つは Blog、もう一つは Author を読み込むための selct です。 Blog の resultMap を見ると、author プロパティを読み込むために \"selectAuthor\" ステートメントを使うように指示されています。

他のプロパティは自動的に読み込まれます（列名とプロパティ名が一致していることが前提となります）。

この方法は簡単ですが、大量のデータやリストを扱うのには向いていません。 この問題は \"N+1 セレクト問題\" として知られています。 要約すると、\"N+1 セレクト問題\" が発生する仕組みは下記のようなものです。

- レコードのリストを取得するために SQL ステートメントを一つ実行します（これが \"+1\" です）。
- 結果に含まれる各レコードに対して、その詳細を取得するための select 文が一回ずつ実行されます（これが \"N\" です）。

場合によっては数百、数千もの SQL ステートメントが実行されることになるので、あまり良い方法とは言えません。

幸い MyBatis ではこのようなクエリに対して遅延読み込み（Lazy load）を設定することができるので、すべてのクエリが同時に実行されるような状況を避けることも可能です。 ただし、リストを読み込んだ直後に各要素を反復処理してネストされたデータにアクセスするような処理を行うと、すべての遅延読み込みが実行されるのでパフォーマンスが極端に悪化します。

では、もう一つの方法について説明しましょう。

#### ネストされた結果を使って association を読み込む

| 属性 | 説明 |
| --- | --- |
| resultMap | 別の場所で定義されている ResultMap の ID を指定します。ネストされた結果をこのプロパティにマップする際、指定した ResultMap が使われます。 この方法は、別の select 文を実行する方法の代わりに使うことができ、複数のテーブルを結合して取得した ResultSet をマップすることができます。 このような ResultSet には重複や繰り返しが含まれているため、正しく分解してからネストされたオブジェクトグラフにマップしてやる必要がありますが、MyBatis ではこの処理を実現するため Result Map を連結できるようになっています。<br /> 実際のサンプルを見た方が分かりやすいと思います。この表の後に例を挙げます。 |
| `columnPrefix` | 複数のテーブルを結合して取得した ResultSet では、列名の重複を防ぐため別名を付けなくてはならない場合があります。 columnPrefix を指定すると、別の場所で定義されている resultMap を使って別名の付けられた列をマップすることができます。後で説明するサンプルを参照してください。 |
| `notNullColumn` | ネストされた結果をマッピングする際、デフォルトでは子オブジェクトのプロパティに対応する列のうち一つでも null でない値がセットされているものがあれば子オブジェクトが生成されます。notNullColumn に列名（複数可）を指定しておくと、指定された列に null 以外の値がセットされた場合にのみ子オブジェクトが生成されます。デフォルト：未指定 |
| `autoMapping` | このプロパティに結果をマッピングする際、自動マッピングを使用するかどうかを true / false で指定します。ここでの指定はグローバルな設定（autoMappingBehavior）より優先されます。この設定は別の場所で定義されている ResultMap には適用されませんので、`select` や `resultMap` が指定されている場合は無効となります。デフォルト：未指定 |

ネストされたアソシエーションについては、既にとても複雑な例を見ていただきました。次のサンプルは、それと比べるとずっと理解しやすいと思います。ここでは、独立した select を実行する代わりに Blog と Author のテーブルを結合するクエリを実行します。

```xml
<select id="selectBlog" resultMap="blogResult">
  select
    B.id       as blog_id,
    B.title     as blog_title,
    B.author_id   as blog_author_id,
    A.id       as author_id,
    A.username    as author_username,
    A.password     as author_password,
    A.email     as author_email,
    A.bio       as author_bio
  from Blog B left outer join Author A on B.author_id = A.id
  where B.id = #{id}
</select>
```

テーブルを結合しているだけでなく、全ての列に固有で分かりやすい別名が指定されていることに注意してください。こうしておくとマッピング作業がずっと楽になります。<br /> これで結果をマップする準備が整いました。

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="blog_id" />
  <result property="title" column="blog_title"/>
  <association property="author" resultMap="authorResult"/>
</resultMap>

<resultMap id="authorResult" type="Author">
  <id property="id" column="author_id"/>
  <result property="username" column="author_username"/>
  <result property="password" column="author_password"/>
  <result property="email" column="author_email"/>
  <result property="bio" column="author_bio"/>
</resultMap>
```

この例では、Blog の \"author\" を読み込むためのアソシエーションで、別の resultMap \"authorResult\" を指定しています。

<span class="important">超重要</span> ネストされた Result Map では、id 要素が重要な役割を果たします。常に、結果の固有性を判定できる一つ以上のプロパティを id として指定するようにしてください。指定がなくても動作はしますが、パフォーマンスに著しい悪影響を与えます。また、結果の固有性を判定するために必要な最小限のプロパティを指定するようにしてください。主キー（複合キーでも可）は良い選択肢です。

上の例では、別の場所で定義した resultMap 要素を指定してアソシエーションを読み込んでいますが、こうすると Author の resultMap を再利用することができます。再利用する必要がない場合や、単に一つの resultMap にマッピングの情報をまとめておきたい場合には、アソシエーションの Result Map をネストすることもできます。この方法で書き直したのが次の例です。

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="blog_id" />
  <result property="title" column="blog_title"/>
  <association property="author" javaType="Author">
    <id property="id" column="author_id"/>
    <result property="username" column="author_username"/>
    <result property="password" column="author_password"/>
    <result property="email" column="author_email"/>
    <result property="bio" column="author_bio"/>
  </association>
</resultMap>
```

もしこの Blog に共著者 co-author が設定されていたらどうなるでしょうか。 select ステートメントは下記のようになります。

```xml
<select id="selectBlog" resultMap="blogResult">
  select
    B.id            as blog_id,
    B.title         as blog_title,
    A.id            as author_id,
    A.username      as author_username,
    A.password      as author_password,
    A.email         as author_email,
    A.bio           as author_bio,
    CA.id           as co_author_id,
    CA.username     as co_author_username,
    CA.password     as co_author_password,
    CA.email        as co_author_email,
    CA.bio          as co_author_bio
  from Blog B
  left outer join Author A on B.author_id = A.id
  left outer join Author CA on B.co_author_id = CA.id
  where B.id = #{id}
</select>
```

Author に対する resultMap は下記のように定義されています。

```xml
<resultMap id="authorResult" type="Author">
  <id property="id" column="author_id"/>
  <result property="username" column="author_username"/>
  <result property="password" column="author_password"/>
  <result property="email" column="author_email"/>
  <result property="bio" column="author_bio"/>
</resultMap>
```

結果に含まれる列名が resultMap で指定されている列名と異なるので、この resultMap を再利用するためには `columnPrefix` を指定する必要があります。

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="blog_id" />
  <result property="title" column="blog_title"/>
  <association property="author"
    resultMap="authorResult" />
  <association property="coAuthor"
    resultMap="authorResult"
    columnPrefix="co_" />
</resultMap>
```

#### ネストされたカーソルを association にマッピングする

データベースによっては列の値として `java.sql.ResultSet` 返すことができます。<br /> このような結果をマッピングする例を説明します。

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="id" />
  <result property="title" column="title" />
  <association property="author" column="author" jdbcType="CURSOR">
    <id property="id" column="id" />
    <result property="username" column="username" />
  </association>
</resultMap>

<select id="selectBlog" resultMap="blogResult">
  select b.id, b.title, cursor(
    select a.id, a.username from author a where b.author_id = a.id
  ) author from blog b
</select>
```

上の章の例との重要な違いは、`<association>` 要素の `jdbcType` 属性に `CURSOR` を指定している点です。<br /> これによって、`author` 列の値をネストされたカーソルとしてマッピングすることができます。

#### 複数の ResultSet を association にマッピングする

| 属性 | 説明 |
| --- | --- |
| `column` | 複数の ResultSet を返すステートメントをマッピングする場合、`foreignColumn` で指定した外部キーと対になってリレーションシップを構成する親オブジェクト側の列を指定します（カンマ区切りで複数指定可能）。 |
| `foreignColumn` | `column` 属性で指定した親オブジェクトの列との照合に使用される外部キーを格納する列を指定します。 |
| `resultSet` | 読み込み元の ResultSet の名前を指定します。下記の説明を参照してください。 |

バージョン 3.2.3 から、N+1 セレクト問題を解決する新しい方法が追加されました。

データベースによってはストアドプロシージャから複数の ResultSet を返すことができます。この機能を利用すると、テーブルの結合（Join）を使わず一度の問い合わせで互いに関連する複数のデータを取得することができます。

以下の例では、ストアドプロシージャが２つの ResultSet を返します。１つめの ResultSet には複数の Blog のデータが含まれており、２つめの ResultSet には複数の Author のデータが含まれています。

```sql
SELECT * FROM BLOG WHERE ID = #{id}

SELECT * FROM AUTHOR WHERE ID = #{id}
```

ストアドプロシージャを実行する select 要素の `resultSets` 属性で、それぞれの ResultSet に名前を付けておく必要があります（カンマ区切り）。ここでは、それぞれ blogs, authors としています。

```xml
<select id="selectBlog" resultSets="blogs,authors" resultMap="blogResult" statementType="CALLABLE">
  {call getBlogsAndAuthors(#{id,jdbcType=INTEGER,mode=IN})}
</select>
```

下記の resultMap では、authors 内の各 Author が、それぞれ対応する Blog にマッピングされます。

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="id" />
  <result property="title" column="title"/>
  <association property="author" javaType="Author" resultSet="authors" column="author_id" foreignColumn="id">
    <id property="id" column="id"/>
    <result property="username" column="username"/>
    <result property="password" column="password"/>
    <result property="email" column="email"/>
    <result property="bio" column="bio"/>
  </association>
</resultMap>
```

ここまで \"has one\" タイプのリレーションシップを扱う方法を見て来ましたが、\"has many\" の場合はどうしたら良いのでしょうか？これは次の章で説明します。

#### collection

```xml
<collection property="posts" ofType="domain.blog.Post">
  <id property="id" column="post_id"/>
  <result property="subject" column="post_subject"/>
  <result property="body" column="post_body"/>
</collection>
```

collection 要素の働きは association とほとんど同じです。同じ説明の繰り返しを省くため、association との違いにフォーカスして見て行きます。

引き続き上記のサンプルを使います。Blog に対する Author は一人だけですが、Post は複数存在します。この関係を Blog クラスで表現すると、次のようになると思います。

```java
private List<Post> posts;
```

collection 要素を使ってこのような List 型のプロパティにネストされた結果セットをマップする場合、select をネストする方法と、結果をネストする方法があります。

#### ネストされた select を使って collection を読み込む

まずは、ネストされた select を使って Blog に関連した Post を読み込む例を見てみましょう。

```xml
<resultMap id="blogResult" type="Blog">
  <collection property="posts" javaType="ArrayList" column="id" ofType="Post" select="selectPostsForBlog"/>
</resultMap>

<select id="selectBlog" resultMap="blogResult">
  SELECT * FROM BLOG WHERE ID = #{id}
</select>

<select id="selectPostsForBlog" resultType="Post">
  SELECT * FROM POST WHERE BLOG_ID = #{id}
</select>
```

いろいろ気づいたことがあると思いますが、上で見た association 要素と似た部分が多いと思います。 まず眼を引くのは collection 要素を使っていることでしょう。次に \"ofType\" という新しい属性があることに気づくでしょう。 この属性は、JavaBean のプロパティ（またはフィールド）の型と、コレクションに含まれている型を区別するために必要です。 したがって、このマッピングは次のように読むことができます。

```xml
<collection property="posts" javaType="ArrayList" column="id" ofType="Post" select="selectPostsForBlog"/>
```

<span class="important">このように読めます</span>: \"投稿のリストは、Post クラスを要素とする ArrayList に格納される。\"

ほとんどの場合、javaType 属性はMyBatis によって推測されるため省略可能です。 ということで、大抵は下記のように短く書くことができます。

```xml
<collection property="posts" column="id" ofType="Post" select="selectPostsForBlog"/>
```

#### ネストされた結果を使って collection を読み込む

ここまで来れば、ネストされた結果を collection に読み込む方法について想像できるのではないでしょうか。 なぜなら、association と全く同じだからです（ただし、ネストされた select と同様、\"ofType\" 属性の指定が必要です）。

まずは SQL を見てみましょう。

```xml
<select id="selectBlog" resultMap="blogResult">
  select
  B.id as blog_id,
  B.title as blog_title,
  B.author_id as blog_author_id,
  P.id as post_id,
  P.subject as post_subject,
  P.body as post_body
  from Blog B
  left outer join Post P on B.id = P.blog_id
  where B.id = #{id}
</select>
```

Blog と Post テーブルを結合した上で、マッピングを簡単にするため各列に別名を付けています。 ここまでくれば、次に挙げたシンプルな ResultMap で、Post のリストも含めて Blog のデータを読み込むことができます。

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="blog_id" />
  <result property="title" column="blog_title"/>
  <collection property="posts" ofType="Post">
    <id property="id" column="post_id"/>
    <result property="subject" column="post_subject"/>
    <result property="body" column="post_body"/>
  </collection>
</resultMap>
```

id 要素の重要さはここでも変わりません。まだ読んでいなければ、上の association 要素についての説明を参照してください。

もし、Post のマッピングを再利用したいのであれば、次のように２つの resultMap に分けて書くこともできます。

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="blog_id" />
  <result property="title" column="blog_title"/>
  <collection property="posts" ofType="Post" resultMap="blogPostResult" columnPrefix="post_"/>
</resultMap>

<resultMap id="blogPostResult" type="Post">
  <id property="id" column="id"/>
  <result property="subject" column="subject"/>
  <result property="body" column="body"/>
</resultMap>
```

#### ネストされたカーソルを collection にマッピングする

当然ですが、ネストされたカーソルが複数の値を返す場合もあります。<br /> 先に説明した `<association>` の場合と同様、 `<collection>` 要素に `jdbcType="CURSOR"` を指定してください。

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="id" />
  <result property="title" column="title" />
  <collection property="posts" column="posts" jdbcType="CURSOR">
    <id property="id" column="id" />
    <result property="subject" column="subject" />
    <result property="body" column="body" />
  </collection>
</resultMap>

<select id="selectBlog" resultMap="blogResult">
  select b.id, b.title, cursor(
    select p.id, p.subject, p.body from post p where p.blog_id = b.id
  ) posts from blog b
</select>
```

#### 複数の ResultSets を collection にマッピングする

association で説明したのと同様に、２つの ResultSet を返すストアドプロシージャを呼び出すことができます。１つ目の ResultSet には Blog のリスト、２つ目の ResultSet には Post のリストが含まれているものとします。

```sql
SELECT * FROM BLOG WHERE ID = #{id}

SELECT * FROM POST WHERE BLOG_ID = #{id}
```

ストアドプロシージャを実行する select 要素の `resultSets` 属性で、それぞれの ResultSet に名前を付けておく必要があります（カンマ区切り）。ここでは、それぞれ blogs, posts としています。

```xml
<select id="selectBlog" resultSets="blogs,posts" resultMap="blogResult" statementType="CALLABLE">
  {call getBlogsAndPosts(#{id,jdbcType=INTEGER,mode=IN})}
</select>
```

以下の resultMap では、posts に含まれる Post が、対応する Blog にマッピングされます。

```xml
<resultMap id="blogResult" type="Blog">
  <id property="id" column="id" />
  <result property="title" column="title"/>
  <collection property="posts" ofType="Post" resultSet="posts" column="id" foreignColumn="blog_id">
    <id property="id" column="id"/>
    <result property="subject" column="subject"/>
    <result property="body" column="body"/>
  </collection>
</resultMap>
```

<span class="important">Note</span> collection と association のマッピングに関して、階層の深さやオブジェクトの大きさに制限はありませんし、両者を組み合わせることも可能です。 ただ、パフォーマンスのことは忘れないようにしてください。 ユニットテストと負荷テストを怠らなければ、あなたのアプリケーションにとっての最適なアプローチを見つけることができます。 幸いなことに、後で別のアプローチに切り替える場合でも、MyBatis ならコードの変更は最小限（あるいはゼロ）で済みます。

association と collection を使った高度なマッピングはかなり深いテーマです。 ドキュメントで全てを網羅することはできませんが、少し練習すればすぐに全体がクリアになるはずです。

#### discriminator

```xml
<discriminator javaType="int" column="draft">
  <case value="1" resultType="DraftPost"/>
</discriminator>
```

データベースに対して実行した１回のクエリによって、複数の異なる（何らかの関係を持った）データ型を含む結果が戻ってくることがあります。 discriminator 要素は、こうしたケースをはじめとして、クラスの継承関係を再現する場合など様々な用途を想定して作られています。 discriminator は、Java における switch 文と同じように働くので理解するのはそれほど難しくないはずです。

discriminator を定義する場合は column と javaType 属性を指定します。 ここで指定した column は、MyBatis が比較対象の値を参照する際に使用されます。 javaType は、値が等しいかどうかを調べる際、確実に正しい比較が行われるように指定します（ですが、ほとんどのケースでは String を指定すれば期待通りに動作するはずです）。 使用例を見てみましょう。

```xml
<resultMap id="vehicleResult" type="Vehicle">
  <id property="id" column="id" />
  <result property="vin" column="vin"/>
  <result property="year" column="year"/>
  <result property="make" column="make"/>
  <result property="model" column="model"/>
  <result property="color" column="color"/>
  <discriminator javaType="int" column="vehicle_type">
    <case value="1" resultMap="carResult"/>
    <case value="2" resultMap="truckResult"/>
    <case value="3" resultMap="vanResult"/>
    <case value="4" resultMap="suvResult"/>
  </discriminator>
</resultMap>
```

この例では、MyBatis は結果セットのレコードをひとつずつ取り出して vehicle\_type の値を比較します。 もし、vehicle\_type の値が case で指定した条件のいずれかに一致した場合、その case 要素で指定されている resultMap を使って結果のマッピングを行います。 この処理は排他的に行われます。言い換えると、使われなかった resultMap は無視されるということです（ただし resultMap が継承されている場合は別です。これについてはすぐ後で説明します）。 一致する case が見つからない場合は、discriminator ブロックの外側で定義されている resultMap だけが適用されます。 仮に、carResult が次のように定義されていたとします。

```xml
<resultMap id="carResult" type="Car">
  <result property="doorCount" column="door_count" />
</resultMap>
```

この場合、doorCount プロパティのみが読み込まれることになります。 これによって、完全に独立した discriminator case のグループを定義することができます。親の resultMap と全く関連性のない resultMap であっても構いません。 今回のケースでは車は乗り物の一種（Car is-a Vehicle）なので、car と vehicle の間に関連性があることは明らかです。 当然、doorCount 以外のプロパティも読み込んで欲しいと思うでしょう。 resultMap を一箇所変更すれば、希望通りの動作になります。

```xml
<resultMap id="carResult" type="Car" extends="vehicleResult">
  <result property="doorCount" column="door_count" />
</resultMap>
```

これで、vehicleResult と carResult の両方で定義されているすべてのプロパティが読み込まれるようになります。

繰り返しになりますが、別々の resultMap を定義するのが面倒だと考える方もいると思います。 簡潔にまとまった表記が好みだという方のために、別の記法も用意されています。

```xml
<resultMap id="vehicleResult" type="Vehicle">
  <id property="id" column="id" />
  <result property="vin" column="vin"/>
  <result property="year" column="year"/>
  <result property="make" column="make"/>
  <result property="model" column="model"/>
  <result property="color" column="color"/>
  <discriminator javaType="int" column="vehicle_type">
    <case value="1" resultType="Car">
      <result property="doorCount" column="door_count" />
    </case>
    <case value="2" resultType="Truck">
      <result property="boxSize" column="box_size" />
      <result property="extendedCab" column="extended_cab" />
    </case>
    <case value="3" resultType="Van">
      <result property="powerSlidingDoor" column="power_sliding_door" />
    </case>
    <case value="4" resultType="SUV">
      <result property="allWheelDrive" column="all_wheel_drive" />
    </case>
  </discriminator>
</resultMap>
```

<span class="important">思い出してください</span> これらは全て Result Map なので、もし result が全く指定されていなければ、MyBatis が自動的に列名と一致する名前を持つプロパティに値を設定します。 ですから、上で挙げた例の多くは必要以上に冗長になっているといえます。 ただ、データベースは複雑ですから全てのケースで自動マッピングが使えるということもないでしょう。

## 自動マッピング

<a id="auto-mapping"></a>

前章では、シンプルなケースでは MyBatis の自動マッピングが利用できるということと、複雑なケースではあなた自身で Result Map を記述する必要があるということを説明しました。 この章では更に、この２つの方法を同時に利用する方法を説明していきます。 まず、自動マッピングの動作について詳しく見て行きましょう。

結果を自動マッピングする際、MyBatis は列名と同じ名前を持つプロパティを探します（大文字と小文字は区別しません）。 例えば <i>ID</i> という名前の列と <i>id</i> という名前のプロパティが見つかった場合、MyBatis は <i>id</i> プロパティに <i>ID</i> 列の値をセットします。

通常、データベースの列名は英数字と単語を区切るアンダースコアで定義され、Java のプロパティはキャメルケースで定義されるのが一般的です。 `mapUnderscoreToCamelCase` に true に設定すると、この一般的な命名規則に基づいて自動マッピングを適用することができます。

Result Map が指定されている場合でも自動マッピングは動作します。この場合、ResultSet に含まれる列のうち、各 Result Map で明示的にマッピングが指定されていない列が自動マッピングの対象となります。 次の例では、<i>hashed\_password</i> 列が password プロパティにマップされ、<i>id</i> と <i>userName</i> 列が自動マッピングの対象となります。

```xml
<select id="selectUsers" resultMap="userResultMap">
  select
    user_id             as "id",
    user_name           as "userName",
    hashed_password
  from some_table
  where id = #{id}
</select>
```

```xml
<resultMap id="userResultMap" type="User">
  <result property="hashedPassword" column="hashed_password"/>
</resultMap>
```

自動マッピングには３つのレベルがあります。

- `NONE` - 自動マッピングを無効化します。明示的にマッピングが指定されたプロパティにのみ値がセットされます。
- `PARTIAL` - ネストされた Result Map を持たない Result Map のみが自動マッピングの対象となります。
- `FULL` - 全ての Result Map が自動マッピングの対象となります。

デフォルト値は `PARTIAL` で、それには理由があります。 `FULL` が指定されていると、JOIN 句によって複数のエンティティに対する結果が一行に結合されているような場合に自動マッピングによって意図しないマッピングが実行されてしまう場合があります。 次の例を見てください。

```xml
<select id="selectBlog" resultMap="blogResult">
  select
    B.id,
    B.title,
    A.username
  from Blog B left outer join Author A on B.author_id = A.id
  where B.id = #{id}
</select>
```

```xml
<resultMap id="blogResult" type="Blog">
  <association property="author" resultMap="authorResult"/>
</resultMap>

<resultMap id="authorResult" type="Author">
  <result property="username" column="author_username"/>
</resultMap>
```

この Result Map では、<i>Blog</i> と <i>Author</i> の両方が自動マッピングの対象となりますが、<i>Author</i> に <i>id</i> というプロパティがあり、ResultSet に <i>id</i> という列が含まれているため、Author の id に Blog の id がセットされることになります。 自動マッピングで `FULL` を指定する場合は、こうした意図しないマッピングが行われないように注意する必要があります。

尚、グローバルな自動マッピング設定とは別に、`autoMapping` 属性を指定することで特定のステートメントでの自動マッピング動作を有効／無効化することもできます。

```xml
<resultMap id="userResultMap" type="User" autoMapping="false">
  <result property="hashedPassword" column="hashed_password"/>
</resultMap>
```

## cache

MyBatis には非常に柔軟なトランザクション対応のクエリキャッシング機能が用意されています。 MyBatis 3 では、より強力で、かつ簡単に設定できるよう、キャッシュの実装に多くの改良が加えられています。

デフォルトでは、セッション生存期間中のデータを保持するローカルセッションキャッシュのみが有効です。 グローバルな２次キャッシュを有効にするためには、Mapper XML ファイルに次の一行を追加するだけです。

```xml
<cache/>
```

文字通りこれだけです。この行の効果は次の通りです。

- `cacheEnabled` が true で、ステートメントの `useCache` も true の場合、カスタム ResultHandler を使用しない select ステートメントの結果は２次キャッシュに保存されます。Cursor を使うクエリはこのキャッシュを使用しません。
- この SQL マップファイルに含まれる insert, update, delete ステートメントを実行するとキャッシュがフラッシュされます。
- このキャッシュは LRU アルゴリズムに基づいて置き換えられます。
- このキャッシュは経過時間によってフラッシュされることはありません（つまり Flush Interval は設定されていないということです）。
- このキャッシュはクエリ結果のリストまたはオブジェクトへの参照を 1024 個格納します。
- このキャッシュは読み書き可能なキャッシュとして扱われます。これはつまり、取得したオブジェクトは共有されず、呼び出した側で安全に変更することができる（別の呼び出し元や他スレッドでの変更の影響を受けない）ということを意味しています。

<span class="label important">重要</span> 対応する Java マッパーのステートメントをキャッシュの適用対象に含めるためには `@CacheNamespaceRef` アノテーションで XML マッパーのネームスペースを指定する必要があります。

これらのプロパティは cache 要素の属性で変更することができます。

```xml
<cache
  eviction="FIFO"
  flushInterval="60000"
  size="512"
  readOnly="true"/>
```

この設定では、以下のようなキャッシュが作成されます。

- FIFO アルゴリズムに基づいて置き換えられる。
- 60 秒ごとにフラッシュされる。
- 結果オブジェクトまたはリストへの参照を 512 個まで格納できる。
- 返されるオブジェクトは読み取り専用（read-only）（変更した場合、他の呼び出し元や別スレッドからの変更とコンフリクトする可能性がある）。

指定可能な置換（eviction）ポリシーは以下の通りです。

- LRU – Least Recently Used: 最も長いこと使われていないオブジェクトを優先的に削除します。
- FIFO – First In First Out: 最初に登録されたオブジェクトから順番に削除します。
- SOFT – Soft Reference: ガベージコレクターの状態と Soft Reference の規則に基づいてオブジェクトを削除します。
- WEAK – Weak Reference: ガベージコレクターの状態と Weak Reference の規則に基づいて、より積極的にオブジェクトを削除します。

デフォルト値は LRU です。

flushInterval には、適切な時間（ミリ秒）を表す正の整数を指定することができます。 デフォルト値は指定なしで、キャッシュがフラッシュされるのはステートメント（insert, update, delete または flushCache が設定された select）が実行された場合のみです。

size には任意の正の整数を指定することができますが、実行環境で利用可能なメモリリソースとキャッシュされるオブジェクトのサイズに配慮してください。デフォルト値は 1024 です。

readOnly 属性には true または false を設定することができます。読み取り専用キャッシュはキャッシュされたオブジェクトのインスタンスをそのまま呼び出し元に返しますので、このオブジェクトを変更すべきではありません。 読み取り専用キャッシュの利点は非常に高速だということです。 読み書き可能なキャッシュは、キャッシュされたオブジェクトを複製して（シリアライゼーションを使います）返しますので読み取り専用と比較すると遅いですが安全です。 デフォルト値は false です。

<span class="label important">NOTE</span> ２次レベルキャッシュはトランザクションに対応しています。SqlSession が (1) commit された場合、あるいは (2) flushCache=true が指定された insert/delete/update が実行されずに rollback された場合に２次レベルキャッシュが更新されます。

#### カスタムキャッシュを使う

上記のプロパティを使ってキャッシュをカスタマイズする方法の他に、自作のキャッシュ実装や 3rd パーティのキャッシュソリューションのアダプタを作成してキャッシュの挙動を完全にオーバーライドしてしまうこともできます。

```xml
<cache type="com.domain.something.MyCustomCache"/>
```

上記は、カスタムキャッシュの実装を使用するための設定例です。 type 属性で指定されているクラスは org.apache.ibatis.cache.Cache インターフェイスを実装している必要があります。 このインターフェイスは MyBatis の中では複雑な方ですが、その役割を考えればシンプルです。

```java
public interface Cache {
  String getId();
  int getSize();
  void putObject(Object key, Object value);
  Object getObject(Object key);
  Object removeObject(Object key);
  void clear();
}
```

あなたが作成したキャッシュを設定するには、キャッシュの実装クラスに public な JavaBean プロパティを追加し、cache 要素でプロパティを渡しします。 例えば次のように設定すると、あなたが作成したキャッシュの実装クラスの \"setCacheFile(String file)\" というメソッドが呼び出されます。

```xml
<cache type="com.domain.something.MyCustomCache">
  <property name="cacheFile" value="/tmp/my-custom-cache.tmp"/>
</cache>
```

設定対象のプロパティは、単純型であれば String 以外でも使用可能です。 加えて、[コンフィギュレーション用のプロパティ](configuration.html#properties)に定義した値で置き換えるために、プレースホルダ(例：`${cache.file}`)を指定することができます。

3.4.2以降では, MyBatisはすべてのプロパティを設定した後に初期化メソッドを呼び出す仕組みをサポートしています。 この機能の使用したい場合は、あなたが作成したキャッシュの実装クラスに`org.apache.ibatis.builder.InitializingObject`インタフェースを実装してください。

```java
public interface InitializingObject {
  void initialize() throws Exception;
}
```

ここで重要なのは、キャッシュの設定とキャッシュのインスタンスは Mapper XML ファイルのネームスペースに関連付けられているということです。 あるネームスペースに対して定義されているキャッシュは、同じネームスペース内で定義されている全てのステートメントに関連付けられます。 ステートメントごとにキャッシュとの関連性を変更したり無効化することができるように、二つの簡単な属性が用意されています。 デフォルトでは、各ステートメントは下記のように設定されています。

```xml
<select ... flushCache="false" useCache="true"/>
<insert ... flushCache="true"/>
<update ... flushCache="true"/>
<delete ... flushCache="true"/>
```

上記の設定はデフォルトですので、明示的に上記のように設定する必要はありません。 デフォルトの動作を変更したい場合に限り、flushCache と useCache 属性を設定するようにしてください。 例えば、特定の select ステートメントの結果をキャッシュの対象から除外したり、ある select ステートメントを実行する際にキャッシュをフラッシュしたい、といった場合です。 同様に、キャッシュのフラッシュを伴わない update ステートメントを定義したいと思う時があるかも知れません。

## cache-ref

前の章で説明した通り、あるネームスペースに対して設定されているキャッシュが適用されるのは同じネームスペース内で定義されているステートメントのみですし、このキャッシュをフラッシュするのも同じネームスペース内で定義されているステートメントのみです。 あるキャッシュの設定とインスタンスを複数のネームスペースで共有したいと思う時があるかも知れません。 その場合、cache-ref 要素を使って別のネームスペースで定義されているキャッシュの定義を参照することができます。

```xml
<cache-ref namespace="com.someone.application.data.SomeMapper"/>
```
