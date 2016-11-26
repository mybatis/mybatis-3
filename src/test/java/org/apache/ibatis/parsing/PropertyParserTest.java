/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.parsing;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class PropertyParserTest {

  @Test
  public void replaceToVariableValue() {
    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    props.setProperty("key", "value");
    props.setProperty("tableName", "members");
    props.setProperty("orderColumn", "member_id");
    props.setProperty("a:b", "c");
    Assert.assertThat(PropertyParser.parse("${key}", props), Is.is("value"));
    Assert.assertThat(PropertyParser.parse("${key:aaaa}", props), Is.is("value"));
    Assert.assertThat(PropertyParser.parse("SELECT * FROM ${tableName:users} ORDER BY ${orderColumn:id}", props), Is.is("SELECT * FROM members ORDER BY member_id"));

    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "false");
    Assert.assertThat(PropertyParser.parse("${a:b}", props), Is.is("c"));

    props.remove(PropertyParser.KEY_ENABLE_DEFAULT_VALUE);
    Assert.assertThat(PropertyParser.parse("${a:b}", props), Is.is("c"));

  }

  @Test
  public void notReplace() {
    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    Assert.assertThat(PropertyParser.parse("${key}", props), Is.is("${key}"));
    Assert.assertThat(PropertyParser.parse("${key}", null), Is.is("${key}"));

    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "false");
    Assert.assertThat(PropertyParser.parse("${a:b}", props), Is.is("${a:b}"));

    props.remove(PropertyParser.KEY_ENABLE_DEFAULT_VALUE);
    Assert.assertThat(PropertyParser.parse("${a:b}", props), Is.is("${a:b}"));

  }

  @Test
  public void applyDefaultValue() {
    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    Assert.assertThat(PropertyParser.parse("${key:default}", props), Is.is("default"));
    Assert.assertThat(PropertyParser.parse("SELECT * FROM ${tableName:users} ORDER BY ${orderColumn:id}", props), Is.is("SELECT * FROM users ORDER BY id"));
    Assert.assertThat(PropertyParser.parse("${key:}", props), Is.is(""));
    Assert.assertThat(PropertyParser.parse("${key: }", props), Is.is(" "));
    Assert.assertThat(PropertyParser.parse("${key::}", props), Is.is(":"));
  }

  @Test
  public void applyCustomSeparator() {
    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    props.setProperty(PropertyParser.KEY_DEFAULT_VALUE_SEPARATOR, "?:");
    Assert.assertThat(PropertyParser.parse("${key?:default}", props), Is.is("default"));
    Assert.assertThat(PropertyParser.parse("SELECT * FROM ${schema?:prod}.${tableName == null ? 'users' : tableName} ORDER BY ${orderColumn}", props), Is.is("SELECT * FROM prod.${tableName == null ? 'users' : tableName} ORDER BY ${orderColumn}"));
    Assert.assertThat(PropertyParser.parse("${key?:}", props), Is.is(""));
    Assert.assertThat(PropertyParser.parse("${key?: }", props), Is.is(" "));
    Assert.assertThat(PropertyParser.parse("${key?::}", props), Is.is(":"));
  }

}
