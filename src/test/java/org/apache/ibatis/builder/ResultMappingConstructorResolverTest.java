/*
 *    Copyright 2009-2025 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ResultMappingConstructorResolverTest {

  static String TEST_ID = "testResultMapId";

  Configuration configuration = new Configuration();

  @Test
  void testResolvesSingleArg() {
    ResultMapping mapping = createConstructorMappingFor(Object.class, "type", "type");

    final ResultMappingConstructorResolver resolver = createResolverFor(ResultType.class, TEST_ID, mapping);
    final List<ResultMapping> mappingList = resolver.resolveWithConstructor();

    assertThat(mappingList).extracting(ResultMapping::getProperty, m -> m.getJavaType().getSimpleName())
        .containsExactly(tuple("type", "String"));
  }

  @Test
  void testResolvesTypeAndOrderWithSingleConstructor() {
    ResultMapping mappingA = createConstructorMappingFor(Object.class, "a", "a");
    ResultMapping mappingB = createConstructorMappingFor(Object.class, "b1", "b1");
    ResultMapping mappingC = createConstructorMappingFor(Object.class, "c", "c");

    // note the incorrect order provided here
    final ResultMappingConstructorResolver resolver = createResolverFor(ResultType2.class, TEST_ID, mappingC, mappingA,
        mappingB);
    final List<ResultMapping> mappingList = resolver.resolveWithConstructor();

    assertThat(mappingList).extracting(ResultMapping::getProperty, mapping -> mapping.getJavaType().getSimpleName())
        .containsExactly(tuple("a", "long"), tuple("b1", "long"), tuple("c", "String"));
  }

  @Test
  void testCannotResolveAmbiguous() {
    ResultMapping mappingA = createConstructorMappingFor(Object.class, "a", "a");
    ResultMapping mappingB = createConstructorMappingFor(Object.class, "b", "b");
    ResultMapping mappingC = createConstructorMappingFor(Object.class, "c", "c");

    // there are two matching constructors here, we need to clarify with type info
    final ResultMappingConstructorResolver resolver = createResolverFor(ResultType1.class, TEST_ID, mappingA, mappingB,
        mappingC);

    assertThatThrownBy(resolver::resolveWithConstructor).isNotNull().isInstanceOf(BuilderException.class)
        .hasMessageContaining(
            "Failed to find a constructor in 'org.apache.ibatis.builder.ResultType1' with arg names [a, b, c]");
  }

  @Test
  void testCanResolveAmbiguousWithMinimalTypeInfo() {
    ResultMapping mappingA = createConstructorMappingFor(Object.class, "a", "a");
    ResultMapping mappingB = createConstructorMappingFor(Object.class, "b", "b");
    ResultMapping mappingC = createConstructorMappingFor(LocalDate.class, "c", "c");

    final ResultMappingConstructorResolver resolver = createResolverFor(ResultType1.class, TEST_ID, mappingA, mappingB,
        mappingC);
    final List<ResultMapping> mappingList = resolver.resolveWithConstructor();

    assertThat(mappingList).extracting(ResultMapping::getProperty, mapping -> mapping.getJavaType().getSimpleName())
        .containsExactly(tuple("a", "long"), tuple("b", "String"), tuple("c", "LocalDate"));
  }

  @Test
  void testCanResolveAmbiguousWithAllTypeInfo() {
    ResultMapping mappingA = createConstructorMappingFor(long.class, "a", "a");
    ResultMapping mappingB = createConstructorMappingFor(String.class, "b", "b");
    ResultMapping mappingC = createConstructorMappingFor(LocalDate.class, "c", "c");

    final ResultMappingConstructorResolver resolver = createResolverFor(ResultType1.class, TEST_ID, mappingA, mappingB,
        mappingC);
    final List<ResultMapping> mappingList = resolver.resolveWithConstructor();

    assertThat(mappingList).extracting(ResultMapping::getProperty, mapping -> mapping.getJavaType().getSimpleName())
        .containsExactly(tuple("a", "long"), tuple("b", "String"), tuple("c", "LocalDate"));
  }

  @Test
  void testCanResolveAmbiguousRandomOrderWithMinimalTypeInfo() {
    ResultMapping mappingA = createConstructorMappingFor(Object.class, "a", "a");
    ResultMapping mappingB = createConstructorMappingFor(Object.class, "b", "b");
    ResultMapping mappingC = createConstructorMappingFor(LocalDate.class, "c", "c");

    final ResultMappingConstructorResolver resolver = createResolverFor(ResultType1.class, TEST_ID, mappingC, mappingA,
        mappingB);
    final List<ResultMapping> mappingList = resolver.resolveWithConstructor();

    assertThat(mappingList).extracting(ResultMapping::getProperty, mapping -> mapping.getJavaType().getSimpleName())
        .containsExactly(tuple("a", "long"), tuple("b", "String"), tuple("c", "LocalDate"));
  }

  @Test
  void testCanResolveAmbiguousRandomOrderWithAllTypeInfo() {
    ResultMapping mappingA = createConstructorMappingFor(long.class, "a", "a");
    ResultMapping mappingB = createConstructorMappingFor(String.class, "b", "b");
    ResultMapping mappingC = createConstructorMappingFor(LocalDate.class, "c", "c");

    final ResultMappingConstructorResolver resolver = createResolverFor(ResultType1.class, TEST_ID, mappingC, mappingA,
        mappingB);
    final List<ResultMapping> mappingList = resolver.resolveWithConstructor();

    assertThat(mappingList).extracting(ResultMapping::getProperty, mapping -> mapping.getJavaType().getSimpleName())
        .containsExactly(tuple("a", "long"), tuple("b", "String"), tuple("c", "LocalDate"));
  }

  @Test
  void testCanResolveOutOfOrderWhenParamIsUsed() {
    ResultMapping mappingA = createConstructorMappingFor(Object.class, "a1", "a1");
    ResultMapping mappingB = createConstructorMappingFor(Object.class, "b1", "b1");
    ResultMapping mappingC = createConstructorMappingFor(Object.class, "c1", "c1");

    final ResultMappingConstructorResolver resolver = createResolverFor(ResultType1.class, TEST_ID, mappingC, mappingA,
        mappingB);
    final List<ResultMapping> mappingList = resolver.resolveWithConstructor();

    assertThat(mappingList).extracting(ResultMapping::getProperty, mapping -> mapping.getJavaType().getSimpleName())
        .containsExactly(tuple("a1", "long"), tuple("b1", "long"), tuple("c1", "String"));
  }

  @Test
  void doesNotResolveWithNoMappingsAsInput() {
    final ResultMappingConstructorResolver resolver = createResolverFor(ResultType1.class, TEST_ID);
    Assertions.assertThat(resolver.resolveWithConstructor()).isNotNull().isEmpty();
  }

  @Test
  void testReturnOriginalMappingsWhenNoPropertyNamesDefinedAndCannotResolveConstructor() {
    ResultMapping mappingA = createConstructorMappingFor(Object.class, null, "a");
    ResultMapping mappingB = createConstructorMappingFor(Object.class, null, "b");
    ResultMapping mappingC = createConstructorMappingFor(Object.class, null, "c");
    ResultMapping[] constructorMappings = new ResultMapping[] { mappingA, mappingB, mappingC };

    // [backwards-compatibility] the mappings do not have type info, or name defined, the original mappings should be
    // returned
    final ResultMappingConstructorResolver resolver = createResolverFor(ResultType1.class, TEST_ID,
        constructorMappings);
    final List<ResultMapping> mappingList = resolver.resolveWithConstructor();

    assertThat(mappingList).containsExactly(constructorMappings);
  }

  @Test
  void testThrowExceptionWithPartialPropertyNameSpecified() {
    ResultMapping mappingA = createConstructorMappingFor(Object.class, "a", "a");
    ResultMapping mappingB = createConstructorMappingFor(Object.class, null, "b");
    ResultMapping mappingC = createConstructorMappingFor(LocalDate.class, null, "c");

    final ResultMappingConstructorResolver resolver = createResolverFor(ResultType1.class, TEST_ID, mappingA, mappingB,
        mappingC);

    assertThatThrownBy(resolver::resolveWithConstructor).isInstanceOf(BuilderException.class)
        .hasMessageContaining("Either specify all property names, or none.");
  }

  @Test
  void testThrowExceptionWithDuplicatedPropertyNames() {
    ResultMapping mappingA = createConstructorMappingFor(Object.class, "a", "a");
    ResultMapping mappingB = createConstructorMappingFor(Object.class, "a", "b");
    ResultMapping mappingC = createConstructorMappingFor(LocalDate.class, "c", "c");

    final ResultMappingConstructorResolver resolver = createResolverFor(ResultType1.class, TEST_ID, mappingA, mappingB,
        mappingC);

    assertThatThrownBy(resolver::resolveWithConstructor).isInstanceOf(BuilderException.class)
        .hasMessageContaining("Either specify all property names, or none.");
  }

  @Test
  void testCanResolveWithMissingPropertyNameAndAllTypeInfo() {
    ResultMapping mappingA = createConstructorMappingFor(long.class, null, "a");
    ResultMapping mappingB = createConstructorMappingFor(String.class, null, "b");
    ResultMapping mappingC = createConstructorMappingFor(String.class, null, "c");
    ResultMapping[] constructorMappings = new ResultMapping[] { mappingA, mappingB, mappingC };

    final ResultMappingConstructorResolver resolver = createResolverFor(ResultType1.class, TEST_ID,
        constructorMappings);
    final List<ResultMapping> mappingList = resolver.resolveWithConstructor();

    assertThat(mappingList).extracting(ResultMapping::getProperty, mapping -> mapping.getJavaType().getSimpleName())
        .containsExactly(tuple(null, "long"), tuple(null, "String"), tuple(null, "String"));
  }

  @Test
  void doesNotChangeCustomTypeHandlerAfterAutoTypeAndOrdering() {
    ResultMapping mappingA = createConstructorMappingFor(String.class, "a", "a");
    ResultMapping mappingB = createConstructorMappingFor(Object.class, "b", "b");
    ResultMapping mappingC = new ResultMapping.Builder(configuration, "c", "c", Object.class)
        .typeHandler(new MyTypeHandler()).build();

    final ResultMappingConstructorResolver resolver = createResolverFor(CustomObj.class, TEST_ID, mappingB, mappingA,
        mappingC);
    final List<ResultMapping> mappingList = resolver.resolveWithConstructor();

    assertThat(mappingList)
        .extracting(ResultMapping::getProperty, mapping -> mapping.getJavaType().getSimpleName(),
            mapping -> mapping.getTypeHandler().getClass().getSimpleName())
        .containsExactly(tuple("a", "String", "StringTypeHandler"), tuple("b", "int", "IntegerTypeHandler"),
            tuple("c", "List", "MyTypeHandler"));
  }

  @Nested
  class MetaInfoTests {

    @Test
    void resolvesEmptyConstructor() {
      List<ResultMappingConstructorResolver.ConstructorMetaInfo> constructorMetaInfos = new ResultMappingConstructorResolver(
          configuration, List.of(), Result.class, TEST_ID).retrieveConstructorCandidates(0);

      Assertions.assertThat(constructorMetaInfos).isNotNull().hasSize(1);

      ResultMappingConstructorResolver.ConstructorMetaInfo constructorMetaInfo = constructorMetaInfos.get(0);
      Assertions.assertThat(constructorMetaInfo.getArgByOriginalIndex(0)).isNull();
      Assertions.assertThat(constructorMetaInfo.constructorArgs).isEmpty();
    }

    @Test
    void resolvesNormalConstructor() {
      List<ResultMappingConstructorResolver.ConstructorMetaInfo> constructorMetaInfos = new ResultMappingConstructorResolver(
          configuration, List.of(), ResultType.class, TEST_ID).retrieveConstructorCandidates(1);

      assertThat(constructorMetaInfos).isNotNull().hasSize(1).satisfiesExactlyInAnyOrder(
          metaInfo0 -> assertThat(metaInfo0.constructorArgs).extractingFromEntries(Map.Entry::getKey,
              entry -> entry.getValue().getType(), entry -> entry.getValue().getName())
              .containsExactly(tuple("type", String.class, "type")));
    }

    @Test
    void resolvesConstructorsWithParams() {
      List<ResultMappingConstructorResolver.ConstructorMetaInfo> constructorMetaInfos = new ResultMappingConstructorResolver(
          configuration, List.of(), ResultType1.class, TEST_ID).retrieveConstructorCandidates(3);

      assertThat(constructorMetaInfos).isNotNull().hasSize(3).satisfiesExactlyInAnyOrder(
          metaInfo0 -> assertThat(metaInfo0.constructorArgs).extractingFromEntries(Map.Entry::getKey,
              entry -> entry.getValue().getType(), entry -> entry.getValue().getName()).containsExactly(
                  tuple("a1", long.class, "a1"), tuple("b1", long.class, "b1"), tuple("c1", String.class, "c1")),
          metaInfo1 -> assertThat(metaInfo1.constructorArgs).extractingFromEntries(Map.Entry::getKey,
              entry -> entry.getValue().getType(), entry -> entry.getValue().getName()).containsExactly(
                  tuple("a", long.class, "a"), tuple("b", String.class, "b"), tuple("c", String.class, "c")),
          metaInfo1 -> assertThat(metaInfo1.constructorArgs).extractingFromEntries(Map.Entry::getKey,
              entry -> entry.getValue().getType(), entry -> entry.getValue().getName()).containsExactly(
                  tuple("a", long.class, "a"), tuple("b", String.class, "b"), tuple("c", LocalDate.class, "c")));
    }

    @Test
    void resolvesConstructorsWithMixedParams() {
      List<ResultMappingConstructorResolver.ConstructorMetaInfo> constructorMetaInfos = new ResultMappingConstructorResolver(
          configuration, List.of(), ResultType2.class, TEST_ID).retrieveConstructorCandidates(3);

      assertThat(constructorMetaInfos).isNotNull().hasSize(1).satisfiesExactlyInAnyOrder(
          metaInfo0 -> assertThat(metaInfo0.constructorArgs).extractingFromEntries(Map.Entry::getKey,
              entry -> entry.getValue().getType(), entry -> entry.getValue().getName()).containsExactly(
                  tuple("a", long.class, "a"), tuple("b1", long.class, "b1"), tuple("c", String.class, "c")));
    }
  }

  private ResultMappingConstructorResolver createResolverFor(Class<?> resultType, String identifier,
      ResultMapping... mappings) {
    return new ResultMappingConstructorResolver(configuration, mappings == null ? List.of() : Arrays.asList(mappings),
        resultType, identifier);
  }

  private ResultMapping createConstructorMappingFor(Class<?> javaType, String property, String column) {
    return new ResultMapping.Builder(configuration, property, column, javaType).build();
  }
}

record Result() {
}

record ResultType(String type) {
}

record ResultType1(long a, String b, String c) {

  ResultType1(@Param("a1") long a, @Param("b1") long b, @Param("c1") String c) {
    this(a, c, c);
  }

  ResultType1(long a, String b, LocalDate c) {
    this(a, b, c.toString());
  }

  ResultType1(long a, String b, LocalDate c, String d) {
    this(a, b, c.toString());
  }
}

record ResultType2(long a, @Param("b1") long b, String c) {
}

class CustomObj {
  CustomObj(String a, int b, List<String> c) {

  }
}

class MyTypeHandler extends BaseTypeHandler<List<?>> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, List<?> parameter, JdbcType jdbcType)
      throws SQLException {

  }

  @Override
  public List<?> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return List.of();
  }

  @Override
  public List<?> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return List.of();
  }

  @Override
  public List<?> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return List.of();
  }
}
