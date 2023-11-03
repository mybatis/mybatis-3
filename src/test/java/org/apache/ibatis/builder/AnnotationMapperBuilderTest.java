/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.builder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class AnnotationMapperBuilderTest {

  @Test
  void withOptions() {
    Configuration configuration = new Configuration();
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, Mapper.class);
    builder.parse();

    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithOptions");
    assertThat(mappedStatement.getFetchSize()).isEqualTo(200);
    assertThat(mappedStatement.getTimeout()).isEqualTo(10);
    assertThat(mappedStatement.getStatementType()).isEqualTo(StatementType.STATEMENT);
    assertThat(mappedStatement.getResultSetType()).isEqualTo(ResultSetType.SCROLL_INSENSITIVE);
    assertThat(mappedStatement.isFlushCacheRequired()).isTrue();
    assertThat(mappedStatement.isUseCache()).isFalse();
    assertThat(mappedStatement.getResultSets()).containsExactly("resultSets");

    mappedStatement = configuration.getMappedStatement("insertWithOptions");
    assertThat(mappedStatement.getKeyGenerator()).isInstanceOf(Jdbc3KeyGenerator.class);
    assertThat(mappedStatement.getKeyColumns()).containsExactly("key_column");
    assertThat(mappedStatement.getKeyProperties()).containsExactly("keyProperty");
  }

  @Test
  void withOptionsAndWithoutOptionsAttributesWhenSpecifyDefaultValue() {
    Configuration configuration = new Configuration();
    configuration.setDefaultResultSetType(ResultSetType.SCROLL_INSENSITIVE);
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, Mapper.class);
    builder.parse();

    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithOptionsAndWithoutOptionsAttributes");
    assertThat(mappedStatement.getResultSetType()).isEqualTo(ResultSetType.SCROLL_INSENSITIVE);
  }

  @Test
  void withOptionsAndWithoutOptionsAttributesWhenNotSpecifyDefaultValue() {
    Configuration configuration = new Configuration();
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, Mapper.class);
    builder.parse();

    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithOptionsAndWithoutOptionsAttributes");
    assertThat(mappedStatement.getResultSetType()).isEqualTo(ResultSetType.DEFAULT);
  }

  @Test
  void withoutOptionsWhenSpecifyDefaultValue() {
    Configuration configuration = new Configuration();
    configuration.setDefaultResultSetType(ResultSetType.SCROLL_INSENSITIVE);
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, Mapper.class);
    builder.parse();

    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithoutOptions");
    assertThat(mappedStatement.getResultSetType()).isEqualTo(ResultSetType.SCROLL_INSENSITIVE);
  }

  @Test
  void withoutOptionsWhenNotSpecifyDefaultValue() {
    Configuration configuration = new Configuration();
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, Mapper.class);
    builder.parse();

    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithoutOptions");
    assertThat(mappedStatement.getResultSetType()).isEqualTo(ResultSetType.DEFAULT);
  }

  @Test
  void withSelectInclude() {
    Configuration configuration = new Configuration();
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, Mapper.class);
    builder.parse();

    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithInclude");
    BoundSql boundsql = mappedStatement.getSqlSource().getBoundSql(null);
    String sql = boundsql.getSql();
    assertThat(sql).isEqualTo("select * from test  where id = ?");
  }

  @Test
  void withUpdateInclude() {
    Configuration configuration = new Configuration();
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, Mapper.class);
    builder.parse();

    MappedStatement mappedStatement = configuration.getMappedStatement("updateWithInclude");
    BoundSql boundsql = mappedStatement.getSqlSource().getBoundSql(null);
    String sql = boundsql.getSql();
    assertThat(sql).isEqualTo("update test set  name = ? where id = ?");
  }

  @Test
  void withSelectIncludeAndIf() {
    Configuration configuration = new Configuration();
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, Mapper.class);
    builder.parse();

    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithIncludeAndIf");
    HashMap<String, Object> params = new HashMap<>();
    params.put("name", "name");
    BoundSql boundsql = mappedStatement.getSqlSource().getBoundSql(params);
    String sql = boundsql.getSql();
    assertThat(sql).isEqualTo("select name  from test");
  }

  interface Mapper {

    @Sql(id = "selectId", value = "select * from test")
    @Sql(id = "updateId", value = "update test set")
    @Sql(id = "selectWithIfId", value = "<script><if test='id != null'>id</if><if test='name != null'>name</if></script>")
    void provideSql();

    @Select("<script><include refid='selectId'/> where id = #{id}</script>")
    String selectWithInclude(Integer id);

    @Update("<script><include refid='updateId'/> name = #{name} where id = #{id}</script>")
    String updateWithInclude(Integer id, String name);

    @Select("<script>select<include refid='selectWithIfId'/> from test</script>")
    String selectWithIncludeAndIf(Integer id, String name);

    @Insert("insert into test (name) values(#{name})")
    @Options(useGeneratedKeys = true, keyColumn = "key_column", keyProperty = "keyProperty")
    void insertWithOptions(String name);

    @Select("select * from test")
    @Options(fetchSize = 200, timeout = 10, statementType = StatementType.STATEMENT, resultSetType = ResultSetType.SCROLL_INSENSITIVE, flushCache = Options.FlushCachePolicy.TRUE, useCache = false, resultSets = "resultSets")
    String selectWithOptions(Integer id);

    @Select("select * from test")
    @Options
    String selectWithOptionsAndWithoutOptionsAttributes(Integer id);

    @Select("select * from test")
    String selectWithoutOptions(Integer id);

  }

}
