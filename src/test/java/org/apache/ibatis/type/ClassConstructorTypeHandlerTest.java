/*
 *    Copyright 2009-2026 the original author or authors.
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
package org.apache.ibatis.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class ClassConstructorTypeHandlerTest {

  @Test
  void classConstructorReceivesRawTypeOfParameterizedProperty() throws Exception {
    TypeHandlerRegistry registry = new TypeHandlerRegistry();
    Type dataType = Credential.class.getDeclaredField("data").getGenericType();
    Type tagsType = Credential.class.getDeclaredField("tags").getGenericType();

    ClassOnlyJsonTypeHandler dataHandler = (ClassOnlyJsonTypeHandler) registry.getInstance(dataType,
        ClassOnlyJsonTypeHandler.class);
    ClassOnlyJsonTypeHandler tagsHandler = (ClassOnlyJsonTypeHandler) registry.getInstance(tagsType,
        ClassOnlyJsonTypeHandler.class);

    assertSame(Map.class, dataHandler.getJavaType());
    assertSame(List.class, tagsHandler.getJavaType());
  }

  @Test
  void classConstructorIsPreferredOverNoArgConstructor() throws Exception {
    Type dataType = Credential.class.getDeclaredField("data").getGenericType();
    ClassOrNoArgTypeHandler handler = (ClassOrNoArgTypeHandler) new TypeHandlerRegistry().getInstance(dataType,
        ClassOrNoArgTypeHandler.class);
    assertSame(Map.class, handler.getJavaType());
  }

  @Test
  void typeConstructorStillReceivesTheFullParameterizedType() throws Exception {
    Type metadataType = Credential.class.getDeclaredField("metadata").getGenericType();
    TypeConstructorJsonTypeHandler handler = (TypeConstructorJsonTypeHandler) new TypeHandlerRegistry()
        .getInstance(metadataType, TypeConstructorJsonTypeHandler.class);

    ParameterizedType parameterizedType = assertInstanceOf(ParameterizedType.class, handler.getJavaType());
    assertSame(Map.class, parameterizedType.getRawType());
    assertSame(String.class, parameterizedType.getActualTypeArguments()[0]);
    assertSame(String.class, parameterizedType.getActualTypeArguments()[1]);
  }

  @Test
  void noArgConstructorIsUsedWhenClassConstructorIsAbsent() throws Exception {
    Type dataType = Credential.class.getDeclaredField("data").getGenericType();
    NoArgTypeHandler handler = (NoArgTypeHandler) new TypeHandlerRegistry().getInstance(dataType,
        NoArgTypeHandler.class);
    assertSame(Void.class, handler.getJavaType());
  }

  @Test
  void missingConstructorStillFails() throws Exception {
    Type dataType = Credential.class.getDeclaredField("data").getGenericType();
    TypeException exception = assertThrows(TypeException.class,
        () -> new TypeHandlerRegistry().getInstance(dataType, StringConstructorTypeHandler.class));
    assertInstanceOf(NoSuchMethodException.class, exception.getCause());
  }

  @Test
  void mapperParsingResolvesClassConstructorForGenericProperties() throws Exception {
    Configuration configuration = new Configuration();
    String resource = "org/apache/ibatis/type/ClassConstructorTypeHandlerMapper.xml";
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, configuration, resource,
          configuration.getSqlFragments());
      builder.parse();
    }

    ResultMap resultMap = configuration
        .getResultMap("org.apache.ibatis.type.ClassConstructorTypeHandlerTest.credentialMap");
    ClassOnlyJsonTypeHandler dataHandler = handler(resultMap, "data", ClassOnlyJsonTypeHandler.class);
    ClassOnlyJsonTypeHandler tagsHandler = handler(resultMap, "tags", ClassOnlyJsonTypeHandler.class);
    ClassOnlyJsonTypeHandler nameHandler = handler(resultMap, "name", ClassOnlyJsonTypeHandler.class);
    ClassOnlyJsonTypeHandler explicitHandler = handler(resultMap, "explicitData", ClassOnlyJsonTypeHandler.class);
    TypeConstructorJsonTypeHandler metadataHandler = handler(resultMap, "metadata",
        TypeConstructorJsonTypeHandler.class);

    assertSame(Map.class, dataHandler.getJavaType());
    assertSame(List.class, tagsHandler.getJavaType());
    assertSame(String.class, nameHandler.getJavaType());
    assertSame(Map.class, explicitHandler.getJavaType());
    ParameterizedType metadataType = assertInstanceOf(ParameterizedType.class, metadataHandler.getJavaType());
    assertSame(Map.class, metadataType.getRawType());
    assertEquals(String.class, metadataType.getActualTypeArguments()[0]);
    assertEquals(String.class, metadataType.getActualTypeArguments()[1]);
  }

  private static <T extends TypeHandler<?>> T handler(ResultMap resultMap, String property, Class<T> handlerType) {
    for (ResultMapping mapping : resultMap.getResultMappings()) {
      if (property.equals(mapping.getProperty())) {
        return handlerType.cast(mapping.getTypeHandler());
      }
    }
    throw new AssertionError("Missing result mapping for " + property);
  }

  public static class Credential {
    private Integer id;
    private String name;
    private Map<String, Object> data;
    private List<String> tags;
    private Map<String, String> metadata;
    private Map<String, Object> explicitData;

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Map<String, Object> getData() {
      return data;
    }

    public void setData(Map<String, Object> data) {
      this.data = data;
    }

    public List<String> getTags() {
      return tags;
    }

    public void setTags(List<String> tags) {
      this.tags = tags;
    }

    public Map<String, String> getMetadata() {
      return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
      this.metadata = metadata;
    }

    public Map<String, Object> getExplicitData() {
      return explicitData;
    }

    public void setExplicitData(Map<String, Object> explicitData) {
      this.explicitData = explicitData;
    }
  }

  public static class ClassOnlyJsonTypeHandler implements TypeHandler<Object> {
    private final Class<?> javaType;

    public ClassOnlyJsonTypeHandler(Class<?> javaType) {
      this.javaType = javaType;
    }

    public Class<?> getJavaType() {
      return javaType;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException {
      return null;
    }

    @Override
    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
      return null;
    }

    @Override
    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
      return null;
    }
  }

  public static class ClassOrNoArgTypeHandler implements TypeHandler<Object> {
    private final Class<?> javaType;

    public ClassOrNoArgTypeHandler() {
      this.javaType = null;
    }

    public ClassOrNoArgTypeHandler(Class<?> javaType) {
      this.javaType = javaType;
    }

    public Class<?> getJavaType() {
      return javaType;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException {
      return null;
    }

    @Override
    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
      return null;
    }

    @Override
    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
      return null;
    }
  }

  public static class TypeConstructorJsonTypeHandler implements TypeHandler<Object> {
    private final Type javaType;

    public TypeConstructorJsonTypeHandler(Type javaType) {
      this.javaType = javaType;
    }

    public TypeConstructorJsonTypeHandler(Class<?> javaType) {
      this.javaType = javaType;
    }

    public Type getJavaType() {
      return javaType;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException {
      return null;
    }

    @Override
    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
      return null;
    }

    @Override
    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
      return null;
    }
  }

  public static class NoArgTypeHandler implements TypeHandler<Object> {
    public Class<?> getJavaType() {
      return Void.class;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException {
      return null;
    }

    @Override
    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
      return null;
    }

    @Override
    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
      return null;
    }
  }

  public static class StringConstructorTypeHandler implements TypeHandler<Object> {
    public StringConstructorTypeHandler(String ignored) {
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException {
      return null;
    }

    @Override
    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
      return null;
    }

    @Override
    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
      return null;
    }
  }
}
