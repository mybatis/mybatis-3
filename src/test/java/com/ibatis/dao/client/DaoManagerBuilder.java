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
package com.ibatis.dao.client;

import com.ibatis.dao.engine.builder.xml.XmlDaoManagerBuilder;

import java.io.Reader;
import java.util.Properties;

/*
 * Builds a DaoManager given a Reader (dao.xml) and optionally a set of
 * properties.  The following is an example of a dao.xml file.
 * <ul>
 * <li> <a href="dao.txt">The DAO Configuration file (dao.xml)</a>
 * <ul>
 * <p/>
 */
public class DaoManagerBuilder {

  /*
   * No instantiation allowed.
   */
  private DaoManagerBuilder() {
  }

  /*
   * Builds a DAO Manager from the reader supplied.
   *
   * @param reader A Reader instance that reads a DAO configuration file (dao.xml).
   * @param props  A Properties instance that will be used for parameter substitution
   *               in the DAO configuration file (dao.xml).
   * @return A configured DaoManager instance
   */
  public static DaoManager buildDaoManager(Reader reader, Properties props)
      throws DaoException {
    return new XmlDaoManagerBuilder().buildDaoManager(reader, props);
  }

  /*
   * Builds a DAO Manager from the reader supplied.
   *
   * @param reader A Reader instance that reads a DAO configuration file (dao.xml).
   * @return A configured DaoManager instance
   */
  public static DaoManager buildDaoManager(Reader reader) {
    return new XmlDaoManagerBuilder().buildDaoManager(reader);
  }


}
