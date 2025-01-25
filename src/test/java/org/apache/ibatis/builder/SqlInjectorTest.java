package org.apache.ibatis.builder;

import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SqlInjectorTest {
  private final String customMapperMethod = "selectCount";
  private static Configuration configuration;
  private static MapperAnnotationBuilder mapperAnnotationBuilder;

  @BeforeEach
  void setUp() {
    // custom configuration
    configuration = new Configuration() {

      @Override
      public SqlInjector getSqlInjector() {

        // custom sql injector
        return (mapperBuilderAssistant, type) -> {
          LanguageDriver languageDriver = configuration.getDefaultScriptingLanguageInstance();
          SqlSource sqlSource = languageDriver.createSqlSource(configuration, "select count(*) from user", null);
          mapperBuilderAssistant.addMappedStatement(customMapperMethod, sqlSource, StatementType.PREPARED, SqlCommandType.SELECT, null, null,
            null, null, null, Long.class, null, false, true, false,
            null, null, null, configuration.getDatabaseId(), languageDriver, null);
        };
      }
    };
    mapperAnnotationBuilder = new MapperAnnotationBuilder(configuration, AnnotationMapperBuilderTest.Mapper.class);
  }

  @Test
  void testSqlInjector() {
    mapperAnnotationBuilder.parse();
    Assertions.assertNotNull(configuration.getMappedStatement(customMapperMethod));
  }
}
