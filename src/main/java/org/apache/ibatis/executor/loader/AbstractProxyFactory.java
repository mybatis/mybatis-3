package org.apache.ibatis.executor.loader;

import org.apache.ibatis.reflection.property.PropertyNamer;

import java.sql.SQLException;
import java.util.Set;

/**
 * @author wangjie
 */
public class AbstractProxyFactory {

  public static void lazyLoaderInject(String methodName, ResultLoaderMap lazyLoader, String finalizeMethod, boolean aggressive, Set<String> lazyLoadTriggerMethods) throws SQLException {
    if (lazyLoader.size() > 0 && !finalizeMethod.equals(methodName)) {
      if (aggressive || lazyLoadTriggerMethods.contains(methodName)) {
        lazyLoader.loadAll();
      } else if (PropertyNamer.isSetter(methodName)) {
        final String property = PropertyNamer.methodToProperty(methodName);
        lazyLoader.remove(property);
      } else if (PropertyNamer.isGetter(methodName)) {
        final String property = PropertyNamer.methodToProperty(methodName);
        if (lazyLoader.hasLoader(property)) {
          lazyLoader.load(property);
        }
      }
    }
  }
}
