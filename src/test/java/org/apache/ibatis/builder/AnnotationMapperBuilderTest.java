/**
 *    Copyright 2009-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.builder;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.junit.Test;

import java.io.InputStream;
import java.util.regex.Pattern;

import static com.googlecode.catchexception.apis.BDDCatchException.caughtException;
import static com.googlecode.catchexception.apis.BDDCatchException.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;

public class AnnotationMapperBuilderTest {

  @Test
  public void withOptions() throws Exception {
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
  public void withOptionsAndWithoutOptionsAttributesWhenSpecifyDefaultValue() throws Exception {
    Configuration configuration = new Configuration();
    configuration.setDefaultResultSetType(ResultSetType.SCROLL_INSENSITIVE);
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, Mapper.class);
    builder.parse();

    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithOptionsAndWithoutOptionsAttributes");
    assertThat(mappedStatement.getResultSetType()).isEqualTo(ResultSetType.SCROLL_INSENSITIVE);
  }


  @Test
  public void withOptionsAndWithoutOptionsAttributesWhenNotSpecifyDefaultValue() throws Exception {
    Configuration configuration = new Configuration();
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, Mapper.class);
    builder.parse();

    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithOptionsAndWithoutOptionsAttributes");
    assertThat(mappedStatement.getResultSetType()).isNull();
  }

  @Test
  public void withoutOptionsWhenSpecifyDefaultValue() throws Exception {
    Configuration configuration = new Configuration();
    configuration.setDefaultResultSetType(ResultSetType.SCROLL_INSENSITIVE);
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, Mapper.class);
    builder.parse();

    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithoutOptions");
    assertThat(mappedStatement.getResultSetType()).isEqualTo(ResultSetType.SCROLL_INSENSITIVE);
  }

  @Test
  public void withoutOptionsWhenNotSpecifyDefaultValue() throws Exception {
    Configuration configuration = new Configuration();
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, Mapper.class);
    builder.parse();

    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithoutOptions");
    assertThat(mappedStatement.getResultSetType()).isNull();
  }

  public interface Mapper {

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
