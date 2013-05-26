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
package com.ibatis.sqlmap.engine.builder;

import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.ibatis.sqlmap.engine.datasource.DataSourceFactory;
import com.ibatis.sqlmap.engine.transaction.TransactionConfig;
import com.ibatis.sqlmap.engine.transaction.TransactionManager;
import com.ibatis.common.util.NodeEvent;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.parsing.XNode;
import com.ibatis.common.util.NodeEventParser;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class XmlSqlMapConfigParser {

  private Reader reader;
  private NodeEventParser parser = new NodeEventParser();

  private Ibatis2Configuration config = new Ibatis2Configuration();
  private Properties dataSourceProps = new Properties();
  private Properties transactionManagerProps = new Properties();
  private boolean useStatementNamespaces;

  private Map<String, XNode> sqlFragments = new HashMap<String, XNode>();

  public XmlSqlMapConfigParser(Reader reader) {
    this.reader = reader;
    this.parser.addNodeletHandler(this);
    this.useStatementNamespaces = false;
    this.parser.setEntityResolver(new SqlMapEntityResolver());
  }

  public XmlSqlMapConfigParser(Reader reader, Properties props) {
    this(reader);
    this.config.setVariables(props);
    this.parser.setVariables(props);
    this.parser.setEntityResolver(new SqlMapEntityResolver());
  }

  public void parse() {
    parser.parse(reader);
  }

  public boolean hasSqlFragment(String id) {
    return sqlFragments.containsKey(id);
  }

  public XNode getSqlFragment(String id) {
    return sqlFragments.get(id);
  }

  public void addSqlFragment(String id, XNode context) {
    sqlFragments.put(id, context);
  }

  public Ibatis2Configuration getConfiguration() {
    return config;
  }

  public boolean isUseStatementNamespaces() {
    return useStatementNamespaces;
  }

  @NodeEvent("/sqlMapConfig/properties")
  public void sqlMapConfigproperties(XNode context) throws Exception {
    String resource = context.getStringAttribute("resource");
    String url = context.getStringAttribute("url");
    Properties fileVariables;
    if (resource != null) {
      fileVariables = Resources.getResourceAsProperties(resource);
    } else if (url != null) {
      fileVariables = Resources.getUrlAsProperties(url);
    } else {
      throw new RuntimeException("The properties element requires either a resource or a url attribute.");
    }
    // Override file variables with those passed in programmatically
    Properties passedVariables = config.getVariables();
    if (passedVariables != null) {
      fileVariables.putAll(passedVariables);
    }
    config.setVariables(fileVariables);
    parser.setVariables(fileVariables);
  }

  @NodeEvent("/sqlMapConfig/settings")
  public void sqlMapConfigsettings(XNode context) throws Exception {
    boolean classInfoCacheEnabled = context.getBooleanAttribute("classInfoCacheEnabled", true);
    MetaClass.setClassCacheEnabled(classInfoCacheEnabled);

    boolean lazyLoadingEnabled = context.getBooleanAttribute("lazyLoadingEnabled", true);
    config.setLazyLoadingEnabled(lazyLoadingEnabled);

    boolean statementCachingEnabled = context.getBooleanAttribute("statementCachingEnabled", true);
    if (statementCachingEnabled) {
      config.setDefaultExecutorType(ExecutorType.REUSE);
    }

    boolean batchUpdatesEnabled = context.getBooleanAttribute("batchUpdatesEnabled", true);
    if (batchUpdatesEnabled) {
      config.setDefaultExecutorType(ExecutorType.BATCH);
    }

    boolean cacheModelsEnabled = context.getBooleanAttribute("cacheModelsEnabled", true);
    config.setCacheEnabled(cacheModelsEnabled);

    boolean useColumnLabel = context.getBooleanAttribute("useColumnLabel", false);
    config.setUseColumnLabel(useColumnLabel);

    boolean forceMultipleResultSetSupport = context.getBooleanAttribute("forceMultipleResultSetSupport", true);
    config.setMultipleResultSetsEnabled(forceMultipleResultSetSupport);

    useStatementNamespaces = context.getBooleanAttribute("useStatementNamespaces", false);

    Integer defaultTimeout = context.getIntAttribute("defaultStatementTimeout");
    config.setDefaultStatementTimeout(defaultTimeout);
  }

  @NodeEvent("/sqlMapConfig/typeAlias")
  public void sqlMapConfigtypeAlias(XNode context) throws Exception {
    String alias = context.getStringAttribute("alias");
    String type = context.getStringAttribute("type");
    config.getTypeAliasRegistry().registerAlias(alias, type);
  }

  @NodeEvent("/sqlMapConfig/typeHandler")
  public void sqlMapConfigtypeHandler(XNode context) throws Exception {
    String jdbcType = context.getStringAttribute("jdbcType");
    String javaType = context.getStringAttribute("javaType");
    String callback = context.getStringAttribute("callback");

    if (javaType != null && callback != null) {
      JdbcType jdbcTypeEnum = JdbcType.valueOf(jdbcType);
      Class javaTypeClass = config.getTypeAliasRegistry().resolveAlias(javaType);
      Class callbackClass = config.getTypeAliasRegistry().resolveAlias(callback);
      Object o = callbackClass.newInstance();
      if (o instanceof TypeHandlerCallback) {
        TypeHandler typeHandler = new TypeHandlerCallbackAdapter((TypeHandlerCallback) o);
        config.getTypeHandlerRegistry().register(javaTypeClass, jdbcTypeEnum, typeHandler);
      }
    }
  }

  @NodeEvent("/sqlMapConfig/transactionManager/end()")
  public void sqlMapConfigtransactionManagerend(XNode context) throws Exception {
    String type = context.getStringAttribute("type");
    Class txClass = config.getTypeAliasRegistry().resolveAlias(type);
    boolean commitRequired = context.getBooleanAttribute("commitRequired", false);

    TransactionConfig txConfig = (TransactionConfig) txClass.newInstance();
    txConfig.setDataSource(config.getDataSource());
    txConfig.setProperties(transactionManagerProps);
    txConfig.setForceCommit(commitRequired);
    config.setTransactionManager(new TransactionManager(config, txConfig));
  }

  @NodeEvent("/sqlMapConfig/transactionManager/property")
  public void sqlMapConfigtransactionManagerproperty(XNode context) throws Exception {
    String name = context.getStringAttribute("name");
    String value = context.getStringAttribute("value");
    transactionManagerProps.setProperty(name, value);
  }

  @NodeEvent("/sqlMapConfig/transactionManager/dataSource/property")
  public void sqlMapConfigtransactionManagerdataSourceproperty(XNode context) throws Exception {
    String name = context.getStringAttribute("name");
    String value = context.getStringAttribute("value");
    dataSourceProps.setProperty(name, value);
  }

  @NodeEvent("/sqlMapConfig/transactionManager/dataSource/end()")
  public void sqlMapConfigtransactionManagerdataSourceend(XNode context) throws Exception {
    String type = context.getStringAttribute("type");
    Class dataSourceClass = config.getTypeAliasRegistry().resolveAlias(type);
    DataSourceFactory dsFactory = (DataSourceFactory) dataSourceClass.newInstance();
    dsFactory.initialize(dataSourceProps);
    config.setDataSource(dsFactory.getDataSource());
  }

  @NodeEvent("/sqlMapConfig/resultObjectFactory")
  public void sqlMapConfigresultObjectFactory(XNode context) throws Exception {
    String type = context.getStringAttribute("type");
    Class factoryClass = Class.forName(type);
    ObjectFactory factory = (ObjectFactory) factoryClass.newInstance();
    Properties props = context.getChildrenAsProperties();
    factory.setProperties(props);
    config.setObjectFactory(factory);
  }

  @NodeEvent("/sqlMapConfig/sqlMap")
  public void sqlMapConfigsqlMap(XNode context) throws Exception {
    String resource = context.getStringAttribute("resource");
    String url = context.getStringAttribute("url");

    Reader reader = null;
    if (resource != null) {
      reader = Resources.getResourceAsReader(resource);
    } else if (url != null) {
      reader = Resources.getUrlAsReader(url);
    } else {
      throw new SqlMapException("The sqlMap element requires either a resource or a url attribute.");
    }
    new XmlSqlMapParser(this, reader).parse();
  }

}
