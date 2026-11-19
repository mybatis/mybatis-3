/*
 *    Copyright 2026 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.SAXParseException;

class XmlMapperErrorLocationTest {
  private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"mybatis-3-mapper.dtd\">\n"
      + "<mapper namespace=\"diagnostic\">\n" + "<select id=broken\" resultType=\"int\">select 1</select>\n</mapper>";
  private static final String RESOURCE = "org/apache/ibatis/builder/XmlMapperErrorLocationTest$BrokenMapper.xml";

  @AfterEach
  void resetErrorContext() {
    ErrorContext.instance().reset();
  }

  @ParameterizedTest
  @ValueSource(strings = { "mappers/Broken.xml", "file [C:/mapper files/Broken.xml]",
      "jar:file:/application.jar!/mappers/Broken.xml" })
  void shouldIdentifyMalformedMapperFromInputStream(String resource) {
    Configuration configuration = new Configuration();
    assertLocation(() -> new XMLMapperBuilder(new ByteArrayInputStream(XML.getBytes(StandardCharsets.UTF_8)),
        configuration, resource, configuration.getSqlFragments()), resource, 4);
  }

  @ParameterizedTest
  @ValueSource(strings = { "mappers/Broken.xml", "file [C:/mapper files/Broken.xml]",
      "jar:file:/application.jar!/mappers/Broken.xml" })
  void shouldIdentifyMalformedMapperFromReader(String resource) {
    Configuration configuration = new Configuration();
    assertLocation(
        () -> new XMLMapperBuilder(new StringReader(XML), configuration, resource, configuration.getSqlFragments()),
        resource, 4);
  }

  @Test
  void shouldIdentifyXmlWhenRegisteringMapperInterface() {
    assertLocation(() -> new Configuration().addMapper(BrokenMapper.class), RESOURCE, 23);
  }

  @Test
  void shouldIdentifyXmlWhenLoadingConfiguration() {
    String configuration = "<!DOCTYPE configuration PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" "
        + "\"https://mybatis.org/dtd/mybatis-3-config.dtd\">" + "<configuration><mappers><mapper resource=\"" + RESOURCE
        + "\"/></mappers></configuration>";
    RuntimeException error = assertThrows(RuntimeException.class,
        () -> new SqlSessionFactoryBuilder().build(new StringReader(configuration)));
    assertThat(error).hasMessageContaining(RESOURCE);
    assertThat(error).hasRootCauseInstanceOf(SAXParseException.class);
    Throwable cause = error;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    assertThat(((SAXParseException) cause).getPublicId()).isEqualTo(RESOURCE);
  }

  @ParameterizedTest
  @ValueSource(strings = { "mappers/Broken.xml", "file [C:/mapper files/Broken.xml]",
      "jar:file:/application.jar!/mappers/Broken.xml" })
  void shouldKeepLoadingValidMapperWithRelativeDtd(String resource) {
    Configuration configuration = new Configuration();
    new XMLMapperBuilder(new StringReader(XML.replace("id=broken", "id=\"broken")), configuration, resource,
        configuration.getSqlFragments()).parse();
    assertThat(configuration.hasStatement("diagnostic.broken")).isTrue();
  }

  private void assertLocation(Executable parse, String resource, int line) {
    BuilderException error = assertThrows(BuilderException.class, parse);
    assertThat(error).hasMessageContaining(resource);
    assertThat(error.getCause()).isInstanceOf(SAXParseException.class);
    SAXParseException cause = (SAXParseException) error.getCause();
    assertThat(cause.getPublicId()).isEqualTo(resource);
    assertThat(cause.getSystemId()).isNull();
    assertThat(cause.getLineNumber()).isEqualTo(line);
    assertThat(cause.getColumnNumber()).isPositive();
  }

  interface BrokenMapper {
  }
}
