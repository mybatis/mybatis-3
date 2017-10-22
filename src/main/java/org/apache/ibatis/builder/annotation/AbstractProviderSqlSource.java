/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.builder.annotation;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 * @author liuzh
 * @since 3.4.6
 */
public abstract class AbstractProviderSqlSource implements SqlSource {

  protected final Configuration configuration;
  protected final SqlSourceBuilder sqlSourceParser;
  protected final Class<?> providerType;
  protected Method providerMethod;
  protected String[] providerMethodArgumentNames;
  protected Class<?>[] providerMethodParameterTypes;
  protected ProviderContext providerContext;
  protected Integer providerContextIndex;

  /**
   * @since 3.5.0
   */
  public AbstractProviderSqlSource(Configuration configuration, Object provider, Class<?> mapperType, Method mapperMethod) {
    String providerMethodName;
    try {
      this.configuration = configuration;
      this.sqlSourceParser = new SqlSourceBuilder(configuration);
      this.providerType = (Class<?>) provider.getClass().getMethod("type").invoke(provider);
      providerMethodName = (String) provider.getClass().getMethod("method").invoke(provider);

      for (Method m : this.providerType.getMethods()) {
        if (providerMethodName.equals(m.getName()) && CharSequence.class.isAssignableFrom(m.getReturnType())) {
          if (providerMethod != null){
            throw new BuilderException("Error creating SqlSource for SqlProvider. Method '"
                + providerMethodName + "' is found multiple in SqlProvider '" + this.providerType.getName()
                + "'. Sql provider method can not overload.");
          }
          this.providerMethod = m;
          this.providerMethodArgumentNames = new ParamNameResolver(configuration, m).getNames();
          this.providerMethodParameterTypes = m.getParameterTypes();
        }
      }
    } catch (BuilderException e) {
      throw e;
    } catch (Exception e) {
      throw new BuilderException("Error creating SqlSource for SqlProvider.  Cause: " + e, e);
    }
    if (this.providerMethod == null) {
      throw new BuilderException("Error creating SqlSource for SqlProvider. Method '"
          + providerMethodName + "' not found in SqlProvider '" + this.providerType.getName() + "'.");
    }
    for (int i = 0; i< this.providerMethodParameterTypes.length; i++) {
      Class<?> parameterType = this.providerMethodParameterTypes[i];
      if (parameterType == ProviderContext.class) {
        if (this.providerContext != null){
          throw new BuilderException("Error creating SqlSource for SqlProvider. ProviderContext found multiple in SqlProvider method ("
              + this.providerType.getName() + "." + providerMethod.getName()
              + "). ProviderContext can not define multiple in SqlProvider method argument.");
        }
        this.providerContext = new ProviderContext(mapperType, mapperMethod);
        this.providerContextIndex = i;
      }
    }
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    SqlSource sqlSource = createSqlSource(parameterObject);
    return sqlSource.getBoundSql(parameterObject);
  }

  public abstract SqlSource createSqlSource(Object parameterObject);
}
