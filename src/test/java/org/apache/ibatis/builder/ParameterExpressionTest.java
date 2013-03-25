/*
 * Copyright 2012-2013 MyBatis.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.builder;

import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class ParameterExpressionTest {

  @Test
  public void simpleProperty() {
    Map<String, String> result = new ParameterExpression("id");
    Assert.assertEquals(1, result.size());
    Assert.assertEquals("id", result.get("property"));
  }

  @Test
  public void simplePropertyWithOldStyleJdbcType() {
    Map<String, String> result = new ParameterExpression("id:VARCHAR");
    Assert.assertEquals(2, result.size());
    Assert.assertEquals("id", result.get("property"));
    Assert.assertEquals("VARCHAR", result.get("jdbcType"));
  }

  @Test
  public void oldStyleJdbcTypeWithExtraWhitespaces() {
    Map<String, String> result = new ParameterExpression(" id :  VARCHAR ");
    Assert.assertEquals(2, result.size());
    Assert.assertEquals("id", result.get("property"));
    Assert.assertEquals("VARCHAR", result.get("jdbcType"));
  }

  @Test
  public void expressionWithOldStyleJdbcType() {
    Map<String, String> result = new ParameterExpression("(id.toString()):VARCHAR");
    Assert.assertEquals(2, result.size());
    Assert.assertEquals("id.toString()", result.get("expression"));
    Assert.assertEquals("VARCHAR", result.get("jdbcType"));
  }

  @Test
  public void simplePropertyWithOneAttribute() {
    Map<String, String> result = new ParameterExpression("id,name=value");
    Assert.assertEquals(2, result.size());
    Assert.assertEquals("id", result.get("property"));
    Assert.assertEquals("value", result.get("name"));
  }

  @Test
  public void expressionWithOneAttribute() {
    Map<String, String> result = new ParameterExpression("(id.toString()),name=value");
    Assert.assertEquals(2, result.size());
    Assert.assertEquals("id.toString()", result.get("expression"));
    Assert.assertEquals("value", result.get("name"));
  }

  @Test
  public void simplePropertyWithManyAttributes() {
    Map<String, String> result = new ParameterExpression("id, attr1=val1, attr2=val2, attr3=val3");
    Assert.assertEquals(4, result.size());
    Assert.assertEquals("id", result.get("property"));
    Assert.assertEquals("val1", result.get("attr1"));
    Assert.assertEquals("val2", result.get("attr2"));
    Assert.assertEquals("val3", result.get("attr3"));
  }

  @Test
  public void expressionWithManyAttributes() {
    Map<String, String> result = new ParameterExpression("(id.toString()), attr1=val1, attr2=val2, attr3=val3");
    Assert.assertEquals(4, result.size());
    Assert.assertEquals("id.toString()", result.get("expression"));
    Assert.assertEquals("val1", result.get("attr1"));
    Assert.assertEquals("val2", result.get("attr2"));
    Assert.assertEquals("val3", result.get("attr3"));
  }

  @Test
  public void simplePropertyWithOldStyleJdbcTypeAndAttributes() {
    Map<String, String> result = new ParameterExpression("id:VARCHAR, attr1=val1, attr2=val2");
    Assert.assertEquals(4, result.size());
    Assert.assertEquals("id", result.get("property"));
    Assert.assertEquals("VARCHAR", result.get("jdbcType"));
    Assert.assertEquals("val1", result.get("attr1"));
    Assert.assertEquals("val2", result.get("attr2"));
  }

  @Test
  public void simplePropertyWithSpaceAndManyAttributes() {
    Map<String, String> result = new ParameterExpression("user name, attr1=val1, attr2=val2, attr3=val3");
    Assert.assertEquals(4, result.size());
    Assert.assertEquals("user name", result.get("property"));
    Assert.assertEquals("val1", result.get("attr1"));
    Assert.assertEquals("val2", result.get("attr2"));
    Assert.assertEquals("val3", result.get("attr3"));
  }

  @Test
  public void shouldIgnoreLeadingAndTrailingSpaces() {
    Map<String, String> result = new ParameterExpression(" id , jdbcType =  VARCHAR,  attr1 = val1 ,  attr2 = val2 ");
    Assert.assertEquals(4, result.size());
    Assert.assertEquals("id", result.get("property"));
    Assert.assertEquals("VARCHAR", result.get("jdbcType"));
    Assert.assertEquals("val1", result.get("attr1"));
    Assert.assertEquals("val2", result.get("attr2"));
  }

}
