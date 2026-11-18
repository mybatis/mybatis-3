title: MyBatis 3 | 設定
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

# 設定

MyBatis の設定ファイルには、MyBatis の動作をカスタマイズするための設定やプロパティが含まれています。ファイルの上位レベルの構造は次のようになっています。

- configuration
  - [properties](#properties)
  - [settings](#settings)
  - [typeAliases](#typeAliases)
  - [typeHandlers](#typeHandlers)
  - [objectFactory](#objectFactory)
  - [plugins](#plugins)
  - [environments](#environments)
    - environment
      - transactionManager
      - dataSource
  - [databaseIdProvider](#databaseIdProvider)
  - [mappers](#mappers)

## properties

\'properties\' は、別ファイルで設定することができて上書き可能なプロパティです。よく使われる Java Properties ファイルのインスタンス、または properties 要素の子要素として設定することができます。<br />例：

```xml
<properties resource="org/mybatis/example/config.properties">
  <property name="username" value="dev_user"/>
  <property name="password" value="F2Fa3!33TYyg"/>
</properties>
```

読み込んだプロパティは設定ファイル全体を通して使用可能で、変数を動的に設定することができます。<br />例：

```xml
<dataSource type="POOLED">
  <property name="driver" value="${driver}"/>
  <property name="url" value="${url}"/>
  <property name="username" value="${username}"/>
  <property name="password" value="${password}"/>
</dataSource>
```

この例では username と password の値が properties 要素で指定した値に、そして driver と url の値が config.properties 内で定義されている値に置き換えられます。この仕組みによって柔軟な設定が実現可能です。

SqlSessionFactoryBuilder.build() メソッドに Properties を渡すこともできます。<br />例：

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, props);

// ... or ...

SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, environment, props);
```

あるプロパティが複数の箇所で指定されている場合、MyBatis は次の順番で値を読み込みます。

- まず最初に properties 要素のボディで指定されたプロパティが読み込まれます。
- 次に、クラスパスリソースや properties 要素の url 属性で指定された Properties が読み込まれます。先に設定されていた値があれば上書きされます。
- そして最後にメソッドの引数として渡された値が読み込まれます。ここでも properties 要素のボディで指定された値や resource/url 属性で指定されたファイルから同じキーを持つ値が読み込まれていた場合には上書きされます。

従って、メソッド引数として渡されたプロパティが最も優先度が高く、次に resource/url 属性、最も優先度が低いのは properties 要素のボディで指定された値ということになります。

MyBatis 3.4.2以降では、下記に示すようにプレースホルダの中にデフォルト値を指定することができます。

```xml
<dataSource type="POOLED">
  <!-- ... -->
  <property name="username" value="${username:ut_user}"/> <!-- 'username'プロパティが存在しない場合は、 usernameは'ut_user'になる -->
</dataSource>
```

この機能はデフォルトでは無効になっています。もしプレースホルダの中にデフォルト値を指定したい場合は、下記に示すように特別なプロパティを追加して機能を有効化する必要があります。

```xml
<properties resource="org/mybatis/example/config.properties">
  <!-- ... -->
  <property name="org.apache.ibatis.parsing.PropertyParser.enable-default-value" value="true"/> <!-- この機能を有効化 -->
</properties>
```

<span class="label important">NOTE</span> また、既にプロパティキーとして`":"`を使用(例: `db:username`)していたり、 SQL定義の中でOGNL式の三項演算子(例: `${tableName != null ? tableName : 'global_constants'}`)を使用している場合は、 下記に示すように特別なプロパティを追加してキーとデフォルト値を分割するための文字を変更する必要があります。

```xml
<properties resource="org/mybatis/example/config.properties">
  <!-- ... -->
  <property name="org.apache.ibatis.parsing.PropertyParser.default-value-separator" value="?:"/> <!-- 分割文字を変更 -->
</properties>
```

```xml
<dataSource type="POOLED">
  <!-- ... -->
  <property name="username" value="${db:username?:ut_user}"/>
</dataSource>
```

## settings

\'settings\' は、実行時に MyBatis の動作を変更することができる、非常に重要な調整点です。 各設定についての説明と設定可能な値およびデフォルト値を以下の表にまとめてあります。

| 設定 | 説明 | 設定可能な値 | デフォルト値 |
| --- | --- | --- | --- |
| cacheEnabled | このコンフィグレーション内で定義されている全てのキャッシュの有効／無効を切り替えるグローバルな設定です。 | true \| false | true |
| lazyLoadingEnabled | Lazy Loading（遅延読み込み）の有効／無効を切り替えるグローバルな設定です。 無効にした場合、association として指定されているデータは直ちに読み込まれます。 association 要素で `fetchType` 属性が指定されている場合はそちらの指定が優先されます。 | true \| false | false |
| aggressiveLazyLoading | この設定が有効の場合、オブジェクトのいずれかのメソッド呼び出しと同時にすべての Lazy loading が実行されます。 無効の場合は、各プロパティはそれぞれ要求時に読み込まれます（関連項目 `lazyLoadTriggerMethods` ）。 | true \| false | false （3.4.1 以下は true） |
| <del>multipleResultSetsEnabled</del> | 非推奨。この設定は何の効果もありません。 | true \| false | true |
| useColumnLabel | 列名の代わりに列ラベルを使用します。 ドライバによって動作が異なります。 ドライバのドキュメントを参照するか、両方のモードを試して動作を確認してください。 | true \| false | true |
| useGeneratedKeys | JDBC の generated keys サポートを使用するかどうかを指定します。 Derby のように非互換となっていても動作するドライバに対応するため、true を設定した場合は強制的に generated keys を使用します。 | true \| false | false |
| autoMappingBehavior | MyBatis の自動マッピング機能（列をフィールド／プロパティに自動的にマッピングする機能）の動作を指定します。 NONE を指定すると自動マッピングは無効となります。 PARTIAL が指定された場合、association や collection などを含まない単純な resultMap のみが自動マッピングの対象となります。 FULL が指定された場合、ネストされた結果などの複雑なものも含めて全てが自動マッピングの対象となります。 | NONE, PARTIAL, FULL | PARTIAL |
| autoMappingUnknownColumnBehavior | 自動マッピング対象のプロパティが存在しない(又はプロパティ型がサポート外の)カラムを検知した時の動作を指定します。 <ul><li>`NONE`: 何もしません</li> <li>`WARNING`: 警告ログを出力します (`'org.apache.ibatis.session.AutoMappingUnknownColumnBehavior'` のログレベルを`WARN`に設定してください)</li> <li>`FAILING`: 自動マッピング処理をエラーにします。(`SqlSessionException`が発生します)</li></ul> この機能には制限があり、\`autoMappingBehavior\` に \`FULL\` がセットされていると誤った結果が報告される場合があります。 | NONE, WARNING, FAILING | NONE |
| defaultExecutorType | デフォルトの executor を指定します。 SIMPLE executor は特別なことは何もしません。 REUSE executor は PreparedStatement を再利用します。 BATCH executor はステートメントを再利用してバッチ更新を実行します。 | SIMPLE REUSE BATCH | SIMPLE |
| defaultStatementTimeout | ドライバがデータベースからの応答を待ち続ける秒数（タイムアウト）を設定します。 | 0 以上の整数（単位：秒） | なし (null) |
| defaultFetchSize | 検索結果のフェッチサイズを制御するためのドライバヒントを設定します。 このパラメータ値はクエリ毎の設定で上書きできます。 | 0 以上の整数 | なし (null) |
| defaultResultSetType | ステートメント毎の設定を省略した場合のスクロール方法を指定します。 (導入されたバージョン: 3.5.2) | FORWARD\_ONLY \| SCROLL\_SENSITIVE \| SCROLL\_INSENSITIVE \| DEFAULT(指定しない時と同じ動作) | なし (null) |
| safeRowBoundsEnabled | true の場合、ネストされた結果マッピングで RowBounds のオフセットが 0 より大きいか、上限がデフォルト値より小さいと例外が発生します。 このチェックを省略する場合は false を設定します。 | true \| false | false |
| safeResultHandlerEnabled | ネストされた結果マッピングを含むステートメントでカスタム ResultHandler を使用する場合の安全性チェックを設定します。true の場合は使用できませんが、resultOrdered が true のステートメントは例外です。 チェックを無効にする場合は false を設定します。 | true \| false | true |
| mapUnderscoreToCamelCase | データベースにある A\_COLUMN のようなアンダースコアを含む列を Camel Case の Java プロパティ aColumn に自動的にマッピングする機能の有効／無効を切り替えます。 | true \| false | false |
| localCacheScope | MyBatis は循環参照の解決やネストされたクエリのスピード向上のためにローカルキャッシュを使用します。 デフォルト（SESSION）では同一セッション内の通常のクエリ結果がキャッシュされます。カスタム ResultHandler を渡すクエリはローカルキャッシュを読み込まず、Cursor を使うクエリはローカルキャッシュを使用しません。localCacheScope に STATEMENT を設定した場合、ローカルキャッシュはステートメントごとに適用されます。言い換えると、同一 SqlSession に対する複数の呼び出しでデータが共有されることはありません。 | SESSION \| STATEMENT | SESSION |
| jdbcTypeForNull | 引数の JDBC タイプが未指定の場合、null 値に対して割り当てられる JDBC タイプを設定します。 ドライバによっては列に対する JDBC タイプの指定が必須な場合もありますが、NULL, VARCHAR, OTHER などの汎用の型を指定すれば動作するものもあります。 | JdbcType 列挙型。一般的には NULL, VARCHAR, OTHER のいずれかです。 | OTHER |
| lazyLoadTriggerMethods | Lazy loading のトリガとなる Object のメソッドを指定します。 | カンマ区切りのメソッド名のリスト | equals,clone,hashCode,toString |
| defaultScriptingLanguage | ダイナミック SQL を記述する際のデフォルトの言語を指定します。 | タイプエイリアスまたは完全修飾クラス名 | org.apache.ibatis.scripting.xmltags.XMLLanguageDriver |
| defaultEnumTypeHandler | Enum型に適用するデフォルトの `TypeHandler` を指定します。(導入されたバージョン: 3.4.5) | タイプエイリアスまたは完全修飾クラス名 | org.apache.ibatis.type.EnumTypeHandler |
| callSettersOnNulls | 取得した値が null の場合にセッターあるいは Map の put メソッドを呼び出すかどうかを指定します。 この設定は Map.keySet() や null 値による初期化を利用している場合に有用です。プリミティブ型（int, boolean, 等）に null がセットされることはありません。 | true \| false | false |
| returnInstanceForEmptyRow | 取得した列が全て NULL だった場合、デフォルトの動作では `null` が返りますが、 returnInstanceForEmptyRow に true を設定すると空のインスタンスが返るようになります。この動作はネストされた結果をマッピングする際にも適用されます。導入されたバージョン: 3.4.2 | true \| false | false |
| logPrefix | MyBatis が出力するログに付加される接頭辞を指定します。 | 任意の文字列 | 未指定 |
| logImpl | MyBatis のログ出力に使用するロギング実装を指定します。未指定の場合は自動的検出されます。 | SLF4J \| LOG4J(3.5.9以降非推奨) \| LOG4J2 \| JDK\_LOGGING \| COMMONS\_LOGGING \| STDOUT\_LOGGING \| NO\_LOGGING | 未指定 |
| proxyFactory | Lazy Loading（遅延読み込み）に対応したオブジェクトを生成する際に使用するプロクシツールを指定します。 | CGLIB (3.5.10 以降非推奨) \| JAVASSIST | JAVASSIST (MyBatis 3.3 以上) |
| vfsImpl | VFS 実装クラスを指定します。 | 完全修飾クラス名（カンマ区切りで複数指定可能） | 未指定 |
| useActualParamName | ステートメントの引数を参照する際、メソッドシグネチャで宣言されている名前で参照できるようにします。 このオプションを有効にする場合、プロジェクトを Java 8 （コンパイラオプション `-parameters` 付き）でコンパイルする必要があります。 (導入されたバージョン: 3.4.1) | true \| false | true |
| configurationFactory | デシリアライズされたオブジェクトの遅延読込（Lazy loading）を行う際に利用される `Configuration` のインスタンスを返すクラスを指定します。 このクラスには次のシグネチャを持つメソッドが定義されている必要があります。 `static Configuration getConfiguration()`. (導入されたバージョン: 3.2.3) | タイプエイリアスまたは完全修飾クラス名 | 未指定 |
| shrinkWhitespacesInSql | SQL 内の余分な空白文字を削除します。リテラル文字列も対象となる点に注意してください。（導入されたバージョン: 3.5.5） | true \| false | false |
| defaultSqlProviderType | SQLを提供するメソッドを保持するSQLプロバイダクラスを指定します(導入されたバージョン: 3.5.6)。 ここで指定したクラスは、SQLプロバイダアノテーション(例: `@SelectProvider`)の`type`(または `value`) 属性を省略した際に適用されます。 | タイプエイリアスまたは完全修飾クラス名 | 未指定 |
| nullableOnForEach | \'foreach\' タグの \'nullable\' 属性のデフォルト値. (導入されたバージョン: 3.5.9) | true \| false | false |
| argNameBasedConstructorAutoMapping | 引数を受け取るコンストラクタに対して自動マッピングを適用する際、引数名に一致する列をマップ対象にします。`false` の場合は列の順序依存となります。 (導入されたバージョン: 3.5.10） | true \| false | false |
| ignoreParameterMetaData | タイプハンドラー解決時のパラメータメタデータ取得をスキップします。 (導入されたバージョン: 3.6.1） | true \| false | false |

各種設定を含む settings の例：

```xml
<settings>
  <setting name="cacheEnabled" value="true"/>
  <setting name="lazyLoadingEnabled" value="true"/>
  <setting name="aggressiveLazyLoading" value="true"/>
  <setting name="useColumnLabel" value="true"/>
  <setting name="useGeneratedKeys" value="false"/>
  <setting name="autoMappingBehavior" value="PARTIAL"/>
  <setting name="autoMappingUnknownColumnBehavior" value="WARNING"/>
  <setting name="defaultExecutorType" value="SIMPLE"/>
  <setting name="defaultStatementTimeout" value="25"/>
  <setting name="defaultFetchSize" value="100"/>
  <setting name="safeRowBoundsEnabled" value="false"/>
  <setting name="safeResultHandlerEnabled" value="true"/>
  <setting name="mapUnderscoreToCamelCase" value="false"/>
  <setting name="localCacheScope" value="SESSION"/>
  <setting name="jdbcTypeForNull" value="OTHER"/>
  <setting name="lazyLoadTriggerMethods" value="equals,clone,hashCode,toString"/>
  <setting name="defaultScriptingLanguage" value="org.apache.ibatis.scripting.xmltags.XMLLanguageDriver"/>
  <setting name="defaultEnumTypeHandler" value="org.apache.ibatis.type.EnumTypeHandler"/>
  <setting name="callSettersOnNulls" value="false"/>
  <setting name="returnInstanceForEmptyRow" value="false"/>
  <setting name="logPrefix" value="exampleLogPreFix_"/>
  <setting name="logImpl" value="JDK_LOGGING"/>
  <setting name="proxyFactory" value="JAVASSIST"/>
  <setting name="vfsImpl" value="org.mybatis.example.YourselfVfsImpl"/>
  <setting name="useActualParamName" value="true"/>
  <setting name="configurationFactory" value="org.mybatis.example.ConfigurationFactory"/>
</settings>
```

## typeAliases

タイプエイリアスとは、Java タイプに対する短縮名です。 XML の中でクラスを指定する際、完全修飾名の記述を最小限で済ませることができます。<br /> 例えば、

```xml
<typeAliases>
  <typeAlias alias="Author" type="domain.blog.Author"/>
  <typeAlias alias="Blog" type="domain.blog.Blog"/>
  <typeAlias alias="Comment" type="domain.blog.Comment"/>
  <typeAlias alias="Post" type="domain.blog.Post"/>
  <typeAlias alias="Section" type="domain.blog.Section"/>
  <typeAlias alias="Tag" type="domain.blog.Tag"/>
</typeAliases>
```

上記のようにタイプエイリアスを定義しておくと、 `domain.blog.Blog` と書く代わりに `Blog` と書くことができます。

全てのクラスを列挙する代わりにパッケージを指定することもできます。

```xml
<typeAliases>
  <package name="domain.blog"/>
</typeAliases>
```

この場合、 `domain.blog` というパッケージ内のクラスに対してタイプエイリアスが登録されます。 例えば `domain.blog.Author` というクラスに対しては `author` というタイプエイリアスが登録されます。<br /> `@Alias` アノテーションを使うと、登録されるエイリアス名を指定することもできます。 次のようになります。

```java
@Alias("author")
public class Author {
    ...
}
```

汎用の Java タイプには、予めタイプエイリアスが用意されています。<br /> これらは全て Case Insensitive （大文字小文字を区別しない）です。 プリミティブ型の場合は特殊な表記が必要となるので注意してください。

| エイリアス | Java タイプ |
| --- | --- |
| \_byte | byte |
| \_char （3.5.10 以降） | char |
| \_character （3.5.10 以降） | char |
| \_long | long |
| \_short | short |
| \_int | int |
| \_integer | int |
| \_double | double |
| \_float | float |
| \_boolean | boolean |
| string | String |
| byte | Byte |
| char （3.5.10 以降） | Character |
| character （3.5.10 以降） | Character |
| long | Long |
| short | Short |
| int | Integer |
| integer | Integer |
| double | Double |
| float | Float |
| boolean | Boolean |
| date | Date |
| decimal | BigDecimal |
| bigdecimal | BigDecimal |
| biginteger | BigInteger |
| object | Object |
| date\[\] | Date\[\] |
| decimal\[\] | BigDecimal\[\] |
| bigdecimal\[\] | BigDecimal\[\] |
| biginteger\[\] | BigInteger\[\] |
| object\[\] | Object\[\] |
| map | Map |
| hashmap | HashMap |
| list | List |
| arraylist | ArrayList |
| collection | Collection |
| iterator | Iterator |

## typeHandlers

MyBatis が PreparedStatement のパラメーターをセットするとき、あるいは ResultSet から値を取得するときには、必ずその Java タイプに対応する TypeHandler が使用されます。<br /> 次の表はデフォルトの TypeHandler の一覧です。

<span class="label important">NOTE</span> バージョン3.4.5より、JSR-310(Date and Time API)のクラスがデフォルトでサポートされました。

| タイプハンドラー | Java タイプ | JDBC タイプ |
| --- | --- | --- |
| BooleanTypeHandler | Boolean, boolean | BOOLEAN 互換の型 |
| ByteTypeHandler | Byte, byte | NUMERIC または TINYINT 互換の型 |
| ShortTypeHandler | Short, short | NUMERIC または SMALLINT 互換の型 |
| IntegerTypeHandler | Integer, int | NUMERIC または INTEGER 互換の型 |
| LongTypeHandler | Long, long | NUMERIC または BIGINT 互換の型 |
| FloatTypeHandler | Float, float | NUMERIC または FLOAT 互換の型 |
| DoubleTypeHandler | Double, double | NUMERIC または DOUBLE 互換の型 |
| BigDecimalTypeHandler | BigDecimal | NUMERIC または DECIMAL 互換の型 |
| StringTypeHandler | String | CHAR, VARCHAR, LONGVARCHAR |
| ClobReaderTypeHandler | java.io.Reader | - |
| ClobTypeHandler | String | CLOB |
| NStringTypeHandler | String | NVARCHAR, NCHAR, LONGNVARCHAR |
| NClobTypeHandler | String | NCLOB |
| BlobInputStreamTypeHandler | java.io.InputStream | - |
| ByteArrayTypeHandler | byte\[\] | byte ストリーム互換の型 |
| BlobTypeHandler | byte\[\] | BLOB, LONGVARBINARY |
| DateTypeHandler | Date (java.util) | TIMESTAMP |
| DateOnlyTypeHandler | Date (java.util) | DATE |
| TimeOnlyTypeHandler | Date (java.util) | TIME |
| SqlTimestampTypeHandler | Timestamp (java.sql) | TIMESTAMP |
| SqlDateTypeHandler | Date (java.sql) | DATE |
| SqlTimeTypeHandler | Time (java.sql) | TIME |
| ObjectTypeHandler | Any | OTHER または指定がない場合 |
| EnumTypeHandler | Enumeration Type | VARCHAR インデックスではなくコードが保存されるので、String 互換の型 |
| EnumOrdinalTypeHandler | Enumeration Type | コードではなく位置（ordinal）が保存されるので、NUMERIC または DOUBLE 互換の型 |
| SqlxmlTypeHandler | java.lang.String | SQLXML |
| InstantTypeHandler | java.time.Instant | TIMESTAMP |
| LocalDateTimeTypeHandler | java.time.LocalDateTime | TIMESTAMP |
| LocalDateTypeHandler | java.time.LocalDate | DATE |
| LocalTimeTypeHandler | java.time.LocalTime | TIME |
| OffsetDateTimeTypeHandler | java.time.OffsetDateTime | TIMESTAMP |
| OffsetTimeTypeHandler | java.time.OffsetTime | TIME |
| ZonedDateTimeTypeHandler | java.time.ZonedDateTime | TIMESTAMP |
| YearTypeHandler | java.time.Year | INTEGER |
| MonthTypeHandler | java.time.Month | INTEGER |
| YearMonthTypeHandler | java.time.YearMonth | VARCHAR または LONGVARCHAR |
| JapaneseDateTypeHandler | java.time.chrono.JapaneseDate | DATE |

標準のタイプハンドラーをオーバーライドしたり、自ら作成したタイプハンドラーを使って未対応あるいは非標準の型を扱うことも可能です。自作する場合は、BaseTypeHandler クラスのサブクラスを作成します。必要に応じて対応する JDBC タイプを指定してください。<br /> 例：

```java
// ExampleTypeHandler.java
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ExampleTypeHandler extends BaseTypeHandler<String> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, parameter);
  }

  @Override
  public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return rs.getString(columnName);
  }

  @Override
  public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return rs.getString(columnIndex);
  }

  @Override
  public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return cs.getString(columnIndex);
  }
}
```

```xml
<!-- mybatis-config.xml -->
<typeHandlers>
  <typeHandler handler="org.mybatis.example.ExampleTypeHandler"/>
</typeHandlers>
```

上記のようにして登録された TypeHandler は、String 型のプロパティと VARCHAR 型の引数または結果のマッピング時に使用されるデフォルトのタイプハンドラーよりも優先的に使用されます。 `ignoreParameterMetaData` が `false`（デフォルト）の場合、パラメーターの JDBC 型が明示されていなければ、MyBatis は JDBC のパラメーターメタデータを参照してタイプハンドラーを選択できます。ドライバーがメタデータを提供しない場合は、パラメーターマッピングに `jdbcType="VARCHAR"` を指定してください。結果マッピングで `typeHandler` と `jdbcType` のどちらも指定していない場合は、結果セットのメタデータを使ってタイプハンドラーを選択できます（後述）。

MyBatis は、このタイプハンドラーの総称型（Generic Type）から適用対象の Java タイプを自動判定しますが、この動作をオーバーライドする方法が２つあります。

- typeHandler 要素に javaType 属性を追加する（例：javaType=\"String\"）
- TypeHandler の実装クラスに `@MappedTypes` アノテーションを付加して適用対象の Java タイプのリストを指定します。javaType とアノテーションを両方指定した場合は javaType の指定が優先されます。

適用対象の JDBC タイプを指定する方法は二つあります。

- 同じ typeHandler 要素に `javaType` と `jdbcType` の両方を指定します（例：`javaType="String" jdbcType="VARCHAR"`）。`javaType` を省略すると XML の `jdbcType` 属性は無視され、`@MappedJdbcTypes` があればその指定が使用されます。
- TypeHandler の実装クラスに `@MappedJdbcTypes` を付加し、JDBC タイプのリストを指定します。同じ要素に `javaType` と `jdbcType` の両方を指定した場合、このアノテーションは無視されます。

`ResultMap` の結果マッピングに `typeHandler` を明示すると、そのハンドラーが使用されます。 `typeHandler` を指定せずに `jdbcType` を指定した場合は、マッピングの解析時に Java 型と指定された JDBC 型からハンドラーを解決します。 どちらも指定していない場合は、結果を読み込む際に Java 型と結果セットのメタデータから取得した実際の列の JDBC 型を使ってハンドラーを選択します。 そのため、`@MappedJdbcTypes` で登録したハンドラーは、`includeNullJdbcType=true` を指定しなくても一致する結果列に使用できます。 `includeNullJdbcType=true` はハンドラーを `jdbcType=null` にも登録する設定であり、`ResultMap` で使用するための必須条件ではありません。

また、MyBatis にタイプハンドラーを自動検出させることもできます。

```xml
<!-- mybatis-config.xml -->
<typeHandlers>
  <package name="org.mybatis.example"/>
</typeHandlers>
```

自動検出機能を使う場合、適用対象の JDBC タイプの指定はアノテーションで行う必要があります。

```java
@MappedTypes(BigInteger.class)
public class BigIntegerHandler implements TypeHandler<BigInteger> {
   ...
}
```

複数のクラスを扱う汎用の TypeHandler を作成することもできます。 引数に Class を受け取るコンストラクタを定義しておくと、MyBatis が TypeHandler のインスタンスを作成する際に実際のクラスが渡されます。

```java
//GenericTypeHandler.java
public class GenericTypeHandler<E extends MyObject> extends BaseTypeHandler<E> {

  private Class<E> type;

  public GenericTypeHandler(Class<E> type) {
    if (type == null) throw new IllegalArgumentException("Type argument cannot be null");
    this.type = type;
  }
  ...
```

この後の節では汎用の TypeHandler である `EnumTypeHandler` と `EnumOrdinalTypeHandler` について解説します。

## Handling Enums

`Enum` （列挙型）をマップする場合、 `EnumTypeHandler` または `EnumOrdinalTypeHandler` のどちらかを使うことになります。

例えば数値の丸めモード（java.math.RoundingMode）を格納する場合、デフォルトでは `EnumTypeHandler` が使われ、各 `Enum` は名前の文字列（DOWN, HALF\_UP, etc.）に変換されます。

<b>他の TypeHandler が特定のクラスを対象としているのに対し、`EnumTypeHandler` は `Enum` を継承した任意のクラスを対象とする特別な TypeHandler です。</b>

では名前以外の値を格納したい場合、例えばデータベース管理者が数値で格納して欲しいと言ってきた場合はどうすれば良いのでしょうか。 簡単です：設定ファイルの `typeHandlers` に `EnumOrdinalTypeHandler` を追加すれば `RoundingMode` は ordinal の数値に変換されてマップされます。

```xml
<!-- mybatis-config.xml -->
<typeHandlers>
  <typeHandler handler="org.apache.ibatis.type.EnumOrdinalTypeHandler" javaType="java.math.RoundingMode"/>
</typeHandlers>
```

では、同じ `Enum` を、あるテーブルには文字列として格納し、別のテーブルには数値として格納したいといった場合はどうでしょう。

上の設定により自動マッピングでは `EnumOrdinalTypeHandler` が使われますので、 `EnumTypeHandler` を使いたい箇所ではこれを明示的に指定する必要があります。

（Mapper ファイルについては次の章で解説していますので、このドキュメントを初めて読む方はこの部分を後回しにした方が良いかも知れません）

```xml
<!DOCTYPE mapper
    PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "https://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="org.apache.ibatis.submitted.rounding.Mapper">
  <resultMap type="org.apache.ibatis.submitted.rounding.User" id="usermap">
    <id column="id" property="id"/>
    <result column="name" property="name"/>
    <result column="funkyNumber" property="funkyNumber"/>
    <result column="roundingMode" property="roundingMode"/>
  </resultMap>

  <select id="getUser" resultMap="usermap">
    select * from users
  </select>
  <insert id="insert">
      insert into users (id, name, funkyNumber, roundingMode) values (
        #{id}, #{name}, #{funkyNumber}, #{roundingMode}
      )
  </insert>

  <resultMap type="org.apache.ibatis.submitted.rounding.User" id="usermap2">
    <id column="id" property="id"/>
    <result column="name" property="name"/>
    <result column="funkyNumber" property="funkyNumber"/>
    <result column="roundingMode" property="roundingMode" typeHandler="org.apache.ibatis.type.EnumTypeHandler"/>
  </resultMap>
  <select id="getUser2" resultMap="usermap2">
    select * from users2
  </select>
  <insert id="insert2">
      insert into users2 (id, name, funkyNumber, roundingMode) values (
        #{id}, #{name}, #{funkyNumber}, #{roundingMode, typeHandler=org.apache.ibatis.type.EnumTypeHandler}
      )
  </insert>

</mapper>
```

typeHandler を指定するため、select 要素では `resultType` ではなく `resultMap` を使う必要があります。

## objectFactory

MyBatis が新しい結果オブジェクトのインスタンスを作るときは ObjectFactory が使われます。 デフォルトの ObjectFactory は、対象クラスのデフォルトコンストラクタまたは引数付きのコンストラクタを使ってインスタンスを作成するだけです。 ObjectFactory を自作することで、デフォルトの ObjectFactory の動作をオーバーライドすることもできます。<br /> 例：

```java
// ExampleObjectFactory.java
public class ExampleObjectFactory extends DefaultObjectFactory {
  @Override
  public <T> T create(Class<T> type) {
    return super.create(type);
  }

  @Override
  public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    return super.create(type, constructorArgTypes, constructorArgs);
  }

  @Override
  public void setProperties(Properties properties) {
    super.setProperties(properties);
  }

  @Override
  public <T> boolean isCollection(Class<T> type) {
    return Collection.class.isAssignableFrom(type);
  }}
```

```xml
<!-- mybatis-config.xml -->
<objectFactory type="org.mybatis.example.ExampleObjectFactory">
  <property name="someProperty" value="100"/>
</objectFactory>
```

ObjectFactory のインターフェイスは非常にシンプルです。 ２つある create メソッドのうち１つはデフォルトコンストラクタを扱うもので、もうひとつは引数を取るコンストラクタを扱います。<br /> isCollection メソッドはコレクション型かどうかを判定します。setProperties メソッドは ObjectFactory を設定するために使います。 設定ファイルの objectFactory 要素のボディで定義されているプロパティは、ObjectFactory が初期化された後に setProperties メソッドの引数として渡されます。

## plugins

MyBatis が Mapped Statement を実行する際、いくつかのタイミングで処理をインターセプトすることができます。 以下は、サポートされているインターフェースでプラグインがインターセプトできるメソッドの例です。

- Executor (update, query, queryCursor, flushStatements, commit, rollback, getTransaction, close, isClosed)
- ParameterHandler (getParameterObject, setParameters)
- ResultSetHandler (handleResultSets, handleCursorResultSets, handleOutputParameters)
- StatementHandler (prepare, parameterize, batch, update, query, queryCursor)

これらのクラスおよびメソッドに関する詳細は、各メソッドのシグネチャや MyBatis のリリースに含まれているソースコードを参照してください。 単にメソッドの呼び出しを監視する以上のことをしたいのなら、その動作について十分に理解しておく必要があります。 メソッドのデフォルトの動作を変更したりオーバーライドするということは、MyBatis のコア部分の動作に影響を与える可能性があるということを忘れないでください。これらのクラスやメソッドは非常に低レベルのものなので、プラグインを使うときは注意が必要です。

プラグインはとても強力ですが、使うのは比較的簡単です。 Interceptor インターフェイスを実装したら、インターセプト対象のメソッドシグネチャを指定するだけです。

```java
// ExamplePlugin.java
@Intercepts({@Signature(
  type= Executor.class,
  method = "update",
  args = {MappedStatement.class,Object.class})})
public class ExamplePlugin implements Interceptor {
  private Properties properties = new Properties();

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    // implement pre processing if need
    Object returnObject = invocation.proceed();
    // implement post processing if need
    return returnObject;
  }

  @Override
  public void setProperties(Properties properties) {
    this.properties = properties;
  }
}
```

```xml
<!-- mybatis-config.xml -->
<plugins>
  <plugin interceptor="org.mybatis.example.ExamplePlugin">
    <property name="someProperty" value="100"/>
  </plugin>
</plugins>
```

上記のプラグインは Executor インスタンスの update メソッド呼び出しをインターセプトします。Executor は、低レベルで mapped statement を実行する内部オブジェクトです。

<span class="label important">NOTE</span> <strong>Configuration クラスをオーバーライドする</strong>

プラグインによって MyBatis の動作を変更する以外に、Configuration クラスをオーバーライドすることもできます。 方法は単純で、Configuration のサブクラスを作って任意のメソッドをオーバーライドし、SqlSessionFactoryBuilder.build(myConfig) のようにして呼び出すだけです。 繰り返しになりますが、これは MyBatis の動作に重大な影響を与える可能性があるので注意してください。

## environments

MyBatis では複数の環境設定を定義することができます。これは、同じ SQL Map を複数のデータベースに対して使いたいとき役に立ちます。例えば、開発環境、デモ環境、本番環境で、それぞれ異なる設定場合や、本番環境の同じスキーマを持つ複数のデータベースがあり、両方に対して同じ SQL Map を使いたい場合など、様々な用途が考えられます。

<strong>ひとつだけ注意しなくてはならないことは、複数の環境設定を扱うことはできますが、一つの SqlSessionFactory インスタンスに対しては一つの環境しか割り当てることができない、ということです。</strong>

もし二つのデータベースに接続したいのなら、それぞれの接続に対して一つずつ、合計二つの SqlSessionFactory を作る必要があります。データベースが三つなら SqlSessionFactory も三つ必要、といった具合です。実に簡単なルールです。

- <strong>一つのデータベースにつき、一つの SqlSessionFactory が必要です。</strong>

SqlSessionFactoryBuilder に environment を渡すことで、どの環境に対する SqlSessionFactory をビルドするか指定することができます。 environment を引数に取るシグネチャは下記の２つです。

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, environment);
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, environment,properties);
```

environment が省略された場合、デフォルトの environment がロードされます。

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader,properties);
```

environment の具体的な設定は、environments 要素で行います。

```xml
<environments default="development">
  <environment id="development">
    <transactionManager type="JDBC">
      <property name="..." value="..."/>
    </transactionManager>
    <dataSource type="POOLED">
      <property name="driver" value="${driver}"/>
      <property name="url" value="${url}"/>
      <property name="username" value="${username}"/>
      <property name="password" value="${password}"/>
    </dataSource>
  </environment>
</environments>
```

ここでのポイントは以下の通りです。

- デフォルトの環境 (default=\"development\")
- 各環境のID (id=\"development\")
- トランザクションマネージャーの設定 (type=\"JDBC\")
- データソースの設定 (type=\"POOLED\")

デフォルト環境と環境 ID は自明です。 各環境の ID は自由に命名して構いませんが、デフォルトとして指定した環境 ID を必ず定義しておいてください。

<strong>transactionManager</strong>

MyBatis では二種類のトランザクションマネージャー（JDBC と MANAGED）が用意されています。

- JDBC - JDBC トランザクションマネージャーは、単純に JDBC のコミットとロールバックの機能を直接利用します。 この場合、トランザクションのスコープはデータソースから取得した接続に依存します。一部のドライバーとの互換性を保つため、デフォルトでは接続をクローズする前にオートコミットを有効化するようになっていますが、この処理は他のドライバにとっては不要なだけでなく負荷の高い操作となる可能性があります。バージョン 3.5.10 以降では skipSetAutoCommitOnClose に true を設定することでオートコミットの有効化処理をスキップできるようになっています。
  ```xml
  <transactionManager type="JDBC">
    <property name="skipSetAutoCommitOnClose" value="true"/>
  </transactionManager>
  ```
- MANAGED - MANAGED トランザクションマネージャーはコミットもロールバックもしません。 トランザクションのライフサイクルは全てコンテナ（JEE アプリケーションサーバーなど）が管理することになります。 MANAGED マネージャーは接続をクローズするのがデフォルトの動作ですが、お使いのコンテナによってはこの動作を変更する必要があります。以下のようにして closeConnection に false を設定してください。
  ```xml
  <transactionManager type="MANAGED">
    <property name="closeConnection" value="false"/>
  </transactionManager>
  ```

<span class="label important">NOTE</span> MyBatis を Spring と組み合わせて使う場合、トランザクションマネージャーの設定は不要です（設定しても Spring 側で用意したトランザクションマネージャーによって上書きされます）。

トランザクションマネージャーに必須のプロパティはありませんが、これらはともにタイプエイリアスです。つまり、TransactionFactory インターフェイスの実装クラスの完全修飾クラス名を指定すれば、MyBatis が用意した二種類のトランザクションマネージャーの代わりに独自に実装したトランザクションマネージャーを利用することが可能です。

```java
public interface TransactionFactory {
  default void setProperties(Properties props) { // Since 3.5.2, change to default method
    // NOP
  }
  Transaction newTransaction(Connection conn);
  Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);
}
```

XML ファイルの中で properties 要素で指定されたプロパティは、インスタンスが作成された後、setProperties() メソッドに渡されます。 トランザクションマネージャーの他に Transaction の実装クラスが必要となります。 こちらも非常にシンプルなインターフェイスです。

```java
public interface Transaction {
  Connection getConnection() throws SQLException;
  void commit() throws SQLException;
  void rollback() throws SQLException;
  void close() throws SQLException;
  Integer getTimeout() throws SQLException;
}
```

これらのインターフェイスを使えば、MyBatis のトランザクション管理方法を完全にカスタマイズすることが可能です。

<strong>dataSource</strong>

dataSource 要素では、JDBC 標準の DataSource インターフェイスを実装している JDBC Connection オブジェクトの取得元を指定します。

- ほとんどの MyBatis アプリケーションではサンプルのように dataSource を設定することになるはずですが、これは必須というわけではありません。 ただし、遅延読み込み (Lazy Loading) を利用する場合はこのデータソースを使う必要があります。

MyBatis では、３つのデータソースタイプが用意されています (type=\"????\")

<strong>UNPOOLED</strong> – このデータソースは、リクエストごとに接続のオープン／クローズを実行します。 少々遅いですが、即時の接続を必要としないシンプルなアプリケーションの場合は良い選択肢です。 パフォーマンスに関しては、お使いのデータベースによっても変わってきます。 接続プールの重要性が低いデータベースの場合はこの設定が最適となります。 UNPOOLED データソースに対して設定可能なプロパティは下記の通りです。

- driver – JDBC ドライバーの完全修飾 Java クラス名（ドライバーに含まれているデータソースクラスではありません）
- url – JDBC 接続 URL
- username – データベース接続時のユーザー名
- password - データベース接続時のパスワード
- autoCommit – 新しい接続のオートコミットを設定します。省略した場合はドライバーのデフォルト値が使用されます。
- defaultTransactionIsolationLevel – デフォルトのトランザクション分離レベル
- defaultNetworkTimeout – 任意の要求に対するデータベースからの応答待機期限のデフォルト値（ミリ秒）。詳細は `java.sql.Connection#setNetworkTimeout()` の javadoc 参照。

オプションとして、データベースドライバーのプロパティを設定することもできます。 この場合、プロパティ名に接頭辞 \"driver.\" を付加します。<br /> 例：

- driver.encoding=UTF8

上記の例では、DriverManager.getConnection(url, driverProperties) メソッド経由でドライバーに渡されるプロパティとして \"encoding=UTF8\" が追加されます。

<strong>POOLED</strong> – このデータソースは新規接続開始時に必要となる初期接続と認証の時間を省くため JDBC 接続をプールします。 Web アプリケーションでは応答速度向上のために良く使われる設定です。

POOLED データソースには、UNPOOLED に対して設定可能なプロパティに加えて以下を指定することができます。

- poolMaximumActiveConnections – 同時にプールされる接続数の最大値です。 デフォルト: 10
- poolMaximumIdleConnections – 同時にプールされるアイドル接続数の最大値です。
- poolMaximumCheckoutTime – プールされた接続が足りなくなった場合、使用中の接続でこの時間を経過しても戻されていない接続があれば、その接続は回収されて別の要求に再割り当てされます。 デフォルト: 20000ms （20秒）
- poolTimeToWait – 接続の取得に長時間を要した場合にログを出力し、接続の再取得を試みる機会を与えるための低レベルの設定です（プールが誤って設定された場合、無限に待機状態となってしまうのを防ぐため）。 デフォルト: 20000ms （20秒）
- `poolMaximumLocalBadConnectionTolerance` – This is a low level setting about tolerance of bad connections got for any thread. If a thread got a bad connection, it may still have another chance to re-attempt to get another connection which is valid. But the retrying times should not more than the sum of `poolMaximumIdleConnections` and `poolMaximumLocalBadConnectionTolerance`. Default: 3 (Since: 3.4.5)
- poolPingQuery – プールされた接続が正常で、リクエストを受け付けられる状態にあるかどうか確認するためにデータベースに送信される ping クエリを設定します。 デフォルトは \"NO PING QUERY SET\" で、一般的なドライバーであれば問い合わせは失敗し、適切なエラーメッセージが出力されるはずです。
- poolPingEnabled – ping クエリの有効／無効を設定します。有効化した場合は poolPingQuery に適切な SQL 文（できるだけ軽いもの）を指定する必要があります。 デフォルト: false
- poolPingConnectionsNotUsedFor – poolPingQuery が実行されるまでの時間を設定します。 データベース接続がタイムアウトするまでの時間に合わせて設定することで、必要以上に ping クエリが実行されるのを防ぎます。 デフォルト: 0（poolPingEnabled が true の場合、すべての接続に対して毎回 ping が実行されます）

<strong>JNDI</strong> – JNDI 経由でデータソースを取得する EJB やアプリケーションサーバーと併用することを想定したデータソースです。 設定に必要なプロパテイは２つだけです。

- initial\_context – InitialContext から Context を参照する際に使用されるプロパティです（i.e. initialContext.lookup(initial\_context))。 このプロパティは省略可能で、省略した場合は InitialContext に対して直接 data\_source プロパティの参照が実行されます。
- data\_source – データソースのインスタンを参照するためのコンテクストパスです。 この参照は initial\_context で指定したコンテキストに対して（initial\_context が省略された場合は直接 InitialContext に対して）実行されます。

他の DataSource 設定と同様に、\"env.\" という接頭辞を付けることで InitialContext にそのまま渡されるプロパティを設定することができます。<br /> 例：

- env.encoding=UTF8

上記のように指定すると、\"encoding=UTF8\" というプロパティが InitialContext のインスタンス生成時にコンストラクタに渡されます。

上記以外の DataSource を利用する場合は `org.apache.ibatis.datasource.DataSourceFactory` インターフェイスを実装したアダプタを作成します。

```java
public interface DataSourceFactory {
  void setProperties(Properties props);
  DataSource getDataSource();
}
```

アダプタを作成する際は `org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory` を継承することもできます。 下記は C3P0 のデータソースを利用する場合の実装例です。

```java
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3P0DataSourceFactory extends UnpooledDataSourceFactory {

  public C3P0DataSourceFactory() {
    this.dataSource = new ComboPooledDataSource();
  }
}
```

作成したアダプタを設定する場合は property 要素を使って必要なプロパティを指定します。 上述のアダプタを使って PostgreSQL に接続する場合の設定例を挙げておきます。

```xml
<dataSource type="org.myproject.C3P0DataSourceFactory">
  <property name="driver" value="org.postgresql.Driver"/>
  <property name="url" value="jdbc:postgresql:mydb"/>
  <property name="username" value="postgres"/>
  <property name="password" value="root"/>
</dataSource>
```

## databaseIdProvider

MyBatis では、利用するデータベースによって異なるステートメントを実行することができます。 複数データベースサポート機能は、各ステートメントの `databaseId` 属性によって実現されます。 MyBatis は定義されているステートメントの中で `databaseId` 属性が指定されていないステートメントおよび現在の設定と一致する `databaseId` 属性を持ったステートメントをロードします。 同じステートメントで、`databaseId` 属性が指定されているものと指定されていないものが両方定義されていた場合、指定がないステートメントは無視されます。 複数データベースサポート機能を有効にするには、mybatis-config.xml に `databaseIdProvider` を追加します。

```xml
<databaseIdProvider type="DB_VENDOR" />
```

databaseIdProvider に DB\_VENDOR が指定された場合は VendorDatabaseIdProvider がプロバイダとして使われます。 この実装は、`DatabaseMetaData#getDatabaseProductName()` から返される製品名の文字列を「現在の databaseId」として返します。この文字列は長すぎる上にバージョンによって異なる値を返すことがあるので、次のようにプロパティを指定して短い文字列に変換できるようになっています。

```xml
<databaseIdProvider type="DB_VENDOR">
  <property name="SQL Server" value="sqlserver"/>
  <property name="DB2" value="db2"/>
  <property name="Oracle" value="oracle" />
</databaseIdProvider>
```

プロパティが指定された場合、VendorDatabaseIdProvider は `DatabaseMetaData#getDatabaseProductName()` から返された製品名に name 属性で指定された文字列が含まれているかどうかチェックし、含まれている場合は value 属性で指定されている文字列を「現在の databaseId」として返します。指定された name が一つも含まれていない場合は null を返します。 上記の例で、`getDatabaseProductName()` が \"Oracle (DataDirect)\" という文字列を返す場合、現在の databaseId は \"oracle\" となります。

DatabaseIdProvider を自作することも可能です。`org.apache.ibatis.mapping.DatabaseIdProvider` インターフェイスを実装したクラスを作成し、mybatis-config.xml で databaseIdProvider の type 属性の値として、作成したクラスの完全修飾クラス名を指定します。

```java
public interface DatabaseIdProvider {
  default void setProperties(Properties p) { // Since 3.5.2, change to default method
    // NOP
  }
  String getDatabaseId(DataSource dataSource) throws SQLException;
}
```

## mappers

上記のように configuration 要素を設定することで MyBatis の動作設定は完了したので、Mapped SQL Statement の定義を行う準備ができました。 まず、定義ファイルの場所を指定する必要があります。 Java にはこのような場合に利用できる自動検出の仕組みがないので、単純にマッピングファイルの場所を指定します。 相対パスでクラスパスリソースへの参照を指定することもできますし、文字列リテラルでフルパスか完全修飾 URL 形式（file&#58;/// でも可）を指定することも可能です。<br /> 例：

```xml
<!-- Using classpath relative resources -->
<mappers>
  <mapper resource="org/mybatis/builder/AuthorMapper.xml"/>
  <mapper resource="org/mybatis/builder/BlogMapper.xml"/>
  <mapper resource="org/mybatis/builder/PostMapper.xml"/>
</mappers>
```

```xml
<!-- Using url fully qualified paths -->
<mappers>
  <mapper url="file:///var/mappers/AuthorMapper.xml"/>
  <mapper url="file:///var/mappers/BlogMapper.xml"/>
  <mapper url="file:///var/mappers/PostMapper.xml"/>
</mappers>
```

```xml
<!-- Using mapper interface classes -->
<mappers>
  <mapper class="org.mybatis.builder.AuthorMapper"/>
  <mapper class="org.mybatis.builder.BlogMapper"/>
  <mapper class="org.mybatis.builder.PostMapper"/>
</mappers>
```

```xml
<!-- Register all interfaces in a package as mappers -->
<mappers>
  <package name="org.mybatis.builder"/>
</mappers>
```

これらの設定は、単に MyBatis にマッピングファイルの場所を伝えているだけで、その先は各 SQL マップファイルの定義次第ということになります（次の章で説明します）。
