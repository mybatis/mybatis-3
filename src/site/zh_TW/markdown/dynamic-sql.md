title: MyBatis 3 | 動態 SQL
author: Clinton Begin

<h1 class="d-none">Avoid blank site</h1>

## 動態 SQL

MyBatis 最強大的特性之一向來是其「動態 SQL」功能。如果您有使用 JDBC 或任何類似框架的經驗，一定能體會根據條件拼接 SQL 字串有多麼痛苦——必須時刻注意不要漏掉空格，或者不要在欄位清單末尾遺漏逗號。處理動態 SQL 往往令人苦不堪言。

雖然撰寫動態 SQL 絕非輕鬆寫意的事，但 MyBatis 透過強大的動態 SQL 語言極大地改善了這種情況，此語言可用於任何已映射的 SQL 陳述式中。

任何使用過 JSTL 或類似 XML 文本處理器的人，對動態 SQL 元素都不會感到陌生。在 MyBatis 先前的版本中，有許多元素需要了解與掌握。MyBatis 3 對此進行了大幅改進，現在需要使用的元素不到原本的一半。MyBatis 採用強大的 OGNL 運算式，從而省去了大部分其他元素：

- if
- choose (when, otherwise)
- trim (where, set)
- foreach

### if

在動態 SQL 中最常見的需求是依條件包含 where 子句的一部分。例如：

```xml
<select id="findActiveBlogWithTitleLike"
     resultType="Blog">
  SELECT * FROM BLOG
  WHERE state = 'ACTIVE'
  <if test="title != null">
    AND title like #{title}
  </if>
</select>
```

此陳述式提供了一種可選的文字搜尋功能。若您未傳入 title，則會回傳所有狀態為 active 的 Blog。但若您傳入了 title，它將會尋找類似該 title 的內容（眼尖的讀者會發現，在此情況下，您的參數值需要包含任何遮罩或萬用字元）。

若我們想要依據 title 與 author 進行可選搜尋呢？首先，我會更改陳述式的名稱使其更具意義。接著只需新增另一個條件即可。

```xml
<select id="findActiveBlogLike"
     resultType="Blog">
  SELECT * FROM BLOG WHERE state = 'ACTIVE'
  <if test="title != null">
    AND title like #{title}
  </if>
  <if test="author != null and author.name != null">
    AND author_name like #{author.name}
  </if>
</select>
```

### choose, when, otherwise

有時候我們不希望所有條件都套用，而是希望在多個選項中僅選擇一種情況。類似 Java 中的 switch 陳述式，MyBatis 提供了 choose 元素。

讓我們使用上面的範例，但現在如果提供了 title 就僅搜尋 title，若提供了 author 就僅搜尋 author。若兩者皆未提供，則僅回傳精選的 Blog（或許是由管理員策略性挑選的清單，而非回傳大量無意義的隨機 Blog 清單）。

```xml
<select id="findActiveBlogLike"
     resultType="Blog">
  SELECT * FROM BLOG WHERE state = 'ACTIVE'
  <choose>
    <when test="title != null">
      AND title like #{title}
    </when>
    <when test="author != null and author.name != null">
      AND author_name like #{author.name}
    </when>
    <otherwise>
      AND featured = 1
    </otherwise>
  </choose>
</select>
```

### trim, where, set

先前的範例刻意巧妙地避開了一個惡名昭彰的動態 SQL 挑戰。試想如果我們回到 "if" 範例，但這次將 state 條件也變成動態的，會發生什麼情況。

```xml
<select id="findActiveBlogLike"
     resultType="Blog">
  SELECT * FROM BLOG
  WHERE
  <if test="state != null">
    state = #{state}
  </if>
  <if test="title != null">
    AND title like #{title}
  </if>
  <if test="author != null and author.name != null">
    AND author_name like #{author.name}
  </if>
</select>
```

如果沒有任何條件滿足會發生什麼？您最終會得到看起來像這樣的 SQL：

```sql
SELECT * FROM BLOG
WHERE
```

這將會執行失敗。那如果只有第二個條件滿足呢？您最終會得到看起來像這樣的 SQL：

```sql
SELECT * FROM BLOG
WHERE
AND title like 'someTitle'
```

這同樣會執行失敗。這個問題不容易單純靠條件判斷解決，如果您曾經寫過這種邏輯，很可能再也不想寫第二次了。

MyBatis 提供了一個簡單的解決方案，在 90% 的情況下都能正常運作。而在無法正常運作的情況下，您可以自行客製化使其生效。只需一個簡單的改變，一切就能正常運作：

```xml
<select id="findActiveBlogLike"
     resultType="Blog">
  SELECT * FROM BLOG
  <where>
    <if test="state != null">
         state = #{state}
    </if>
    <if test="title != null">
        AND title like #{title}
    </if>
    <if test="author != null and author.name != null">
        AND author_name like #{author.name}
    </if>
  </where>
</select>
```

*where* 元素知道只有在內含標籤回傳任何內容時，才插入 "WHERE"。此外，如果該內容以 "AND" 或 "OR" 開頭，它知道將其去除。

如果 *where* 元素的行為不完全符合您的預期，您可以透過定義自己的 trim 元素來自訂它。例如，與 *where* 元素等效的 trim 設定為：

```xml
<trim prefix="WHERE" prefixOverrides="AND |OR ">
  ...
</trim>
```

*prefixOverrides* 屬性接受以垂直線（pipe）分隔的欲覆寫文字清單，其中空格<u>具有</u>實質意義。其運作結果是移除 *prefixOverrides* 屬性中指定的任何內容，並插入 *prefix* 屬性中的內容。

對於動態 update 陳述式，有一個類似的解決方案稱為 *set*。*set* 元素可用於動態包含要更新的欄位，並略過其他欄位。例如：

```xml
<update id="updateAuthorIfNecessary">
  update Author
    <set>
      <if test="username != null">username=#{username},</if>
      <if test="password != null">password=#{password},</if>
      <if test="email != null">email=#{email},</if>
      <if test="bio != null">bio=#{bio}</if>
    </set>
  where id=#{id}
</update>
```

在這裡，*set* 元素將動態前置 SET 關鍵字，同時在條件套用後消除數值指派後面可能多餘的結尾逗號。

或者，您也可以使用 *trim* 元素達成相同的效果：

```xml
<trim prefix="SET" suffixOverrides=",">
  ...
</trim>
```

請注意在此情況下，我們覆寫的是字尾（suffix），同時仍然附加字首（prefix）。

### foreach

動態 SQL 的另一個常見需求是反覆運算集合，通常用於建構 IN 條件。例如：

```xml
<select id="selectPostIn" resultType="domain.blog.Post">
  SELECT *
  FROM POST P
  <where>
    <foreach item="item" index="index" collection="list"
        open="ID in (" separator="," close=")" nullable="true">
          #{item}
    </foreach>
  </where>
</select>
```

*foreach* 元素非常強大，它允許您指定一個集合，並宣告可在元素主體內部使用的 item 與 index 變數。它還允許您指定開頭與結尾字串，並新增在每次反覆運算之間放置的分隔符號。該元素非常聰明，不會意外附加多餘的分隔符號。

<span class="label important">注意</span> 您可以將任何 Iterable 物件（例如 List、Set 等），以及任何 Map 或陣列物件傳遞給 foreach 作為 collection 參數。使用 Iterable 或陣列時，index 為目前反覆運算的次數，而 item 值為本次反覆運算中擷取的元素。使用 Map（或 Map.Entry 物件的集合）時，index 為索引鍵物件，item 則為數值物件。

至此結束了關於 XML 設定檔與 XML 映射檔的討論。下一節將詳細討論 Java API，以便您充分發揮所建立映射的最大效益。

### script

若要在使用註解的 Mapper 類別中使用動態 SQL，可以使用 *script* 元素。例如：

```java
    @Update({"<script>",
      "update Author",
      "  <set>",
      "    <if test='username != null'>username=#{username},</if>",
      "    <if test='password != null'>password=#{password},</if>",
      "    <if test='email != null'>email=#{email},</if>",
      "    <if test='bio != null'>bio=#{bio}</if>",
      "  </set>",
      "where id=#{id}",
      "</script>"})
    void updateAuthorValues(Author author);
```

### bind

`bind` 元素讓您可以從 OGNL 運算式建立變數，並將其繫結至內容環境中。例如：

```xml
<select id="selectBlogsLike" resultType="Blog">
  <bind name="pattern" value="'%' + _parameter.getTitle() + '%'" />
  SELECT * FROM BLOG
  WHERE title LIKE #{pattern}
</select>
```

### 多資料庫廠商支援 (Multi-db vendor support)

若已設定 databaseIdProvider，則在動態程式碼中可以使用 "_databaseId" 變數，因此您可以根據資料庫廠商建置不同的陳述式。請參考以下範例：

```xml
<insert id="insert">
  <selectKey keyProperty="id" resultType="int" order="BEFORE">
    <if test="_databaseId == 'oracle'">
      select seq_users.nextval from dual
    </if>
    <if test="_databaseId == 'db2'">
      select nextval for seq_users from sysibm.sysdummy1
    </if>
  </selectKey>
  insert into users values (#{id}, #{name})
</insert>
```

### 動態 SQL 的可插拔指令碼語言 (Pluggable Scripting Languages For Dynamic SQL)

從 3.2 版開始，MyBatis 支援可插拔的指令碼語言，因此您可以外掛語言驅動程式，並使用該語言撰寫動態 SQL 查詢。

您可以透過實作以下介面來外掛語言：

```java
public interface LanguageDriver {
  ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);
  SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType);
  SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);
}
```

擁有了自訂語言驅動程式之後，您可以透過在 mybatis-config.xml 檔案中進行設定將其設為預設：

```xml
<typeAliases>
  <typeAlias type="org.sample.MyLanguageDriver" alias="myLanguage"/>
</typeAliases>
<settings>
  <setting name="defaultScriptingLanguage" value="myLanguage"/>
</settings>
```

除了變更預設值之外，您還可以透過新增 `lang` 屬性為特定陳述式指定語言，如下所示：

```xml
<select id="selectBlog" lang="myLanguage">
  SELECT * FROM BLOG
</select>
```

或者，若您使用映射器介面，則可以使用 `@Lang` 註解：

```java
public interface Mapper {
  @Lang(MyLanguageDriver.class)
  @Select("SELECT * FROM BLOG")
  List<Blog> selectBlog();
}
```

<span class="label important">注意</span> 您可以使用 Apache Velocity 作為動態語言。詳情請參閱 MyBatis-Velocity 專案。

您在前面幾節中看到的所有 XML 標籤，均由預設的 MyBatis 語言提供，該語言由驅動程式 `org.apache.ibatis.scripting.xmltags.XMLLanguageDriver` 提供，其別名為 `xml`。
