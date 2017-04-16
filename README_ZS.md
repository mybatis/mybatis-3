Mybatis 修改记录
=================================


### 2017-04-16 

针对ORACLE数据库表全是CHAR类型的情况修改PreparedStatement为Oracle驱动包中的OraclePreparedStatement类。

 ```
OraclePreparedStatement ops = ps.unwrap(OraclePreparedStatement.class);
```
 `unwrap` 方法需要数据库连接池有对应实现方法，一般会默认实现，如：druid

本次修改的文件如下：
```
src/main/java
- org/apache/ibatis/jdbc/SqlRunner.java
- org/apache/ibatis/scripting/defaults/DefaultParameterHandler.java
- org/apache/ibatis/type/* ，目前只实现了String、Long、Integer、Double 等常用基础类型，其它类型暂不可用

src/test/java
- org/apache/ibatis/jdbc/SqlRunner.java 测试方法

```


<br><br><br><br>


`QQ:916931772`   欢迎加企鹅交流。

<br><br><br><br><br><br><br><br><br><br><br><br>