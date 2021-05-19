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
package org.apache.ibatis.submitted.language;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;

/**
 * Just a test case. Not a real Velocity implementation.
 */
public class VelocitySqlSource implements SqlSource {

  public static final String PARAMETER_OBJECT_KEY = "_parameter";
  public static final String DATABASE_ID_KEY = "_databaseId";

  private final Configuration configuration;
  private final Template script;

  static {
    Velocity.setProperty("runtime.log", "target/velocity.log");
    Velocity.init();
  }

  public VelocitySqlSource(Configuration configuration, String scriptText) {
    this.configuration = configuration;
    try {
      RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
      StringReader reader = new StringReader(scriptText);
      Template template = new Template();
      template.setName("Template name");
      SimpleNode node = runtimeServices.parse(reader, template);
      script = new Template();
      script.setRuntimeServices(runtimeServices);
      script.setData(node);
      script.initDocument();
    } catch (Exception ex) {
      throw new BuilderException("Error parsing velocity script", ex);
    }
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    Map<String, Object> bindings = createBindings(parameterObject, configuration);
    VelocityContext context = new VelocityContext(bindings);
    StringWriter sw = new StringWriter();
    script.merge(context, sw);
    VelocitySqlSourceBuilder sqlSourceParser = new VelocitySqlSourceBuilder(configuration);
    Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
    SqlSource sqlSource = sqlSourceParser.parse(sw.toString(), parameterType);
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    for (Map.Entry<String, Object> entry : bindings.entrySet()) {
      boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
    }
    return boundSql;

  }

  public static Map<String, Object> createBindings(Object parameterObject, Configuration configuration) {
    Map<String, Object> bindings = new HashMap<>();
    bindings.put(PARAMETER_OBJECT_KEY, parameterObject);
    bindings.put(DATABASE_ID_KEY, configuration.getDatabaseId());
    bindings.put("it", new IteratorParameter(bindings));
    return bindings;
  }

  public static class IteratorParameter {

    private static final String PREFIX = "__frch_";
    private int count = 0;
    private final Map<String, Object> bindings;

    public IteratorParameter(Map<String, Object> bindings) {
      this.bindings = bindings;
    }

    public String next(Object prop) {
      StringBuilder sb = new StringBuilder();
      String name = sb.append(PREFIX).append("_ITEM").append("_").append(count++).toString();
      bindings.put(name, prop);
      return name;
    }
  }
}
