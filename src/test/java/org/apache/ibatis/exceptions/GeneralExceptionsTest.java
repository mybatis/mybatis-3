package org.apache.ibatis.exceptions;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.datasource.DataSourceException;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.logging.LogException;
import org.apache.ibatis.mapping.SqlMapperException;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.parsing.ParsingException;
import org.apache.ibatis.plugin.PluginException;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.session.SqlSessionException;
import org.apache.ibatis.transaction.TransactionException;
import org.apache.ibatis.type.TypeException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

public class GeneralExceptionsTest {

  private static final String EXPECTED_MESSAGE = "Test Message";
  private static final Exception EXPECTED_CAUSE = new Exception("Nested Exception");

  @Test
  public void should() {
    RuntimeException thrown = ExceptionFactory.wrapException(EXPECTED_MESSAGE, EXPECTED_CAUSE);
    assertTrue("Exception should be wrapped in RuntimeSqlException.", thrown instanceof PersistenceException);
    testThrowException(thrown);
  }

  @Test
  public void shouldInstantiateAndThrowAllCustomExceptions() throws Exception {
    Class[] exceptionTypes = {
        BindingException.class,
        CacheException.class,
        DataSourceException.class,
        ExecutorException.class,
        LogException.class,
        MigrationException.class,
        ParsingException.class,
        BuilderException.class,
        PluginException.class,
        ReflectionException.class,
        PersistenceException.class,
        SqlSessionException.class,
        SqlMapperException.class,
        TransactionException.class,
        TypeException.class
    };
    for (Class exceptionType : exceptionTypes) {
      testExceptionConstructors(exceptionType);
    }

  }

  private void testExceptionConstructors(Class exceptionType) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    Exception e = (Exception) exceptionType.newInstance();
    testThrowException(e);
    e = (Exception) exceptionType.getConstructor(String.class).newInstance(EXPECTED_MESSAGE);
    testThrowException(e);
    e = (Exception) exceptionType.getConstructor(String.class, Throwable.class).newInstance(EXPECTED_MESSAGE, EXPECTED_CAUSE);
    testThrowException(e);
    e = (Exception) exceptionType.getConstructor(Throwable.class).newInstance(EXPECTED_CAUSE);
    testThrowException(e);
  }

  private void testThrowException(Exception thrown) {
    try {
      throw thrown;
    } catch (Exception caught) {
      assertEquals(thrown.getMessage(), caught.getMessage());
      assertEquals(thrown.getCause(), caught.getCause());
    }
  }

}
