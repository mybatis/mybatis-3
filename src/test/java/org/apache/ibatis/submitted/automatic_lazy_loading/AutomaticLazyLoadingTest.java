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
package org.apache.ibatis.submitted.automatic_lazy_loading;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AutomaticLazyLoadingTest {

  private SqlSession sqlSession;

  private static List<Method> noMethods; 
  private static List<Method> objectMethods;
  
  @BeforeClass
  public static void beforeClass() 
  throws Exception {
    noMethods = new ArrayList<Method>();

    objectMethods = new ArrayList<Method>();
    objectMethods.add(Element.class.getMethod("hashCode"));
    objectMethods.add(Element.class.getMethod("equals", Object.class));
    objectMethods.add(Element.class.getMethod("toString"));
  }

  /**
   * Load an element with 'default' configuration.
   * Expect an exception when none of the 'object methods' is called.
   */
  @Test
  public void selectElementValue_default_nomethods()
  throws Exception {
    testScenario("default", true, noMethods);
  }

  /**
   * Load an element with 'default' configuration.
   * Expect no exception when one of the 'object methods' is called.
   */
  @Test
  public void selectElementValue_default_objectmethods()
  throws Exception {
  testScenario("default", false, objectMethods);
  }

  /**
   * Load an element with 'disabled' configuration.
   * Expect an exception when none of the 'object methods' is called.
   */
  @Test
  public void selectElementValue_disabled_nomethods()
  throws Exception {
    testScenario("disabled", true, noMethods);
  }

  /**
   * Load an element with 'disabled' configuration.
   * Expect an exception when one of the 'object methods' is called.
   * (because calling object methods should not trigger lazy loading)
   */
  @Test
  public void selectElementValue_disabled_objectmethods()
  throws Exception {
  testScenario("disabled", true, objectMethods);
  }

  /**
   * Load an element with 'enabled' configuration.
   * Expect an exception when none of the 'object methods' is called.
   */
  @Test
  public void selectElementValue_enabled_nomethods()
  throws Exception {
    testScenario("enabled", true, noMethods);
  }

  /**
   * Load an element with 'enabled' configuration.
   * Expect no exception when one of the 'object methods' is called.
   */
  @Test
  public void selectElementValue_enabled_objectmethods()
  throws Exception {
  testScenario("enabled", false, objectMethods);
  }
   
  /**
   * Load an element with default configuration and call toString.
   * Expect that the toString triggers lazy loading which loads the nested Element
   * is available after closing the session.
   */
  private void testScenario(String aConfiguration, boolean anExpectingAnException, List<Method> aMethodsToCall)
  throws Exception {
  if (aMethodsToCall.isEmpty()) {
    testScenario(aConfiguration, anExpectingAnException, (Method)null);
  } else {
    for (Method myMethod : aMethodsToCall) {
      testScenario(aConfiguration, anExpectingAnException, myMethod);
    }
  }
 }
  
  /**
   * Load an element with default configuration and call toString.
   * Expect that the toString triggers lazy loading which loads the nested Element
   * is available after closing the session.
   */
  private void testScenario(String aConfiguration, boolean anExpectingAnException, Method aMethodToCall)
  throws Exception {
    openSession(aConfiguration);

    Element myElement = selectElement();

    String myMethodCallDescription;
    
    if (aMethodToCall != null) {
      Object[] myArguments = new Object[aMethodToCall.getParameterTypes().length];
      
      aMethodToCall.invoke(myElement, myArguments);
      
      myMethodCallDescription = aMethodToCall.toString();
    } else {
      myMethodCallDescription = "not called a method";
    }
    
    myElement = disableLazyLoaders(myElement);
    
    closeSession();
    
    try {
      Assert.assertEquals("parent", myElement.getElement().getValue());
      
        if (anExpectingAnException) {
          Assert.fail("Excpected an exception, since " + myMethodCallDescription + " should not trigger lazy loading");
        }
    } catch(Exception anException) {
        if (!anExpectingAnException) {
          Assert.fail("Did not expect an exception, because " + myMethodCallDescription + " should trigger lazy loading");
        }
    }
  }
  
  /**
   * Create a session with the specified configuration and initialized the database.
   */
  private void openSession(String aConfig) throws Exception {
    final String resource = "org/apache/ibatis/submitted/automatic_lazy_loading/ibatis-automatic-lazy-load-" + aConfig + ".xml";
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
    Reader createScriptReader = Resources.getResourceAsReader("org/apache/ibatis/submitted/automatic_lazy_loading/create.sql");
    runner.runScript(createScriptReader);

    sqlSession = sqlSessionFactory.openSession();
  }

  private void closeSession() {
    if (sqlSession != null) {
      sqlSession.close();
    }
  }

  /**
   * Select the child element.
   */
  private Element selectElement() {
    Element myElement = sqlSession.getMapper(ElementMapper.class).selectElementById("child");
    
    Assert.assertNotNull("Test setup failure; Could not load element", myElement);
    
    return myElement;
  }


  /**
   * Disable lazy loaders by serializing / deserializing the element
   */
  private Element disableLazyLoaders(Element anElement) 
  throws Exception {
    ByteArrayOutputStream myBuffer = new ByteArrayOutputStream();
    
    ObjectOutputStream myObjectOutputStream = new ObjectOutputStream(myBuffer);
    myObjectOutputStream.writeObject(anElement);
    myObjectOutputStream.close();
    
    myBuffer.close();
    
    ObjectInputStream myObjectInputStream = new ObjectInputStream(new ByteArrayInputStream(myBuffer.toByteArray()));
    Element myResult = (Element)myObjectInputStream.readObject();
    myObjectInputStream.close();
    
    return myResult;
  }
}