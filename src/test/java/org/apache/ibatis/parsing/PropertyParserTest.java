/*
 *    Copyright 2009-2020 the original author or authors.
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

import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PropertyParserTest {

  @Test
  void replaceToVariableValue() {
    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    props.setProperty("key", "value");
    props.setProperty("tableName", "members");
    props.setProperty("orderColumn", "member_id");
    props.setProperty("a:b", "c");
    Assertions.assertThat(PropertyParser.parse("${key}", props)).isEqualTo("value");
    Assertions.assertThat(PropertyParser.parse("${key:aaaa}", props)).isEqualTo("value");
    Assertions.assertThat(PropertyParser.parse("SELECT * FROM ${tableName:users} ORDER BY ${orderColumn:id}", props)).isEqualTo("SELECT * FROM members ORDER BY member_id");

    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "false");
    Assertions.assertThat(PropertyParser.parse("${a:b}", props)).isEqualTo("c");

    props.remove(PropertyParser.KEY_ENABLE_DEFAULT_VALUE);
    Assertions.assertThat(PropertyParser.parse("${a:b}", props)).isEqualTo("c");

  }

  @Test
  void notReplace() {
    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    Assertions.assertThat(PropertyParser.parse("${key}", props)).isEqualTo("${key}");
    Assertions.assertThat(PropertyParser.parse("${key}", null)).isEqualTo("${key}");

    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "false");
    Assertions.assertThat(PropertyParser.parse("${a:b}", props)).isEqualTo("${a:b}");

    props.remove(PropertyParser.KEY_ENABLE_DEFAULT_VALUE);
    Assertions.assertThat(PropertyParser.parse("${a:b}", props)).isEqualTo("${a:b}");

  }

  @Test
  void applyDefaultValue() {
    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    Assertions.assertThat(PropertyParser.parse("${key:default}", props)).isEqualTo("default");
    Assertions.assertThat(PropertyParser.parse("SELECT * FROM ${tableName:users} ORDER BY ${orderColumn:id}", props)).isEqualTo("SELECT * FROM users ORDER BY id");
    Assertions.assertThat(PropertyParser.parse("${key:}", props)).isEmpty();
    Assertions.assertThat(PropertyParser.parse("${key: }", props)).isEqualTo(" ");
    Assertions.assertThat(PropertyParser.parse("${key::}", props)).isEqualTo(":");
  }

  @Test
  void applyCustomSeparator() {
    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    props.setProperty(PropertyParser.KEY_DEFAULT_VALUE_SEPARATOR, "?:");
    Assertions.assertThat(PropertyParser.parse("${key?:default}", props)).isEqualTo("default");
    Assertions.assertThat(PropertyParser.parse("SELECT * FROM ${schema?:prod}.${tableName == null ? 'users' : tableName} ORDER BY ${orderColumn}", props)).isEqualTo("SELECT * FROM prod.${tableName == null ? 'users' : tableName} ORDER BY ${orderColumn}");
    Assertions.assertThat(PropertyParser.parse("${key?:}", props)).isEmpty();
    Assertions.assertThat(PropertyParser.parse("${key?: }", props)).isEqualTo(" ");
    Assertions.assertThat(PropertyParser.parse("${key?::}", props)).isEqualTo(":");
  }

}
