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
package org.apache.ibatis.submitted.inline_association_with_dot;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.sql.Connection;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Test;

public class InlineCollectionWithDotTest {

  private SqlSession sqlSession;

  public void openSession(String aConfig) throws Exception {

	final String resource = "org/apache/ibatis/submitted/inline_association_with_dot/ibatis-" + aConfig + ".xml";
	Reader batisConfigReader = Resources.getResourceAsReader(resource);

	SqlSessionFactory sqlSessionFactory;
	try {
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(batisConfigReader);
	} catch(Exception anException) {
		throw new RuntimeException("Mapper configuration failed, expected this to work: " + anException.getMessage(), anException);
	}

	SqlSession session = sqlSessionFactory.openSession();

	Connection conn = session.getConnection();
	ScriptRunner runner = new ScriptRunner(conn);
	runner.setLogWriter(null);
	runner.setErrorLogWriter(null);
	Reader createScriptReader = Resources.getResourceAsReader("org/apache/ibatis/submitted/inline_association_with_dot/create.sql");
	runner.runScript(createScriptReader);

	sqlSession = sqlSessionFactory.openSession();
  }

  @After
  public void closeSession() {
	  if (sqlSession != null) {
		  sqlSession.close();
	  }
  }

  /*
   * Load an element with an element with and element with a value. Expect that this is
   * possible bij using an inline 'association' map.
   */
  @Test
  public void selectElementValueInContainerUsingInline()
  throws Exception {
    openSession("inline");

    Element myElement = sqlSession.getMapper(ElementMapperUsingInline.class).selectElement();

    assertEquals("value", myElement.getElement().getElement().getValue());
  }

  /*
   * Load an element with an element with and element with a value. Expect that this is
   * possible bij using an sub-'association' map.
   */
  @Test
  public void selectElementValueInContainerUsingSubMap()
  throws Exception {
	 openSession("submap");

	 Element myElement = sqlSession.getMapper(ElementMapperUsingSubMap.class).selectElement();

     assertEquals("value", myElement.getElement().getElement().getValue());
  }
}