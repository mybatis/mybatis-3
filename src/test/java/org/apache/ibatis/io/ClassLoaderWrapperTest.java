package org.apache.ibatis.io;

import org.apache.ibatis.BaseDataTest;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

public class ClassLoaderWrapperTest extends BaseDataTest {

  ClassLoaderWrapper wrapper;
  ClassLoader loader;
  private final String RESOURCE_NOT_FOUND = "some_resource_that_does_not_exist.properties";
  private final String CLASS_NOT_FOUND = "some.random.class.that.does.not.Exist";
  private final String CLASS_FOUND = "java.lang.Object";


  @Before
  public void beforeClassLoaderWrapperTest() {
    wrapper = new ClassLoaderWrapper();
    loader = getClass().getClassLoader();
  }

  @Test
  public void classForName() throws ClassNotFoundException {
    assertNotNull(wrapper.classForName(CLASS_FOUND));
  }

  @Test(expected = ClassNotFoundException.class)
  public void classForNameNotFound() throws ClassNotFoundException {
    assertNotNull(wrapper.classForName(CLASS_NOT_FOUND));
  }

  @Test
  public void classForNameWithClassLoader() throws ClassNotFoundException {
    assertNotNull(wrapper.classForName(CLASS_FOUND, loader));
  }

  @Test
  public void getResourceAsURL() {
    assertNotNull(wrapper.getResourceAsURL(JPETSTORE_PROPERTIES));
  }

  @Test
  public void getResourceAsURLNotFound() {
    assertNull(wrapper.getResourceAsURL(RESOURCE_NOT_FOUND));
  }

  @Test
  public void getResourceAsURLWithClassLoader() {
    assertNotNull(wrapper.getResourceAsURL(JPETSTORE_PROPERTIES, loader));
  }

  @Test
  public void getResourceAsStream() {
    assertNotNull(wrapper.getResourceAsStream(JPETSTORE_PROPERTIES));
  }

  @Test
  public void getResourceAsStreamNotFound() {
    assertNull(wrapper.getResourceAsStream(RESOURCE_NOT_FOUND));
  }

  @Test
  public void getResourceAsStreamWithClassLoader() {
    assertNotNull(wrapper.getResourceAsStream(JPETSTORE_PROPERTIES, loader));
  }

}
