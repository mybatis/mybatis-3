package org.apache.ibatis.builder;

import java.util.List;

import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;

public class ResultMapResolver {
  private final MapperBuilderAssistant assistant;
  private String id;
  private Class<?> type;
  private String extend;
  private Discriminator discriminator;
  private String notNullColumn;
  private List<ResultMapping> resultMappings;

  public ResultMapResolver(MapperBuilderAssistant assistant, String id, Class<?> type, String extend, Discriminator discriminator, String notNullColumn,
      List<ResultMapping> resultMappings) {
    this.assistant = assistant;
    this.id = id;
    this.type = type;
    this.extend = extend;
    this.discriminator = discriminator;
    this.notNullColumn = notNullColumn;
    this.resultMappings = resultMappings;
  }

  public ResultMap resolve() {
    return assistant.addResultMap(this.id, this.type, this.extend, this.discriminator, this.notNullColumn, this.resultMappings);
  }
  
}