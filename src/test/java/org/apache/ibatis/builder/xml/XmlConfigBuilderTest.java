/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.builder.xml;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlConfigBuilderTest {

  /**
   * set checkMapperMethodRelatedMappedStatementExist= true , and there is one  RelatedMappedStatement(updateByName) not exist
   * <p>
   * and throw exception when startup
   * <p>
   * and this will throw exception to interrupt startup and find bug as soon as possible
   */
  @Test
  public void testParseExceptionThrowWhenNoMappedStatement() {
    boolean noExceptionThrow = true;
    try {
      String resource = "org/apache/ibatis/builder/xml/UserMapperConfig.xml";
      InputStream inputStream = Resources.getResourceAsStream(resource);
      new SqlSessionFactoryBuilder().build(inputStream);
    } catch (Exception exp) {
      assertThat(exp.getMessage().contains("related mappedStatement not found")).isTrue();
      noExceptionThrow = false;
    }
    assertThat(noExceptionThrow).isFalse();
  }

  /**
   * set checkMapperMethodRelatedMappedStatementExist= true , and all RelatedMappedStatement exist and no exception throw
   */
  @Test
  public void testParseNoExceptionThrow() {
    boolean noExceptionThrow = true;
    try {
      String resource = "org/apache/ibatis/builder/xml/ManMapperConfig.xml";
      InputStream inputStream = Resources.getResourceAsStream(resource);
      new SqlSessionFactoryBuilder().build(inputStream);
    } catch (Exception exp) {
      assertThat(exp.getMessage().contains("related mappedStatement not found")).isTrue();
      noExceptionThrow = false;
    }
    assertThat(noExceptionThrow).isTrue();
  }

  /**
   * set checkMapperMethodRelatedMappedStatementExist= false , and not check mapper method related mapped statement exist
   */
  @Test
  public void testParseNotCheckMapperMethodRelatedMappedStatementExist() {
    boolean noExceptionThrow = true;
    try {
      String resource = "org/apache/ibatis/builder/xml/PersonMapperConfig.xml";
      InputStream inputStream = Resources.getResourceAsStream(resource);
      new SqlSessionFactoryBuilder().build(inputStream);
    } catch (Exception exp) {
      assertThat(exp.getMessage().contains("related mappedStatement not found")).isTrue();
      noExceptionThrow = false;
    }
    assertThat(noExceptionThrow).isTrue();
  }
}
