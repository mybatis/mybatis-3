/*
 *    Copyright 2009-2012 the original author or authors.
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.Test;

public class XmlConfigBuilderTest {

  @Test
  public void shouldSuccessfullyLoadMinimalXMLConfigFile() throws Exception {
    String resource = "org/apache/ibatis/builder/MinimalMapperConfig.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
    Configuration config = builder.parse();
    assertNotNull(config);
  }

  enum MyEnum {
    ONE, TWO
  }

  public static class EnumOrderTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    private E[] constants;

    public EnumOrderTypeHandler(Class<E> javaType) {
      constants = javaType.getEnumConstants();
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
      ps.setInt(i, parameter.ordinal() + 1); // 0 means NULL so add +1
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
      int index = rs.getInt(columnName) - 1;
      return index < 0 ? null : constants[index];
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
      int index = rs.getInt(rs.getInt(columnIndex)) - 1;
      return index < 0 ? null : constants[index];
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
      int index = cs.getInt(columnIndex) - 1;
      return index < 0 ? null : constants[index];
    }
  }

  @Test
  public void registerJavaTypeInitializingTypeHandler() {
    final String MAPPER_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
        + "<!DOCTYPE configuration PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-config.dtd\">\n" 
        + "<configuration>\n" 
        + "  <typeHandlers>\n"
        + "    <typeHandler javaType=\"org.apache.ibatis.builder.XmlConfigBuilderTest$MyEnum\"\n"
        + "      handler=\"org.apache.ibatis.builder.XmlConfigBuilderTest$EnumOrderTypeHandler\"/>\n" 
        + "  </typeHandlers>\n" 
        + "</configuration>\n";

    XMLConfigBuilder builder = new XMLConfigBuilder(new StringReader(MAPPER_CONFIG));
    builder.parse();

    TypeHandlerRegistry typeHandlerRegistry = builder.getConfiguration().getTypeHandlerRegistry();
    TypeHandler<MyEnum> typeHandler = typeHandlerRegistry.getTypeHandler(MyEnum.class);

    assertTrue(typeHandler instanceof EnumOrderTypeHandler);
    assertArrayEquals(MyEnum.values(), ((EnumOrderTypeHandler) typeHandler).constants);
  }
}
