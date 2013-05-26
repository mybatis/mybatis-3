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
package com.ibatis.sqlmap.engine.datasource;


import com.ibatis.common.jdbc.DbcpConfiguration;

import javax.sql.DataSource;
import java.util.Map;

/*
 * DataSourceFactory implementation for DBCP
 */
public class DbcpDataSourceFactory implements DataSourceFactory {

  private DataSource dataSource;

  public void initialize(Map map) {
    DbcpConfiguration dbcp = new DbcpConfiguration(map);
    dataSource = dbcp.getDataSource();
  }

  public DataSource getDataSource() {
    return dataSource;
  }

}



